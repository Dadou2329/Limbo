#!/usr/bin/env python3
"""Génère les modèles d'entités pilotés par données et leurs textures.

Usage : python3 tools/models/gen_models.py [--rapport]

Produit, pour chaque modèle de `modeles.py` :
- assets/les_limbes/models/entite/<id>.json : os, cubes, animations (lu par le client Java) ;
- assets/les_limbes/textures/entity/<id>.png : texture peinte cube par cube selon les « peintures » ;
- assets/les_limbes/textures/entity/<id>_lueur.png : parties émissives seulement (si une peinture est lumineuse).
Les textures sont dérivées du modèle : impossible de désaligner un cube et sa texture.
"""
from __future__ import annotations

import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(os.path.dirname(HERE), "textures"))

from PIL import Image  # noqa: E402

from modeles import MODELES  # noqa: E402
from texlib import Box, Rng, draw_on_face, fill_noise, hex_to_rgba, mix, new, paint_box, put, save, shade  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(HERE))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "les_limbes")


def motif_texture(p: dict, seed_base: int):
    """Fabrique la fonction de rendu de face (voir texlib.paint_box) à partir d'une peinture."""
    base = hex_to_rgba(p["base"], p.get("alpha", 255))
    sombre = hex_to_rgba(p["sombre"], p.get("alpha", 255)) if "sombre" in p else shade(base, 0.72)
    clair = hex_to_rgba(p["clair"], p.get("alpha", 255)) if "clair" in p else shade(base, 1.2)
    motif = p.get("motif", "uni")
    variation = p.get("variation", 0.06)
    faces = {k: hex_to_rgba(v, p.get("alpha", 255)) for k, v in p.get("faces", {}).items()}
    fs = {"top": 1.08, "bottom": 0.82, "right": 0.94, "front": 1.0, "left": 0.94, "back": 0.96}

    def texture(face, w, h, seed):
        f = new(w, h)
        couleur = faces.get(face, shade(base, fs[face]))
        rng = Rng(seed_base * 31 + seed)
        if motif == "pierre":
            fill_noise(f, couleur, palette=[sombre, couleur, couleur, clair], scale=3, seed=seed)
        else:
            fill_noise(f, couleur, variation=variation, scale=3, seed=seed)
        px = f.load()
        if motif == "taches":
            for _ in range(max(1, (w * h) // 7)):
                x, y = rng.i(0, w - 1), rng.i(0, h - 1)
                px[x, y] = clair if rng.f() > 0.4 else sombre
        elif motif == "ecailles":
            for y in range(h):
                for x in range(w):
                    if (x + (y // 2) % 2 * 1) % 2 == 0 and y % 2 == 0:
                        px[x, y] = mix(couleur, sombre, 0.5)
                    elif y % 2 == 1 and x % 2 == 1:
                        px[x, y] = mix(couleur, clair, 0.35)
        elif motif == "rayures":
            for y in range(h):
                if y % 3 == 0:
                    for x in range(w):
                        if rng.f() > 0.25:
                            px[x, y] = mix(couleur, sombre, 0.45)
        elif motif == "fourrure":
            for _ in range(max(1, (w * h) // 4)):
                x, y = rng.i(0, w - 1), rng.i(0, h - 1)
                px[x, y] = mix(couleur, sombre if rng.f() > 0.5 else clair, 0.4)
                if y + 1 < h and rng.f() > 0.5:
                    px[x, y + 1] = mix(couleur, sombre, 0.3)
        elif motif == "plumes":
            for y in range(h):
                for x in range(w):
                    if (x + y) % 4 == 0:
                        px[x, y] = mix(couleur, sombre, 0.4)
                    elif (x + y) % 4 == 2:
                        px[x, y] = mix(couleur, clair, 0.25)
        elif motif == "cristal":
            for _ in range(max(1, (w * h) // 5)):
                x, y = rng.i(0, w - 1), rng.i(0, h - 1)
                px[x, y] = clair
                if x + 1 < w:
                    px[x + 1, y] = mix(clair, couleur, 0.5)
        elif motif == "bois":
            for y in range(h):
                for x in range(w):
                    if x % 4 == 0:
                        px[x, y] = mix(couleur, sombre, 0.5)
        return f

    return texture


def peindre_modele(nom: str, m: dict):
    tw, th = m["texture"]
    img = new(tw, th)
    lueur = new(tw, th)
    a_lueur = False
    index = 0
    for o in m["os"]:
        for c in o["cubes"]:
            sx, sy, sz = c["taille"]
            if any(abs(v - round(v)) > 1e-6 for v in (sx, sy, sz)):
                raise ValueError(f"{nom}/{o['nom']} : tailles de cube non entières {c['taille']}")
            box = Box(int(c["uv"][0]), int(c["uv"][1]), int(round(sx)), int(round(sy)), int(round(sz)))
            p = m["peintures"][c["peinture"]]
            index += 1
            seed = index * 17 + len(o["nom"])
            paint_box(img, box, hex_to_rgba(p["base"]), seed=seed, texture=motif_texture(p, seed))
            if p.get("yeux"):
                y = p["yeux"]
                couleur = hex_to_rgba(y.get("couleur", "1A1822"))
                blanc = hex_to_rgba(y.get("blanc", "F4F1EA"))

                def dessiner(put_local, positions=y["positions"], couleur=couleur, blanc=blanc, contour=y.get("contour", True)):
                    for (x, yy) in positions:
                        put_local(x, yy, couleur)
                        if contour:
                            put_local(x, yy + 1, blanc)
                draw_on_face(img, box, y.get("face", "front"), dessiner)
            if p.get("lueur"):
                a_lueur = True
                paint_box(lueur, box, hex_to_rgba(p["base"]), seed=seed, texture=motif_texture(p, seed))
    return img, (lueur if a_lueur else None)


def modele_json(nom: str, m: dict) -> dict:
    noms = {o["nom"] for o in m["os"]}
    for o in m["os"]:
        if o["parent"] and o["parent"] not in noms:
            raise ValueError(f"{nom} : os {o['nom']} a un parent inconnu {o['parent']}")
    for a in m["animations"]:
        if a["os"] not in noms:
            raise ValueError(f"{nom} : animation sur un os inconnu {a['os']}")
    tw, th = m["texture"]
    lueur = any(p.get("lueur") for p in m["peintures"].values())
    return {"texture": [tw, th], "echelle": m.get("echelle", 1.0), "ombre": m.get("ombre", 0.5), "lueur": lueur,
            "os": [{"nom": o["nom"], "parent": o["parent"], "pivot": o["pivot"], "rotation": o["rotation"],
                    "cubes": [{k: c[k] for k in ("origine", "taille", "uv", "gonflement", "miroir")} for c in o["cubes"]]} for o in m["os"]],
            "animations": m["animations"]}


def main() -> None:
    rapport = "--rapport" in sys.argv
    out_models = os.path.join(ASSETS, "models", "entite")
    out_tex = os.path.join(ASSETS, "textures", "entity")
    os.makedirs(out_models, exist_ok=True)
    for nom, m in MODELES.items():
        js = modele_json(nom, m)
        with open(os.path.join(out_models, nom + ".json"), "w", encoding="utf-8") as fh:
            fh.write(json.dumps(js, ensure_ascii=False, separators=(",", ":")) + "\n")
        img, lueur = peindre_modele(nom, m)
        save(img, os.path.join(out_tex, nom + ".png"))
        if lueur is not None:
            save(lueur, os.path.join(out_tex, nom + "_lueur.png"))
        if rapport:
            rep = os.path.join(ROOT, "tools", "models", "rapport")
            os.makedirs(rep, exist_ok=True)
            img.resize((img.width * 4, img.height * 4), Image.NEAREST).save(os.path.join(rep, nom + "_x4.png"))
        print(f"  {nom}: {len(m['os'])} os, {sum(len(o['cubes']) for o in m['os'])} cubes, {len(m['animations'])} animations, texture {m['texture'][0]}x{m['texture'][1]}" + (" + lueur" if lueur else ""))
    print(f"{len(MODELES)} modèle(s) généré(s).")


if __name__ == "__main__":
    main()
