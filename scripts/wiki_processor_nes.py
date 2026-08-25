#!/usr/bin/env python3
"""Convert the NES wiki HTML file into a Retroboy area-grouped JSON file.

Usage:
    python scripts/wiki_processor_nes.py
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass, field
from html import unescape
from html.parser import HTMLParser
from pathlib import Path
from typing import Iterable

AREA_ORDER = ("JPN", "USA", "PAL")
NES_RELEASE_COLUMNS = {
    "JPN": 4,  # JP column in the NES Wikipedia table
    "USA": 5,  # NA column in the NES Wikipedia table
    "PAL": 6,  # PAL column in the NES Wikipedia table
}
TITLE_AREA_ALIASES = {
    "JP": "JPN",
    "JPN": "JPN",
    "NA": "USA",
    "US": "USA",
    "USA": "USA",
    "PAL": "PAL",
    "EU": "PAL",
    "EUR": "PAL",
    "UK": "PAL",
    "GB": "PAL",
    "AU": "PAL",
    "AUS": "PAL",
    "FR": "PAL",
    "FRA": "PAL",
    "ES": "PAL",
    "ESP": "PAL",
    "DE": "PAL",
    "GER": "PAL",
    "IT": "PAL",
    "ITA": "PAL",
}
UNRELEASED_WORDS = ("unreleased", "cancelled", "canceled")
TITLE_AREA_TOKEN_PATTERN = "|".join(
    re.escape(token) for token in sorted(TITLE_AREA_ALIASES, key=len, reverse=True)
)


@dataclass
class HtmlCell:
    tag: str
    attrs: dict[str, str]
    lines: list[str]

    @property
    def text(self) -> str:
        return " ".join(self.lines)


@dataclass
class HtmlRow:
    cells: list[HtmlCell] = field(default_factory=list)


@dataclass(frozen=True)
class WikiTitleEntry:
    title: str
    publisher: str


class WikiTableParser(HTMLParser):
    """Small HTML table parser tailored for exported MediaWiki table HTML.

    The project should not need a third-party dependency just to process checked-in
    wiki exports. This parser keeps enough table structure to support the NES
    table and similar future platform-specific processors.
    """

    def __init__(self, table_id: str | None = None) -> None:
        super().__init__(convert_charrefs=True)
        self.table_id = table_id
        self.rows: list[HtmlRow] = []
        self._table_depth = 0
        self._in_target_table = False
        self._current_row: HtmlRow | None = None
        self._current_cell: dict[str, object] | None = None

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        attrs_dict = {name: value or "" for name, value in attrs}

        if tag == "table":
            if self._in_target_table:
                self._table_depth += 1
            elif self.table_id is None or attrs_dict.get("id") == self.table_id:
                self._in_target_table = True
                self._table_depth = 1
            return

        if not self._in_target_table:
            return

        if tag == "tr" and self._table_depth == 1:
            self._current_row = HtmlRow()
        elif tag in {"td", "th"} and self._current_row is not None:
            self._current_cell = {
                "tag": tag,
                "attrs": attrs_dict,
                "lines": [[]],
            }
        elif tag == "sup" and self._current_cell is not None:
            lines = self._current_cell["lines"]
            assert isinstance(lines, list)
            if lines[-1] and not str(lines[-1][-1]).endswith(" "):
                lines[-1].append(" ")
        elif tag == "br" and self._current_cell is not None:
            lines = self._current_cell["lines"]
            assert isinstance(lines, list)
            if lines[-1]:
                lines.append([])

    def handle_data(self, data: str) -> None:
        if self._current_cell is None:
            return
        text = unescape(data).replace("\xa0", " ")
        if not text:
            return
        lines = self._current_cell["lines"]
        assert isinstance(lines, list)
        lines[-1].append(text)

    def handle_endtag(self, tag: str) -> None:
        if not self._in_target_table:
            return

        if tag in {"td", "th"} and self._current_cell is not None:
            raw_lines = self._current_cell["lines"]
            assert isinstance(raw_lines, list)
            cleaned_lines = [normalize_text("".join(parts)) for parts in raw_lines]
            cleaned_lines = [line for line in cleaned_lines if line]
            self._current_row.cells.append(
                HtmlCell(
                    tag=str(self._current_cell["tag"]),
                    attrs=dict(self._current_cell["attrs"]),
                    lines=cleaned_lines,
                )
            )
            self._current_cell = None
        elif tag == "tr" and self._current_row is not None:
            if self._current_row.cells:
                self.rows.append(self._current_row)
            self._current_row = None
        elif tag == "table":
            self._table_depth -= 1
            if self._table_depth <= 0:
                self._in_target_table = False


def normalize_text(value: str) -> str:
    value = unescape(value).replace("\xa0", " ")
    value = re.sub(r"\s+", " ", value)
    return value.strip()


def parse_table(html: str, table_id: str | None) -> list[HtmlRow]:
    parser = WikiTableParser(table_id=table_id)
    parser.feed(html)
    parser.close()
    return parser.rows


def is_released(cell: HtmlCell) -> bool:
    text = cell.text.casefold()
    class_names = set(cell.attrs.get("class", "").split())
    if "table-na" in class_names:
        return False
    if any(word in text for word in UNRELEASED_WORDS):
        return False
    return bool(text)


def split_area_marker(marker: str) -> list[str]:
    areas: list[str] = []
    for part in re.split(r"[/,]", marker):
        area = TITLE_AREA_ALIASES.get(part.strip().upper())
        if area and area not in areas:
            areas.append(area)
    return areas


def parse_title_lines(title_cell: HtmlCell) -> tuple[list[str], list[tuple[str, list[str]]]]:
    """Return unmarked titles and explicitly region-marked titles.

    MediaWiki title cells often look like:
        1943: The Battle of Midway<br>1943: The Battle of Valhalla<sup>JP</sup>
    In that case the unmarked title is assigned to released areas that do not
    already have explicit title variants, while the marked title goes to JPN.
    """

    unmarked_titles: list[str] = []
    marked_titles: list[tuple[str, list[str]]] = []
    marker_pattern = re.compile(
        rf"^(?P<title>.+?)\s+(?P<marker>(?:{TITLE_AREA_TOKEN_PATTERN})(?:/(?:{TITLE_AREA_TOKEN_PATTERN}))*)$",
        re.IGNORECASE,
    )

    for line in title_cell.lines:
        title = re.sub(r"\[[^\]]+\]", "", normalize_text(line))
        if not title:
            continue

        match = marker_pattern.match(title)
        if match:
            parsed_title = normalize_text(match.group("title"))
            areas = split_area_marker(match.group("marker"))
            if parsed_title and areas:
                marked_titles.append((parsed_title, areas))
            continue

        unmarked_titles.append(title)

    return unmarked_titles, marked_titles


def clean_publisher(cell: HtmlCell) -> str:
    publisher = re.sub(r"\[[^\]]+\]", "", cell.text)
    return normalize_text(publisher)


def disambiguate_duplicate_titles(entries: list[WikiTitleEntry]) -> list[str]:
    title_counts: dict[str, int] = {}
    for entry in entries:
        title_counts[entry.title] = title_counts.get(entry.title, 0) + 1

    result: list[str] = []
    for entry in entries:
        if title_counts[entry.title] > 1 and entry.publisher:
            result.append(f"{entry.title} ({entry.publisher})")
        else:
            result.append(entry.title)
    return result


def assign_titles_to_areas(
    result: dict[str, list[WikiTitleEntry]],
    released_areas: Iterable[str],
    unmarked_titles: Iterable[str],
    marked_titles: Iterable[tuple[str, list[str]]],
    publisher: str,
) -> None:
    released = [area for area in AREA_ORDER if area in set(released_areas)]
    default_titles = list(unmarked_titles)
    explicit_titles = {area: [] for area in AREA_ORDER}

    for title, areas in marked_titles:
        for area in areas:
            if area in released:
                explicit_titles[area].append(title)

    for area in released:
        if explicit_titles[area]:
            result[area].append(WikiTitleEntry(explicit_titles[area][0], publisher))
        elif default_titles:
            result[area].append(WikiTitleEntry(default_titles[0], publisher))


def process_nes(html: str) -> dict[str, list[str]]:
    rows = parse_table(html, table_id="softwarelist")
    result: dict[str, list[WikiTitleEntry]] = {area: [] for area in AREA_ORDER}

    for row in rows:
        # NES rows have 7 columns: title, developer, publisher, first released,
        # JP release date, NA release date, PAL release date. Header rows and
        # malformed rows are ignored.
        if len(row.cells) != 7 or row.cells[0].tag != "td":
            continue

        released_areas = [
            area
            for area, column_index in NES_RELEASE_COLUMNS.items()
            if is_released(row.cells[column_index])
        ]
        if not released_areas:
            continue

        unmarked_titles, marked_titles = parse_title_lines(row.cells[0])
        publisher = clean_publisher(row.cells[2])
        assign_titles_to_areas(result, released_areas, unmarked_titles, marked_titles, publisher)

    return {area: disambiguate_duplicate_titles(result[area]) for area in AREA_ORDER}


def default_resource_path(project_root: Path, suffix: str) -> Path:
    return project_root / "src" / "main" / "resources" / "platform" / "nes" / f"nes_{suffix}"


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate NES wiki JSON from the checked-in NES wiki HTML.",
    )
    parser.add_argument(
        "--input",
        type=Path,
        help="Input wiki HTML. Defaults to src/main/resources/platform/nes/nes_wiki.html.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        help="Output wiki JSON. Defaults to src/main/resources/platform/nes/nes_wiki.json.",
    )
    parser.add_argument(
        "--project-root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Project root used to resolve default input/output paths.",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])
    input_path = args.input or default_resource_path(args.project_root, "wiki.html")
    output_path = args.output or default_resource_path(args.project_root, "wiki.json")

    if not input_path.is_file():
        print(f"Input file not found: {input_path}", file=sys.stderr)
        return 1

    html = input_path.read_text(encoding="utf-8")
    data = process_nes(html)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )

    counts = ", ".join(f"{area}={len(data[area])}" for area in AREA_ORDER)
    print(f"Generated {output_path} ({counts})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

