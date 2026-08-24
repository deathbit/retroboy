#!/usr/bin/env python3
"""Filter Parent/Clone text files by removing records with excluded tags."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path


DEFAULT_INPUT = Path("ai/nes/nes_pc.txt")
DEFAULT_EXCLUDE_TAGS = ("(Unl)", "(Pirate)")


def default_output_path(input_path: Path) -> Path:
    if input_path.suffix:
        return input_path.with_name(f"{input_path.stem}_clean{input_path.suffix}")
    return input_path.with_name(f"{input_path.name}_clean")


def filter_lines(lines: list[str], exclude_tags: tuple[str, ...]) -> tuple[list[str], dict[str, int]]:
    kept: list[str] = []
    stats = {
        "total": 0,
        "kept": 0,
        "removed": 0,
    }
    for tag in exclude_tags:
        stats[f"removed_{tag}"] = 0

    for line in lines:
        stats["total"] += 1
        matched_tags = [tag for tag in exclude_tags if tag in line]
        if matched_tags:
            stats["removed"] += 1
            for tag in matched_tags:
                stats[f"removed_{tag}"] += 1
            continue
        kept.append(line)
        stats["kept"] += 1

    return kept, stats


def parse_tags(raw_tags: str) -> tuple[str, ...]:
    tags = tuple(tag.strip() for tag in raw_tags.split(",") if tag.strip())
    if not tags:
        raise ValueError("at least one exclude tag is required")
    return tags


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Generate a cleaned Parent/Clone text file by removing lines containing excluded tags."
    )
    parser.add_argument(
        "--input",
        type=Path,
        default=DEFAULT_INPUT,
        help=f"input Parent/Clone txt file, defaults to {DEFAULT_INPUT}",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=None,
        help="output txt file; defaults to '<input_stem>_clean.txt' beside the input",
    )
    parser.add_argument(
        "--exclude-tags",
        default=",".join(DEFAULT_EXCLUDE_TAGS),
        help="comma-separated exact substrings to remove, defaults to '(Unl),(Pirate)'",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="validate and print statistics without writing the output file",
    )
    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)

    input_path = args.input
    output_path = args.output or default_output_path(input_path)

    try:
        exclude_tags = parse_tags(args.exclude_tags)
    except ValueError as exc:
        parser.error(str(exc))

    if not input_path.is_file():
        print(f"ERROR: input file does not exist: {input_path}", file=sys.stderr)
        return 1

    if input_path.resolve() == output_path.resolve() and not args.dry_run:
        print("ERROR: output path must be different from input path", file=sys.stderr)
        return 1

    try:
        lines = input_path.read_text(encoding="utf-8-sig").splitlines(keepends=True)
    except UnicodeDecodeError as exc:
        print(f"ERROR: failed to read UTF-8 text from {input_path}: {exc}", file=sys.stderr)
        return 1

    filtered_lines, stats = filter_lines(lines, exclude_tags)

    if not args.dry_run:
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text("".join(filtered_lines), encoding="utf-8")

        output_text = output_path.read_text(encoding="utf-8")
        remaining_tags = [tag for tag in exclude_tags if tag in output_text]
        if remaining_tags:
            print(f"ERROR: output still contains excluded tags: {', '.join(remaining_tags)}", file=sys.stderr)
            return 1

    print(f"input={input_path}")
    print(f"output={output_path}")
    print(f"total={stats['total']}")
    print(f"kept={stats['kept']}")
    print(f"removed={stats['removed']}")
    for tag in exclude_tags:
        print(f"removed[{tag}]={stats[f'removed_{tag}']}")
    if args.dry_run:
        print("dry_run=true")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
