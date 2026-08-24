#!/usr/bin/env python3
"""Convert Wikipedia game list HTML tables into normalized JSON.

The parser intentionally uses only Python's standard library so it can run
without project-specific dependencies. Platform differences are expressed with
column options, for example: ``--areas jp:4,us:5,pal:6``.
"""

from __future__ import annotations

import argparse
import json
import re
from dataclasses import dataclass, field
from html.parser import HTMLParser
from pathlib import Path
from typing import Iterable


WHITESPACE_RE = re.compile(r"[ \t\r\f\v]+")
MONTHS = {
    "January": "01",
    "February": "02",
    "March": "03",
    "April": "04",
    "May": "05",
    "June": "06",
    "July": "07",
    "August": "08",
    "September": "09",
    "October": "10",
    "November": "11",
    "December": "12",
}
KNOWN_EMPTY_VALUES = {
    "",
    "—",
    "-",
    "n/a",
    "na",
    "not released",
    "unreleased",
    "cancelled",
    "canceled",
}


@dataclass
class Cell:
    text_parts: list[str] = field(default_factory=list)
    span_parts: list[str] = field(default_factory=list)
    spans: list[str] = field(default_factory=list)

    def append_text(self, text: str) -> None:
        self.text_parts.append(text)

    def append_break(self) -> None:
        self.text_parts.append("\n")
        if self.span_parts:
            self.span_parts.append("\n")

    def append_span_text(self, text: str) -> None:
        self.span_parts.append(text)

    def close_span(self) -> None:
        span_text = normalize_text("".join(self.span_parts))
        if span_text:
            self.spans.append(span_text)
        self.span_parts.clear()

    @property
    def text(self) -> str:
        return normalize_text("".join(self.text_parts), preserve_newlines=True)


class WikiTableParser(HTMLParser):
    """Small forgiving table parser for Wikipedia HTML fragments."""

    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.rows: list[list[Cell]] = []
        self._current_row: list[Cell] | None = None
        self._current_cell: Cell | None = None
        self._cell_tag: str | None = None
        self._span_depth = 0
        self._ignored_depth = 0

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        tag = tag.lower()
        if tag == "tr":
            self._finish_row()
            self._current_row = []
            return

        if self._current_row is None:
            return

        if tag in {"td", "th"} and self._current_cell is None:
            self._current_cell = Cell()
            self._cell_tag = tag
            return

        if self._current_cell is None:
            return

        if tag == "br":
            self._current_cell.append_break()
        elif tag == "sup":
            self._ignored_depth += 1
        elif tag == "span" and self._ignored_depth == 0:
            self._span_depth += 1

    def handle_endtag(self, tag: str) -> None:
        tag = tag.lower()
        if self._current_cell is not None:
            if tag == "sup" and self._ignored_depth:
                self._ignored_depth -= 1
                return
            if tag == "span" and self._span_depth:
                self._span_depth -= 1
                if self._span_depth == 0:
                    self._current_cell.close_span()
                return
            if tag == self._cell_tag:
                self._finish_cell()
                return
        if tag == "tr":
            self._finish_row()

    def handle_data(self, data: str) -> None:
        if self._current_cell is None or self._ignored_depth:
            return
        self._current_cell.append_text(data)
        if self._span_depth:
            self._current_cell.append_span_text(data)

    def close(self) -> None:
        super().close()
        self._finish_cell()
        self._finish_row()

    def _finish_cell(self) -> None:
        if self._current_row is not None and self._current_cell is not None:
            self._current_row.append(self._current_cell)
        self._current_cell = None
        self._cell_tag = None
        self._span_depth = 0
        self._ignored_depth = 0

    def _finish_row(self) -> None:
        self._finish_cell()
        if self._current_row:
            self.rows.append(self._current_row)
        self._current_row = None


def normalize_text(value: str, preserve_newlines: bool = False) -> str:
    value = value.replace("\xa0", " ").replace("\u200b", "")
    if preserve_newlines:
        lines = [WHITESPACE_RE.sub(" ", line).strip() for line in value.splitlines()]
        return "\n".join(line for line in lines if line)
    return WHITESPACE_RE.sub(" ", value).strip()


def split_values(value: str) -> list[str]:
    values: list[str] = []
    seen: set[str] = set()
    for line in value.splitlines() or [value]:
        item = normalize_text(line)
        if not item or item.lower() in KNOWN_EMPTY_VALUES:
            continue
        if item not in seen:
            seen.add(item)
            values.append(item)
    return values


def is_empty_release(value: str) -> bool:
    normalized = normalize_text(value).lower()
    return normalized in KNOWN_EMPTY_VALUES or normalized.startswith("unreleased")


def release_date(cell: Cell) -> str | None:
    if is_empty_release(cell.text):
        return None
    for span in cell.spans:
        if not is_empty_release(span):
            return format_release_date(span)
    text = normalize_text(cell.text)
    return None if is_empty_release(text) else format_release_date(text)


def format_release_date(value: str) -> str:
    """Normalize Wikipedia release dates to YYYY-MM-DD, YYYY-MM, or YYYY."""
    value = normalize_text(value)
    full_date = re.fullmatch(r"([A-Z][a-z]+) (\d{1,2}), (\d{4})", value)
    if full_date:
        month_name, day, year = full_date.groups()
        month = MONTHS.get(month_name)
        if month:
            return f"{year}-{month}-{int(day):02d}"

    month_date = re.fullmatch(r"([A-Z][a-z]+) (\d{4})", value)
    if month_date:
        month_name, year = month_date.groups()
        month = MONTHS.get(month_name)
        if month:
            return f"{year}-{month}"

    if re.fullmatch(r"\d{4}", value):
        return value

    raise ValueError(f"Unsupported release date format: {value!r}")


def parse_area_columns(value: str) -> list[tuple[str, int]]:
    result: list[tuple[str, int]] = []
    for part in value.split(","):
        if not part.strip():
            continue
        if ":" not in part:
            raise argparse.ArgumentTypeError(f"Invalid area column '{part}', expected area:index")
        area, index = part.split(":", 1)
        area = area.strip().lower()
        if area == "na":
            area = "us"
        if area not in {"us", "jp", "pal"}:
            raise argparse.ArgumentTypeError(f"Unsupported area '{area}', expected us, jp, or pal")
        result.append((area, int(index.strip())))
    if not result:
        raise argparse.ArgumentTypeError("At least one area column is required")
    return result


def parse_games(
    html: str,
    title_col: int,
    developers_col: int,
    publishers_col: int,
    area_columns: Iterable[tuple[str, int]],
) -> list[dict[str, object]]:
    parser = WikiTableParser()
    parser.feed(html)
    parser.close()

    required_columns = [title_col, developers_col, publishers_col, *(index for _, index in area_columns)]
    min_columns = max(required_columns) + 1
    games: list[dict[str, object]] = []

    for row in parser.rows:
        if len(row) < min_columns:
            continue
        title_text = row[title_col].text
        titles = split_values(title_text)
        if not titles or title_text.lower().startswith("title"):
            continue

        release_areas: list[str] = []
        release_dates: list[str] = []
        for area, column in area_columns:
            date = release_date(row[column])
            if date is None:
                continue
            release_areas.append(area)
            release_dates.append(date)

        games.append(
            {
                "id": len(games) + 1,
                "titles": titles,
                "developers": split_values(row[developers_col].text),
                "publishers": split_values(row[publishers_col].text),
                "releaseAreas": release_areas,
                "releaseDates": release_dates,
            }
        )

    return games


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path, help="Wikipedia HTML table fragment to parse")
    parser.add_argument("--output", "-o", type=Path, help="Output JSON path; defaults to <input>_json.txt")
    parser.add_argument("--title-col", type=int, default=0, help="Zero-based title column index")
    parser.add_argument("--developers-col", type=int, default=1, help="Zero-based developers column index")
    parser.add_argument("--publishers-col", type=int, default=2, help="Zero-based publishers column index")
    parser.add_argument(
        "--areas",
        type=parse_area_columns,
        default=parse_area_columns("jp:4,us:5,pal:6"),
        help="Comma-separated release area columns, e.g. jp:4,us:5,pal:6; NA is normalized to us",
    )
    parser.add_argument("--indent", type=int, default=2, help="JSON indentation")
    return parser


def main() -> None:
    args = build_arg_parser().parse_args()
    input_path: Path = args.input
    output_path: Path = args.output or input_path.with_suffix(".json")

    games = parse_games(
        input_path.read_text(encoding="utf-8"),
        title_col=args.title_col,
        developers_col=args.developers_col,
        publishers_col=args.publishers_col,
        area_columns=args.areas,
    )
    output_path.write_text(json.dumps(games, ensure_ascii=False, indent=args.indent) + "\n", encoding="utf-8")
    print(f"Wrote {len(games)} games to {output_path}")


if __name__ == "__main__":
    main()
