#!/usr/bin/env python3
"""Génère les fichiers de données et d'assets JSON du mod Les Limbes.

Usage : python3 tools/datagen/gen_data.py

Produit (de façon déterministe, à relancer après toute modification) :
- assets : blockstates, modèles de blocs et d'objets, particules, copie fr_fr -> en_us
- data   : tags, tables de butin, recettes, progrès, génération de monde (dimension, type, bruit,
           fonctions de densité, biomes, features), ensembles de structures « vestiges »
Les structures NBT et les structures composées sont produites par tools/structgen/build_structures.py.
"""
from __future__ import annotations

import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "les_limbes")
DATA = os.path.join(RES, "data", "les_limbes")
NS = "les_limbes"

written = 0


def write(path: str, obj) -> None:
    global written
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(obj, fh, indent=2, ensure_ascii=False)
        fh.write("\n")
    written += 1


def asset(kind: str, name: str, obj) -> None:
    write(os.path.join(ASSETS, kind, name + ".json"), obj)


def data(kind: str, name: str, obj) -> None:
    write(os.path.join(DATA, kind, name + ".json"), obj)


def mc(path: str) -> str:
    return "minecraft:" + path


def lb(path: str) -> str:
    return NS + ":" + path


# ----------------------------------------------------------------------------- blocs
# Blocs que la génération peut poser (sols, plafonds, plantes, coraux, cristaux, arbres) : étiquette
# les_limbes:terrain_naturel, lue par ArriveeManager.naturel pour qu'une Arrivée ne détruise jamais une construction.
TERRAIN_NATUREL = ["pierre_nacree", "terre_des_limbes", "herbe_argentee", "herbe_des_geants", "sable_de_nacre", "sable_d_opale",
                   "gres_des_heures", "pierre_des_heures", "pierre_de_verre", "verre_de_montagne", "pierre_de_couronne",
                   "corail_azur", "corail_de_nacre", "cristal_de_nacre", "cristal_resonnant", "fibres_nacrees", "herbe_de_l_aube",
                   "fleur_de_rosee", "petale_bleu", "petale_rose", "buche_d_aube", "feuilles_d_aube", "pousse_d_aube",
                   "buche_de_geant", "feuilles_de_geant"]
CUBE_ALL = ["pierre_nacree", "pierre_nacree_taillee", "pierre_nacree_polie", "pierre_nacree_sculptee",
            "planches_d_aube", "terre_des_limbes", "cristal_de_nacre", "feuilles_d_aube", "meule_royale",
            # régions (étape 5)
            "sable_de_nacre", "corail_de_nacre", "corail_azur", "planches_de_geant", "feuilles_de_geant", "petale_rose", "petale_bleu",
            "sable_d_opale", "gres_des_heures", "gres_des_heures_taille", "pierre_des_heures",
            "pierre_de_verre", "pierre_de_verre_taillee", "verre_de_montagne", "cristal_resonnant",
            "pierre_de_couronne", "pierre_de_couronne_taillee", "pierre_de_couronne_polie", "pierre_de_couronne_sculptee"]
# Blocs régionaux à modèle particulier (pilier, herbe, lanterne, colonnes, blocs horaires)
REGIONS_SPECIAUX = ["buche_de_geant", "herbe_des_geants", "lanterne_royale", "seve_ascendante", "courant_cristallin", "cadran_des_heures", "pave_horaire", "pont_de_verre"]
CROSS = ["fibres_nacrees", "herbe_de_l_aube", "fleur_de_rosee", "pousse_d_aube"]


def gen_blocks() -> None:
    for b in CUBE_ALL:
        asset("blockstates", b, {"variants": {"": {"model": lb("block/" + b)}}})
        asset("models/block", b, {"parent": mc("block/cube_all"), "textures": {"all": lb("block/" + b)}})
        asset("models/item", b, {"parent": lb("block/" + b)})
    # herbe argentée : 4 rotations pour casser la répétition
    asset("blockstates", "herbe_argentee", {"variants": {"": [
        {"model": lb("block/herbe_argentee")}, {"model": lb("block/herbe_argentee"), "y": 90},
        {"model": lb("block/herbe_argentee"), "y": 180}, {"model": lb("block/herbe_argentee"), "y": 270}]}})
    asset("models/block", "herbe_argentee", {"parent": mc("block/cube_bottom_top"), "textures": {
        "top": lb("block/herbe_argentee_top"), "side": lb("block/herbe_argentee_side"), "bottom": lb("block/terre_des_limbes")}})
    asset("models/item", "herbe_argentee", {"parent": lb("block/herbe_argentee")})
    # bûche
    asset("blockstates", "buche_d_aube", {"variants": {
        "axis=x": {"model": lb("block/buche_d_aube_horizontal"), "x": 90, "y": 90},
        "axis=y": {"model": lb("block/buche_d_aube")},
        "axis=z": {"model": lb("block/buche_d_aube_horizontal"), "x": 90}}})
    asset("models/block", "buche_d_aube", {"parent": mc("block/cube_column"), "textures": {"end": lb("block/buche_d_aube_top"), "side": lb("block/buche_d_aube")}})
    asset("models/block", "buche_d_aube_horizontal", {"parent": mc("block/cube_column_horizontal"), "textures": {"end": lb("block/buche_d_aube_top"), "side": lb("block/buche_d_aube")}})
    asset("models/item", "buche_d_aube", {"parent": lb("block/buche_d_aube")})
    # plantes en croix
    for b in CROSS:
        asset("blockstates", b, {"variants": {"": {"model": lb("block/" + b)}}})
        asset("models/block", b, {"parent": mc("block/cross"), "textures": {"cross": lb("block/" + b)}})
        asset("models/item", b, {"parent": mc("item/generated"), "textures": {"layer0": lb("block/" + b)}})
    # lanterne nacrée
    asset("blockstates", "lanterne_nacree", {"variants": {
        "hanging=false": {"model": lb("block/lanterne_nacree")}, "hanging=true": {"model": lb("block/lanterne_nacree_suspendue")}}})
    asset("models/block", "lanterne_nacree", {"parent": mc("block/template_lantern"), "textures": {"lantern": lb("block/lanterne_nacree")}})
    asset("models/block", "lanterne_nacree_suspendue", {"parent": mc("block/template_hanging_lantern"), "textures": {"lantern": lb("block/lanterne_nacree")}})
    asset("models/item", "lanterne_nacree", {"parent": mc("item/generated"), "textures": {"layer0": lb("block/lanterne_nacree")}})
    # escaliers
    for name, tex in [("escalier_de_pierre_nacree", "pierre_nacree_taillee"), ("escalier_d_aube", "planches_d_aube")]:
        variants = {}
        for facing, y in [("east", 0), ("west", 180), ("south", 90), ("north", 270)]:
            for half in ["bottom", "top"]:
                for shape in ["straight", "inner_left", "inner_right", "outer_left", "outer_right"]:
                    model = name if shape == "straight" else name + ("_inner" if shape.startswith("inner") else "_outer")
                    rot = y
                    if shape in ("inner_left", "outer_left"):
                        rot = (y + 270) % 360
                    if half == "top":
                        rot = (rot + 90) % 360 if shape != "straight" else rot
                    entry = {"model": lb("block/" + model)}
                    if half == "top":
                        entry["x"] = 180
                    if rot:
                        entry["y"] = rot
                    if half == "top" or rot:
                        entry["uvlock"] = True
                    variants[f"facing={facing},half={half},shape={shape}"] = entry
        asset("blockstates", name, {"variants": variants})
        t = {"bottom": lb("block/" + tex), "top": lb("block/" + tex), "side": lb("block/" + tex)}
        asset("models/block", name, {"parent": mc("block/stairs"), "textures": t})
        asset("models/block", name + "_inner", {"parent": mc("block/inner_stairs"), "textures": t})
        asset("models/block", name + "_outer", {"parent": mc("block/outer_stairs"), "textures": t})
        asset("models/item", name, {"parent": lb("block/" + name)})
    # dalles
    for name, tex, full in [("dalle_de_pierre_nacree", "pierre_nacree_taillee", "pierre_nacree_taillee"), ("dalle_d_aube", "planches_d_aube", "planches_d_aube")]:
        asset("blockstates", name, {"variants": {
            "type=bottom": {"model": lb("block/" + name)}, "type=double": {"model": lb("block/" + full)}, "type=top": {"model": lb("block/" + name + "_haut")}}})
        t = {"bottom": lb("block/" + tex), "top": lb("block/" + tex), "side": lb("block/" + tex)}
        asset("models/block", name, {"parent": mc("block/slab"), "textures": t})
        asset("models/block", name + "_haut", {"parent": mc("block/slab_top"), "textures": t})
        asset("models/item", name, {"parent": lb("block/" + name)})
    # muret
    name, tex = "muret_de_pierre_nacree", "pierre_nacree_taillee"
    asset("blockstates", name, {"multipart": [
        {"apply": {"model": lb("block/" + name + "_poteau")}, "when": {"up": "true"}},
        {"apply": {"model": lb("block/" + name + "_cote"), "uvlock": True}, "when": {"north": "low"}},
        {"apply": {"model": lb("block/" + name + "_cote"), "uvlock": True, "y": 90}, "when": {"east": "low"}},
        {"apply": {"model": lb("block/" + name + "_cote"), "uvlock": True, "y": 180}, "when": {"south": "low"}},
        {"apply": {"model": lb("block/" + name + "_cote"), "uvlock": True, "y": 270}, "when": {"west": "low"}},
        {"apply": {"model": lb("block/" + name + "_cote_haut"), "uvlock": True}, "when": {"north": "tall"}},
        {"apply": {"model": lb("block/" + name + "_cote_haut"), "uvlock": True, "y": 90}, "when": {"east": "tall"}},
        {"apply": {"model": lb("block/" + name + "_cote_haut"), "uvlock": True, "y": 180}, "when": {"south": "tall"}},
        {"apply": {"model": lb("block/" + name + "_cote_haut"), "uvlock": True, "y": 270}, "when": {"west": "tall"}}]})
    asset("models/block", name + "_poteau", {"parent": mc("block/template_wall_post"), "textures": {"wall": lb("block/" + tex)}})
    asset("models/block", name + "_cote", {"parent": mc("block/template_wall_side"), "textures": {"wall": lb("block/" + tex)}})
    asset("models/block", name + "_cote_haut", {"parent": mc("block/template_wall_side_tall"), "textures": {"wall": lb("block/" + tex)}})
    asset("models/item", name, {"parent": mc("block/wall_inventory"), "textures": {"wall": lb("block/" + tex)}})
    # barrière
    name, tex = "barriere_d_aube", "planches_d_aube"
    asset("blockstates", name, {"multipart": [
        {"apply": {"model": lb("block/" + name + "_poteau")}},
        {"apply": {"model": lb("block/" + name + "_cote"), "uvlock": True}, "when": {"north": "true"}},
        {"apply": {"model": lb("block/" + name + "_cote"), "uvlock": True, "y": 90}, "when": {"east": "true"}},
        {"apply": {"model": lb("block/" + name + "_cote"), "uvlock": True, "y": 180}, "when": {"south": "true"}},
        {"apply": {"model": lb("block/" + name + "_cote"), "uvlock": True, "y": 270}, "when": {"west": "true"}}]})
    asset("models/block", name + "_poteau", {"parent": mc("block/fence_post"), "textures": {"texture": lb("block/" + tex)}})
    asset("models/block", name + "_cote", {"parent": mc("block/fence_side"), "textures": {"texture": lb("block/" + tex)}})
    asset("models/item", name, {"parent": mc("block/fence_inventory"), "textures": {"texture": lb("block/" + tex)}})
    # porte d'aube (copie de la logique vanilla des portes)
    name = "porte_d_aube"
    variants = {}
    for facing, y in [("east", 0), ("south", 90), ("west", 180), ("north", 270)]:
        for half in ["lower", "upper"]:
            for hinge in ["left", "right"]:
                for open_ in ["false", "true"]:
                    part = "bottom" if half == "lower" else "top"
                    model = f"{name}_{part}_{hinge}" + ("_open" if open_ == "true" else "")
                    rot = y
                    if open_ == "true":
                        rot = (y + (90 if hinge == "left" else 270)) % 360
                    entry = {"model": lb("block/" + model)}
                    if rot:
                        entry["y"] = rot
                    variants[f"facing={facing},half={half},hinge={hinge},open={open_}"] = entry
    asset("blockstates", name, {"variants": variants})
    for part in ["bottom", "top"]:
        for hinge in ["left", "right"]:
            for suffix in ["", "_open"]:
                asset("models/block", f"{name}_{part}_{hinge}{suffix}", {"parent": mc(f"block/door_{part}_{hinge}{suffix}"),
                                                                      "textures": {"bottom": lb("block/porte_d_aube_bottom"), "top": lb("block/porte_d_aube_top")}})
    asset("models/item", name, {"parent": mc("item/generated"), "textures": {"layer0": lb("item/porte_d_aube")}})
    # Porte des Limbes
    asset("blockstates", "porte_des_limbes", {"variants": {
        "active=false,axis=x": {"model": lb("block/porte_des_limbes_dormante")},
        "active=false,axis=z": {"model": lb("block/porte_des_limbes_dormante")},
        "active=true,axis=x": {"model": lb("block/porte_des_limbes_active")},
        "active=true,axis=z": {"model": lb("block/porte_des_limbes_active"), "y": 90}}})
    asset("models/block", "porte_des_limbes_dormante", {"parent": mc("block/cube_all"), "textures": {"all": lb("block/porte_des_limbes_dormante")}})
    asset("models/block", "porte_des_limbes_active", {
        "textures": {"particle": lb("block/porte_des_limbes_active"), "voile": lb("block/porte_des_limbes_active")},
        "elements": [{"from": [0, 0, 6], "to": [16, 16, 10], "faces": {
            "north": {"uv": [0, 0, 16, 16], "texture": "#voile"}, "south": {"uv": [0, 0, 16, 16], "texture": "#voile"},
            "east": {"uv": [6, 0, 10, 16], "texture": "#voile"}, "west": {"uv": [6, 0, 10, 16], "texture": "#voile"},
            "up": {"uv": [0, 6, 16, 10], "texture": "#voile"}, "down": {"uv": [0, 6, 16, 10], "texture": "#voile"}}}]})
    asset("models/item", "porte_des_limbes", {"parent": lb("block/porte_des_limbes_dormante")})
    # Dalle de rosée : cachée (translucide) ou révélée (lumineuse)
    asset("blockstates", "dalle_de_rosee", {"variants": {
        "revelee=false": {"model": lb("block/dalle_de_rosee_cachee")}, "revelee=true": {"model": lb("block/dalle_de_rosee")}}})
    asset("models/block", "dalle_de_rosee", {"parent": mc("block/cube_all"), "textures": {"all": lb("block/dalle_de_rosee")}})
    asset("models/block", "dalle_de_rosee_cachee", {"parent": mc("block/cube_all"), "textures": {"all": lb("block/dalle_de_rosee_cachee")}})
    asset("models/item", "dalle_de_rosee", {"parent": lb("block/dalle_de_rosee")})
    # Meule du moulin (colonne) et axe des ailes (petit cube, les ailes sont rendues par le bloc-entité)
    asset("blockstates", "meule_de_moulin", {"variants": {"": {"model": lb("block/meule_de_moulin")}}})
    asset("models/block", "meule_de_moulin", {"parent": mc("block/cube_column"), "textures": {"end": lb("block/meule_de_moulin_top"), "side": lb("block/meule_de_moulin")}})
    asset("models/item", "meule_de_moulin", {"parent": lb("block/meule_de_moulin")})
    variants = {}
    for facing, y in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]:
        for actif in ("false", "true"):
            entry = {"model": lb("block/ailes_de_moulin")}
            if y:
                entry["y"] = y
            variants[f"actif={actif},facing={facing}"] = entry
    asset("blockstates", "ailes_de_moulin", {"variants": variants})
    asset("models/block", "ailes_de_moulin", {"textures": {"particle": lb("block/buche_d_aube"), "axe": lb("block/buche_d_aube")},
                                             "elements": [{"from": [5, 5, 0], "to": [11, 11, 16], "faces": {f: {"texture": "#axe"} for f in ["north", "south", "east", "west", "up", "down"]}}]})
    asset("models/item", "ailes_de_moulin", {"parent": lb("block/ailes_de_moulin")})
    # Vitrine (galerie des merveilles) : socle nacré, cage de verre ; l'objet exposé est rendu par le bloc-entité
    variants = {}
    for facing, y in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]:
        entry = {"model": lb("block/vitrine")}
        if y:
            entry["y"] = y
        variants[f"facing={facing}"] = entry
    asset("blockstates", "vitrine", {"variants": variants})
    faces_verre = {f: {"texture": "#verre"} for f in ["north", "south", "east", "west", "up"]}
    asset("models/block", "vitrine", {"textures": {"particle": lb("block/vitrine"), "verre": lb("block/vitrine"), "socle": lb("block/vitrine_socle")},
                                      "elements": [
                                          {"from": [1, 0, 1], "to": [15, 3, 15], "faces": {f: {"texture": "#socle"} for f in ["north", "south", "east", "west", "up", "down"]}},
                                          {"from": [2, 3, 2], "to": [14, 15, 14], "faces": faces_verre}]})
    asset("models/item", "vitrine", {"parent": lb("block/vitrine")})
    # Porte de relais : arche de deux blocs de haut (éléments jusqu'à y=32) ; voile éteint ou éveillé
    variants = {}
    for facing, y in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]:
        for eveillee in ("false", "true"):
            entry = {"model": lb("block/porte_de_relais" + ("_eveillee" if eveillee == "true" else ""))}
            if y:
                entry["y"] = y
            variants[f"eveillee={eveillee},facing={facing}"] = entry
    asset("blockstates", "porte_de_relais", {"variants": variants})
    for suffixe, voile in [("", "porte_de_relais_voile_eteint"), ("_eveillee", "porte_de_relais_voile")]:
        pierre = {f: {"texture": "#pierre"} for f in ["north", "south", "east", "west", "up", "down"]}
        asset("models/block", "porte_de_relais" + suffixe, {
            "textures": {"particle": lb("block/porte_de_relais"), "pierre": lb("block/porte_de_relais"), "voile": lb("block/" + voile)},
            "elements": [
                {"from": [0, 0, 5], "to": [3, 29, 11], "faces": pierre},
                {"from": [13, 0, 5], "to": [16, 29, 11], "faces": pierre},
                {"from": [0, 29, 5], "to": [16, 32, 11], "faces": pierre},
                {"from": [3, 0, 7], "to": [13, 29, 9], "faces": {"north": {"texture": "#voile"}, "south": {"texture": "#voile"}}}]})
    asset("models/item", "porte_de_relais", {"parent": lb("block/porte_de_relais_eveillee")})
    # --- Régions ---
    # bûche de géant (pilier)
    asset("blockstates", "buche_de_geant", {"variants": {
        "axis=x": {"model": lb("block/buche_de_geant_horizontal"), "x": 90, "y": 90},
        "axis=y": {"model": lb("block/buche_de_geant")},
        "axis=z": {"model": lb("block/buche_de_geant_horizontal"), "x": 90}}})
    asset("models/block", "buche_de_geant", {"parent": mc("block/cube_column"), "textures": {"end": lb("block/buche_de_geant_top"), "side": lb("block/buche_de_geant")}})
    asset("models/block", "buche_de_geant_horizontal", {"parent": mc("block/cube_column_horizontal"), "textures": {"end": lb("block/buche_de_geant_top"), "side": lb("block/buche_de_geant")}})
    asset("models/item", "buche_de_geant", {"parent": lb("block/buche_de_geant")})
    # herbe des géants
    asset("blockstates", "herbe_des_geants", {"variants": {"": [{"model": lb("block/herbe_des_geants")}, {"model": lb("block/herbe_des_geants"), "y": 90},
                                                                {"model": lb("block/herbe_des_geants"), "y": 180}, {"model": lb("block/herbe_des_geants"), "y": 270}]}})
    asset("models/block", "herbe_des_geants", {"parent": mc("block/cube_bottom_top"), "textures": {
        "top": lb("block/herbe_des_geants_top"), "side": lb("block/herbe_des_geants_side"), "bottom": lb("block/terre_des_limbes")}})
    asset("models/item", "herbe_des_geants", {"parent": lb("block/herbe_des_geants")})
    # lanterne royale
    asset("blockstates", "lanterne_royale", {"variants": {
        "hanging=false": {"model": lb("block/lanterne_royale")}, "hanging=true": {"model": lb("block/lanterne_royale_suspendue")}}})
    asset("models/block", "lanterne_royale", {"parent": mc("block/template_lantern"), "textures": {"lantern": lb("block/lanterne_royale")}})
    asset("models/block", "lanterne_royale_suspendue", {"parent": mc("block/template_hanging_lantern"), "textures": {"lantern": lb("block/lanterne_royale")}})
    asset("models/item", "lanterne_royale", {"parent": mc("item/generated"), "textures": {"layer0": lb("block/lanterne_royale")}})
    # colonnes porteuses (sève, courant) : cube translucide
    for b in ["seve_ascendante", "courant_cristallin"]:
        asset("blockstates", b, {"variants": {"": {"model": lb("block/" + b)}}})
        asset("models/block", b, {"parent": mc("block/cube_all"), "textures": {"all": lb("block/" + b)}})
        asset("models/item", b, {"parent": lb("block/" + b)})
    # cadran des heures : quatre faces supérieures
    asset("blockstates", "cadran_des_heures", {"variants": {f"heure={h}": {"model": lb(f"block/cadran_des_heures_{h}")} for h in range(4)}})
    for h in range(4):
        asset("models/block", f"cadran_des_heures_{h}", {"parent": mc("block/cube_bottom_top"), "textures": {
            "top": lb(f"block/cadran_des_heures_{h}"), "side": lb("block/gres_des_heures_taille"), "bottom": lb("block/gres_des_heures_taille")}})
    asset("models/item", "cadran_des_heures", {"parent": lb("block/cadran_des_heures_1")})
    # pavé horaire : plein quand visible, reflet mince sinon
    variants = {}
    for ph in range(4):
        variants[f"phase={ph},visible=true"] = {"model": lb("block/pave_horaire")}
        variants[f"phase={ph},visible=false"] = {"model": lb("block/pave_horaire_reflet")}
    asset("blockstates", "pave_horaire", {"variants": variants})
    asset("models/block", "pave_horaire", {"parent": mc("block/cube_all"), "textures": {"all": lb("block/pave_horaire")}})
    asset("models/block", "pave_horaire_reflet", {"textures": {"particle": lb("block/pave_horaire_reflet"), "r": lb("block/pave_horaire_reflet")},
                                                  "elements": [{"from": [0, 0, 0], "to": [16, 1, 16], "faces": {f: {"texture": "#r"} for f in ["north", "south", "east", "west", "up", "down"]}}]})
    asset("models/item", "pave_horaire", {"parent": lb("block/pave_horaire")})
    # pont de verre : plein quand formé, trame mince sinon
    asset("blockstates", "pont_de_verre", {"variants": {"forme=true": {"model": lb("block/pont_de_verre")}, "forme=false": {"model": lb("block/pont_de_verre_trame")}}})
    asset("models/block", "pont_de_verre", {"parent": mc("block/cube_all"), "textures": {"all": lb("block/pont_de_verre")}})
    asset("models/block", "pont_de_verre_trame", {"textures": {"particle": lb("block/pont_de_verre_trame"), "r": lb("block/pont_de_verre_trame")},
                                                  "elements": [{"from": [0, 0, 0], "to": [16, 1, 16], "faces": {f: {"texture": "#r"} for f in ["north", "south", "east", "west", "up", "down"]}}]})
    asset("models/item", "pont_de_verre", {"parent": lb("block/pont_de_verre")})
    # Capitale (étape 6) : lanterne éteinte, porte royale (voile fermé / passage ouvert invisible), pont royal.
    asset("blockstates", "lanterne_royale_eteinte", {"variants": {
        "hanging=false": {"model": lb("block/lanterne_royale_eteinte")}, "hanging=true": {"model": lb("block/lanterne_royale_eteinte_suspendue")}}})
    asset("models/block", "lanterne_royale_eteinte", {"parent": mc("block/template_lantern"), "textures": {"lantern": lb("block/lanterne_royale_eteinte")}})
    asset("models/block", "lanterne_royale_eteinte_suspendue", {"parent": mc("block/template_hanging_lantern"), "textures": {"lantern": lb("block/lanterne_royale_eteinte")}})
    asset("models/item", "lanterne_royale_eteinte", {"parent": mc("item/generated"), "textures": {"layer0": lb("block/lanterne_royale_eteinte")}})
    asset("blockstates", "porte_royale", {"variants": {**{f"niveau={n},ouverte=false": {"model": lb("block/porte_royale")} for n in (1, 2, 3)},
                                                      **{f"niveau={n},ouverte=true": {"model": lb("block/porte_royale_ouverte")} for n in (1, 2, 3)}}})
    asset("models/block", "porte_royale", {"parent": mc("block/cube_all"), "textures": {"all": lb("block/porte_royale")}})
    asset("models/block", "porte_royale_ouverte", {"textures": {"particle": lb("block/porte_royale")}, "elements": []})
    asset("models/item", "porte_royale", {"parent": lb("block/porte_royale")})
    asset("blockstates", "pont_royal", {"variants": {"forme=true": {"model": lb("block/pont_royal")}, "forme=false": {"model": lb("block/pont_royal_trame")}}})
    asset("models/block", "pont_royal", {"parent": mc("block/cube_all"), "textures": {"all": lb("block/pont_royal")}})
    asset("models/block", "pont_royal_trame", {"textures": {"particle": lb("block/pont_royal_trame"), "r": lb("block/pont_royal_trame")},
                                               "elements": [{"from": [0, 0, 0], "to": [16, 1, 16], "faces": {f: {"texture": "#r"} for f in ["north", "south", "east", "west", "up", "down"]}}]})
    asset("models/item", "pont_royal", {"parent": lb("block/pont_royal")})
    # Décor (étape 8) : tapis (modèle vanilla de tapis), vase (éléments), guirlande (cordon et trois lanternes, deux axes).
    asset("blockstates", "tapis_de_nacre", {"variants": {"": {"model": lb("block/tapis_de_nacre")}}})
    asset("models/block", "tapis_de_nacre", {"parent": mc("block/carpet"), "textures": {"wool": lb("block/tapis_de_nacre")}})
    asset("models/item", "tapis_de_nacre", {"parent": lb("block/tapis_de_nacre")})
    asset("blockstates", "vase_de_nacre", {"variants": {"": {"model": lb("block/vase_de_nacre")}}})
    asset("models/block", "vase_de_nacre", {"textures": {"particle": lb("block/vase_de_nacre"), "v": lb("block/vase_de_nacre")}, "elements": [
        {"from": [5, 0, 5], "to": [11, 2, 11], "faces": {f: {"texture": "#v", "uv": [5, 14, 11, 16]} for f in ["north", "south", "east", "west", "up", "down"]}},
        {"from": [4, 2, 4], "to": [12, 8, 12], "faces": {f: {"texture": "#v", "uv": [4, 6, 12, 14]} for f in ["north", "south", "east", "west", "up", "down"]}},
        {"from": [5, 8, 5], "to": [11, 11, 11], "faces": {f: {"texture": "#v", "uv": [5, 0, 11, 6]} for f in ["north", "south", "east", "west", "up", "down"]}},
        {"from": [6, 11, 6], "to": [10, 13, 10], "faces": {f: {"texture": "#v", "uv": [6, 0, 10, 2]} for f in ["north", "south", "east", "west", "up"]}}]})
    asset("models/item", "vase_de_nacre", {"parent": lb("block/vase_de_nacre")})
    asset("blockstates", "guirlande_de_fete", {"variants": {"axis=x": {"model": lb("block/guirlande_de_fete")}, "axis=z": {"model": lb("block/guirlande_de_fete"), "y": 90}}})
    asset("models/block", "guirlande_de_fete", {"textures": {"particle": lb("block/guirlande_de_fete"), "g": lb("block/guirlande_de_fete")}, "elements": [
        {"from": [0, 15, 8], "to": [16, 16, 8], "faces": {"north": {"texture": "#g", "uv": [0, 0, 16, 1]}, "south": {"texture": "#g", "uv": [0, 0, 16, 1]}}},
        *[{"from": [x, 9, 7], "to": [x + 2, 14, 9], "faces": {f: {"texture": "#g", "uv": [0, 2, 2, 7]} for f in ["north", "south", "east", "west", "down"]}} for x in (2, 7, 12)],
        *[{"from": [x, 14, 8], "to": [x + 2, 15, 8], "faces": {"north": {"texture": "#g", "uv": [4, 2, 6, 3]}, "south": {"texture": "#g", "uv": [4, 2, 6, 3]}}} for x in (2, 7, 12)]]})
    asset("models/item", "guirlande_de_fete", {"parent": mc("item/generated"), "textures": {"layer0": lb("item/guirlande_de_fete")}})
    # Pont de lumière (décret « Que le chemin s'ouvre ») : cube translucide.
    asset("blockstates", "pont_de_lumiere", {"variants": {"": {"model": lb("block/pont_de_lumiere")}}})
    asset("models/block", "pont_de_lumiere", {"parent": mc("block/cube_all"), "textures": {"all": lb("block/pont_de_lumiere")}})
    asset("models/item", "pont_de_lumiere", {"parent": lb("block/pont_de_lumiere")})


# ----------------------------------------------------------------------------- objets
ITEMS_GENERATED = ["fragment_de_nacre", "boussole_nacree", "fibres_nacrees", "herbe_de_l_aube", "bouillon_de_l_aube",
                   "fragment_de_couronne", "cle_du_manoir", "oeuf_habitant", "oeuf_poisson_lune", "oeuf_cerf_lanterne", "oeuf_veilleur_egare",
                   "sifflet_de_raie", "oeuf_raie_celeste", "farine", "pain_d_emile", "journal_de_bord", "ragout_du_domaine",
                   "poisson_lanterne", "perle_de_nacre", "fruit_de_geant", "miel_de_rosee", "dattes_des_heures", "sablier_d_opale",
                   "the_des_cimes", "eclat_resonnant", "pain_de_la_cour"]

# Espèces régionales (étape 5) : un œuf d'apparition par espèce, texture oeuf_<id>.
ESPECES_REGIONALES = ["crabe_de_nacre", "meduse_celeste", "poisson_lanterne", "gardien_des_epaves", "tortue_des_lagons", "geant_des_jardins", "libellule_de_rosee", "escargot_des_racines", "rodeur_des_racines", "scarabee_de_seve", "fennec_des_dunes", "scorpion_des_heures", "lezard_cadran", "sentinelle_de_sable", "gazelle_d_opale", "bouquetin_de_verre", "papillon_de_cristal", "colosse_de_cristal", "echo_resonnant", "griffon_de_verre", "colombe_astrale", "chat_de_palais", "sentinelle_de_la_couronne", "spectre_de_la_cour", "cerf_royal"]
# Renforts et soutiens des duels (étape 6)
ESPECES_REGIONALES += ["chevalier_de_bois_blanc", "reflet_de_verre", "double_des_heures", "legionnaire_astral", "champion_des_heures", "fleur_de_vie", "point_de_rupture", "banniere_astrale"]
# Garde astrale et règne (étape 7)
ESPECES_REGIONALES += ["chevalier_astral", "archer_astral", "protecteur_astral", "baleine_des_hauteurs"]
ITEMS_GENERATED += ["oeuf_" + e for e in ESPECES_REGIONALES] + ["sceau_royal", "bouclier_du_souverain"]


def gen_items() -> None:
    for it in ITEMS_GENERATED:
        asset("models/item", it, {"parent": mc("item/generated"), "textures": {"layer0": lb("item/" + it)}})
    asset("models/item", "epee_du_souverain", {"parent": mc("item/handheld"), "textures": {"layer0": lb("item/epee_du_souverain")}})
    asset("models/item", "lance_du_souverain", {"parent": mc("item/handheld"), "textures": {"layer0": lb("item/lance_du_souverain")}})
    # Arc du Souverain : mêmes transformations et prédicats que l'arc vanilla (pull / pulling, enregistrés côté client).
    asset("models/item", "arc_du_souverain", {"parent": mc("item/bow"), "textures": {"layer0": lb("item/arc_du_souverain")}, "overrides": [
        {"predicate": {"pulling": 1}, "model": lb("item/arc_du_souverain_pulling_0")},
        {"predicate": {"pulling": 1, "pull": 0.65}, "model": lb("item/arc_du_souverain_pulling_1")},
        {"predicate": {"pulling": 1, "pull": 0.9}, "model": lb("item/arc_du_souverain_pulling_2")}]})
    for n in range(3):
        asset("models/item", f"arc_du_souverain_pulling_{n}", {"parent": lb("item/arc_du_souverain"), "textures": {"layer0": lb(f"item/arc_du_souverain_pulling_{n}")}})


# ----------------------------------------------------------------------------- particules & sons
SOUNDS = {
    "bloc.porte.eveil": ("bloc/porte_eveil", "subtitles.les_limbes.porte.eveil"),
    "bloc.porte.passage": ("bloc/porte_passage", "subtitles.les_limbes.porte.passage"),
    "bloc.porte.ambiance": ("bloc/porte_ambiance", "subtitles.les_limbes.porte.ambiance"),
    "pouvoir.epee": ("pouvoir/epee", "subtitles.les_limbes.pouvoir.epee"),
    "pouvoir.epee_final": ("pouvoir/epee_final", "subtitles.les_limbes.pouvoir.epee_final"),
    "pouvoir.pas": ("pouvoir/pas", "subtitles.les_limbes.pouvoir.pas"),
    "pouvoir.decret": ("pouvoir/decret", "subtitles.les_limbes.pouvoir.decret"),
    "pouvoir.refus": ("pouvoir/refus", "subtitles.les_limbes.pouvoir.refus"),
    "pouvoir.parade": ("pouvoir/parade", "subtitles.les_limbes.pouvoir.parade"),
    "pouvoir.lance": ("pouvoir/lance", "subtitles.les_limbes.pouvoir.lance"),
    "pouvoir.arc": ("pouvoir/arc", "subtitles.les_limbes.pouvoir.arc"),
    "pouvoir.garde": ("pouvoir/garde", "subtitles.les_limbes.pouvoir.garde"),
    "pouvoir.main": ("pouvoir/main", "subtitles.les_limbes.pouvoir.main"),
    "pouvoir.constellation": ("pouvoir/constellation", "subtitles.les_limbes.pouvoir.constellation"),
    "pouvoir.avenement": ("pouvoir/avenement", "subtitles.les_limbes.pouvoir.avenement"),
    "creature.baleine.chant": ("creature/baleine_chant", "subtitles.les_limbes.creature.baleine.chant"),
    "objet.fragment": ("objet/fragment", "subtitles.les_limbes.objet.fragment"),
    "quete.etape": ("quete/etape", "subtitles.les_limbes.quete.etape"),
    "quete.terminee": ("quete/terminee", "subtitles.les_limbes.quete.terminee"),
    "monde.brumes": ("monde/brumes", "subtitles.les_limbes.monde.brumes"),
    "creature.cerf.appel": ("creature/cerf_appel", "subtitles.les_limbes.creature.cerf"),
    "creature.poisson.scintille": ("creature/poisson_scintille", "subtitles.les_limbes.creature.poisson"),
    "creature.veilleur.prepare": ("creature/veilleur_prepare", "subtitles.les_limbes.creature.veilleur.prepare"),
    "creature.veilleur.frappe": ("creature/veilleur_frappe", "subtitles.les_limbes.creature.veilleur.frappe"),
    "creature.veilleur.blesse": ("creature/veilleur_blesse", "subtitles.les_limbes.creature.veilleur.blesse"),
    "creature.veilleur.mort": ("creature/veilleur_mort", "subtitles.les_limbes.creature.veilleur.mort"),
    "interface.dialogue": ("interface/dialogue", "subtitles.les_limbes.interface.dialogue"),
    "creature.raie.appel": ("creature/raie_appel", "subtitles.les_limbes.creature.raie"),
    "creature.raie.pacte": ("creature/raie_pacte", "subtitles.les_limbes.creature.raie.pacte"),
    "monde.arrivee": ("monde/arrivee", "subtitles.les_limbes.monde.arrivee"),
    "bloc.moulin.grince": ("bloc/moulin_grince", "subtitles.les_limbes.bloc.moulin"),
    "bloc.dalle.emerge": ("bloc/dalle_emerge", "subtitles.les_limbes.bloc.dalle"),
    "bloc.cristal.resonne": ("bloc/cristal_resonne", "subtitles.les_limbes.bloc.cristal"),
    "creature.crabe.clic": ("creature/crabe_clic", "subtitles.les_limbes.creature.crabe"),
    "creature.insecte.bourdon": ("creature/insecte_bourdon", "subtitles.les_limbes.creature.insecte"),
    "creature.oiseau.chant": ("creature/oiseau_chant", "subtitles.les_limbes.creature.oiseau"),
    "creature.geant.pas": ("creature/geant_pas", "subtitles.les_limbes.creature.geant"),
    "creature.monture.appel": ("creature/monture_appel", "subtitles.les_limbes.creature.monture"),
    "creature.tireur.tir": ("creature/tireur_tir", "subtitles.les_limbes.creature.tireur"),
    "creature.petit.cri": ("creature/petit_cri", "subtitles.les_limbes.creature.petit"),
}


# Musiques originales (étape 8) : une pièce par région, jouée par ses biomes (voir biome()).
REGIONS_MUSIQUE = ["prairies", "mer", "jardins", "desert", "montagnes", "couronne"]


def gen_particles_sounds() -> None:
    for p in ["etoile_royale", "eclat_de_nacre", "lueur", "brume"]:
        asset("particles", p, {"textures": [lb(p)]})
    sounds = {}
    for event, (path, subtitle) in SOUNDS.items():
        sounds[event] = {"subtitle": subtitle, "sounds": [lb(path)]}
    for region in REGIONS_MUSIQUE:
        sounds["musique." + region] = {"subtitle": "subtitles.les_limbes.musique", "sounds": [{"name": lb("musique/" + region), "stream": True}]}
    write(os.path.join(ASSETS, "sounds.json"), sounds)


# ----------------------------------------------------------------------------- tags
def gen_tags() -> None:
    def tag(kind, name, values, replace=False):
        """Un nom préfixé par « ../../minecraft/tags/<kind>/ » cible le namespace minecraft (tags vanilla à étendre)."""
        prefix = "../../minecraft/tags/" + kind + "/"
        if name.startswith(prefix):
            write(os.path.join(RES, "data", "minecraft", "tags", kind, name[len(prefix):] + ".json"), {"replace": replace, "values": values})
        else:
            data("tags/" + kind, name, {"replace": replace, "values": values})

    pierre = [lb(x) for x in ["pierre_nacree", "pierre_nacree_taillee", "pierre_nacree_polie", "pierre_nacree_sculptee",
                             "escalier_de_pierre_nacree", "dalle_de_pierre_nacree", "muret_de_pierre_nacree", "cristal_de_nacre", "lanterne_nacree",
                             "meule_royale", "meule_de_moulin", "vitrine", "porte_de_relais",
                             "corail_de_nacre", "corail_azur", "gres_des_heures", "gres_des_heures_taille", "pierre_des_heures", "cadran_des_heures",
                             "pierre_de_verre", "pierre_de_verre_taillee", "verre_de_montagne", "cristal_resonnant",
                             "pierre_de_couronne", "pierre_de_couronne_taillee", "pierre_de_couronne_polie", "pierre_de_couronne_sculptee", "lanterne_royale"]]
    bois = [lb(x) for x in ["buche_d_aube", "planches_d_aube", "escalier_d_aube", "dalle_d_aube", "barriere_d_aube", "porte_d_aube", "ailes_de_moulin", "buche_de_geant", "planches_de_geant"]]
    tag("block", "../../minecraft/tags/block/mineable/pickaxe", pierre)
    tag("block", "../../minecraft/tags/block/mineable/axe", bois)
    tag("block", "../../minecraft/tags/block/mineable/shovel", [lb("terre_des_limbes"), lb("herbe_argentee"), lb("sable_de_nacre"), lb("sable_d_opale"), lb("herbe_des_geants")])
    tag("block", "../../minecraft/tags/block/mineable/hoe", [lb("feuilles_d_aube"), lb("feuilles_de_geant"), lb("petale_rose"), lb("petale_bleu")])
    tag("block", "../../minecraft/tags/block/leaves", [lb("feuilles_d_aube"), lb("feuilles_de_geant")])
    tag("block", "../../minecraft/tags/block/logs", [lb("buche_d_aube"), lb("buche_de_geant")])
    tag("block", "../../minecraft/tags/block/logs_that_burn", [lb("buche_d_aube"), lb("buche_de_geant")])
    tag("block", "../../minecraft/tags/block/sand", [lb("sable_de_nacre"), lb("sable_d_opale")])
    tag("block", "../../minecraft/tags/block/dirt", [lb("terre_des_limbes"), lb("herbe_argentee"), lb("herbe_des_geants")])
    tag("block", "../../minecraft/tags/block/animals_spawnable_on", [lb("herbe_argentee")])
    tag("block", "terrain_naturel", [lb(b) for b in TERRAIN_NATUREL])
    # Trouvailles posées par la génération (lanternes perdues) : une Arrivée peut se poser dessus, elles tombent en objets.
    tag("block", "trouvailles", [lb("lanterne_nacree"), lb("lanterne_royale")])
    tag("block", "../../minecraft/tags/block/planks", [lb("planches_d_aube")])
    tag("block", "../../minecraft/tags/block/saplings", [lb("pousse_d_aube")])
    tag("block", "../../minecraft/tags/block/fences", [lb("barriere_d_aube")])
    tag("block", "../../minecraft/tags/block/wooden_fences", [lb("barriere_d_aube")])
    tag("block", "../../minecraft/tags/block/walls", [lb("muret_de_pierre_nacree")])
    tag("block", "../../minecraft/tags/block/stairs", [lb("escalier_de_pierre_nacree"), lb("escalier_d_aube")])
    tag("block", "../../minecraft/tags/block/wooden_stairs", [lb("escalier_d_aube")])
    tag("block", "../../minecraft/tags/block/slabs", [lb("dalle_de_pierre_nacree"), lb("dalle_d_aube")])
    tag("block", "../../minecraft/tags/block/wooden_slabs", [lb("dalle_d_aube")])
    tag("block", "../../minecraft/tags/block/doors", [lb("porte_d_aube")])
    tag("block", "../../minecraft/tags/block/wooden_doors", [lb("porte_d_aube")])
    tag("block", "../../minecraft/tags/block/flowers", [lb("fleur_de_rosee")])
    tag("block", "../../minecraft/tags/block/small_flowers", [lb("fleur_de_rosee")])
    tag("block", "../../minecraft/tags/block/replaceable_by_trees", [lb("fibres_nacrees"), lb("herbe_de_l_aube")])
    tag("block", "../../minecraft/tags/block/sword_efficient", [lb("fibres_nacrees"), lb("herbe_de_l_aube"), lb("feuilles_d_aube")])
    tag("block", "../../minecraft/tags/block/enderman_holdable", [lb("terre_des_limbes")])
    tag("block", "../../minecraft/tags/block/stone_bricks", [lb("pierre_nacree_taillee")])
    tag("block", "../../minecraft/tags/block/base_stone_overworld", [lb("pierre_nacree")])
    tag("block", "../../minecraft/tags/block/dragon_immune", [lb("porte_des_limbes")])
    tag("block", "../../minecraft/tags/block/wither_immune", [lb("porte_des_limbes")])
    tag("item", "../../minecraft/tags/item/logs", [lb("buche_d_aube")])
    tag("item", "../../minecraft/tags/item/logs_that_burn", [lb("buche_d_aube")])
    tag("item", "../../minecraft/tags/item/planks", [lb("planches_d_aube")])
    tag("item", "../../minecraft/tags/item/leaves", [lb("feuilles_d_aube")])
    tag("item", "../../minecraft/tags/item/saplings", [lb("pousse_d_aube")])
    tag("item", "../../minecraft/tags/item/fences", [lb("barriere_d_aube")])
    tag("item", "../../minecraft/tags/item/wooden_fences", [lb("barriere_d_aube")])
    tag("item", "../../minecraft/tags/item/walls", [lb("muret_de_pierre_nacree")])
    tag("item", "../../minecraft/tags/item/stairs", [lb("escalier_de_pierre_nacree"), lb("escalier_d_aube")])
    tag("item", "../../minecraft/tags/item/slabs", [lb("dalle_de_pierre_nacree"), lb("dalle_d_aube")])
    tag("item", "../../minecraft/tags/item/doors", [lb("porte_d_aube")])
    tag("item", "../../minecraft/tags/item/wooden_doors", [lb("porte_d_aube")])
    tag("item", "../../minecraft/tags/item/flowers", [lb("fleur_de_rosee")])
    tag("item", "../../minecraft/tags/item/swords", [lb("epee_du_souverain")])
    tag("item", "../../minecraft/tags/item/stone_bricks", [lb("pierre_nacree_taillee")])
    # tags du mod
    tag("worldgen/biome", "limbes", [lb(b) for b in TOUS_LES_BIOMES])
    for region, biomes in BIOMES_PAR_REGION.items():
        tag("worldgen/biome", region, [lb(b) for b in biomes])
    tag("worldgen/biome", "a_vestiges", [mc(x) for x in ["plains", "sunflower_plains", "forest", "flower_forest", "birch_forest", "meadow",
                                                         "savanna", "taiga", "cherry_grove", "dark_forest", "old_growth_birch_forest", "snowy_plains", "windswept_hills"]])
    tag("worldgen/structure", "vestiges", [lb("vestige")])
    tag("entity_type", "../../minecraft/tags/entity_type/fall_damage_immune", [lb("poisson_lune"), lb("raie_celeste"), lb("meduse_celeste"), lb("libellule_de_rosee"),
                                                                              lb("papillon_de_cristal"), lb("colombe_astrale"), lb("griffon_de_verre"), lb("echo_resonnant")])
    # Respiration sous l'eau : la respiration des entités vivantes est décidée par cette étiquette vanilla (méthode finale).
    tag("entity_type", "../../minecraft/tags/entity_type/can_breathe_under_water", [lb("poisson_lanterne"), lb("tortue_des_lagons"), lb("crabe_de_nacre")])
    tag("entity_type", "../../minecraft/tags/entity_type/aquatic", [lb("poisson_lanterne")])


# ----------------------------------------------------------------------------- butin
def self_drop(block: str):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": lb(block)}],
                                                 "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def silk_or(block: str, alt: str):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": lb(block), "conditions": [{"condition": "minecraft:match_tool", "predicate": {"predicates": {
            "minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
        {"type": "minecraft:item", "name": lb(alt), "conditions": [{"condition": "minecraft:survives_explosion"}]}]}]}]}


def butin_entite(entrees):
    """Table de butin d'une créature : liste de (objet, min, max, chance)."""
    pools = []
    for n, a, b, chance in entrees:
        entry = {"type": "minecraft:item", "name": n, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": a, "max": b}},
                                                                    {"function": "minecraft:enchanted_count_increase", "enchantment": "minecraft:looting", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}
        pool = {"rolls": 1, "bonus_rolls": 0, "entries": [entry]}
        if chance < 1:
            pool["conditions"] = [{"condition": "minecraft:random_chance", "chance": chance}]
        pools.append(pool)
    return {"type": "minecraft:entity", "pools": pools}


BUTINS_ENTITES = {
    "crabe_de_nacre": [(lb("perle_de_nacre"), 1, 1, 0.25)],
    "poisson_lanterne": [(lb("poisson_lanterne"), 1, 1, 1.0)],
    "gardien_des_epaves": [(lb("perle_de_nacre"), 1, 2, 1.0), ("minecraft:prismarine_crystals", 1, 2, 0.5)],
    "rodeur_des_racines": [(lb("fruit_de_geant"), 1, 2, 0.6)],
    "scorpion_des_heures": [(lb("sable_d_opale"), 1, 2, 0.5)],
    "sentinelle_de_sable": [(lb("sable_d_opale"), 1, 3, 1.0), (lb("gres_des_heures"), 1, 2, 0.4)],
    "colosse_de_cristal": [(lb("eclat_resonnant"), 1, 3, 1.0), (lb("pierre_de_verre"), 1, 2, 0.5)],
    "echo_resonnant": [(lb("eclat_resonnant"), 0, 1, 1.0)],
    "sentinelle_de_la_couronne": [(lb("pierre_de_couronne"), 1, 2, 1.0), (lb("lanterne_royale"), 1, 1, 0.15)],
    "spectre_de_la_cour": [(lb("pain_de_la_cour"), 1, 1, 0.3)],
    "fennec_des_dunes": [], "lezard_cadran": [], "escargot_des_racines": [], "chat_de_palais": [], "bouquetin_de_verre": [], "geant_des_jardins": [],
    "meduse_celeste": [], "libellule_de_rosee": [], "papillon_de_cristal": [], "colombe_astrale": [],
    "tortue_des_lagons": [], "scarabee_de_seve": [], "gazelle_d_opale": [], "griffon_de_verre": [], "cerf_royal": [],
    "chevalier_de_bois_blanc": [], "reflet_de_verre": [(lb("eclat_resonnant"), 0, 1, 1.0)], "double_des_heures": [], "legionnaire_astral": [(lb("pierre_de_couronne"), 0, 1, 1.0)],
    "champion_des_heures": [(lb("dattes_des_heures"), 1, 3, 1.0)], "fleur_de_vie": [], "point_de_rupture": [], "banniere_astrale": [],
    "chevalier_astral": [], "archer_astral": [], "protecteur_astral": [], "baleine_des_hauteurs": [(lb("perle_de_nacre"), 1, 3, 1.0), ("minecraft:leather", 2, 5, 1.0)],
}


def gen_loot() -> None:
    for ident, entrees in BUTINS_ENTITES.items():
        data("loot_table/entities", ident, butin_entite(entrees))
    for b in ["pierre_nacree", "pierre_nacree_taillee", "pierre_nacree_polie", "pierre_nacree_sculptee", "escalier_de_pierre_nacree",
              "muret_de_pierre_nacree", "cristal_de_nacre", "terre_des_limbes", "buche_d_aube", "planches_d_aube", "escalier_d_aube",
              "barriere_d_aube", "pousse_d_aube", "fleur_de_rosee", "lanterne_nacree", "meule_royale", "meule_de_moulin", "ailes_de_moulin",
              "vitrine", "porte_de_relais",
              "sable_de_nacre", "corail_de_nacre", "corail_azur", "buche_de_geant", "planches_de_geant", "herbe_des_geants", "petale_rose", "petale_bleu", "seve_ascendante",
              "sable_d_opale", "gres_des_heures", "gres_des_heures_taille", "pierre_des_heures", "cadran_des_heures",
              "pierre_de_verre", "pierre_de_verre_taillee", "verre_de_montagne", "courant_cristallin",
              "pierre_de_couronne", "pierre_de_couronne_taillee", "pierre_de_couronne_polie", "pierre_de_couronne_sculptee", "lanterne_royale",
              "tapis_de_nacre", "vase_de_nacre", "guirlande_de_fete"]:
        data("loot_table/blocks", b, self_drop(b))
    for b in ["pave_horaire", "pont_de_verre", "porte_royale", "pont_royal", "pont_de_lumiere"]:
        data("loot_table/blocks", b, {"type": "minecraft:block", "pools": []})
    data("loot_table/blocks", "lanterne_royale_eteinte", self_drop("lanterne_royale_eteinte"))
    data("loot_table/blocks", "feuilles_de_geant", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": lb("fruit_de_geant"),
        "conditions": [{"condition": "minecraft:random_chance", "chance": 0.08}]}]}]})
    data("loot_table/blocks", "dalle_de_rosee", {"type": "minecraft:block", "pools": []})
    # Cristal résonnant : 1 à 2 éclats résonnants à la main, le bloc entier au toucher de soie (quête « Le pont qui chante »).
    data("loot_table/blocks", "cristal_resonnant", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": lb("cristal_resonnant"), "conditions": [{"condition": "minecraft:match_tool", "predicate": {"predicates": {
            "minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
        {"type": "minecraft:item", "name": lb("eclat_resonnant"), "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]}]}]}]})
    data("loot_table/blocks", "herbe_argentee", silk_or("herbe_argentee", "terre_des_limbes"))
    for b in ["dalle_de_pierre_nacree", "dalle_d_aube"]:
        data("loot_table/blocks", b, {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": lb(b), "functions": [
            {"function": "minecraft:set_count", "count": 2, "conditions": [{"condition": "minecraft:block_state_property", "block": lb(b), "properties": {"type": "double"}}]},
            {"function": "minecraft:explosion_decay"}]}]}]})
    data("loot_table/blocks", "porte_d_aube", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": lb("porte_d_aube")}],
                                                                               "conditions": [{"condition": "minecraft:block_state_property", "block": lb("porte_d_aube"), "properties": {"half": "lower"}}, {"condition": "minecraft:survives_explosion"}]}]})
    data("loot_table/blocks", "fibres_nacrees", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": lb("fibres_nacrees"),
                                                                                                                                 "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}]}]})
    data("loot_table/blocks", "herbe_de_l_aube", {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": lb("herbe_de_l_aube")}]}]})
    data("loot_table/blocks", "feuilles_d_aube", {"type": "minecraft:block", "pools": [
        {"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": lb("feuilles_d_aube"), "conditions": [{"condition": "minecraft:any_of", "terms": [
                {"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
                {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}]},
            {"type": "minecraft:item", "name": lb("pousse_d_aube"), "conditions": [{"condition": "minecraft:survives_explosion"},
                                                                                   {"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.05, 0.0625, 0.083333336, 0.1]}]}]}]},
        {"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": lb("fibres_nacrees"), "conditions": [{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.02, 0.03, 0.05, 0.1]}]}]}]})
    data("loot_table/blocks", "porte_des_limbes", {"type": "minecraft:block", "pools": []})
    # coffres
    data("loot_table/chests", "vestige", {"type": "minecraft:chest", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": lb("fragment_de_nacre"), "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}]},
        {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 4}, "entries": [
            {"type": "minecraft:item", "name": "minecraft:bread", "weight": 5, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "minecraft:torch", "weight": 5, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 6}}]},
            {"type": "minecraft:item", "name": "minecraft:paper", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "minecraft:amethyst_shard", "weight": 4, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}]},
            {"type": "minecraft:item", "name": "minecraft:compass", "weight": 2},
            {"type": "minecraft:item", "name": lb("fibres_nacrees"), "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]}]}]})
    data("loot_table/chests", "vestige_cave", {"type": "minecraft:chest", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": lb("fragment_de_nacre")}]},
        {"rolls": {"type": "minecraft:uniform", "min": 1, "max": 2}, "entries": [
            {"type": "minecraft:item", "name": "minecraft:gold_nugget", "weight": 4, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 6}}]},
            {"type": "minecraft:item", "name": "minecraft:lantern", "weight": 2},
            {"type": "minecraft:item", "name": "minecraft:book", "weight": 2}]}]})
    data("loot_table/chests", "relais", {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 3, "max": 5}, "entries": [
            {"type": "minecraft:item", "name": lb("fibres_nacrees"), "weight": 4, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 5}}]},
            {"type": "minecraft:item", "name": lb("bouillon_de_l_aube"), "weight": 3},
            {"type": "minecraft:item", "name": "minecraft:iron_ingot", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": lb("cristal_de_nacre"), "weight": 2},
            {"type": "minecraft:item", "name": "minecraft:map", "weight": 1},
            {"type": "minecraft:item", "name": lb("lanterne_nacree"), "weight": 2, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}]}]})
    def coffre(nom, entrees, rolls=(2, 4), garanti=()):
        pools = [{"rolls": {"type": "minecraft:uniform", "min": rolls[0], "max": rolls[1]}, "entries": [
            {"type": "minecraft:item", "name": n, "weight": w, **({"functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": a, "max": b}}]} if a else {})}
            for (n, w, a, b) in entrees]}]
        for n in garanti:
            pools.append({"rolls": 1, "entries": [{"type": "minecraft:item", "name": n}]})
        data("loot_table/chests", nom, {"type": "minecraft:chest", "pools": pools})

    coffre("capitainerie", [("minecraft:map", 2, 0, 0), ("minecraft:compass", 2, 0, 0), (lb("perle_de_nacre"), 3, 1, 3), ("minecraft:spyglass", 1, 0, 0), (lb("poisson_lanterne"), 3, 2, 4), ("minecraft:emerald", 2, 1, 3)])
    coffre("marche_flottant", [("minecraft:clock", 1, 0, 0), (lb("perle_de_nacre"), 3, 1, 2), ("minecraft:name_tag", 1, 0, 0), (lb("journal_de_bord"), 1, 0, 0), ("minecraft:glass_bottle", 2, 1, 3), (lb("sable_de_nacre"), 2, 4, 8)])
    coffre("navire_perdu", [(lb("perle_de_nacre"), 4, 2, 4), ("minecraft:gold_ingot", 3, 1, 3), ("minecraft:emerald", 2, 1, 2), ("minecraft:prismarine_crystals", 3, 2, 5), (lb("cristal_de_nacre"), 2, 1, 2), ("minecraft:nautilus_shell", 1, 0, 0), ("minecraft:dark_oak_planks", 2, 4, 9)], rolls=(3, 5), garanti=[lb("journal_de_bord")])
    coffre("quai", [(lb("poisson_lanterne"), 3, 1, 3), ("minecraft:oak_boat", 1, 0, 0), ("minecraft:string", 2, 2, 5), (lb("fibres_nacrees"), 2, 2, 4)])
    coffre("sylvecime", [(lb("fruit_de_geant"), 4, 2, 4), (lb("miel_de_rosee"), 3, 1, 2), ("minecraft:bone_meal", 2, 2, 4), (lb("planches_de_geant"), 2, 4, 8), ("minecraft:shears", 1, 0, 0)])
    coffre("meridiane", [(lb("dattes_des_heures"), 4, 2, 5), ("minecraft:clock", 1, 0, 0), (lb("gres_des_heures_taille"), 2, 4, 8), ("minecraft:gold_nugget", 3, 2, 6), ("minecraft:lantern", 1, 1, 2)])
    coffre("verreole", [(lb("eclat_resonnant"), 4, 1, 3), (lb("the_des_cimes"), 3, 1, 2), ("minecraft:iron_ingot", 3, 1, 3), (lb("cristal_resonnant"), 1, 0, 0), ("minecraft:glass", 2, 3, 6)])
    coffre("belvedere", [("minecraft:gold_ingot", 3, 2, 4), (lb("pierre_de_couronne_sculptee"), 2, 2, 4), (lb("lanterne_royale"), 2, 1, 2), ("minecraft:emerald", 2, 1, 3)], rolls=(2, 3), garanti=[lb("sceau_royal")])
    # Étape 7 : l'île des ruines garantit le journal de l'expédition royale ; la cabine du navire royal est garnie.
    coffre("ruines", [("minecraft:gold_ingot", 2, 1, 3), (lb("perle_de_nacre"), 3, 1, 2), (lb("pierre_de_couronne_sculptee"), 2, 1, 3), ("minecraft:emerald", 1, 1, 2)], rolls=(2, 3), garanti=[lb("journal_de_bord")])
    # Étape 8 : coffres des nouvelles Arrivées
    coffre("charrette", [("minecraft:bread", 4, 2, 5), ("minecraft:hay_block", 2, 1, 3), (lb("farine"), 3, 2, 4), ("minecraft:leather", 2, 1, 2), ("minecraft:emerald", 1, 1, 2)], rolls=(2, 3))
    coffre("puits", [("minecraft:bucket", 2, 1, 1), (lb("fragment_de_nacre"), 2, 1, 2), ("minecraft:emerald", 2, 1, 3), ("minecraft:string", 2, 2, 5)], rolls=(2, 3))
    coffre("kiosque", [("minecraft:note_block", 2, 1, 2), ("minecraft:paper", 3, 2, 6), ("minecraft:music_disc_strad", 1, 1, 1), (lb("perle_de_nacre"), 1, 1, 2)], rolls=(2, 3))
    coffre("radeau", [("minecraft:cod", 3, 2, 5), ("minecraft:spruce_planks", 2, 4, 8), ("minecraft:fishing_rod", 1, 1, 1), (lb("perle_de_nacre"), 2, 1, 3), ("minecraft:compass", 1, 1, 1)], rolls=(2, 4))
    coffre("serre_tombee", [("minecraft:wheat_seeds", 3, 3, 6), ("minecraft:carrot", 3, 2, 5), ("minecraft:potato", 3, 2, 5), ("minecraft:bone_meal", 2, 2, 4), (lb("fleur_de_rosee"), 1, 1, 2)], rolls=(2, 4))
    coffre("tente", [(lb("dattes_des_heures"), 3, 2, 5), ("minecraft:leather_boots", 1, 1, 1), ("minecraft:paper", 2, 1, 3), ("minecraft:gold_nugget", 2, 2, 5), (lb("sable_d_opale"), 2, 2, 4)], rolls=(2, 3))
    coffre("cloche", [("minecraft:copper_ingot", 3, 2, 4), (lb("eclat_resonnant"), 3, 1, 3), (lb("the_des_cimes"), 2, 1, 2), ("minecraft:amethyst_shard", 2, 1, 3)], rolls=(2, 3))
    coffre("bibliotheque", [("minecraft:book", 3, 2, 5), ("minecraft:paper", 3, 3, 8), ("minecraft:ink_sac", 2, 1, 3), (lb("perle_de_nacre"), 2, 1, 2), ("minecraft:enchanted_book", 1, 1, 1)], rolls=(2, 4))
    coffre("navire_royal", [(lb("poisson_lanterne"), 3, 2, 5), ("minecraft:bread", 3, 2, 4), (lb("perle_de_nacre"), 2, 1, 2), ("minecraft:spyglass", 1, 1, 1), (lb("fibres_nacrees"), 2, 2, 4)], rolls=(2, 4))
    coffre("haute_garde", [(lb("pain_de_la_cour"), 4, 1, 3), ("minecraft:iron_ingot", 3, 2, 4), (lb("pierre_de_couronne_taillee"), 2, 4, 8), ("minecraft:shield", 1, 0, 0), (lb("lanterne_royale"), 1, 1, 2)])
    data("loot_table/chests", "manoir", {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 4}, "entries": [
            {"type": "minecraft:item", "name": "minecraft:book", "weight": 4},
            {"type": "minecraft:item", "name": "minecraft:candle", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": lb("fibres_nacrees"), "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}]},
            {"type": "minecraft:item", "name": lb("bouillon_de_l_aube"), "weight": 2},
            {"type": "minecraft:item", "name": "minecraft:bread", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}]}]})
    data("loot_table/chests", "maison_egaree", {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 4}, "entries": [
            {"type": "minecraft:item", "name": "minecraft:bread", "weight": 5, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "minecraft:wheat", "weight": 4, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 5}}]},
            {"type": "minecraft:item", "name": "minecraft:egg", "weight": 2, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "minecraft:sugar", "weight": 2, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]},
            {"type": "minecraft:item", "name": "minecraft:book", "weight": 1}]}]})
    data("loot_table/chests", "caisse_tombee", {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 3}, "entries": [
            {"type": "minecraft:item", "name": lb("fragment_de_nacre"), "weight": 4, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]},
            {"type": "minecraft:item", "name": "minecraft:amethyst_shard", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": "minecraft:lantern", "weight": 2},
            {"type": "minecraft:item", "name": "minecraft:name_tag", "weight": 1},
            {"type": "minecraft:item", "name": "minecraft:paper", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}]},
            {"type": "minecraft:item", "name": lb("fleur_de_rosee"), "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": lb("pain_d_emile"), "weight": 2}]}]})
    data("loot_table/chests", "barque", {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 3}, "entries": [
            {"type": "minecraft:item", "name": "minecraft:cod", "weight": 4, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 4}}]},
            {"type": "minecraft:item", "name": "minecraft:fishing_rod", "weight": 2},
            {"type": "minecraft:item", "name": "minecraft:string", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 2, "max": 5}}]},
            {"type": "minecraft:item", "name": "minecraft:lantern", "weight": 2},
            {"type": "minecraft:item", "name": "minecraft:spyglass", "weight": 1}]}]})
    data("loot_table/chests", "ilot", {"type": "minecraft:chest", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": lb("journal_de_bord")}]},
        {"rolls": {"type": "minecraft:uniform", "min": 1, "max": 2}, "entries": [
            {"type": "minecraft:item", "name": lb("fragment_de_nacre"), "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]},
            {"type": "minecraft:item", "name": lb("cristal_de_nacre"), "weight": 2}]}]})
    # entités
    data("loot_table/entities", "veilleur_egare", {"type": "minecraft:entity", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": lb("fragment_de_nacre"), "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]}]},
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": lb("cristal_de_nacre"), "weight": 1, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 1}}]},
                                 {"type": "minecraft:item", "name": lb("pierre_nacree"), "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]}]}]})
    data("loot_table/entities", "cerf_lanterne", {"type": "minecraft:entity", "pools": [
        {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:leather", "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 0, "max": 2}}]}]}]})
    data("loot_table/entities", "poisson_lune", {"type": "minecraft:entity", "pools": []})
    data("loot_table/entities", "habitant", {"type": "minecraft:entity", "pools": []})


# ----------------------------------------------------------------------------- recettes
def shaped(name, pattern, key, result, count=1, group=None):
    r = {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern, "key": {k: {"item": v} for k, v in key.items()},
         "result": {"id": result, "count": count}}
    if group:
        r["group"] = group
    data("recipe", name, r)


def shapeless(name, ingredients, result, count=1):
    data("recipe", name, {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": [{"item": i} for i in ingredients],
                          "result": {"id": result, "count": count}})


def gen_recipes() -> None:
    shapeless("planches_d_aube", [lb("buche_d_aube")], lb("planches_d_aube"), 4)
    # Décor (étape 8)
    shaped("tapis_de_nacre", ["##"], {"#": lb("fibres_nacrees")}, lb("tapis_de_nacre"), 3)
    shaped("vase_de_nacre", ["# #", "###"], {"#": lb("pierre_nacree_taillee")}, lb("vase_de_nacre"), 1)
    shaped("guirlande_de_fete", ["#L#"], {"#": lb("fibres_nacrees"), "L": lb("lanterne_nacree")}, lb("guirlande_de_fete"), 2)
    shaped("escalier_d_aube", ["#  ", "## ", "###"], {"#": lb("planches_d_aube")}, lb("escalier_d_aube"), 4)
    shaped("dalle_d_aube", ["###"], {"#": lb("planches_d_aube")}, lb("dalle_d_aube"), 6)
    shaped("barriere_d_aube", ["#/#", "#/#"], {"#": lb("planches_d_aube"), "/": "minecraft:stick"}, lb("barriere_d_aube"), 3)
    shaped("porte_d_aube", ["##", "##", "##"], {"#": lb("planches_d_aube")}, lb("porte_d_aube"), 3)
    shaped("pierre_nacree_taillee", ["##", "##"], {"#": lb("pierre_nacree")}, lb("pierre_nacree_taillee"), 4)
    shaped("pierre_nacree_polie", ["##", "##"], {"#": lb("pierre_nacree_taillee")}, lb("pierre_nacree_polie"), 4)
    shaped("pierre_nacree_sculptee", ["#", "#"], {"#": lb("dalle_de_pierre_nacree")}, lb("pierre_nacree_sculptee"), 1)
    shaped("escalier_de_pierre_nacree", ["#  ", "## ", "###"], {"#": lb("pierre_nacree_taillee")}, lb("escalier_de_pierre_nacree"), 4)
    shaped("dalle_de_pierre_nacree", ["###"], {"#": lb("pierre_nacree_taillee")}, lb("dalle_de_pierre_nacree"), 6)
    shaped("muret_de_pierre_nacree", ["###", "###"], {"#": lb("pierre_nacree_taillee")}, lb("muret_de_pierre_nacree"), 6)
    shaped("boussole_nacree", [" A ", "ACA", " A "], {"A": "minecraft:amethyst_shard", "C": "minecraft:compass"}, lb("boussole_nacree"), 1)
    shapeless("bouillon_de_l_aube", ["minecraft:bowl", lb("herbe_de_l_aube"), lb("herbe_de_l_aube"), lb("fibres_nacrees")], lb("bouillon_de_l_aube"), 1)
    shaped("lanterne_nacree", ["NNN", "NCN", "NNN"], {"N": "minecraft:iron_nugget", "C": lb("cristal_de_nacre")}, lb("lanterne_nacree"), 2)
    shaped("cristal_de_nacre", ["FF", "FF"], {"F": lb("fragment_de_nacre")}, lb("cristal_de_nacre"), 1)
    shaped("corde_de_fibres", ["F", "F", "F"], {"F": lb("fibres_nacrees")}, "minecraft:string", 2)
    shapeless("farine", ["minecraft:wheat", "minecraft:wheat", "minecraft:wheat"], lb("farine"), 2)
    shapeless("pain_d_emile", [lb("farine"), lb("farine"), "minecraft:bucket"], lb("pain_d_emile"), 1)
    shapeless("ragout_du_domaine", ["minecraft:bowl", lb("herbe_de_l_aube"), "minecraft:carrot", "minecraft:cod", lb("pain_d_emile")], lb("ragout_du_domaine"), 1)
    shapeless("planches_de_geant", [lb("buche_de_geant")], lb("planches_de_geant"), 4)
    shaped("gres_des_heures", ["##", "##"], {"#": lb("sable_d_opale")}, lb("gres_des_heures"), 1)
    shaped("gres_des_heures_taille", ["##", "##"], {"#": lb("gres_des_heures")}, lb("gres_des_heures_taille"), 4)
    shaped("pierre_de_verre_taillee", ["##", "##"], {"#": lb("pierre_de_verre")}, lb("pierre_de_verre_taillee"), 4)
    shaped("pierre_de_couronne_taillee", ["##", "##"], {"#": lb("pierre_de_couronne")}, lb("pierre_de_couronne_taillee"), 4)
    shaped("pierre_de_couronne_polie", ["##", "##"], {"#": lb("pierre_de_couronne_taillee")}, lb("pierre_de_couronne_polie"), 4)
    shaped("lanterne_royale", ["NNN", "NCN", "NNN"], {"N": "minecraft:gold_nugget", "C": lb("cristal_de_nacre")}, lb("lanterne_royale"), 2)
    shaped("cadran_des_heures", ["GGG", "GCG", "GGG"], {"G": lb("gres_des_heures_taille"), "C": "minecraft:clock"}, lb("cadran_des_heures"), 1)
    shapeless("miel_de_rosee", ["minecraft:honey_bottle", lb("fleur_de_rosee"), lb("petale_rose")], lb("miel_de_rosee"), 1)
    shapeless("the_des_cimes", ["minecraft:glass_bottle", lb("herbe_de_l_aube"), lb("eclat_resonnant")], lb("the_des_cimes"), 2)
    shapeless("pain_de_la_cour", [lb("farine"), lb("farine"), lb("miel_de_rosee"), "minecraft:egg"], lb("pain_de_la_cour"), 1)
    data("recipe", "poisson_lanterne_cuit", {"type": "minecraft:smelting", "ingredient": {"item": lb("poisson_lanterne")}, "result": {"id": "minecraft:cooked_cod"}, "experience": 0.3, "cookingtime": 200})
    for b in ["pierre_nacree_taillee", "pierre_nacree_polie", "escalier_de_pierre_nacree", "dalle_de_pierre_nacree", "muret_de_pierre_nacree", "pierre_nacree_sculptee"]:
        count = 2 if b == "dalle_de_pierre_nacree" else 1
        data("recipe", "tailleur_" + b, {"type": "minecraft:stonecutting", "ingredient": {"item": lb("pierre_nacree")}, "result": {"id": lb(b), "count": count}})


# ----------------------------------------------------------------------------- progrès
def gen_advancements() -> None:
    data("advancement/les_limbes", "racine", {
        "display": {"icon": {"id": lb("fragment_de_nacre")}, "title": {"translate": "advancements.les_limbes.racine.title"},
                    "description": {"translate": "advancements.les_limbes.racine.description"}, "background": lb("textures/block/pierre_nacree.png"),
                    "show_toast": False, "announce_to_chat": False},
        "criteria": {"carnet": {"trigger": "minecraft:tick"}}})
    data("advancement/les_limbes", "boussole", {
        "parent": lb("les_limbes/racine"),
        "display": {"icon": {"id": lb("boussole_nacree")}, "title": {"translate": "advancements.les_limbes.boussole.title"},
                    "description": {"translate": "advancements.les_limbes.boussole.description"}},
        "criteria": {"boussole": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": lb("boussole_nacree")}]}}}})
    data("advancement/les_limbes", "arrivee", {
        "parent": lb("les_limbes/boussole"),
        "display": {"icon": {"id": lb("porte_des_limbes")}, "title": {"translate": "advancements.les_limbes.arrivee.title"},
                    "description": {"translate": "advancements.les_limbes.arrivee.description"}, "frame": "goal"},
        "criteria": {"arrivee": {"trigger": "minecraft:changed_dimension", "conditions": {"to": lb("limbes")}}}})
    data("advancement/les_limbes", "couronne", {
        "parent": lb("les_limbes/arrivee"),
        "display": {"icon": {"id": lb("fragment_de_couronne")}, "title": {"translate": "advancements.les_limbes.couronne.title"},
                    "description": {"translate": "advancements.les_limbes.couronne.description"}, "frame": "challenge"},
        "criteria": {"couronne": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": lb("fragment_de_couronne")}]}}}})


# ----------------------------------------------------------------------------- génération de monde
def state(name, props=None):
    s = {"Name": name}
    if props:
        s["Properties"] = props
    return s


def simple(name, props=None):
    return {"type": "minecraft:simple_state_provider", "state": state(name, props)}


def noise(name, xz, y):
    return {"type": "minecraft:noise", "noise": name, "xz_scale": xz, "y_scale": y}


def ycg(from_y, to_y, from_v, to_v):
    return {"type": "minecraft:y_clamped_gradient", "from_y": from_y, "to_y": to_y, "from_value": from_v, "to_value": to_v}


def add(a, b):
    return {"type": "minecraft:add", "argument1": a, "argument2": b}


def mul(a, b):
    return {"type": "minecraft:mul", "argument1": a, "argument2": b}


def dmin(a, b):
    return {"type": "minecraft:min", "argument1": a, "argument2": b}


def dmax(a, b):
    return {"type": "minecraft:max", "argument1": a, "argument2": b}


# Sites aplanis : doivent rester cohérents avec fr.leslimbes.world.dimension.LimbesSites (test unitaire).
SITES = [
    {"x": 0, "z": 140, "y": 66, "rayon": 70, "transition": 40},   # Clairval
    {"x": -120, "z": 150, "y": 66, "rayon": 40, "transition": 30},  # Manoir et ses annexes (projets jusqu'à 37 blocs du centre)
    {"x": 150, "z": 60, "y": 66, "rayon": 34, "transition": 30},    # Relais royal
    {"x": 0, "z": 56, "y": 66, "rayon": 34, "transition": 30},      # Pied du pont
    {"x": 70, "z": 210, "y": 66, "rayon": 16, "transition": 14},    # Site d'Arrivée 1
    {"x": -80, "z": 80, "y": 66, "rayon": 16, "transition": 14},    # Site d'Arrivée 2
    {"x": 220, "z": 150, "y": 66, "rayon": 16, "transition": 14},   # Site d'Arrivée 3
    {"x": -60, "z": 20, "y": 66, "rayon": 22, "transition": 16},    # Lac du Crépuscule
    {"x": -30, "z": 20, "y": 66, "rayon": 12, "transition": 12},    # Rive est du lac (site d'Arrivée « rive »)
    # --- Mer suspendue (étape 5) ---
    {"x": 950, "z": -750, "y": 66, "rayon": 60, "transition": 26},   # Port-Nacre (île-ville)
    {"x": 1150, "z": -950, "y": 40, "rayon": 40, "transition": 16},  # Navire perdu (fond plat)
    {"x": 1050, "z": -560, "y": 44, "rayon": 24, "transition": 14},  # Sanctuaire des bancs (sous l'eau)
    {"x": 1440, "z": -750, "y": 84, "rayon": 18, "transition": 12},  # Phare du Dessous (rebord)
    {"x": 560, "z": -360, "y": 82, "rayon": 16, "transition": 12},   # Quai des Falaises (rebord, fin de la route)
    # --- Jardins des Géants ---
    {"x": 1050, "z": 350, "y": 72, "rayon": 70, "transition": 30},   # Sylvécime
    {"x": 900, "z": 520, "y": 72, "rayon": 30, "transition": 20},    # Serre ancienne
    {"x": 1220, "z": 230, "y": 72, "rayon": 30, "transition": 20},   # Pépinière royale
    {"x": 1050, "z": 620, "y": 72, "rayon": 30, "transition": 20},   # Arbre creux
    {"x": 1280, "z": 480, "y": 72, "rayon": 40, "transition": 24},   # Clairière du géant
    # --- Désert des Heures ---
    {"x": 150, "z": 1250, "y": 66, "rayon": 70, "transition": 30},   # Méridiane
    {"x": 0, "z": 1420, "y": 66, "rayon": 30, "transition": 20},     # Observatoire des heures
    {"x": 380, "z": 1130, "y": 66, "rayon": 34, "transition": 20},   # Jardin redevenant fertile
    # --- Montagnes de Verre ---
    {"x": -1150, "z": 500, "y": 96, "rayon": 70, "transition": 34},  # Verréole (plateau)
    {"x": -1000, "z": 380, "y": 90, "rayon": 30, "transition": 24},  # Bains des sources
    {"x": -1320, "z": 620, "y": 100, "rayon": 30, "transition": 24}, # Atelier d'instruments
    {"x": -1150, "z": 655, "y": 104, "rayon": 14, "transition": 14}, # Pont qui chante : rive nord
    {"x": -1150, "z": 705, "y": 104, "rayon": 14, "transition": 14}, # Pont qui chante : rive sud
    {"x": -1150, "z": 680, "y": 60, "rayon": 9, "transition": 8},    # Pont qui chante : gorge
    # --- Couronne Immobile ---
    {"x": -610, "z": -610, "y": 82, "rayon": 70, "transition": 30},  # Haute-Garde (cité de Vaëron)
    {"x": -900, "z": -720, "y": 78, "rayon": 50, "transition": 26},  # Quartier des portes et des jardins
    {"x": -900, "z": -900, "y": 86, "rayon": 90, "transition": 40},  # Capitale et palais
    # --- Étape 6 : arènes des épreuves, campement de l'archipel ---
    {"x": 1150, "z": 300, "y": 72, "rayon": 24, "transition": 16},    # Jardin de l'épreuve (Aélis)
    {"x": -1230, "z": 500, "y": 96, "rayon": 24, "transition": 16},   # Salle des miroirs (Orsane)
    {"x": 230, "z": 1330, "y": 66, "rayon": 24, "transition": 16},    # Cour des heures (Sahir)
    {"x": 1250, "z": -650, "y": 66, "rayon": 14, "transition": 12},   # Campement de l'archipel (expédition de Kaël)
    {"x": -700, "z": -1060, "y": 86, "rayon": 20, "transition": 16},  # Site d'Arrivée du royaume 1 (belvédère)
    {"x": -1060, "z": -700, "y": 86, "rayon": 20, "transition": 16},  # Site d'Arrivée du royaume 2
    # --- Étape 7 : le règne ---
    {"x": 1030, "z": -800, "y": 56, "rayon": 24, "transition": 14},   # Chantier naval (fond plat sous la Mer, le navire royal y est bâti)
    {"x": 1300, "z": -1000, "y": 70, "rayon": 26, "transition": 18},  # Île des ruines (expédition du navire royal)
    {"x": -900, "z": -1380, "y": 86, "rayon": 30, "transition": 24},  # Lisière des brumes (frontière du nord, l'ancien roi)
    # --- Étape 8 : sites d'Arrivée des régions ---
    {"x": 900, "z": -650, "y": 66, "rayon": 16, "transition": 14},    # Îlot d'Arrivée de la Mer (radeau)
    {"x": 960, "z": 400, "y": 72, "rayon": 16, "transition": 14},     # Site d'Arrivée des Jardins (serre)
    {"x": 260, "z": 1150, "y": 66, "rayon": 16, "transition": 14},    # Site d'Arrivée du Désert (tente)
    {"x": -1060, "z": 560, "y": 96, "rayon": 16, "transition": 14},   # Site d'Arrivée des Montagnes (cloche)
    {"x": -720, "z": -760, "y": 82, "rayon": 16, "transition": 14},   # Site d'Arrivée de la Couronne (bibliothèque)
]


def _app(ident, poids, mini, maxi):
    return {"type": lb(ident), "weight": poids, "minCount": mini, "maxCount": maxi}


REMPLACEMENTS_APPARITIONS = {"minecraft:cod": lb("poisson_lanterne"), "minecraft:turtle": lb("tortue_des_lagons"), "minecraft:goat": lb("bouquetin_de_verre")}

# Créatures et montures des régions (étape 5) ajoutées aux listes d'apparition des biomes ; le groupe d'apparition
# (creature, ambient, monster, water_creature) doit correspondre au SpawnGroup du type d'entité (LimbesEspeces).
SPAWNS_REGIONAUX = {
    "lagons_inverses": {"creature": [_app("crabe_de_nacre", 8, 2, 4)], "ambient": [_app("meduse_celeste", 6, 1, 3)]},
    "recifs_celestes": {"creature": [_app("crabe_de_nacre", 8, 2, 4)], "ambient": [_app("meduse_celeste", 4, 1, 2)]},
    "archipel_des_epaves": {"creature": [_app("crabe_de_nacre", 4, 1, 3), _app("tortue_des_lagons", 2, 1, 1)], "ambient": [_app("meduse_celeste", 3, 1, 2)],
                            "monster": [_app("gardien_des_epaves", 5, 1, 2)]},
    "canopee_habitee": {"creature": [_app("escargot_des_racines", 6, 1, 3), _app("scarabee_de_seve", 3, 1, 2)], "ambient": [_app("libellule_de_rosee", 8, 2, 4)]},
    "jardins_de_rosee": {"creature": [_app("escargot_des_racines", 5, 1, 3), _app("scarabee_de_seve", 2, 1, 1), _app("geant_des_jardins", 1, 1, 1)],
                         "ambient": [_app("libellule_de_rosee", 10, 2, 5)]},
    "racines_profondes": {"creature": [_app("escargot_des_racines", 6, 1, 3)], "ambient": [_app("libellule_de_rosee", 3, 1, 2)], "monster": [_app("rodeur_des_racines", 6, 1, 2)]},
    "dunes_d_opale": {"creature": [_app("fennec_des_dunes", 8, 1, 3), _app("gazelle_d_opale", 4, 2, 4), _app("lezard_cadran", 6, 1, 3)],
                      "monster": [_app("scorpion_des_heures", 6, 1, 2), _app("sentinelle_de_sable", 3, 1, 1)]},
    "oasis_des_cadrans": {"creature": [_app("fennec_des_dunes", 6, 1, 3), _app("gazelle_d_opale", 4, 2, 4), _app("lezard_cadran", 6, 1, 3)], "monster": [_app("scorpion_des_heures", 3, 1, 1)]},
    "terrasses_anciennes": {"creature": [_app("lezard_cadran", 8, 1, 3), _app("fennec_des_dunes", 4, 1, 2)], "monster": [_app("sentinelle_de_sable", 5, 1, 2), _app("scorpion_des_heures", 4, 1, 2)]},
    "aiguilles_de_verre": {"creature": [_app("griffon_de_verre", 2, 1, 1)], "ambient": [_app("papillon_de_cristal", 6, 2, 4)], "monster": [_app("colosse_de_cristal", 2, 1, 1), _app("echo_resonnant", 3, 1, 1)]},
    "vallees_resonnantes": {"creature": [_app("griffon_de_verre", 2, 1, 1)], "ambient": [_app("papillon_de_cristal", 8, 2, 4)], "monster": [_app("echo_resonnant", 4, 1, 2)]},
    "cavernes_de_cristal": {"ambient": [_app("papillon_de_cristal", 4, 1, 3)], "monster": [_app("colosse_de_cristal", 4, 1, 1), _app("echo_resonnant", 4, 1, 2)]},
    "capitale_endormie": {"creature": [_app("cerf_royal", 3, 1, 2), _app("chat_de_palais", 5, 1, 2)], "ambient": [_app("colombe_astrale", 8, 2, 4)],
                          "monster": [_app("sentinelle_de_la_couronne", 4, 1, 1), _app("spectre_de_la_cour", 3, 1, 1)]},
    "anneaux_suspendus": {"creature": [_app("cerf_royal", 3, 1, 2), _app("chat_de_palais", 3, 1, 1)], "ambient": [_app("colombe_astrale", 6, 1, 3)],
                          "monster": [_app("sentinelle_de_la_couronne", 3, 1, 1), _app("spectre_de_la_cour", 3, 1, 1)]},
    "confins_de_la_couronne": {"creature": [_app("cerf_royal", 2, 1, 2)], "ambient": [_app("colombe_astrale", 6, 1, 3)], "monster": [_app("spectre_de_la_cour", 2, 1, 1)]},
}

# Biomes par région, dans l'ordre des sous-biomes de LimbesRegions.sousBiome (0, 1, 2).
BIOMES_PAR_REGION = {
    "prairies": ["plaines_nacrees", "vergers_suspendus", "vallee_des_lanternes"],
    "mer": ["lagons_inverses", "recifs_celestes", "archipel_des_epaves"],
    "jardins": ["canopee_habitee", "jardins_de_rosee", "racines_profondes"],
    "desert": ["dunes_d_opale", "oasis_des_cadrans", "terrasses_anciennes"],
    "montagnes": ["aiguilles_de_verre", "vallees_resonnantes", "cavernes_de_cristal"],
    "couronne": ["capitale_endormie", "anneaux_suspendus", "confins_de_la_couronne"],
}
TOUS_LES_BIOMES = [b for l in BIOMES_PAR_REGION.values() for b in l]

# Ordre canonique des features végétales (étape vegetal_decoration), partagé par tous les biomes.
ORDRE_VEGETAL = ["arbres_geants", "arbres_geants_epars", "arbres_aube_epars", "arbres_aube_verger", "grands_arbres_aube", "palmiers_des_heures",
                 "fleurs_geantes_rose", "fleurs_geantes_bleu", "coraux_nacre", "coraux_azur", "bosquet_fibres", "bosquet_fibres_dense",
                 "herbes_aube", "herbes_aube_denses", "herbes_des_cadrans", "fleurs_rosee", "lanternes_perdues", "lanternes_royales_perdues", "cristaux_de_surface"]

# Routes royales : polylignes (x, z) entre les sites, pavées selon la région de départ.
ROUTES = [
    {"nom": "route_du_relais", "materiau": "minecraft:dirt_path", "points": [[0, 140], [150, 60]]},
    {"nom": "route_du_manoir", "materiau": "minecraft:dirt_path", "points": [[-10, 140], [-110, 150]]},
    {"nom": "route_de_la_mer", "materiau": "les_limbes:pierre_nacree_taillee", "points": [[150, 60], [400, -150], [560, -360]]},
    {"nom": "route_des_jardins", "materiau": "les_limbes:planches_de_geant", "points": [[10, 140], [300, 200], [600, 260], [850, 320], [1050, 350]]},
    {"nom": "route_des_heures", "materiau": "les_limbes:gres_des_heures_taille", "points": [[0, 150], [80, 420], [120, 800], [150, 1100], [150, 1250]]},
    {"nom": "route_de_verre", "materiau": "les_limbes:pierre_de_verre_taillee", "points": [[-120, 150], [-400, 250], [-750, 380], [-1050, 470], [-1150, 500]]},
    {"nom": "route_de_la_couronne", "materiau": "les_limbes:pierre_de_couronne_taillee", "points": [[0, 56], [-250, -200], [-450, -450], [-610, -610]]},
    {"nom": "route_des_portes", "materiau": "les_limbes:pierre_de_couronne_taillee", "points": [[-610, -610], [-760, -700], [-900, -720]]},
    {"nom": "sentiers_de_sylvecime", "materiau": "les_limbes:planches_de_geant", "lampadaires": 0, "points": [[900, 520], [1050, 350], [1220, 230]]},
    {"nom": "sentier_de_l_arbre_creux", "materiau": "les_limbes:planches_de_geant", "lampadaires": 0, "points": [[1050, 350], [1050, 620], [1280, 480]]},
    {"nom": "pistes_de_meridiane", "materiau": "les_limbes:gres_des_heures_taille", "lampadaires": 0, "points": [[0, 1420], [150, 1250], [380, 1130]]},
    {"nom": "sentiers_de_verreole", "materiau": "les_limbes:pierre_de_verre_taillee", "lampadaires": 0, "points": [[-1000, 380], [-1150, 500], [-1320, 620]]},
    {"nom": "sentier_du_pont", "materiau": "les_limbes:pierre_de_verre_taillee", "lampadaires": 0, "points": [[-1150, 500], [-1150, 655]]},
]


def surface_rules() -> dict:
    """Règles de surface par région : sol et sous-sol propres à chaque biome, plafonds nacrés partout."""
    def floor(depth_add: bool, offset: int = 0):
        return {"type": "minecraft:stone_depth", "add_surface_depth": depth_add, "offset": offset, "secondary_depth_range": 0, "surface_type": "floor"}

    def above(y: int):
        return {"type": "minecraft:y_above", "anchor": {"absolute": y}, "surface_depth_multiplier": 0, "add_stone_depth": False}

    def cond(if_true, then_run):
        return {"type": "minecraft:condition", "if_true": if_true, "then_run": then_run}

    def block(b):
        return {"type": "minecraft:block", "result_state": state(lb(b))}

    def biome_is(biomes):
        return {"type": "minecraft:biome", "biome_is": [lb(b) for b in biomes]}

    def sol(biomes, dessus, dessous, profond=None, y_min=24):
        regles = [cond(floor(False), cond(above(y_min), block(dessus))), cond(floor(True), cond(above(y_min), block(dessous)))]
        if profond:
            regles.append(cond(floor(True, 6), block(profond)))
        return cond(biome_is(biomes), {"type": "minecraft:sequence", "sequence": regles})

    mer = BIOMES_PAR_REGION["mer"]
    return {"type": "minecraft:sequence", "sequence": [
        # Mer : plages de sable nacré sous 66, herbe argentée sur les îles au-dessus ; fond sableux
        cond(biome_is(mer), {"type": "minecraft:sequence", "sequence": [
            cond(floor(False), cond(above(66), block("herbe_argentee"))),
            cond(floor(True), cond(above(66), block("terre_des_limbes"))),
            cond(floor(True, 2), block("sable_de_nacre"))]}),
        sol(BIOMES_PAR_REGION["jardins"], "herbe_des_geants", "terre_des_limbes"),
        cond(biome_is(["terrasses_anciennes"]), {"type": "minecraft:sequence", "sequence": [
            cond(floor(False), block("pierre_des_heures")), cond(floor(True, 3), block("gres_des_heures"))]}),
        cond(biome_is(["dunes_d_opale", "oasis_des_cadrans"]), {"type": "minecraft:sequence", "sequence": [
            cond(floor(True, 3), block("sable_d_opale")), cond(floor(True, 7), block("gres_des_heures"))]}),
        cond(biome_is(BIOMES_PAR_REGION["montagnes"]), {"type": "minecraft:sequence", "sequence": [
            cond(floor(False), cond(above(100), block("verre_de_montagne"))),
            cond(floor(True, 8), block("pierre_de_verre"))]}),
        cond(biome_is(BIOMES_PAR_REGION["couronne"]), {"type": "minecraft:sequence", "sequence": [
            cond(floor(False), cond(above(24), block("herbe_argentee"))),
            cond(floor(True, 1), block("terre_des_limbes")),
            cond(floor(True, 6), block("pierre_de_couronne"))]}),
        # Prairies (et défaut)
        cond(floor(False), cond(above(24), block("herbe_argentee"))),
        cond(floor(True), cond(above(24), block("terre_des_limbes"))),
        # Plafonds (surplombs, grottes) : pierre nacrée brute, jamais un bloc de construction (zones d'Arrivée).
        cond({"type": "minecraft:stone_depth", "add_surface_depth": False, "offset": 0, "secondary_depth_range": 0, "surface_type": "ceiling"}, block("pierre_nacree"))]}


def gen_worldgen() -> None:
    # Bruits
    data("worldgen/noise", "collines", {"firstOctave": -7, "amplitudes": [1.0, 0.7, 0.35, 0.15]})
    data("worldgen/noise", "iles", {"firstOctave": -6, "amplitudes": [1.0, 0.5]})
    data("worldgen/noise", "dessous", {"firstOctave": -5, "amplitudes": [1.0, 0.6]})
    # Fonctions de densité
    # Relief régional (les_limbes:regions, Java) : hauteur de surface par région à partir des bruits, puis
    # plateaux des sites fixes (les_limbes:aplanir). Dans les Prairies, le résultat est identique à l'ancien
    # terme « ycg(32, 96) + 0,42 × collines » (test de non-régression des captures).
    shifted = lambda n: {"type": "minecraft:shifted_noise", "noise": n, "shift_x": "minecraft:shift_x", "shift_y": 0.0, "shift_z": "minecraft:shift_z", "xz_scale": 0.25, "y_scale": 0.0}
    bruit2d = lambda n: {"type": "minecraft:cache_2d", "argument": noise(lb(n), 1.0, 0.0)}
    regions = {"type": lb("regions"), "collines": bruit2d("collines"), "iles": bruit2d("iles"),
               "temperature": {"type": "minecraft:cache_2d", "argument": shifted("minecraft:temperature")},
               "humidite": {"type": "minecraft:cache_2d", "argument": shifted("minecraft:vegetation")}}
    sol_haut = {"type": lb("aplanir"), "pente": 0.03125, "sites": SITES, "argument": regions}
    sol_bas = add(ycg(10, 34, -1.0, 1.0), mul(noise(lb("dessous"), 1.0, 1.0), 0.5))
    bande = mul(ycg(104, 118, 0.0, 1.0), ycg(140, 164, 1.0, 0.0))
    # Hors de la bande d'altitude des îles, le terme vaut -0,6 (franchement négatif) : il ne doit jamais
    # relever la surface du sol par interpolation entre coins de cellule (voir docs/DEVELOPPEMENT.md).
    iles = add(mul(bande, add(noise(lb("iles"), 0.8, 1.6), -0.52)), mul(add(bande, -1.0), 0.6))
    terrain = dmax(dmin(sol_haut, sol_bas), iles)
    data("worldgen/density_function", "terrain", terrain)
    data("worldgen/density_function", "densite_finale", {"type": "minecraft:squeeze", "argument": mul(0.64, {"type": "minecraft:interpolated", "argument": {"type": "minecraft:blend_density", "argument": lb("terrain")}})})
    data("worldgen/noise_settings", "limbes", {
        "sea_level": -64, "disable_mob_generation": False, "aquifers_enabled": False, "ore_veins_enabled": False, "legacy_random_source": False,
        "default_block": state(lb("pierre_nacree")), "default_fluid": state("minecraft:water", {"level": "0"}),
        "noise": {"min_y": 0, "height": 256, "size_horizontal": 2, "size_vertical": 1},
        "noise_router": {
            "barrier": 0.0, "fluid_level_floodedness": 0.0, "fluid_level_spread": 0.0, "lava": 0.0,
            "temperature": shifted("minecraft:temperature"), "vegetation": shifted("minecraft:vegetation"),
            "continents": 0.0, "erosion": 0.0, "depth": 0.0, "ridges": 0.0,
            "initial_density_without_jaggedness": lb("terrain"), "final_density": lb("densite_finale"),
            "vein_toggle": 0.0, "vein_ridged": 0.0, "vein_gap": 0.0},
        "spawn_target": [],
        "surface_rule": surface_rules()})
    # Type de dimension et dimension
    data("dimension_type", "limbes", {
        "ultrawarm": False, "natural": True, "coordinate_scale": 1.0, "has_skylight": True, "has_ceiling": False, "ambient_light": 0.1,
        "piglin_safe": False, "bed_works": True, "respawn_anchor_works": False, "has_raids": False,
        "logical_height": 256, "min_y": 0, "height": 256, "infiniburn": "#minecraft:infiniburn_overworld", "effects": lb("limbes"),
        "monster_spawn_light_level": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 7}, "monster_spawn_block_light_limit": 0})
    data("dimension", "limbes", {"type": lb("limbes"), "generator": {"type": "minecraft:noise", "settings": lb("limbes"), "biome_source": {
        "type": lb("regions"), "biomes": {region: [lb(b) for b in biomes] for region, biomes in BIOMES_PAR_REGION.items()}}}})
    # Features configurées
    feuilles = simple(lb("feuilles_d_aube"), {"distance": "7", "persistent": "false", "waterlogged": "false"})
    data("worldgen/configured_feature", "arbre_d_aube", {"type": "minecraft:tree", "config": {
        "trunk_provider": simple(lb("buche_d_aube"), {"axis": "y"}), "foliage_provider": feuilles, "dirt_provider": simple(lb("terre_des_limbes")),
        "trunk_placer": {"type": "minecraft:forking_trunk_placer", "base_height": 5, "height_rand_a": 2, "height_rand_b": 1},
        "foliage_placer": {"type": "minecraft:blob_foliage_placer", "radius": 2, "offset": 0, "height": 3},
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
        "force_dirt": False, "ignore_vines": True, "decorators": []}})
    data("worldgen/configured_feature", "grand_arbre_d_aube", {"type": "minecraft:tree", "config": {
        "trunk_provider": simple(lb("buche_d_aube"), {"axis": "y"}), "foliage_provider": feuilles, "dirt_provider": simple(lb("terre_des_limbes")),
        "trunk_placer": {"type": "minecraft:fancy_trunk_placer", "base_height": 6, "height_rand_a": 4, "height_rand_b": 0},
        "foliage_placer": {"type": "minecraft:fancy_foliage_placer", "radius": 3, "offset": 4, "height": 4},
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 0, "lower_size": 0, "upper_size": 0, "min_clipped_height": 4},
        "force_dirt": False, "ignore_vines": True, "decorators": []}})

    def patch(name, block, tries, spread=7, props=None):
        data("worldgen/configured_feature", name, {"type": "minecraft:random_patch", "config": {"tries": tries, "xz_spread": spread, "y_spread": 3, "feature": {
            "feature": {"type": "minecraft:simple_block", "config": {"to_place": simple(lb(block), props)}},
            "placement": [{"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}}]}}})

    patch("bosquet_fibres", "fibres_nacrees", 48)
    patch("herbes_aube", "herbe_de_l_aube", 20, 5)
    patch("fleurs_rosee", "fleur_de_rosee", 32)
    patch("lanternes_perdues", "lanterne_nacree", 3, 6, {"hanging": "false", "waterlogged": "false"})
    patch("lanternes_royales_perdues", "lanterne_royale", 2, 6, {"hanging": "false", "waterlogged": "false"})
    patch("coraux_nacre", "corail_de_nacre", 24, 6)
    patch("coraux_azur", "corail_azur", 24, 6)
    patch("cristaux_de_surface", "cristal_resonnant", 6, 5)
    patch("herbes_des_cadrans", "herbe_de_l_aube", 10, 5)
    data("worldgen/configured_feature", "lac", {"type": lb("lac"), "config": {}})
    data("worldgen/configured_feature", "mer", {"type": lb("mer"), "config": {}})
    data("worldgen/configured_feature", "routes", {"type": lb("routes"), "config": {"routes": [dict({"largeur": 3, "lampadaires": 24}, **r) for r in ROUTES]}})
    # Arbres géants (tronc 2x2, 28 à 36 blocs), fleurs monumentales (tige de bois de géant, corolle de pétales)
    feuilles_geant = simple(lb("feuilles_de_geant"), {"distance": "7", "persistent": "false", "waterlogged": "false"})
    data("worldgen/configured_feature", "arbre_geant", {"type": "minecraft:tree", "config": {
        "trunk_provider": simple(lb("buche_de_geant"), {"axis": "y"}), "foliage_provider": feuilles_geant, "dirt_provider": simple(lb("terre_des_limbes")),
        "trunk_placer": {"type": "minecraft:giant_trunk_placer", "base_height": 26, "height_rand_a": 8, "height_rand_b": 2},
        "foliage_placer": {"type": "minecraft:mega_pine_foliage_placer", "radius": 3, "offset": 2, "crown_height": {"type": "minecraft:uniform", "min_inclusive": 10, "max_inclusive": 16}},
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 1, "upper_size": 2},
        "force_dirt": False, "ignore_vines": True, "decorators": []}})
    for couleur in ("rose", "bleu"):
        data("worldgen/configured_feature", "fleur_geante_" + couleur, {"type": "minecraft:tree", "config": {
            "trunk_provider": simple(lb("buche_de_geant"), {"axis": "y"}), "foliage_provider": simple(lb("petale_" + couleur)), "dirt_provider": simple(lb("terre_des_limbes")),
            "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": 6, "height_rand_a": 3, "height_rand_b": 0},
            "foliage_placer": {"type": "minecraft:blob_foliage_placer", "radius": 2, "offset": 0, "height": 2},
            "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
            "force_dirt": False, "ignore_vines": True, "decorators": []}})
    data("worldgen/configured_feature", "palmier_des_heures", {"type": "minecraft:tree", "config": {
        "trunk_provider": simple("minecraft:jungle_log", {"axis": "y"}), "foliage_provider": simple("minecraft:jungle_leaves", {"distance": "7", "persistent": "false", "waterlogged": "false"}),
        "dirt_provider": simple(lb("sable_d_opale")),
        "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": 6, "height_rand_a": 3, "height_rand_b": 0},
        "foliage_placer": {"type": "minecraft:acacia_foliage_placer", "radius": 2, "offset": 0},
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
        "force_dirt": False, "ignore_vines": True, "decorators": []}})
    data("worldgen/configured_feature", "cristaux_resonnants", {"type": "minecraft:ore", "config": {"discard_chance_on_air_exposure": 0.0, "size": 5, "targets": [
        {"target": {"predicate_type": "minecraft:block_match", "block": lb("pierre_de_verre")}, "state": state(lb("cristal_resonnant"))},
        {"target": {"predicate_type": "minecraft:block_match", "block": lb("pierre_nacree")}, "state": state(lb("cristal_de_nacre"))}]}})
    data("worldgen/configured_feature", "cristaux", {"type": "minecraft:ore", "config": {"discard_chance_on_air_exposure": 0.0, "size": 4, "targets": [
        {"target": {"predicate_type": "minecraft:block_match", "block": lb("pierre_nacree")}, "state": state(lb("cristal_de_nacre"))}]}})

    def placed(name, feature, placement):
        data("worldgen/placed_feature", name, {"feature": lb(feature), "placement": placement})

    arbre_filtre = {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive", "state": state(lb("pousse_d_aube"), {"stage": "0"})}}
    heightmap = {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING"}
    placed("arbres_aube_epars", "arbre_d_aube", [{"type": "minecraft:count", "count": 1}, {"type": "minecraft:rarity_filter", "chance": 3}, {"type": "minecraft:in_square"},
                                                 {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0}, {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
                                                 {"type": "minecraft:biome"}, arbre_filtre])
    placed("arbres_aube_verger", "arbre_d_aube", [{"type": "minecraft:count", "count": 5}, {"type": "minecraft:in_square"}, {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
                                                  {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"}, {"type": "minecraft:biome"}, arbre_filtre])
    placed("grands_arbres_aube", "grand_arbre_d_aube", [{"type": "minecraft:count", "count": 1}, {"type": "minecraft:rarity_filter", "chance": 6}, {"type": "minecraft:in_square"},
                                                       {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0}, {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
                                                       {"type": "minecraft:biome"}, arbre_filtre])
    placed("bosquet_fibres", "bosquet_fibres", [{"type": "minecraft:count", "count": 4}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    placed("bosquet_fibres_dense", "bosquet_fibres", [{"type": "minecraft:count", "count": 12}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    placed("herbes_aube", "herbes_aube", [{"type": "minecraft:rarity_filter", "chance": 3}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    placed("herbes_aube_denses", "herbes_aube", [{"type": "minecraft:count", "count": 2}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    placed("fleurs_rosee", "fleurs_rosee", [{"type": "minecraft:rarity_filter", "chance": 4}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    placed("lanternes_perdues", "lanternes_perdues", [{"type": "minecraft:rarity_filter", "chance": 2}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    placed("lacs", "lac", [{"type": "minecraft:rarity_filter", "chance": 9}, {"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}])
    placed("cristaux", "cristaux", [{"type": "minecraft:count", "count": 6}, {"type": "minecraft:in_square"},
                                    {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 20}, "max_inclusive": {"absolute": 60}}}, {"type": "minecraft:biome"}])
    placed("mer", "mer", [{"type": "minecraft:biome"}])
    placed("routes", "routes", [])
    fond_marin = {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR_WG"}
    placed("coraux_nacre", "coraux_nacre", [{"type": "minecraft:count", "count": 2}, {"type": "minecraft:in_square"}, fond_marin, {"type": "minecraft:biome"}])
    placed("coraux_azur", "coraux_azur", [{"type": "minecraft:count", "count": 2}, {"type": "minecraft:in_square"}, fond_marin, {"type": "minecraft:biome"}])
    placed("arbres_geants", "arbre_geant", [{"type": "minecraft:count", "count": 1}, {"type": "minecraft:rarity_filter", "chance": 2}, {"type": "minecraft:in_square"},
                                            {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0}, {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
                                            {"type": "minecraft:biome"}])
    placed("arbres_geants_epars", "arbre_geant", [{"type": "minecraft:rarity_filter", "chance": 6}, {"type": "minecraft:in_square"},
                                                  {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0}, {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
                                                  {"type": "minecraft:biome"}])
    for couleur in ("rose", "bleu"):
        placed("fleurs_geantes_" + couleur, "fleur_geante_" + couleur, [{"type": "minecraft:rarity_filter", "chance": 3}, {"type": "minecraft:in_square"},
                                                                         {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0}, {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
                                                                         {"type": "minecraft:biome"}])
    placed("palmiers_des_heures", "palmier_des_heures", [{"type": "minecraft:count", "count": 1}, {"type": "minecraft:rarity_filter", "chance": 2}, {"type": "minecraft:in_square"},
                                                         {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0}, {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"},
                                                         {"type": "minecraft:biome"}])
    placed("cristaux_resonnants", "cristaux_resonnants", [{"type": "minecraft:count", "count": 10}, {"type": "minecraft:in_square"},
                                                          {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 20}, "max_inclusive": {"absolute": 120}}}, {"type": "minecraft:biome"}])
    placed("cristaux_de_surface", "cristaux_de_surface", [{"type": "minecraft:rarity_filter", "chance": 3}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    placed("lanternes_royales_perdues", "lanternes_royales_perdues", [{"type": "minecraft:rarity_filter", "chance": 3}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    placed("herbes_des_cadrans", "herbes_des_cadrans", [{"type": "minecraft:count", "count": 2}, {"type": "minecraft:in_square"}, heightmap, {"type": "minecraft:biome"}])
    # Biomes
    def remplacer_apparitions(liste):
        """Les animaux vanilla provisoires cèdent la place aux espèces des Limbes."""
        return [dict(e, type=REMPLACEMENTS_APPARITIONS[e["type"]]) if e["type"] in REMPLACEMENTS_APPARITIONS else e for e in liste]

    def biome(name, temperature, sky, fog, water, water_fog, grass, foliage, features_vegetal, creatures, ambient, monsters, music,
              lacs=True, cristaux=True, mer=False, carvers=False, water_creatures=None, extra_underground=None):
        for region_musique, noms in BIOMES_PAR_REGION.items():
            if name in noms:
                music = lb("musique." + region_musique)
        features = [[] for _ in range(11)]
        features[1] = ([lb("lacs")] if lacs else []) + ([lb("mer")] if mer else [])
        features[2] = [lb("routes")]
        features[6] = ([lb("cristaux")] if cristaux else []) + (extra_underground or [])
        # Ordre global des features végétales : Minecraft exige un ordre cohérent entre biomes d'une même étape
        # (sinon « Feature order cycle found » à la génération).
        features[9] = sorted(features_vegetal, key=lambda f: ORDRE_VEGETAL.index(f.split(":")[1]) if f.split(":")[1] in ORDRE_VEGETAL else 99)
        creatures = remplacer_apparitions(creatures) + SPAWNS_REGIONAUX.get(name, {}).get("creature", [])
        ambient = remplacer_apparitions(ambient) + SPAWNS_REGIONAUX.get(name, {}).get("ambient", [])
        monsters = remplacer_apparitions(monsters) + SPAWNS_REGIONAUX.get(name, {}).get("monster", [])
        water_creatures = remplacer_apparitions(water_creatures or []) + SPAWNS_REGIONAUX.get(name, {}).get("water_creature", [])
        data("worldgen/biome", name, {
            "temperature": temperature, "downfall": 0.4, "has_precipitation": False,
            "effects": {"sky_color": sky, "fog_color": fog, "water_color": water, "water_fog_color": water_fog, "grass_color": grass, "foliage_color": foliage,
                        "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0},
                        "music": {"sound": music, "min_delay": 12000, "max_delay": 24000, "replace_current_music": False}},
            "spawners": {"creature": creatures, "ambient": ambient, "monster": monsters, "water_creature": water_creatures or [], "water_ambient": [], "underground_water_creature": [], "axolotls": [], "misc": []},
            "spawn_costs": {}, "carvers": ({"air": ["minecraft:cave", "minecraft:cave_extra_underground"]} if carvers else {}), "features": features})

    cerf = {"type": lb("cerf_lanterne"), "weight": 10, "minCount": 2, "maxCount": 4}
    mouton = {"type": "minecraft:sheep", "weight": 4, "minCount": 2, "maxCount": 3}
    lapin = {"type": "minecraft:rabbit", "weight": 3, "minCount": 2, "maxCount": 3}
    poisson = {"type": lb("poisson_lune"), "weight": 12, "minCount": 3, "maxCount": 6}
    veilleur = {"type": lb("veilleur_egare"), "weight": 3, "minCount": 1, "maxCount": 1}
    raie = {"type": lb("raie_celeste"), "weight": 4, "minCount": 1, "maxCount": 2}
    biome("plaines_nacrees", 0.7, 0x9FC4E8, 0xDDE3F0, 0x6EC4E8, 0x9AD8F0, 0xB4C6AC, 0xC9B7A2,
          [lb("arbres_aube_epars"), lb("grands_arbres_aube"), lb("bosquet_fibres"), lb("herbes_aube"), lb("fleurs_rosee")],
          [cerf, mouton, lapin, raie], [poisson], [veilleur], "minecraft:music.overworld.meadow")
    biome("vergers_suspendus", 0.8, 0xA8C8EC, 0xE6E0EA, 0x6EC4E8, 0x9AD8F0, 0xC2CDA8, 0xD9B59A,
          [lb("arbres_aube_verger"), lb("bosquet_fibres"), lb("herbes_aube_denses"), lb("fleurs_rosee")],
          [cerf, mouton, lapin], [poisson], [], "minecraft:music.overworld.cherry_grove")
    biome("vallee_des_lanternes", 0.6, 0x8FB8E6, 0xCFD9EE, 0x6EC4E8, 0x9AD8F0, 0xA9C0B4, 0xC9B7A2,
          [lb("arbres_aube_epars"), lb("bosquet_fibres_dense"), lb("lanternes_perdues"), lb("fleurs_rosee")],
          [{"type": lb("cerf_lanterne"), "weight": 16, "minCount": 2, "maxCount": 5}, raie], [{"type": lb("poisson_lune"), "weight": 16, "minCount": 4, "maxCount": 8}], [veilleur],
          "minecraft:music.overworld.forest")
    # --- Régions (étape 5) ---
    poisson_lanterne = {"type": "minecraft:cod", "weight": 8, "minCount": 3, "maxCount": 6}
    tortue = {"type": "minecraft:turtle", "weight": 2, "minCount": 1, "maxCount": 2}
    chevre = {"type": "minecraft:goat", "weight": 4, "minCount": 1, "maxCount": 3}
    # Mer suspendue : eau claire, îles herbeuses, coraux lumineux sous l'eau
    biome("lagons_inverses", 0.75, 0x8FD0F0, 0xD6ECF8, 0x3C8FD6, 0x2C6FB0, 0xA8CFB4, 0xB8C9A2,
          [lb("coraux_azur"), lb("bosquet_fibres"), lb("fleurs_rosee")], [raie, tortue], [poisson], [], "minecraft:music.overworld.lush_caves",
          lacs=False, mer=True, water_creatures=[poisson_lanterne])
    biome("recifs_celestes", 0.8, 0x9FD8F2, 0xDDF0FA, 0x4FA8E0, 0x3680C0, 0xB4D6B0, 0xC2CDA8,
          [lb("coraux_nacre"), lb("coraux_azur"), lb("arbres_aube_epars"), lb("bosquet_fibres"), lb("herbes_aube")], [raie, tortue, lapin], [poisson], [], "minecraft:music.overworld.lush_caves",
          lacs=False, mer=True, water_creatures=[poisson_lanterne, {"type": "minecraft:squid", "weight": 3, "minCount": 1, "maxCount": 2}])
    biome("archipel_des_epaves", 0.7, 0x86B8DC, 0xC8DAE8, 0x2F78B8, 0x1F5890, 0x9FC2A8, 0xB4C6AC,
          [lb("coraux_nacre"), lb("arbres_aube_epars"), lb("bosquet_fibres_dense"), lb("lanternes_perdues")], [raie], [poisson], [veilleur], "minecraft:music.overworld.deep_dark",
          lacs=False, mer=True, water_creatures=[poisson_lanterne])
    # Jardins des Géants : arbres immenses, fleurs monumentales, herbe profonde
    biome("canopee_habitee", 0.8, 0x9FD0C8, 0xD6EEDC, 0x5AAEC8, 0x3F86A0, 0x6FA85A, 0x4E8F3E,
          [lb("arbres_geants"), lb("fleurs_geantes_rose"), lb("herbes_aube_denses"), lb("fleurs_rosee")], [cerf, mouton, lapin], [poisson], [], "minecraft:music.overworld.jungle")
    biome("jardins_de_rosee", 0.85, 0xB4DCD0, 0xE2F2E8, 0x5AAEC8, 0x3F86A0, 0x8FBF6E, 0x7FBF5E,
          [lb("fleurs_geantes_rose"), lb("fleurs_geantes_bleu"), lb("arbres_geants_epars"), lb("herbes_aube_denses"), lb("fleurs_rosee"), lb("bosquet_fibres")], [cerf, mouton, lapin, {"type": "minecraft:bee", "weight": 4, "minCount": 1, "maxCount": 3}], [poisson], [], "minecraft:music.overworld.flower_forest")
    biome("racines_profondes", 0.7, 0x86B0A8, 0xBFD6CC, 0x4E9AB4, 0x356E88, 0x5A8A4E, 0x3F7A3A,
          [lb("arbres_geants"), lb("bosquet_fibres_dense"), lb("lanternes_perdues")], [cerf], [poisson], [veilleur], "minecraft:music.overworld.old_growth_taiga")
    # Désert des Heures : dunes claires, oasis, terrasses
    biome("dunes_d_opale", 1.6, 0xC8E0F0, 0xF4EBD6, 0x6EC4E8, 0x9AD8F0, 0xC9C29A, 0xC9B07E,
          [lb("herbes_des_cadrans")], [lapin, {"type": "minecraft:camel", "weight": 2, "minCount": 1, "maxCount": 2}], [], [veilleur], "minecraft:music.overworld.badlands",
          lacs=False)
    biome("oasis_des_cadrans", 1.2, 0xBEE0F0, 0xEEEAD8, 0x3C9AD6, 0x2C6FB0, 0xA8C77E, 0xA6B86E,
          [lb("palmiers_des_heures"), lb("herbes_des_cadrans"), lb("herbes_aube"), lb("fleurs_rosee")], [lapin, mouton, {"type": "minecraft:camel", "weight": 2, "minCount": 1, "maxCount": 2}], [poisson], [], "minecraft:music.overworld.desert")
    biome("terrasses_anciennes", 1.3, 0xC2D8EC, 0xEADFCC, 0x6EC4E8, 0x9AD8F0, 0xB8AE86, 0xB49A6E,
          [lb("herbes_des_cadrans"), lb("lanternes_perdues")], [lapin], [], [veilleur], "minecraft:music.overworld.badlands", lacs=False)
    # Montagnes de Verre : sommets translucides, vallées, cavernes de cristal
    biome("aiguilles_de_verre", 0.2, 0xB4D8F0, 0xE6F2FA, 0x7FC8E8, 0x4A92B8, 0xA8C4B8, 0xB8C9A2,
          [lb("cristaux_de_surface")], [chevre], [poisson], [], "minecraft:music.overworld.frozen_peaks", lacs=False, cristaux=False, extra_underground=[lb("cristaux_resonnants")])
    biome("vallees_resonnantes", 0.4, 0xA8CCE8, 0xD8E6F2, 0x6EC4E8, 0x9AD8F0, 0x9FBFA8, 0xB4C6AC,
          [lb("arbres_aube_epars"), lb("cristaux_de_surface"), lb("herbes_aube"), lb("bosquet_fibres")], [chevre, cerf, mouton], [poisson], [veilleur], "minecraft:music.overworld.grove",
          extra_underground=[lb("cristaux_resonnants")])
    biome("cavernes_de_cristal", 0.3, 0x9FC0DC, 0xCCDCEA, 0x6EC4E8, 0x9AD8F0, 0x9FB8B0, 0xA9C0B4,
          [lb("cristaux_de_surface"), lb("lanternes_perdues")], [chevre], [poisson], [veilleur], "minecraft:music.overworld.dripstone_caves",
          lacs=False, carvers=True, extra_underground=[lb("cristaux_resonnants")])
    # Couronne Immobile : terrasses suspendues, pierre blanche, lanternes royales éteintes
    biome("capitale_endormie", 0.6, 0xC8D4F0, 0xE6E8F4, 0x6EC4E8, 0x9AD8F0, 0xB8C6B4, 0xC9B7A2,
          [lb("lanternes_royales_perdues"), lb("herbes_aube"), lb("fleurs_rosee")], [], [poisson], [veilleur], "minecraft:music.overworld.stony_peaks", lacs=False)
    biome("anneaux_suspendus", 0.6, 0xB8CCF0, 0xDEE4F2, 0x6EC4E8, 0x9AD8F0, 0xB4C6AC, 0xC9B7A2,
          [lb("arbres_aube_epars"), lb("lanternes_royales_perdues"), lb("herbes_aube"), lb("fleurs_rosee"), lb("bosquet_fibres")], [cerf, mouton], [poisson], [veilleur], "minecraft:music.overworld.meadow")
    biome("confins_de_la_couronne", 0.6, 0xA8BCE0, 0xD2DAEC, 0x6EC4E8, 0x9AD8F0, 0xA9C0B4, 0xC9B7A2,
          [lb("arbres_aube_epars"), lb("grands_arbres_aube"), lb("bosquet_fibres"), lb("herbes_aube")], [cerf, mouton, lapin], [poisson], [veilleur], "minecraft:music.overworld.forest")
    # Vestiges dans l'Overworld (structure composée ; le gabarit est produit par structgen)
    data("worldgen/structure", "vestige", {"type": lb("composee"), "biomes": "#" + lb("a_vestiges"), "step": "surface_structures", "terrain_adaptation": "beard_thin",
                                           "spawn_overrides": {}, "ancre": [8, 0, 8], "heightmap": "WORLD_SURFACE_WG",
                                           "pieces": [{"gabarit": lb("vestige/vestige"), "decalage": [-8, -5, -8]}]})
    data("worldgen/structure_set", "vestiges", {"placement": {"type": "minecraft:random_spread", "salt": 71309, "separation": 8, "spacing": 22},
                                                "structures": [{"structure": lb("vestige"), "weight": 1}]})


def gen_lang_copy() -> None:
    src = os.path.join(ASSETS, "lang", "fr_fr.json")
    dst = os.path.join(ASSETS, "lang", "en_us.json")
    if os.path.exists(src):
        shutil.copyfile(src, dst)


def main() -> None:
    gen_blocks()
    gen_items()
    gen_particles_sounds()
    gen_tags()
    gen_loot()
    gen_recipes()
    gen_advancements()
    gen_worldgen()
    gen_lang_copy()
    print(f"{written} fichiers JSON générés.")


if __name__ == "__main__":
    main()
