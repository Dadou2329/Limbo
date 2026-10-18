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
    # Vaëron sur la place (monde (3, 66, 142)), panneau de bienvenue à l'entrée nord
    data_marker(c, L(3, 142)[0], 1, L(3, 142)[1], "pnj:vaeron")
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
    # Thibault derrière le comptoir
    data_marker(c, 4, 1, 3, "pnj:thibault")
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
    # Ailes figées (croix de barrières en façade sud, axe en bûche)
    c.set(4, 8, D - 1, log("z"))
    for i in range(1, 5):
        if c.inside(4 + i, 8, D - 1):
            c.set(4 + i, 8, D - 1, BARRIERE)
        if c.inside(4 - i, 8, D - 1):
            c.set(4 - i, 8, D - 1, BARRIERE)
        if c.inside(4, 8 + i, D - 1):
            c.set(4, 8 + i, D - 1, BARRIERE)
        if c.inside(4, 8 - i, D - 1):
            c.set(4, 8 - i, D - 1, BARRIERE)
    for i in range(1, 5):
        c.set(4 + i, 8, D - 1, "minecraft:white_wool") if i == 4 else None
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
    }
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
        piece("les_limbes:clairval/moulin", (-32, 65, 118), o)]))
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
    with open(os.path.join(RAPPORT_DIR, "RAPPORT.md"), "w", encoding="utf-8") as fh:
        fh.write("\n".join(lignes))
    print(f"{len(canvases)} gabarits écrits ; rapport : {os.path.relpath(RAPPORT_DIR, ROOT)}/RAPPORT.md")


if __name__ == "__main__":
    main()
