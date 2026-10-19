"""Gabarits de la Mer suspendue : Port-Nacre, phare du Dessous, marché flottant, navire perdu, sanctuaire des bancs, quai des falaises.

Conventions : voir build_structures.py (Canvas, y local 0 = premier bloc du gabarit). Les gabarits sont
assemblés en structures composées à position fixe (voir LimbesSites / gen_data SITES).
"""
from __future__ import annotations

import math

from builders import (BARRIERE, CRISTAL, DALLE_BOIS, DALLE_PIERRE, FIBRES, FLEUR, HERBE, LANTERNE, LANTERNE_H, PIERRE_P, PIERRE_S, PIERRE_T, PLANCHES,
                      STAIR_BOIS, STAIR_PIERRE, TERRE, chair, corner_logs, counter, data_marker, door, fence_rect, hanging_lantern, lamppost, log, slab,
                      stair, table, tree, window)
from canvas import AIR, VOID, Canvas

SABLE = "les_limbes:sable_de_nacre"
CORAIL = "les_limbes:corail_de_nacre"
CORAIL_AZUR = "les_limbes:corail_azur"
EAU = "minecraft:water[level=0]"
BOIS_SOMBRE = "minecraft:dark_oak_planks"
BUCHE_SOMBRE = "minecraft:dark_oak_log[axis=y]"
STAIR_SOMBRE = "minecraft:dark_oak"
VERRE = "minecraft:glass"
PORTE_RELAIS = "les_limbes:porte_de_relais"


def relais(c: Canvas, x: int, y: int, z: int, facing: str, ident: str) -> None:
    c.set(x, y, z, f"{PORTE_RELAIS}[eveillee=false,facing={facing}]", {"id": PORTE_RELAIS, "relais": ident})
    c.set(x, y + 1, z, AIR)


def maison_de_port(c: Canvas, x0: int, y: int, z0: int, w: int, d: int, role: str | None, enseigne: list[str] | None, facing: str = "south") -> None:
    """Maison de pêcheur : soubassement de pierre, murs de planches sombres, toit à deux pans, mobilier."""
    c.fill(x0, y, z0, x0 + w - 1, y, z0 + d - 1, PIERRE_T)
    c.fill(x0 + 1, y, z0 + 1, x0 + w - 2, y, z0 + d - 2, PLANCHES)
    c.walls(x0, y + 1, z0, x0 + w - 1, y + 3, z0 + d - 1, BOIS_SOMBRE)
    c.fill(x0 + 1, y + 1, z0 + 1, x0 + w - 2, y + 3, z0 + d - 2, AIR)
    for cx, cz in [(x0, z0), (x0 + w - 1, z0), (x0, z0 + d - 1), (x0 + w - 1, z0 + d - 1)]:
        c.column(cx, y + 1, y + 3, cz, BUCHE_SOMBRE)
    c.fill(x0, y + 4, z0, x0 + w - 1, y + 4, z0 + d - 1, PLANCHES)
    c.gable_roof(x0, z0, x0 + w - 1, z0 + d - 1, y + 5, STAIR_SOMBRE, axis="x" if w >= d else "z", overhang=1)
    dz = z0 + d - 1 if facing == "south" else z0
    door(c, x0 + w // 2, y + 1, dz, facing)
    window(c, x0 + 1, y + 2, dz, facing, 1)
    window(c, x0 + w - 2, y + 2, dz, facing, 1)
    window(c, x0, y + 2, z0 + d // 2, "west", 1)
    window(c, x0 + w - 1, y + 2, z0 + d // 2, "east", 1)
    c.bed(x0 + 1, y + 1, z0 + 1, "cyan", "south" if facing == "south" else "north")
    c.set(x0 + w - 2, y + 1, z0 + 1, "minecraft:chest[facing=south,type=single,waterlogged=false]")
    c.set(x0 + w - 2, y + 1, z0 + 2, "minecraft:barrel[facing=up,open=false]")
    table(c, x0 + w // 2 - 1, y + 1, z0 + d // 2, 2, "x")
    chair(c, x0 + w // 2 - 2, y + 1, z0 + d // 2, "east")
    hanging_lantern(c, x0 + w // 2, y + 3, z0 + d // 2)
    c.item_frame(x0 + 1, y + 2, z0 + d - 2, "south", "minecraft:fishing_rod") if facing == "south" else None
    if enseigne:
        c.sign_wall(x0 + w // 2 + 1, y + 2, dz, "minecraft:dark_oak", facing, enseigne)
    if role:
        data_marker(c, x0 + w // 2, y + 1, z0 + d // 2 + 1, "pnj:" + role)


def build_port_nacre_ile() -> Canvas:
    """Île de Port-Nacre : plateau herbeux bordé de sable, quais de bois sur l'eau, place du marché, maisons, capitainerie."""
    W, H, D = 57, 20, 57
    c = Canvas("port_nacre_ile", W, H, D)
    c.note("Port-Nacre : île-ville de la Mer suspendue. Quais, marché aux poissons, capitainerie de Kaël, fumoir, maisons de pêcheurs, porte de relais.")
    cx, cz = 28, 28
    # Sol : y local 0..2 = monde 63..65 (bloc de surface à 65, on marche à 66) ; plage sous 66
    for x in range(W):
        for z in range(D):
            d = math.hypot(x - cx, z - cz)
            if d > 27.5:
                continue
            c.set(x, 0, z, SABLE)
            c.set(x, 1, z, SABLE if d > 24 else TERRE)
            c.set(x, 2, z, SABLE if d > 23 else HERBE)
    # Place pavée centrale (rayon 9) et fontaine à corail
    for x in range(W):
        for z in range(D):
            d = math.hypot(x - cx, z - cz)
            if d <= 9.5:
                c.set(x, 2, z, PIERRE_P if int(d) % 3 == 0 else PIERRE_T)
    c.fill(cx - 1, 3, cz - 1, cx + 1, 3, cz + 1, PIERRE_P)
    c.set(cx, 3, cz, CORAIL)
    c.set(cx, 4, cz, CORAIL_AZUR)
    c.set(cx, 5, cz, LANTERNE)
    for x, z in [(cx - 7, cz - 7), (cx + 7, cz - 7), (cx - 7, cz + 7), (cx + 7, cz + 7)]:
        lamppost(c, x, 3, z, 3)
    # Quai sud : ponton de bois sombre qui s'avance sur l'eau (monde 63 = y local 0 → ponton à y local 3)
    for z in range(cz + 24, D):
        for x in range(cx - 2, cx + 3):
            if c.inside(x, 3, z):
                c.set(x, 3, z, BOIS_SOMBRE)
                for y in range(0, 3):
                    if c.get(x, y, z) in (VOID, SABLE):
                        c.set(x, y, z, EAU if x in (cx - 1, cx, cx + 1) else BUCHE_SOMBRE)
    for z in (cz + 26, cz + 28):
        c.set(cx - 3, 4, z, BARRIERE); c.set(cx + 3, 4, z, BARRIERE)
    c.set(cx - 3, 5, cz + 28, LANTERNE); c.set(cx + 3, 5, cz + 28, LANTERNE)
    c.set(cx - 3, 4, D - 1, "minecraft:dark_oak_fence[east=false,north=false,south=false,waterlogged=false,west=false]")
    # Quais latéraux (est/ouest) avec étals de poissons
    for x in range(cx + 24, W):
        for z in range(cz - 2, cz + 3):
            if c.inside(x, 3, z):
                c.set(x, 3, z, BOIS_SOMBRE)
    for x in range(0, cx - 23):
        for z in range(cz - 2, cz + 3):
            c.set(x, 3, z, BOIS_SOMBRE)
    # Marché aux poissons (nord-est de la place) : trois étals couverts
    for i, (ex, ez) in enumerate([(cx + 10, cz - 12), (cx + 14, cz - 8), (cx + 10, cz - 4)]):
        c.fill(ex, 3, ez, ex + 2, 3, ez, "minecraft:dark_oak_slab[type=top,waterlogged=false]")
        c.set(ex - 1, 3, ez, BUCHE_SOMBRE); c.set(ex + 3, 3, ez, BUCHE_SOMBRE)
        c.set(ex - 1, 4, ez, BUCHE_SOMBRE); c.set(ex + 3, 4, ez, BUCHE_SOMBRE)
        c.fill(ex - 1, 5, ez - 1, ex + 3, 5, ez + 1, ["minecraft:cyan_wool", "minecraft:white_wool", "minecraft:light_blue_wool"][i])
        c.item_frame(ex + 1, 4, ez, "south", ["les_limbes:poisson_lanterne", "minecraft:cod", "les_limbes:perle_de_nacre"][i])
    data_marker(c, cx + 12, 3, cz - 6, "pnj:poissonniere")
    # Capitainerie de Kaël (ouest de la place) : grande maison à étage avec balcon et lunette
    kx, kz = cx - 22, cz - 8
    maison_de_port(c, kx, 2, kz, 11, 11, None, None, facing="east")
    c.fill(kx, 7, kz, kx + 10, 7, kz + 10, PLANCHES)
    c.walls(kx, 8, kz, kx + 10, 10, kz + 10, BOIS_SOMBRE)
    c.fill(kx + 1, 8, kz + 1, kx + 9, 10, kz + 9, AIR)
    c.fill(kx, 11, kz, kx + 10, 11, kz + 10, AIR)
    c.gable_roof(kx, kz, kx + 10, kz + 10, 11, STAIR_SOMBRE, axis="z", overhang=1)
    c.set(kx + 5, 5, kz + 10, AIR)  # retire la porte posée par maison_de_port côté sud (porte à l'est)
    for z in (kz + 2, kz + 5, kz + 8):
        window(c, kx + 10, 9, z, "east", 1)
    # escalier intérieur
    for i, (sx, sz) in enumerate([(kx + 1, kz + 8), (kx + 2, kz + 8), (kx + 3, kz + 8), (kx + 4, kz + 8)]):
        c.set(sx, 3 + i, sz, stair(STAIR_SOMBRE, "east"))
    c.set(kx + 5, 7, kz + 8, AIR); c.set(kx + 4, 7, kz + 8, AIR)
    # bureau de la capitainerie : cartes, table, coffre, journal
    c.set(kx + 2, 8, kz + 2, "minecraft:cartography_table")
    c.set(kx + 3, 8, kz + 2, "minecraft:lectern[facing=south,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Routes de Kaël", "Capitainerie de Port-Nacre", [
            "Routes ouvertes : Port-Nacre — Quai des Falaises (une heure de voile). Port-Nacre — marché flottant (lagons).",
            "Route fermée : l'archipel des épaves. Le navire perdu bouge encore quand la mer se plisse. Personne n'y va seul.",
            "Note du capitaine : relier tous les territoires, c'est bien. Savoir ce qu'on réveille en route, c'est mieux. Je l'apprends."])})
    c.item_frame(kx + 6, 9, kz + 1, "south", "minecraft:map")
    c.item_frame(kx + 7, 9, kz + 1, "south", "minecraft:compass")
    table(c, kx + 5, 8, kz + 5, 3, "x")
    c.set(kx + 8, 8, kz + 8, "minecraft:chest[facing=north,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/capitainerie"})
    hanging_lantern(c, kx + 5, 10, kz + 5)
    data_marker(c, kx + 5, 8, kz + 3, "pnj:kael")
    data_marker(c, kx + 7, 3, kz + 5, "pnj:second_de_kael")
    c.hanging_sign_wall(kx + 10, 4, kz + 5, "minecraft:dark_oak", "east", ["Capitainerie", "Routes de Kaël"])
    # Fumoir (sud-ouest) et maisons de pêcheurs (nord, est)
    maison_de_port(c, cx - 20, 2, cz + 6, 7, 7, "fumeur", ["Fumoir", "poisson-lanterne"], facing="east")
    c.set(cx - 17, 3, cz + 9, "minecraft:smoker[facing=east,lit=true]")
    c.set(cx - 17, 3, cz + 10, "minecraft:campfire[facing=east,lit=true,signal_fire=true,waterlogged=false]")
    maison_de_port(c, cx - 8, 2, cz - 22, 9, 7, "pecheur_1", None, facing="south")
    maison_de_port(c, cx + 3, 2, cz - 22, 9, 7, "pecheur_2", None, facing="south")
    maison_de_port(c, cx + 14, 2, cz + 8, 9, 9, "plongeuse", ["Plongée", "& épaves"], facing="west")
    # Auberge « Le Banc Lumineux » (sud-est), simple salle avec comptoir et chambres
    ax, az = cx + 4, cz + 12
    maison_de_port(c, ax, 2, az, 13, 9, "aubergiste_port", ["Le Banc", "Lumineux"], facing="north")
    counter(c, ax + 2, 3, az + 6, 5, "x")
    c.set(ax + 1, 3, az + 7, "minecraft:barrel[facing=up,open=false]")
    c.set(ax + 10, 3, az + 2, "minecraft:bed[facing=north,part=foot,occupied=false]") if False else None
    c.bed(ax + 10, 3, az + 1, "light_blue", "south")
    c.bed(ax + 10, 3, az + 5, "white", "south")
    # Porte de relais (nord de la place), arbres et végétation
    relais(c, cx, 3, cz - 11, "south", "port_nacre")
    for x, z in [(cx - 14, cz + 16), (cx + 18, cz - 16), (cx - 18, cz - 18)]:
        tree(c, x, 3, z, 5, 2)
    for x, z in [(cx - 12, cz - 2), (cx + 16, cz + 2), (cx - 4, cz + 18), (cx + 6, cz - 16)]:
        if c.get(x, 2, z) == HERBE:
            c.set(x, 3, z, FIBRES if (x + z) % 2 else FLEUR)
    c.sign_standing(cx - 2, 3, cz + 21, "minecraft:dark_oak", 0, ["Port-Nacre", "quai sud :", "Quai des Falaises", "marché flottant"])
    return c


def build_phare_du_dessous() -> Canvas:
    """Phare sur le rebord de falaises : tour de pierre, lanterne de cristal, galerie basse vitrée donnant sous l'eau."""
    W, H, D = 11, 27, 11
    c = Canvas("phare_du_dessous", W, H, D)
    c.note("Phare du Dessous : tour de pierre nacrée sur la falaise de la Mer, feu de cristal, galerie vitrée qui regarde sous la surface ; le gardien Anselme.")
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    for y in range(1, 20):
        r = 4 if y < 14 else 3
        for x in range(W):
            for z in range(D):
                d = math.hypot(x - 5, z - 5)
                if d <= r + 0.5:
                    c.set(x, y, z, AIR if d < r - 0.6 else (PIERRE_P if y % 5 == 0 else PIERRE_T))
    door(c, 5, 1, 9, "south")
    for y in (5, 10, 15):
        window(c, 5, y, 1, "north", 1)
        window(c, 1, y, 5, "west", 1)
        window(c, 9, y, 5, "east", 1)
    # escalier intérieur en colimaçon
    marches = [(3, 3, "east"), (4, 3, "east"), (5, 3, "east"), (6, 3, "east"), (7, 4, "south"), (7, 5, "south"), (7, 6, "south"), (7, 7, "south"),
               (6, 7, "west"), (5, 7, "west"), (4, 7, "west"), (3, 7, "west"), (3, 6, "north"), (3, 5, "north"), (3, 4, "north"), (3, 3, "north"),
               (4, 3, "east"), (5, 3, "east")]
    for i, (x, z, f) in enumerate(marches):
        c.set(x, 1 + i, z, stair(STAIR_PIERRE, f))
    # chambre du gardien au rez-de-chaussée
    c.bed(2, 1, 5, "blue", "south") if False else None
    c.set(2, 1, 6, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.set(5, 1, 5, "minecraft:lectern[facing=south,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Carnet du phare", "Anselme", [
            "Le phare ne guide pas les bateaux : il guide ceux qui regardent dessous. La galerie du bas donne sous la surface ; on y voit passer les bancs.",
            "Quand la mer se plisse (une Arrivée), le feu change de couleur. J'ai noté chaque fois. Lisbeth, à Clairval, tient le même registre."])})
    # feu : plate-forme vitrée et cristal
    c.fill(2, 20, 2, 8, 20, 8, PIERRE_P)
    c.fill(3, 20, 3, 7, 20, 7, PLANCHES)
    for x in range(2, 9):
        for z in range(2, 9):
            if x in (2, 8) or z in (2, 8):
                c.set(x, 21, z, VERRE); c.set(x, 22, z, VERRE)
    c.fill(2, 23, 2, 8, 23, 8, slab(DALLE_PIERRE))
    c.set(5, 21, 5, CRISTAL); c.set(5, 22, 5, CRISTAL)
    c.set(5, 24, 5, LANTERNE)
    c.set(7, 20, 3, AIR)
    data_marker(c, 4, 1, 3, "pnj:anselme")
    # Galerie basse : couloir en verre partant du pied du phare vers l'extérieur (côté nord), sous la crête
    for x in range(4, 7):
        for z in range(0, 2):
            c.set(x, 1, z, VERRE); c.set(x, 2, z, AIR); c.set(x, 3, z, VERRE)
    c.set(5, 1, 2, AIR)
    c.sign_wall(6, 2, 9, "minecraft:dark_oak", "south", ["Phare du", "Dessous", "regardez", "sous la mer"])
    return c


def build_marche_flottant() -> Canvas:
    """Marché flottant : radeaux de planches reliés par des passerelles, étals, lanternes ; posé sur l'eau des lagons (y local 0 = monde 62)."""
    W, H, D = 25, 8, 25
    c = Canvas("marche_flottant", W, H, D)
    c.note("Marché flottant des lagons : quatre radeaux reliés, étals d'objets arrivés d'ailleurs, marchande Nérine, barques.")
    radeaux = [(2, 2), (14, 2), (2, 14), (14, 14)]
    for rx, rz in radeaux:
        c.fill(rx, 0, rz, rx + 8, 0, rz + 8, BUCHE_SOMBRE)   # flotteurs dans l'eau
        c.fill(rx, 1, rz, rx + 8, 1, rz + 8, PLANCHES)
        for x, z in [(rx, rz), (rx + 8, rz), (rx, rz + 8), (rx + 8, rz + 8)]:
            c.set(x, 2, z, BARRIERE); c.set(x, 3, z, LANTERNE)
    # passerelles
    c.fill(11, 1, 5, 13, 1, 6, BOIS_SOMBRE)
    c.fill(11, 1, 17, 13, 1, 18, BOIS_SOMBRE)
    c.fill(5, 1, 11, 6, 1, 13, BOIS_SOMBRE)
    c.fill(17, 1, 11, 18, 1, 13, BOIS_SOMBRE)
    # étals
    for i, (ex, ez) in enumerate([(4, 4), (16, 4), (4, 16), (16, 16)]):
        c.fill(ex, 2, ez, ex + 4, 2, ez, "minecraft:dark_oak_slab[type=top,waterlogged=false]")
        c.set(ex, 3, ez, BUCHE_SOMBRE); c.set(ex + 4, 3, ez, BUCHE_SOMBRE)
        c.fill(ex, 4, ez - 1, ex + 4, 4, ez + 1, ["minecraft:light_blue_wool", "minecraft:white_wool", "minecraft:cyan_wool", "minecraft:magenta_wool"][i])
        c.item_frame(ex + 2, 3, ez, "south", ["minecraft:clock", "les_limbes:perle_de_nacre", "minecraft:spyglass", "les_limbes:journal_de_bord"][i])
    c.set(6, 2, 6, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/marche_flottant"})
    data_marker(c, 18, 2, 6, "pnj:nerine")
    data_marker(c, 6, 2, 18, "pnj:marchand_flottant")
    c.sign_standing(12, 2, 12, "minecraft:dark_oak", 0, ["Marché", "flottant", "objets venus", "d'ailleurs"])
    c.fill(10, 1, 10, 14, 1, 14, PLANCHES)
    return c


def build_navire_perdu() -> Canvas:
    """Navire perdu monumental, couché sur le fond (y local 0 = monde 39) : coque, trois mâts hors de l'eau, cargaison, journal."""
    W, H, D = 21, 44, 59
    c = Canvas("navire_perdu", W, H, D)
    c.note("Navire perdu : grand vaisseau d'un autre monde reposant sur le fond des récifs ; mâts émergés, cale inondée pleine de cargaison, journal du bord.")
    # coque : section elliptique le long de z, fond à y 0..2, pont à y 14 (monde 53, sous l'eau)
    for z in range(2, D - 2):
        t = (z - 2) / (D - 5)
        demi = int(round(9 * math.sin(math.pi * min(1.0, max(0.0, t))) ** 0.6)) if 0.02 < t < 0.98 else 1
        for y in range(0, 15):
            lx = int(round(demi * (0.35 + 0.65 * min(1.0, y / 10.0))))
            for x in range(10 - lx, 11 + lx):
                bord = x in (10 - lx, 10 + lx) or y == 0 or y == 14
                if bord:
                    c.set(x, y, z, BOIS_SOMBRE if y != 14 else PLANCHES)
                elif y < 14:
                    c.set(x, y, z, EAU)
    # cale : caisses et coffres
    for z, x in [(12, 7), (12, 13), (24, 9), (30, 11), (40, 8), (46, 12)]:
        c.set(x, 1, z, "minecraft:chest[facing=north,type=single,waterlogged=true]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/navire_perdu"})
        c.set(x + 1, 1, z, "minecraft:barrel[facing=up,open=false]")
    # pont : rambardes, mâts (hors de l'eau jusqu'à y 40 = monde 79), cabine arrière
    for z in range(4, D - 4):
        c.set(2, 15, z, "minecraft:dark_oak_fence[east=false,north=true,south=true,waterlogged=true,west=false]")
        c.set(18, 15, z, "minecraft:dark_oak_fence[east=false,north=true,south=true,waterlogged=true,west=false]")
    for mz, haut in [(14, 40), (30, 43), (46, 38)]:
        c.column(10, 15, haut, mz, BUCHE_SOMBRE)
        c.fill(5, haut - 8, mz, 15, haut - 8, mz, BUCHE_SOMBRE)
        c.fill(6, haut - 7, mz, 14, haut - 1, mz, "minecraft:white_wool")
        c.set(10, haut + 1, mz, "minecraft:white_banner[rotation=0]") if False else None
    # cabine arrière (émergée partiellement : pont à 53, cabine 53..57 ; le haut dépasse à peine)
    c.fill(5, 15, 48, 15, 15, 56, PLANCHES)
    c.walls(5, 16, 48, 15, 19, 56, BOIS_SOMBRE)
    c.fill(6, 16, 49, 14, 19, 55, EAU)
    c.fill(5, 20, 48, 15, 20, 56, PLANCHES)
    c.set(10, 16, 52, "minecraft:lectern[facing=north,has_book=true,powered=false,waterlogged=true]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Journal du bord", "Capitaine inconnu", [
            "Jour 40. La tempête nous a pris au large. Quand elle s'est ouverte, le ciel était nacré et la mer tenait toute seule dans le vide.",
            "Jour 41. Nous avons touché un récif qui n'était pas sur la carte. L'eau monte dans la cale mais ne coule pas : elle reste. Tout reste, ici.",
            "Jour 44. Les hommes sont partis vers une île au feu de cristal. J'ai laissé la cargaison. Celui qui lira ceci : prenez ce qu'il faut, et racontez."])})
    c.set(8, 16, 50, "minecraft:chest[facing=east,type=single,waterlogged=true]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/navire_perdu"})
    return c


def build_sanctuaire_des_bancs() -> Canvas:
    """Sanctuaire sous-marin : dôme de verre et pierre polie sur le fond, autel, bancs de poissons-lanternes (apparitions)."""
    W, H, D = 17, 14, 17
    c = Canvas("sanctuaire_des_bancs", W, H, D)
    c.note("Sanctuaire des bancs : dôme vitré au fond des lagons, autel de nacre, tunnel d'accès vertical ; les bancs lumineux le traversent.")
    cx, cz = 8, 8
    for y in range(0, 10):
        for x in range(W):
            for z in range(D):
                d = math.sqrt((x - cx) ** 2 + (z - cz) ** 2 + (y - 1) ** 2 * 1.4)
                if d <= 8.5:
                    if d >= 7.4:
                        c.set(x, y, z, PIERRE_P if y <= 1 else (PIERRE_T if (x + z) % 3 == 0 else VERRE))
                    else:
                        c.set(x, y, z, PIERRE_P if y == 1 else AIR)
    c.set(cx, 2, cz, PIERRE_S)
    c.set(cx, 3, cz, CRISTAL)
    for x, z in [(cx - 3, cz - 3), (cx + 3, cz - 3), (cx - 3, cz + 3), (cx + 3, cz + 3)]:
        c.set(x, 2, z, CORAIL if (x + z) % 2 else CORAIL_AZUR)
        c.set(x, 3, z, LANTERNE)
    # tunnel vertical d'accès (verre) jusqu'au-dessus de l'eau : y local 10..13 (monde 53..56) ; le reste est de l'eau libre
    for y in range(9, H):
        for x in range(cx - 1, cx + 2):
            for z in range(cz - 1, cz + 2):
                c.set(x, y, z, AIR if (x == cx and z == cz) else VERRE)
    c.set(cx, 9, cz, "minecraft:ladder[facing=north,waterlogged=false]")
    for y in range(10, H):
        c.set(cx, y, cz, "minecraft:ladder[facing=north,waterlogged=false]")
    c.sign_wall(cx + 1, 3, cz + 6, "minecraft:dark_oak", "north", ["Sanctuaire", "des bancs", "ils passent", "à l'aube"])
    data_marker(c, cx - 2, 2, cz - 5, "entite:les_limbes:poisson_lune")
    data_marker(c, cx + 2, 2, cz + 5, "entite:les_limbes:poisson_lune")
    return c


def build_quai_des_falaises() -> Canvas:
    """Fin de la route de la Mer : belvédère sur la falaise, escalier taillé jusqu'à un ponton, barques, panneau."""
    W, H, D = 15, 24, 23
    c = Canvas("quai_des_falaises", W, H, D)
    c.note("Quai des Falaises : terminus de la route depuis Clairval, escalier de pierre descendant la falaise jusqu'au ponton ; passeur Bastien.")
    # y local 0 = monde 62 (eau à 63 = y1), crête à monde 82 = y 20 ; le gabarit s'enfonce dans la falaise aplanie
    c.fill(0, 19, 12, W - 1, 19, D - 1, PIERRE_T)      # belvédère (monde 81 = surface du plateau à 82-1)
    c.fill(1, 19, 13, W - 3, 19, D - 2, PIERRE_P)
    for x in range(0, W):
        c.set(x, 20, 12, "les_limbes:muret_de_pierre_nacree[east=low,north=none,south=none,up=true,waterlogged=false,west=low]")
    c.set(7, 20, 12, AIR)
    lamppost(c, 1, 20, 21, 3); lamppost(c, W - 2, 20, 21, 3)
    c.sign_standing(3, 20, 15, "minecraft:dark_oak", 8, ["Quai des", "Falaises", "Port-Nacre", "par la mer"])
    # escalier taillé dans la falaise (côté nord), marches de pierre en zigzag jusqu'au ponton
    y = 19
    x, z = 7, 11
    while y > 2:
        c.set(x, y, z, stair(STAIR_PIERRE, "south"))
        c.set(x, y + 1, z, AIR); c.set(x, y + 2, z, AIR)
        y -= 1
        z -= 1 if z > 1 else 0
    # ponton de bois à la surface de l'eau (monde 64 = y 2)
    c.fill(3, 2, 0, 11, 2, 4, BOIS_SOMBRE)
    for x in (3, 11):
        c.set(x, 3, 0, BARRIERE); c.set(x, 4, 0, LANTERNE)
    c.set(6, 2, 1, AIR); c.set(7, 2, 1, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/quai"})
    data_marker(c, 9, 3, 2, "pnj:bastien")
    c.set(5, 2, 2, "minecraft:barrel[facing=up,open=false]")
    return c


MER = {
    "mer/port_nacre_ile": build_port_nacre_ile,
    "mer/phare_du_dessous": build_phare_du_dessous,
    "mer/marche_flottant": build_marche_flottant,
    "mer/navire_perdu": build_navire_perdu,
    "mer/sanctuaire_des_bancs": build_sanctuaire_des_bancs,
    "mer/quai_des_falaises": build_quai_des_falaises,
}
