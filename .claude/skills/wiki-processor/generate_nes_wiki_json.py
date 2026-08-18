#!/usr/bin/env python3
"""Generate ai/nes/nes_wiki.json from ai/nes/nes_wiki.txt.

NES wiki output is grouped by release region:

{
  "JPN": [{"title": "...", "developer": "...", "publisher": "...", "releaseDate": "YYYYMMDD"}],
  "USA": [],
  "PAL": []
}

Each released region is emitted as an independent object. Unreleased regions are skipped.
Missing values are written as null.
The generated JSON is cleaned before each run.
Unused title entries are appended to ai/nes/nes_error.txt.
"""

from __future__ import annotations

import argparse
import html
import json
import re
from pathlib import Path
from typing import Any

PLATFORM = "nes"
REGION_MAP = {
    "JP": "JPN",
    "NA": "USA",
    "PAL": "PAL",
}
REGION_COLUMNS = ["JP", "NA", "PAL"]
PAL_TITLE_REGIONS = {"PAL", "FR", "ESP", "EU", "EUR", "AUS", "UK", "GB", "DE", "GER", "ITA", "NL", "SWE"}
USED_TITLE_EXCEPTIONS = {
    # This game was only released in Japan. The English article title and JP title both refer to the same release.
    ("Adventure Island IV", "Adventure Island IV"),
}
REGION_RE = re.compile(r"^[A-Z]{2,3}(?:/[A-Z]{2,3})*$")
ROW_RE = re.compile(r"<tr\b[^>]*>(.*?)</tr>", re.IGNORECASE | re.DOTALL)
TD_RE = re.compile(r"<td\b([^>]*)>(.*?)</td>", re.IGNORECASE | re.DOTALL)
BR_RE = re.compile(r"<br\b[^>]*>", re.IGNORECASE)
TAG_RE = re.compile(r"<[^>]+>")


def text_from_html(fragment: str) -> str:
    fragment = BR_RE.sub("\n", fragment)
    fragment = re.sub(
        r"<sup\b[^>]*class=\"mw-ref reference\".*?</sup>",
        "",
        fragment,
        flags=re.IGNORECASE | re.DOTALL,
    )
    text = TAG_RE.sub("", fragment)
    text = html.unescape(text)
    text = re.sub(r"[ \t\r\f\v]+", " ", text)
    text = re.sub(r" *\n *", "\n", text)
    return text.strip()


def normalize_spaces(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip()


def unique(values: list[str]) -> list[str]:
    seen: set[str] = set()
    result: list[str] = []
    for value in values:
        if value and value not in seen:
            seen.add(value)
            result.append(value)
    return result


def sup_regions(fragment: str) -> list[str]:
    regions: list[str] = []
    for sup in re.findall(
        r"<sup\b(?![^>]*class=\"mw-ref reference\")[^>]*>(.*?)</sup>",
        fragment,
        re.IGNORECASE | re.DOTALL,
    ):
        label = normalize_spaces(text_from_html(sup))
        if REGION_RE.match(label):
            regions.extend(label.split("/"))
    return unique(regions)


def parse_titles(fragment: str) -> list[dict[str, Any]]:
    titles: list[dict[str, Any]] = []
    for match in re.finditer(
        r"<i\b[^>]*>(.*?)</i>(?P<after>(?:\s*<sup\b[^>]*>.*?</sup>)?)",
        fragment,
        re.IGNORECASE | re.DOTALL,
    ):
        title = normalize_spaces(text_from_html(match.group(1)))
        if title:
            titles.append({"title": title, "regions": sup_regions(match.group("after") or "")})

    if not titles:
        fallback = normalize_spaces(text_from_html(fragment))
        if fallback:
            titles.append({"title": fallback, "regions": []})
    return titles


def titles_for_region(titles: list[dict[str, Any]], region: str) -> tuple[list[str], set[int]]:
    if not titles:
        return [], set()

    if region == "PAL":
        selected: list[str] = []
        used_indexes: set[int] = set()
        for index, title in enumerate(titles):
            if any(title_region in PAL_TITLE_REGIONS for title_region in title["regions"]):
                selected.append(title["title"])
                used_indexes.add(index)
        if selected:
            return unique(selected), used_indexes

    for index, title in enumerate(titles):
        if region in title["regions"]:
            return [title["title"]], {index}
    return [titles[0]["title"]], {0}


def parse_people_cell(fragment: str) -> list[dict[str, Any]]:
    parts = [part for part in BR_RE.split(fragment) if text_from_html(part)]
    people: list[dict[str, Any]] = []
    for part in parts:
        regions = sup_regions(part)
        cleaned = re.sub(r"<sup\b[^>]*>.*?</sup>", "", part, flags=re.IGNORECASE | re.DOTALL)
        name = normalize_spaces(text_from_html(cleaned))
        if name:
            people.append({"name": name, "regions": regions})

    if not people:
        name = normalize_spaces(text_from_html(fragment))
        if name:
            people.append({"name": name, "regions": []})
    return people


def null_if_empty(value: str | None) -> str | None:
    return value if value else None


def join_titles(values: list[str]) -> str | None:
    return " | ".join(values) if values else None


def people_for_region(people: list[dict[str, Any]], region: str) -> str | None:
    region_matches = [item["name"] for item in people if region in item["regions"]]
    if region_matches:
        return "; ".join(unique(region_matches))

    global_matches = [item["name"] for item in people if not item["regions"]]
    if global_matches:
        return "; ".join(unique(global_matches))

    return null_if_empty("; ".join(unique([item["name"] for item in people])))


def date_precision(display: str) -> str | None:
    if not display or display.lower().startswith("unreleased"):
        return None
    if re.fullmatch(r"\d{4}", display):
        return "year"
    if re.fullmatch(r"[A-Za-z]+ \d{4}", display):
        return "month"
    return "day"


def date_from_sort(sort_value: str | None, precision: str | None) -> str | None:
    if not sort_value or not precision:
        return None
    match = re.search(r"(\d{4})-(\d{2})-(\d{2})", sort_value)
    if not match:
        return None
    year, month, day = match.groups()
    if precision == "year":
        return year
    if precision == "month":
        return f"{year}{month}"
    return f"{year}{month}{day}"


def parse_release_date(fragment: str) -> str | None:
    clean_without_refs = re.sub(
        r"<sup\b[^>]*class=\"mw-ref reference\".*?</sup>",
        "",
        fragment,
        flags=re.IGNORECASE | re.DOTALL,
    )
    display_match = re.search(r"<span\b[^>]*>(.*?)</span>", clean_without_refs, re.IGNORECASE | re.DOTALL)
    display = normalize_spaces(text_from_html(display_match.group(1))) if display_match else normalize_spaces(text_from_html(clean_without_refs))
    if not display or display.lower().startswith("unreleased"):
        return None

    sort_value_match = re.search(r'data-sort-value="([^"]*)"', fragment, re.IGNORECASE)
    sort_value = html.unescape(sort_value_match.group(1)) if sort_value_match else None
    normalized_date = date_from_sort(sort_value, date_precision(display))
    return normalized_date or display


def format_unused_title(row_number: int, primary_title: str | None, title: dict[str, Any]) -> str:
    regions = "/".join(title["regions"]) if title["regions"] else "-"
    return f"row={row_number}\tprimary={primary_title or 'null'}\tunusedTitle={title['title']}\tregions={regions}"


def is_used_title_exception(primary_title: str | None, title: dict[str, Any]) -> bool:
    return (primary_title or "", title["title"]) in USED_TITLE_EXCEPTIONS


def clean_generated_output(output_path: Path) -> None:
    if output_path.exists():
        output_path.unlink()


def append_error_entries(error_path: Path, unused_title_entries: list[str]) -> None:
    error_path.parent.mkdir(parents=True, exist_ok=True)
    if not error_path.exists():
        error_path.touch()
    if not unused_title_entries:
        return

    needs_leading_newline = error_path.stat().st_size > 0 and not error_path.read_text(encoding="utf-8").endswith("\n")
    with error_path.open("a", encoding="utf-8") as error_file:
        if needs_leading_newline:
            error_file.write("\n")
        error_file.write("\n".join(unused_title_entries) + "\n")


def parse_nes_wiki(source: str) -> tuple[dict[str, list[dict[str, Any]]], list[str]]:
    grouped: dict[str, list[dict[str, Any]]] = {region: [] for region in REGION_MAP.values()}
    unused_title_entries: list[str] = []

    for row_number, row_html in enumerate(ROW_RE.findall(source), start=1):
        cells = TD_RE.findall(row_html)
        if len(cells) != 7:
            continue

        titles = parse_titles(cells[0][1])
        used_title_indexes: set[int] = set()
        developers = parse_people_cell(cells[1][1])
        publishers = parse_people_cell(cells[2][1])

        for index, source_region in enumerate(REGION_COLUMNS, start=4):
            release_date = parse_release_date(cells[index][1])
            if not release_date:
                continue

            output_region = REGION_MAP[source_region]
            selected_titles, title_indexes = titles_for_region(titles, source_region)
            used_title_indexes.update(title_indexes)
            grouped[output_region].append({
                "title": join_titles(selected_titles),
                "developer": people_for_region(developers, source_region),
                "publisher": people_for_region(publishers, source_region),
                "releaseDate": release_date,
            })

        primary_title = titles[0]["title"] if titles else None
        for title_index, title in enumerate(titles):
            if title_index not in used_title_indexes and not is_used_title_exception(primary_title, title):
                unused_title_entries.append(format_unused_title(row_number, primary_title, title))

    return grouped, unused_title_entries


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate NES wiki JSON as a list of release rows.")
    parser.add_argument("--input", default=f"ai/{PLATFORM}/{PLATFORM}_wiki.txt", help="Input NES wiki HTML/text file.")
    parser.add_argument("--output", default=f"ai/{PLATFORM}/{PLATFORM}_wiki.json", help="Output NES wiki JSON file.")
    parser.add_argument("--error", default=f"ai/{PLATFORM}/{PLATFORM}_error.txt", help="Output unused title entries text file.")
    args = parser.parse_args()

    repo_root = Path(__file__).resolve().parents[3]
    input_path = Path(args.input)
    output_path = Path(args.output)
    error_path = Path(args.error)
    if not input_path.is_absolute():
        input_path = repo_root / input_path
    if not output_path.is_absolute():
        output_path = repo_root / output_path
    if not error_path.is_absolute():
        error_path = repo_root / error_path

    output_path.parent.mkdir(parents=True, exist_ok=True)
    clean_generated_output(output_path)

    data, unused_title_entries = parse_nes_wiki(input_path.read_text(encoding="utf-8"))
    output_path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    append_error_entries(error_path, unused_title_entries)
    total_rows = sum(len(rows) for rows in data.values())
    print(f"Generated {output_path} with {total_rows} release rows")
    print(f"Appended {len(unused_title_entries)} unused title entries to {error_path}")


if __name__ == "__main__":
    main()

