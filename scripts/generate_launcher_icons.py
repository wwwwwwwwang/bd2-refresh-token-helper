#!/usr/bin/env python3
import argparse
import shutil
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
ARTWORK = ROOT / "artwork" / "launcher-icon-source.jpg"
LEGACY_SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}
FOREGROUND_SIZES = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}


def center_square(image: Image.Image) -> Image.Image:
    edge = min(image.size)
    left = (image.width - edge) // 2
    top = (image.height - edge) // 2
    return image.crop((left, top, left + edge, top + edge)).convert("RGB")


def save_png(image: Image.Image, path: Path, size: int) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    resized = image.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(path, format="PNG", optimize=True)


def generate(source: Path) -> None:
    ARTWORK.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, ARTWORK)
    with Image.open(source) as original:
        square = center_square(original)
        for density, size in LEGACY_SIZES.items():
            directory = RES / f"mipmap-{density}"
            save_png(square, directory / "ic_launcher.png", size)
            save_png(square, directory / "ic_launcher_round.png", size)
        for density, size in FOREGROUND_SIZES.items():
            save_png(
                square,
                RES / f"mipmap-{density}" / "ic_launcher_foreground.png",
                size,
            )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    args = parser.parse_args()
    generate(args.source.resolve())


if __name__ == "__main__":
    main()
