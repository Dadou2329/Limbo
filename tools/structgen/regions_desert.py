"""Gabarits du Désert des Heures : Méridiane (horloge, caravansérail, marché des objets arrivés, quartier figé, archives de Sahir), observatoire des heures, jardin fertile."""
from __future__ import annotations

import math

from builders import (BARRIERE, CRISTAL, LANTERNE, LANTERNE_H, PIERRE_P, PIERRE_S, PIERRE_T, PLANCHES, chair, counter, data_marker, door, hanging_lantern, lamppost,
                      table, window)
from canvas import AIR, VOID, Canvas

SABLE = "les_limbes:sable_d_opale"
GRES = "les_limbes:gres_des_heures"
GRES_T = "les_limbes:gres_des_heures_taille"
PIERRE_H = "les_limbes:pierre_des_heures"
CADRAN = "les_limbes:cadran_des_heures[heure=1]"
PAVE = "les_limbes:pave_horaire"
PORTE_RELAIS = "les_limbes:porte_de_relais"
TAPIS = ["minecraft:orange_carpet", "minecraft:red_carpet", "minecraft:yellow_carpet", "minecraft:white_carpet"]
EAU = "minecraft:water[level=0]"
PALMIER = "minecraft:jungle_log[axis=y]"
PALMES = "minecraft:jungle_leaves[distance=1,persistent=true,waterlogged=false]"
STAIR_GRES = "minecraft:smooth_sandstone"
DALLE_GRES = "minecraft:smooth_sandstone_slab[type=bottom,waterlogged=false]"
DALLE_GRES_H = "minecraft:smooth_sandstone_slab[type=top,waterlogged=false]"


def relais(c: Canvas, x: int, y: int, z: int, facing: str, ident: str) -> None:
    c.set(x, y, z, f"{PORTE_RELAIS}[eveillee=false,facing={facing}]", {"id": PORTE_RELAIS, "relais": ident})
    c.set(x, y + 1, z, AIR)


def palmier(c: Canvas, x: int, y: int, z: int, h: int = 6) -> None:
    for i in range(h):
        c.set(x, y + i, z, PALMIER)
    top = y + h
    for dx, dz in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1), (2, 0), (-2, 0), (0, 2), (0, -2), (1, 1), (-1, -1), (1, -1), (-1, 1)):
        yy = top if abs(dx) + abs(dz) <= 1 else top - 1
        if c.inside(x + dx, yy, z + dz) and c.get(x + dx, yy, z + dz) in (VOID, AIR):
            c.set(x + dx, yy, z + dz, PALMES)


def maison_de_sable(c: Canvas, x0: int, y: int, z0: int, w: int, d: int, role: str | None, facing: str = "south", enseigne: list[str] | None = None, toit_plat: bool = True) -> None:
    """Maison de grès à toit plat et terrasse, porche, tapis, jarre ; style des cités du désert."""
    c.fill(x0, y, z0, x0 + w - 1, y, z0 + d - 1, GRES_T)
    c.fill(x0 + 1, y, z0 + 1, x0 + w - 2, y, z0 + d - 2, GRES)
    c.walls(x0, y + 1, z0, x0 + w - 1, y + 3, z0 + d - 1, GRES)
    c.fill(x0 + 1, y + 1, z0 + 1, x0 + w - 2, y + 3, z0 + d - 2, AIR)
    for cx, cz in [(x0, z0), (x0 + w - 1, z0), (x0, z0 + d - 1), (x0 + w - 1, z0 + d - 1)]:
        c.column(cx, y + 1, y + 4, cz, GRES_T)
    c.fill(x0, y + 4, z0, x0 + w - 1, y + 4, z0 + d - 1, GRES_T)
    c.fill(x0 + 1, y + 4, z0 + 1, x0 + w - 2, y + 4, z0 + d - 2, PIERRE_H)
    for x in range(x0, x0 + w):
        for z in (z0, z0 + d - 1):
            if (x - x0) % 2 == 0:
                c.set(x, y + 5, z, DALLE_GRES)
    for z in range(z0, z0 + d):
        for x in (x0, x0 + w - 1):
            if (z - z0) % 2 == 0:
                c.set(x, y + 5, z, DALLE_GRES)
    dz = z0 + d - 1 if facing == "south" else z0
    door(c, x0 + w // 2, y + 1, dz, facing)
    window(c, x0 + 1, y + 2, dz, facing, 1)
    window(c, x0 + w - 2, y + 2, dz, facing, 1)
    window(c, x0, y + 2, z0 + d // 2, "west", 1)
    window(c, x0 + w - 1, y + 2, z0 + d // 2, "east", 1)
    c.bed(x0 + 1, y + 1, z0 + 1, "orange", "south" if facing == "south" else "north")
    c.set(x0 + w - 2, y + 1, z0 + 1, "minecraft:chest[facing=south,type=single,waterlogged=false]")
    c.set(x0 + w - 2, y + 1, z0 + 2, "minecraft:decorated_pot[cracked=false,facing=north,waterlogged=false]")
    for i, x in enumerate(range(x0 + 2, x0 + w - 2)):
        c.carpet(x, y + 1, z0 + d // 2, TAPIS[i % 4].split(":")[1].replace("_carpet", ""))
    hanging_lantern(c, x0 + w // 2, y + 3, z0 + d // 2)
    if enseigne:
        c.sign_wall(x0 + w // 2 + 1, y + 2, dz, "minecraft:jungle", facing, enseigne)
    if role:
        data_marker(c, x0 + w // 2, y + 1, z0 + d // 2 + (1 if facing == "north" else -1), "pnj:" + role)


def build_meridiane() -> Canvas:
    """Méridiane : murs de grès, grande horloge centrale (tour au cadran), caravansérail en anneau, marché des objets arrivés, quartier figé, archives."""
    W, H, D = 71, 36, 71
    c = Canvas("meridiane", W, H, D)
    c.note("Méridiane : cité du Désert des Heures. Tour de l'horloge et son cadran, caravansérail autour, marché des objets venus d'ailleurs, quartier figé pendant une fête, archives de Sahir, porte de relais.")
    cx, cz = 35, 35
    # sol (y local 0 = monde 65, on marche à 66 = y1) : sable d'opale, place pavée de grès au centre, routes
    for x in range(W):
        for z in range(D):
            d = math.hypot(x - cx, z - cz)
            if d <= 35:
                c.set(x, 0, z, SABLE)
            if d <= 14:
                c.set(x, 0, z, GRES_T if int(d) % 4 else PIERRE_H)
    for x in range(W):
        if abs(x - cx) > 14:
            c.set(x, 0, cz, GRES_T); c.set(x, 0, cz + 1, GRES_T)
    for z in range(D):
        if abs(z - cz) > 14:
            c.set(cx, 0, z, GRES_T); c.set(cx + 1, 0, z, GRES_T)
    # enceinte octogonale basse (rayon 33) avec quatre portes
    for a in range(0, 360):
        r = 33.0
        x = int(round(cx + math.cos(math.radians(a)) * r)); z = int(round(cz + math.sin(math.radians(a)) * r))
        if not c.inside(x, 1, z):
            continue
        if abs(x - cx) <= 2 or abs(z - cz) <= 2:
            continue
        c.column(x, 1, 3, z, GRES_T if a % 15 else PIERRE_H)
        if a % 30 == 0:
            c.set(x, 4, z, LANTERNE)
    # tour de l'horloge : base 7x7, 24 de haut, cadran face sud
    tx, tz = cx - 3, cz - 3
    c.fill(tx, 1, tz, tx + 6, 24, tz + 6, GRES_T)
    c.fill(tx + 1, 1, tz + 1, tx + 5, 23, tz + 5, AIR)
    for y in range(1, 24, 6):
        c.walls(tx, y, tz, tx + 6, y, tz + 6, PIERRE_H)
    c.fill(tx, 25, tz, tx + 6, 25, tz + 6, PIERRE_H)
    c.fill(tx + 1, 26, tz + 1, tx + 5, 26, tz + 5, GRES_T)
    c.fill(tx + 2, 27, tz + 2, tx + 4, 27, tz + 4, GRES_T)
    c.set(tx + 3, 28, tz + 3, CRISTAL); c.set(tx + 3, 29, tz + 3, LANTERNE)
    # cadran : grand disque de pierre des heures sur la face sud, cadran mécanique au centre (redstone)
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            if dx * dx + dy * dy <= 10:
                c.set(tx + 3 + dx, 18 + dy, tz + 6, PIERRE_P if dx * dx + dy * dy > 6 else "minecraft:gold_block")
    c.set(tx + 3, 18, tz + 6, CADRAN)
    door(c, tx + 3, 1, tz + 6, "south")
    for i in range(20):
        x = tx + 1 + (i % 4) if (i // 4) % 2 == 0 else tx + 5 - (i % 4)
        c.set(x, 1 + i, tz + 1 + (i // 4) % 5, stair(STAIR_GRES, "east") if (i // 4) % 2 == 0 else stair(STAIR_GRES, "west"))
    c.set(tx + 3, 10, tz + 3, "minecraft:lectern[facing=south,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "L'horloge de Méridiane", "Les horlogers", [
            "L'horloge ne donne pas l'heure : elle la garde. Quatre quarts, quatre lumières. Les pavés horaires des routes n'existent qu'à leur quart.",
            "Le quartier de la fête est figé depuis la disparition du roi : ses habitants n'ont pas vieilli d'un jour. Sahir dit qu'il faut les laisser tant qu'on ne sait pas les rendre à aujourd'hui sans les perdre.",
            "Mécanisme : le cadran alimente la redstone selon le quart. Aube 4, midi 8, soir 12, nuit 16."])})
    data_marker(c, tx + 3, 1, tz + 3, "pnj:horloger")
    # caravansérail : anneau d'arcades autour de la place (rayon 18..22), toits plats, étals et stalles
    for a in range(0, 360, 12):
        for r in (18, 22):
            x = int(round(cx + math.cos(math.radians(a)) * r)); z = int(round(cz + math.sin(math.radians(a)) * r))
            if abs(x - cx) <= 2 or abs(z - cz) <= 2:
                continue
            c.column(x, 1, 4, z, GRES_T)
    for a in range(0, 360):
        for r in range(18, 23):
            x = int(round(cx + math.cos(math.radians(a)) * r)); z = int(round(cz + math.sin(math.radians(a)) * r))
            if abs(x - cx) <= 2 or abs(z - cz) <= 2:
                continue
            c.set(x, 5, z, DALLE_GRES_H if r in (18, 22) else PIERRE_H)
            if r in (19, 20, 21) and a % 45 == 20:
                c.set(x, 1, z, "minecraft:hay_block[axis=y]")
    for a in range(10, 360, 90):
        x = int(round(cx + math.cos(math.radians(a)) * 20)); z = int(round(cz + math.sin(math.radians(a)) * 20))
        c.set(x, 1, z, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/meridiane"})
        c.set(x, 4, z, LANTERNE_H)
    data_marker(c, cx + 20, 1, cz + 5, "pnj:caravanier")
    data_marker(c, cx - 20, 1, cz - 5, "pnj:chamelier")
    # marché des objets arrivés d'ailleurs (quart nord-est de la place) : étals sous toiles
    for i, (ex, ez) in enumerate([(cx + 6, cz - 10), (cx + 10, cz - 6), (cx + 6, cz - 6)]):
        c.set(ex, 1, ez, "minecraft:jungle_slab[type=top,waterlogged=false]"); c.set(ex + 1, 1, ez, "minecraft:jungle_slab[type=top,waterlogged=false]")
        c.set(ex - 1, 1, ez, "minecraft:jungle_fence[east=false,north=false,south=false,waterlogged=false,west=false]"); c.set(ex - 1, 2, ez, "minecraft:jungle_fence[east=false,north=false,south=false,waterlogged=false,west=false]")
        c.set(ex + 2, 1, ez, "minecraft:jungle_fence[east=false,north=false,south=false,waterlogged=false,west=false]"); c.set(ex + 2, 2, ez, "minecraft:jungle_fence[east=false,north=false,south=false,waterlogged=false,west=false]")
        c.fill(ex - 1, 3, ez - 1, ex + 2, 3, ez + 1, ["minecraft:orange_wool", "minecraft:red_wool", "minecraft:yellow_wool"][i])
        c.item_frame(ex, 2, ez, "south", ["minecraft:clock", "minecraft:spyglass", "les_limbes:sablier_d_opale"][i])
    data_marker(c, cx + 8, 1, cz - 8, "pnj:marchande_d_ailleurs")
    # archives de Sahir (ouest) : maison de grès à deux salles, lutrins, bibliothèques
    maison_de_sable(c, cx - 30, 1, cz - 12, 11, 11, None, facing="east")
    c.set(cx - 25, 2, cz - 2, AIR)
    for x in range(cx - 29, cx - 21):
        c.set(x, 2, cz - 11, "minecraft:bookshelf"); c.set(x, 3, cz - 11, "minecraft:bookshelf")
    c.set(cx - 25, 2, cz - 8, "minecraft:lectern[facing=south,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Archives des heures", "Sahir", [
            "J'ai connu le roi. Il ne gouvernait pas le temps ; il le respectait. Ce que je protège ici, c'est ce que les gens ont vécu : on ne l'efface pas pour faire place nette.",
            "Celui qui prétendra au trône devra comprendre une chose : rendre un quartier à aujourd'hui sans perdre ceux qui y vivent. Je l'attends."])})
    data_marker(c, cx - 25, 2, cz - 6, "pnj:sahir")
    data_marker(c, cx - 23, 2, cz - 4, "pnj:archiviste")
    c.hanging_sign_wall(cx - 20, 3, cz - 7, "minecraft:jungle", "east", ["Archives", "des heures"])
    # quartier figé (sud-est) : ruelle avec lampions, tables de fête, habitants immobiles (rôle fige_*)
    qx, qz = cx + 10, cz + 12
    c.fill(qx, 0, qz, qx + 20, 0, qz + 16, PIERRE_H)
    for x in range(qx, qx + 21, 5):
        c.column(x, 1, 4, qz, GRES_T); c.column(x, 1, 4, qz + 16, GRES_T)
        c.set(x, 5, qz, LANTERNE); c.set(x, 5, qz + 16, LANTERNE)
    for x in range(qx + 1, qx + 20, 2):
        c.set(x, 5, qz + 8, "minecraft:yellow_wool" if x % 4 == 1 else "minecraft:orange_wool")
        c.set(x, 4, qz + 8, LANTERNE_H)
    for x in (qx + 3, qx + 9, qx + 15):
        table(c, x, 1, qz + 5, 3, "x")
        chair(c, x - 1, 1, qz + 5, "east"); chair(c, x + 3, 1, qz + 5, "west")
        c.set(x + 1, 2, qz + 5, "minecraft:cake[bites=0]") if x == qx + 9 else None
        table(c, x, 1, qz + 11, 3, "x")
    for i, (fx, fz) in enumerate([(qx + 2, qz + 8), (qx + 7, qz + 3), (qx + 12, qz + 13), (qx + 17, qz + 7), (qx + 10, qz + 9)]):
        data_marker(c, fx, 1, fz, f"pnj:fige_{i + 1}")
    c.sign_standing(qx, 1, qz + 18, "minecraft:jungle", 0, ["Quartier figé", "la fête du roi", "n'a jamais", "fini"])
    # maisons du sud-ouest et nord, puits, palmiers, porte de relais au nord de la place
    maison_de_sable(c, cx - 28, 1, cz + 8, 9, 9, "potiere", facing="east", enseigne=["Poterie", "des dunes"])
    maison_de_sable(c, cx - 16, 1, cz + 18, 9, 9, "guide_des_dunes", facing="north", enseigne=["Guide", "des dunes"])
    maison_de_sable(c, cx - 8, 1, cz - 32, 11, 9, "aubergiste_meridiane", facing="south", enseigne=["Auberge de", "l'Ombre longue"])
    maison_de_sable(c, cx + 8, 1, cz - 32, 9, 9, "habitant_dunes", facing="south")
    c.fill(cx + 24, 1, cz - 24, cx + 26, 1, cz - 22, GRES_T); c.set(cx + 25, 1, cz - 23, EAU); c.set(cx + 25, 0, cz - 23, EAU)
    for x, z in [(cx + 28, cz - 20), (cx - 24, cz + 26), (cx + 26, cz + 24), (cx - 26, cz - 26)]:
        palmier(c, x, 1, z, 6)
    relais(c, cx, 1, cz - 16, "south", "meridiane")
    for a in range(0, 360, 60):
        x = int(round(cx + math.cos(math.radians(a + 30)) * 12)); z = int(round(cz + math.sin(math.radians(a + 30)) * 12))
        lamppost(c, x, 1, z, 3)
    # route de midi : pavés horaires (phase midi) qui traversent la place vers la porte est
    for x in range(cx + 3, cx + 32):
        c.set(x, 0, cz - 1, f"{PAVE}[phase=1,visible=false]")
    c.sign_standing(cx + 16, 1, cz - 3, "minecraft:jungle", 4, ["Route de midi", "elle n'existe", "qu'au milieu", "du jour"])
    return c


def build_observatoire_des_heures() -> Canvas:
    """Observatoire des heures : tour-cadran isolée, escalier extérieur en spirale, lunette, pavés horaires de la route de nuit ; astronome."""
    W, H, D = 15, 26, 15
    c = Canvas("observatoire_des_heures", W, H, D)
    c.note("Observatoire des heures : tour de grès à cadran, route de nuit en pavés horaires, lunette ; l'astronome Yéléna.")
    cx, cz = 7, 7
    c.fill(0, 0, 0, W - 1, 0, D - 1, SABLE)
    c.fill(2, 0, 2, W - 3, 0, D - 3, GRES_T)
    for y in range(1, 18):
        r = 4.5 if y < 12 else 3.5
        for x in range(W):
            for z in range(D):
                d = math.hypot(x - cx, z - cz)
                if d <= r:
                    c.set(x, y, z, AIR if d < r - 1 else (PIERRE_H if y % 4 == 0 else GRES))
    door(c, cx, 1, cz + 4, "south")
    for y in (4, 8, 12):
        window(c, cx, y, cz - 4, "north", 1)
    for i in range(16):
        a = i * 50
        x = int(round(cx + math.cos(math.radians(a)) * 3.2)); z = int(round(cz + math.sin(math.radians(a)) * 3.2))
        c.set(x, 1 + i, z, stair(STAIR_GRES, ["east", "south", "west", "north"][(i // 4) % 4]))
    c.fill(cx - 3, 18, cz - 3, cx + 3, 18, cz + 3, PIERRE_H)
    c.fill(cx - 2, 18, cz - 2, cx + 2, 18, cz + 2, PLANCHES)
    for x in range(cx - 3, cx + 4):
        c.set(x, 19, cz - 3, BARRIERE); c.set(x, 19, cz + 3, BARRIERE)
    for z in range(cz - 3, cz + 4):
        c.set(cx - 3, 19, z, BARRIERE); c.set(cx + 3, 19, z, BARRIERE)
    c.set(cx, 19, cz, "minecraft:lightning_rod[facing=up,powered=false,waterlogged=false]"); c.set(cx, 20, cz, "minecraft:glass")
    c.set(cx - 2, 19, cz - 2, CADRAN)
    for x, z in [(cx - 3, cz - 3), (cx + 3, cz - 3), (cx - 3, cz + 3), (cx + 3, cz + 3)]:
        c.set(x, 20, z, LANTERNE)
    c.set(cx + 2, 1, cz, "minecraft:lectern[facing=west,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Carnet d'observation", "Yéléna", [
            "La route de nuit passe au pied de l'observatoire : des pavés qui n'existent qu'une fois le soleil couché. Le jour, on marche à côté, dans le sable.",
            "Depuis la terrasse, à minuit, on voit la silhouette du palais plus nette qu'ailleurs. Comme si la Couronne Immobile était plus proche la nuit."])})
    data_marker(c, cx, 18, cz + 2, "pnj:yelena")
    for x in range(0, W):
        c.set(x, 0, 1, f"{PAVE}[phase=3,visible=false]")
    c.sign_standing(cx + 3, 1, cz + 5, "minecraft:jungle", 0, ["Observatoire", "des heures", "route de nuit", "au nord"])
    return c


def build_jardin_fertile() -> Canvas:
    """Jardin redevenant fertile : bassin sec, canaux, plantations mortes à réveiller (quête du sablier), palmiers ; jardinier des heures."""
    W, H, D = 25, 12, 25
    c = Canvas("jardin_fertile", W, H, D)
    c.note("Jardin redevenant fertile : canaux à sec, bassin central, parterres de sable ; une fois le sablier d'opale rendu, l'eau revient (quête « Le jardin des heures »), jardinier Ismaël.")
    for x in range(W):
        for z in range(D):
            c.set(x, 0, z, SABLE)
    c.walls(0, 1, 0, W - 1, 1, D - 1, GRES_T)
    c.set(12, 1, D - 1, AIR); c.set(12, 1, 0, AIR)
    for x in range(2, W - 2):
        c.set(x, 0, 12, GRES_T); c.set(x, 0, 11, GRES_T)
    for z in range(2, D - 2):
        c.set(12, 0, z, GRES_T); c.set(11, 0, z, GRES_T)
    c.fill(9, 0, 9, 15, 0, 15, PIERRE_H)
    c.fill(10, 0, 10, 14, 0, 14, GRES)  # bassin à sec (le fond se remplira d'eau)
    c.set(12, 1, 12, PIERRE_S)
    c.set(12, 2, 12, "minecraft:gold_block")
    for x in range(3, 9, 2):
        for z in range(3, 9, 2):
            c.set(x, 1, z, "minecraft:dead_bush")
    for x in range(16, 22, 2):
        for z in range(16, 22, 2):
            c.set(x, 1, z, "minecraft:dead_bush")
    for x, z in [(4, 18), (20, 4)]:
        palmier(c, x, 1, z, 5)
    c.set(2, 1, 2, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/meridiane"})
    c.sign_standing(12, 1, 7, "minecraft:jungle", 8, ["Jardin des", "heures", "le sablier", "manque"])
    data_marker(c, 14, 1, 17, "pnj:ismael")
    return c


def stair(material: str, facing: str, half: str = "bottom") -> str:
    return f"{material}_stairs[facing={facing},half={half},shape=straight,waterlogged=false]"


DESERT = {
    "desert/meridiane": build_meridiane,
    "desert/observatoire_des_heures": build_observatoire_des_heures,
    "desert/jardin_fertile": build_jardin_fertile,
}
