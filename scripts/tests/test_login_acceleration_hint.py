import pathlib
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


class LoginAccelerationHintTest(unittest.TestCase):
    def test_hint_is_shown_above_the_web_content(self) -> None:
        source = APP_SOURCE.read_text(encoding="utf-8")
        hint_index = source.find('"请开启加速后登录"')
        web_view_index = source.find("TokenWebViewHost(")

        self.assertGreaterEqual(hint_index, 0)
        self.assertGreater(web_view_index, hint_index)
        self.assertIn("MaterialTheme.colorScheme.secondaryContainer", source)


if __name__ == "__main__":
    unittest.main()
