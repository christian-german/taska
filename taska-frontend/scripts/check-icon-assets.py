#!/usr/bin/env python3
"""Verify that all web and Tauri icon representations use Taska signal green."""
from pathlib import Path
import struct
import zlib

ROOT = Path(__file__).resolve().parents[1]
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
GREEN = bytes((0x14, 0xB3, 0x7D))
ORANGE = bytes((0xFF, 0x8A, 0x3D))


def png_payloads(data: bytes):
    position = 0
    while (start := data.find(PNG_SIGNATURE, position)) >= 0:
        cursor = start + len(PNG_SIGNATURE)
        chunks = []
        while True:
            length = struct.unpack_from(">I", data, cursor)[0]
            kind = data[cursor + 4 : cursor + 8]
            payload = data[cursor + 8 : cursor + 8 + length]
            chunks.append((kind, payload))
            cursor += length + 12
            if kind == b"IEND":
                break
        yield chunks
        position = cursor


def rgba_pixels(chunks):
    header = next(payload for kind, payload in chunks if kind == b"IHDR")
    width, height, depth, colour = struct.unpack_from(">IIBB", header)
    assert (depth, colour) == (8, 6), f"unexpected PNG format: depth={depth}, colour={colour}"
    compressed = b"".join(payload for kind, payload in chunks if kind == b"IDAT")
    raw = zlib.decompress(compressed)
    stride = width * 4
    previous = bytearray(stride)
    for y in range(height):
        filter_type = raw[y * (stride + 1)]
        source = raw[y * (stride + 1) + 1 : (y + 1) * (stride + 1)]
        current = bytearray(stride)
        for x, value in enumerate(source):
            left = current[x - 4] if x >= 4 else 0
            above = previous[x]
            upper_left = previous[x - 4] if x >= 4 else 0
            if filter_type == 1:
                value += left
            elif filter_type == 2:
                value += above
            elif filter_type == 3:
                value += (left + above) // 2
            elif filter_type == 4:
                estimate = left + above - upper_left
                distances = (abs(estimate - left), abs(estimate - above), abs(estimate - upper_left))
                value += (left, above, upper_left)[distances.index(min(distances))]
            elif filter_type != 0:
                raise AssertionError(f"unsupported PNG filter: {filter_type}")
            current[x] = value & 0xFF
        yield from (bytes(current[x : x + 3]) for x in range(0, stride, 4) if current[x + 3])
        previous = current


svg = (ROOT / "public/favicon.svg").read_text()
assert "#14B37D" in svg and "#FF8A3D" not in svg

assets = [ROOT / "public/favicon.ico"]
assets += sorted((ROOT / "src-tauri/icons").glob("*.png"))
assets += [ROOT / "src-tauri/icons/icon.ico", ROOT / "src-tauri/icons/icon.icns"]
assets += sorted((ROOT / "src-tauri/icons/ios").glob("*.png"))
assets += sorted((ROOT / "src-tauri/icons/android").glob("mipmap-*/*.png"))
assert len(assets) == 51, f"expected 51 raster/container assets, found {len(assets)}"

for asset in assets:
    payloads = list(png_payloads(asset.read_bytes()))
    assert payloads, f"{asset.relative_to(ROOT)} contains no PNG representation"
    pixels = [pixel for payload in payloads for pixel in rgba_pixels(payload)]
    assert GREEN in pixels, f"{asset.relative_to(ROOT)} does not contain exact #14B37D"
    assert ORANGE not in pixels, f"{asset.relative_to(ROOT)} still contains #FF8A3D"

adaptive_background = (ROOT / "src-tauri/icons/android/values/ic_launcher_background.xml").read_text()
assert "#14B37D" in adaptive_background and "#FF8A3D" not in adaptive_background
print(f"Verified the SVG and {len(assets)} web/Tauri icon assets.")
