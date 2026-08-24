#!/usr/bin/env python3
"""Fetch ScreenScraper game details listed in a semicolon-delimited CSV.

For each row in a ``*_ss.csv`` file, the script reads ``Game ID``, calls the
ScreenScraper ``jeuInfos.php`` API sequentially, and writes only the
``response.jeu`` object to ``<platform>/games/{gameId}.json``.
"""

from __future__ import annotations

import argparse
import csv
import gzip
import http.client
import json
import ssl
import sys
import time
from pathlib import Path
from typing import Any
from urllib.parse import urlencode


API_HOST = "api.screenscraper.fr"
API_PATH = "/api2/jeuInfos.php"
DEFAULT_API_PARAMS = {
    "devid": "muldjord",
    "devpassword": "uWu5VRc9QDVMPpD8",
    "softname": "skyscraper3.20.3",
    "output": "json",
    "ssid": "zjkiki",
    "sspassword": "zjkiki225",
}
DEFAULT_INPUT = Path("ai/nes/nes_ss.csv")


class ScreenScraperError(RuntimeError):
    """Raised when a ScreenScraper request or response is invalid."""


def read_game_ids(csv_path: Path, limit: int | None = None) -> list[str]:
    with csv_path.open("r", encoding="utf-8-sig", newline="") as csv_file:
        reader = csv.DictReader(csv_file, delimiter=";")
        if reader.fieldnames != ["Game ID", "Game Name"]:
            raise ValueError(f"Unexpected CSV header in {csv_path}: {reader.fieldnames!r}")

        game_ids: list[str] = []
        seen: set[str] = set()
        for line_number, row in enumerate(reader, start=2):
            game_id = (row.get("Game ID") or "").strip()
            if not game_id:
                raise ValueError(f"Missing Game ID at {csv_path}:{line_number}")
            if not game_id.isdigit():
                raise ValueError(f"Non-numeric Game ID at {csv_path}:{line_number}: {game_id!r}")
            if game_id in seen:
                raise ValueError(f"Duplicate Game ID at {csv_path}:{line_number}: {game_id}")
            seen.add(game_id)
            game_ids.append(game_id)
            if limit is not None and len(game_ids) >= limit:
                break

    return game_ids


def build_path(game_id: str) -> str:
    params = dict(DEFAULT_API_PARAMS)
    params["gameid"] = game_id
    return f"{API_PATH}?{urlencode(params)}"


class ScreenScraperClient:
    def __init__(self, timeout: float, verify_tls: bool) -> None:
        context = None if verify_tls else ssl._create_unverified_context()
        self.connection = http.client.HTTPSConnection(API_HOST, timeout=timeout, context=context)

    def close(self) -> None:
        self.connection.close()

    def fetch_game(self, game_id: str) -> dict[str, Any]:
        headers = {
            "User-Agent": "retroboy-ss-processor/1.0",
            "Accept-Encoding": "gzip",
            "Connection": "keep-alive",
        }
        try:
            self.connection.request("GET", build_path(game_id), headers=headers)
            response = self.connection.getresponse()
            status = response.status
            reason = response.reason
            content_encoding = response.getheader("Content-Encoding", "").lower()
            body = response.read()
        except TimeoutError as exc:
            raise ScreenScraperError(f"Timeout for gameId={game_id}") from exc
        except OSError as exc:
            raise ScreenScraperError(f"Network error for gameId={game_id}: {exc}") from exc

        if status != 200:
            raise ScreenScraperError(f"HTTP {status} for gameId={game_id}: {reason}")

        if content_encoding == "gzip":
            try:
                body = gzip.decompress(body)
            except OSError as exc:
                raise ScreenScraperError(f"Invalid gzip response for gameId={game_id}: {exc}") from exc

        try:
            payload = json.loads(body.decode("utf-8"))
        except json.JSONDecodeError as exc:
            raise ScreenScraperError(f"Invalid JSON for gameId={game_id}: {exc}") from exc

        return extract_jeu(game_id, payload)


def extract_jeu(game_id: str, payload: dict[str, Any]) -> dict[str, Any]:

    header = payload.get("header")
    if not isinstance(header, dict):
        raise ScreenScraperError(f"Missing header for gameId={game_id}")
    if str(header.get("success", "")).lower() != "true":
        error = header.get("error") or "unknown ScreenScraper error"
        raise ScreenScraperError(f"API failure for gameId={game_id}: {error}")

    response = payload.get("response")
    if not isinstance(response, dict):
        raise ScreenScraperError(f"Missing response for gameId={game_id}")
    jeu = response.get("jeu")
    if not isinstance(jeu, dict):
        raise ScreenScraperError(f"Missing response.jeu for gameId={game_id}")
    if str(jeu.get("id", "")) != game_id:
        raise ScreenScraperError(f"Mismatched response.jeu.id for gameId={game_id}: {jeu.get('id')!r}")

    return jeu


def write_game(output_dir: Path, game_id: str, jeu: dict[str, Any]) -> Path:
    output_dir.mkdir(parents=True, exist_ok=True)
    output_path = output_dir / f"{game_id}.json"
    output_path.write_text(json.dumps(jeu, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return output_path


def default_output_dir(csv_path: Path) -> Path:
    return csv_path.parent / "games"


def default_report_path(csv_path: Path) -> Path:
    return csv_path.with_name(f"{csv_path.stem}_report.txt")


def default_aggregate_path(csv_path: Path) -> Path:
    return csv_path.with_suffix(".json")


def validate_game_json(output_path: Path, game_id: str) -> None:
    try:
        data = json.loads(output_path.read_text(encoding="utf-8"))
    except OSError as exc:
        raise ScreenScraperError(f"Cannot read JSON for gameId={game_id}: {output_path}: {exc}") from exc
    except json.JSONDecodeError as exc:
        raise ScreenScraperError(f"Invalid saved JSON for gameId={game_id}: {output_path}: {exc}") from exc

    if not isinstance(data, dict):
        raise ScreenScraperError(f"Saved JSON is not an object for gameId={game_id}: {output_path}")
    if "id" not in data:
        raise ScreenScraperError(f"Saved JSON missing id field for gameId={game_id}: {output_path}")
    if str(data.get("id")) != game_id:
        raise ScreenScraperError(f"Saved JSON id mismatch for gameId={game_id}: {output_path}: {data.get('id')!r}")


def write_report_line(
    report_file: Any,
    index: int,
    total: int,
    game_id: str,
    status: str,
    output_path: Path,
    message: str,
) -> None:
    safe_message = message.replace("\t", " ").replace("\n", " ").replace("\r", " ")
    report_file.write(f"{index}/{total}\t{game_id}\t{status}\t{output_path}\t{safe_message}\n")
    report_file.flush()


def append_unique(values: list[str], value: object) -> None:
    if not isinstance(value, str):
        return
    normalized = value.strip()
    if normalized and normalized not in values:
        values.append(normalized)


def text_field(value: object) -> str | None:
    if not isinstance(value, dict):
        return None
    text = value.get("text")
    if not isinstance(text, str):
        return None
    text = text.strip()
    return text or None


def ss_game_entry(game_id: str, game: dict[str, Any]) -> dict[str, object] | None:
    names = game.get("noms")
    if not isinstance(names, list):
        return None

    titles: list[str] = []
    release_areas: list[str] = []
    for name in names:
        if not isinstance(name, dict) or name.get("region") == "ss":
            continue
        append_unique(titles, name.get("text"))
        append_unique(release_areas, name.get("region"))

    if not titles:
        return None

    release_dates: list[str] = []
    dates = game.get("dates")
    if isinstance(dates, list):
        for date in dates:
            if isinstance(date, dict):
                append_unique(release_dates, date.get("text"))

    developer = text_field(game.get("developpeur"))
    publisher = text_field(game.get("editeur"))

    return {
        "id": int(game_id),
        "titles": titles,
        "developers": [developer] if developer else [],
        "publishers": [publisher] if publisher else [],
        "releaseAreas": release_areas,
        "releaseDates": release_dates,
    }


def write_ss_json(csv_path: Path, games_dir: Path, output_path: Path) -> int:
    game_ids = read_game_ids(csv_path)
    entries: list[dict[str, object]] = []
    skipped_only_ss = 0

    for game_id in game_ids:
        game_path = games_dir / f"{game_id}.json"
        validate_game_json(game_path, game_id)
        game = json.loads(game_path.read_text(encoding="utf-8"))
        entry = ss_game_entry(game_id, game)
        if entry is None:
            skipped_only_ss += 1
            continue
        entries.append(entry)

    output_path.write_text(json.dumps(entries, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {len(entries)} games to {output_path}; skipped {skipped_only_ss} games without non-ss names")
    return len(entries)


def process_games(
    csv_path: Path,
    output_dir: Path,
    report_path: Path,
    timeout: float,
    delay: float,
    limit: int | None,
    dry_run: bool,
    force_refresh: bool,
    verify_tls: bool,
) -> None:
    game_ids = read_game_ids(csv_path, limit=limit)
    print(f"Loaded {len(game_ids)} game IDs from {csv_path}")

    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_file = report_path.open("w", encoding="utf-8", newline="")
    report_file.write("index\tgameId\tstatus\tjsonPath\tmessage\n")

    try:
        if dry_run:
            for index, game_id in enumerate(game_ids, start=1):
                output_path = output_dir / f"{game_id}.json"
                write_report_line(report_file, index, len(game_ids), game_id, "DRY_RUN", output_path, "not requested")
            preview = ", ".join(game_ids[:10])
            suffix = "..." if len(game_ids) > 10 else ""
            print(f"Dry run only. First IDs: {preview}{suffix}")
            print(f"Wrote report to {report_path}")
            return

        output_dir.mkdir(parents=True, exist_ok=True)
        client = ScreenScraperClient(timeout=timeout, verify_tls=verify_tls)
        for index, game_id in enumerate(game_ids, start=1):
            output_path = output_dir / f"{game_id}.json"
            if output_path.exists() and not force_refresh:
                try:
                    validate_game_json(output_path, game_id)
                except Exception as exc:
                    write_report_line(report_file, index, len(game_ids), game_id, "FAIL_EXISTING_INVALID", output_path, str(exc))
                    raise
                write_report_line(report_file, index, len(game_ids), game_id, "SKIP_EXISTING_VALID", output_path, "json verified")
                print(f"[{index}/{len(game_ids)}] skip existing valid {output_path}")
                continue

            try:
                print(f"[{index}/{len(game_ids)}] fetch gameId={game_id}")
                jeu = client.fetch_game(game_id)
                written_path = write_game(output_dir, game_id, jeu)
                validate_game_json(written_path, game_id)
                write_report_line(report_file, index, len(game_ids), game_id, "DOWNLOAD_OK", written_path, "json verified")
                print(f"[{index}/{len(game_ids)}] wrote and verified {written_path}")
                if delay > 0 and index < len(game_ids):
                    time.sleep(delay)
            except Exception as exc:
                write_report_line(report_file, index, len(game_ids), game_id, "FAIL", output_path, str(exc))
                raise
    finally:
        if "client" in locals():
            client.close()
        report_file.close()
    print(f"Wrote report to {report_path}")


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT, help="Input CSV path")
    parser.add_argument("--output-dir", type=Path, help="Directory for {gameId}.json files; defaults to <input-dir>/games")
    parser.add_argument("--report", type=Path, help="Execution report path; defaults to <input-stem>_report.txt next to input")
    parser.add_argument("--aggregate-output", type=Path, help="Aggregated JSON path; defaults to <input>.json")
    parser.add_argument("--timeout", type=float, default=60.0, help="Per-request timeout in seconds")
    parser.add_argument("--delay", type=float, default=0.0, help="Delay between requests in seconds")
    parser.add_argument("--limit", type=int, help="Process only the first N rows")
    parser.add_argument("--dry-run", action="store_true", help="Validate CSV and print IDs without calling the API")
    parser.add_argument(
        "--force-refresh",
        action="store_true",
        help="Call the API even when {gameId}.json already exists; by default existing files are skipped",
    )
    parser.add_argument(
        "--insecure-skip-tls-verify",
        action="store_true",
        help="Disable HTTPS certificate verification; use only when local Python certificate verification is broken",
    )
    return parser


def main() -> int:
    args = build_arg_parser().parse_args()
    if args.limit is not None and args.limit < 1:
        print("--limit must be greater than 0", file=sys.stderr)
        return 2
    if args.timeout <= 0:
        print("--timeout must be greater than 0", file=sys.stderr)
        return 2
    if args.delay < 0:
        print("--delay must be greater than or equal to 0", file=sys.stderr)
        return 2

    try:
        csv_path: Path = args.input
        output_dir: Path = args.output_dir or default_output_dir(csv_path)
        process_games(
            csv_path=csv_path,
            output_dir=output_dir,
            report_path=args.report or default_report_path(csv_path),
            timeout=args.timeout,
            delay=args.delay,
            limit=args.limit,
            dry_run=args.dry_run,
            force_refresh=args.force_refresh,
            verify_tls=not args.insecure_skip_tls_verify,
        )
        if not args.dry_run and args.limit is None:
            write_ss_json(csv_path, output_dir, args.aggregate_output or default_aggregate_path(csv_path))
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())


