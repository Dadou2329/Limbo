#!/usr/bin/env python3
"""Construit les structures des Limbes (gabarits NBT) et les fichiers JSON de placement associés.

Usage : python3 tools/structgen/build_structures.py [--rapport]

Produit :
- data/les_limbes/structure/**/*.nbt (gabarits)
- data/les_limbes/worldgen/structure/{sanctuaire,clairval,manoir,relais_royal}.json (structures composées)
- data/les_limbes/worldgen/structure_set/*.json (placement fixe)
- tools/structgen/rapport/ : statistiques, vues ASCII et rendus PNG (inspection)

Conventions : x+ = est, z+ = sud. Les coordonnées monde des sites sont celles de LimbesSites.java.
"""
from __future__ import annotations

import json
import os
import sys
import zlib

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from builders import (BARRIERE, BUCHE, CHEMIN, COBBLE, CRISTAL, DALLE_BOIS, DALLE_PIERRE, FEUILLES, FIBRES, FLEUR, GRAVIER,  # noqa: E402
                      HERBE, HERBE_AUBE, LANTERNE, LANTERNE_H, MOUSSE, MURET, PIERRE, PIERRE_P, PIERRE_S, PIERRE_T, PLANCHES, PORTE,
                      STAIR_BOIS, STAIR_PIERRE, TERRE, VERRE, bookshelves, box_room, chair, corner_logs, counter, data_marker, door,
                      fence_rect, fill_void_with, fountain, garden_bed, ground, hanging_lantern, lamppost, log, path, slab, stair, table,
                      tree, window)
from canvas import AIR, VOID, Canvas
from regions_mer import MER  # noqa: E402
from regions_jardins import JARDINS  # noqa: E402
from regions_desert import DESERT  # noqa: E402
from regions_montagnes import MONTAGNES  # noqa: E402
from regions_couronne import COURONNE  # noqa: E402
from arenes import ARENES  # noqa: E402
from capitale import CAPITALE  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "les_limbes")
STRUCT_DIR = os.path.join(DATA, "structure")
RAPPORT_DIR = os.path.join(ROOT, "tools", "structgen", "rapport")

# Sites (monde) : doivent correspondre à LimbesSites.java
SOL = 66            # altitude où l'on se tient sur les plateaux (bloc de surface à 65)
SANCTUAIRE = (0, 82, 0)
CLAIRVAL = (0, SOL, 140)
MANOIR = (-120, SOL, 150)
RELAIS = (150, SOL, 60)
PIED_DU_PONT = (0, SOL, 56)


def chunk_of(x: int, z: int):
    return (x >> 4, z >> 4)


# ============================================================================== VESTIGE (Overworld)
def build_vestige() -> Canvas:
    c = Canvas("vestige", 17, 13, 17)
    c.note("Vestige des Limbes : petite ruine de pierre nacrée avec une porte dormante, un pupitre et une cave cachée.")
    # Sous-sol (y 0..3) : masse de pierre sous la ruine + cave
    c.fill(2, 0, 2, 14, 3, 14, PIERRE)
    c.fill(7, 1, 5, 13, 3, 11, COBBLE)          # parois de la cave
    c.fill(8, 1, 6, 12, 3, 10, AIR)             # intérieur 5x3x5
    c.fill(8, 0, 6, 12, 0, 10, MOUSSE)
    c.chest(9, 1, 6, "south", loot_table="les_limbes:chests/vestige_cave")
    c.set(11, 1, 10, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.set(10, 1, 10, "minecraft:barrel[facing=up,open=false]", {"id": "minecraft:barrel", "LootTable": "les_limbes:chests/vestige_cave"})
    c.set(8, 1, 10, "minecraft:cobweb")
    for y in range(1, 4):
        c.set(12, y, 8, "minecraft:ladder[facing=west,waterlogged=false]")
    # Sol de la cour (y=4) : dalles anciennes, mousse, herbe aux bords
    for x in range(0, 17):
        for z in range(0, 17):
            if 3 <= x <= 13 and 3 <= z <= 13:
                st = [PIERRE_T, PIERRE_T, "minecraft:cracked_stone_bricks", "minecraft:mossy_stone_bricks", PIERRE_P][(x * 7 + z * 13) % 5]
                c.set(x, 4, z, st)
            elif 1 <= x <= 15 and 1 <= z <= 15:
                c.set(x, 4, z, "minecraft:grass_block[snowy=false]")
    c.trapdoor(12, 4, 8, "minecraft:oak", "west", half="top")   # accès à la cave (sous une dalle)
    # Arche de la porte (côté nord, z=3) : piliers, linteau sculpté, voile dormant 3x3
    for y in range(5, 10):
        c.set(6, y, 3, PIERRE_T)
        c.set(10, y, 3, PIERRE_T)
    c.fill(6, 10, 3, 10, 10, 3, PIERRE_T)
    c.set(8, 10, 3, PIERRE_S)
    c.set(7, 9, 3, stair(STAIR_PIERRE, "east", "top"))
    c.set(9, 9, 3, stair(STAIR_PIERRE, "west", "top"))
    for x in range(7, 10):
        for y in range(5, 8):
            c.set(x, y, 3, f"{PORTE}[active=false,axis=x]")
    c.set(8, 8, 3, PIERRE_P)
    c.set(7, 8, 3, PIERRE_T)
    c.set(9, 8, 3, PIERRE_T)
    c.set(6, 11, 3, LANTERNE)
    c.set(10, 11, 3, LANTERNE)
    # Murs en ruine (ouest, est) de hauteurs variables, sud avec entrée
    heights = [3, 2, 3, 1, 0, 1, 2, 3, 2, 1, 1]
    for i, z in enumerate(range(3, 14)):
        for y in range(5, 5 + heights[i]):
            c.set(3, y, z, MOUSSE if y == 5 else PIERRE_T)
        for y in range(5, 5 + heights[(i * 3) % len(heights)]):
            c.set(13, y, z, MOUSSE if y == 5 else PIERRE_T)
    for x in range(3, 14):
        if x in (7, 8, 9):
            continue
        for y in range(5, 5 + [2, 1, 3, 2, 1, 0, 0, 0, 1, 2, 1][x - 3]):
            c.set(x, y, 13, PIERRE_T if y > 5 else MOUSSE)
    for x, z in [(3, 3), (13, 3), (3, 13), (13, 13)]:
        c.column(x, 5, 7, z, PIERRE_T)
        c.set(x, 8, z, slab(DALLE_PIERRE))
    # Mobilier : pupitre avec journal, coffre, végétation nacrée
    pages = [
        "Journal du gardien\n\nJe suis resté pour veiller sur la porte. Elle dort depuis que les routes royales se sont éteintes, mais elle n'est pas morte.",
        "Trois éclats de nacre, tenus contre la pierre gravée, suffisent à la réveiller. J'en ai laissé dans le coffre.\n\nCherchez aussi sous les dalles : j'y ai caché ce que j'ai pu sauver.",
        "De l'autre côté, on arrive dans une prairie d'argent, sous un ciel de nacre. Quelqu'un vous y attend. N'ayez pas peur.",
    ]
    c.lectern(5, 5, 10, "east", Canvas.written_book("Journal du gardien", "Le gardien du vestige", pages))
    c.chest(11, 5, 10, "west", loot_table="les_limbes:chests/vestige")
    for x, z in [(4, 4), (12, 4), (4, 12), (12, 12), (6, 11)]:
        c.set(x, 4, z, TERRE)
        c.set(x, 5, z, FIBRES)
    c.set(8, 4, 8, PIERRE_S)
    c.sign_standing(9, 5, 12, "minecraft:oak", 0, ["Vestige", "des Limbes", "— la porte dort —", "trois éclats"])
    return c


# ============================================================================== SANCTUAIRE : ÎLE
def build_ile() -> Canvas:
    W, H, D = 41, 24, 41
    c = Canvas("ile", W, H, D)
    c.note("Sanctuaire de l'Arrivée : île flottante, esplanade circulaire, arche de retour, Maëlle.")
    cx, cz = 20, 20
    SURF = 14  # y local de la surface (bloc de sol) ; on se tient à SURF+1
    R = 19
    for x in range(W):
        for z in range(D):
            d = ((x - cx) ** 2 + (z - cz) ** 2) ** 0.5
            if d > R:
                continue
            t = max(0.0, 1.0 - (d / R) ** 2)
            depth = int(round(12 * t ** 0.65))
            bottom = SURF - depth
            for y in range(bottom, SURF + 1):
                if y == SURF:
                    c.set(x, y, z, HERBE)
                elif y >= SURF - 2:
                    c.set(x, y, z, TERRE)
                else:
                    c.set(x, y, z, PIERRE if (x * 3 + y * 5 + z * 7) % 11 else CRISTAL)
    # Esplanade circulaire (rayon 9) en pierre taillée, anneaux polis, centre sculpté
    for x in range(W):
        for z in range(D):
            d = ((x - cx) ** 2 + (z - cz) ** 2) ** 0.5
            if d <= 9.5:
                ring = int(d) % 3
                c.set(x, SURF, z, PIERRE_P if ring == 1 else PIERRE_T)
    c.set(cx, SURF, cz, PIERRE_S)
    for dx, dz in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
        c.set(cx + dx, SURF, cz + dz, PIERRE_P)
    # Bordure de l'esplanade : murets bas et lampadaires
    import math
    for i in range(16):
        a = i / 16 * 2 * math.pi
        x = int(round(cx + math.cos(a) * 10))
        z = int(round(cz + math.sin(a) * 10))
        if i % 2 == 0:
            lamppost(c, x, SURF + 1, z, 2)
        else:
            c.set(x, SURF + 1, z, MURET)
    # Arche de retour (nord) : monde (0,84,-8) => local (20, 16, 12) ; voile 3x3 actif, plan est-ouest (axis=x)
    ax, az = 20, 12
    for y in range(SURF + 1, SURF + 6):
        c.set(ax - 2, y, az, PIERRE_T)
        c.set(ax + 2, y, az, PIERRE_T)
    c.fill(ax - 2, SURF + 6, az, ax + 2, SURF + 6, az, PIERRE_T)
    c.set(ax, SURF + 6, az, PIERRE_S)
    c.set(ax - 1, SURF + 5, az, stair(STAIR_PIERRE, "east", "top"))
    c.set(ax + 1, SURF + 5, az, stair(STAIR_PIERRE, "west", "top"))
    c.set(ax, SURF + 5, az, PIERRE_P)
    c.set(ax, SURF + 4, az, PIERRE_P)
    c.set(ax - 1, SURF + 4, az, PIERRE_T)
    c.set(ax + 1, SURF + 4, az, PIERRE_T)
    for x in range(ax - 1, ax + 2):
        for y in range(SURF + 1, SURF + 4):
            c.set(x, y, az, f"{PORTE}[active=true,axis=x]")
    c.set(ax - 2, SURF + 7, az, LANTERNE)
    c.set(ax + 2, SURF + 7, az, LANTERNE)
    c.set(ax - 3, SURF + 1, az, slab(DALLE_PIERRE))
    c.set(ax + 3, SURF + 1, az, slab(DALLE_PIERRE))
    # Dalles d'arrivée devant l'arche (sud de l'arche) : monde (0,83,-3.5) => local (20, SURF+1, 16..17)
    c.fill(ax - 1, SURF, az + 1, ax + 1, SURF, az + 6, PIERRE_P)
    # Maëlle : monde (3,83,-2) => local (23, SURF+1, 18)
    data_marker(c, 23, SURF + 1, 18, "pnj:maelle")
    # Porte de relais du Sanctuaire (ouest de l'esplanade) : monde (-8,83,0) => local (12, SURF+1, 20) ; arrivée en (-6,83,0)
    c.set(12, SURF + 1, 20, "les_limbes:porte_de_relais[eveillee=false,facing=east]", {"id": "les_limbes:porte_de_relais", "relais": "sanctuaire"})
    c.set(12, SURF + 2, 20, AIR)
    # Bancs, fleurs et arbres
    for x, z, f in [(14, 26, "east"), (26, 26, "west"), (14, 14, "east"), (26, 14, "west")]:
        c.set(x, SURF + 1, z, stair(STAIR_BOIS, f))
        c.set(x, SURF + 1, z + 1, stair(STAIR_BOIS, f))
    for x, z in [(8, 8), (32, 8), (8, 32), (32, 32)]:
        tree(c, x, SURF + 1, z, 5, 2)
    for x, z in [(6, 20), (34, 20), (20, 33), (12, 30), (28, 30), (11, 10), (29, 10)]:
        if c.get(x, SURF, z) == HERBE:
            c.set(x, SURF + 1, z, FIBRES)
    for x, z in [(10, 24), (30, 24), (16, 6), (24, 6), (5, 17), (35, 23)]:
        if c.get(x, SURF, z) == HERBE:
            c.set(x, SURF + 1, z, FLEUR)
    # Porte du pont (sud) : deux piliers à lanternes, x 18..22 => local, z 36..38
    for x in (17, 23):
        c.column(x, SURF + 1, SURF + 4, 37, PIERRE_T)
        c.set(x, SURF + 5, 37, LANTERNE)
    c.fill(18, SURF, 30, 22, SURF, 40, PIERRE_T)
    c.line(20, SURF, 30, 20, 40, PIERRE_P)
    for z in range(30, 41):
        c.set(17, SURF + 1, z, MURET)
        c.set(23, SURF + 1, z, MURET)
    # Sous le pont au bord : console en escaliers
    for x in range(18, 23):
        c.set(x, SURF - 1, 40, stair(STAIR_PIERRE, "north", "top"))
    # Panneau d'accueil
    c.sign_standing(24, SURF + 1, 26, "minecraft:oak", 8, ["Sanctuaire", "de l'Arrivée", "↓ Clairval ↓", "par le pont"])
    return c


# ============================================================================== SANCTUAIRE : PONT
def build_pont() -> Canvas:
    # monde : x -3..3 ; z 18..56 ; y 60..90 ; local x 0..6, z 0..38, y 0..30
    L = 39
    c = Canvas("pont", 7, 31, L)
    c.note("Grand pont descendant de l'île du Sanctuaire vers les Prairies (pente ~0,4, escaliers de pierre nacrée).")

    def floor_y(z: int) -> int:
        return 21 - (16 * z) // (L - 1)   # 21 (monde 81) -> 5 (monde 65)

    prev = floor_y(0) + 1
    for z in range(L):
        y = floor_y(z)
        for x in range(1, 6):
            c.set(x, y, z, PIERRE_P if x == 3 else PIERRE_T)
            if y < prev:  # marche descendante : escalier au niveau bas, plein côté nord
                c.set(x, y + 1, z, AIR)
                c.set(x, y, z, stair(STAIR_PIERRE, "north"))
                c.set(x, y - 1, z, PIERRE_T)
        c.set(0, y, z, PIERRE_T)
        c.set(6, y, z, PIERRE_T)
        c.set(0, y + 1, z, MURET)
        c.set(6, y + 1, z, MURET)
        if z % 6 == 3:
            c.set(0, y + 2, z, LANTERNE)
            c.set(6, y + 2, z, LANTERNE)
        # Dégagement au-dessus de la chaussée
        for x in range(1, 6):
            for yy in range(y + 1, min(30, y + 6)):
                if c.get(x, yy, z) == VOID:
                    c.set(x, yy, z, AIR)
        # Piliers et arches de soutien
        if z % 6 == 0:
            for x in (1, 5):
                for yy in range(5, y):
                    c.set(x, yy, z, PIERRE_T)
                c.set(x, 4, z, PIERRE_T)
            for x in range(2, 5):
                c.set(x, y - 1, z, stair(STAIR_PIERRE, "south" if x == 2 else "north", "top") if x != 3 else PIERRE_T)
        prev = y
    # Soubassement au pied du pont (z = 38, monde 56) : palier
    c.fill(0, 5, 36, 6, 5, 38, PIERRE_T)
    return c


# ============================================================================== CLAIRVAL
def build_clairval_sol() -> Canvas:
    # monde x -40..39, z 50..149 ; local y0 = monde 65 (bloc de surface)
    c = Canvas("sol", 80, 10, 112)
    c.note("Sol de Clairval : route depuis le pont, place centrale avec fontaine, chemins, lampadaires, jardins, Vaëron.")
    ox, oz = -40, 50

    def L(wx, wz):
        return wx - ox, wz - oz

    # Route principale nord-sud, du pied du pont (z=56) à la place (z=130)
    for wz in range(56, 131):
        x, z = L(0, wz)
        path(c, x - 2, z, x + 2, z, 0, CHEMIN)
        c.set(x, 0, z, GRAVIER)
        c.fill(x - 2, 1, z, x + 2, 2, z, AIR)
        if wz % 8 == 0:
            lamppost(c, x - 3, 1, z, 3)
            lamppost(c, x + 3, 1, z, 3)
    # Place centrale 21x21 (monde x -10..10, z 130..150)
    for wx in range(-10, 11):
        for wz in range(130, 151):
            x, z = L(wx, wz)
            d = max(abs(wx), abs(wz - 140))
            c.set(x, 0, z, PIERRE_P if d % 4 == 0 else PIERRE_T)
            c.set(x, 1, z, AIR)
            c.set(x, 2, z, AIR)
    x, z = L(0, 140)
    fountain(c, x, 0, z)
    for wx, wz in [(-8, 132), (8, 132), (-8, 148), (8, 148)]:
        lamppost(c, L(wx, wz)[0], 1, L(wx, wz)[1], 3)
    for wx, wz, f in [(-4, 134, "south"), (4, 134, "south"), (-4, 146, "north"), (4, 146, "north")]:
        xx, zz = L(wx, wz)
        c.set(xx, 1, zz, stair(STAIR_BOIS, f))
        c.set(xx + 1, 1, zz, stair(STAIR_BOIS, f))
    # Massifs fleuris aux coins de la place
    for wx, wz in [(-9, 131), (9, 131), (-9, 149), (9, 149)]:
        xx, zz = L(wx, wz)
        c.set(xx, 0, zz, TERRE)
        c.set(xx, 1, zz, FLEUR)
    # Chemins vers les bâtiments
    for wx in range(10, 18):      # chemin place -> porte de l'auberge (porte en (15..16, 130))
        xx, zz = L(wx, 131)
        path(c, xx, zz, xx, zz + 1, 0)
    for wx in range(10, 15):      # vers la forge (est)
        xx, zz = L(wx, 144)
        path(c, xx, zz, xx, zz + 1, 0)
    for wx in range(-19, -10):    # vers maison 1 (porte en (-18, 132))
        xx, zz = L(wx, 133)
        path(c, xx, zz, xx, zz + 1, 0)
    for wx in range(-17, -9):     # vers maison 2 (porte en (-16, 154))
        xx, zz = L(wx, 155)
        path(c, xx, zz, xx, zz + 1, 0)
    for wz in range(150, 157):
        xx, zz = L(-10, wz)
        path(c, xx, zz, xx + 1, zz, 0)
    for wz in range(150, 156):    # vers maison 3 (sud)
        xx, zz = L(8, wz)
        path(c, xx, zz, xx + 1, zz, 0)
    for wx in range(-28, -10):    # vers le moulin (porte en (-28, 125))
        xx, zz = L(wx, 127)
        path(c, xx, zz, xx, zz + 1, 0)
    for wz in range(126, 128):
        xx, zz = L(-28, wz)
        path(c, xx, zz, xx, zz, 0)
    for wx in range(-40, -22):
        xx, zz = L(wx, 140)
        path(c, xx, zz, xx, zz + 1, 0)
    for wx in range(22, 40):      # amorce est vers le relais
        xx, zz = L(wx, 139)
        path(c, xx, zz, xx, zz + 1, 0)
    # Arbres et potagers
    for wx, wz in [(-30, 136), (30, 134), (-6, 118), (6, 118), (26, 150), (-28, 150)]:
        tree(c, L(wx, wz)[0], 1, L(wx, wz)[1], 5, 2)
    xx, zz = L(16, 110)
    garden_bed(c, xx, 0, zz, xx + 5, zz + 3)
    fence_rect(c, xx - 1, 1, zz - 1, xx + 6, zz + 4, (xx + 2, zz - 1, "north"))
    xx, zz = L(-20, 110)
    garden_bed(c, xx, 0, zz, xx + 4, zz + 4, HERBE_AUBE)
    fence_rect(c, xx - 1, 1, zz - 1, xx + 5, zz + 5, (xx + 2, zz + 5, "south"))
    # Porte de relais de Clairval, au sud de la place : monde (0,66,149) ; arrivée en (0,66,147)
    xx, zz = L(0, 149)
    c.set(xx, 1, zz, "les_limbes:porte_de_relais[eveillee=false,facing=north]", {"id": "les_limbes:porte_de_relais", "relais": "clairval"})
    c.set(xx, 2, zz, AIR)
    # Vaëron sur la place (monde (3, 66, 142)), panneau de bienvenue à l'entrée nord ; Théo au pied du pont
    data_marker(c, L(3, 142)[0], 1, L(3, 142)[1], "pnj:vaeron")
    data_marker(c, L(4, 62)[0], 1, L(4, 62)[1], "pnj:theo")
    xx, zz = L(3, 127)
    c.sign_standing(xx, 1, zz, "minecraft:oak", 8, ["Clairval", "Auberge du Moulin →", "Forge : est", "Manoir : ouest"])
    return c


def build_auberge() -> Canvas:
    # monde coin (6, 65, 122) ; 16 x 13 x 13 ; entrée au sud (z local 12) vers la place
    c = Canvas("auberge", 18, 19, 15)
    c.shift = (1, 0, 1)
    c.note("Auberge du Moulin : salle commune avec comptoir et cheminée, cuisine, réserve ; trois chambres à l'étage ; bains. Thibault.")
    W, D = 16, 13
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)             # soubassement visible
    c.fill(1, 0, 1, W - 2, 0, D - 2, PLANCHES)             # plancher
    c.walls(0, 1, 0, W - 1, 4, D - 1, PLANCHES)
    c.fill(1, 1, 1, W - 2, 4, D - 2, AIR)
    corner_logs(c, 0, 1, 0, W - 1, D - 1, 3)
    c.fill(0, 1, 0, W - 1, 1, D - 1, PIERRE_T)              # bandeau de pierre au rez-de-chaussée
    c.fill(1, 1, 1, W - 2, 1, D - 2, AIR)
    # Étage : plancher à y=5, murs y 6..8, toit à partir de y 9
    c.fill(0, 5, 0, W - 1, 5, D - 1, PLANCHES)
    c.walls(0, 6, 0, W - 1, 8, D - 1, PLANCHES)
    c.fill(1, 6, 1, W - 2, 8, D - 2, AIR)
    corner_logs(c, 0, 6, 0, W - 1, D - 1, 2)
    c.gable_roof(0, 0, W - 1, D - 1, 9, STAIR_BOIS, axis="x", overhang=1)
    # Fenêtres rez-de-chaussée (sud et nord), étage
    for x in (3, 7, 12):
        window(c, x, 2, D - 1, "south", 2)
        window(c, x, 2, 0, "north", 2)
        window(c, x, 7, D - 1, "south", 2)
        window(c, x, 7, 0, "north", 2)
    for z in (3, 8):
        window(c, 0, 2, z, "west", 2)
        window(c, W - 1, 2, z, "east", 2)
        window(c, 0, 7, z, "west", 2)
        window(c, W - 1, 7, z, "east", 2)
    # Entrée sud (porte double) + auvent
    door(c, 9, 1, D - 1, "south")
    door(c, 10, 1, D - 1, "south")
    c.set(9, 3, D - 1, PLANCHES)
    c.set(10, 3, D - 1, PLANCHES)
    for x in range(8, 12):
        c.set(x, 4, D, slab(DALLE_BOIS, "top")) if c.inside(x, 4, D) else None
    c.sign_wall(8, 3, D - 1, "minecraft:oak", "south", ["Auberge", "du Moulin", "chambres", "& bouillon"])
    # Salle commune (x 1..10, z 1..11) : comptoir, cheminée, tables
    counter(c, 2, 1, 2, 6, "x")
    c.set(2, 1, 1, "minecraft:barrel[facing=up,open=false]")
    c.set(3, 1, 1, "minecraft:barrel[facing=up,open=false]")
    c.set(5, 1, 1, "minecraft:smoker[facing=south,lit=false]")
    c.set(7, 1, 1, "minecraft:cauldron")
    c.set(1, 1, 1, "minecraft:lantern[hanging=false,waterlogged=false]")
    # cheminée ouest
    c.fill(0, 1, 5, 0, 3, 7, PIERRE_T)
    c.set(1, 1, 6, "minecraft:campfire[facing=east,lit=true,signal_fire=false,waterlogged=false]")
    c.column(1, 2, 4, 6, AIR)
    c.fill(0, 4, 5, 0, 8, 7, PIERRE_T)
    c.set(0, 9, 6, PIERRE_T)
    c.set(0, 10, 6, PIERRE_T)
    for x, z in [(3, 5), (3, 9), (7, 5), (7, 9)]:
        table(c, x, 1, z, 2, "x")
        chair(c, x - 1, 1, z, "east")
        chair(c, x + 2, 1, z, "west")
    hanging_lantern(c, 5, 4, 6)
    hanging_lantern(c, 9, 4, 3)
    c.carpet(5, 1, 7, "light_blue")
    c.carpet(6, 1, 7, "light_blue")
    # Cuisine et réserve (x 11..14, z 1..5 cuisine ; z 7..11 réserve), cloison x=11
    c.fill(11, 1, 1, 11, 4, 11, PLANCHES)
    c.set(11, 1, 4, AIR)
    c.set(11, 2, 4, AIR)
    c.set(11, 1, 8, AIR)
    c.set(11, 2, 8, AIR)
    c.fill(12, 1, 6, 14, 4, 6, PLANCHES)      # cloison cuisine / réserve
    c.set(13, 1, 6, AIR)
    c.set(13, 2, 6, AIR)
    c.set(12, 1, 1, "minecraft:furnace[facing=south,lit=false]")
    c.set(13, 1, 1, "minecraft:crafting_table")
    c.set(14, 1, 1, "minecraft:water_cauldron[level=3]")
    counter(c, 12, 1, 2, 3, "x")
    c.set(14, 1, 3, "minecraft:composter[level=0]")
    hanging_lantern(c, 13, 4, 3)
    c.set(12, 1, 8, "minecraft:barrel[facing=up,open=false]", {"id": "minecraft:barrel", "LootTable": "les_limbes:chests/relais"})
    c.set(12, 1, 9, "minecraft:barrel[facing=up,open=false]")
    c.set(12, 2, 9, "minecraft:barrel[facing=up,open=false]")
    c.set(14, 1, 8, "minecraft:hay_block[axis=y]")
    c.set(14, 1, 9, "minecraft:hay_block[axis=y]")
    c.set(14, 1, 10, "minecraft:chest[facing=west,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/relais"})
    c.set(13, 1, 10, "minecraft:lantern[hanging=false,waterlogged=false]")
    # Escalier vers l'étage (x 9..10, z 1..4)
    for i, z in enumerate(range(4, 0, -1)):
        c.set(10, 1 + i, z, stair(STAIR_BOIS, "north"))
        c.set(10, 5, z, AIR)
    c.set(10, 5, 1, AIR)
    c.set(9, 5, 1, AIR)
    c.set(10, 5, 2, AIR)
    for z in range(1, 5):
        c.set(9, 1, z, BARRIERE)
    # Étage : couloir central (z 5..7), trois chambres (nord : x 1..5, x 7..11 ; sud : x 1..6), bains (x 12..14, sud)
    c.fill(1, 6, 4, 14, 8, 4, PLANCHES)
    c.fill(1, 6, 8, 14, 8, 8, PLANCHES)
    c.set(6, 6, 1, PLANCHES); c.set(6, 7, 1, PLANCHES); c.set(6, 6, 2, PLANCHES); c.set(6, 7, 2, PLANCHES); c.set(6, 6, 3, PLANCHES); c.set(6, 7, 3, PLANCHES)
    c.set(12, 6, 1, PLANCHES); c.set(12, 7, 1, PLANCHES); c.set(12, 6, 2, PLANCHES); c.set(12, 7, 2, PLANCHES); c.set(12, 6, 3, PLANCHES); c.set(12, 7, 3, PLANCHES)
    c.set(7, 6, 9, PLANCHES); c.set(7, 7, 9, PLANCHES); c.set(7, 6, 10, PLANCHES); c.set(7, 7, 10, PLANCHES); c.set(7, 6, 11, PLANCHES); c.set(7, 7, 11, PLANCHES)
    c.set(11, 6, 9, PLANCHES); c.set(11, 7, 9, PLANCHES); c.set(11, 6, 10, PLANCHES); c.set(11, 7, 10, PLANCHES); c.set(11, 6, 11, PLANCHES); c.set(11, 7, 11, PLANCHES)
    for x in (3, 9, 13):
        door(c, x, 6, 4, "north")
    for x in (4, 9):
        door(c, x, 6, 8, "south")
    door(c, 13, 6, 8, "south")
    # Chambres nord
    for bx in (1, 7):
        c.bed(bx + 1, 6, 2, "light_blue" if bx == 1 else "white", "north")
        c.set(bx, 6, 1, "minecraft:chest[facing=east,type=single,waterlogged=false]")
        c.set(bx + 3, 6, 1, "minecraft:lantern[hanging=false,waterlogged=false]")
        c.flower_pot(bx + 3, 6, 2, "minecraft:potted_azure_bluet")
    c.bed(14, 6, 2, "cyan", "north")
    c.set(13, 6, 1, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    # Chambres sud + bains
    c.bed(2, 6, 10, "white", "south")
    c.set(1, 6, 11, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.set(5, 6, 11, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.bed(9, 6, 10, "light_gray", "south")
    c.set(8, 6, 11, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.fill(12, 6, 10, 14, 6, 11, "minecraft:water_cauldron[level=3]")
    c.set(12, 6, 9, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.set(14, 6, 9, "minecraft:barrel[facing=up,open=false]")
    hanging_lantern(c, 7, 8, 6)
    hanging_lantern(c, 12, 8, 6)
    # Thibault derrière le comptoir, Marguerite en cuisine
    data_marker(c, 4, 1, 3, "pnj:thibault")
    data_marker(c, 13, 1, 4, "pnj:marguerite")
    # Lanterne extérieure sous l'auvent
    if c.inside(7, 3, D):
        c.set(7, 3, D, LANTERNE_H)
    return c


def build_forge() -> Canvas:
    c = Canvas("forge", 13, 12, 12)
    c.shift = (1, 0, 1)
    c.note("Forge de Brann : atelier ouvert sur la rue (fourneau, enclume, meule), stockage, logement à l'arrière.")
    W, D = 11, 10
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    c.fill(1, 0, 1, W - 2, 0, D - 2, PIERRE_P)
    c.walls(0, 1, 0, W - 1, 3, D - 1, PIERRE_T)
    c.fill(1, 1, 1, W - 2, 3, D - 2, AIR)
    corner_logs(c, 0, 1, 0, W - 1, D - 1, 4)
    c.fill(0, 4, 0, W - 1, 4, D - 1, PLANCHES)
    c.gable_roof(0, 0, W - 1, D - 1, 5, STAIR_PIERRE, axis="z", overhang=1)
    # Façade ouest ouverte (atelier) : grande ouverture x=0, z 2..6
    for z in range(2, 7):
        c.set(0, 1, z, AIR)
        c.set(0, 2, z, AIR)
    c.set(0, 3, 4, LANTERNE_H)
    # Atelier (z 1..6)
    c.set(2, 1, 1, "minecraft:blast_furnace[facing=south,lit=true]")
    c.set(3, 1, 1, "minecraft:furnace[facing=south,lit=false]")
    c.set(4, 1, 1, PIERRE_T)
    c.set(4, 2, 1, "minecraft:campfire[facing=south,lit=true,signal_fire=false,waterlogged=false]")
    c.column(4, 3, 7, 1, PIERRE_T)  # cheminée
    c.set(4, 3, 1, AIR)
    c.set(6, 1, 2, "minecraft:anvil[facing=west]")
    c.set(8, 1, 2, "minecraft:grindstone[face=floor,facing=west]")
    c.set(8, 1, 4, "minecraft:smithing_table")
    c.set(8, 1, 5, "minecraft:stonecutter[facing=west]")
    c.set(2, 1, 5, "minecraft:water_cauldron[level=3]")
    c.set(2, 1, 3, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/relais"})
    c.set(6, 1, 5, "minecraft:barrel[facing=up,open=false]")
    hanging_lantern(c, 5, 3, 3)
    # Logement (z 7..8) séparé par cloison
    c.fill(1, 1, 7, W - 2, 3, 7, PLANCHES)
    c.set(5, 1, 7, AIR)
    c.set(5, 2, 7, AIR)
    c.bed(1, 1, 8, "red", "east")
    c.set(4, 1, 8, "minecraft:chest[facing=north,type=single,waterlogged=false]")
    c.set(7, 1, 8, "minecraft:crafting_table")
    c.set(9, 1, 8, "minecraft:lantern[hanging=false,waterlogged=false]")
    table(c, 6, 1, 8, 1)
    window(c, 2, 2, D - 1, "south", 2)
    window(c, 7, 2, D - 1, "south", 2)
    window(c, W - 1, 2, 3, "east", 2)
    c.sign_wall(0, 3, 1, "minecraft:oak", "west", ["Forge", "de Brann", "réparations", "& outils"])
    data_marker(c, 5, 1, 4, "pnj:brann")
    data_marker(c, 8, 1, 3, "pnj:sabine")
    return c


def build_maison(nom: str, role: str, lit: str, variante: int) -> Canvas:
    c = Canvas(nom, 11, 12, 11)
    c.shift = (1, 0, 1)
    c.note(f"Maison de Clairval ({variante}) : pièce à vivre, coin cuisine, lit, rangements ; habitant {role}.")
    W, D = 9, 9
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    c.fill(1, 0, 1, W - 2, 0, D - 2, PLANCHES)
    wall = PLANCHES if variante != 2 else PIERRE_T
    c.walls(0, 1, 0, W - 1, 3, D - 1, wall)
    c.fill(1, 1, 1, W - 2, 3, D - 2, AIR)
    corner_logs(c, 0, 1, 0, W - 1, D - 1, 3)
    c.fill(0, 4, 0, W - 1, 4, D - 1, PLANCHES)
    c.gable_roof(0, 0, W - 1, D - 1, 5, STAIR_BOIS if variante != 3 else STAIR_PIERRE, axis="x" if variante != 2 else "z", overhang=1)
    door(c, 4, 1, D - 1, "south")
    window(c, 1, 2, D - 1, "south", 2)
    window(c, 6, 2, D - 1, "south", 2)
    window(c, 0, 2, 3, "west", 2)
    window(c, W - 1, 2, 3, "east", 2)
    window(c, 3, 2, 0, "north", 2)
    c.bed(1, 1, 2, lit, "north")
    c.set(1, 1, 1, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.set(7, 1, 1, "minecraft:furnace[facing=south,lit=false]")
    c.set(6, 1, 1, "minecraft:crafting_table")
    c.set(7, 1, 2, "minecraft:barrel[facing=up,open=false]")
    table(c, 3, 1, 5, 2, "x")
    chair(c, 2, 1, 5, "east")
    chair(c, 5, 1, 5, "west")
    c.set(7, 1, 7, "minecraft:bookshelf" if variante == 1 else "minecraft:loom[facing=west]" if variante == 2 else "minecraft:composter[level=0]")
    c.flower_pot(1, 1, 7, "minecraft:potted_azure_bluet")
    hanging_lantern(c, 4, 3, 3)
    c.carpet(3, 1, 7, ["light_blue", "white", "lime"][variante - 1])
    c.carpet(4, 1, 7, ["light_blue", "white", "lime"][variante - 1])
    data_marker(c, 4, 1, 6, "pnj:" + role)
    return c


def build_moulin() -> Canvas:
    c = Canvas("moulin", 9, 18, 9)
    c.note("Moulin de Clairval, immobile depuis des années : tour de pierre, ailes figées, meule.")
    W, D = 9, 9
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    for y in range(1, 9):
        c.walls(1, y, 1, W - 2, y, D - 2, PIERRE_T if y % 3 else PIERRE_P)
        c.fill(2, y, 2, W - 3, y, D - 3, AIR)
    c.fill(1, 9, 1, W - 2, 9, D - 2, PLANCHES)
    c.gable_roof(1, 1, W - 2, D - 2, 10, STAIR_BOIS, axis="x", overhang=0)
    door(c, 4, 1, D - 2, "south")
    window(c, 4, 4, D - 2, "south", 1)
    window(c, 4, 7, D - 2, "south", 1)
    c.set(2, 1, 2, "minecraft:grindstone[face=floor,facing=south]")
    c.set(3, 1, 2, "minecraft:hay_block[axis=y]")
    c.set(6, 1, 6, "minecraft:barrel[facing=up,open=false]")
    c.set(5, 1, 6, "minecraft:barrel[facing=up,open=false]")
    c.set(2, 1, 6, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/manoir"})
    hanging_lantern(c, 4, 4, 4)
    # Ailes du moulin : axe en façade sud (bloc animé côté client, immobile tant que le moulin n'est pas réparé)
    c.set(4, 8, D - 1, "les_limbes:ailes_de_moulin[actif=false,facing=south]")
    # Meule du moulin (remplace la botte de foin) et Rosalie, la meunière
    c.set(3, 1, 2, "les_limbes:meule_de_moulin")
    data_marker(c, 4, 1, 5, "pnj:rosalie")
    c.sign_wall(5, 3, D - 2, "minecraft:oak", "south", ["Moulin", "de Clairval", "— immobile —", "Rosalie"])
    return c


# ============================================================================== MANOIR
def build_manoir() -> Canvas:
    W, H, D = 27, 24, 23
    c = Canvas("manoir", W, H, D)
    c.note("Manoir de Clairval : hall, bibliothèque, cuisine, réserve ; chambres à l'étage ; aile ouest à restaurer ; jardin, serre.")
    # Corps principal x 2..24, z 4..18 ; jardin devant (sud, z 19..22) ; serre à l'est hors corps
    x1, x2, z1, z2 = 2, 24, 4, 18
    c.fill(x1, 0, z1, x2, 0, z2, PIERRE_T)
    c.fill(x1 + 1, 0, z1 + 1, x2 - 1, 0, z2 - 1, PLANCHES)
    c.walls(x1, 1, z1, x2, 4, z2, PIERRE_T)
    c.fill(x1 + 1, 1, z1 + 1, x2 - 1, 4, z2 - 1, AIR)
    corner_logs(c, x1, 1, z1, x2, z2, 9)
    c.fill(x1, 5, z1, x2, 5, z2, PLANCHES)                # plancher étage
    c.walls(x1, 6, z1, x2, 9, z2, PLANCHES)
    c.fill(x1 + 1, 6, z1 + 1, x2 - 1, 9, z2 - 1, AIR)
    c.fill(x1, 6, z1, x2, 6, z2, PIERRE_T)                # bandeau
    c.fill(x1 + 1, 6, z1 + 1, x2 - 1, 6, z2 - 1, AIR)
    c.fill(x1, 10, z1, x2, 10, z2, PLANCHES)              # plafond / grenier
    c.gable_roof(x1, z1, x2, z2, 11, STAIR_BOIS, axis="x", overhang=1)
    # Aile ouest endommagée : trou dans le toit et le plancher du grenier, toiles d'araignée
    for x in range(3, 8):
        for z in range(8, 14):
            for y in range(10, 17):
                if c.get(x, y, z) != VOID:
                    c.set(x, y, z, AIR)
    for x, z in [(4, 9), (6, 12), (5, 11)]:
        c.set(x, 9, z, "minecraft:cobweb")
    c.set(4, 10, 10, slab(DALLE_BOIS))
    c.set(6, 10, 9, stair(STAIR_BOIS, "south"))
    # Entrée sud centrée (x=13) : double porte, perron, colonnes
    door(c, 12, 1, z2, "south")
    door(c, 13, 1, z2, "south")
    c.set(12, 3, z2, PIERRE_S)
    c.set(13, 3, z2, PIERRE_S)
    c.fill(10, 0, z2 + 1, 15, 0, z2 + 2, PIERRE_P)
    c.set(10, 1, z2 + 1, LANTERNE)
    c.set(15, 1, z2 + 1, LANTERNE)
    # Fenêtres
    for x in (4, 8, 17, 21):
        window(c, x, 2, z2, "south", 2)
        window(c, x, 7, z2, "south", 2)
        window(c, x, 2, z1, "north", 2)
        window(c, x, 7, z1, "north", 2)
    for z in (7, 11, 15):
        window(c, x1, 2, z, "west", 2)
        window(c, x2, 2, z, "east", 2)
        window(c, x1, 7, z, "west", 2)
        window(c, x2, 7, z, "east", 2)
    # Rez-de-chaussée : cloisons. Hall central x 9..16 ; bibliothèque x 3..8 ; cuisine x 17..23 (nord) ; réserve x 17..23 (sud)
    c.fill(9, 1, z1 + 1, 9, 4, z2 - 1, PLANCHES)
    c.fill(16, 1, z1 + 1, 16, 4, z2 - 1, PLANCHES)
    c.fill(17, 1, 11, 23, 4, 11, PLANCHES)
    for z in (8, 14):
        c.set(9, 1, z, AIR); c.set(9, 2, z, AIR)
        c.set(16, 1, z, AIR); c.set(16, 2, z, AIR)
    c.set(20, 1, 11, AIR); c.set(20, 2, 11, AIR)
    # Hall : tapis, escalier, lanternes, tableau d'accueil (panneau)
    c.fill(11, 0, 6, 14, 0, 17, PIERRE_P)
    for z in range(13, 17):
        c.carpet(12, 1, z, "blue")
        c.carpet(13, 1, z, "blue")
    for i, z in enumerate(range(8, 4, -1)):
        c.set(11, 1 + i, z, stair(STAIR_BOIS, "north"))
        c.set(14, 1 + i, z, stair(STAIR_BOIS, "north"))
    c.fill(11, 5, 5, 14, 5, 8, AIR)
    c.set(12, 1, 5, PLANCHES); c.set(13, 1, 5, PLANCHES)
    c.set(12, 2, 5, PLANCHES); c.set(13, 2, 5, PLANCHES)
    c.set(12, 3, 5, PLANCHES); c.set(13, 3, 5, PLANCHES)
    c.set(12, 4, 5, PLANCHES); c.set(13, 4, 5, PLANCHES)
    hanging_lantern(c, 10, 4, 10)
    hanging_lantern(c, 15, 4, 10)
    c.set(10, 1, 16, "minecraft:lectern[facing=east,has_book=false,powered=false]")
    c.flower_pot(15, 1, 16, "minecraft:potted_azure_bluet")
    # Bibliothèque (x 3..8, z 5..17)
    bookshelves(c, 3, 1, 5, 3, 3, 17)
    bookshelves(c, 4, 1, 5, 8, 3, 5)
    c.set(3, 1, 7, AIR); c.set(3, 2, 7, AIR)  # fenêtre dégagée
    c.set(3, 1, 11, AIR); c.set(3, 2, 11, AIR)
    c.set(3, 1, 15, AIR); c.set(3, 2, 15, AIR)
    table(c, 5, 1, 10, 2, "z")
    chair(c, 4, 1, 10, "east")
    chair(c, 6, 1, 11, "west")
    c.set(7, 1, 16, "minecraft:lectern[facing=north,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Registre de l'intendant", "L'intendant Aubin", [
            "Registre du manoir de Clairval.\n\nLe roi n'est pas revenu de la dernière Arrivée. Les routes s'éteignent une à une. Je tiens le manoir tant que je le peux.",
            "Ce qu'il faudra restaurer : l'aile ouest (toit), la serre, le puits du jardin, la forge royale au sous-sol.\n\nQuiconque reprend ces murs en reprend la charge."])})
    c.set(5, 1, 16, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.carpet(5, 1, 13, "white"); c.carpet(6, 1, 13, "white")
    hanging_lantern(c, 6, 4, 8)
    # Cuisine (x 17..23, z 5..10)
    c.set(18, 1, 5, "minecraft:furnace[facing=south,lit=false]")
    c.set(19, 1, 5, "minecraft:smoker[facing=south,lit=false]")
    c.set(20, 1, 5, "minecraft:crafting_table")
    c.set(21, 1, 5, "minecraft:water_cauldron[level=3]")
    counter(c, 22, 1, 5, 1)
    counter(c, 23, 1, 6, 4, "z")
    c.set(17, 1, 8, "minecraft:barrel[facing=up,open=false]")
    c.set(17, 1, 9, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/manoir"})
    table(c, 19, 1, 8, 2, "x")
    chair(c, 18, 1, 8, "east")
    chair(c, 21, 1, 8, "west")
    hanging_lantern(c, 20, 4, 7)
    # Réserve (x 17..23, z 12..17)
    for z in (13, 15, 17):
        c.set(23, 1, z, "minecraft:barrel[facing=up,open=false]")
        c.set(23, 2, z, "minecraft:barrel[facing=up,open=false]") if z != 17 else None
    c.set(17, 1, 13, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/manoir"})
    c.set(17, 1, 15, "minecraft:hay_block[axis=y]")
    c.set(18, 1, 17, "minecraft:composter[level=0]")
    c.set(20, 1, 17, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.set(19, 1, 14, "minecraft:cobweb")
    # Étage : couloir central x 9..16 ; chambre principale ouest (x 3..8, z 5..11) ; bureau ouest sud (x 3..8, z 13..17) ;
    # chambre d'amis est (x 17..23, z 5..10) ; chambre de compagnon est (x 17..23, z 12..17)
    c.fill(9, 6, z1 + 1, 9, 9, z2 - 1, PLANCHES)
    c.fill(16, 6, z1 + 1, 16, 9, z2 - 1, PLANCHES)
    c.fill(3, 6, 12, 8, 9, 12, PLANCHES)
    c.fill(17, 6, 11, 23, 9, 11, PLANCHES)
    for z in (8, 15):
        door(c, 9, 6, z, "east")
        door(c, 16, 6, z, "west")
    c.bed(5, 6, 7, "blue", "north")
    c.set(4, 6, 6, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.set(7, 6, 6, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.carpet(5, 6, 9, "light_blue"); c.carpet(6, 6, 9, "light_blue")
    c.flower_pot(4, 6, 10, "minecraft:potted_azure_bluet")
    table(c, 5, 6, 14, 2, "x")
    chair(c, 4, 6, 14, "east")
    bookshelves(c, 3, 6, 16, 3, 8, 17)
    c.set(7, 6, 16, "minecraft:lectern[facing=west,has_book=false,powered=false]")
    c.set(6, 6, 17, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.bed(20, 6, 7, "white", "north")
    c.set(18, 6, 6, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.set(22, 6, 6, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.bed(20, 6, 14, "light_gray", "south")
    c.set(18, 6, 16, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.set(22, 6, 16, "minecraft:lantern[hanging=false,waterlogged=false]")
    hanging_lantern(c, 12, 9, 12)
    c.carpet(12, 6, 10, "blue"); c.carpet(13, 6, 10, "blue"); c.carpet(12, 6, 11, "blue"); c.carpet(13, 6, 11, "blue")
    # Jardin sud (z 19..22) : fontaine, massifs, allée ; serre à restaurer à l'est (x 20..26, z 0..3) ; puits ouest
    c.fill(0, 0, 19, W - 1, 0, D - 1, HERBE)
    c.fill(12, 0, 19, 13, 0, D - 1, CHEMIN)
    garden_bed(c, 16, 0, 20, 21, 21)
    garden_bed(c, 4, 0, 20, 9, 21, HERBE_AUBE)
    for x in (1, 25):
        lamppost(c, x, 1, 20, 3)
    c.fill(20, 0, 0, 26, 0, 3, PIERRE_T)
    c.walls(20, 1, 0, 26, 1, 3, PIERRE_T)
    c.fill(21, 1, 1, 25, 1, 2, TERRE)
    c.fill(21, 2, 1, 25, 2, 2, AIR)
    for x in range(20, 27, 2):
        c.set(x, 2, 0, VERRE); c.set(x, 2, 3, VERRE)
    c.set(20, 2, 1, VERRE); c.set(26, 2, 2, VERRE)
    c.set(22, 2, 1, HERBE_AUBE); c.set(24, 2, 2, FLEUR)
    # Puits
    c.fill(0, 0, 7, 1, 0, 8, COBBLE)
    c.set(0, 1, 7, COBBLE); c.set(1, 1, 8, COBBLE)
    # Orielle devant l'entrée
    data_marker(c, 14, 1, z2 + 2, "pnj:orielle")
    # Lanterne de porte
    return c


# ============================================================================== RELAIS ROYAL
def build_relais() -> Canvas:
    W, H, D = 21, 13, 19
    c = Canvas("relais_royal", W, H, D)
    c.note("Ancien relais royal en ruine : grande salle au toit effondré, écurie, cave, Gaspard caché, trois Veilleurs égarés.")
    c.fill(0, 0, 0, W - 1, 0, D - 1, HERBE)
    # Bâtiment principal x 2..14, z 2..12
    x1, x2, z1, z2 = 2, 14, 2, 12
    c.fill(x1, 0, z1, x2, 0, z2, PIERRE_T)
    c.fill(x1 + 1, 0, z1 + 1, x2 - 1, 0, z2 - 1, "minecraft:cracked_stone_bricks")
    c.walls(x1, 1, z1, x2, 4, z2, PIERRE_T)
    c.fill(x1 + 1, 1, z1 + 1, x2 - 1, 4, z2 - 1, AIR)
    # Murs partiellement effondrés : on retire des blocs du haut
    import random
    rng = random.Random(7)
    for x in range(x1, x2 + 1):
        for z in (z1, z2):
            if rng.random() < 0.35:
                c.set(x, 4, z, AIR)
                if rng.random() < 0.5:
                    c.set(x, 3, z, AIR)
    for z in range(z1, z2 + 1):
        for x in (x1, x2):
            if rng.random() < 0.3:
                c.set(x, 4, z, AIR)
    # Toit effondré : moitié ouest conservée
    c.fill(x1, 5, z1, 8, 5, z2, PLANCHES)
    c.gable_roof(x1, z1, 8, z2, 6, STAIR_PIERRE, axis="z", overhang=0)
    for x in range(9, x2 + 1):
        for z in range(z1 + 1, z2):
            if rng.random() < 0.15:
                c.set(x, 1, z, "minecraft:cobblestone")
    # Entrée (sud) et portail est
    c.set(8, 1, z2, AIR); c.set(8, 2, z2, AIR)
    c.set(x2, 1, 7, AIR); c.set(x2, 2, 7, AIR); c.set(x2, 1, 8, AIR); c.set(x2, 2, 8, AIR)
    # Intérieur : cheminée, tables brisées, arrière-salle (Gaspard) à l'ouest
    c.fill(3, 1, 3, 3, 3, 5, PIERRE_T)
    c.set(4, 1, 4, "minecraft:campfire[facing=east,lit=false,signal_fire=false,waterlogged=false]")
    table(c, 6, 1, 5, 2, "x")
    c.set(10, 1, 5, "minecraft:barrel[facing=up,open=false]")
    c.set(10, 1, 4, "minecraft:cobweb")
    c.set(12, 1, 3, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/relais"})
    # Porte de relais royale, contre le mur nord de la grande salle : monde (148,66,54) ; arrivée en (148,66,56)
    c.set(8, 1, 3, "les_limbes:porte_de_relais[eveillee=false,facing=south]", {"id": "les_limbes:porte_de_relais", "relais": "relais_royal"})
    c.set(8, 2, 3, AIR)
    c.set(6, 1, 10, "minecraft:cobweb")
    c.set(12, 1, 10, "minecraft:hay_block[axis=y]")
    c.set(13, 1, 10, "minecraft:hay_block[axis=y]")
    c.set(11, 1, 11, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.fill(3, 1, 7, 7, 3, 7, PIERRE_T)          # cloison de l'arrière-salle
    c.set(5, 1, 7, AIR); c.set(5, 2, 7, AIR)
    c.set(3, 1, 10, "minecraft:barrel[facing=up,open=false]")
    c.set(4, 1, 11, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.bed(6, 1, 9, "brown", "south")
    data_marker(c, 4, 1, 9, "pnj:gaspard")
    # Cave (sous l'arrière-salle)
    # (pas de cave ici : la masse sous le gabarit est remplie par beard_thin)
    # Écurie (est, x 15..19, z 3..9) : barrières, foin, abreuvoir
    fence_rect(c, 15, 1, 3, 19, 9, (17, 9, "south"))
    c.set(16, 1, 4, "minecraft:hay_block[axis=y]")
    c.set(18, 1, 4, "minecraft:water_cauldron[level=2]")
    c.set(16, 1, 8, "minecraft:hay_block[axis=y]")
    c.fill(15, 0, 3, 19, 0, 9, "minecraft:coarse_dirt")
    for x in range(15, 20):
        c.set(x, 4, 3, PLANCHES); c.set(x, 4, 9, PLANCHES)
    for x in (15, 19):
        c.column(x, 1, 3, 3, log()); c.column(x, 1, 3, 9, log())
    c.fill(15, 4, 3, 19, 4, 9, slab(DALLE_BOIS, "top"))
    # Puits et cour sud (z 13..17)
    c.fill(8, 0, 14, 9, 0, 15, COBBLE)
    c.set(8, 1, 14, COBBLE); c.set(9, 1, 15, COBBLE)
    c.fill(2, 0, 13, 14, 0, 13, GRAVIER)
    c.sign_standing(11, 1, 14, "minecraft:oak", 0, ["Relais royal", "Route de la", "Couronne", "— fermé —"])
    # Végétation envahissante
    for x, z in [(1, 1), (19, 1), (1, 17), (19, 17), (16, 12), (5, 15), (13, 16)]:
        c.set(x, 0, z, TERRE)
        c.set(x, 1, z, FIBRES)
    for x, z in [(x1, z1), (x2, z1), (x1, z2), (x2, z2)]:
        c.set(x, 1, z, MOUSSE)
    # Lanternes brisées (lanternes d'âmes) aux piliers
    c.set(x1, 5, z2, "minecraft:soul_lantern[hanging=false,waterlogged=false]")
    # Veilleurs égarés : trois marqueurs autour de la cour
    data_marker(c, 10, 1, 15, "entite:les_limbes:veilleur_egare")
    data_marker(c, 17, 1, 13, "entite:les_limbes:veilleur_egare")
    data_marker(c, 11, 1, 7, "entite:les_limbes:veilleur_egare")
    return c


# ============================================================================== ARRIVÉES
def build_maison_egaree() -> Canvas:
    """Maison venue d'un autre monde : chêne, pierre grise, toit d'épicéa ; four à pain, Émile désorienté."""
    c = Canvas("maison_egaree", 13, 14, 13)
    c.note("Arrivée « La maison égarée » : chaumière d'ailleurs (chêne, pavés), four à pain, Émile.")
    W, D = 11, 11
    c.shift = (1, 1, 1)
    c.fill(-1, -1, -1, W, -1, D, "minecraft:coarse_dirt")       # terre retournée par l'atterrissage
    c.fill(0, -1, 0, W - 1, -1, D - 1, "minecraft:cobblestone")
    c.fill(1, -1, 1, W - 2, -1, D - 2, "minecraft:oak_planks")
    c.walls(0, 0, 0, W - 1, 3, D - 1, "minecraft:oak_planks")
    c.fill(1, 0, 1, W - 2, 3, D - 2, AIR)
    c.fill(0, 0, 0, W - 1, 0, D - 1, "minecraft:cobblestone")
    c.fill(1, 0, 1, W - 2, 0, D - 2, AIR)
    for x, z in [(0, 0), (0, D - 1), (W - 1, 0), (W - 1, D - 1)]:
        c.column(x, 0, 3, z, "minecraft:oak_log[axis=y]")
    c.fill(0, 4, 0, W - 1, 4, D - 1, "minecraft:oak_planks")
    c.gable_roof(0, 0, W - 1, D - 1, 5, "minecraft:spruce", axis="x", overhang=1)
    c.door(5, 0, D - 1, "minecraft:oak", "south")
    for x in (2, 8):
        window(c, x, 1, D - 1, "south", 2)
        window(c, x, 1, 0, "north", 2)
    window(c, 0, 1, 4, "west", 2)
    window(c, W - 1, 1, 4, "east", 2)
    # Four à pain (fournil) côté ouest, lit côté est, table au centre
    c.fill(1, 0, 1, 3, 2, 1, "minecraft:bricks")
    c.set(2, 0, 1, "minecraft:smoker[facing=south,lit=true]")
    c.set(2, 1, 1, "minecraft:furnace[facing=south,lit=false]")
    c.column(2, 3, 6, 1, "minecraft:bricks")
    c.set(1, 0, 2, "minecraft:barrel[facing=up,open=false]", {"id": "minecraft:barrel", "LootTable": "les_limbes:chests/maison_egaree"})
    c.set(3, 0, 2, "minecraft:crafting_table")
    c.set(1, 0, 4, "minecraft:cauldron")
    c.bed(8, 0, 2, "red", "north")
    c.set(9, 0, 1, "minecraft:chest[facing=west,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/maison_egaree"})
    c.set(7, 0, 1, "minecraft:lantern[hanging=false,waterlogged=false]")
    for i in range(2):
        c.set(5 + i, 0, 5, "minecraft:oak_fence")
        c.set(5 + i, 1, 5, "minecraft:white_carpet")
    c.set(4, 0, 5, "minecraft:oak_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]")
    c.set(7, 0, 5, "minecraft:oak_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]")
    c.set(5, 3, 5, "minecraft:lantern[hanging=true,waterlogged=false]")
    c.set(1, 0, 8, "minecraft:bookshelf")
    c.flower_pot(9, 0, 8, "minecraft:potted_red_tulip")
    c.set(8, 0, 9, "minecraft:composter[level=2]")
    c.set(2, 0, 9, "minecraft:hay_block[axis=y]")
    # Trace de l'Arrivée : cristal de nacre sur le faîte, éclats autour
    c.set(5, 10, 5, CRISTAL)
    for x, z in [(-1, 3), (W, 7), (4, -1), (6, D)]:
        c.set(x, 0, z, CRISTAL) if c.inside(x, 0, z) else None
    data_marker(c, 6, 0, 7, "pnj:emile")
    return c


def build_caisse_tombee() -> Canvas:
    """Petite Arrivée : caisse de voyage, lanterne, éclats de nacre encore tièdes."""
    c = Canvas("caisse_tombee", 3, 3, 3)
    c.note("Arrivée « La caisse tombée » : coffre, lanterne et cristaux ; disparaît après deux jours.")
    c.fill(0, 0, 0, 2, 0, 2, "minecraft:coarse_dirt")
    c.set(1, 1, 1, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/caisse_tombee"})
    c.set(0, 1, 0, CRISTAL)
    c.set(2, 1, 2, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.set(2, 1, 0, slab(DALLE_PIERRE))
    return c


def build_barque_echouee() -> Canvas:
    """Arrivée : barque de pêche échouée sur la rive, voile déchirée, caisses ; Ysolde la capitaine."""
    c = Canvas("barque_echouee", 11, 6, 7)
    c.note("Arrivée « La barque échouée » : coque de planches couchée sur la rive, mât brisé, caisses ; Ysolde.")
    W, D = 11, 7
    for x in range(1, 10):
        for z in range(1, 6):
            larg = 2 if x in (1, 9) else 3
            if abs(z - 3) <= larg - 1:
                c.set(x, 0, z, "minecraft:spruce_planks")
                if abs(z - 3) == larg - 1 or x in (1, 9):
                    c.set(x, 1, z, "minecraft:spruce_planks")
                    c.set(x, 2, z, "minecraft:spruce_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]") if x not in (1, 9) else None
    c.set(0, 0, 3, "minecraft:spruce_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]")
    c.set(10, 0, 3, "minecraft:spruce_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]")
    c.column(5, 1, 4, 3, "minecraft:spruce_fence")
    c.set(5, 5, 3, "minecraft:white_wool"); c.set(6, 4, 3, "minecraft:white_wool"); c.set(6, 3, 4, "minecraft:white_wool")
    c.set(3, 1, 3, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/barque"})
    c.set(7, 1, 2, "minecraft:barrel[facing=up,open=false]")
    c.set(7, 1, 4, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.set(2, 1, 4, "minecraft:oak_fence"); c.set(2, 2, 4, "minecraft:white_carpet")
    c.set(0, 0, 0, CRISTAL); c.set(10, 0, 6, CRISTAL)
    data_marker(c, 5, 1, 2, "pnj:ysolde")
    return c


# ============================================================================== OBSERVATOIRE DE CLAIRVAL
def build_observatoire() -> Canvas:
    """Tour de Lisbeth : base de pierre, escalier intérieur, plate-forme de bois avec lunette et registre."""
    c = Canvas("observatoire", 9, 17, 9)
    c.note("Observatoire de Clairval : tour de pierre nacrée, plate-forme d'observation, lunette, registre des Arrivées, Lisbeth.")
    W, D = 9, 9
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    for y in range(1, 11):
        c.walls(1, y, 1, W - 2, y, D - 2, PIERRE_T if y % 4 else PIERRE_P)
        c.fill(2, y, 2, W - 3, y, D - 3, AIR)
    door(c, 4, 1, D - 2, "south")
    window(c, 4, 4, 1, "north", 1)
    window(c, 1, 7, 4, "west", 1)
    window(c, W - 2, 7, 4, "east", 1)
    # Escalier en colimaçon (marches d'angle) jusqu'à la plate-forme (y=11)
    marches = [(2, 2, "east"), (3, 2, "east"), (4, 2, "east"), (5, 2, "east"), (6, 2, "south"), (6, 3, "south"), (6, 4, "south"), (6, 5, "south"), (6, 6, "west"),
               (5, 6, "west"), (4, 6, "west"), (3, 6, "west"), (2, 6, "north"), (2, 5, "north"), (2, 4, "north"), (2, 3, "north"), (3, 2, "east"), (4, 2, "east"), (5, 2, "east"), (6, 2, "south")]
    y = 1
    for i, (x, z, f) in enumerate(marches[:10]):
        c.set(x, y, z, stair(STAIR_BOIS, f))
        y += 1
    c.fill(2, 11, 2, 6, 11, 6, PLANCHES)
    c.set(6, 11, 2, AIR)
    c.set(6, 10, 2, stair(STAIR_BOIS, "south"))
    c.set(4, 1, 4, "minecraft:lectern[facing=south,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Registre des Arrivées", "Lisbeth", [
            "Registre des Arrivées de Clairval.\n\nPremier printemps : une barque sans rames, au nord du lac. Deux voyageurs, vivants, qui parlaient une langue de chansons.",
            "Hiver suivant : un étage de bibliothèque, tombé entier dans les vergers. Les livres sentaient encore la poussière de l'autre monde.",
            "Règle que je me suis fixée : noter la direction, la distance, et aller voir avec quelqu'un. On ne laisse personne seul dans une maison tombée du ciel."])})
    c.bed(2, 1, 6, "blue", "north")
    c.set(6, 1, 6, "minecraft:chest[facing=north,type=single,waterlogged=false]")
    c.set(3, 4, 2, "minecraft:lantern[hanging=true,waterlogged=false]")
    c.set(5, 8, 6, "minecraft:lantern[hanging=true,waterlogged=false]")
    # Plate-forme : garde-corps, lunette (barre de cuivre + verre), lanternes, Lisbeth
    c.fill(1, 12, 1, W - 2, 12, D - 2, PLANCHES)
    c.fill(2, 12, 2, W - 3, 12, D - 3, PLANCHES)
    for x in range(1, W - 1):
        c.set(x, 13, 1, BARRIERE); c.set(x, 13, D - 2, BARRIERE)
    for z in range(1, D - 1):
        c.set(1, 13, z, BARRIERE); c.set(W - 2, 13, z, BARRIERE)
    for x, z in [(1, 1), (W - 2, 1), (1, D - 2), (W - 2, D - 2)]:
        c.set(x, 14, z, LANTERNE)
    c.set(4, 13, 2, "minecraft:lightning_rod[facing=north,powered=false,waterlogged=false]")
    c.set(4, 14, 2, "minecraft:glass")
    c.set(4, 13, 3, "minecraft:spruce_trapdoor[facing=south,half=top,open=false,powered=false,waterlogged=false]")
    c.item_frame(4, 13, 6, "up", "minecraft:spyglass")
    c.hanging_sign_wall(4, 15, 2, "minecraft:oak", "south", ["Observatoire", "de Clairval"])
    data_marker(c, 3, 13, 4, "pnj:lisbeth")
    return c


# ============================================================================== LAC DU CRÉPUSCULE
def build_lac_crepuscule() -> Canvas:
    """Lac aux dalles de rosée : bassin, îlot-sanctuaire avec la meule royale, chemin immergé qui n'émerge qu'au crépuscule."""
    import math
    W, H, D = 45, 8, 45
    c = Canvas("lac_crepuscule", W, H, D)
    c.note("Lac du Crépuscule : bassin clair, îlot avec la meule royale ; les dalles de rosée n'offrent un appui qu'au crépuscule et à l'aube.")
    cx, cz = 22, 22
    FOND, EAU_HAUT = 1, 4   # y local : fond 1 (monde 62), eau 2..4 (monde 63..65), sol des rives à 4 (monde 65)
    for x in range(W):
        for z in range(D):
            d = math.hypot((x - cx) / 20.0, (z - cz) / 16.0)
            if d <= 1.0:
                c.set(x, FOND, z, "minecraft:gravel" if (x * 3 + z * 5) % 4 else PIERRE)
                c.fill(x, FOND + 1, z, x, EAU_HAUT, z, "minecraft:water[level=0]")
                c.fill(x, EAU_HAUT + 1, z, x, EAU_HAUT + 3, z, AIR)
            elif d <= 1.12:
                c.set(x, 4, z, "minecraft:sand" if (x + z) % 3 else PIERRE_P)
                c.set(x, 5, z, AIR)
    # Îlot central (rayon 4) : herbe, sanctuaire à quatre piliers, meule royale sur socle sculpté
    for x in range(cx - 4, cx + 5):
        for z in range(cz - 4, cz + 5):
            if math.hypot(x - cx, z - cz) <= 4.2:
                c.set(x, 2, z, TERRE); c.set(x, 3, z, TERRE); c.set(x, 4, z, HERBE)
                c.set(x, 5, z, AIR)
    c.fill(cx - 1, 4, cz - 1, cx + 1, 4, cz + 1, PIERRE_P)
    c.set(cx, 5, cz, PIERRE_S)
    c.set(cx, 6, cz, "les_limbes:meule_royale")
    for dx, dz in [(-3, -3), (3, -3), (-3, 3), (3, 3)]:
        c.column(cx + dx, 5, 7, cz + dz, PIERRE_T)
        c.set(cx + dx, 8 - 1, cz + dz, LANTERNE) if c.inside(cx + dx, 7, cz + dz) else None
    for dx, dz in [(-2, 0), (2, 0), (0, -2), (0, 2)]:
        c.set(cx + dx, 5, cz + dz, FIBRES)
    c.sign_standing(cx + 1, 5, cz + 2, "minecraft:oak", 0, ["Ici dormait", "la meule", "du moulin", "de Clairval"])
    # Chemin de dalles de rosée : de la rive ouest à l'îlot, en zigzag, au niveau de la surface de l'eau
    chemin = [(3, 22), (5, 22), (7, 23), (9, 23), (11, 22), (12, 20), (13, 18), (15, 18), (17, 19), (18, 21), (19, 22)]
    for x, z in chemin:
        c.set(x, EAU_HAUT, z, "les_limbes:dalle_de_rosee[revelee=false]")
    # Rives : fibres, fleurs, un banc et un panneau côté ouest
    for x, z in [(1, 20), (1, 25), (43, 20), (22, 1), (22, 43), (10, 6), (34, 38)]:
        if c.get(x, 4, z) in (VOID, "minecraft:sand"):
            c.set(x, 4, z, TERRE)
            c.set(x, 5, z, FIBRES if (x + z) % 2 else FLEUR)
    c.set(2, 5, 19, stair(STAIR_BOIS, "east"))
    c.set(2, 5, 20, stair(STAIR_BOIS, "east"))
    c.sign_standing(2, 5, 24, "minecraft:oak", 12, ["Lac du", "Crépuscule", "« Quand le jour", "s'incline... »"])
    lamppost(c, 2, 5, 26, 2)
    return c


# ============================================================================== DOMAINE : PROJETS DU MANOIR
# Coin du manoir : (-133, 65, 139). Les annexes se placent sur le plateau aplani (rayon 34 autour de -120/150).
def build_aile_ouest() -> Canvas:
    """Réparation de l'aile ouest : plancher du grenier, rangée de toit manquante, toiles retirées."""
    c = Canvas("aile_ouest", 5, 8, 6)
    c.note("Projet : réparer l'aile ouest du manoir (plancher du grenier, toit, toiles).")
    c.fill(0, 0, 0, 4, 0, 5, AIR)                      # y local 0 = monde 74 : toiles d'araignée retirées
    c.fill(0, 1, 0, 4, 1, 5, PLANCHES)                  # plancher du grenier (monde 75)
    c.fill(0, 2, 0, 4, 6, 5, AIR)
    c.fill(0, 7, 0, 4, 7, 0, stair(STAIR_BOIS, "south"))  # rangée de toit (monde 81, z 147)
    c.set(2, 2, 3, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.set(1, 2, 4, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/manoir"})
    c.set(3, 2, 1, "minecraft:barrel[facing=up,open=false]")
    return c


def build_forge_royale() -> Canvas:
    """Forge royale : atelier à deux foyers, enclume, meule, bassin de trempe, plan de travail ; Sabine."""
    c = Canvas("forge_royale", 11, 9, 9)
    c.note("Projet : forge royale (annexe ouest). Deux foyers, enclume, bassin de trempe, établi d'armes ; Sabine la forgeronne.")
    W, D = 11, 9
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    c.fill(1, 0, 1, W - 2, 0, D - 2, PIERRE_P)
    c.walls(0, 1, 0, W - 1, 4, D - 1, PIERRE_T)
    c.fill(1, 1, 1, W - 2, 4, D - 2, AIR)
    corner_logs(c, 0, 1, 0, W - 1, D - 1, 4)
    c.fill(0, 5, 0, W - 1, 5, D - 1, slab(DALLE_PIERRE))
    c.fill(1, 5, 1, W - 2, 5, D - 2, PIERRE_T)
    door(c, 5, 1, D - 1, "south")
    door(c, 5, 1, 0, "north")
    window(c, 2, 2, D - 1, "south", 2); window(c, 7, 2, D - 1, "south", 2)
    window(c, 0, 2, 3, "west", 2); window(c, W - 1, 2, 3, "east", 2)
    for x in (2, 8):
        c.set(x, 1, 1, "minecraft:blast_furnace[facing=south,lit=true]")
        c.set(x, 2, 1, PIERRE_T); c.column(x, 3, 7, 1, PIERRE_T); c.set(x, 8, 1, slab(DALLE_PIERRE))
    c.set(5, 1, 1, "minecraft:anvil[facing=east]")
    c.set(4, 1, 1, PIERRE_S); c.set(6, 1, 1, PIERRE_S)
    c.set(1, 1, 4, "minecraft:water_cauldron[level=3]")
    c.set(1, 1, 5, "minecraft:grindstone[face=floor,facing=east]")
    c.set(9, 1, 4, "minecraft:smithing_table")
    c.set(9, 1, 5, "minecraft:chest[facing=west,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/relais"})
    c.set(9, 1, 6, "minecraft:barrel[facing=up,open=false]")
    counter(c, 3, 1, 7, 5, "x")
    c.set(2, 1, 7, CRISTAL)
    hanging_lantern(c, 5, 4, 4)
    c.item_frame(5, 2, 2, "south", "les_limbes:epee_du_souverain")
    c.sign_wall(4, 3, D - 1, "minecraft:oak", "south", ["Forge royale", "Sabine", "armes & trempe", ""])
    return c


def build_ecuries() -> Canvas:
    """Écuries et volière : stalles, foin, abreuvoirs, perchoir à raies sous auvent ; Loïc."""
    c = Canvas("ecuries", 13, 9, 11)
    c.note("Projet : écuries et volière (annexe nord-ouest). Stalles, abreuvoirs, perchoir ouvert pour les raies ; Loïc l'éleveur.")
    W, D = 13, 11
    c.fill(0, 0, 0, W - 1, 0, D - 1, "minecraft:coarse_dirt")
    c.fill(0, 0, 0, W - 1, 0, 0, PIERRE_T); c.fill(0, 0, D - 1, W - 1, 0, D - 1, PIERRE_T)
    for x in (0, 6, 12):
        c.column(x, 1, 5, 0, log()); c.column(x, 1, 5, D - 1, log())
    c.fill(0, 6, 0, W - 1, 6, D - 1, slab(DALLE_BOIS, "top"))
    c.fill(0, 5, 0, W - 1, 5, 0, PLANCHES); c.fill(0, 5, D - 1, W - 1, 5, D - 1, PLANCHES)
    # Stalles (ouest) : trois boxes fermés par barrières
    for i in range(3):
        z = 1 + i * 3
        c.fill(1, 1, z, 4, 1, z, BARRIERE) if i else None
        c.set(5, 1, z + 1, f"minecraft:oak_fence_gate[facing=east,in_wall=false,open=false,powered=false]")
        c.column(5, 1, 3, z, BARRIERE) if i else None
        c.set(2, 1, z + 1, "minecraft:hay_block[axis=y]")
        c.set(1, 1, z + 2, "minecraft:water_cauldron[level=3]")
    c.fill(5, 1, 1, 5, 1, 9, BARRIERE)
    c.set(5, 1, 2, "minecraft:oak_fence_gate[facing=east,in_wall=false,open=false,powered=false]")
    c.set(5, 1, 5, "minecraft:oak_fence_gate[facing=east,in_wall=false,open=false,powered=false]")
    c.set(5, 1, 8, "minecraft:oak_fence_gate[facing=east,in_wall=false,open=false,powered=false]")
    # Volière (est) : perchoir ouvert, cristaux, fleurs de rosée en pots
    c.fill(7, 1, 1, 11, 1, 9, PIERRE_P)
    c.fill(8, 1, 2, 10, 1, 8, HERBE)
    for x, z in [(8, 2), (10, 2), (8, 8), (10, 8)]:
        c.column(x, 2, 3, z, BARRIERE)
        c.set(x, 4, z, LANTERNE)
    c.set(9, 1, 5, PIERRE_S); c.set(9, 2, 5, CRISTAL)
    c.set(8, 2, 5, "minecraft:potted_azure_bluet"); c.set(10, 2, 5, "minecraft:potted_azure_bluet")
    c.fill(7, 2, 1, 11, 2, 1, "minecraft:oak_trapdoor[facing=south,half=top,open=false,powered=false,waterlogged=false]")
    c.set(6, 1, 1, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.set(6, 1, 9, "minecraft:barrel[facing=up,open=false]")
    c.sign_standing(6, 1, 5, "minecraft:oak", 12, ["Écuries", "& volière", "Loïc", "soins des montures"])
    return c


def build_serres() -> Canvas:
    """Serres : verrière sur murets, bacs de culture régionale, cuve d'alchimie ; Hanna."""
    c = Canvas("serres", 9, 7, 15)
    c.note("Projet : serres (nord-est, à la place de la serre effondrée). Verrière, bacs de plantes régionales, cuve ; Hanna la jardinière.")
    W, D = 9, 15
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    c.fill(1, 0, 1, W - 2, 0, D - 2, "minecraft:dirt_path")
    c.walls(0, 1, 0, W - 1, 1, D - 1, PIERRE_T)
    for y in (2, 3):
        c.walls(0, y, 0, W - 1, y, D - 1, "minecraft:glass")
    c.fill(1, 1, 1, W - 2, 3, D - 2, AIR)
    c.fill(0, 4, 0, W - 1, 4, D - 1, "minecraft:glass")
    for x in range(1, W - 1):
        c.set(x, 5, 7, "minecraft:glass")
    c.fill(1, 4, 1, W - 2, 4, D - 2, "minecraft:glass")
    for x in (0, W - 1):
        for z in (0, 7, D - 1):
            c.column(x, 1, 4, z, log())
    door(c, 4, 1, D - 1, "south")
    # Bacs : herbes de l'aube, fleurs de rosée, blé, fibres
    for z0, crop in [(1, HERBE_AUBE), (5, FLEUR), (9, "minecraft:wheat[age=7]"), (12, FIBRES)]:
        for x in (1, 7):
            c.fill(x, 0, z0, x + 1, 0, z0 + 2, TERRE if crop != "minecraft:wheat[age=7]" else "minecraft:farmland[moisture=7]")
            for zz in range(z0, z0 + 3):
                c.set(x, 1, zz, crop); c.set(x + 1, 1, zz, crop)
    c.set(4, 0, 4, "minecraft:water[level=0]"); c.set(4, 0, 10, "minecraft:water[level=0]")
    c.set(4, 1, 1, "minecraft:water_cauldron[level=3]")
    c.set(4, 1, 2, "minecraft:brewing_stand[has_bottle_0=false,has_bottle_1=false,has_bottle_2=false]")
    c.set(3, 1, 1, "minecraft:composter[level=3]")
    c.set(5, 1, 1, "minecraft:chest[facing=south,type=single,waterlogged=false]")
    hanging_lantern(c, 4, 3, 7)
    return c


def build_maison_explorateurs() -> Canvas:
    """Maison des explorateurs : table des cartes, cartothèque, préparatifs, dortoir ; Théo, puis Ysolde."""
    c = Canvas("maison_explorateurs", 13, 13, 13)
    c.shift = (1, 0, 1)
    c.note("Projet : maison des explorateurs (annexe est). Table des cartes, cartothèque, préparatifs d'expédition ; Théo le cartographe, Ysolde la capitaine.")
    W, D = 11, 11
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    c.fill(1, 0, 1, W - 2, 0, D - 2, PLANCHES)
    c.walls(0, 1, 0, W - 1, 4, D - 1, PLANCHES)
    c.fill(1, 1, 1, W - 2, 4, D - 2, AIR)
    corner_logs(c, 0, 1, 0, W - 1, D - 1, 4)
    c.fill(0, 1, 0, W - 1, 1, D - 1, PIERRE_T); c.fill(1, 1, 1, W - 2, 1, D - 2, AIR)
    c.fill(0, 5, 0, W - 1, 5, D - 1, PLANCHES)
    c.gable_roof(0, 0, W - 1, D - 1, 6, STAIR_PIERRE, axis="z", overhang=1)
    door(c, 0, 1, 5, "west")
    window(c, 3, 2, 0, "north", 2); window(c, 6, 2, 0, "north", 2)
    window(c, 3, 2, D - 1, "south", 2); window(c, 6, 2, D - 1, "south", 2)
    window(c, W - 1, 2, 4, "east", 2)
    # Table des cartes (centre) : cartographie, cartes encadrées
    c.set(4, 1, 5, "minecraft:cartography_table"); c.set(5, 1, 5, "minecraft:cartography_table")
    c.set(6, 1, 5, "minecraft:lectern[facing=west,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Rumeurs des Prairies", "Théo", [
            "Rumeurs notées par Théo, cartographe du domaine.\n\nUn moulin qui tourne sans vent, une tour où l'on regarde tomber le ciel, un lac qui n'a de chemin qu'au crépuscule.",
            "Plus loin : une mer qui tient dans le ciel, des arbres gros comme des villes, un désert qui garde les heures, des montagnes de verre. Les routes royales y menaient. Elles sont éteintes.",
            "Règle d'explorateur : on note tout, on revient toujours, on raconte au retour."])})
    for z in (2, 8):
        c.item_frame(5, 2, z, "south" if z == 2 else "north", "minecraft:map")
    bookshelves(c, 1, 1, 1, 1, 3, 3)
    bookshelves(c, 9, 1, 1, 9, 3, 3)
    c.set(1, 1, 9, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/relais"})
    c.set(2, 1, 9, "minecraft:barrel[facing=up,open=false]")
    c.set(9, 1, 9, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.bed(8, 1, 7, "green", "east")
    c.set(1, 1, 6, "minecraft:campfire[facing=east,lit=true,signal_fire=false,waterlogged=false]")
    hanging_lantern(c, 5, 4, 5)
    c.sign_wall(0, 3, 4, "minecraft:oak", "west", ["Maison des", "explorateurs", "cartes & rumeurs", ""])
    return c


def build_galerie() -> Canvas:
    """Galerie des merveilles : vitrines sur socles, éclairage, cartouches ; les objets exposés racontent leur provenance."""
    c = Canvas("galerie", 15, 8, 9)
    c.note("Projet : galerie des merveilles (sud-ouest). Huit vitrines sur socles ; un objet posé y est exposé avec son histoire.")
    W, D = 15, 9
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    c.fill(1, 0, 1, W - 2, 0, D - 2, PIERRE_P)
    c.walls(0, 1, 0, W - 1, 4, D - 1, PIERRE_T)
    c.fill(1, 1, 1, W - 2, 4, D - 2, AIR)
    for x in range(0, W, 2):
        c.column(x, 1, 4, 0, PIERRE_S); c.column(x, 1, 4, D - 1, PIERRE_S)
    c.fill(0, 5, 0, W - 1, 5, D - 1, PIERRE_T)
    c.fill(1, 5, 1, W - 2, 5, D - 2, "minecraft:glass")
    c.walls(1, 6, 1, W - 2, 6, D - 2, slab(DALLE_PIERRE))
    door(c, 7, 1, 0, "north")
    for x in (3, 11):
        window(c, x, 2, D - 1, "south", 2)
    for i, x in enumerate((2, 5, 9, 12)):
        for z in (2, 6):
            c.set(x, 1, z, PIERRE_S)
            c.set(x, 2, z, "les_limbes:vitrine[facing=south]")
    c.fill(7, 1, 3, 7, 1, 5, "minecraft:blue_carpet")
    hanging_lantern(c, 4, 4, 4); hanging_lantern(c, 10, 4, 4)
    c.sign_wall(6, 3, 0, "minecraft:oak", "north", ["Galerie des", "merveilles", "posez un objet", "dans une vitrine"])
    return c


def build_salle_passages() -> Canvas:
    """Salle des passages : rotonde avec le cadran des relais ; porte de relais du domaine."""
    c = Canvas("salle_passages", 9, 8, 9)
    c.note("Projet : salle des passages (sud). Rotonde de pierre, porte de relais du domaine reliée aux relais découverts.")
    W, D = 9, 9
    import math
    for x in range(W):
        for z in range(D):
            d = math.hypot(x - 4, z - 4)
            if d <= 4.3:
                c.set(x, 0, z, PIERRE_P if int(d) % 2 else PIERRE_T)
                if 3.4 < d <= 4.3:
                    c.column(x, 1, 4, z, PIERRE_T if (x + z) % 2 else PIERRE_S)
                    c.set(x, 5, z, slab(DALLE_PIERRE))
                else:
                    c.fill(x, 1, z, x, 5, z, AIR)
                    c.set(x, 6, z, PIERRE_P)
    c.set(4, 1, 0, AIR); c.set(4, 2, 0, AIR)  # entrée nord
    c.set(4, 7, 4, CRISTAL)
    c.set(4, 1, 4, "les_limbes:porte_de_relais[eveillee=false,facing=north]", {"id": "les_limbes:porte_de_relais", "relais": "manoir"})
    c.set(4, 2, 4, AIR)
    for x, z in [(1, 4), (7, 4), (4, 7)]:
        c.set(x, 1, z, LANTERNE)
    for x, z in [(2, 2), (6, 2), (2, 6), (6, 6)]:
        c.set(x, 4, z, LANTERNE_H) if c.get(x, 5, z) != VOID else None
    c.sign_standing(4, 1, 6, "minecraft:oak", 0, ["Salle des", "passages", "un pas, un relais", ""])
    return c


def build_chambres() -> Canvas:
    """Chambres des compagnons : grenier du manoir aménagé (lits, coffres, coin de lecture) ; scènes de vie des recrues."""
    c = Canvas("chambres", 21, 4, 7)
    c.note("Projet : chambres des compagnons (grenier du manoir). Quatre alcôves, coin commun, lanternes.")
    W, D = 21, 7
    c.fill(0, 0, 0, W - 1, 0, D - 1, PLANCHES)
    c.fill(0, 1, 0, W - 1, 3, D - 1, AIR)
    for i, x in enumerate((1, 6, 14, 19)):
        c.bed(x, 1, 2, ["blue", "white", "green", "red"][i], "north")
        c.set(x, 1, 1, "minecraft:chest[facing=south,type=single,waterlogged=false]") if i % 2 else c.set(x + 1, 1, 1, "minecraft:chest[facing=south,type=single,waterlogged=false]")
        c.set(x, 1, 5, "minecraft:lantern[hanging=false,waterlogged=false]")
        c.set(x, 1, 4, "minecraft:potted_azure_bluet") if i in (0, 3) else None
    for x in (4, 9, 11, 16):
        c.column(x, 1, 3, 0, PLANCHES); c.column(x, 1, 3, 1, PLANCHES)
    table(c, 9, 1, 3, 3, "x")
    chair(c, 9, 1, 4, "north"); chair(c, 11, 1, 4, "north")
    c.set(10, 1, 5, "minecraft:bookshelf"); c.set(9, 1, 5, "minecraft:bookshelf")
    hanging_lantern(c, 10, 3, 3)
    c.carpet(8, 1, 4, "light_blue"); c.carpet(12, 1, 4, "light_blue")
    return c


def build_jardin() -> Canvas:
    """Jardin d'agrément : belvédère, bains, bancs, massifs."""
    c = Canvas("jardin", 11, 6, 11)
    c.note("Projet : jardin d'agrément et bains (sud-est). Belvédère, bassin d'eau chaude, bancs, massifs de fleurs.")
    W, D = 11, 11
    c.fill(0, 0, 0, W - 1, 0, D - 1, HERBE)
    c.fill(3, 0, 3, 7, 0, 7, PIERRE_P)
    c.fill(4, 0, 4, 6, 0, 6, "minecraft:water[level=0]")
    c.set(5, 0, 5, PIERRE_S)
    for x, z in [(3, 3), (7, 3), (3, 7), (7, 7)]:
        c.column(x, 1, 3, z, BARRIERE); c.set(x, 4, z, LANTERNE)
    c.fill(3, 4, 3, 7, 4, 7, slab(DALLE_BOIS, "top")) if False else None
    for x in (3, 7):
        c.set(x, 1, 5, stair(STAIR_BOIS, "west" if x == 3 else "east"))
    for z in (3, 7):
        c.set(5, 1, z, stair(STAIR_BOIS, "north" if z == 3 else "south"))
    for x, z in [(1, 1), (9, 1), (1, 9), (9, 9), (1, 5), (9, 5), (5, 1), (5, 9)]:
        c.set(x, 0, z, TERRE); c.set(x, 1, z, FLEUR if (x + z) % 2 else HERBE_AUBE)
    c.fill(0, 0, 5, 2, 0, 5, "minecraft:dirt_path"); c.fill(8, 0, 5, 10, 0, 5, "minecraft:dirt_path")
    lamppost(c, 0, 1, 0, 3); lamppost(c, 10, 1, 10, 3)
    c.sign_standing(2, 1, 8, "minecraft:oak", 4, ["Bains du", "domaine", "eau de source", "chaude"])
    return c


def build_marche() -> Canvas:
    """Marché et ateliers : trois étals couverts, atelier de tissage, étal de cuisine ; Marguerite."""
    c = Canvas("marche", 15, 7, 7)
    c.note("Projet : marché et ateliers (sud). Trois étals couverts, atelier de tissage, cuisine de plein air ; Marguerite la cuisinière.")
    W, D = 15, 7
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    c.fill(1, 0, 1, W - 2, 0, D - 2, "minecraft:dirt_path")
    for i, x0 in enumerate((0, 5, 10)):
        for x, z in [(x0, 0), (x0 + 4, 0), (x0, D - 1), (x0 + 4, D - 1)]:
            c.column(x, 1, 3, z, log())
        c.fill(x0, 4, 0, x0 + 4, 4, D - 1, slab(DALLE_BOIS, "bottom") if i % 2 else ["minecraft:white_wool", "minecraft:light_blue_wool", "minecraft:white_wool"][i])
        counter(c, x0 + 1, 1, 1, 3, "x")
        c.set(x0 + 2, 2, 1, "minecraft:lantern[hanging=false,waterlogged=false]")
    c.set(1, 1, 5, "minecraft:loom[facing=north]"); c.set(2, 1, 5, "minecraft:barrel[facing=up,open=false]")
    c.set(6, 1, 5, "minecraft:smoker[facing=north,lit=true]"); c.set(7, 1, 5, "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]"); c.set(8, 1, 5, "minecraft:water_cauldron[level=3]")
    c.set(11, 1, 5, "minecraft:crafting_table"); c.set(12, 1, 5, "minecraft:chest[facing=north,type=single,waterlogged=false]"); c.set(13, 1, 5, "minecraft:composter[level=0]")
    c.item_frame(2, 2, 0, "south", "les_limbes:fibres_nacrees")
    c.item_frame(7, 2, 0, "south", "les_limbes:pain_d_emile")
    c.item_frame(12, 2, 0, "south", "les_limbes:lanterne_nacree")
    data_marker(c, 2, 1, 3, "pnj:marchand_1")
    data_marker(c, 12, 1, 3, "pnj:marchand_2")
    return c


# ============================================================================== RENCONTRES DES RECRUES
def build_cabane_loic() -> Canvas:
    """Cabane de Loïc, près du lac : abri de planches, foin, observation des raies."""
    c = Canvas("cabane_loic", 9, 9, 9)
    c.shift = (1, 0, 1)
    c.note("Cabane de Loïc l'éleveur, au bord du Lac du Crépuscule.")
    W, D = 7, 7
    c.fill(0, 0, 0, W - 1, 0, D - 1, PLANCHES)
    c.walls(0, 1, 0, W - 1, 3, D - 1, PLANCHES)
    c.fill(1, 1, 1, W - 2, 3, D - 2, AIR)
    corner_logs(c, 0, 1, 0, W - 1, D - 1, 3)
    c.gable_roof(0, 0, W - 1, D - 1, 4, STAIR_BOIS, axis="z", overhang=1)
    door(c, 3, 1, D - 1, "south")
    window(c, 3, 2, 0, "north", 1)
    c.bed(1, 1, 1, "brown", "north")
    c.set(5, 1, 1, "minecraft:hay_block[axis=y]"); c.set(5, 1, 2, "minecraft:hay_block[axis=y]")
    c.set(1, 1, 5, "minecraft:chest[facing=east,type=single,waterlogged=false]")
    c.set(5, 1, 5, "minecraft:potted_azure_bluet")
    hanging_lantern(c, 3, 3, 3)
    data_marker(c, 3, 1, 3, "pnj:loic")
    return c


def build_jardin_hanna() -> Canvas:
    """Jardin de Hanna : carrés de plantes, tonnelle, cabanon."""
    c = Canvas("jardin_hanna", 13, 7, 11)
    c.note("Jardin de Hanna la jardinière, sur le plateau de Clairval : carrés de cultures, tonnelle, cabanon.")
    W, D = 13, 11
    c.fill(0, 0, 0, W - 1, 0, D - 1, HERBE)
    fence_rect(c, 0, 1, 0, W - 1, D - 1, (6, D - 1, "south"))
    garden_bed(c, 1, 0, 1, 4, 3, HERBE_AUBE)
    garden_bed(c, 8, 0, 1, 11, 3, FLEUR)
    c.fill(1, 0, 6, 4, 0, 9, "minecraft:farmland[moisture=7]")
    for x in range(1, 5):
        for z in range(6, 10):
            c.set(x, 1, z, "minecraft:wheat[age=7]")
    c.fill(8, 0, 6, 11, 0, 9, PLANCHES)
    c.walls(8, 1, 6, 11, 3, 9, PLANCHES); c.fill(9, 1, 7, 10, 3, 8, AIR)
    c.fill(8, 4, 6, 11, 4, 9, slab(DALLE_BOIS, "bottom"))
    c.set(9, 1, 6, AIR); c.set(9, 2, 6, AIR)
    c.set(10, 1, 8, "minecraft:chest[facing=west,type=single,waterlogged=false]")
    c.set(10, 1, 7, "minecraft:composter[level=4]")
    c.set(6, 0, 5, "minecraft:water[level=0]")
    for x, z in [(5, 4), (7, 4), (5, 6), (7, 6)]:
        c.column(x, 1, 2, z, BARRIERE)
    c.fill(5, 3, 4, 7, 3, 6, FEUILLES)
    c.set(6, 2, 5, LANTERNE_H)
    data_marker(c, 6, 1, 7, "pnj:hanna")
    return c


# ============================================================================== ASSEMBLAGE ET EXPORT
def structure_json(biomes: str, adaptation: str, y: int, pieces: list[dict]) -> dict:
    return {"type": "les_limbes:composee", "biomes": biomes, "step": "surface_structures", "terrain_adaptation": adaptation,
            "spawn_overrides": {}, "ancre": [0, 0, 0], "y": y, "pieces": pieces}


def set_json(structure: str, chunk: tuple[int, int], salt: int) -> dict:
    return {"placement": {"type": "les_limbes:fixed", "salt": salt, "chunks": [[chunk[0], chunk[1]]]},
            "structures": [{"structure": structure, "weight": 1}]}


def piece(gabarit: str, world_min: tuple[int, int, int], origin: tuple[int, int, int]) -> dict:
    return {"gabarit": gabarit, "decalage": [world_min[0] - origin[0], world_min[1] - origin[1], world_min[2] - origin[2]]}


def write_json(path: str, obj) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as fh:
        json.dump(obj, fh, indent=2, ensure_ascii=False)
        fh.write("\n")


def render_png(c: Canvas, path: str, scale: int = 4) -> None:
    """Vue de dessus (bloc le plus haut) et coupe longitudinale, pour inspection rapide."""
    try:
        from PIL import Image, ImageDraw
    except ImportError:
        return
    colors = {}

    def color_of(state: str):
        name = state.split("[")[0]
        if name == AIR:
            return (0, 0, 0, 0)
        if name not in colors:
            h = zlib.crc32(name.encode("utf-8"))
            base = {
                "les_limbes:pierre_nacree": (233, 227, 218), "les_limbes:pierre_nacree_taillee": (215, 208, 198), "les_limbes:pierre_nacree_polie": (247, 243, 236),
                "les_limbes:pierre_nacree_sculptee": (227, 201, 126), "les_limbes:planches_d_aube": (227, 201, 175), "les_limbes:buche_d_aube": (185, 157, 143),
                "les_limbes:feuilles_d_aube": (232, 205, 184), "les_limbes:herbe_argentee": (180, 198, 172), "les_limbes:terre_des_limbes": (127, 111, 118),
                "les_limbes:fibres_nacrees": (223, 232, 234), "les_limbes:lanterne_nacree": (255, 240, 180), "les_limbes:porte_des_limbes": (60, 87, 160),
                "les_limbes:escalier_d_aube": (210, 185, 160), "les_limbes:escalier_de_pierre_nacree": (205, 198, 188), "les_limbes:cristal_de_nacre": (200, 230, 255),
                "minecraft:dirt_path": (160, 140, 100), "minecraft:water": (80, 150, 220), "minecraft:glass_pane": (200, 230, 240), "minecraft:bookshelf": (160, 110, 70),
                "minecraft:grass_block": (120, 170, 90), "minecraft:cobblestone": (120, 120, 120), "minecraft:mossy_cobblestone": (100, 130, 90),
            }.get(name, ((h >> 16) & 255, (h >> 8) & 255, h & 255))
            colors[name] = base + (255,)
        return colors[name]

    top = Image.new("RGBA", (c.sx * scale, c.sz * scale), (40, 40, 48, 255))
    d = ImageDraw.Draw(top)
    for x in range(c.sx):
        for z in range(c.sz):
            for y in range(c.sy - 1, -1, -1):
                st = c.blocks.get((x, y, z), VOID)
                if st != VOID and st != AIR:
                    col = color_of(st)
                    shade = 0.6 + 0.4 * (y / max(1, c.sy - 1))
                    col = (int(col[0] * shade), int(col[1] * shade), int(col[2] * shade), 255)
                    d.rectangle([x * scale, z * scale, x * scale + scale - 1, z * scale + scale - 1], fill=col)
                    break
    zc = c.sz // 2
    cut = Image.new("RGBA", (c.sx * scale, c.sy * scale), (40, 40, 48, 255))
    d2 = ImageDraw.Draw(cut)
    for x in range(c.sx):
        for y in range(c.sy):
            st = c.blocks.get((x, y, zc), VOID)
            if st != VOID and st != AIR:
                d2.rectangle([x * scale, (c.sy - 1 - y) * scale, x * scale + scale - 1, (c.sy - 1 - y) * scale + scale - 1], fill=color_of(st))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    top.save(path + "_dessus.png")
    cut.save(path + "_coupe.png")


def main() -> None:
    rapport = "--rapport" in sys.argv
    os.makedirs(STRUCT_DIR, exist_ok=True)
    os.makedirs(RAPPORT_DIR, exist_ok=True)
    canvases: dict[str, Canvas] = {
        "vestige/vestige": build_vestige(),
        "sanctuaire/ile": build_ile(),
        "sanctuaire/pont": build_pont(),
        "clairval/sol": build_clairval_sol(),
        "clairval/auberge": build_auberge(),
        "clairval/forge": build_forge(),
        "clairval/maison_1": build_maison("maison_1", "habitant_1", "light_blue", 1),
        "clairval/maison_2": build_maison("maison_2", "habitant_2", "white", 2),
        "clairval/maison_3": build_maison("maison_3", "habitant_3", "lime", 3),
        "clairval/moulin": build_moulin(),
        "manoir/manoir": build_manoir(),
        "relais/relais_royal": build_relais(),
        "clairval/observatoire": build_observatoire(),
        "arrivees/maison_egaree": build_maison_egaree(),
        "arrivees/caisse_tombee": build_caisse_tombee(),
        "arrivees/barque_echouee": build_barque_echouee(),
        "lac/lac_crepuscule": build_lac_crepuscule(),
        "manoir/aile_ouest": build_aile_ouest(),
        "manoir/forge_royale": build_forge_royale(),
        "manoir/ecuries": build_ecuries(),
        "manoir/serres": build_serres(),
        "manoir/maison_explorateurs": build_maison_explorateurs(),
        "manoir/galerie": build_galerie(),
        "manoir/salle_passages": build_salle_passages(),
        "manoir/chambres": build_chambres(),
        "manoir/jardin": build_jardin(),
        "manoir/marche": build_marche(),
        "rencontres/cabane_loic": build_cabane_loic(),
        "rencontres/jardin_hanna": build_jardin_hanna(),
        }
    for module in (MER, JARDINS, DESERT, MONTAGNES, COURONNE, ARENES, CAPITALE):
        for cle, fabrique in module.items():
            canvases[cle] = fabrique()
    lignes = ["# Rapport des structures\n"]
    for rel, c in canvases.items():
        out_dir = os.path.join(STRUCT_DIR, os.path.dirname(rel))
        c.name = os.path.basename(rel)
        p = c.save(out_dir)
        st = c.stats()
        lignes.append(f"## {rel}\n\n{c.notes[0] if c.notes else ''}\n\n- taille : {st['size']}\n- blocs : {st['blocks']} ; entités de bloc : {st['block_entities']} ; entités : {st['entities']}\n- palette : {', '.join(f'{k} ({v})' for k, v in list(st['palette'].items())[:12])}\n")
        if rapport:
            render_png(c, os.path.join(RAPPORT_DIR, rel.replace("/", "_")))
            if c.sx * c.sz <= 500:
                lignes.append("```\n" + c.ascii_layers() + "\n```\n")
        print(f"  {os.path.relpath(p, ROOT)}  {st['size']}  {st['blocks']} blocs  hors volume : {st['hors_volume']}")
    # Structures composées à position fixe (voir LimbesSites.java)
    WG = os.path.join(DATA, "worldgen")
    lim = "#les_limbes:limbes"
    # Sanctuaire : chunk (0,0), origine (0, 82, 0)
    o = (0, 82, 0)
    write_json(os.path.join(WG, "structure", "sanctuaire.json"), structure_json(lim, "none", 82, [
        piece("les_limbes:sanctuaire/ile", (-20, 68, -20), o),
        piece("les_limbes:sanctuaire/pont", (-3, 60, 18), o)]))
    write_json(os.path.join(WG, "structure_set", "sanctuaire.json"), set_json("les_limbes:sanctuaire", (0, 0), 1001))
    # Clairval : chunk (0,8), origine (0, 66, 128)
    o = (0, 66, 128)
    write_json(os.path.join(WG, "structure", "clairval.json"), structure_json(lim, "none", 66, [
        piece("les_limbes:clairval/sol", (-40, 65, 50), o),
        piece("les_limbes:clairval/auberge", (5, 65, 117), o),
        piece("les_limbes:clairval/forge", (11, 65, 141), o),
        piece("les_limbes:clairval/maison_1", (-23, 65, 123), o),
        piece("les_limbes:clairval/maison_2", (-21, 65, 145), o),
        piece("les_limbes:clairval/maison_3", (7, 65, 153), o),
        piece("les_limbes:clairval/moulin", (-32, 65, 118), o),
        piece("les_limbes:clairval/observatoire", (26, 65, 108), o),
        piece("les_limbes:rencontres/jardin_hanna", (54, 65, 106), o)]))
    write_json(os.path.join(WG, "structure_set", "clairval.json"), set_json("les_limbes:clairval", chunk_of(0, 140), 1002))
    # Manoir : chunk (-8,9), origine (-128, 66, 144)
    o = (-128, 66, 144)
    write_json(os.path.join(WG, "structure", "manoir.json"), structure_json(lim, "none", 66, [
        piece("les_limbes:manoir/manoir", (-133, 65, 139), o)]))
    write_json(os.path.join(WG, "structure_set", "manoir.json"), set_json("les_limbes:manoir", chunk_of(-120, 150), 1003))
    # Relais royal : chunk (9,3), origine (144, 66, 48)
    o = (144, 66, 48)
    write_json(os.path.join(WG, "structure", "relais_royal.json"), structure_json(lim, "none", 66, [
        piece("les_limbes:relais/relais_royal", (140, 65, 51), o)]))
    write_json(os.path.join(WG, "structure_set", "relais_royal.json"), set_json("les_limbes:relais_royal", chunk_of(150, 60), 1004))
    # Lac du Crépuscule : site (-60, 66, 20) ; l'origine d'une structure composée est le coin de son chunk
    o = (chunk_of(-60, 20)[0] * 16, 66, chunk_of(-60, 20)[1] * 16)
    write_json(os.path.join(WG, "structure", "lac_crepuscule.json"), structure_json(lim, "none", 66, [
        piece("les_limbes:lac/lac_crepuscule", (-82, 61, -2), o),
        piece("les_limbes:rencontres/cabane_loic", (-41, 65, 29), o)]))
    write_json(os.path.join(WG, "structure_set", "lac_crepuscule.json"), set_json("les_limbes:lac_crepuscule", chunk_of(-60, 20), 1005))
    # --- Régions (étape 5) : une structure composée par site, origine = coin du chunk du site, altitude fixe ---
    def site(nom, centre, y, pieces, salt):
        o = (chunk_of(centre[0], centre[1])[0] * 16, y, chunk_of(centre[0], centre[1])[1] * 16)
        write_json(os.path.join(WG, "structure", nom + ".json"), structure_json(lim, "none", y, [piece(g, wm, o) for g, wm in pieces]))
        write_json(os.path.join(WG, "structure_set", nom + ".json"), set_json("les_limbes:" + nom, chunk_of(centre[0], centre[1]), salt))

    site("port_nacre", (950, -750), 66, [("les_limbes:mer/port_nacre_ile", (922, 63, -778))], 1006)
    site("phare_du_dessous", (1440, -750), 84, [("les_limbes:mer/phare_du_dessous", (1435, 83, -755))], 1007)
    site("marche_flottant", (800, -600), 63, [("les_limbes:mer/marche_flottant", (788, 62, -612))], 1008)
    site("navire_perdu", (1150, -950), 40, [("les_limbes:mer/navire_perdu", (1140, 39, -979))], 1009)
    site("sanctuaire_des_bancs", (1050, -560), 44, [("les_limbes:mer/sanctuaire_des_bancs", (1042, 43, -568))], 1010)
    site("quai_des_falaises", (560, -360), 82, [("les_limbes:mer/quai_des_falaises", (553, 62, -371))], 1011)
    site("sylvecime", (1050, 350), 72, [("les_limbes:jardins/sylvecime", (1020, 71, 320))], 1012)
    site("serre_ancienne", (900, 520), 72, [("les_limbes:jardins/serre_ancienne", (891, 71, 508))], 1013)
    site("pepiniere_royale", (1220, 230), 72, [("les_limbes:jardins/pepiniere_royale", (1209, 71, 222))], 1014)
    site("arbre_creux", (1050, 620), 72, [("les_limbes:jardins/arbre_creux", (1042, 71, 612))], 1015)
    site("clairiere_du_geant", (1280, 480), 72, [("les_limbes:jardins/clairiere_du_geant", (1264, 71, 464))], 1016)
    site("meridiane", (150, 1250), 66, [("les_limbes:desert/meridiane", (115, 65, 1215))], 1017)
    site("observatoire_des_heures", (0, 1420), 66, [("les_limbes:desert/observatoire_des_heures", (-7, 65, 1413))], 1018)
    site("jardin_fertile", (380, 1130), 66, [("les_limbes:desert/jardin_fertile", (368, 65, 1118))], 1019)
    site("verreole", (-1150, 500), 96, [("les_limbes:montagnes/verreole", (-1182, 95, 468))], 1020)
    site("bains_des_sources", (-1000, 380), 90, [("les_limbes:montagnes/bains_des_sources", (-1010, 89, 370))], 1021)
    site("atelier_d_instruments", (-1320, 620), 100, [("les_limbes:montagnes/atelier_d_instruments", (-1328, 99, 613))], 1022)
    site("pont_qui_chante", (-1150, 680), 104, [("les_limbes:montagnes/pont_qui_chante", (-1154, 100, 653))], 1023)
    site("haute_garde", (-610, -610), 82, [("les_limbes:couronne/haute_garde", (-644, 81, -644))], 1024)
    site("quartier_des_portes", (-900, -720), 78, [("les_limbes:couronne/quartier_des_portes", (-925, 77, -740))], 1025)
    # --- Étape 6 : arènes des épreuves, campement, capitale ---
    site("jardin_de_l_epreuve", (1150, 300), 72, [("les_limbes:arenes/jardin_de_l_epreuve", (1130, 71, 280))], 1026)
    site("pont_du_vent_debout", (1000, -850), 90, [("les_limbes:arenes/pont_du_vent_debout", (960, 86, -880))], 1027)
    site("salle_des_miroirs", (-1230, 500), 96, [("les_limbes:arenes/salle_des_miroirs", (-1250, 95, 480))], 1028)
    site("cour_des_heures", (230, 1330), 66, [("les_limbes:arenes/cour_des_heures", (210, 65, 1310))], 1029)
    site("campement_de_l_archipel", (1250, -650), 66, [("les_limbes:arenes/campement_de_l_archipel", (1240, 65, -660))], 1030)
    site("capitale_place", (-900, -900), 86, [("les_limbes:capitale/place", (-930, 85, -930))], 1031)
    site("capitale_palais", (-900, -960), 86, [("les_limbes:capitale/palais", (-940, 85, -985))], 1032)
    site("capitale_marche", (-955, -880), 86, [("les_limbes:capitale/marche_royal", (-975, 85, -900))], 1033)
    site("capitale_ambassades", (-845, -880), 86, [("les_limbes:capitale/ambassades", (-865, 85, -900))], 1034)
    site("capitale_archives", (-955, -945), 86, [("les_limbes:capitale/archives", (-970, 85, -960))], 1035)
    site("capitale_ateliers", (-900, -840), 86, [("les_limbes:capitale/ateliers", (-920, 85, -860))], 1036)
    site("capitale_residences", (-838, -945), 86, [("les_limbes:capitale/residences", (-858, 85, -965))], 1037)
    with open(os.path.join(RAPPORT_DIR, "RAPPORT.md"), "w", encoding="utf-8") as fh:
        fh.write("\n".join(lignes))
    print(f"{len(canvases)} gabarits écrits ; rapport : {os.path.relpath(RAPPORT_DIR, ROOT)}/RAPPORT.md")


if __name__ == "__main__":
    main()
