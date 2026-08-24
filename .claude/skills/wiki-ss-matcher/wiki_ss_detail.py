#!/usr/bin/env python3
"""Generate detailed wiki/ScreenScraper match records from an AI-reviewed mapping.

This script does not decide matches. It only validates a manually/AI-authored
``*_wiki_ss.json`` mapping and expands each mapping into the original wiki and
ScreenScraper records.
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any


DEFAULT_PLATFORM = "nes"


def default_paths(platform: str) -> tuple[Path, Path, Path, Path]:
    platform_dir = Path("ai") / platform
    return (
        platform_dir / f"{platform}_wiki.json",
        platform_dir / f"{platform}_ss.json",
        platform_dir / f"{platform}_wiki_ss.json",
        platform_dir / f"{platform}_wiki_ss_detail.json",
    )


class MatchDetailError(RuntimeError):
    """Raised when the mapping cannot be expanded safely."""


def read_json_list(path: Path) -> list[dict[str, Any]]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, list):
        raise MatchDetailError(f"Expected a JSON list: {path}")
    for index, item in enumerate(data, start=1):
        if not isinstance(item, dict):
            raise MatchDetailError(f"Expected object at {path}[{index}]")
    return data


def read_mapping(path: Path) -> list[dict[str, int]]:
    mapping = read_json_list(path)
    result: list[dict[str, int]] = []
    seen_wiki_ids: set[int] = set()

    for index, item in enumerate(mapping, start=1):
        if set(item) != {"wikiId", "gameId"}:
            raise MatchDetailError(f"Mapping row {index} must contain exactly wikiId and gameId: {item}")
        wiki_id = item["wikiId"]
        game_id = item["gameId"]
        if not isinstance(wiki_id, int) or not isinstance(game_id, int):
            raise MatchDetailError(f"Mapping row {index} ids must be integers: {item}")
        if wiki_id in seen_wiki_ids:
            raise MatchDetailError(f"Duplicate wikiId in mapping: {wiki_id}")
        seen_wiki_ids.add(wiki_id)
        result.append({"wikiId": wiki_id, "gameId": game_id})

    return result


def index_by_id(records: list[dict[str, Any]], source_name: str) -> dict[int, dict[str, Any]]:
    result: dict[int, dict[str, Any]] = {}
    for item in records:
        record_id = item.get("id")
        if not isinstance(record_id, int):
            raise MatchDetailError(f"{source_name} record id must be an integer: {item}")
        if record_id in result:
            raise MatchDetailError(f"Duplicate id in {source_name}: {record_id}")
        result[record_id] = item
    return result


def generate_detail(
    wiki_path: Path,
    ss_path: Path,
    mapping_path: Path,
    output_path: Path,
) -> int:
    wiki_by_id = index_by_id(read_json_list(wiki_path), "wiki")
    ss_by_id = index_by_id(read_json_list(ss_path), "ss")
    mapping = read_mapping(mapping_path)
    mapped_wiki_ids = {row["wikiId"] for row in mapping}
    missing_wiki_ids = sorted(set(wiki_by_id) - mapped_wiki_ids)
    extra_wiki_ids = sorted(mapped_wiki_ids - set(wiki_by_id))
    if missing_wiki_ids:
        raise MatchDetailError(f"Missing wikiId mappings: {missing_wiki_ids[:20]}")
    if extra_wiki_ids:
        raise MatchDetailError(f"wikiId not found: {extra_wiki_ids[:20]}")

    detail: list[dict[str, Any]] = []
    for row in mapping:
        wiki_id = row["wikiId"]
        game_id = row["gameId"]
        if wiki_id not in wiki_by_id:
            raise MatchDetailError(f"wikiId not found: {wiki_id}")
        if game_id not in ss_by_id:
            raise MatchDetailError(f"gameId not found in ss id: {game_id}")
        detail.append(
            {
                "wikiId": wiki_id,
                "gameId": game_id,
                "wikiRecord": wiki_by_id[wiki_id],
                "ssRecord": ss_by_id[game_id],
            }
        )

    output_path.write_text(json.dumps(detail, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return len(detail)


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--platform", default=DEFAULT_PLATFORM, help="Platform name used to infer ai/<platform> paths")
    parser.add_argument("--wiki", type=Path, help="Wiki JSON path")
    parser.add_argument("--ss", type=Path, help="ScreenScraper aggregate JSON path")
    parser.add_argument("--mapping", type=Path, help="AI-reviewed wikiId->gameId mapping path")
    parser.add_argument("--output", type=Path, help="Detailed output path")
    return parser


def main() -> int:
    args = build_arg_parser().parse_args()
    default_wiki, default_ss, default_mapping, default_output = default_paths(args.platform)
    try:
        output = args.output or default_output
        count = generate_detail(
            args.wiki or default_wiki,
            args.ss or default_ss,
            args.mapping or default_mapping,
            output,
        )
    except Exception as exc:
        print(f"ERROR: {exc}")
        return 1
    print(f"Wrote {count} detailed matches to {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

