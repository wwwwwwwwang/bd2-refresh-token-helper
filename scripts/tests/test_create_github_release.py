import importlib.util
import pathlib
import unittest


MODULE_PATH = pathlib.Path(__file__).parents[1] / "create_github_release.py"
SPEC = importlib.util.spec_from_file_location("create_github_release", MODULE_PATH)
create_github_release = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(create_github_release)


class RetryReleaseTest(unittest.TestCase):
    def test_retries_transient_failure_then_succeeds(self) -> None:
        attempts = []

        def action() -> None:
            attempts.append(len(attempts) + 1)
            if len(attempts) < 3:
                raise create_github_release.ReleaseCommandError(
                    "HTTP 503: Service Unavailable"
                )

        create_github_release.run_with_retry(
            action,
            max_attempts=5,
            initial_delay_seconds=0,
            sleep=lambda _: None,
        )

        self.assertEqual([1, 2, 3], attempts)

    def test_does_not_retry_permanent_failure(self) -> None:
        attempts = []

        def action() -> None:
            attempts.append(len(attempts) + 1)
            raise create_github_release.ReleaseCommandError("HTTP 422: invalid tag")

        with self.assertRaises(create_github_release.ReleaseCommandError):
            create_github_release.run_with_retry(
                action,
                max_attempts=5,
                initial_delay_seconds=0,
                sleep=lambda _: None,
            )

        self.assertEqual([1], attempts)

    def test_fails_after_retry_limit(self) -> None:
        attempts = []

        def action() -> None:
            attempts.append(len(attempts) + 1)
            raise create_github_release.ReleaseCommandError(
                "HTTP 503: Service Unavailable"
            )

        with self.assertRaises(create_github_release.ReleaseCommandError):
            create_github_release.run_with_retry(
                action,
                max_attempts=4,
                initial_delay_seconds=0,
                sleep=lambda _: None,
            )

        self.assertEqual([1, 2, 3, 4], attempts)


if __name__ == "__main__":
    unittest.main()
