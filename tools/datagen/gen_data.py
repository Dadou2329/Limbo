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
CUBE_ALL = ["pierre_nacree", "pierre_nacree_taillee", "pierre_nacree_polie", "pierre_nacree_sculptee",
            "planches_d_aube", "terre_des_limbes", "cristal_de_nacre", "feuilles_d_aube"]
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


# ----------------------------------------------------------------------------- objets
ITEMS_GENERATED = ["fragment_de_nacre", "boussole_nacree", "fibres_nacrees", "herbe_de_l_aube", "bouillon_de_l_aube",
                   "fragment_de_couronne", "cle_du_manoir", "oeuf_habitant", "oeuf_poisson_lune", "oeuf_cerf_lanterne", "oeuf_veilleur_egare"]


def gen_items() -> None:
    for it in ITEMS_GENERATED:
        asset("models/item", it, {"parent": mc("item/generated"), "textures": {"layer0": lb("item/" + it)}})
    asset("models/item", "epee_du_souverain", {"parent": mc("item/handheld"), "textures": {"layer0": lb("item/epee_du_souverain")}})


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
}


def gen_particles_sounds() -> None:
    for p in ["etoile_royale", "eclat_de_nacre", "lueur", "brume"]:
        asset("particles", p, {"textures": [lb(p)]})
    sounds = {}
    for event, (path, subtitle) in SOUNDS.items():
        sounds[event] = {"subtitle": subtitle, "sounds": [lb(path)]}
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
                             "escalier_de_pierre_nacree", "dalle_de_pierre_nacree", "muret_de_pierre_nacree", "cristal_de_nacre", "lanterne_nacree"]]
    bois = [lb(x) for x in ["buche_d_aube", "planches_d_aube", "escalier_d_aube", "dalle_d_aube", "barriere_d_aube", "porte_d_aube"]]
    tag("block", "../../minecraft/tags/block/mineable/pickaxe", pierre)
    tag("block", "../../minecraft/tags/block/mineable/axe", bois)
    tag("block", "../../minecraft/tags/block/mineable/shovel", [lb("terre_des_limbes"), lb("herbe_argentee")])
    tag("block", "../../minecraft/tags/block/mineable/hoe", [lb("feuilles_d_aube")])
    tag("block", "../../minecraft/tags/block/dirt", [lb("terre_des_limbes"), lb("herbe_argentee")])
    tag("block", "../../minecraft/tags/block/animals_spawnable_on", [lb("herbe_argentee")])
    tag("block", "../../minecraft/tags/block/logs", [lb("buche_d_aube")])
    tag("block", "../../minecraft/tags/block/logs_that_burn", [lb("buche_d_aube")])
    tag("block", "../../minecraft/tags/block/planks", [lb("planches_d_aube")])
    tag("block", "../../minecraft/tags/block/leaves", [lb("feuilles_d_aube")])
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
    tag("worldgen/biome", "limbes", [lb("plaines_nacrees"), lb("vergers_suspendus"), lb("vallee_des_lanternes")])
    tag("worldgen/biome", "a_vestiges", [mc(x) for x in ["plains", "sunflower_plains", "forest", "flower_forest", "birch_forest", "meadow",
                                                         "savanna", "taiga", "cherry_grove", "dark_forest", "old_growth_birch_forest", "snowy_plains", "windswept_hills"]])
    tag("worldgen/structure", "vestiges", [lb("vestige")])
    tag("entity_type", "../../minecraft/tags/entity_type/fall_damage_immune", [lb("poisson_lune")])


# ----------------------------------------------------------------------------- butin
def self_drop(block: str):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:item", "name": lb(block)}],
                                                 "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def silk_or(block: str, alt: str):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": lb(block), "conditions": [{"condition": "minecraft:match_tool", "predicate": {"predicates": {
            "minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
        {"type": "minecraft:item", "name": lb(alt), "conditions": [{"condition": "minecraft:survives_explosion"}]}]}]}]}


def gen_loot() -> None:
    for b in ["pierre_nacree", "pierre_nacree_taillee", "pierre_nacree_polie", "pierre_nacree_sculptee", "escalier_de_pierre_nacree",
              "muret_de_pierre_nacree", "cristal_de_nacre", "terre_des_limbes", "buche_d_aube", "planches_d_aube", "escalier_d_aube",
              "barriere_d_aube", "pousse_d_aube", "fleur_de_rosee", "lanterne_nacree"]:
        data("loot_table/blocks", b, self_drop(b))
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
    data("loot_table/chests", "manoir", {"type": "minecraft:chest", "pools": [
        {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 4}, "entries": [
            {"type": "minecraft:item", "name": "minecraft:book", "weight": 4},
            {"type": "minecraft:item", "name": "minecraft:candle", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": lb("fibres_nacrees"), "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}]},
            {"type": "minecraft:item", "name": lb("bouillon_de_l_aube"), "weight": 2},
            {"type": "minecraft:item", "name": "minecraft:bread", "weight": 3, "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}]}]}]})
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
    {"x": -120, "z": 150, "y": 66, "rayon": 34, "transition": 30},  # Manoir
    {"x": 150, "z": 60, "y": 66, "rayon": 34, "transition": 30},    # Relais royal
    {"x": 0, "z": 56, "y": 66, "rayon": 34, "transition": 30},      # Pied du pont
]


def gen_worldgen() -> None:
    # Bruits
    data("worldgen/noise", "collines", {"firstOctave": -7, "amplitudes": [1.0, 0.7, 0.35, 0.15]})
    data("worldgen/noise", "iles", {"firstOctave": -6, "amplitudes": [1.0, 0.5]})
    data("worldgen/noise", "dessous", {"firstOctave": -5, "amplitudes": [1.0, 0.6]})
    # Fonctions de densité
    sol_haut = {"type": lb("aplanir"), "pente": 0.03125, "sites": SITES,
                "argument": add(ycg(32, 96, 1.0, -1.0), mul(noise(lb("collines"), 1.0, 0.0), 0.42))}
    sol_bas = add(ycg(10, 34, -1.0, 1.0), mul(noise(lb("dessous"), 1.0, 1.0), 0.5))
    bande = mul(ycg(104, 118, 0.0, 1.0), ycg(140, 164, 1.0, 0.0))
    iles = add(mul(bande, add(noise(lb("iles"), 0.8, 1.6), -0.52)), -0.03)
    terrain = dmax(dmin(sol_haut, sol_bas), iles)
    data("worldgen/density_function", "terrain", terrain)
    data("worldgen/density_function", "densite_finale", {"type": "minecraft:squeeze", "argument": mul(0.64, {"type": "minecraft:interpolated", "argument": {"type": "minecraft:blend_density", "argument": lb("terrain")}})})
    shifted = lambda n: {"type": "minecraft:shifted_noise", "noise": n, "shift_x": "minecraft:shift_x", "shift_y": 0.0, "shift_z": "minecraft:shift_z", "xz_scale": 0.25, "y_scale": 0.0}
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
        "surface_rule": {"type": "minecraft:sequence", "sequence": [
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "add_surface_depth": False, "offset": 0, "secondary_depth_range": 0, "surface_type": "floor"},
             "then_run": {"type": "minecraft:condition", "if_true": {"type": "minecraft:y_above", "anchor": {"absolute": 24}, "surface_depth_multiplier": 0, "add_stone_depth": False},
                          "then_run": {"type": "minecraft:block", "result_state": state(lb("herbe_argentee"))}}},
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "add_surface_depth": True, "offset": 0, "secondary_depth_range": 0, "surface_type": "floor"},
             "then_run": {"type": "minecraft:condition", "if_true": {"type": "minecraft:y_above", "anchor": {"absolute": 24}, "surface_depth_multiplier": 0, "add_stone_depth": False},
                          "then_run": {"type": "minecraft:block", "result_state": state(lb("terre_des_limbes"))}}},
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:stone_depth", "add_surface_depth": False, "offset": 0, "secondary_depth_range": 0, "surface_type": "ceiling"},
             "then_run": {"type": "minecraft:block", "result_state": state(lb("pierre_nacree_polie"))}}]}})
    # Type de dimension et dimension
    data("dimension_type", "limbes", {
        "ultrawarm": False, "natural": True, "coordinate_scale": 1.0, "has_skylight": True, "has_ceiling": False, "ambient_light": 0.1,
        "piglin_safe": False, "bed_works": True, "respawn_anchor_works": False, "has_raids": False,
        "logical_height": 256, "min_y": 0, "height": 256, "infiniburn": "#minecraft:infiniburn_overworld", "effects": lb("limbes"),
        "monster_spawn_light_level": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 7}, "monster_spawn_block_light_limit": 0})
    full = [-1.0, 1.0]
    data("dimension", "limbes", {"type": lb("limbes"), "generator": {"type": "minecraft:noise", "settings": lb("limbes"), "biome_source": {"type": "minecraft:multi_noise", "biomes": [
        {"biome": lb("plaines_nacrees"), "parameters": {"temperature": [-1.0, 0.1], "humidity": full, "continentalness": full, "erosion": full, "weirdness": full, "depth": full, "offset": 0.0}},
        {"biome": lb("vergers_suspendus"), "parameters": {"temperature": [0.1, 1.0], "humidity": [-1.0, 0.0], "continentalness": full, "erosion": full, "weirdness": full, "depth": full, "offset": 0.0}},
        {"biome": lb("vallee_des_lanternes"), "parameters": {"temperature": [0.1, 1.0], "humidity": [0.0, 1.0], "continentalness": full, "erosion": full, "weirdness": full, "depth": full, "offset": 0.0}}]}}})
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
    data("worldgen/configured_feature", "lac", {"type": "minecraft:lake", "config": {"barrier": simple(lb("pierre_nacree_polie")), "fluid": simple("minecraft:water", {"level": "0"})}})
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
    # Biomes
    def biome(name, temperature, sky, fog, water, water_fog, grass, foliage, features_vegetal, creatures, ambient, monsters, music):
        features = [[] for _ in range(11)]
        features[1] = [lb("lacs")]
        features[6] = [lb("cristaux")]
        features[9] = features_vegetal
        data("worldgen/biome", name, {
            "temperature": temperature, "downfall": 0.4, "has_precipitation": False,
            "effects": {"sky_color": sky, "fog_color": fog, "water_color": water, "water_fog_color": water_fog, "grass_color": grass, "foliage_color": foliage,
                        "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0},
                        "music": {"sound": music, "min_delay": 12000, "max_delay": 24000, "replace_current_music": False}},
            "spawners": {"creature": creatures, "ambient": ambient, "monster": monsters, "water_creature": [], "water_ambient": [], "underground_water_creature": [], "axolotls": [], "misc": []},
            "spawn_costs": {}, "carvers": {}, "features": features})

    cerf = {"type": lb("cerf_lanterne"), "weight": 10, "minCount": 2, "maxCount": 4}
    mouton = {"type": "minecraft:sheep", "weight": 4, "minCount": 2, "maxCount": 3}
    lapin = {"type": "minecraft:rabbit", "weight": 3, "minCount": 2, "maxCount": 3}
    poisson = {"type": lb("poisson_lune"), "weight": 12, "minCount": 3, "maxCount": 6}
    veilleur = {"type": lb("veilleur_egare"), "weight": 3, "minCount": 1, "maxCount": 1}
    biome("plaines_nacrees", 0.7, 0x9FC4E8, 0xDDE3F0, 0x6EC4E8, 0x9AD8F0, 0xB4C6AC, 0xC9B7A2,
          [lb("arbres_aube_epars"), lb("grands_arbres_aube"), lb("bosquet_fibres"), lb("herbes_aube"), lb("fleurs_rosee")],
          [cerf, mouton, lapin], [poisson], [veilleur], "minecraft:music.overworld.meadow")
    biome("vergers_suspendus", 0.8, 0xA8C8EC, 0xE6E0EA, 0x6EC4E8, 0x9AD8F0, 0xC2CDA8, 0xD9B59A,
          [lb("arbres_aube_verger"), lb("bosquet_fibres"), lb("herbes_aube_denses"), lb("fleurs_rosee")],
          [cerf, mouton, lapin], [poisson], [], "minecraft:music.overworld.cherry_grove")
    biome("vallee_des_lanternes", 0.6, 0x8FB8E6, 0xCFD9EE, 0x6EC4E8, 0x9AD8F0, 0xA9C0B4, 0xC9B7A2,
          [lb("arbres_aube_epars"), lb("bosquet_fibres_dense"), lb("lanternes_perdues"), lb("fleurs_rosee")],
          [{"type": lb("cerf_lanterne"), "weight": 16, "minCount": 2, "maxCount": 5}], [{"type": lb("poisson_lune"), "weight": 16, "minCount": 4, "maxCount": 8}], [veilleur],
          "minecraft:music.overworld.forest")
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
