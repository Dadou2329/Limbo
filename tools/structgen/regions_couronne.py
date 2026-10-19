"""Gabarits de la Couronne Immobile : Haute-Garde (cité fortifiée de Vaëron) et le quartier des portes et des jardins de la capitale endormie."""
from __future__ import annotations

import math

from builders import (BARRIERE, CRISTAL, HERBE, LANTERNE, LANTERNE_H, PIERRE_P, PIERRE_S, PIERRE_T, PLANCHES, STAIR_BOIS, TERRE, chair, counter, data_marker, door,
                      hanging_lantern, table, tree, window)
from canvas import AIR, VOID, Canvas

PC = "les_limbes:pierre_de_couronne"
PC_T = "les_limbes:pierre_de_couronne_taillee"
PC_P = "les_limbes:pierre_de_couronne_polie"
PC_S = "les_limbes:pierre_de_couronne_sculptee"
LANTERNE_R = "les_limbes:lanterne_royale[hanging=false,waterlogged=false]"
LANTERNE_RH = "les_limbes:lanterne_royale[hanging=true,waterlogged=false]"
PORTE_RELAIS = "les_limbes:porte_de_relais"
STAIR_PC = "minecraft:quartz"
DALLE_PC = "minecraft:quartz_slab[type=bottom,waterlogged=false]"
DALLE_PC_H = "minecraft:quartz_slab[type=top,waterlogged=false]"
BANNIERE = "minecraft:blue_banner[rotation=0]"
EAU = "minecraft:water[level=0]"
PORTE_FER = "minecraft:iron_door"


def stair(material: str, facing: str, half: str = "bottom") -> str:
    return f"{material}_stairs[facing={facing},half={half},shape=straight,waterlogged=false]"


def relais(c: Canvas, x: int, y: int, z: int, facing: str, ident: str) -> None:
    c.set(x, y, z, f"{PORTE_RELAIS}[eveillee=false,facing={facing}]", {"id": PORTE_RELAIS, "relais": ident})
    c.set(x, y + 1, z, AIR)


def batiment_royal(c: Canvas, x0: int, y: int, z0: int, w: int, d: int, h: int, role: str | None, facing: str = "south", enseigne: list[str] | None = None, toit: str = STAIR_PC) -> None:
    c.fill(x0, y, z0, x0 + w - 1, y, z0 + d - 1, PC_T)
    c.fill(x0 + 1, y, z0 + 1, x0 + w - 2, y, z0 + d - 2, PLANCHES)
    c.walls(x0, y + 1, z0, x0 + w - 1, y + h, z0 + d - 1, PC_T)
    c.fill(x0 + 1, y + 1, z0 + 1, x0 + w - 2, y + h, z0 + d - 2, AIR)
    for cx, cz in [(x0, z0), (x0 + w - 1, z0), (x0, z0 + d - 1), (x0 + w - 1, z0 + d - 1)]:
        c.column(cx, y + 1, y + h, cz, PC_P)
    c.fill(x0, y + h + 1, z0, x0 + w - 1, y + h + 1, z0 + d - 1, PC_T)
    c.gable_roof(x0, z0, x0 + w - 1, z0 + d - 1, y + h + 2, toit, axis="x" if w >= d else "z", overhang=1)
    dz = z0 + d - 1 if facing == "south" else z0
    door(c, x0 + w // 2, y + 1, dz, facing)
    window(c, x0 + 1, y + 2, dz, facing, 1)
    window(c, x0 + w - 2, y + 2, dz, facing, 1)
    window(c, x0, y + 2, z0 + d // 2, "west", 1)
    window(c, x0 + w - 1, y + 2, z0 + d // 2, "east", 1)
    hanging_lantern(c, x0 + w // 2, y + h, z0 + d // 2)
    if enseigne:
        c.sign_wall(x0 + w // 2 + 1, y + 2, dz, "minecraft:birch", facing, enseigne)
    if role:
        data_marker(c, x0 + w // 2, y + 1, z0 + d // 2, "pnj:" + role)


def build_haute_garde() -> Canvas:
    """Haute-Garde : remparts carrés avec tours, porte fortifiée, place d'armes, hall de Vaëron, caserne, école, infirmerie, maisons, marché couvert."""
    W, H, D = 69, 30, 69
    c = Canvas("haute_garde", W, H, D)
    c.note("Haute-Garde : cité fortifiée de Vaëron sur l'anneau extérieur de la Couronne. Remparts et tours, porte sud, place d'armes, hall, caserne, école, infirmerie, logements, marché, porte de relais.")
    cx, cz = 34, 34
    # sol : y local 0 = monde 81 (on marche à 82 = y1)
    for x in range(W):
        for z in range(D):
            if 2 <= x <= W - 3 and 2 <= z <= D - 3:
                c.set(x, 0, z, HERBE if (x + z) % 7 else TERRE)
    for x in range(cx - 12, cx + 13):
        for z in range(cz - 12, cz + 13):
            c.set(x, 0, z, PC_P if max(abs(x - cx), abs(z - cz)) % 4 == 0 else PC_T)
    for x in range(W):
        c.set(x, 0, cz, PC_T) if 2 <= x <= W - 3 else None
    for z in range(D):
        c.set(cx, 0, z, PC_T) if 2 <= z <= D - 3 else None
    # remparts (x/z 2 et 66), hauteur 7, chemin de ronde, créneaux ; tours d'angle 5x5 hauteur 12
    c.walls(2, 1, 2, W - 3, 6, D - 3, PC_T)
    c.walls(3, 1, 3, W - 4, 6, D - 4, PC)
    c.fill(4, 1, 4, W - 5, 6, D - 5, AIR)
    c.walls(2, 7, 2, W - 3, 7, D - 3, PC_P)
    c.walls(3, 7, 3, W - 4, 7, D - 4, PC_P)
    for i in range(2, W - 2, 2):
        c.set(i, 8, 2, PC_T); c.set(i, 8, D - 3, PC_T); c.set(2, 8, i, PC_T); c.set(W - 3, 8, i, PC_T)
    for tx, tz in [(2, 2), (W - 7, 2), (2, D - 7), (W - 7, D - 7)]:
        c.fill(tx, 1, tz, tx + 4, 11, tz + 4, PC_T)
        c.fill(tx + 1, 1, tz + 1, tx + 3, 10, tz + 3, AIR)
        c.fill(tx, 12, tz, tx + 4, 12, tz + 4, PC_P)
        for i in range(0, 5, 2):
            c.set(tx + i, 13, tz, PC_T); c.set(tx + i, 13, tz + 4, PC_T); c.set(tx, 13, tz + i, PC_T); c.set(tx + 4, 13, tz + i, PC_T)
        c.set(tx + 2, 13, tz + 2, LANTERNE_R)
        for y in range(1, 11):
            c.set(tx + 1 + (y % 3), y, tz + 1 + ((y // 3) % 3), "minecraft:ladder[facing=north,waterlogged=false]") if False else None
        c.column(tx + 2, 1, 10, tz + 2, "minecraft:ladder[facing=north,waterlogged=false]")
        c.set(tx + 2, 1, tz + 1, PC) ; c.set(tx + 2, 12, tz + 2, AIR)
    # porte sud fortifiée : ouverture 3 de large, herse de fer (portes de fer ouvertes), bannières
    for x in range(cx - 1, cx + 2):
        for y in range(1, 5):
            c.set(x, y, D - 3, AIR); c.set(x, y, D - 4, AIR)
    c.fill(cx - 3, 1, D - 5, cx - 2, 9, D - 2, PC_P); c.fill(cx + 2, 1, D - 5, cx + 3, 9, D - 2, PC_P)
    c.fill(cx - 3, 10, D - 5, cx + 3, 10, D - 2, PC_T)
    c.set(cx, 5, D - 3, PC_S); c.set(cx - 1, 5, D - 3, PC_T); c.set(cx + 1, 5, D - 3, PC_T)
    for x in (cx - 2, cx + 2):
        c.set(x, 8, D - 1, "minecraft:blue_wall_banner[facing=south]")
    c.set(cx - 3, 11, D - 3, LANTERNE_R); c.set(cx + 3, 11, D - 3, LANTERNE_R)
    # place d'armes : estrade, bannières, fontaine
    c.fill(cx - 1, 1, cz - 1, cx + 1, 1, cz + 1, PC_P); c.set(cx, 1, cz, EAU); c.set(cx, 2, cz, PC_S); c.set(cx, 3, cz, LANTERNE_R)
    for x, z in [(cx - 8, cz - 8), (cx + 8, cz - 8), (cx - 8, cz + 8), (cx + 8, cz + 8)]:
        c.column(x, 1, 3, z, PC_P); c.set(x, 4, z, BANNIERE)
    # hall de Vaëron (nord) : grande salle, colonnes, trône de pierre sculptée, table du conseil, cartes
    hx, hz = cx - 11, cz - 30
    batiment_royal(c, hx, 1, hz, 23, 15, 7, None, facing="south", enseigne=["Hall de", "Haute-Garde"])
    for x in (hx + 4, hx + 11, hx + 18):
        for z in (hz + 4, hz + 10):
            c.column(x, 2, 7, z, PC_P)
    c.fill(hx + 9, 2, hz + 1, hx + 13, 2, hz + 2, PC_P)
    c.set(hx + 11, 3, hz + 1, stair(STAIR_PC, "south")); c.set(hx + 10, 3, hz + 1, PC_S); c.set(hx + 12, 3, hz + 1, PC_S)
    c.set(hx + 11, 4, hz + 1, "minecraft:blue_wall_banner[facing=south]")
    table(c, hx + 9, 2, hz + 7, 5, "x")
    for x in (hx + 8, hx + 14):
        chair(c, x, 2, hz + 7, "east" if x < hx + 11 else "west")
    c.item_frame(hx + 3, 3, hz + 1, "south", "minecraft:map"); c.item_frame(hx + 19, 3, hz + 1, "south", "minecraft:filled_map")
    c.set(hx + 1, 2, hz + 1, "minecraft:lectern[facing=east,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Registre de Haute-Garde", "Intendance", [
            "Routes sûres : patrouilles de l'aube et du soir sur la route de la Couronne. Aucun voyageur perdu depuis trois ans.",
            "Logements : quarante familles installées, école ouverte, infirmerie tenue par Dame Hersent. Les Arrivées sont accueillies ici, puis logées.",
            "Vaëron dit : « Un trône se mérite par ce qu'on a bâti. » Regardez autour de vous."])})
    for z in (hz + 3, hz + 11):
        hanging_lantern(c, hx + 7, 7, z); hanging_lantern(c, hx + 15, 7, z)
    data_marker(c, hx + 11, 2, hz + 4, "pnj:intendant_garde")
    data_marker(c, hx + 6, 2, hz + 12, "pnj:garde_1")
    data_marker(c, hx + 16, 2, hz + 12, "pnj:garde_2")
    # caserne (ouest), école (est), infirmerie (sud-ouest), maisons (sud-est), marché couvert (nord-est)
    batiment_royal(c, cx - 30, 1, cz - 10, 13, 17, 5, "sergent", facing="east", enseigne=["Caserne de", "la Garde"])
    for z in range(cz - 8, cz + 5, 2):
        c.bed(cx - 29, 2, z, "blue", "east")
    c.set(cx - 29, 2, cz + 5, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/haute_garde"})
    batiment_royal(c, cx + 18, 1, cz - 10, 13, 11, 5, "maitresse_d_ecole", facing="west", enseigne=["École de", "Haute-Garde"])
    for z in range(cz - 8, cz - 1, 2):
        table(c, cx + 20, 2, z, 2, "x"); table(c, cx + 25, 2, z, 2, "x")
    c.set(cx + 29, 2, cz - 9, "minecraft:bookshelf"); c.set(cx + 29, 2, cz - 8, "minecraft:bookshelf"); c.set(cx + 29, 2, cz - 7, "minecraft:lectern[facing=west,has_book=false,powered=false]")
    batiment_royal(c, cx - 30, 1, cz + 10, 13, 13, 5, "hersent", facing="east", enseigne=["Infirmerie", "Dame Hersent"])
    for z in (cz + 12, cz + 15, cz + 18):
        c.bed(cx - 29, 2, z, "white", "east")
    c.set(cx - 20, 2, cz + 20, "minecraft:brewing_stand[has_bottle_0=true,has_bottle_1=false,has_bottle_2=false]"); c.set(cx - 21, 2, cz + 20, "minecraft:cauldron")
    batiment_royal(c, cx + 16, 1, cz + 12, 9, 9, 4, "famille_1", facing="west")
    batiment_royal(c, cx + 16, 1, cz + 23, 9, 9, 4, "famille_2", facing="west")
    batiment_royal(c, cx + 24, 1, cz + 17, 9, 9, 4, "famille_3", facing="west")
    # marché couvert (nord-est) : halle ouverte sur colonnes
    mx, mz = cx + 14, cz - 30
    c.fill(mx, 1, mz, mx + 16, 1, mz + 12, PC_T)
    for x in range(mx, mx + 17, 4):
        for z in (mz, mz + 12):
            c.column(x, 2, 5, z, PC_P)
    c.fill(mx, 6, mz, mx + 16, 6, mz + 12, PC_T)
    c.fill(mx + 1, 7, mz + 1, mx + 15, 7, mz + 11, DALLE_PC)
    for i, x in enumerate(range(mx + 2, mx + 15, 4)):
        counter(c, x, 2, mz + 3, 2, "x")
        c.item_frame(x, 3, mz + 1, "south", ["les_limbes:pain_de_la_cour", "minecraft:iron_ingot", "les_limbes:lanterne_royale", "minecraft:white_wool"][i])
        c.set(x + 1, 5, mz + 6, LANTERNE_RH)
    data_marker(c, mx + 8, 2, mz + 8, "pnj:boulanger_garde")
    data_marker(c, mx + 3, 2, mz + 8, "pnj:marchand_garde")
    # porte de relais (place, ouest), lanternes royales, arbres
    relais(c, cx - 12, 1, cz, "east", "haute_garde")
    for x, z in [(cx - 6, cz - 14), (cx + 6, cz - 14), (cx - 6, cz + 14), (cx + 6, cz + 14), (cx - 14, cz - 6), (cx + 14, cz - 6), (cx - 14, cz + 6), (cx + 14, cz + 6)]:
        c.column(x, 1, 3, z, "les_limbes:muret_de_pierre_nacree[east=none,north=none,south=none,up=true,waterlogged=false,west=none]"); c.set(x, 4, z, LANTERNE_R)
    for x, z in [(10, 10), (W - 11, 10), (10, D - 11), (W - 11, D - 14)]:
        tree(c, x, 1, z, 5, 2)
    c.sign_standing(cx + 2, 1, cz + 28, "minecraft:birch", 8, ["Haute-Garde", "cité de Vaëron", "routes sûres,", "portes ouvertes"])
    return c


def build_quartier_des_portes() -> Canvas:
    """Quartier des portes et des jardins : premier quartier de la capitale endormie — grandes portes closes, jardins figés, lanternes éteintes, gardien."""
    W, H, D = 51, 24, 41
    c = Canvas("quartier_des_portes", W, H, D)
    c.note("Capitale endormie, quartier des portes et des jardins : porte monumentale close, allée des lanternes éteintes, jardins figés, pavillon du gardien Elouan ; s'éveille avec la campagne.")
    cx = 25
    for x in range(W):
        for z in range(D):
            c.set(x, 0, z, PC_T if (x + z) % 5 else PC_P)
    # porte monumentale (nord) : deux pylônes de 16, linteau, battants de pierre sculptée fermés
    for px in (cx - 7, cx + 4):
        c.fill(px, 1, 2, px + 3, 16, 5, PC_T)
        c.fill(px + 1, 1, 3, px + 2, 15, 4, PC)
        c.set(px + 1, 17, 3, PC_S); c.set(px + 2, 17, 4, PC_S)
    c.fill(cx - 7, 15, 2, cx + 7, 17, 5, PC_T)
    c.fill(cx - 3, 1, 3, cx + 3, 12, 4, PC_S)
    c.fill(cx - 2, 1, 3, cx + 2, 11, 4, PC_P)
    c.set(cx, 6, 3, "minecraft:gold_block"); c.set(cx, 7, 3, CRISTAL)
    c.set(cx - 8, 18, 3, LANTERNE_R); c.set(cx + 8, 18, 3, LANTERNE_R)
    # allée des lanternes éteintes (lanternes vanilla d'âmes) vers le sud, murets, bassins secs
    for z in range(8, D - 4, 6):
        for x in (cx - 10, cx + 10):
            c.column(x, 1, 3, z, PC_P); c.set(x, 4, z, "minecraft:soul_lantern[hanging=false,waterlogged=false]")
    for x in range(cx - 1, cx + 2):
        for z in range(6, D - 2):
            c.set(x, 0, z, PC_P)
    for bx in (cx - 20, cx + 12):
        c.fill(bx, 0, 14, bx + 8, 0, 22, PC_P)
        c.fill(bx + 1, 0, 15, bx + 7, 0, 21, PC)  # bassin à sec
        c.set(bx + 4, 1, 18, PC_S)
    # jardins figés : parterres de terre nue, arbres d'aube sans feuilles (troncs), haies de feuilles persistantes
    for gx in (cx - 20, cx + 12):
        for gz in (24, 30):
            c.fill(gx, 0, gz, gx + 8, 0, gz + 4, TERRE)
            for x in range(gx + 1, gx + 8, 3):
                c.column(x, 1, 4, gz + 2, "les_limbes:buche_d_aube[axis=y]")
    # pavillon du gardien (sud-est)
    batiment_royal(c, cx + 12, 1, D - 12, 11, 9, 4, "elouan", facing="west", enseigne=["Pavillon du", "gardien"])
    c.set(cx + 14, 2, D - 10, "minecraft:lectern[facing=east,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Veille du quartier des portes", "Elouan", [
            "Je garde une porte qui ne s'ouvre pas. Depuis la disparition du roi, la capitale dort : lanternes éteintes, bassins secs, jardins sans feuilles.",
            "On dit qu'un fragment de couronne suffit à réveiller un quartier. Je n'ai vu que des prétendants qui parlaient fort.",
            "Si quelqu'un porte un fragment et vient avec de l'eau, de la lumière et de la patience, je lui montrerai où commencer."])})
    c.sign_standing(cx, 1, 7, "minecraft:birch", 0, ["Quartier des", "portes", "la capitale", "dort"])
    return c


COURONNE = {
    "couronne/haute_garde": build_haute_garde,
    "couronne/quartier_des_portes": build_quartier_des_portes,
}
