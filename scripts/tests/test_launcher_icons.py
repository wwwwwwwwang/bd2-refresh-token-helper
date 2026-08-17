import pathlib
import struct
import unittest
import xml.etree.ElementTree as ET


ROOT = pathlib.Path(__file__).parents[2]
RES = ROOT / "app" / "src" / "main" / "res"
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"


def png_size(path: pathlib.Path) -> tuple[int, int]:
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise AssertionError(f"Not a PNG file: {path}")
    return struct.unpack(">II", data[16:24])


class LauncherIconTest(unittest.TestCase):
    def test_manifest_uses_launcher_icons(self) -> None:
        manifest = ET.parse(ROOT / "app" / "src" / "main" / "AndroidManifest.xml")
        application = manifest.getroot().find("application")
        self.assertIsNotNone(application)
        self.assertEqual("@mipmap/ic_launcher", application.get(f"{ANDROID_NS}icon"))
        self.assertEqual(
            "@mipmap/ic_launcher_round",
            application.get(f"{ANDROID_NS}roundIcon"),
        )

    def test_legacy_icons_have_android_density_sizes(self) -> None:
        expected = {
            "mdpi": 48,
            "hdpi": 72,
            "xhdpi": 96,
            "xxhdpi": 144,
            "xxxhdpi": 192,
        }
        for density, size in expected.items():
            with self.subTest(density=density):
                icon = RES / f"mipmap-{density}" / "ic_launcher.png"
                round_icon = RES / f"mipmap-{density}" / "ic_launcher_round.png"
                self.assertEqual((size, size), png_size(icon))
                self.assertEqual((size, size), png_size(round_icon))

    def test_adaptive_foregrounds_have_android_density_sizes(self) -> None:
        expected = {
            "mdpi": 108,
            "hdpi": 162,
            "xhdpi": 216,
            "xxhdpi": 324,
            "xxxhdpi": 432,
        }
        for density, size in expected.items():
            with self.subTest(density=density):
                foreground = (
                    RES / f"mipmap-{density}" / "ic_launcher_foreground.png"
                )
                self.assertEqual((size, size), png_size(foreground))

    def test_adaptive_icon_xml_references_photo_foreground(self) -> None:
        for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
            with self.subTest(name=name):
                root = ET.parse(RES / "mipmap-anydpi-v26" / name).getroot()
                background = root.find("background")
                foreground = root.find("foreground")
                self.assertEqual(
                    "@color/ic_launcher_background",
                    background.get(f"{ANDROID_NS}drawable"),
                )
                self.assertEqual(
                    "@mipmap/ic_launcher_foreground",
                    foreground.get(f"{ANDROID_NS}drawable"),
                )


if __name__ == "__main__":
    unittest.main()
