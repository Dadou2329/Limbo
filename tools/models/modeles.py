"""Définitions des modèles d'entités pilotés par données (source unique des JSON et des textures).

Convention (comme Blockbench « Java Entity ») : y vers le bas, le sol est à y=24 ; pivot d'un os
relatif au pivot de son parent ; origine d'un cube relative au pivot de son os. Les tailles de cubes
sont entières (dépliage UV Minecraft).

Chaque modèle :
  texture   : (largeur, hauteur) de la texture
  os        : liste d'os {nom, parent, pivot, rotation, cubes[{origine, taille, uv, peinture, gonflement, miroir}]}
  animations: liste {os, propriete (rx|ry|rz|x|y|z), pilote, amplitude, frequence, phase, valeur, base}
  peintures : nom -> {base, sombre, clair, motif, variation, lueur, alpha, yeux}
  echelle, ombre
"""
from __future__ import annotations


def cube(origine, taille, uv, peinture, gonflement=0.0, miroir=False):
    return {"origine": list(origine), "taille": list(taille), "uv": list(uv), "peinture": peinture, "gonflement": gonflement, "miroir": miroir}


def os(nom, pivot, cubes, parent="", rotation=(0.0, 0.0, 0.0)):
    return {"nom": nom, "parent": parent, "pivot": list(pivot), "rotation": list(rotation), "cubes": cubes}


def anim(os_, propriete, pilote, amplitude, frequence=1.0, phase=0.0, valeur=0, base=0.0):
    return {"os": os_, "propriete": propriete, "pilote": pilote, "amplitude": amplitude, "frequence": frequence, "phase": phase, "valeur": valeur, "base": base}


def peinture(base, sombre=None, clair=None, motif="uni", variation=0.06, lueur=False, alpha=255, yeux=None, faces=None):
    p = {"base": base, "motif": motif, "variation": variation, "lueur": lueur, "alpha": alpha}
    if sombre:
        p["sombre"] = sombre
    if clair:
        p["clair"] = clair
    if yeux:
        p["yeux"] = yeux
    if faces:
        p["faces"] = faces
    return p


PI = 3.14159265

# ----------------------------------------------------------------------------- Raie céleste
# Grande raie plate qui nage dans l'air : corps 10x3x16, deux ailes 12x1x14, queue 2x1x10, tête 6x2x4.
RAIE_CELESTE = {
    "texture": (128, 64),
    "echelle": 1.0,
    "ombre": 0.9,
    "os": [
        os("corps", (0, 13, 0), [cube((-5, -1.5, -8), (10, 3, 16), (0, 0), "dos")]),
        os("tete", (0, -0.5, -8), [cube((-3, -1.5, -4), (6, 2, 4), (28, 36), "dos")], parent="corps"),
        os("aile_g", (5, 0, 0), [cube((0, -0.5, -7), (12, 1, 14), (0, 20), "aile")], parent="corps"),
        os("aile_d", (-5, 0, 0), [cube((-12, -0.5, -7), (12, 1, 14), (52, 20), "aile", miroir=True)], parent="corps"),
        os("queue", (0, 0, 8), [cube((-1, -0.5, 0), (2, 1, 10), (0, 36), "queue")], parent="corps"),
        os("lanternes", (0, 1.5, 0), [cube((-2, 0, -3), (4, 1, 6), (56, 36), "lueur")], parent="corps"),
    ],
    "animations": [
        anim("aile_g", "rz", "vol", -0.55, 0.22, 0.0),
        anim("aile_d", "rz", "vol", 0.55, 0.22, 0.0),
        anim("aile_g", "rz", "temps", -0.08, 0.05, 1.0),
        anim("aile_d", "rz", "temps", 0.08, 0.05, 1.0),
        anim("corps", "y", "temps", 0.9, 0.11, 0.0),
        anim("queue", "ry", "temps", 0.22, 0.14, 0.5),
        anim("queue", "rx", "vol", 0.12, 0.22, 1.5),
        anim("tete", "rx", "regard_pitch", 0.4),
        anim("corps", "rx", "vitesse_verticale", -0.5),
    ],
    "peintures": {
        "dos": peinture("8FB5DE", sombre="5C7FB0", clair="C9DCF2", motif="taches", variation=0.05,
                        faces={"bottom": "E9EEF8"}, yeux={"face": "front", "positions": [[1, 0], [4, 0]], "couleur": "1F2A4D"}),
        "aile": peinture("A7C6E8", sombre="6F93C4", clair="D6E6F6", motif="rayures", variation=0.05, faces={"bottom": "EEF3FA"}),
        "queue": peinture("8FB5DE", sombre="5C7FB0", motif="uni", variation=0.08),
        "lueur": peinture("FFF3C8", clair="FFFFFF", motif="uni", variation=0.02, lueur=True),
    },
}

MODELES = {
    "raie_celeste": RAIE_CELESTE,
}
