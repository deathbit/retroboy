#!/usr/bin/env python3
"""
Analyze attributes on <archive /> tags in nes_db.txt and list their distinct values.

The database file has an XML declaration followed by multiple top-level elements
(<header> and <datafile>), so this script feeds the parser a virtual <root> wrapper
instead of requiring the source file to be changed.

Usage:
    python3 analyze_archive_attrs.py
    python3 analyze_archive_attrs.py nes_db.txt -o archive_attrs_values.json
    python3 analyze_archive_attrs.py /path/to/nes_db.txt --max-print-values 20

By default, records with licensed="0" or licensed="2" are excluded.
"""

from __future__ import annotations

import argparse
import json
from collections import defaultdict
from pathlib import Path
from typing import DefaultDict, Dict, Iterable, List, Set, TextIO, Tuple
from xml.etree.ElementTree import XMLPullParser, ParseError


DEFAULT_INPUT = Path(__file__).with_name("nes_db.txt")
DEFAULT_OUTPUT = Path(__file__).with_name("archive_attrs_values.json")
DEFAULT_EXCLUDED_LICENSED_VALUES = ("0", "2")


def _chunks_with_virtual_root(file_obj: TextIO, chunk_size: int = 1024 * 1024) -> Iterable[str]:
    """Yield XML text chunks wrapped in one virtual root element.

    ElementTree requires one document root, while nes_db.txt starts with an XML
    declaration and then contains sibling top-level elements. The XML declaration
    is only legal at the beginning of a document, so it is skipped before adding
    the wrapper.
    """
    yield "<root>"

    first_chunk = True
    while True:
        chunk = file_obj.read(chunk_size)
        if not chunk:
            break

        if first_chunk:
            first_chunk = False
            stripped = chunk.lstrip("\ufeff\n\r\t ")
            if stripped.startswith("<?xml"):
                declaration_end = stripped.find("?>")
                if declaration_end == -1:
                    raise ValueError("XML declaration is not closed with ?>")
                chunk = stripped[declaration_end + 2 :]
            else:
                chunk = stripped

        yield chunk

    yield "</root>"


def collect_archive_attribute_values(
    input_path: Path,
    excluded_licensed_values: Set[str],
) -> Tuple[Dict[str, List[str]], int, int]:
    """Return ({attribute_name: sorted_unique_values}, total_count, skipped_count)."""
    values_by_attr: DefaultDict[str, Set[str]] = defaultdict(set)
    parser = XMLPullParser(events=("end",))
    total_archives = 0
    skipped_archives = 0

    try:
        with input_path.open("r", encoding="utf-8") as file_obj:
            for chunk in _chunks_with_virtual_root(file_obj):
                parser.feed(chunk)
                for _event, elem in parser.read_events():
                    if elem.tag == "archive":
                        total_archives += 1
                        if elem.attrib.get("licensed") in excluded_licensed_values:
                            skipped_archives += 1
                            elem.clear()
                            continue
                        for attr_name, attr_value in elem.attrib.items():
                            values_by_attr[attr_name].add(attr_value)
                    elem.clear()

        parser.close()
    except ParseError as exc:
        raise SystemExit(f"XML parse failed for {input_path}: {exc}") from exc

    return (
        {
            attr_name: sorted(attr_values)
            for attr_name, attr_values in sorted(values_by_attr.items())
        },
        total_archives,
        skipped_archives,
    )


def write_json_report(
    output_path: Path,
    input_path: Path,
    archive_values: Dict[str, List[str]],
    excluded_licensed_values: Set[str],
    total_archives: int,
    skipped_archives: int,
) -> None:
    """Write a JSON report containing counts and distinct values."""
    report = {
        "input": str(input_path),
        "excluded_licensed_values": sorted(excluded_licensed_values),
        "total_archive_count": total_archives,
        "skipped_archive_count": skipped_archives,
        "included_archive_count": total_archives - skipped_archives,
        "attribute_count": len(archive_values),
        "attributes": {
            attr_name: {
                "distinct_value_count": len(attr_values),
                "values": attr_values,
            }
            for attr_name, attr_values in archive_values.items()
        },
    }

    output_path.write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def print_summary(
    archive_values: Dict[str, List[str]],
    max_print_values: int,
    excluded_licensed_values: Set[str],
    total_archives: int,
    skipped_archives: int,
) -> None:
    """Print a compact terminal summary."""
    print(f"Total <archive> record(s): {total_archives}")
    print(
        "Skipped <archive> record(s) with licensed in "
        f"{sorted(excluded_licensed_values)}: {skipped_archives}"
    )
    print(f"Included <archive> record(s): {total_archives - skipped_archives}")
    print(f"Found {len(archive_values)} archive attribute(s).")
    for attr_name, attr_values in archive_values.items():
        print(f"\n{attr_name}: {len(attr_values)} distinct value(s)")
        shown_values = attr_values[:max_print_values]
        for value in shown_values:
            print(f"  - {value}")
        remaining = len(attr_values) - len(shown_values)
        if remaining > 0:
            print(f"  ... {remaining} more value(s); see JSON report for the full list")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Collect all attribute names and distinct values from <archive /> tags."
    )
    parser.add_argument(
        "input",
        nargs="?",
        type=Path,
        default=DEFAULT_INPUT,
        help=f"Path to nes_db.txt. Default: {DEFAULT_INPUT}",
    )
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        default=DEFAULT_OUTPUT,
        help=f"Path to write the JSON report. Default: {DEFAULT_OUTPUT}",
    )
    parser.add_argument(
        "--max-print-values",
        type=int,
        default=30,
        help="Maximum values to print per attribute in terminal summary. Use 0 to print counts only.",
    )
    parser.add_argument(
        "--exclude-licensed",
        default=",".join(DEFAULT_EXCLUDED_LICENSED_VALUES),
        help=(
            "Comma-separated licensed values to exclude. "
            "Default: 0,2. Use an empty string to disable this filter."
        ),
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    input_path = args.input.expanduser().resolve()
    output_path = args.output.expanduser().resolve()

    if args.max_print_values < 0:
        raise SystemExit("--max-print-values must be >= 0")
    if not input_path.is_file():
        raise SystemExit(f"Input file does not exist: {input_path}")

    excluded_licensed_values = {
        value.strip() for value in args.exclude_licensed.split(",") if value.strip()
    }
    archive_values, total_archives, skipped_archives = collect_archive_attribute_values(
        input_path,
        excluded_licensed_values,
    )

    output_path.parent.mkdir(parents=True, exist_ok=True)
    write_json_report(
        output_path,
        input_path,
        archive_values,
        excluded_licensed_values,
        total_archives,
        skipped_archives,
    )
    print_summary(
        archive_values,
        args.max_print_values,
        excluded_licensed_values,
        total_archives,
        skipped_archives,
    )
    print(f"\nFull JSON report written to: {output_path}")


if __name__ == "__main__":
    main()

