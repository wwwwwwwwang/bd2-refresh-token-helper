import pathlib
import re
import unittest


ROOT = pathlib.Path(__file__).parents[2]
APP_SOURCE = (
    ROOT
    / "app"
    / "src"
    / "main"
    / "java"
    / "ci"
    / "us"
    / "bd2"
    / "tokenhelper"
    / "TokenHelperApp.kt"
)


class TokenCaptureNavigationTest(unittest.TestCase):
    def test_capture_does_not_clear_data_or_reload_home(self) -> None:
        source = APP_SOURCE.read_text(encoding="utf-8")
        match = re.search(
            r"onToken = (?P<body>store::capture|\{.*?\n\s*\})[,\n]",
            source,
            re.DOTALL,
        )
        self.assertIsNotNone(match)
        body = match.group("body")
        self.assertIn("store::capture", body)
        self.assertNotIn("clearBrowsingData", body)
        self.assertNotIn("controller.reload", body)
        self.assertNotIn("controller.goHome", body)

    def test_manual_refresh_action_remains_available(self) -> None:
        source = APP_SOURCE.read_text(encoding="utf-8")
        self.assertIn(
            'TextButton(onClick = controller::reload) { Text("刷新") }',
            source,
        )


if __name__ == "__main__":
    unittest.main()
