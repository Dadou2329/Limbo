#!/usr/bin/env python3
"""Génère toutes les textures originales du mod Les Limbes (blocs, objets, entités, particules, GUI).

Usage : python3 tools/textures/gen_textures.py [--out src/main/resources/assets/les_limbes/textures]

Ce script est la source des assets : modifier une palette ou une fonction puis relancer suffit
à régénérer l'ensemble de façon déterministe (graines fixes).
"""
from __future__ import annotations

import argparse
import json
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from PIL import Image  # noqa: E402

from texlib import (Box, Rng, bevel, bricks, cross_plant, draw_on_face, fill_noise, gradient_v, hex_to_rgba,  # noqa: E402
                    leaves, log_side, log_top, mix, new, outline, paint_box, planks, put, rect, save, shade,
                    speckle, value_noise)

# ----------------------------------------------------------------------------- palette
P = {
    "nacre": hex_to_rgba("E9E3DA"), "nacre_sombre": hex_to_rgba("CFC7BC"), "nacre_clair": hex_to_rgba("F7F3EC"),
    "nacre_rose": hex_to_rgba("F0CBD8"), "nacre_bleu": hex_to_rgba("C2D8EF"), "nacre_menthe": hex_to_rgba("CDE9DF"),
    "mortier": hex_to_rgba("B7AEA3"),
    "or": hex_to_rgba("E3C97E"), "or_clair": hex_to_rgba("F8EBB6"), "or_sombre": hex_to_rgba("B4934A"),
    "bleu": hex_to_rgba("22305E"), "bleu_clair": hex_to_rgba("3C57A0"), "bleu_nuit": hex_to_rgba("151E3D"),
    "verre": hex_to_rgba("DDF3FF"), "verre_sombre": hex_to_rgba("A9D2EC"),
    "herbe": hex_to_rgba("B4C6AC"), "herbe_clair": hex_to_rgba("D3DFCB"), "herbe_sombre": hex_to_rgba("8FA589"),
    "terre": hex_to_rgba("7F6F76"), "terre_clair": hex_to_rgba("96868C"), "terre_sombre": hex_to_rgba("63555C"),
    "ecorce": hex_to_rgba("B99D8F"), "ecorce_sombre": hex_to_rgba("87695F"), "bois": hex_to_rgba("E3C9AF"),
    "bois_ligne": hex_to_rgba("B8977E"), "bois_coeur": hex_to_rgba("D9B99A"),
    "feuille": hex_to_rgba("F4D9E3"), "feuille_sombre": hex_to_rgba("B8789C"), "feuille_ombre": hex_to_rgba("8E5A7C"),
    "feuille_lueur": hex_to_rgba("FFF6E0"),
    "fibre": hex_to_rgba("DFE8EA"), "fibre_sombre": hex_to_rgba("B4C4CA"), "blanc": hex_to_rgba("FFFFFF"),
    "lueur": hex_to_rgba("FFF8E2"), "metal": hex_to_rgba("4A4858"), "metal_clair": hex_to_rgba("6A6880"),
    "cristal": hex_to_rgba("E3F1FF"), "cristal_sombre": hex_to_rgba("B9D3EC"),
    "porte_sombre": hex_to_rgba("3B3748"), "porte_glyphe": hex_to_rgba("6B6686"),
    "noir": hex_to_rgba("1A1822"),
}

OUT = "src/main/resources/assets/les_limbes/textures"
MANIFEST: dict[str, list[str]] = {"block": [], "item": [], "entity": [], "particle": [], "gui": [], "misc": []}


def out_path(kind: str, name: str) -> str:
    MANIFEST.setdefault(kind, []).append(name)
    return os.path.join(OUT, kind, name + ".png")


def write_mcmeta(path: str, data: dict) -> None:
    with open(path + ".mcmeta", "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2)


# ----------------------------------------------------------------------------- blocs
def tex_pierre_nacree(seed: int = 1) -> Image.Image:
    img = new()
    fill_noise(img, P["nacre"], palette=[P["nacre_sombre"], P["nacre"], P["nacre"], P["nacre_clair"]], scale=4, seed=seed)
    # éclats irisés discrets
    speckle(img, P["nacre_rose"], 3, seed + 1)
    speckle(img, P["nacre_bleu"], 3, seed + 2)
    speckle(img, P["nacre_menthe"], 2, seed + 3)
    # fissures claires façon pierre
    rng = Rng(seed + 9)
    for _ in range(3):
        x, y = rng.i(0, 15), rng.i(0, 15)
        for k in range(rng.i(2, 5)):
            put(img, (x + k) % 16, (y + (k // 2)) % 16, P["nacre_sombre"])
    return img


def tex_pierre_nacree_taillee() -> Image.Image:
    img = new()
    bricks(img, P["nacre"], P["mortier"], bw=8, bh=4, seed=11, variation=0.05)
    speckle(img, P["nacre_rose"], 2, 12)
    speckle(img, P["nacre_bleu"], 2, 13)
    return img


def tex_pierre_nacree_polie() -> Image.Image:
    img = new()
    fill_noise(img, P["nacre_clair"], variation=0.03, scale=8, seed=21)
    bevel(img, (0, 0, 16, 16), 1.06, 0.9)
    outline(img, (0, 0, 16, 16), P["nacre_sombre"])
    return img


def tex_pierre_nacree_sculptee() -> Image.Image:
    """Motif de couronne (trois pointes) gravé, évoquant la royauté."""
    img = tex_pierre_nacree_polie()
    g = P["or_sombre"]
    o = P["or"]
    pts = [(4, 6), (8, 4), (12, 6), (5, 9), (11, 9)]
    for x, y in pts:
        put(img, x, y, o)
    for x in range(4, 13):
        put(img, x, 10, g)
        put(img, x, 11, g)
    for x in range(5, 12):
        put(img, x, 9, g)
    put(img, 4, 7, g); put(img, 4, 8, g); put(img, 12, 7, g); put(img, 12, 8, g)
    put(img, 8, 5, g); put(img, 8, 6, g); put(img, 8, 7, g); put(img, 8, 8, g)
    put(img, 6, 8, g); put(img, 10, 8, g); put(img, 6, 7, o); put(img, 10, 7, o)
    put(img, 8, 10, P["bleu_clair"])
    return img


def tex_herbe_argentee_top() -> Image.Image:
    img = new()
    fill_noise(img, P["herbe"], palette=[P["herbe_sombre"], P["herbe"], P["herbe"], P["herbe_clair"]], scale=3, seed=31)
    speckle(img, P["blanc"], 2, 32)
    return img


def tex_terre_des_limbes() -> Image.Image:
    img = new()
    fill_noise(img, P["terre"], palette=[P["terre_sombre"], P["terre"], P["terre"], P["terre_clair"]], scale=3, seed=41)
    speckle(img, P["nacre_sombre"], 3, 42)
    return img


def tex_herbe_argentee_side() -> Image.Image:
    img = tex_terre_des_limbes()
    top = tex_herbe_argentee_top()
    rng = Rng(43)
    for x in range(16):
        h = 2 + (1 if rng.f() > 0.5 else 0) + (1 if rng.f() > 0.8 else 0)
        for y in range(h):
            put(img, x, y, top.getpixel((x, y)))
    return img


def tex_buche_aube_side() -> Image.Image:
    img = new()
    log_side(img, P["ecorce"], P["ecorce_sombre"], seed=51)
    return img


def tex_buche_aube_top() -> Image.Image:
    img = new()
    log_top(img, P["ecorce"], P["ecorce_sombre"], P["bois_coeur"], seed=52)
    return img


def tex_planches_aube() -> Image.Image:
    img = new()
    planks(img, P["bois"], P["bois_ligne"], seed=61)
    return img


def tex_feuilles_aube() -> Image.Image:
    """Feuillage rose nacré : grain de feuilles contrasté (ombres, creux transparents, pétales clairs)."""
    img = new()
    leaves(img, P["feuille"], P["feuille_sombre"], holes=14, seed=71, accent=P["feuille_lueur"], accents=5)
    speckle(img, P["feuille_ombre"], count=26, seed=74)
    speckle(img, P["feuille_sombre"], count=18, seed=75)
    speckle(img, P["nacre_clair"], count=10, seed=76)
    return img


def tex_pousse_aube() -> Image.Image:
    img = new()
    # petit arbuste : tronc fin + boule de feuilles roses
    for y in range(9, 16):
        put(img, 8, y, P["ecorce_sombre"])
    rng = Rng(72)
    for y in range(3, 11):
        for x in range(4, 13):
            if math.hypot(x - 8, y - 7) <= 3.8 and rng.f() > 0.15:
                put(img, x, y, mix(P["feuille_sombre"], P["feuille"], rng.f()))
    put(img, 7, 5, P["feuille_lueur"]); put(img, 10, 8, P["feuille_lueur"])
    return img


def tex_fibres_nacrees() -> Image.Image:
    img = new()
    cross_plant(img, P["fibre"], P["fibre_sombre"], P["blanc"], seed=81, blades=6, min_h=7, max_h=14, glow=P["lueur"])
    return img


def tex_herbe_de_laube() -> Image.Image:
    img = new()
    cross_plant(img, P["herbe_sombre"], shade(P["herbe_sombre"], 0.85), P["herbe_clair"], seed=91, blades=4, min_h=5, max_h=9)
    # petits bourgeons dorés
    for x, y in [(5, 7), (10, 6), (7, 9)]:
        put(img, x, y, P["or"])
    return img


def tex_fleur_de_rosee() -> Image.Image:
    img = new()
    for y in range(8, 16):
        put(img, 8, y, P["herbe_sombre"])
    put(img, 7, 12, P["herbe_sombre"]); put(img, 6, 12, P["herbe_sombre"]); put(img, 9, 10, P["herbe_sombre"])
    petals = [(8, 4), (6, 5), (10, 5), (5, 7), (11, 7), (6, 9), (10, 9), (8, 10)]
    for x, y in petals:
        put(img, x, y, P["blanc"])
        put(img, x, y + 1 if y < 9 else y - 1, P["nacre_clair"])
    for x in range(7, 10):
        for y in range(6, 9):
            put(img, x, y, P["blanc"])
    put(img, 8, 7, P["bleu_clair"]); put(img, 7, 7, P["verre_sombre"]); put(img, 9, 7, P["verre_sombre"])
    return img


def tex_lanterne_nacree() -> Image.Image:
    """Dépliage compatible avec le modèle de lanterne vanilla (template_lantern)."""
    img = new()
    # corps : zone (0,2)-(6,9) et (0,9)-(6,13) ; crochet (11,1)-(14,6) selon le modèle vanilla.
    rect(img, (0, 0, 16, 16), (0, 0, 0, 0))
    rect(img, (0, 2, 6, 9), P["metal"])
    rect(img, (1, 3, 5, 8), P["lueur"])
    rect(img, (2, 4, 4, 7), P["or_clair"])
    rect(img, (0, 9, 6, 15), P["metal"])
    rect(img, (1, 10, 5, 14), P["lueur"])
    rect(img, (6, 2, 12, 9), P["metal"])
    rect(img, (7, 3, 11, 8), P["lueur"])
    rect(img, (6, 9, 12, 15), P["metal"])
    rect(img, (7, 10, 11, 14), P["lueur"])
    # chaîne / crochet
    rect(img, (11, 1, 14, 6), P["metal_clair"])
    for y in range(1, 6):
        put(img, 12, y, P["metal"])
    return img


def tex_cristal_de_nacre() -> Image.Image:
    img = new()
    fill_noise(img, P["cristal"], palette=[P["cristal_sombre"], P["cristal"], P["cristal"], P["blanc"]], scale=5, seed=101)
    # facettes : diagonales claires
    for i in range(16):
        put(img, i, (i * 2) % 16, shade(P["blanc"], 1.0))
        put(img, (i + 8) % 16, (15 - i) % 16, P["cristal_sombre"])
    speckle(img, P["or_clair"], 2, 102)
    return img


def tex_porte_dormante() -> Image.Image:
    img = new()
    fill_noise(img, P["porte_sombre"], variation=0.06, scale=4, seed=111)
    # glyphe de porte : rectangle avec arc
    g = P["porte_glyphe"]
    for y in range(4, 14):
        put(img, 5, y, g); put(img, 10, y, g)
    for x in range(5, 11):
        put(img, x, 13, g)
    for x, y in [(6, 3), (7, 2), (8, 2), (9, 3)]:
        put(img, x, y, g)
    put(img, 8, 9, shade(g, 1.2))
    return img


def tex_porte_active(frames: int = 8) -> Image.Image:
    """Texture animée (frames empilés verticalement) : volutes lumineuses bleues et paillettes d'or."""
    img = new(16, 16 * frames)
    for f in range(frames):
        t = f / frames
        n1 = value_noise(16, 16, 4, 200 + f, octaves=2)
        n2 = value_noise(16, 16, 6, 300 + f, octaves=1)
        for y in range(16):
            for x in range(16):
                v = 0.5 + 0.5 * math.sin((x + y) * 0.6 + t * 2 * math.pi + n1[y, x] * 3.0)
                c = mix(P["bleu_clair"], P["verre"], v)
                c = mix(c, P["blanc"], max(0.0, n2[y, x] - 0.6) * 2)
                put(img, x, f * 16 + y, (c[0], c[1], c[2], 225))
        rng = Rng(400 + f)
        for _ in range(3):
            put(img, rng.i(0, 15), f * 16 + rng.i(0, 15), P["or_clair"])
    return img


def tex_porte_d_aube(part: str) -> Image.Image:
    img = new()
    planks(img, P["bois"], P["bois_ligne"], seed=121 if part == "top" else 122, vertical=True, plank_h=5)
    outline(img, (0, 0, 16, 16), P["bois_ligne"])
    if part == "top":
        # petite fenêtre en verre lumineux
        rect(img, (5, 4, 11, 10), P["bois_ligne"])
        rect(img, (6, 5, 10, 9), P["verre"])
        put(img, 7, 6, P["blanc"])
    else:
        rect(img, (4, 6, 12, 7), P["bois_ligne"])
        put(img, 12, 8, P["or"]); put(img, 13, 8, P["or"])
    return img


def tex_dalle_de_rosee() -> Image.Image:
    img = new()
    fill_noise(img, P["nacre_clair"], palette=[P["nacre"], P["nacre_clair"], P["nacre_clair"], P["blanc"]], scale=3, seed=81)
    outline(img, (0, 0, 16, 16), P["nacre_bleu"])
    for x, y in [(4, 4), (11, 5), (7, 11), (12, 12), (3, 10)]:
        put(img, x, y, P["lueur"])
    return img


def tex_dalle_de_rosee_cachee() -> Image.Image:
    img = new()
    eau = hex_to_rgba("6EC4E8", 70)
    fill_noise(img, eau, variation=0.1, scale=4, seed=82)
    for x, y in [(4, 4), (11, 5), (7, 11), (12, 12)]:
        put(img, x, y, hex_to_rgba("FFF8E2", 120))
    return img


def tex_meule_royale() -> Image.Image:
    img = new()
    fill_noise(img, P["nacre_sombre"], palette=[P["mortier"], P["nacre_sombre"], P["nacre"], P["nacre_clair"]], scale=3, seed=83)
    for a in range(0, 360, 30):
        x = int(round(7.5 + 6 * math.cos(math.radians(a)))); y = int(round(7.5 + 6 * math.sin(math.radians(a))))
        put(img, x, y, P["or_sombre"])
    rect(img, (6, 6, 10, 10), P["or"])
    put(img, 7, 7, P["bleu_clair"]); put(img, 8, 8, P["bleu_clair"])
    return img


def tex_meule_de_moulin() -> Image.Image:
    img = new()
    fill_noise(img, P["nacre_sombre"], palette=[P["mortier"], P["nacre_sombre"], P["nacre_sombre"], P["nacre"]], scale=3, seed=84)
    for y in range(0, 16, 4):
        for x in range(16):
            put(img, x, y, shade(P["nacre_sombre"], 0.8))
    return img


def tex_meule_de_moulin_top() -> Image.Image:
    img = new()
    fill_noise(img, P["nacre_sombre"], palette=[P["mortier"], P["nacre_sombre"], P["nacre"]], scale=3, seed=85)
    for a in range(0, 360, 20):
        for r in (3, 6):
            x = int(round(7.5 + r * math.cos(math.radians(a)))); y = int(round(7.5 + r * math.sin(math.radians(a))))
            put(img, x, y, shade(P["nacre_sombre"], 0.7))
    rect(img, (7, 7, 9, 9), P["noir"])
    return img


def tex_toile_de_moulin() -> Image.Image:
    img = new()
    fill_noise(img, hex_to_rgba("F4EFE6"), variation=0.08, scale=2, seed=86)
    for y in range(0, 16, 5):
        for x in range(16):
            put(img, x, y, hex_to_rgba("D9CFC0"))
    for x in (0, 15):
        for y in range(16):
            put(img, x, y, hex_to_rgba("B8977E"))
    return img


def tex_vitrine() -> Image.Image:
    """Verre de vitrine : panneau translucide bordé d'or, reflet diagonal."""
    img = new()
    verre = hex_to_rgba("DDF3FF", 90)
    for y in range(16):
        for x in range(16):
            put(img, x, y, verre)
    for d in range(16):
        if 3 <= d <= 12:
            put(img, d, 15 - d, hex_to_rgba("FFFFFF", 140))
    outline(img, (0, 0, 16, 16), P["or_sombre"])
    for x, y in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        put(img, x, y, P["or"])
    return img


def tex_vitrine_socle() -> Image.Image:
    img = new()
    fill_noise(img, P["nacre_clair"], variation=0.03, scale=8, seed=91)
    outline(img, (0, 0, 16, 16), P["or_sombre"])
    outline(img, (1, 1, 15, 15), P["nacre_sombre"])
    return img


def tex_porte_de_relais() -> Image.Image:
    """Pierre nacrée des portes de relais : blocs taillés, incrustations d'or."""
    img = new()
    bricks(img, P["nacre"], P["mortier"], bw=8, bh=4, seed=92, variation=0.05)
    for y in (2, 6, 10, 14):
        put(img, 3, y, P["or_sombre"]); put(img, 12, y, P["or_sombre"])
    put(img, 7, 7, P["or"]); put(img, 8, 8, P["or"]); put(img, 8, 7, P["or_clair"]); put(img, 7, 8, P["or_clair"])
    return img


def tex_porte_de_relais_voile() -> Image.Image:
    """Voile éveillé : bleu profond traversé de constellations, translucide."""
    img = new()
    for y in range(16):
        for x in range(16):
            t = y / 15.0
            put(img, x, y, hex_to_rgba("3C57A0", int(150 + 60 * (1 - t))))
    for x, y in [(3, 2), (11, 4), (6, 7), (13, 9), (2, 11), (9, 13), (12, 14), (5, 12)]:
        put(img, x, y, P["lueur"])
    for x, y in [(4, 2), (6, 8), (8, 13)]:
        put(img, x, y, hex_to_rgba("FFF8E2", 160))
    return img


def tex_porte_de_relais_voile_eteint() -> Image.Image:
    img = new()
    for y in range(16):
        for x in range(16):
            put(img, x, y, hex_to_rgba("151E3D", 110 if (x + y) % 3 else 140))
    for x, y in [(6, 7), (9, 13)]:
        put(img, x, y, hex_to_rgba("B8B4B0", 120))
    return img


# ----------------------------------------------------------------------------- blocs des régions (étape 5)
def tex_sable(base: str, sombre: str, clair: str, seed: int) -> Image.Image:
    img = new()
    fill_noise(img, hex_to_rgba(base), palette=[hex_to_rgba(sombre), hex_to_rgba(base), hex_to_rgba(base), hex_to_rgba(clair)], scale=2, seed=seed)
    speckle(img, hex_to_rgba(clair), 3, seed + 1)
    return img


def tex_corail(base: str, sombre: str, clair: str, seed: int) -> Image.Image:
    img = new()
    fill_noise(img, hex_to_rgba(base), palette=[hex_to_rgba(sombre), hex_to_rgba(base), hex_to_rgba(clair)], scale=3, seed=seed)
    rng = Rng(seed + 7)
    for _ in range(9):
        x, y = int(rng.f() * 14) + 1, int(rng.f() * 14) + 1
        put(img, x, y, hex_to_rgba(clair)); put(img, x, y - 1, hex_to_rgba(sombre)); put(img, x + 1, y, hex_to_rgba(sombre))
    return img


def tex_pierre_regionale(base: str, sombre: str, clair: str, mortier: str, seed: int, taillee: bool = False, polie: bool = False, sculptee: bool = False) -> Image.Image:
    img = new()
    if taillee:
        bricks(img, hex_to_rgba(base), hex_to_rgba(mortier), bw=8, bh=4, seed=seed, variation=0.05)
    elif polie:
        fill_noise(img, hex_to_rgba(clair), variation=0.03, scale=8, seed=seed)
        outline(img, (0, 0, 16, 16), hex_to_rgba(sombre))
    elif sculptee:
        fill_noise(img, hex_to_rgba(base), variation=0.03, scale=8, seed=seed)
        outline(img, (0, 0, 16, 16), hex_to_rgba(sombre))
        for d in range(2, 14):
            put(img, d, 7, hex_to_rgba(mortier)); put(img, d, 8, hex_to_rgba(mortier)); put(img, 7, d, hex_to_rgba(mortier)); put(img, 8, d, hex_to_rgba(mortier))
        rect(img, (6, 6, 10, 10), hex_to_rgba(clair))
        put(img, 7, 7, hex_to_rgba(sombre)); put(img, 8, 8, hex_to_rgba(sombre))
    else:
        fill_noise(img, hex_to_rgba(base), palette=[hex_to_rgba(sombre), hex_to_rgba(base), hex_to_rgba(base), hex_to_rgba(clair)], scale=4, seed=seed)
        speckle(img, hex_to_rgba(clair), 2, seed + 3)
    return img


def tex_verre_de_montagne() -> Image.Image:
    img = new()
    for y in range(16):
        for x in range(16):
            put(img, x, y, hex_to_rgba("C9E8F6", 110))
    for d in range(16):
        put(img, d, 15 - d, hex_to_rgba("FFFFFF", 160))
        if d < 12:
            put(img, d + 4, 15 - d, hex_to_rgba("FFFFFF", 90))
    outline(img, (0, 0, 16, 16), hex_to_rgba("A9D2EC", 200))
    return img


def tex_cristal_resonnant() -> Image.Image:
    img = new()
    fill_noise(img, hex_to_rgba("7FD6D6"), palette=[hex_to_rgba("3C9AA8"), hex_to_rgba("7FD6D6"), hex_to_rgba("BDF2F2")], scale=4, seed=131)
    for i in range(16):
        put(img, i, (i * 3) % 16, hex_to_rgba("FFFFFF"))
        put(img, (i + 5) % 16, (15 - i) % 16, hex_to_rgba("3C9AA8"))
    return img


def tex_colonne(base: str, clair: str, alpha: int, seed: int) -> Image.Image:
    img = new()
    rng = Rng(seed)
    for y in range(16):
        for x in range(16):
            put(img, x, y, hex_to_rgba(base, alpha))
    for _ in range(14):
        x, y = int(rng.f() * 16), int(rng.f() * 16)
        put(img, x, y, hex_to_rgba(clair, min(255, alpha + 80)))
        put(img, x, (y + 1) % 16, hex_to_rgba(clair, min(255, alpha + 40)))
    return img


def tex_cadran(h: int) -> Image.Image:
    img = new()
    fill_noise(img, hex_to_rgba("E6D2A6"), variation=0.03, scale=8, seed=140 + h)
    outline(img, (0, 0, 16, 16), hex_to_rgba("B4934A"))
    for a in range(0, 360, 30):
        x = int(round(7.5 + 6 * math.cos(math.radians(a)))); y = int(round(7.5 + 6 * math.sin(math.radians(a))))
        put(img, x, y, hex_to_rgba("B4934A"))
    angle = {0: -90, 1: 0, 2: 90, 3: 180}[h]
    for r in range(1, 6):
        x = int(round(7.5 + r * math.cos(math.radians(angle - 90)))); y = int(round(7.5 + r * math.sin(math.radians(angle - 90))))
        put(img, x, y, hex_to_rgba("22305E"))
    put(img, 7, 7, hex_to_rgba("E3C97E")); put(img, 8, 8, hex_to_rgba("E3C97E"))
    return img


def tex_pave_horaire(reflet: bool) -> Image.Image:
    img = new()
    if reflet:
        for y in range(16):
            for x in range(16):
                put(img, x, y, hex_to_rgba("F8EBB6", 60 if (x + y) % 2 else 90))
        return img
    fill_noise(img, hex_to_rgba("F0DCA8"), palette=[hex_to_rgba("D9BE86"), hex_to_rgba("F0DCA8"), hex_to_rgba("FFF3C8")], scale=3, seed=150)
    outline(img, (0, 0, 16, 16), hex_to_rgba("B4934A"))
    for x, y in [(4, 4), (11, 5), (7, 11), (12, 12)]:
        put(img, x, y, hex_to_rgba("FFF8E2"))
    return img


def tex_pont_de_verre(trame: bool) -> Image.Image:
    img = new()
    if trame:
        for y in range(16):
            for x in range(16):
                put(img, x, y, hex_to_rgba("A9D2EC", 50 if (x % 4 and y % 4) else 110))
        return img
    for y in range(16):
        for x in range(16):
            put(img, x, y, hex_to_rgba("BDE3F5", 150))
    for d in range(16):
        put(img, d, 15 - d, hex_to_rgba("FFFFFF", 200))
    outline(img, (0, 0, 16, 16), hex_to_rgba("7FB8DC", 230))
    return img


def tex_lanterne_royale() -> Image.Image:
    img = tex_lanterne_nacree()
    for y in range(16):
        for x in range(16):
            px = img.getpixel((x, y))
            if px[:3] == P["metal"][:3]:
                put(img, x, y, P["or_sombre"])
            elif px[:3] == P["metal_clair"][:3]:
                put(img, x, y, P["or"])
    return img


def tex_lanterne_royale_eteinte() -> Image.Image:
    """Lanterne royale sans flamme : même ferronnerie dorée, vitre grise."""
    img = tex_lanterne_royale()
    for y in range(16):
        for x in range(16):
            px = img.getpixel((x, y))
            if px[3] and px[:3] not in (P["or_sombre"][:3], P["or"][:3]):
                g = int(0.3 * px[0] + 0.5 * px[1] + 0.2 * px[2])
                put(img, x, y, (g // 2 + 40, g // 2 + 40, g // 2 + 48, px[3]))
    return img


def tex_porte_royale() -> Image.Image:
    """Voile de la porte royale : pierre de couronne et filets d'or, étoile au centre."""
    img = new()
    for y in range(16):
        for x in range(16):
            bord = x in (0, 15) or y in (0, 15)
            c = "C9A227" if bord else ("E6E8F4" if (x + y) % 2 else "DDE0EE")
            put(img, x, y, hex_to_rgba(c))
    for (x, y) in [(7, 5), (8, 5), (7, 10), (8, 10), (5, 7), (5, 8), (10, 7), (10, 8), (7, 7), (8, 7), (7, 8), (8, 8), (6, 6), (9, 6), (6, 9), (9, 9)]:
        put(img, x, y, hex_to_rgba("FFE08A"))
    return img


def tex_pont_royal(trame: bool) -> Image.Image:
    img = new()
    if trame:
        for y in range(16):
            for x in range(16):
                put(img, x, y, hex_to_rgba("E3C97E", 40 if (x % 4 and y % 4) else 100))
        return img
    for y in range(16):
        for x in range(16):
            put(img, x, y, hex_to_rgba("F2E3B8", 170))
    for d in range(16):
        put(img, d, d, hex_to_rgba("FFF2B0", 220))
        put(img, 15 - d, d, hex_to_rgba("FFF2B0", 220))
    for x in range(16):
        put(img, x, 0, hex_to_rgba("C9A227", 230))
        put(img, x, 15, hex_to_rgba("C9A227", 230))
    return img


def tex_item_sceau_royal() -> Image.Image:
    """Sceau du roi disparu : disque d'or pâle à l'étoile bleue, ruban."""
    img = new()
    for y in range(16):
        for x in range(16):
            d = ((x - 8) ** 2 + (y - 7) ** 2) ** 0.5
            if d <= 5.5:
                put(img, x, y, hex_to_rgba("E3C97E" if d > 4.5 else "FFE08A"))
    for (x, y) in [(8, 4), (8, 5), (8, 6), (8, 7), (8, 8), (8, 9), (8, 10), (5, 7), (6, 7), (7, 7), (9, 7), (10, 7), (11, 7), (6, 5), (10, 9), (6, 9), (10, 5)]:
        put(img, x, y, hex_to_rgba("2E3A6E"))
    for y in range(12, 16):
        put(img, 6, y, hex_to_rgba("B23A48"))
        put(img, 9, y, hex_to_rgba("B23A48"))
    return img


def tex_petale(base: str, sombre: str, clair: str, seed: int) -> Image.Image:
    img = new()
    fill_noise(img, hex_to_rgba(base), palette=[hex_to_rgba(sombre), hex_to_rgba(base), hex_to_rgba(base), hex_to_rgba(clair)], scale=4, seed=seed)
    for i in range(16):
        put(img, i, int(7.5 + 4 * math.sin(i / 2.5)), hex_to_rgba(clair))
        put(img, i, int(7.5 + 4 * math.sin(i / 2.5 + 1.5)), hex_to_rgba(sombre))
    return img


def tex_herbe_geants_top() -> Image.Image:
    img = new()
    fill_noise(img, hex_to_rgba("6FA85A"), palette=[hex_to_rgba("4E7F3E"), hex_to_rgba("6FA85A"), hex_to_rgba("6FA85A"), hex_to_rgba("9CCB7E")], scale=3, seed=160)
    speckle(img, hex_to_rgba("F0CBD8"), 2, 161)
    return img


def tex_herbe_geants_side() -> Image.Image:
    img = tex_terre_des_limbes()
    top = tex_herbe_geants_top()
    rng = Rng(162)
    for x in range(16):
        h = 2 + (1 if rng.f() > 0.5 else 0) + (1 if rng.f() > 0.7 else 0)
        for y in range(h):
            put(img, x, y, top.getpixel((x, y)))
    return img


def tex_buche_geant_side() -> Image.Image:
    img = new()
    log_side(img, hex_to_rgba("6B4A32"), hex_to_rgba("4A3222"), seed=170)
    return img


def tex_buche_geant_top() -> Image.Image:
    img = new()
    log_top(img, hex_to_rgba("6B4A32"), hex_to_rgba("4A3222"), hex_to_rgba("B88A5E"), seed=171)
    return img


def tex_planches_geant() -> Image.Image:
    img = new()
    planks(img, hex_to_rgba("A87C56"), hex_to_rgba("7A5638"), seed=172)
    return img


def tex_feuilles_geant() -> Image.Image:
    img = new()
    fill_noise(img, hex_to_rgba("4E8F3E"), palette=[hex_to_rgba("2F6B2A"), hex_to_rgba("4E8F3E"), hex_to_rgba("4E8F3E"), hex_to_rgba("7FBF5E")], scale=2, seed=173)
    rng = Rng(174)
    for _ in range(10):
        put(img, int(rng.f() * 16), int(rng.f() * 16), (0, 0, 0, 0))
    return img


def gen_blocks_regions() -> None:
    save(tex_sable("EDE4D0", "D6CBB2", "FFFBEE", 201), out_path("block", "sable_de_nacre"))
    save(tex_corail("F0A3B8", "C86F86", "FFD6E0", 202), out_path("block", "corail_de_nacre"))
    save(tex_corail("7FC8E8", "4A92B8", "C6ECFA", 203), out_path("block", "corail_azur"))
    save(tex_buche_geant_side(), out_path("block", "buche_de_geant"))
    save(tex_buche_geant_top(), out_path("block", "buche_de_geant_top"))
    save(tex_planches_geant(), out_path("block", "planches_de_geant"))
    save(tex_feuilles_geant(), out_path("block", "feuilles_de_geant"))
    save(tex_herbe_geants_top(), out_path("block", "herbe_des_geants_top"))
    save(tex_herbe_geants_side(), out_path("block", "herbe_des_geants_side"))
    save(tex_petale("F0A3C8", "C66F9A", "FFD6EC", 204), out_path("block", "petale_rose"))
    save(tex_petale("8FB5E8", "5C7FB8", "C9DCF6", 205), out_path("block", "petale_bleu"))
    save(tex_colonne("B8E986", "F4FFD6", 120, 206), out_path("block", "seve_ascendante"))
    save(tex_sable("F2E8D8", "DDD0B8", "FFFFFF", 207), out_path("block", "sable_d_opale"))
    save(tex_pierre_regionale("E6D2A6", "C9B07E", "F4E6C2", "B8A070", 208), out_path("block", "gres_des_heures"))
    save(tex_pierre_regionale("E6D2A6", "C9B07E", "F4E6C2", "B8A070", 209, taillee=True), out_path("block", "gres_des_heures_taille"))
    save(tex_pierre_regionale("C98A5E", "9A6240", "E0B088", "8A5A3C", 210), out_path("block", "pierre_des_heures"))
    for h in range(4):
        save(tex_cadran(h), out_path("block", f"cadran_des_heures_{h}"))
    save(tex_pave_horaire(False), out_path("block", "pave_horaire"))
    save(tex_pave_horaire(True), out_path("block", "pave_horaire_reflet"))
    save(tex_pierre_regionale("B9C8D4", "8FA3B4", "DCE6EE", "7F94A6", 211), out_path("block", "pierre_de_verre"))
    save(tex_pierre_regionale("B9C8D4", "8FA3B4", "DCE6EE", "7F94A6", 212, taillee=True), out_path("block", "pierre_de_verre_taillee"))
    save(tex_verre_de_montagne(), out_path("block", "verre_de_montagne"))
    save(tex_cristal_resonnant(), out_path("block", "cristal_resonnant"))
    save(tex_pont_de_verre(False), out_path("block", "pont_de_verre"))
    save(tex_pont_de_verre(True), out_path("block", "pont_de_verre_trame"))
    save(tex_colonne("A9E4F0", "FFFFFF", 110, 213), out_path("block", "courant_cristallin"))
    save(tex_pierre_regionale("EFE6D8", "CFC2AE", "FBF6EC", "D6C79C", 214), out_path("block", "pierre_de_couronne"))
    save(tex_pierre_regionale("EFE6D8", "CFC2AE", "FBF6EC", "D6C79C", 215, taillee=True), out_path("block", "pierre_de_couronne_taillee"))
    save(tex_pierre_regionale("EFE6D8", "CFC2AE", "FBF6EC", "D6C79C", 216, polie=True), out_path("block", "pierre_de_couronne_polie"))
    save(tex_pierre_regionale("EFE6D8", "CFC2AE", "FBF6EC", "E3C97E", 217, sculptee=True), out_path("block", "pierre_de_couronne_sculptee"))
    save(tex_lanterne_royale(), out_path("block", "lanterne_royale"))
    save(tex_lanterne_royale_eteinte(), out_path("block", "lanterne_royale_eteinte"))
    save(tex_porte_royale(), out_path("block", "porte_royale"))
    save(tex_pont_royal(False), out_path("block", "pont_royal"))
    save(tex_pont_royal(True), out_path("block", "pont_royal_trame"))
    save(tex_item_sceau_royal(), out_path("item", "sceau_royal"))


def gen_blocks() -> None:
    gen_blocks_regions()
    save(tex_vitrine(), out_path("block", "vitrine"))
    save(tex_vitrine_socle(), out_path("block", "vitrine_socle"))
    save(tex_porte_de_relais(), out_path("block", "porte_de_relais"))
    save(tex_porte_de_relais_voile(), out_path("block", "porte_de_relais_voile"))
    save(tex_porte_de_relais_voile_eteint(), out_path("block", "porte_de_relais_voile_eteint"))
    save(tex_dalle_de_rosee(), out_path("block", "dalle_de_rosee"))
    save(tex_dalle_de_rosee_cachee(), out_path("block", "dalle_de_rosee_cachee"))
    save(tex_meule_royale(), out_path("block", "meule_royale"))
    save(tex_meule_de_moulin(), out_path("block", "meule_de_moulin"))
    save(tex_meule_de_moulin_top(), out_path("block", "meule_de_moulin_top"))
    save(tex_toile_de_moulin(), out_path("block", "toile_de_moulin"))
    save(tex_pierre_nacree(), out_path("block", "pierre_nacree"))
    save(tex_pierre_nacree_taillee(), out_path("block", "pierre_nacree_taillee"))
    save(tex_pierre_nacree_polie(), out_path("block", "pierre_nacree_polie"))
    save(tex_pierre_nacree_sculptee(), out_path("block", "pierre_nacree_sculptee"))
    save(tex_herbe_argentee_top(), out_path("block", "herbe_argentee_top"))
    save(tex_herbe_argentee_side(), out_path("block", "herbe_argentee_side"))
    save(tex_terre_des_limbes(), out_path("block", "terre_des_limbes"))
    save(tex_buche_aube_side(), out_path("block", "buche_d_aube"))
    save(tex_buche_aube_top(), out_path("block", "buche_d_aube_top"))
    save(tex_planches_aube(), out_path("block", "planches_d_aube"))
    save(tex_feuilles_aube(), out_path("block", "feuilles_d_aube"))
    save(tex_pousse_aube(), out_path("block", "pousse_d_aube"))
    save(tex_fibres_nacrees(), out_path("block", "fibres_nacrees"))
    save(tex_herbe_de_laube(), out_path("block", "herbe_de_l_aube"))
    save(tex_fleur_de_rosee(), out_path("block", "fleur_de_rosee"))
    save(tex_lanterne_nacree(), out_path("block", "lanterne_nacree"))
    save(tex_cristal_de_nacre(), out_path("block", "cristal_de_nacre"))
    save(tex_porte_dormante(), out_path("block", "porte_des_limbes_dormante"))
    p = out_path("block", "porte_des_limbes_active")
    save(tex_porte_active(), p)
    write_mcmeta(p, {"animation": {"frametime": 3, "interpolate": True}})
    save(tex_porte_d_aube("top"), out_path("block", "porte_d_aube_top"))
    save(tex_porte_d_aube("bottom"), out_path("block", "porte_d_aube_bottom"))


# ----------------------------------------------------------------------------- objets
def item_outline(img: Image.Image, color) -> None:
    """Contour sombre d'un pixel autour des pixels opaques (lisibilité en inventaire)."""
    src = img.copy()
    for y in range(img.height):
        for x in range(img.width):
            if src.getpixel((x, y))[3] == 0:
                near = any(0 <= x + dx < img.width and 0 <= y + dy < img.height and src.getpixel((x + dx, y + dy))[3] > 0
                           for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)])
                if near:
                    put(img, x, y, color)


def tex_item_fragment_de_nacre() -> Image.Image:
    img = new()
    pts = [(7, 2), (8, 2), (6, 3), (9, 3), (5, 4), (10, 4), (4, 6), (11, 6), (4, 8), (10, 9), (5, 10), (9, 11), (6, 12), (8, 13), (7, 13)]
    # polygone plein par balayage approximatif
    shape = {(x, y) for y in range(2, 14) for x in range(3, 12) if (abs(x - 7.5) < (6 - abs(y - 7) * 0.55))}
    for x, y in shape:
        t = (x + y) / 26
        c = mix(P["nacre_rose"], P["nacre_bleu"], t)
        c = mix(c, P["nacre_clair"], 0.4)
        put(img, x, y, c)
    for x, y in pts:
        put(img, x, y, P["nacre_clair"])
    put(img, 6, 5, P["blanc"]); put(img, 7, 4, P["blanc"])
    item_outline(img, P["nacre_sombre"])
    return img


def tex_item_boussole_nacree() -> Image.Image:
    img = new()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d <= 6.5:
                put(img, x, y, P["nacre"] if d > 5.2 else P["bleu"])
    for x, y in [(7, 2), (8, 2), (7, 13), (8, 13), (2, 7), (2, 8), (13, 7), (13, 8)]:
        put(img, x, y, P["or_sombre"])
    # aiguille d'or
    for k in range(4):
        put(img, 7 + k, 8 - k, P["or_clair"])
        put(img, 8 - k, 8 + k, P["or"])
    put(img, 7, 7, P["blanc"]); put(img, 8, 8, P["blanc"])
    item_outline(img, P["metal"])
    return img


def tex_item_fibres_nacrees() -> Image.Image:
    img = new()
    rng = Rng(131)
    for i in range(7):
        x = 4 + i
        top = rng.i(2, 5)
        for y in range(top, 13):
            put(img, x + (1 if y > 9 and i % 2 else 0), y, P["fibre"] if (y + i) % 3 else P["fibre_sombre"])
    for x in range(4, 12):
        put(img, x, 9, P["or_sombre"])
    item_outline(img, P["fibre_sombre"])
    return img


def tex_item_herbe_de_laube() -> Image.Image:
    img = new()
    for k in range(10):
        put(img, 5 + k // 2, 13 - k, P["herbe_sombre"])
    for k in range(5):
        put(img, 6 + k, 10 - k, P["herbe"]); put(img, 7 + k, 9 - k, P["herbe_clair"])
        put(img, 4 + k // 2, 11 - k, P["herbe"])
    put(img, 10, 4, P["or"]); put(img, 11, 3, P["or_clair"])
    item_outline(img, shade(P["herbe_sombre"], 0.7))
    return img


def tex_item_bouillon_de_laube() -> Image.Image:
    img = new()
    bowl = hex_to_rgba("8B5E3C")
    for y in range(7, 14):
        for x in range(2, 14):
            if abs(x - 7.5) <= 6 - (y - 7) * 0.5:
                put(img, x, y, bowl)
    for x in range(3, 13):
        put(img, x, 7, P["lueur"]); put(img, x, 8, mix(P["lueur"], P["or"], 0.4))
    put(img, 6, 7, P["herbe_sombre"]); put(img, 9, 8, P["herbe_sombre"]); put(img, 7, 8, P["herbe"])
    item_outline(img, shade(bowl, 0.6))
    return img


def tex_item_fragment_de_couronne() -> Image.Image:
    img = new()
    g, gc, gs = P["or"], P["or_clair"], P["or_sombre"]
    for x in range(3, 13):
        put(img, x, 10, g); put(img, x, 11, gs)
    for x in range(3, 13):
        put(img, x, 9, gc if x % 3 == 0 else g)
    for x, h in [(3, 3), (7, 5), (11, 3)]:
        for y in range(9 - h, 9):
            put(img, x, y, g if y > 9 - h else gc)
        put(img, x, 9 - h, gc)
    put(img, 5, 8, g); put(img, 9, 8, g); put(img, 5, 7, g); put(img, 9, 7, g)
    put(img, 7, 10, P["bleu_clair"]); put(img, 7, 11, P["bleu"])
    put(img, 4, 10, P["verre_sombre"]); put(img, 10, 10, P["verre_sombre"])
    item_outline(img, shade(gs, 0.7))
    return img


def tex_item_epee_du_souverain() -> Image.Image:
    img = new()
    # lame diagonale (de bas-gauche vers haut-droite), couleur verre lumineux / bleu
    for k in range(9):
        x, y = 5 + k, 10 - k
        put(img, x, y, P["verre"])
        put(img, x, y + 1, P["verre_sombre"])
        put(img, x - 1, y, P["blanc"] if k % 2 else P["verre"])
    put(img, 14, 1, P["blanc"])
    # garde en or
    for k in range(-1, 3):
        put(img, 4 + k, 12 - k, P["or"]); put(img, 3 + k, 11 - k, P["or_clair"])
    put(img, 2, 12, P["or_sombre"]); put(img, 6, 8, P["or_sombre"])
    # poignée bleue
    put(img, 3, 13, P["bleu"]); put(img, 2, 14, P["bleu_clair"]); put(img, 1, 15, P["or"])
    # éclat d'étoile
    put(img, 10, 3, P["or_clair"]); put(img, 11, 2, P["blanc"]); put(img, 11, 4, P["or_clair"]); put(img, 12, 3, P["or_clair"])
    item_outline(img, P["bleu_nuit"])
    return img


def tex_item_cle_du_manoir() -> Image.Image:
    img = new()
    g, gs = P["or"], P["or_sombre"]
    for x, y in [(4, 3), (5, 2), (6, 2), (7, 3), (4, 4), (7, 4), (5, 5), (6, 5)]:
        put(img, x, y, g)
    put(img, 5, 3, P["bleu_clair"]); put(img, 6, 3, P["bleu_clair"]); put(img, 5, 4, P["bleu"]); put(img, 6, 4, P["bleu"])
    for y in range(6, 14):
        put(img, 6, y, g if y % 2 else gs)
    put(img, 7, 11, g); put(img, 8, 11, gs); put(img, 7, 13, g)
    item_outline(img, shade(gs, 0.7))
    return img


def tex_item_oeuf(base, dots) -> Image.Image:
    img = new()
    for y in range(2, 15):
        for x in range(3, 13):
            w = 4.6 if y > 6 else 4.6 - (6 - y) * 0.55
            if abs(x - 7.5) <= w and y < 15:
                put(img, x, y, base)
    for x, y in [(5, 5), (9, 7), (6, 10), (10, 11), (8, 4)]:
        put(img, x, y, dots)
    put(img, 5, 3, shade(base, 1.2))
    item_outline(img, shade(base, 0.55))
    return img


def tex_item_porte_d_aube() -> Image.Image:
    img = new()
    rect(img, (4, 1, 12, 15), P["bois"])
    outline(img, (4, 1, 12, 15), P["bois_ligne"])
    rect(img, (6, 3, 10, 6), P["verre"])
    put(img, 7, 4, P["blanc"])
    for y in range(7, 14, 2):
        put(img, 5, y, P["bois_ligne"]); put(img, 10, y, P["bois_ligne"])
    put(img, 10, 9, P["or"])
    item_outline(img, shade(P["bois_ligne"], 0.7))
    return img


def tex_item_sifflet_de_raie() -> Image.Image:
    """Sifflet d'os nacré, cordon bleu, embouchure dorée."""
    img = new()
    os_ = P["nacre"]
    for x in range(3, 13):
        put(img, x, 7, os_); put(img, x, 8, shade(os_, 0.85))
    for x in range(3, 7):
        put(img, x, 6, shade(os_, 1.1))
    put(img, 12, 6, P["or"]); put(img, 13, 7, P["or"]); put(img, 13, 8, P["or_sombre"])
    put(img, 5, 9, P["bleu_clair"]); put(img, 4, 10, P["bleu_clair"]); put(img, 3, 11, P["bleu"]); put(img, 2, 12, P["bleu"]); put(img, 2, 13, P["bleu_clair"])
    put(img, 8, 6, P["noir"]); put(img, 9, 6, P["noir"])
    return img


def tex_item_farine() -> Image.Image:
    """Sac de toile noué, farine qui déborde."""
    img = new()
    toile = hex_to_rgba("D9C7A8")
    for y in range(5, 15):
        w = 4 if y < 7 else 5
        for x in range(8 - w, 8 + w):
            put(img, x, y, shade(toile, 0.9 + 0.1 * ((x + y) % 2)))
    for x in range(5, 11):
        put(img, x, 4, hex_to_rgba("8E7260"))
        put(img, x, 3, P["blanc"])
    put(img, 6, 2, P["blanc"]); put(img, 9, 2, P["nacre_clair"])
    for x in range(4, 12):
        put(img, x, 14, shade(toile, 0.75))
    return img


def tex_item_pain_d_emile() -> Image.Image:
    """Miche dorée, grignée."""
    img = new()
    croute = hex_to_rgba("C98B45")
    mie = hex_to_rgba("F2DDB0")
    for y in range(5, 12):
        for x in range(2, 14):
            dx = abs(x - 7.5) / 6.5
            dy = abs(y - 8.5) / 3.5
            if dx * dx + dy * dy <= 1.05:
                put(img, x, y, shade(croute, 0.85 + 0.25 * (1 - dy)))
    for x in range(4, 12, 3):
        put(img, x, 6, mie); put(img, x + 1, 7, mie)
    for x in range(3, 13):
        put(img, x, 12, shade(croute, 0.7))
    return img


def tex_item_journal_de_bord() -> Image.Image:
    img = new()
    cuir = hex_to_rgba("22305E")
    rect(img, (3, 2, 13, 14), cuir)
    rect(img, (4, 3, 12, 13), shade(cuir, 1.15))
    for y in range(4, 13, 2):
        put(img, 5, y, P["or"]); put(img, 10, y, P["or"])
    rect(img, (6, 6, 10, 8), P["or_clair"])
    put(img, 3, 7, P["blanc"]); put(img, 3, 8, P["blanc"])
    return img


def tex_item_ragout_du_domaine() -> Image.Image:
    img = new()
    bol = hex_to_rgba("8E7260")
    for x in range(3, 13):
        put(img, x, 9, bol); put(img, x, 10, shade(bol, 0.9)); put(img, x, 11, shade(bol, 0.8))
    for x in range(4, 12):
        put(img, x, 12, shade(bol, 0.7)); put(img, x, 8, hex_to_rgba("C98B45"))
    for x in range(5, 11):
        put(img, x, 7, hex_to_rgba("D9A066"))
    put(img, 6, 6, P["herbe_sombre"]); put(img, 9, 6, hex_to_rgba("E07A5A")); put(img, 7, 5, P["blanc"])
    return img


def tex_item_simple(forme: str, base: str, sombre: str, clair: str, seed: int) -> Image.Image:
    """Icône 16x16 : forme ("rond", "goutte", "fruit", "poisson", "sablier", "eclat", "pain", "fiole", "perle") en palette donnée."""
    img = new()
    b, s, c = hex_to_rgba(base), hex_to_rgba(sombre), hex_to_rgba(clair)
    if forme == "poisson":
        for x in range(3, 12):
            h = 2 + int(2.5 * math.sin((x - 3) / 8 * math.pi))
            for y in range(8 - h, 8 + h):
                put(img, x, y, b)
        for y in range(5, 11):
            put(img, 12, y, s); put(img, 13, y if y % 2 else 8, s)
        put(img, 5, 7, hex_to_rgba("1F2A4D")); put(img, 4, 8, c)
    elif forme == "perle":
        for y in range(16):
            for x in range(16):
                if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 20:
                    put(img, x, y, b)
        put(img, 6, 6, c); put(img, 5, 7, c); put(img, 9, 9, s)
    elif forme == "fruit":
        for y in range(16):
            for x in range(16):
                if (x - 7.5) ** 2 / 30 + (y - 8.5) ** 2 / 36 <= 1:
                    put(img, x, y, b if (x + y) % 5 else s)
        put(img, 7, 3, hex_to_rgba("4E7F3E")); put(img, 7, 2, hex_to_rgba("4E7F3E")); put(img, 8, 2, hex_to_rgba("7FBF5E")); put(img, 5, 6, c)
    elif forme == "fiole":
        rect(img, (6, 2, 10, 4), s); rect(img, (4, 4, 12, 14), c); rect(img, (5, 7, 11, 13), b)
        outline(img, (4, 4, 12, 14), s)
    elif forme == "sablier":
        for y in range(2, 14):
            w = abs(y - 8) // 2 + 1
            for x in range(8 - w, 8 + w):
                put(img, x, y, c if y > 8 else b)
        rect(img, (4, 1, 12, 2), s); rect(img, (4, 14, 12, 15), s)
    elif forme == "eclat":
        for d in range(12):
            put(img, 2 + d, 13 - d, b); put(img, 3 + d, 13 - d, c); put(img, 2 + d, 14 - d, s)
    elif forme == "pain":
        for x in range(2, 14):
            h = 3 + int(2 * math.sin((x - 2) / 11 * math.pi))
            for y in range(10 - h, 11):
                put(img, x, y, b if y > 10 - h + 1 else c)
        for x in (5, 8, 11):
            put(img, x, 7, s)
    elif forme == "dattes":
        for cx, cy in [(5, 8), (9, 6), (10, 10)]:
            for y in range(cy - 3, cy + 3):
                for x in range(cx - 1, cx + 2):
                    put(img, x, y, b if (x + y) % 3 else s)
            put(img, cx, cy - 2, c)
    elif forme == "miel":
        rect(img, (5, 3, 11, 5), s); rect(img, (4, 5, 12, 13), b); rect(img, (5, 6, 11, 12), c); rect(img, (6, 8, 10, 11), b)
    item_outline(img, hex_to_rgba("1A1822"))
    return img


def gen_items() -> None:
    save(tex_item_simple("poisson", "8FD6E8", "4A92B8", "FFF8E2", 301), out_path("item", "poisson_lanterne"))
    save(tex_item_simple("perle", "F4EFE6", "C9B7A2", "FFFFFF", 302), out_path("item", "perle_de_nacre"))
    save(tex_item_simple("fruit", "F0A3C8", "C66F9A", "FFD6EC", 303), out_path("item", "fruit_de_geant"))
    save(tex_item_simple("miel", "F2C14E", "B4934A", "FFE9A8", 304), out_path("item", "miel_de_rosee"))
    save(tex_item_simple("dattes", "8A5A3C", "5A3A26", "C98A5E", 305), out_path("item", "dattes_des_heures"))
    save(tex_item_simple("sablier", "E3C97E", "B4934A", "FFF3C8", 306), out_path("item", "sablier_d_opale"))
    save(tex_item_simple("fiole", "7FD6D6", "3C9AA8", "DDF3FF", 307), out_path("item", "the_des_cimes"))
    save(tex_item_simple("eclat", "7FD6D6", "3C9AA8", "FFFFFF", 308), out_path("item", "eclat_resonnant"))
    save(tex_item_simple("pain", "E0B088", "9A6240", "F4E6C2", 309), out_path("item", "pain_de_la_cour"))
    save(tex_item_sifflet_de_raie(), out_path("item", "sifflet_de_raie"))
    save(tex_item_journal_de_bord(), out_path("item", "journal_de_bord"))
    save(tex_item_ragout_du_domaine(), out_path("item", "ragout_du_domaine"))
    save(tex_item_farine(), out_path("item", "farine"))
    save(tex_item_pain_d_emile(), out_path("item", "pain_d_emile"))
    save(tex_item_oeuf(hex_to_rgba("8FB5DE"), P["lueur"]), out_path("item", "oeuf_raie_celeste"))
    save(tex_item_porte_d_aube(), out_path("item", "porte_d_aube"))
    save(tex_item_fragment_de_nacre(), out_path("item", "fragment_de_nacre"))
    save(tex_item_boussole_nacree(), out_path("item", "boussole_nacree"))
    save(tex_item_fibres_nacrees(), out_path("item", "fibres_nacrees"))
    save(tex_item_herbe_de_laube(), out_path("item", "herbe_de_l_aube"))
    save(tex_item_bouillon_de_laube(), out_path("item", "bouillon_de_l_aube"))
    save(tex_item_fragment_de_couronne(), out_path("item", "fragment_de_couronne"))
    save(tex_item_epee_du_souverain(), out_path("item", "epee_du_souverain"))
    save(tex_item_cle_du_manoir(), out_path("item", "cle_du_manoir"))
    save(tex_item_oeuf(P["lueur"], P["bleu_clair"]), out_path("item", "oeuf_poisson_lune"))
    save(tex_item_oeuf(P["ecorce"], P["or"]), out_path("item", "oeuf_cerf_lanterne"))
    save(tex_item_oeuf(P["metal"], P["verre_sombre"]), out_path("item", "oeuf_veilleur_egare"))
    save(tex_item_oeuf(P["bois"], P["bleu"]), out_path("item", "oeuf_habitant"))
    # Œufs des espèces régionales (étape 5) : couleurs identiques à celles déclarées dans LimbesEspeces.
    for nom_oeuf, (fond, points) in {
        "crabe_de_nacre": ("E8D6E4", "B98FB4"),
        "meduse_celeste": ("9FD3F0", "FFF1B8"),
        "poisson_lanterne": ("4F7FB8", "FFF3B0"),
        "gardien_des_epaves": ("5B4A3A", "7FE6FF"),
        "tortue_des_lagons": ("3F6F7F", "7FB58A"),
        "geant_des_jardins": ("6B5540", "5F9A4E"),
        "libellule_de_rosee": ("4FB8A8", "DFFFB0"),
        "escargot_des_racines": ("7A5A3C", "A48B6E"),
        "rodeur_des_racines": ("4A3B2C", "FF8A3C"),
        "scarabee_de_seve": ("4E8A3A", "DFF7A0"),
        "fennec_des_dunes": ("E9CFA3", "2B1E12"),
        "scorpion_des_heures": ("C98B3C", "3A2A1A"),
        "lezard_cadran": ("C9B07E", "E07A3C"),
        "sentinelle_de_sable": ("D9C08C", "1F2A4D"),
        "gazelle_d_opale": ("E3CDA6", "DDE8F0"),
        "bouquetin_de_verre": ("C9D3DE", "BFE6F2"),
        "papillon_de_cristal": ("C8F0FF", "6F7A86"),
        "colosse_de_cristal": ("6B7A8A", "AEE6F8"),
        "echo_resonnant": ("B8E8F6", "DFF6FF"),
        "griffon_de_verre": ("D9C7A8", "E6F2F8"),
        "colombe_astrale": ("F2F4FA", "FFF3C8"),
        "chat_de_palais": ("8E8FA6", "C9A227"),
        "sentinelle_de_la_couronne": ("D8DDE6", "C9A227"),
        "spectre_de_la_cour": ("C9D0E8", "7FE6FF"),
        "cerf_royal": ("F2EBDD", "E6C76A"),
        "chevalier_de_bois_blanc": ("EDE6D6", "5F9A4E"), "reflet_de_verre": ("C8F0FF", "FFFFFF"), "double_des_heures": ("D9C08C", "3A2A1A"),
        "legionnaire_astral": ("2E3A6E", "C9A227"), "champion_des_heures": ("C98B3C", "FFE08A"), "fleur_de_vie": ("F2A5C8", "DFFFB0"),
        "point_de_rupture": ("AEE6F8", "6B7A8A"), "banniere_astrale": ("2E3A6E", "FFF3C8"),
    }.items():
        save(tex_item_oeuf(hex_to_rgba(fond), hex_to_rgba(points)), out_path("item", "oeuf_" + nom_oeuf))


# ----------------------------------------------------------------------------- entités : skins humains (64x64)
SKIN_HEAD = Box(0, 0, 8, 8, 8)
SKIN_HAT = Box(32, 0, 8, 8, 8)
SKIN_BODY = Box(16, 16, 8, 12, 4)
SKIN_RARM = Box(40, 16, 4, 12, 4)
SKIN_LARM = Box(32, 48, 4, 12, 4)
SKIN_RLEG = Box(0, 16, 4, 12, 4)
SKIN_LLEG = Box(16, 48, 4, 12, 4)
SKIN_JACKET = Box(16, 32, 8, 12, 4)
SKIN_RSLEEVE = Box(40, 32, 4, 12, 4)
SKIN_LSLEEVE = Box(48, 48, 4, 12, 4)
SKIN_RPANTS = Box(0, 32, 4, 12, 4)
SKIN_LPANTS = Box(0, 48, 4, 12, 4)


def make_skin(spec: dict) -> Image.Image:
    """Construit un skin 64x64 à partir d'une description (teint, cheveux, vêtements)."""
    img = new(64, 64)
    seed = spec.get("seed", 1)
    skin = hex_to_rgba(spec["peau"])
    hair = hex_to_rgba(spec["cheveux"])
    shirt = hex_to_rgba(spec["haut"])
    pants = hex_to_rgba(spec["bas"])
    shoes = hex_to_rgba(spec.get("chaussures", "3A2A22"))
    accent = hex_to_rgba(spec.get("accent", "E3C97E"))
    eyes = hex_to_rgba(spec.get("yeux", "3C57A0"))
    # tête : peau partout, cheveux sur le dessus et l'arrière
    paint_box(img, SKIN_HEAD, skin, seed=seed, variation=0.03)
    hair_len = spec.get("longueur_cheveux", 3)  # rangées de cheveux sur les côtés/arrière
    for face in ("top",):
        rect(img, face_rectangle(SKIN_HEAD, face), hair)
    for face in ("right", "left", "back", "front"):
        x0, y0, x1, y1 = face_rectangle(SKIN_HEAD, face)
        rows = hair_len if face != "front" else spec.get("frange", 1)
        if face == "back":
            rows = max(rows, spec.get("cheveux_arriere", hair_len))
        rect(img, (x0, y0, x1, y0 + rows), hair)
        if face == "back" and spec.get("cheveux_longs"):
            rect(img, (x0, y0, x1, y1), hair)
    # visage
    def face_draw(p):
        p(2, 4, P["blanc"]); p(3, 4, eyes); p(4, 4, eyes); p(5, 4, P["blanc"])
        # sourcils fins
        p(2, 3, shade(hair, 0.9)); p(3, 3, shade(hair, 0.9)); p(4, 3, shade(hair, 0.9)); p(5, 3, shade(hair, 0.9))
        p(3, 6, shade(skin, 0.8)); p(4, 6, shade(skin, 0.8))  # bouche
        if spec.get("barbe"):
            b = hex_to_rgba(spec["barbe"])
            for x in range(1, 7):
                p(x, 7, b)
            p(2, 6, b); p(5, 6, b); p(1, 6, b); p(6, 6, b)
    draw_on_face(img, SKIN_HEAD, "front", face_draw)
    # couche chapeau : volume de cheveux ou capuche / couvre-chef
    if spec.get("chapeau"):
        hat = hex_to_rgba(spec["chapeau"])
        rect(img, face_rectangle(SKIN_HAT, "top"), hat)
        for face in ("right", "left", "back", "front"):
            x0, y0, x1, y1 = face_rectangle(SKIN_HAT, face)
            rect(img, (x0, y0, x1, y0 + (2 if face == "front" else 3)), hat)
    elif spec.get("cheveux_longs"):
        for face in ("right", "left", "back"):
            x0, y0, x1, y1 = face_rectangle(SKIN_HAT, face)
            rect(img, (x0, y0 + 2, x1, y1 - (1 if face != "back" else 0)), hair)
    # corps : haut
    paint_box(img, SKIN_BODY, shirt, seed=seed + 1, variation=0.05)
    bx0, by0, bx1, by1 = face_rectangle(SKIN_BODY, "front")
    # ceinture
    belt = hex_to_rgba(spec.get("ceinture", "4A3A2E"))
    for face in ("front", "back", "right", "left"):
        x0, y0, x1, y1 = face_rectangle(SKIN_BODY, face)
        rect(img, (x0, y0 + 8, x1, y0 + 9), belt)
    put(img, bx0 + 3, by0 + 8, accent); put(img, bx0 + 4, by0 + 8, accent)
    if spec.get("tablier"):
        ap = hex_to_rgba(spec["tablier"])
        rect(img, (bx0 + 1, by0 + 2, bx1 - 1, by1), ap)
        rect(img, (bx0 + 2, by0 + 3, bx1 - 2, by0 + 4), shade(ap, 0.85))
    if spec.get("col"):
        col = hex_to_rgba(spec["col"])
        rect(img, (bx0, by0, bx1, by0 + 1), col)
        put(img, bx0 + 3, by0 + 1, col); put(img, bx0 + 4, by0 + 1, col)
    if spec.get("broderie"):
        for y in range(by0 + 1, by1 - 1, 2):
            put(img, bx0, y, accent); put(img, bx1 - 1, y, accent)
    # bras
    sleeve = hex_to_rgba(spec.get("manches", spec["haut"]))
    for arm in (SKIN_RARM, SKIN_LARM):
        paint_box(img, arm, sleeve, seed=seed + 2, variation=0.05)
        # mains couleur peau (3 derniers px)
        for face in ("front", "back", "right", "left"):
            x0, y0, x1, y1 = face_rectangle(arm, face)
            rect(img, (x0, y1 - 3, x1, y1), skin)
        rect(img, face_rectangle(arm, "bottom"), skin)
    # jambes
    for leg in (SKIN_RLEG, SKIN_LLEG):
        paint_box(img, leg, pants, seed=seed + 3, variation=0.05)
        for face in ("front", "back", "right", "left"):
            x0, y0, x1, y1 = face_rectangle(leg, face)
            rect(img, (x0, y1 - 2, x1, y1), shoes)
        rect(img, face_rectangle(leg, "bottom"), shoes)
    # cape / manteau long sur la couche veste (seconde couche visible)
    if spec.get("manteau"):
        coat = hex_to_rgba(spec["manteau"])
        for face in ("back", "right", "left"):
            x0, y0, x1, y1 = face_rectangle(SKIN_JACKET, face)
            rect(img, (x0, y0, x1, y1), coat)
        x0, y0, x1, y1 = face_rectangle(SKIN_JACKET, "front")
        rect(img, (x0, y0, x0 + 2, y1), coat); rect(img, (x1 - 2, y0, x1, y1), coat)
        for y in range(y0, y1, 2):
            put(img, x0 + 1, y, accent); put(img, x1 - 2, y, accent)
        for pants_layer in (SKIN_RPANTS, SKIN_LPANTS):
            for face in ("back", "right", "left"):
                px0, py0, px1, py1 = face_rectangle(pants_layer, face)
                rect(img, (px0, py0, px1, py0 + 5), coat)
    if spec.get("epaulettes"):
        ep = hex_to_rgba(spec["epaulettes"])
        for sl in (SKIN_RSLEEVE, SKIN_LSLEEVE):
            for face in ("front", "back", "right", "left", "top"):
                x0, y0, x1, y1 = face_rectangle(sl, face)
                rect(img, (x0, y0, x1, min(y1, y0 + 2)), ep)
    return img


def face_rectangle(box: Box, face: str):
    from texlib import face_rect
    return face_rect(box, face)


SKINS = {
    "maelle": {"seed": 1, "peau": "E9C4A8", "cheveux": "C9B7A2", "haut": "5B6E9A", "bas": "3A4668", "chaussures": "4A3A2E",
               "accent": "E3C97E", "yeux": "6A8FC8", "cheveux_longs": True, "longueur_cheveux": 4, "manteau": "3F4F78", "col": "E9E3DA"},
    "thibault": {"seed": 2, "peau": "D9A77C", "cheveux": "5A3A26", "haut": "A33E3E", "bas": "4F3B2E", "chaussures": "2E2219",
                 "accent": "E3C97E", "barbe": "5A3A26", "tablier": "D9CBB4", "manches": "E9DCC8", "longueur_cheveux": 2},
    "orielle": {"seed": 3, "peau": "F0D3BF", "cheveux": "B8B4B0", "haut": "4E7A5A", "bas": "3B3A4A", "chaussures": "2E2219",
                "accent": "E3C97E", "yeux": "5A7A4E", "chapeau": "B8B4B0", "col": "E9E3DA", "broderie": True, "manches": "4E7A5A"},
    "brann": {"seed": 4, "peau": "B5805C", "cheveux": "26211F", "haut": "6B4B36", "bas": "3A3A3A", "chaussures": "1F1A17",
              "accent": "C2663A", "barbe": "26211F", "tablier": "7A5A42", "manches": "B5805C", "longueur_cheveux": 1},
    "vaeron": {"seed": 5, "peau": "D8B094", "cheveux": "2E2A33", "haut": "22305E", "bas": "1F2444", "chaussures": "2A1E18",
               "accent": "E3C97E", "yeux": "B4934A", "manteau": "151E3D", "col": "E3C97E", "broderie": True, "epaulettes": "E3C97E",
               "longueur_cheveux": 3, "ceinture": "B4934A"},
    "gaspard": {"seed": 6, "peau": "E2BBA0", "cheveux": "8A5A3C", "haut": "E9E3DA", "bas": "6A5A8A", "chaussures": "4A3A2E",
                "accent": "A33E3E", "tablier": "F4EFE6", "chapeau": "F4EFE6", "longueur_cheveux": 2},
    "habitant_1": {"seed": 11, "peau": "E0B898", "cheveux": "6B4A2E", "haut": "7A8AA6", "bas": "4F4A3E", "accent": "C9B7A2"},
    "habitant_2": {"seed": 12, "peau": "C58F6A", "cheveux": "1E1A18", "haut": "8A6A4E", "bas": "35455E", "accent": "E3C97E", "col": "E9E3DA"},
    "habitant_3": {"seed": 13, "peau": "F2D6C2", "cheveux": "D9A066", "haut": "5E8A6E", "bas": "6A5A4A", "accent": "F0CBD8",
                   "cheveux_longs": True, "longueur_cheveux": 3},
    "emile": {"seed": 21, "peau": "E8C4A8", "cheveux": "4A3326", "haut": "F4EFE6", "bas": "6B5A4A", "accent": "C9B7A2", "tablier": "E9E3DA"},
    "lisbeth": {"seed": 22, "peau": "D9B092", "cheveux": "2E2A3A", "haut": "3C57A0", "bas": "22305E", "accent": "E3C97E", "cheveux_longs": True, "longueur_cheveux": 3, "col": "F8EBB6"},
    "rosalie": {"seed": 23, "peau": "F0D2BE", "cheveux": "A8743E", "haut": "B4C6AC", "bas": "8E7260", "accent": "F0CBD8", "tablier": "FFFFFF", "chapeau": "E3C97E", "cheveux_longs": True, "longueur_cheveux": 2},
    "sabine": {"seed": 31, "peau": "C8926C", "cheveux": "2A1E1A", "haut": "6A6880", "bas": "3B3748", "accent": "E3C97E", "tablier": "4A4858", "cheveux_longs": True, "longueur_cheveux": 1},
    "loic": {"seed": 32, "peau": "E6C0A0", "cheveux": "8E5A2E", "haut": "8FA589", "bas": "63555C", "accent": "C2D8EF", "chapeau": "B99D8F"},
    "hanna": {"seed": 33, "peau": "A86C48", "cheveux": "1E1A18", "haut": "CDE9DF", "bas": "7F6F76", "accent": "F0CBD8", "tablier": "D3DFCB", "cheveux_longs": True, "longueur_cheveux": 3},
    "theo": {"seed": 34, "peau": "F2D6C2", "cheveux": "C9B7A2", "haut": "3C57A0", "bas": "22305E", "accent": "F8EBB6", "col": "E9E3DA"},
    "marguerite": {"seed": 35, "peau": "E0B898", "cheveux": "D9D4CC", "haut": "A33E3E", "bas": "4F4A3E", "accent": "F4EFE6", "tablier": "F4EFE6", "cheveux_longs": True, "longueur_cheveux": 2},
    "ysolde": {"seed": 36, "peau": "D4A07A", "cheveux": "6B3A1E", "haut": "22305E", "bas": "151E3D", "accent": "E3C97E", "col": "B4934A", "chapeau": "151E3D"},
    "marchand_1": {"seed": 37, "peau": "E8C4A8", "cheveux": "4A3326", "haut": "D9B99A", "bas": "6B5A4A", "accent": "A33E3E"},
    "marchand_2": {"seed": 38, "peau": "C58F6A", "cheveux": "1E1A18", "haut": "CDE9DF", "bas": "35455E", "accent": "E3C97E", "cheveux_longs": True, "longueur_cheveux": 2},
    # --- Personnages des régions (étape 5) ---
    # --- Capitale et épreuves (étape 6) ---
    "chambellan": {'seed': 101, 'peau': 'E6C7A8', 'cheveux': 'C9C9C9', 'haut': '2E3A6E', 'bas': '1C2447', 'accent': 'C9A227', 'longueur_cheveux': 2, 'manteau': '3A4A8A', 'col': 'FFE08A', 'broderie': True, 'epaulettes': 'C9A227'},
    "heraut": {'seed': 102, 'peau': 'D9A77C', 'cheveux': '5A3A26', 'haut': 'B23A48', 'bas': '2E3A6E', 'accent': 'FFE08A', 'longueur_cheveux': 1, 'col': 'FFE08A', 'ceinture': 'C9A227'},
    "archiviste_royale": {'seed': 103, 'peau': 'E9C4A8', 'cheveux': '8A5A3C', 'haut': '4A4668', 'bas': '2E2A40', 'accent': 'DDE0EE', 'longueur_cheveux': 4, 'cheveux_longs': True, 'manteau': '5D5E73', 'col': 'DDE0EE'},
    "intendante_du_palais": {'seed': 104, 'peau': 'C98B5A', 'cheveux': '2B1E12', 'haut': '6E2E3A', 'bas': '3A2416', 'accent': 'E3C97E', 'longueur_cheveux': 3, 'tablier': 'E9E3DA', 'manches': 'E9E3DA'},
    "conseiller_royal": {'seed': 105, 'peau': 'E6C7A8', 'cheveux': 'A9A9A9', 'haut': '3F4F78', 'bas': '2E3A6E', 'accent': 'C9A227', 'longueur_cheveux': 2, 'barbe': 'A9A9A9', 'manteau': '2E3A6E', 'col': 'DDE0EE'},
    "ambassadeur_mer": {'seed': 106, 'peau': 'B5805C', 'cheveux': '2B1E12', 'haut': '2E5F8A', 'bas': '1E3660', 'accent': 'FFF8E2', 'longueur_cheveux': 1, 'manteau': '3C7FA8', 'col': 'FFF8E2', 'epaulettes': 'E3C97E'},
    "ambassadrice_jardins": {'seed': 107, 'peau': 'E9C4A8', 'cheveux': '7A4A2A', 'haut': '5F9A4E', 'bas': '3E6B33', 'accent': 'F2A5C8', 'longueur_cheveux': 4, 'cheveux_longs': True, 'manteau': '8FC779', 'col': 'FFFFFF'},
    "ambassadeur_verre": {'seed': 108, 'peau': 'F2E3D5', 'cheveux': 'F2F4FA', 'haut': '9BA4B3', 'bas': '6F7A86', 'accent': 'BFE6F2', 'longueur_cheveux': 2, 'manteau': 'D8DDE6', 'col': 'BFE6F2'},
    "marchand_royal_1": {'seed': 109, 'peau': 'D9A77C', 'cheveux': '5A3A26', 'haut': 'A33E3E', 'bas': '4F3B2E', 'accent': 'E3C97E', 'longueur_cheveux': 2, 'tablier': 'D9CBB4', 'ceinture': 'B4934A'},
    "marchand_royal_2": {'seed': 110, 'peau': 'C98B5A', 'cheveux': 'D9A066', 'haut': '2E5F8A', 'bas': '2E2A40', 'accent': 'FFE08A', 'longueur_cheveux': 3, 'cheveux_longs': True, 'tablier': 'E9E3DA'},
    "artisan_royal_1": {'seed': 111, 'peau': 'B5805C', 'cheveux': '2B1E12', 'haut': '4A4668', 'bas': '2E2219', 'accent': 'C9A227', 'longueur_cheveux': 1, 'barbe': '2B1E12', 'tablier': '5A5A5A', 'manches': 'A33E3E'},
    "artisan_royal_2": {'seed': 112, 'peau': 'E9C4A8', 'cheveux': '8A5A3C', 'haut': '7FB7CC', 'bas': '3A4668', 'accent': 'BFE6F2', 'longueur_cheveux': 3, 'tablier': 'D8DDE6'},
    "maitre_des_jeux": {'seed': 113, 'peau': 'A86E42', 'cheveux': '2B1E12', 'haut': 'C98B3C', 'bas': '8E5C22', 'accent': 'FFE08A', 'longueur_cheveux': 1, 'manteau': 'E3C97E', 'col': 'FFF2B0', 'ceinture': '3A2A1A'},
    "capitaine_lio": {'seed': 114, 'peau': 'D9A77C', 'cheveux': '8A5A3C', 'haut': '2E5F8A', 'bas': '3A4668', 'accent': 'FFF8E2', 'longueur_cheveux': 2, 'barbe': '8A5A3C', 'manteau': '3C7FA8', 'col': 'FFF8E2', 'ceinture': 'B4934A'},
    "exploratrice_mina": {'seed': 115, 'peau': 'E9C4A8', 'cheveux': 'B23A48', 'haut': '4A92B8', 'bas': '4F3B2E', 'accent': 'FFF8E2', 'longueur_cheveux': 3, 'cheveux_longs': True, 'ceinture': 'B4934A'},
    "kael": {'seed': 41, 'peau': 'D9A77C', 'cheveux': '5A3A26', 'haut': '3C7FA8', 'bas': '2A3A4E', 'accent': 'FFF8E2', 'longueur_cheveux': 1, 'manteau': '2E5F8A', 'col': 'FFF8E2', 'epaulettes': 'E3C97E', 'ceinture': 'B4934A', 'broderie': True},
    "second_de_kael": {'seed': 42, 'peau': 'E9C4A8', 'cheveux': '8A5A3C', 'haut': '4A92B8', 'bas': '3A4668', 'accent': 'FFF8E2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "poissonniere": {'seed': 43, 'peau': 'B5805C', 'cheveux': 'D9A066', 'haut': '2E5F8A', 'bas': '5A5A5A', 'accent': 'FFF8E2', 'longueur_cheveux': 3},
    "fumeur": {'seed': 44, 'peau': 'F0D3BF', 'cheveux': '4A3326', 'haut': '6EA8C8', 'bas': '2A3A4E', 'accent': 'FFF8E2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "pecheur_1": {'seed': 45, 'peau': 'D9A77C', 'cheveux': '5A3A26', 'haut': 'DDF3FF', 'bas': '3A4668', 'accent': 'FFF8E2', 'longueur_cheveux': 2},
    "pecheur_2": {'seed': 46, 'peau': 'E9C4A8', 'cheveux': '8A5A3C', 'haut': '3C7FA8', 'bas': '5A5A5A', 'accent': 'FFF8E2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "plongeuse": {'seed': 47, 'peau': 'B5805C', 'cheveux': 'D9A066', 'haut': '4A92B8', 'bas': '2A3A4E', 'accent': 'FFF8E2', 'longueur_cheveux': 1},
    "aubergiste_port": {'seed': 48, 'peau': 'F0D3BF', 'cheveux': '4A3326', 'haut': '2E5F8A', 'bas': '3A4668', 'accent': 'FFF8E2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "nerine": {'seed': 49, 'peau': 'D9A77C', 'cheveux': '5A3A26', 'haut': '6EA8C8', 'bas': '5A5A5A', 'accent': 'FFF8E2', 'longueur_cheveux': 4, 'cheveux_longs': True, 'col': 'FFF8E2'},
    "marchand_flottant": {'seed': 50, 'peau': 'E9C4A8', 'cheveux': '8A5A3C', 'haut': 'DDF3FF', 'bas': '2A3A4E', 'accent': 'FFF8E2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "anselme": {'seed': 51, 'peau': 'B5805C', 'cheveux': 'D9A066', 'haut': '3C7FA8', 'bas': '3A4668', 'accent': 'FFF8E2', 'longueur_cheveux': 2, 'barbe': 'C9B7A2', 'chapeau': '2A3A4E', 'manteau': '3C7FA8'},
    "bastien": {'seed': 52, 'peau': 'F0D3BF', 'cheveux': '4A3326', 'haut': '4A92B8', 'bas': '5A5A5A', 'accent': 'FFF8E2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "aelis": {'seed': 53, 'peau': 'E0B898', 'cheveux': 'D9A066', 'haut': '4E7F3E', 'bas': '4A3222', 'accent': 'FFD6EC', 'longueur_cheveux': 4, 'cheveux_longs': True, 'manteau': '4E7F3E', 'col': 'FFD6EC', 'broderie': True, 'chapeau': 'F0A3C8'},
    "intendante_sylve": {'seed': 54, 'peau': 'C58F6A', 'cheveux': '4A3326', 'haut': '6FA85A', 'bas': '6B4A32', 'accent': 'FFD6EC', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "boulangere_sylve": {'seed': 55, 'peau': 'F2D6C2', 'cheveux': '5A3A26', 'haut': '8FBF6E', 'bas': '3F4F2E', 'accent': 'FFD6EC', 'longueur_cheveux': 3},
    "tisserande": {'seed': 56, 'peau': 'D8B094', 'cheveux': '8A5A3C', 'haut': 'A87C56', 'bas': '4A3222', 'accent': 'FFD6EC', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "herboriste": {'seed': 57, 'peau': 'E0B898', 'cheveux': 'D9A066', 'haut': 'F0A3C8', 'bas': '6B4A32', 'accent': 'FFD6EC', 'longueur_cheveux': 2},
    "guetteur_sylve": {'seed': 58, 'peau': 'C58F6A', 'cheveux': '4A3326', 'haut': '4E7F3E', 'bas': '3F4F2E', 'accent': 'FFD6EC', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "pepinieriste": {'seed': 59, 'peau': 'F2D6C2', 'cheveux': '5A3A26', 'haut': '6FA85A', 'bas': '4A3222', 'accent': 'FFD6EC', 'longueur_cheveux': 1},
    "oriane": {'seed': 60, 'peau': 'D8B094', 'cheveux': '8A5A3C', 'haut': '8FBF6E', 'bas': '6B4A32', 'accent': 'FFD6EC', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "lioran": {'seed': 61, 'peau': 'E0B898', 'cheveux': 'D9A066', 'haut': 'A87C56', 'bas': '3F4F2E', 'accent': 'FFD6EC', 'longueur_cheveux': 3},
    "baruch": {'seed': 62, 'peau': 'C58F6A', 'cheveux': '4A3326', 'haut': 'F0A3C8', 'bas': '4A3222', 'accent': 'FFD6EC', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "sahir": {'seed': 63, 'peau': 'B5805C', 'cheveux': '4A3326', 'haut': 'E6D2A6', 'bas': '8A5A3C', 'accent': '22305E', 'longueur_cheveux': 1, 'barbe': 'B8B4B0', 'manteau': 'E6D2A6', 'col': '22305E', 'broderie': True, 'chapeau': 'E6D2A6'},
    "horloger": {'seed': 64, 'peau': 'C58F6A', 'cheveux': '5A3A26', 'haut': 'C98A5E', 'bas': '5A3A26', 'accent': '22305E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "caravanier": {'seed': 65, 'peau': '9A6240', 'cheveux': '8A5A3C', 'haut': 'F4E6C2', 'bas': 'B4934A', 'accent': '22305E', 'longueur_cheveux': 3},
    "chamelier": {'seed': 66, 'peau': 'D9A77C', 'cheveux': 'D9A066', 'haut': 'A33E3E', 'bas': '8A5A3C', 'accent': '22305E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "marchande_d_ailleurs": {'seed': 67, 'peau': 'B5805C', 'cheveux': '4A3326', 'haut': 'E3C97E', 'bas': '5A3A26', 'accent': '22305E', 'longueur_cheveux': 2},
    "archiviste": {'seed': 68, 'peau': 'C58F6A', 'cheveux': '5A3A26', 'haut': 'E6D2A6', 'bas': 'B4934A', 'accent': '22305E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "fige_1": {'seed': 69, 'peau': '9A6240', 'cheveux': '8A5A3C', 'haut': 'C98A5E', 'bas': '8A5A3C', 'accent': '22305E', 'longueur_cheveux': 1},
    "fige_2": {'seed': 70, 'peau': 'D9A77C', 'cheveux': 'D9A066', 'haut': 'F4E6C2', 'bas': '5A3A26', 'accent': '22305E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "fige_3": {'seed': 71, 'peau': 'B5805C', 'cheveux': '4A3326', 'haut': 'A33E3E', 'bas': 'B4934A', 'accent': '22305E', 'longueur_cheveux': 3},
    "fige_4": {'seed': 72, 'peau': 'C58F6A', 'cheveux': '5A3A26', 'haut': 'E3C97E', 'bas': '8A5A3C', 'accent': '22305E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "fige_5": {'seed': 73, 'peau': '9A6240', 'cheveux': '8A5A3C', 'haut': 'E6D2A6', 'bas': '5A3A26', 'accent': '22305E', 'longueur_cheveux': 2},
    "potiere": {'seed': 74, 'peau': 'D9A77C', 'cheveux': 'D9A066', 'haut': 'C98A5E', 'bas': 'B4934A', 'accent': '22305E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "guide_des_dunes": {'seed': 75, 'peau': 'B5805C', 'cheveux': '4A3326', 'haut': 'F4E6C2', 'bas': '8A5A3C', 'accent': '22305E', 'longueur_cheveux': 1},
    "aubergiste_meridiane": {'seed': 76, 'peau': 'C58F6A', 'cheveux': '5A3A26', 'haut': 'A33E3E', 'bas': '5A3A26', 'accent': '22305E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "habitant_dunes": {'seed': 77, 'peau': '9A6240', 'cheveux': '8A5A3C', 'haut': 'E3C97E', 'bas': 'B4934A', 'accent': '22305E', 'longueur_cheveux': 3},
    "yelena": {'seed': 78, 'peau': 'D9A77C', 'cheveux': 'D9A066', 'haut': 'E6D2A6', 'bas': '8A5A3C', 'accent': '22305E', 'longueur_cheveux': 3, 'cheveux_longs': True, 'manteau': '22305E', 'col': 'E3C97E'},
    "ismael": {'seed': 79, 'peau': 'B5805C', 'cheveux': '4A3326', 'haut': 'C98A5E', 'bas': '5A3A26', 'accent': '22305E', 'longueur_cheveux': 2},
    "orsane": {'seed': 80, 'peau': 'F0D3BF', 'cheveux': '2E2A33', 'haut': 'B9C8D4', 'bas': '4A4858', 'accent': 'BDF2F2', 'longueur_cheveux': 3, 'cheveux_longs': True, 'manteau': 'DCE6EE', 'col': '7FD6D6', 'epaulettes': 'BDF2F2', 'broderie': True},
    "maitre_forgeron": {'seed': 81, 'peau': 'E9C4A8', 'cheveux': 'C9B7A2', 'haut': '7FD6D6', 'bas': '35455E', 'accent': 'BDF2F2', 'longueur_cheveux': 2, 'barbe': '4A3326', 'tablier': '4A4858', 'manches': 'B9C8D4'},
    "apprentie_verriere": {'seed': 82, 'peau': 'D8B094', 'cheveux': '1E1A18', 'haut': 'DCE6EE', 'bas': '6A6880', 'accent': 'BDF2F2', 'longueur_cheveux': 3},
    "garde_de_verre": {'seed': 83, 'peau': 'F2D6C2', 'cheveux': 'B8B4B0', 'haut': '4A4858', 'bas': '4A4858', 'accent': 'BDF2F2', 'longueur_cheveux': 1, 'epaulettes': 'BDF2F2', 'manteau': 'DCE6EE'},
    "garde_de_verre_2": {'seed': 84, 'peau': 'F0D3BF', 'cheveux': '2E2A33', 'haut': '3C9AA8', 'bas': '35455E', 'accent': 'BDF2F2', 'longueur_cheveux': 2, 'epaulettes': 'BDF2F2', 'manteau': 'DCE6EE'},
    "baigneur": {'seed': 85, 'peau': 'E9C4A8', 'cheveux': 'C9B7A2', 'haut': 'B9C8D4', 'bas': '6A6880', 'accent': 'BDF2F2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "luthiere": {'seed': 86, 'peau': 'D8B094', 'cheveux': '1E1A18', 'haut': '7FD6D6', 'bas': '4A4858', 'accent': 'BDF2F2', 'longueur_cheveux': 1},
    "verrier_1": {'seed': 87, 'peau': 'F2D6C2', 'cheveux': 'B8B4B0', 'haut': 'DCE6EE', 'bas': '35455E', 'accent': 'BDF2F2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "verrier_2": {'seed': 88, 'peau': 'F0D3BF', 'cheveux': '2E2A33', 'haut': '4A4858', 'bas': '6A6880', 'accent': 'BDF2F2', 'longueur_cheveux': 3},
    "aubergiste_verreole": {'seed': 89, 'peau': 'E9C4A8', 'cheveux': 'C9B7A2', 'haut': '3C9AA8', 'bas': '4A4858', 'accent': 'BDF2F2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "nell": {'seed': 90, 'peau': 'D8B094', 'cheveux': '1E1A18', 'haut': 'B9C8D4', 'bas': '35455E', 'accent': 'BDF2F2', 'longueur_cheveux': 2},
    "tobie": {'seed': 91, 'peau': 'F2D6C2', 'cheveux': 'B8B4B0', 'haut': '7FD6D6', 'bas': '6A6880', 'accent': 'BDF2F2', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "intendant_garde": {'seed': 92, 'peau': 'D8B094', 'cheveux': '1E1A18', 'haut': '22305E', 'bas': '151E3D', 'accent': 'E3C97E', 'longueur_cheveux': 1},
    "garde_1": {'seed': 93, 'peau': 'E0B898', 'cheveux': 'B8B4B0', 'haut': 'E9E3DA', 'bas': '1F2444', 'accent': 'E3C97E', 'longueur_cheveux': 2, 'epaulettes': 'B4934A', 'manteau': '22305E'},
    "garde_2": {'seed': 94, 'peau': 'C58F6A', 'cheveux': '2E2A33', 'haut': '3C57A0', 'bas': '3A3A3A', 'accent': 'E3C97E', 'longueur_cheveux': 3, 'epaulettes': 'B4934A', 'manteau': '22305E'},
    "sergent": {'seed': 95, 'peau': 'F0D3BF', 'cheveux': 'C9B7A2', 'haut': 'B4934A', 'bas': '151E3D', 'accent': 'E3C97E', 'longueur_cheveux': 1, 'epaulettes': 'B4934A', 'ceinture': 'B4934A', 'manteau': '22305E'},
    "maitresse_d_ecole": {'seed': 96, 'peau': 'D8B094', 'cheveux': '1E1A18', 'haut': 'F4EFE6', 'bas': '1F2444', 'accent': 'E3C97E', 'longueur_cheveux': 2},
    "hersent": {'seed': 97, 'peau': 'E0B898', 'cheveux': 'B8B4B0', 'haut': '22305E', 'bas': '3A3A3A', 'accent': 'E3C97E', 'longueur_cheveux': 3, 'cheveux_longs': True, 'tablier': 'F4EFE6', 'col': 'E9E3DA'},
    "famille_1": {'seed': 98, 'peau': 'C58F6A', 'cheveux': '2E2A33', 'haut': 'E9E3DA', 'bas': '151E3D', 'accent': 'E3C97E', 'longueur_cheveux': 1},
    "famille_2": {'seed': 99, 'peau': 'F0D3BF', 'cheveux': 'C9B7A2', 'haut': '3C57A0', 'bas': '1F2444', 'accent': 'E3C97E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "famille_3": {'seed': 100, 'peau': 'D8B094', 'cheveux': '1E1A18', 'haut': 'B4934A', 'bas': '3A3A3A', 'accent': 'E3C97E', 'longueur_cheveux': 3},
    "boulanger_garde": {'seed': 101, 'peau': 'E0B898', 'cheveux': 'B8B4B0', 'haut': 'F4EFE6', 'bas': '151E3D', 'accent': 'E3C97E', 'longueur_cheveux': 3, 'cheveux_longs': True},
    "marchand_garde": {'seed': 102, 'peau': 'C58F6A', 'cheveux': '2E2A33', 'haut': '22305E', 'bas': '1F2444', 'accent': 'E3C97E', 'longueur_cheveux': 2},
    "elouan": {'seed': 103, 'peau': 'F0D3BF', 'cheveux': 'C9B7A2', 'haut': 'E9E3DA', 'bas': '3A3A3A', 'accent': 'E3C97E', 'longueur_cheveux': 3, 'barbe': 'B8B4B0', 'manteau': '22305E', 'col': 'E3C97E'},
}


# ----------------------------------------------------------------------------- entités : créatures
def tex_poisson_lune() -> Image.Image:
    """Poisson-lune (32x32) : corps 6x4x10 (u0,v0), nageoire dorsale 1x3x6 (u0,v14), queue 1x4x4 (u16,v14)."""
    img = new(32, 32)
    body = Box(0, 0, 6, 4, 10)
    glow = mix(P["lueur"], P["or_clair"], 0.3)

    def body_tex(face, w, h, seed):
        f = new(w, h)
        base = glow if face in ("top", "back") else mix(glow, P["blanc"], 0.3)
        fill_noise(f, base, variation=0.05, scale=3, seed=seed)
        rng = Rng(seed)
        for _ in range(max(1, (w * h) // 6)):
            put(f, rng.i(0, w - 1), rng.i(0, h - 1), P["bleu_clair"])
        if face == "front":  # avant : œil et bouche
            put(f, 1, 1, P["bleu_nuit"]); put(f, w - 2, 1, P["bleu_nuit"])
            put(f, 1, 1, P["bleu_nuit"]); put(f, 2, 3, P["bleu"]); put(f, 3, 3, P["bleu"])
        return f

    paint_box(img, body, glow, seed=7, texture=body_tex)
    fin = Box(0, 14, 1, 3, 6)
    paint_box(img, fin, (P["verre"][0], P["verre"][1], P["verre"][2], 200), seed=8, variation=0.05)
    tail = Box(16, 14, 1, 4, 4)
    paint_box(img, tail, (P["verre"][0], P["verre"][1], P["verre"][2], 200), seed=9, variation=0.05)
    side_fin = Box(16, 24, 3, 1, 3)
    paint_box(img, side_fin, (P["verre_sombre"][0], P["verre_sombre"][1], P["verre_sombre"][2], 200), seed=10)
    return img


def tex_cerf_lanterne() -> Image.Image:
    """Cerf-lanterne (64x64). Boîtes : corps 8x9x14 (u0,v0) ; cou 5x7x5 (u0,v23) ; tête 5x6x7 (u20,v23) ;
    pattes 3x11x3 (u44,v0 / u44,v14 / u44,v28 / u44,v42) ; bois 1x6x4 (u0,v36 et u10,v36) ;
    lanternes 2x2x2 (u40,v36 et u48,v36) ; queue 2x3x1 (u56,v36)."""
    img = new(64, 64)
    fur = hex_to_rgba("B99C86")
    fur_dark = hex_to_rgba("8E7260")
    belly = hex_to_rgba("E6D7C8")

    def fur_tex(face, w, h, seed):
        f = new(w, h)
        base = belly if face == "bottom" else (fur_dark if face == "top" else fur)
        fill_noise(f, base, variation=0.07, scale=3, seed=seed)
        rng = Rng(seed)
        if face not in ("bottom",):
            for _ in range(max(1, (w * h) // 10)):
                put(f, rng.i(0, w - 1), rng.i(0, h - 1), P["nacre_clair"])  # taches claires
        return f

    paint_box(img, Box(0, 0, 8, 9, 14), fur, seed=20, texture=fur_tex)
    paint_box(img, Box(0, 23, 5, 7, 5), fur, seed=21, texture=fur_tex)
    head = Box(20, 23, 5, 6, 7)
    paint_box(img, head, fur, seed=22, texture=fur_tex)

    def head_front(p):  # museau et yeux sur la face avant (5x6)
        p(1, 1, P["noir"]); p(3, 1, P["noir"])
        p(1, 1, P["bleu_nuit"]); p(3, 1, P["bleu_nuit"])
        p(2, 4, P["noir"]); p(1, 5, fur_dark); p(2, 5, fur_dark); p(3, 5, fur_dark)
    draw_on_face(img, head, "front", head_front)
    for v in (0, 14, 28, 42):
        leg = Box(44, v, 3, 11, 3)
        paint_box(img, leg, fur, seed=23 + v, texture=fur_tex)
        for face in ("front", "back", "right", "left"):
            x0, y0, x1, y1 = face_rectangle(leg, face)
            rect(img, (x0, y1 - 2, x1, y1), fur_dark)  # sabots
    antler = hex_to_rgba("E9E3DA")
    for u in (0, 10):
        paint_box(img, Box(u, 36, 1, 6, 4), antler, seed=30 + u, variation=0.08)
    for u in (40, 48):
        paint_box(img, Box(u, 36, 2, 2, 2), P["lueur"], seed=31 + u, variation=0.02)
    paint_box(img, Box(56, 36, 2, 3, 1), belly, seed=33)
    return img


def tex_veilleur_egare() -> Image.Image:
    """Veilleur égaré (64x64) : torse 10x10x6 (u0,v0) ; tête 6x6x6 (u32,v0) ; bras 4x12x4 (u0,v16 / u16,v16) ;
    jambes 4x8x4 (u32,v12 / u48,v12) ; épaulières 5x3x7 (u0,v32 / u24,v32)."""
    img = new(64, 64)
    stone = hex_to_rgba("8E8A97")
    stone_dark = hex_to_rgba("5E5A6A")
    crack = P["verre_sombre"]

    def stone_tex(face, w, h, seed):
        f = new(w, h)
        fill_noise(f, stone, palette=[stone_dark, stone, stone, shade(stone, 1.1)], scale=3, seed=seed)
        rng = Rng(seed)
        # fissures lumineuses bleues
        for _ in range(max(1, (w * h) // 14)):
            x, y = rng.i(0, w - 1), rng.i(0, h - 1)
            put(f, x, y, crack)
            if rng.f() > 0.5 and y + 1 < h:
                put(f, x, y + 1, mix(crack, stone, 0.5))
        return f

    torso = Box(0, 0, 10, 10, 6)
    paint_box(img, torso, stone, seed=40, texture=stone_tex)

    def torso_front(p):  # cœur de nacre lumineux au centre
        for x in range(4, 6):
            for y in range(3, 6):
                p(x, y, P["lueur"])
        p(4, 2, P["or"]); p(5, 6, P["or"])
    draw_on_face(img, torso, "front", torso_front)
    head = Box(32, 0, 6, 6, 6)
    paint_box(img, head, stone, seed=41, texture=stone_tex)

    def head_front(p):  # fente oculaire lumineuse
        for x in range(1, 5):
            p(x, 2, P["verre"])
        p(2, 2, P["blanc"]); p(3, 2, P["blanc"])
    draw_on_face(img, head, "front", head_front)
    for u in (0, 16):
        arm = Box(u, 16, 4, 12, 4)
        paint_box(img, arm, stone, seed=42 + u, texture=stone_tex)
        for face in ("front", "back", "right", "left"):
            x0, y0, x1, y1 = face_rectangle(arm, face)
            rect(img, (x0, y1 - 3, x1, y1), stone_dark)  # poings massifs
    for u in (32, 48):
        paint_box(img, Box(u, 12, 4, 8, 4), stone, seed=44 + u, texture=stone_tex)
    for u in (0, 24):
        paint_box(img, Box(u, 32, 5, 3, 7), stone_dark, seed=46 + u, variation=0.08)
    return img


def gen_entities() -> None:
    for name, spec in SKINS.items():
        save(make_skin(spec), out_path("entity/habitant", name))
    save(tex_poisson_lune(), out_path("entity", "poisson_lune"))
    save(tex_cerf_lanterne(), out_path("entity", "cerf_lanterne"))
    save(tex_veilleur_egare(), out_path("entity", "veilleur_egare"))
    # Épée invoquée : rendue comme objet ; fragment de couronne flottant : texture 16x16 utilisée par le renderer
    crown = new(16, 16)
    g, gc = P["or"], P["or_clair"]
    for x in range(2, 14):
        put(crown, x, 11, g); put(crown, x, 12, shade(g, 0.8))
    for x, h in [(2, 3), (5, 5), (8, 6), (11, 5), (13, 3)]:
        for y in range(11 - h, 11):
            put(crown, x, y, g)
        put(crown, x, 11 - h, gc)
    put(crown, 8, 11, P["bleu_clair"]); put(crown, 5, 11, P["verre_sombre"]); put(crown, 11, 11, P["verre_sombre"])
    save(crown, out_path("entity", "fragment_de_couronne"))


# ----------------------------------------------------------------------------- particules
def tex_particle_etoile() -> Image.Image:
    img = new(8, 8)
    c, cc = P["or_clair"], P["blanc"]
    for i in range(8):
        put(img, 3, i, c); put(img, 4, i, c); put(img, i, 3, c); put(img, i, 4, c)
    for x, y in [(3, 3), (4, 3), (3, 4), (4, 4)]:
        put(img, x, y, cc)
    for x, y in [(2, 2), (5, 2), (2, 5), (5, 5)]:
        put(img, x, y, (c[0], c[1], c[2], 140))
    return img


def tex_particle_eclat() -> Image.Image:
    img = new(8, 8)
    for x, y in [(3, 1), (4, 1), (2, 2), (5, 2), (2, 3), (5, 3), (3, 4), (4, 4), (3, 5), (4, 6)]:
        put(img, x, y, mix(P["nacre_rose"], P["nacre_bleu"], (x + y) / 12))
    put(img, 3, 2, P["blanc"]); put(img, 3, 3, P["nacre_clair"])
    return img


def tex_particle_lueur() -> Image.Image:
    img = new(8, 8)
    for y in range(8):
        for x in range(8):
            d = math.hypot(x - 3.5, y - 3.5)
            if d <= 3.6:
                a = int(255 * max(0.0, 1 - d / 3.8) ** 1.3)
                c = mix(P["verre"], P["blanc"], max(0.0, 1 - d / 2))
                put(img, x, y, (c[0], c[1], c[2], a))
    return img


def tex_particle_brume() -> Image.Image:
    img = new(16, 16)
    n = value_noise(16, 16, 4, 77, octaves=2, tile=False)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5) / 8
            a = max(0.0, 1 - d) * (0.6 + 0.4 * n[y, x])
            put(img, x, y, (236, 240, 248, int(170 * a)))
    return img


def gen_particles() -> None:
    save(tex_particle_etoile(), out_path("particle", "etoile_royale"))
    save(tex_particle_eclat(), out_path("particle", "eclat_de_nacre"))
    save(tex_particle_lueur(), out_path("particle", "lueur"))
    save(tex_particle_brume(), out_path("particle", "brume"))


# ----------------------------------------------------------------------------- interface
def gen_gui() -> None:
    # Barre de majesté : 0..61 x 0..7 fond, 0..61 x 8..15 remplissage, 0..15 x 16..31 icône couronne
    img = new(64, 32)
    rect(img, (0, 0, 62, 8), (20, 24, 44, 200))
    outline(img, (0, 0, 62, 8), P["or_sombre"])
    gradient_v(img, (1, 9, 61, 15), P["verre"], P["bleu_clair"])
    outline(img, (0, 8, 62, 16), P["or"])
    for x in range(2, 60, 6):
        put(img, x, 10, P["blanc"])
    crown = new(16, 16)
    for x in range(3, 13):
        put(crown, x, 11, P["or"]); put(crown, x, 12, P["or_sombre"])
    for x, h in [(3, 3), (6, 5), (9, 5), (12, 3)]:
        for y in range(11 - h, 11):
            put(crown, x, y, P["or"])
        put(crown, x, 11 - h, P["or_clair"])
    put(crown, 7, 11, P["bleu_clair"]); put(crown, 8, 11, P["bleu_clair"])
    img.alpha_composite(crown, (0, 16))
    save(img, out_path("gui", "majeste"))
    # Marqueur de quête : losange lumineux 16x16
    mk = new(16, 16)
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) + abs(y - 7.5)
            if d <= 7:
                c = P["or_clair"] if d <= 3 else P["or"]
                if 6 < d <= 7:
                    c = P["or_sombre"]
                put(mk, x, y, c)
    put(mk, 7, 7, P["blanc"]); put(mk, 8, 7, P["blanc"]); put(mk, 7, 8, P["blanc"]); put(mk, 8, 8, P["blanc"])
    save(mk, out_path("gui", "marqueur"))
    # Panneau de dialogue 9-slice 32x32 : nacre bordée d'or
    pn = new(32, 32)
    fill_noise(pn, P["nacre_clair"], variation=0.03, scale=6, seed=5)
    outline(pn, (0, 0, 32, 32), P["or_sombre"])
    outline(pn, (1, 1, 31, 31), P["or"])
    outline(pn, (2, 2, 30, 30), P["nacre_sombre"])
    save(pn, out_path("gui", "panneau"))
    # Fond sombre translucide pour le journal
    jn = new(32, 32)
    fill_noise(jn, (28, 32, 56, 235), variation=0.04, scale=6, seed=6)
    outline(jn, (0, 0, 32, 32), P["or_sombre"])
    outline(jn, (1, 1, 31, 31), P["bleu_clair"])
    save(jn, out_path("gui", "journal"))


def gen_icon() -> None:
    img = new(128, 128)
    for y in range(128):
        t = y / 127
        c = mix(P["bleu_nuit"], P["bleu"], t)
        for x in range(128):
            put(img, x, y, c)
    rng = Rng(99)
    for _ in range(90):
        x, y = rng.i(0, 127), rng.i(0, 127)
        c = P["blanc"] if rng.f() > 0.6 else P["or_clair"]
        put(img, x, y, c)
        if rng.f() > 0.7:
            put(img, x + 1, y, (c[0], c[1], c[2], 120)); put(img, x - 1, y, (c[0], c[1], c[2], 120))
            put(img, x, y + 1, (c[0], c[1], c[2], 120)); put(img, x, y - 1, (c[0], c[1], c[2], 120))
    crown = new(16, 16)
    for x in range(2, 14):
        put(crown, x, 11, P["or"]); put(crown, x, 12, P["or_sombre"])
    for x, h in [(2, 4), (5, 6), (8, 8), (11, 6), (13, 4)]:
        for y in range(11 - h, 11):
            put(crown, x, y, P["or"])
        put(crown, x, 11 - h, P["or_clair"])
    put(crown, 8, 11, P["bleu_clair"]); put(crown, 5, 11, P["verre"]); put(crown, 11, 11, P["verre"])
    big = crown.resize((96, 96), Image.NEAREST)
    img.alpha_composite(big, (16, 20))
    save(img, os.path.join(OUT, "..", "icon.png"))


def main() -> None:
    global OUT
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default=OUT)
    args = ap.parse_args()
    OUT = args.out
    gen_blocks()
    gen_items()
    gen_entities()
    gen_particles()
    gen_gui()
    gen_icon()
    total = sum(len(v) for v in MANIFEST.values())
    with open(os.path.join(OUT, "MANIFEST.json"), "w", encoding="utf-8") as fh:
        json.dump(MANIFEST, fh, indent=2, ensure_ascii=False)
    print(f"{total} textures générées dans {OUT}")
    for k, v in MANIFEST.items():
        print(f"  {k}: {len(v)}")


if __name__ == "__main__":
    main()
