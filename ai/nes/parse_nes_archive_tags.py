from __future__ import annotations

import argparse
import json
from collections import Counter, defaultdict
from pathlib import Path
from xml.etree.ElementTree import XMLPullParser


def strip_xml_declaration(chunk: str) -> str:
    chunk = chunk.lstrip("\ufeff")
    if not chunk.startswith("<?xml"):
        return chunk

    declaration_end = chunk.find("?>")
    if declaration_end == -1:
        return chunk

    return chunk[declaration_end + 2 :]


def collect_archive_events(
    parser: XMLPullParser,
    values_by_attribute: dict[str, Counter[str]],
) -> int:
    archive_count = 0

    for event, element in parser.read_events():
        if event == "start" and element.tag == "archive":
            archive_count += 1
            for attribute, value in element.attrib.items():
                values_by_attribute[attribute][value] += 1

        if event == "end":
            element.clear()

    return archive_count


def parse_archive_attributes(input_path: Path, chunk_size: int) -> tuple[int, dict[str, Counter[str]]]:
    archive_count = 0
    values_by_attribute: dict[str, Counter[str]] = defaultdict(Counter)
    parser = XMLPullParser(events=("start", "end"))

    parser.feed("<root>")

    with input_path.open("r", encoding="utf-8") as handle:
        first_chunk = True
        for chunk in iter(lambda: handle.read(chunk_size), ""):
            if first_chunk:
                chunk = strip_xml_declaration(chunk)
                first_chunk = False
            parser.feed(chunk)
            archive_count += collect_archive_events(parser, values_by_attribute)

    parser.feed("</root>")
    archive_count += collect_archive_events(parser, values_by_attribute)

    return archive_count, dict(values_by_attribute)


def build_report(archive_count: int, values_by_attribute: dict[str, Counter[str]]) -> dict[str, object]:
    attributes = {}
    for attribute in sorted(values_by_attribute):
        values = values_by_attribute[attribute]
        attributes[attribute] = {
            "unique_value_count": len(values),
            "values": dict(sorted(values.items())),
        }

    return {
        "archive_count": archive_count,
        "attribute_count": len(values_by_attribute),
        "attributes": attributes,
    }


def print_summary(report: dict[str, object], max_values: int) -> None:
    print(f"archive_count: {report['archive_count']}")
    print(f"attribute_count: {report['attribute_count']}")
    print()

    attributes = report["attributes"]
    if not isinstance(attributes, dict):
        return

    for attribute, info in attributes.items():
        if not isinstance(info, dict):
            continue

        values = info["values"]
        if not isinstance(values, dict):
            continue

        print(f"[{attribute}] unique_value_count: {info['unique_value_count']}")
        for index, (value, count) in enumerate(values.items()):
            if index >= max_values:
                remaining = len(values) - max_values
                print(f"  ... {remaining} more values in JSON output")
                break
            print(f"  {value}: {count}")
        print()


def main() -> None:
    default_input = Path(__file__).with_name("nes_db.txt")
    default_output = Path(__file__).with_name("nes_db_archive_values.json")

    parser = argparse.ArgumentParser(
        description="统计 nes_db.txt 中 archive 标签的属性种类，以及每个属性出现过的不同值。"
    )
    parser.add_argument(
        "-i",
        "--input",
        type=Path,
        default=default_input,
        help=f"输入 XML 文件路径，默认: {default_input}",
    )
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        default=default_output,
        help=f"完整 JSON 结果输出路径，默认: {default_output}",
    )
    parser.add_argument(
        "--max-values",
        type=int,
        default=20,
        help="控制台中每个属性最多展示多少个不同值，默认: 20。完整值总会写入 JSON。",
    )
    parser.add_argument(
        "--chunk-size",
        type=int,
        default=1024 * 1024,
        help="读取文件时的块大小，默认: 1048576。",
    )
    args = parser.parse_args()

    archive_count, values_by_attribute = parse_archive_attributes(args.input, args.chunk_size)
    report = build_report(archive_count, values_by_attribute)

    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8") as handle:
        json.dump(report, handle, ensure_ascii=False, indent=2)
        handle.write("\n")

    print_summary(report, args.max_values)
    print(f"Full result written to: {args.output}")


if __name__ == "__main__":
    main()
