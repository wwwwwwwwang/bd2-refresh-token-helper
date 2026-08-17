#!/usr/bin/env python3
import argparse
import re
import subprocess
import sys
import time
from collections.abc import Callable


class ReleaseCommandError(RuntimeError):
    pass


TRANSIENT_ERROR = re.compile(
    r"HTTP\s+(?:408|429|5\d\d)"
    r"|Service Unavailable"
    r"|unexpected EOF"
    r"|timed? out"
    r"|temporarily unavailable"
    r"|connection (?:reset|refused)",
    re.IGNORECASE,
)


def is_retryable(error: ReleaseCommandError) -> bool:
    return TRANSIENT_ERROR.search(str(error)) is not None


def run_with_retry(
    action: Callable[[], None],
    *,
    max_attempts: int,
    initial_delay_seconds: float,
    sleep: Callable[[float], None] = time.sleep,
) -> None:
    for attempt in range(1, max_attempts + 1):
        try:
            action()
            return
        except ReleaseCommandError as error:
            if attempt == max_attempts or not is_retryable(error):
                raise
            delay = initial_delay_seconds * (2 ** (attempt - 1))
            print(
                f"Transient GitHub Release error; retrying in {delay:g}s "
                f"({attempt}/{max_attempts})",
                file=sys.stderr,
            )
            sleep(delay)


def create_release(tag: str, title: str, files: list[str]) -> None:
    command = [
        "gh",
        "release",
        "create",
        tag,
        *files,
        "--verify-tag",
        "--title",
        title,
        "--generate-notes",
    ]
    result = subprocess.run(command, text=True, capture_output=True, check=False)
    if result.stdout:
        print(result.stdout, end="")
    if result.returncode != 0:
        detail = result.stderr.strip() or f"gh exited with {result.returncode}"
        raise ReleaseCommandError(detail)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--tag", required=True)
    parser.add_argument("--title", required=True)
    parser.add_argument("--max-attempts", type=int, default=5)
    parser.add_argument("--initial-delay-seconds", type=float, default=3)
    parser.add_argument("files", nargs="+")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        run_with_retry(
            lambda: create_release(args.tag, args.title, args.files),
            max_attempts=args.max_attempts,
            initial_delay_seconds=args.initial_delay_seconds,
        )
    except ReleaseCommandError as error:
        print(str(error), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
