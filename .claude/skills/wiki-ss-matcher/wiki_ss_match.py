#!/usr/bin/env python3
"""Generate wiki -> ScreenScraper match mappings with an explicit scoring model.

The script is platform-agnostic. For a platform name such as ``nes`` it reads:

- ai/nes/nes_wiki.json
- ai/nes/nes_ss.json

and writes:

- ai/nes/nes_wiki_ss.json
- ai/nes/nes_wiki_ss_review.json

``gameId`` values may be reused by multiple wiki rows. Every wiki row must
receive exactly one mapping.
"""

from __future__ import annotations

import argparse
import json
import re
import unicodedata
from dataclasses import dataclass
from difflib import SequenceMatcher
from pathlib import Path
from typing import Any


DEFAULT_PLATFORM = "nes"

TITLE_WEIGHT = 60.0
DATE_WEIGHT = 36.0
DEVELOPER_WEIGHT = 12.0
PUBLISHER_WEIGHT = 12.0
AREA_WEIGHT = 8.0
EXACT_TITLE_BONUS = 25.0
MIN_CANDIDATE_TITLE_SCORE = 0.20


@dataclass(frozen=True)
class ScoredCandidate:
    score: float
    title_score: float
    date_score: float
    developer_match: bool
    publisher_match: bool
    area_match: bool
    exact_title: bool
    title_pair: tuple[str, str]
    ss_record: dict[str, Any]


def default_paths(platform: str) -> tuple[Path, Path, Path, Path]:
    platform_dir = Path("ai") / platform
    return (
        platform_dir / f"{platform}_wiki.json",
        platform_dir / f"{platform}_ss.json",
        platform_dir / f"{platform}_wiki_ss.json",
        platform_dir / f"{platform}_wiki_ss_review.json",
    )


def read_json_list(path: Path) -> list[dict[str, Any]]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, list):
        raise ValueError(f"Expected JSON list: {path}")
    for index, item in enumerate(data, start=1):
        if not isinstance(item, dict):
            raise ValueError(f"Expected object at {path}[{index}]")
    return data


def normalize_text(value: str) -> str:
    value = unicodedata.normalize("NFKD", value).encode("ascii", "ignore").decode().lower()
    value = value.replace("&", " and ")
    # Japanese romanization often adds/removes "no"; articles also differ across sources.
    value = re.sub(r"\b(the|a|an|no)\b", " ", value)
    value = re.sub(r"[^a-z0-9]+", " ", value)
    return re.sub(r"\s+", " ", value).strip()


def tokens(value: str) -> set[str]:
    return set(normalize_text(value).split())


def jaccard(a: str, b: str) -> float:
    left = tokens(a)
    right = tokens(b)
    if not left or not right:
        return 0.0
    return len(left & right) / len(left | right)


def title_similarity(wiki_record: dict[str, Any], ss_record: dict[str, Any]) -> tuple[float, bool, tuple[str, str]]:
    best_score = 0.0
    exact_title = False
    best_pair = ("", "")

    for wiki_title in wiki_record["titles"]:
        normalized_wiki = normalize_text(wiki_title)
        for ss_title in ss_record["titles"]:
            normalized_ss = normalize_text(ss_title)
            score = max(
                jaccard(wiki_title, ss_title),
                SequenceMatcher(None, normalized_wiki, normalized_ss).ratio() * 0.92,
            )
            if normalized_wiki == normalized_ss:
                exact_title = True
                score = 1.0
            if score > best_score:
                best_score = score
                best_pair = (wiki_title, ss_title)

    return best_score, exact_title, best_pair


def release_date_similarity(wiki_record: dict[str, Any], ss_record: dict[str, Any]) -> float:
    wiki_dates = wiki_record["releaseDates"]
    ss_dates = ss_record["releaseDates"]
    if not wiki_dates or not ss_dates:
        return 0.0

    best_score = 0.0
    for wiki_date in wiki_dates:
        for ss_date in ss_dates:
            if wiki_date == ss_date:
                best_score = max(best_score, 1.0)
            elif len(wiki_date) >= 7 and len(ss_date) >= 7 and wiki_date[:7] == ss_date[:7]:
                best_score = max(best_score, 0.82)
            elif len(wiki_date) >= 4 and len(ss_date) >= 4 and wiki_date[:4] == ss_date[:4]:
                best_score = max(best_score, 0.50)
    return best_score


def normalized_overlap(left_values: list[str], right_values: list[str]) -> bool:
    left = {normalize_text(value) for value in left_values if normalize_text(value)}
    right = {normalize_text(value) for value in right_values if normalize_text(value)}
    return bool(left & right)


def release_area_match(wiki_record: dict[str, Any], ss_record: dict[str, Any]) -> bool:
    return bool(set(wiki_record["releaseAreas"]) & set(ss_record["releaseAreas"]))


def score_candidate(wiki_record: dict[str, Any], ss_record: dict[str, Any]) -> ScoredCandidate | None:
    title_score, exact_title, title_pair = title_similarity(wiki_record, ss_record)
    if title_score < MIN_CANDIDATE_TITLE_SCORE and not exact_title:
        return None

    date_score = release_date_similarity(wiki_record, ss_record)
    developer_match = normalized_overlap(wiki_record["developers"], ss_record["developers"])
    publisher_match = normalized_overlap(wiki_record["publishers"], ss_record["publishers"])
    area_match = release_area_match(wiki_record, ss_record)

    score = (
        title_score * TITLE_WEIGHT
        + date_score * DATE_WEIGHT
        + (DEVELOPER_WEIGHT if developer_match else 0.0)
        + (PUBLISHER_WEIGHT if publisher_match else 0.0)
        + (AREA_WEIGHT if area_match else 0.0)
        + (EXACT_TITLE_BONUS if exact_title else 0.0)
    )

    return ScoredCandidate(
        score=score,
        title_score=title_score,
        date_score=date_score,
        developer_match=developer_match,
        publisher_match=publisher_match,
        area_match=area_match,
        exact_title=exact_title,
        title_pair=title_pair,
        ss_record=ss_record,
    )


def candidate_to_review(candidate: ScoredCandidate) -> dict[str, Any]:
    ss_record = candidate.ss_record
    return {
        "gameId": ss_record["id"],
        "score": round(candidate.score, 2),
        "titleScore": round(candidate.title_score, 4),
        "dateScore": round(candidate.date_score, 4),
        "developerMatch": candidate.developer_match,
        "publisherMatch": candidate.publisher_match,
        "areaMatch": candidate.area_match,
        "exactTitle": candidate.exact_title,
        "titlePair": list(candidate.title_pair),
        "titles": ss_record["titles"],
        "developers": ss_record["developers"],
        "publishers": ss_record["publishers"],
        "releaseAreas": ss_record["releaseAreas"],
        "releaseDates": ss_record["releaseDates"],
    }


def match_records(
    wiki_records: list[dict[str, Any]],
    ss_records: list[dict[str, Any]],
) -> tuple[list[dict[str, int]], list[dict[str, Any]]]:
    mapping: list[dict[str, int]] = []
    review: list[dict[str, Any]] = []

    for wiki_record in wiki_records:
        candidates: list[ScoredCandidate] = []
        for ss_record in ss_records:
            candidate = score_candidate(wiki_record, ss_record)
            if candidate is not None:
                candidates.append(candidate)
        candidates.sort(key=lambda item: item.score, reverse=True)
        if not candidates:
            raise ValueError(f"No ScreenScraper candidate for wikiId={wiki_record['id']}: {wiki_record['titles']}")

        selected = candidates[0]
        mapping.append({"wikiId": wiki_record["id"], "gameId": selected.ss_record["id"]})
        review.append(
            {
                "wikiId": wiki_record["id"],
                "gameId": selected.ss_record["id"],
                "score": round(selected.score, 2),
                "wikiRecord": wiki_record,
                "selectedCandidate": candidate_to_review(selected),
                "topCandidates": [candidate_to_review(candidate) for candidate in candidates[:5]],
            }
        )

    return mapping, review


def validate_mapping(mapping: list[dict[str, int]], wiki_records: list[dict[str, Any]], ss_records: list[dict[str, Any]]) -> None:
    wiki_ids = {record["id"] for record in wiki_records}
    ss_ids = {record["id"] for record in ss_records}
    mapped_wiki_ids = [row["wikiId"] for row in mapping]
    mapped_game_ids = [row["gameId"] for row in mapping]

    if len(mapped_wiki_ids) != len(set(mapped_wiki_ids)):
        raise ValueError("Duplicate wikiId in mapping")
    if set(mapped_wiki_ids) != wiki_ids:
        missing = sorted(wiki_ids - set(mapped_wiki_ids))
        extra = sorted(set(mapped_wiki_ids) - wiki_ids)
        raise ValueError(f"Mapping must cover all wiki ids; missing={missing[:20]}, extra={extra[:20]}")
    if not set(mapped_game_ids) <= ss_ids:
        missing_game_ids = sorted(set(mapped_game_ids) - ss_ids)
        raise ValueError(f"Mapping contains unknown gameId values: {missing_game_ids[:20]}")


def generate_mapping(wiki_path: Path, ss_path: Path, mapping_path: Path, review_path: Path) -> int:
    wiki_records = read_json_list(wiki_path)
    ss_records = read_json_list(ss_path)
    mapping, review = match_records(wiki_records, ss_records)
    validate_mapping(mapping, wiki_records, ss_records)

    mapping_path.write_text(json.dumps(mapping, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    review_path.write_text(json.dumps(review, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return len(mapping)


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--platform", default=DEFAULT_PLATFORM, help="Platform name used to infer ai/<platform> paths")
    parser.add_argument("--wiki", type=Path, help="Wiki JSON path")
    parser.add_argument("--ss", type=Path, help="ScreenScraper aggregate JSON path")
    parser.add_argument("--mapping", type=Path, help="Output wikiId->gameId mapping path")
    parser.add_argument("--review", type=Path, help="Output scored review path")
    return parser


def main() -> int:
    args = build_arg_parser().parse_args()
    default_wiki, default_ss, default_mapping, default_review = default_paths(args.platform)
    wiki_path = args.wiki or default_wiki
    ss_path = args.ss or default_ss
    mapping_path = args.mapping or default_mapping
    review_path = args.review or default_review

    try:
        count = generate_mapping(wiki_path, ss_path, mapping_path, review_path)
    except Exception as exc:
        print(f"ERROR: {exc}")
        return 1
    print(f"Wrote {count} matches to {mapping_path}")
    print(f"Wrote scored review to {review_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

