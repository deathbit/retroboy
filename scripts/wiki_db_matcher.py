#!/usr/bin/env python3
"""Match platform wiki JSON entries to DB JSON entries.

The script deliberately separates deterministic candidate generation from the
final judgement step. It can produce useful fuzzy candidates, while
platform-specific aliases should be reviewed by an AI agent and stored in an
overrides JSON file so every wiki item maps to at most one DB item.
"""

from __future__ import annotations

import argparse
import json
import re
import unicodedata
from dataclasses import dataclass
from difflib import SequenceMatcher
from functools import lru_cache
from pathlib import Path
from typing import Any


REGION_TAGS = {
	"asia",
	"australia",
	"austria",
	"brazil",
	"canada",
	"china",
	"denmark",
	"europe",
	"finland",
	"france",
	"germany",
	"hong kong",
	"italy",
	"japan",
	"korea",
	"netherlands",
	"norway",
	"spain",
	"sweden",
	"taiwan",
	"uk",
	"usa",
	"world",
}

COMMON_TAGS = {
	"en",
	"fr",
	"de",
	"es",
	"it",
	"nl",
	"pt",
	"rev 1",
	"rev 2",
	"beta",
	"prototype",
	"proto",
	"sample",
}

STOP_TOKENS = {
	"a",
	"an",
	"and",
	"de",
	"for",
	"in",
	"no",
	"of",
	"the",
	"to",
	"vs",
}

ALIAS_SEPARATOR_PATTERN = re.compile(r"\s+\|\s+")

ROMAN_NUMERALS = {
	" i ": " 1 ",
	" ii ": " 2 ",
	" iii ": " 3 ",
	" iv ": " 4 ",
	" v ": " 5 ",
	" vi ": " 6 ",
}


@dataclass(frozen=True)
class Candidate:
	wiki_index: int
	wiki: str
	db: str
	score: float


def load_json(path: Path) -> dict[str, list[str]]:
	data = json.loads(path.read_text(encoding="utf-8"))
	if not isinstance(data, dict):
		raise ValueError(f"{path} must contain a JSON object")
	result: dict[str, list[str]] = {}
	for area, items in data.items():
		if not isinstance(items, list) or not all(isinstance(item, str) for item in items):
			raise ValueError(f"{path}:{area} must be a list of strings")
		result[area] = items
	return result


@lru_cache(maxsize=None)
def strip_accents(text: str) -> str:
	return "".join(
		char
		for char in unicodedata.normalize("NFKD", text)
		if not unicodedata.combining(char)
	)


@lru_cache(maxsize=None)
def remove_parenthetical_tags(text: str) -> str:
	# DB titles are No-Intro style names where parenthesised fragments are
	# usually regions, languages, revisions, publishers, dump status, etc. Wiki
	# titles can also contain disambiguators such as "(JP)". Treat them as tags
	# for matching; AI-reviewed overrides can still pin rare true-title cases.
	return re.sub(r"\([^()]*\)", " ", text)


@lru_cache(maxsize=None)
def normalize_basic(text: str) -> str:
	text = strip_accents(text).lower()
	text = text.replace("&", " and ")
	text = text.replace("'n", " and ")
	text = re.sub(r"\bvs\.\b", " versus ", text)
	text = re.sub(r"\bvol\.\b", " volume ", text)
	text = re.sub(r"[^a-z0-9]+", " ", text)
	text = f" {re.sub(r'\s+', ' ', text).strip()} "
	for roman, number in ROMAN_NUMERALS.items():
		text = text.replace(roman, number)
	return re.sub(r"\s+", " ", text).strip()


@lru_cache(maxsize=None)
def normalize_title(text: str) -> str:
	text = remove_parenthetical_tags(text)
	text = normalize_basic(text)
	# Japanese romanisation in Wiki and No-Intro DB frequently differs only by
	# long vowels: ou/oo/o, uu/u, doubled vowels, etc. Collapse repeated vowels
	# after ordinary normalisation to make candidate retrieval tolerant.
	text = re.sub(r"([aeiou])\1+", r"\1", text)
	text = re.sub(r"ou", "o", text)
	return re.sub(r"\s+", " ", text).strip()


@lru_cache(maxsize=None)
def title_variants(text: str) -> frozenset[str]:
	parts = [text]
	if " | " in text:
		parts.extend(part for part in ALIAS_SEPARATOR_PATTERN.split(text) if part)

	variants: set[str] = set()
	for part in parts:
		normalized = normalize_title(part)
		variants.add(normalized)

		# Handle No-Intro sort titles such as "Bard's Tale, The".
		moved_article = re.sub(r"^(.+),\s*(the|a|an)$", r"\2 \1", strip_accents(part), flags=re.I)
		variants.add(normalize_title(moved_article))

		# Article-less variant.
		variants.add(re.sub(r"^(the|a|an)\s+", "", normalized))
	return frozenset(variant for variant in variants if variant)


@lru_cache(maxsize=None)
def token_sort_key(text: str) -> str:
	return " ".join(sorted(normalize_title(text).split()))


@lru_cache(maxsize=None)
def similarity(a: str, b: str) -> float:
	a_variants = title_variants(a)
	b_variants = title_variants(b)
	best = 0.0
	for av in a_variants:
		for bv in b_variants:
			number_cap = 0.84 if variant_number_tokens(av) != variant_number_tokens(bv) else 1.0
			if av == bv:
				return number_cap
			ratio = SequenceMatcher(None, av, bv).ratio()
			token_ratio = SequenceMatcher(None, " ".join(sorted(av.split())), " ".join(sorted(bv.split()))).ratio()
			prefix_bonus = 0.04 if av.startswith(bv) or bv.startswith(av) else 0.0
			best = max(best, min(number_cap, max(ratio, token_ratio) + prefix_bonus))
	return best


@lru_cache(maxsize=None)
def significant_tokens(text: str) -> frozenset[str]:
	tokens: set[str] = set()
	for variant in title_variants(text):
		tokens.update(
			token
			for token in variant.split()
			if len(token) >= 3 and token not in STOP_TOKENS
		)
	return frozenset(tokens)


@lru_cache(maxsize=None)
def number_tokens(text: str) -> frozenset[str]:
	return frozenset(token for token in normalize_title(text).split() if token.isdigit())


@lru_cache(maxsize=None)
def variant_number_tokens(normalized_variant: str) -> frozenset[str]:
	return frozenset(token for token in normalized_variant.split() if token.isdigit())


def build_candidate_indexes(db_items: list[str]) -> tuple[dict[str, list[str]], dict[str, list[str]]]:
	by_token: dict[str, list[str]] = {}
	by_first: dict[str, list[str]] = {}
	for db in db_items:
		for normalized in title_variants(db):
			if normalized:
				by_first.setdefault(normalized[0], []).append(db)
		for token in significant_tokens(db):
			by_token.setdefault(token, []).append(db)
	return by_token, by_first


def candidate_pool(wiki: str, db_items: list[str], by_token: dict[str, list[str]], by_first: dict[str, list[str]]) -> list[str]:
	pool: dict[str, None] = {}
	for token in significant_tokens(wiki):
		for db in by_token.get(token, []):
			pool[db] = None

	normalized = normalize_title(wiki)
	if normalized:
		for db in by_first.get(normalized[0], []):
			pool.setdefault(db, None)

	# Very short titles often have no stable significant token. Falling back to
	# the whole DB list is acceptable for those rare cases.
	if not pool:
		return db_items
	return list(pool)


def parse_area_map(values: list[str]) -> dict[str, list[str]]:
	area_map: dict[str, list[str]] = {}
	for value in values:
		if "=" not in value:
			raise ValueError(f"invalid area mapping {value!r}; expected WIKI=DB1,DB2")
		wiki_area, db_areas = value.split("=", 1)
		area_map[wiki_area] = [part for part in db_areas.split(",") if part]
	return area_map


def db_primary_title(db: str) -> str:
	return ALIAS_SEPARATOR_PATTERN.split(db, maxsplit=1)[0]


def resolve_db_override(area: str, override_db: str, db_items: list[str]) -> str:
	if override_db in db_items:
		return override_db

	matches = [db for db in db_items if db_primary_title(db) == override_db]
	if len(matches) == 1:
		return matches[0]
	if len(matches) > 1:
		raise ValueError(f"override for {area}: ambiguous db item after alias expansion: {override_db}")
	raise ValueError(f"override for {area}: db item not found: {override_db}")


def load_overrides(path: Path | None) -> dict[str, dict[str, str | list[str]]]:
	if path is None or not path.exists():
		return {}
	data = json.loads(path.read_text(encoding="utf-8"))
	if not isinstance(data, dict):
		raise ValueError("overrides must be an object: {area: {wiki: db_or_db_list}}")
	overrides: dict[str, dict[str, str | list[str]]] = {}
	for area, mapping in data.items():
		if not isinstance(mapping, dict):
			raise ValueError(f"overrides[{area!r}] must be an object")
		checked: dict[str, str | list[str]] = {}
		for key, value in mapping.items():
			if not isinstance(key, str):
				raise ValueError(f"overrides[{area!r}] contains a non-string wiki key")
			if isinstance(value, str):
				checked[key] = value
			elif isinstance(value, list) and all(isinstance(item, str) for item in value):
				checked[key] = value
			else:
				raise ValueError(f"overrides[{area!r}][{key!r}] must be a string or string list")
		overrides[area] = checked
	return overrides


def top_candidates(wiki_items: list[str], db_items: list[str], limit: int) -> dict[str, list[dict[str, Any]]]:
	report: dict[str, list[dict[str, Any]]] = {}
	by_token, by_first = build_candidate_indexes(db_items)
	for wiki in wiki_items:
		pool = candidate_pool(wiki, db_items, by_token, by_first)
		scored = sorted(
			(Candidate(0, wiki, db, similarity(wiki, db)) for db in pool),
			key=lambda item: (-item.score, token_sort_key(item.db), item.db),
		)[:limit]
		report[wiki] = [{"db": item.db, "score": round(item.score, 4)} for item in scored]
	return report


def match_area(
	area: str,
	wiki_items: list[str],
	db_items: list[str],
	overrides: dict[str, str | list[str]],
	threshold: float,
) -> tuple[list[str], list[str], list[str]]:
	used_db: set[str] = set()
	matched_by_index: dict[int, str] = {}

	for wiki, override_value in overrides.items():
		if wiki not in wiki_items:
			raise ValueError(f"override for {area}: wiki item not found: {wiki}")
		db_values = override_value if isinstance(override_value, list) else [override_value]
		available_indexes = [
			index
			for index, item in enumerate(wiki_items)
			if item == wiki and index not in matched_by_index
		]
		if len(db_values) > len(available_indexes):
			raise ValueError(f"override for {area}: too many db items for wiki item: {wiki}")
		for index, override_db in zip(available_indexes, db_values):
			db = resolve_db_override(area, override_db, db_items)
			if db in used_db:
				raise ValueError(f"override for {area}: duplicate db item: {db}")
			matched_by_index[index] = db
			used_db.add(db)

	candidates: list[Candidate] = []
	by_token, by_first = build_candidate_indexes([db for db in db_items if db not in used_db])
	for index, wiki in enumerate(wiki_items):
		if index in matched_by_index:
			continue
		for db in candidate_pool(wiki, db_items, by_token, by_first):
			if db in used_db:
				continue
			score = similarity(wiki, db)
			if score >= threshold:
				candidates.append(Candidate(index, wiki, db, score))

	candidates.sort(key=lambda item: (-item.score, item.wiki_index, token_sort_key(item.db), item.db))

	for candidate in candidates:
		if candidate.wiki_index in matched_by_index or candidate.db in used_db:
			continue
		matched_by_index[candidate.wiki_index] = candidate.db
		used_db.add(candidate.db)

	match = [
		f"{wiki} | {matched_by_index[index]}"
		for index, wiki in enumerate(wiki_items)
		if index in matched_by_index
	]
	mismatch = [wiki for index, wiki in enumerate(wiki_items) if index not in matched_by_index]
	extra = [db for db in db_items if db not in used_db]
	return match, mismatch, extra


def build_result(
	wiki_data: dict[str, list[str]],
	db_data: dict[str, list[str]],
	area_map: dict[str, list[str]],
	overrides: dict[str, dict[str, str | list[str]]],
	threshold: float,
) -> dict[str, dict[str, list[str]]]:
	result = {"match": {}, "mismatch": {}, "extra": {}}
	for wiki_area, wiki_items in wiki_data.items():
		db_areas = area_map.get(wiki_area, [wiki_area])
		db_items = [item for db_area in db_areas for item in db_data.get(db_area, [])]
		match, mismatch, extra = match_area(
			wiki_area,
			wiki_items,
			db_items,
			overrides.get(wiki_area, {}),
			threshold,
		)
		result["match"][wiki_area] = match
		result["mismatch"][wiki_area] = mismatch
		result["extra"][wiki_area] = extra
	return result


def write_json(path: Path, data: Any) -> None:
	path.parent.mkdir(parents=True, exist_ok=True)
	path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
	parser = argparse.ArgumentParser(description="Match platform wiki JSON entries to DB JSON entries.")
	parser.add_argument("--platform", default="nes", help="platform name used by default paths")
	parser.add_argument("--wiki", type=Path, help="wiki JSON path")
	parser.add_argument("--db", type=Path, help="db JSON path")
	parser.add_argument("--output", type=Path, help="match result JSON path")
	parser.add_argument("--overrides", type=Path, help="AI-reviewed override mappings JSON path")
	parser.add_argument("--candidates", type=Path, help="write top candidates JSON for AI review")
	parser.add_argument("--candidate-limit", type=int, default=8, help="top candidate count per wiki item")
	parser.add_argument("--threshold", type=float, default=0.88, help="automatic fuzzy match threshold")
	parser.add_argument(
		"--area-map",
		action="append",
		default=[],
		help="wiki-to-db area mapping, e.g. PAL=EUR,AUS; repeatable",
	)
	args = parser.parse_args()

	root = Path(__file__).resolve().parents[1]
	platform_dir = root / "src" / "main" / "resources" / "platform" / args.platform
	wiki_path = args.wiki or platform_dir / f"{args.platform}_wiki.json"
	db_path = args.db or platform_dir / f"{args.platform}_db.json"
	output_path = args.output or platform_dir / f"{args.platform}_wiki_db_match.json"

	wiki_data = load_json(wiki_path)
	db_data = load_json(db_path)
	area_map = parse_area_map(args.area_map)
	overrides = load_overrides(args.overrides)

	result = build_result(wiki_data, db_data, area_map, overrides, args.threshold)
	write_json(output_path, result)

	if args.candidates:
		candidates: dict[str, dict[str, list[dict[str, Any]]]] = {}
		for wiki_area, wiki_items in wiki_data.items():
			db_areas = area_map.get(wiki_area, [wiki_area])
			db_items = [item for db_area in db_areas for item in db_data.get(db_area, [])]
			candidates[wiki_area] = top_candidates(wiki_items, db_items, args.candidate_limit)
		write_json(args.candidates, candidates)

	for section in ("match", "mismatch", "extra"):
		print(section)
		for area, items in result[section].items():
			print(f"  {area}: {len(items)}")

	return 0


if __name__ == "__main__":
	raise SystemExit(main())






