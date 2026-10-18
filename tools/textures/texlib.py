"""Bibliothèque de dessin procédural pour les textures des Limbes (PIL + numpy).

Toutes les textures du mod sont produites par ces fonctions : elles constituent la
« source » des assets. Style visé : pixel-art 16x16 cohérent avec Minecraft, palettes
douces (nacre, or pâle, bleu profond, verre lumineux), bruit discret, biseaux légers.
"""
from __future__ import annotations

import math
import random
import zlib
from dataclasses import dataclass

import numpy as np
from PIL import Image, ImageDraw

RGBA = tuple[int, int, int, int]


def hex_to_rgba(h: str, a: int = 255) -> RGBA:
    h = h.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a


def clamp(v: float, lo: float = 0, hi: float = 255) -> int:
    return int(max(lo, min(hi, round(v))))


def shade(c: RGBA, f: float) -> RGBA:
    """Éclaircit (f>1) ou assombrit (f<1) une couleur en conservant l'alpha."""
    return clamp(c[0] * f), clamp(c[1] * f), clamp(c[2] * f), c[3]


def mix(a: RGBA, b: RGBA, t: float) -> RGBA:
    return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(4))  # type: ignore[return-value]


def tint(c: RGBA, toward: RGBA, t: float) -> RGBA:
    return mix(c, (toward[0], toward[1], toward[2], c[3]), t)


class Rng:
    def __init__(self, seed: int):
        self.r = random.Random(seed)

    def f(self) -> float:
        return self.r.random()

    def i(self, a: int, b: int) -> int:
        return self.r.randint(a, b)

    def choice(self, seq):
        return self.r.choice(seq)


def value_noise(w: int, h: int, scale: float, seed: int, octaves: int = 2, tile: bool = True) -> np.ndarray:
    """Bruit de valeur [0,1], périodique (tuilable) si `tile`."""
    rng = np.random.default_rng(seed)
    total = np.zeros((h, w), dtype=np.float64)
    amp, norm = 1.0, 0.0
    for o in range(octaves):
        cells = max(1, int(round(scale / (2 ** o))))
        gw, gh = max(1, w // cells), max(1, h // cells)
        grid = rng.random((gh, gw))
        ys = np.linspace(0, gh, h, endpoint=False)
        xs = np.linspace(0, gw, w, endpoint=False)
        y0 = np.floor(ys).astype(int)
        x0 = np.floor(xs).astype(int)
        fy = ys - y0
        fx = xs - x0
        fy = fy * fy * (3 - 2 * fy)
        fx = fx * fx * (3 - 2 * fx)
        y1 = (y0 + 1) % gh if tile else np.minimum(y0 + 1, gh - 1)
        x1 = (x0 + 1) % gw if tile else np.minimum(x0 + 1, gw - 1)
        a = grid[np.ix_(y0, x0)]
        b = grid[np.ix_(y0, x1)]
        c = grid[np.ix_(y1, x0)]
        d = grid[np.ix_(y1, x1)]
        top = a + (b - a) * fx[None, :]
        bot = c + (d - c) * fx[None, :]
        layer = top + (bot - top) * fy[:, None]
        total += layer * amp
        norm += amp
        amp *= 0.5
    return total / norm


def new(w: int = 16, h: int = 16, color: RGBA = (0, 0, 0, 0)) -> Image.Image:
    return Image.new("RGBA", (w, h), color)


def fill_noise(img: Image.Image, base: RGBA, variation: float = 0.08, scale: float = 4, seed: int = 0,
               box: tuple[int, int, int, int] | None = None, palette: list[RGBA] | None = None,
               octaves: int = 2) -> None:
    """Remplit `box` (x0,y0,x1,y1 exclusif) de `base` modulé par un bruit ; `palette` remplace la
    modulation par un choix de teintes proches selon le bruit (rendu plus « pixel art »)."""
    x0, y0, x1, y1 = box or (0, 0, img.width, img.height)
    w, h = x1 - x0, y1 - y0
    n = value_noise(w, h, scale, seed, octaves=octaves)
    px = img.load()
    for y in range(h):
        for x in range(w):
            v = n[y, x]
            if palette:
                idx = min(len(palette) - 1, int(v * len(palette)))
                px[x0 + x, y0 + y] = palette[idx]
            else:
                px[x0 + x, y0 + y] = shade(base, 1 + (v - 0.5) * 2 * variation)


def speckle(img: Image.Image, color: RGBA, count: int, seed: int, box: tuple[int, int, int, int] | None = None,
            size: int = 1) -> None:
    rng = Rng(seed)
    x0, y0, x1, y1 = box or (0, 0, img.width, img.height)
    px = img.load()
    for _ in range(count):
        x, y = rng.i(x0, x1 - size), rng.i(y0, y1 - size)
        for dx in range(size):
            for dy in range(size):
                px[x + dx, y + dy] = color


def bevel(img: Image.Image, box: tuple[int, int, int, int], light: float = 1.15, dark: float = 0.85, width: int = 1) -> None:
    """Biseau : bord haut/gauche éclairci, bas/droite assombri."""
    x0, y0, x1, y1 = box
    px = img.load()
    for i in range(width):
        for x in range(x0 + i, x1 - i):
            px[x, y0 + i] = shade(px[x, y0 + i], light)
            px[x, y1 - 1 - i] = shade(px[x, y1 - 1 - i], dark)
        for y in range(y0 + i + 1, y1 - 1 - i):
            px[x0 + i, y] = shade(px[x0 + i, y], light)
            px[x1 - 1 - i, y] = shade(px[x1 - 1 - i, y], dark)


def rect(img: Image.Image, box: tuple[int, int, int, int], color: RGBA) -> None:
    x0, y0, x1, y1 = box
    px = img.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            if 0 <= x < img.width and 0 <= y < img.height:
                px[x, y] = color


def outline(img: Image.Image, box: tuple[int, int, int, int], color: RGBA) -> None:
    x0, y0, x1, y1 = box
    px = img.load()
    for x in range(x0, x1):
        px[x, y0] = color
        px[x, y1 - 1] = color
    for y in range(y0, y1):
        px[x0, y] = color
        px[x1 - 1, y] = color


def put(img: Image.Image, x: int, y: int, color: RGBA) -> None:
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)


def gradient_v(img: Image.Image, box: tuple[int, int, int, int], top: RGBA, bottom: RGBA) -> None:
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        t = (y - y0) / max(1, (y1 - y0 - 1))
        c = mix(top, bottom, t)
        for x in range(x0, x1):
            put(img, x, y, c)


def bricks(img: Image.Image, base: RGBA, mortar: RGBA, bw: int = 8, bh: int = 4, seed: int = 0, variation: float = 0.06) -> None:
    """Motif de briques décalées sur tout le canevas."""
    rng = Rng(seed)
    w, h = img.width, img.height
    rect(img, (0, 0, w, h), mortar)
    rows = h // bh
    for r in range(rows):
        off = (bw // 2) if r % 2 else 0
        for c in range(-1, w // bw + 1):
            x0 = c * bw + off
            y0 = r * bh
            col = shade(base, 1 + (rng.f() - 0.5) * 2 * variation)
            for y in range(y0, y0 + bh - 1):
                for x in range(x0, x0 + bw - 1):
                    put(img, x % w, y, col)
            # léger biseau par brique
            for x in range(x0, x0 + bw - 1):
                put(img, x % w, y0, shade(col, 1.1))
            for y in range(y0, y0 + bh - 1):
                put(img, (x0 + bw - 2) % w, y, shade(col, 0.9))


def planks(img: Image.Image, base: RGBA, line: RGBA, seed: int = 0, plank_h: int = 4, vertical: bool = False) -> None:
    rng = Rng(seed)
    w, h = img.width, img.height
    n = value_noise(w, h, 5, seed, octaves=2)
    px = img.load()
    for y in range(h):
        for x in range(w):
            v = n[y, x]
            px[x, y] = shade(base, 1 + (v - 0.5) * 0.16)
    count = (w if vertical else h) // plank_h
    for i in range(count):
        offset = rng.i(0, 7)
        if vertical:
            x = i * plank_h
            for y in range(h):
                px[x, y] = line
            # joint court
            for y in range(offset, min(h, offset + 1)):
                for xx in range(x + 1, x + plank_h):
                    put(img, xx, y, line)
        else:
            y = i * plank_h
            for x in range(w):
                px[x, y] = line
            for x in range(offset, min(w, offset + 1)):
                for yy in range(y + 1, y + plank_h):
                    put(img, x, yy, line)
    # fil du bois : stries fines
    for i in range(count):
        for k in range(3):
            if vertical:
                x = i * plank_h + rng.i(1, plank_h - 1)
                y0 = rng.i(0, h - 1)
                for y in range(y0, min(h, y0 + rng.i(2, 6))):
                    px[x, y] = shade(px[x, y], 0.92)
            else:
                y = i * plank_h + rng.i(1, plank_h - 1)
                x0 = rng.i(0, w - 1)
                for x in range(x0, min(w, x0 + rng.i(2, 6))):
                    px[x, y] = shade(px[x, y], 0.92)


def log_side(img: Image.Image, bark: RGBA, dark: RGBA, seed: int = 0) -> None:
    rng = Rng(seed)
    n = value_noise(img.width, img.height, 3, seed, octaves=2)
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            v = n[y, x]
            px[x, y] = mix(bark, dark, 0.3 + v * 0.5) if v > 0.5 else shade(bark, 0.95 + v * 0.15)
    for _ in range(6):
        x = rng.i(0, img.width - 1)
        y0 = rng.i(0, img.height - 1)
        for y in range(y0, min(img.height, y0 + rng.i(3, 9))):
            px[x, y] = dark


def log_top(img: Image.Image, bark: RGBA, dark: RGBA, core: RGBA, seed: int = 0) -> None:
    w, h = img.width, img.height
    cx, cy = (w - 1) / 2, (h - 1) / 2
    rng = Rng(seed)
    px = img.load()
    for y in range(h):
        for x in range(w):
            d = math.hypot(x - cx, y - cy)
            if d > 6.6:
                px[x, y] = shade(bark, 0.9 + rng.f() * 0.2)
            elif d > 5.8:
                px[x, y] = dark
            else:
                ring = int(d) % 2
                c = shade(core, 1.0 if ring else 0.9)
                px[x, y] = c


def leaves(img: Image.Image, light: RGBA, dark: RGBA, holes: int = 14, seed: int = 0, accent: RGBA | None = None, accents: int = 3) -> None:
    rng = Rng(seed)
    n = value_noise(img.width, img.height, 3, seed, octaves=2)
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            v = n[y, x]
            px[x, y] = mix(dark, light, v)
    for _ in range(holes):
        put(img, rng.i(0, img.width - 1), rng.i(0, img.height - 1), (0, 0, 0, 0))
    if accent:
        for _ in range(accents):
            put(img, rng.i(0, img.width - 1), rng.i(0, img.height - 1), accent)


def cross_plant(img: Image.Image, stem: RGBA, stem_dark: RGBA, tips: RGBA, seed: int = 0, blades: int = 5,
                base_y: int = 15, min_h: int = 6, max_h: int = 13, glow: RGBA | None = None) -> None:
    """Plante en croix (type herbes hautes) : brins fins partant du bas."""
    rng = Rng(seed)
    xs = sorted(set(rng.i(2, 13) for _ in range(blades)))
    for x in xs:
        h = rng.i(min_h, max_h)
        lean = rng.choice([-1, 0, 1])
        for i in range(h):
            y = base_y - i
            xx = x + (lean if i > h // 2 else 0)
            c = stem if i < h - 2 else tips
            if i % 3 == 0:
                c = stem_dark if i < h - 2 else tips
            put(img, xx, y, c)
        if glow and h > 7:
            put(img, x + lean, base_y - h, glow)


def paste_scaled(dst: Image.Image, src: Image.Image, x: int, y: int) -> None:
    dst.alpha_composite(src, (x, y))


# ----------------------------------------------------------------------- boîtes UV (entités)

@dataclass
class Box:
    """Cuboïde w×h×d dont la texture est dépliée à (u,v) selon la convention Minecraft."""
    u: int
    v: int
    w: int
    h: int
    d: int


def paint_box(img: Image.Image, box: Box, color: RGBA, seed: int = 0, variation: float = 0.06,
              face_shade: dict[str, float] | None = None, texture=None) -> None:
    """Peint les six faces d'une boîte. `texture(face, w, h, seed) -> Image` permet un rendu par face."""
    fs = {"top": 1.12, "bottom": 0.78, "right": 0.92, "front": 1.0, "left": 0.92, "back": 0.95}
    if face_shade:
        fs.update(face_shade)
    u, v, w, h, d = box.u, box.v, box.w, box.h, box.d
    faces = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + d + w + d, v + d, w, h),
    }
    for name, (fx, fy, fw, fh) in faces.items():
        if fw <= 0 or fh <= 0:
            continue
        if texture is not None:
            face_img = texture(name, fw, fh, seed + zlib.crc32(name.encode("utf-8")) % 1000)
            img.alpha_composite(face_img, (fx, fy))
        else:
            face = new(fw, fh)
            fill_noise(face, shade(color, fs[name]), variation=variation, scale=3, seed=seed + len(name))
            img.alpha_composite(face, (fx, fy))


def face_rect(box: Box, face: str) -> tuple[int, int, int, int]:
    u, v, w, h, d = box.u, box.v, box.w, box.h, box.d
    return {
        "top": (u + d, v, u + d + w, v + d),
        "bottom": (u + d + w, v, u + d + w + w, v + d),
        "right": (u, v + d, u + d, v + d + h),
        "front": (u + d, v + d, u + d + w, v + d + h),
        "left": (u + d + w, v + d, u + d + w + d, v + d + h),
        "back": (u + d + w + d, v + d, u + d + w + d + w, v + d + h),
    }[face]


def draw_on_face(img: Image.Image, box: Box, face: str, fn) -> None:
    """Appelle fn(put_local) où put_local(x, y, color) dessine en coordonnées locales de la face."""
    x0, y0, x1, y1 = face_rect(box, face)

    def put_local(x: int, y: int, color: RGBA) -> None:
        if 0 <= x < (x1 - x0) and 0 <= y < (y1 - y0):
            put(img, x0 + x, y0 + y, color)

    fn(put_local)


def save(img: Image.Image, path: str) -> None:
    import os
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
