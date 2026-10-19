"""Gabarits de la capitale endormie (étape 6) : place des cérémonies, palais, marché royal, ambassades, archives, ateliers, résidences.

Tout est posé sur le plateau aplani de la Couronne (−900/−900, y 86 ; y local 0 = 85). Les lanternes royales sont éteintes et les
portes royales fermées : la capitale s'éveille par niveaux (quartiers → palais → couronnement), voir CapitaleManager.
"""
from __future__ import annotations

from builders import (BARRIERE, HERBE, LANTERNE, LANTERNE_H, PIERRE_P, PLANCHES, TERRE, bookshelves, chair, counter, data_marker, door, garden_bed, hanging_lantern,
                      table, tree, window)
from canvas import AIR, Canvas
from regions_couronne import BANNIERE, PC, PC_P, PC_S, PC_T, batiment_royal, stair

LANTERNE_RE = "les_limbes:lanterne_royale_eteinte[hanging=false,waterlogged=false]"
LANTERNE_REH = "les_limbes:lanterne_royale_eteinte[hanging=true,waterlogged=false]"
PONT_ROYAL = "les_limbes:pont_royal[forme=false]"
QUARTZ = "minecraft:quartz_block"
QUARTZ_P = "minecraft:quartz_pillar[axis=y]"
TAPIS_R = "minecraft:red_carpet"
TAPIS_B = "minecraft:blue_carpet"
OR = "minecraft:gold_block"
VITRE = "minecraft:light_blue_stained_glass_pane[east=false,north=false,south=false,waterlogged=false,west=false]"
EAU = "minecraft:water[level=0]"
FEUILLES = "les_limbes:feuilles_d_aube[distance=1,persistent=true,waterlogged=false]"


def porte_royale(niveau: int) -> str:
    return f"les_limbes:porte_royale[niveau={niveau},ouverte=false]"


def lampadaire_royal(c: Canvas, x: int, y: int, z: int, hauteur: int = 4) -> None:
    c.column(x, y, y + hauteur - 1, z, PC_P)
    c.set(x, y + hauteur, z, LANTERNE_RE)


def arche(c: Canvas, x: int, y: int, z: int, axe: str, niveau: int, largeur: int = 3, hauteur: int = 4) -> None:
    """Arche de quartier barrée d'une porte royale (ouverture largeur × hauteur, axe 'x' = arche traversée en z)."""
    for i in range(-largeur // 2 - 1, largeur // 2 + 2):
        px, pz = (x + i, z) if axe == "x" else (x, z + i)
        if abs(i) == largeur // 2 + 1:
            c.column(px, y, y + hauteur, pz, PC_P)
        else:
            c.set(px, y + hauteur + 1, pz, PC_T)
            for h in range(hauteur + 1):
                c.set(px, y + h, pz, porte_royale(niveau))
    c.set(x, y + hauteur + 2, z, LANTERNE_RE)


def salle(c: Canvas, x1: int, y: int, z1: int, x2: int, z2: int, h: int, sol: str = PLANCHES) -> None:
    c.fill(x1, y, z1, x2, y, z2, sol)
    c.fill(x1, y + 1, z1, x2, y + h, z2, AIR)


# ----------------------------------------------------------------------------- place des cérémonies

def build_place() -> Canvas:
    W, H, D = 61, 16, 61
    c = Canvas("place", W, H, D)
    c.note("Place des cérémonies : dallage royal, allées de pont royal (s'illuminent au couronnement), bassin sec, estrade et trône, quatre arches de quartier, héraut et chambellan.")
    cx, cz = 30, 30
    c.fill(0, 0, 0, W - 1, 0, D - 1, PC_T)
    for x in range(W):
        for z in range(D):
            if (x // 3 + z // 3) % 2 == 0 and max(abs(x - cx), abs(z - cz)) > 8:
                c.set(x, 0, z, PC)
    # allées de pont royal (croix) et disque central poli
    for i in range(-28, 29):
        if abs(i) > 9:
            c.set(cx + i, 0, cz, PONT_ROYAL)
            c.set(cx, 0, cz + i, PONT_ROYAL)
    for x in range(cx - 8, cx + 9):
        for z in range(cz - 8, cz + 9):
            if (x - cx) ** 2 + (z - cz) ** 2 <= 64:
                c.set(x, 0, z, PC_P)
    # bassin sec et colonne d'étoile
    for x in range(cx - 3, cx + 4):
        for z in range(cz - 3, cz + 4):
            if abs(x - cx) == 3 or abs(z - cz) == 3:
                c.set(x, 1, z, PC_S)
    c.column(cx, 1, 4, cz, QUARTZ_P)
    c.set(cx, 5, cz, LANTERNE_RE)
    # estrade au nord : trois marches, trône, bannières
    for n, z in enumerate((14, 13, 12)):
        c.fill(cx - 6 + n, 1 + n, z - 0, cx + 6 - n, 1 + n, z, PC_T)
    c.fill(cx - 5, 1, 8, cx + 5, 3, 11, PC_T)
    c.set(cx, 4, 9, stair("minecraft:quartz", "south"))
    c.set(cx - 1, 4, 8, OR)
    c.set(cx + 1, 4, 8, OR)
    c.set(cx, 5, 8, OR)
    for x in (cx - 5, cx + 5):
        c.column(x, 4, 7, 8, PC_P)
        c.set(x, 8, 8, LANTERNE_RE)
        c.set(x, 6, 9, BANNIERE)
    # lampadaires et bancs
    for (x, z) in ((cx - 12, cz - 12), (cx + 12, cz - 12), (cx - 12, cz + 12), (cx + 12, cz + 12), (cx - 20, cz), (cx + 20, cz), (cx, cz + 20)):
        lampadaire_royal(c, x, 1, z, 4)
    for (x, z) in ((cx - 15, cz - 4), (cx + 13, cz - 4), (cx - 15, cz + 4), (cx + 13, cz + 4)):
        c.fill(x, 1, z, x + 2, 1, z, stair("minecraft:quartz", "south" if z < cz else "north"))
    # arches de quartier (niveau 1) sur les quatre allées
    arche(c, cx, 1, 2, "x", 1)
    arche(c, cx, 1, D - 3, "x", 1)
    arche(c, 2, 1, cz, "z", 1)
    arche(c, W - 3, 1, cz, "z", 1)
    data_marker(c, cx + 3, 1, 16, "pnj:heraut")
    data_marker(c, cx - 4, 1, 18, "pnj:chambellan")
    data_marker(c, cx + 9, 1, cz + 6, "pnj:habitant_1")
    data_marker(c, cx - 10, 1, cz + 7, "pnj:habitant_2")
    return c


# ----------------------------------------------------------------------------- palais

def build_palais() -> Canvas:
    W, H, D = 81, 36, 51
    c = Canvas("palais", W, H, D)
    c.note("Palais royal : façade monumentale et grande porte royale (niveau 2), salle du trône, salle du conseil, archives, cuisines et service, appartements sur deux étages, jardin intérieur, deux tours.")
    cx = 40
    # soubassement et parvis
    c.fill(0, 0, 0, W - 1, 0, D - 1, PC_T)
    c.fill(10, 1, 3, W - 11, 1, D - 4, PC_T)
    # corps principal : x 14..66, z 6..44 ; murs jusqu'à y 13
    x1, z1, x2, z2 = 14, 6, 66, 44
    c.fill(x1, 1, z1, x2, 1, z2, PC_P)
    c.walls(x1, 2, z1, x2, 13, z2, PC_T)
    c.fill(x1 + 1, 2, z1 + 1, x2 - 1, 13, z2 - 1, AIR)
    c.fill(x1, 14, z1, x2, 14, z2, PC_T)
    for x in range(x1, x2 + 1, 4):
        c.column(x, 2, 14, z2, PC_P)
        c.column(x, 2, 14, z1, PC_P)
    for z in range(z1, z2 + 1, 4):
        c.column(x1, 2, 14, z, PC_P)
        c.column(x2, 2, 14, z, PC_P)
    # fenêtres hautes de la façade sud
    for x in range(x1 + 2, x2 - 1, 4):
        for y in (5, 6, 9, 10):
            c.set(x, y, z2, VITRE)
    # grande porte royale (niveau 2) au sud, 5 de large, 6 de haut, encadrée de colonnes
    for dx in range(-2, 3):
        for y in range(2, 8):
            c.set(cx + dx, y, z2, porte_royale(2))
    c.column(cx - 3, 2, 10, z2, QUARTZ_P)
    c.column(cx + 3, 2, 10, z2, QUARTZ_P)
    c.fill(cx - 3, 11, z2, cx + 3, 11, z2, PC_S)
    c.set(cx - 3, 12, z2 + 1, LANTERNE_RE)
    c.set(cx + 3, 12, z2 + 1, LANTERNE_RE)
    c.fill(cx - 4, 1, z2 + 1, cx + 4, 1, z2 + 4, PC_P)
    # salle du trône : x 30..50, z 20..43
    salle(c, 31, 1, 21, 49, 43, 11, PC_P)
    c.fill(cx - 1, 1, 22, cx + 1, 1, 42, TAPIS_R)
    for x in (33, 47):
        for z in range(24, 42, 5):
            c.column(x, 2, 11, z, QUARTZ_P)
            c.set(x, 8, z, LANTERNE_RE)
            c.set(x, 6, z + 1, BANNIERE)
    c.fill(cx - 4, 2, 21, cx + 4, 2, 24, PC_T)
    c.fill(cx - 3, 3, 21, cx + 3, 3, 23, PC_T)
    c.set(cx, 4, 22, stair("minecraft:quartz", "south"))
    c.set(cx - 1, 4, 21, OR)
    c.set(cx + 1, 4, 21, OR)
    c.set(cx, 5, 21, OR)
    c.set(cx, 10, 30, LANTERNE_REH)
    c.set(cx, 10, 38, LANTERNE_REH)
    # cloisons entre la salle du trône et les ailes
    c.fill(30, 2, 21, 30, 12, 43, PC_T)
    c.fill(50, 2, 21, 50, 12, 43, PC_T)
    for z in (28, 36):
        c.fill(30, 2, z, 30, 4, z + 1, AIR)
        c.fill(50, 2, z, 50, 4, z + 1, AIR)
    # aile ouest : salle du conseil (x 15..29, z 26..43) ; cuisines et service (z 7..24)
    salle(c, 15, 1, 26, 29, 43, 6)
    table(c, 19, 1, 33, 6, "x")
    for x in range(19, 25):
        chair(c, x, 1, 32, "south")
        chair(c, x, 1, 34, "north")
    c.fill(16, 1, 27, 28, 1, 42, TAPIS_B)
    bookshelves(c, 15, 2, 27, 15, 4, 42)
    hanging_lantern(c, 22, 6, 34)
    c.set(22, 7, 34, LANTERNE_REH)
    c.fill(15, 1, 25, 29, 7, 25, PC_T)
    c.fill(21, 2, 25, 23, 4, 25, AIR)
    salle(c, 15, 1, 7, 29, 24, 5)
    for x in range(16, 28, 2):
        c.set(x, 1, 8, "minecraft:smoker[facing=south,lit=false]" if x % 4 == 0 else "minecraft:barrel[facing=up,open=false]")
    counter(c, 16, 1, 12, 12, "x")
    c.set(20, 1, 15, "minecraft:cauldron")
    c.chest(28, 1, 10, "west", "les_limbes:chests/capitainerie")
    data_marker(c, 22, 1, 16, "pnj:intendante_du_palais")
    hanging_lantern(c, 22, 5, 14)
    # aile est : archives (x 51..65, z 26..43) ; appartements sur deux étages (z 7..24)
    salle(c, 51, 1, 26, 65, 43, 6)
    bookshelves(c, 65, 2, 27, 65, 5, 42)
    bookshelves(c, 52, 2, 43, 64, 5, 43)
    for z in (30, 36):
        bookshelves(c, 55, 2, z, 61, 3, z)
    c.lectern(58, 1, 40, "south", Canvas.written_book("Registre des Arrivées", "Archives royales", [
        "Année du Dernier Roi. Les Arrivées se font plus nombreuses : un moulin, une tour, un navire entier.\n\nSa Majesté dit qu'elles répondent à la couronne.",
        "Le roi part vers les frontières, seul, avec le sceau. Il dit : « Si je ne reviens pas, c'est que la lumière tient. »\n\nLes routes se sont éteintes la nuit suivante."]))
    hanging_lantern(c, 58, 6, 34)
    c.fill(51, 1, 25, 65, 7, 25, PC_T)
    c.fill(57, 2, 25, 59, 4, 25, AIR)
    salle(c, 51, 1, 7, 65, 24, 5)
    for i, z in enumerate((9, 14, 19)):
        c.bed(53, 1, z, "red" if i % 2 == 0 else "blue", "west")
        c.bed(63, 1, z, "white", "east")
        table(c, 57, 1, z, 2, "x")
    c.fill(51, 6, 7, 65, 6, 24, PLANCHES)
    salle(c, 51, 6, 7, 65, 24, 5)
    for z in (9, 14, 19):
        c.bed(53, 7, z, "blue", "west")
        c.bed(63, 7, z, "red", "east")
    c.fill(57, 1, 23, 59, 5, 23, "minecraft:ladder[facing=north,waterlogged=false]")
    # jardin intérieur au centre nord (x 31..49, z 7..19) à ciel ouvert
    c.fill(31, 1, 7, 49, 1, 19, HERBE)
    c.fill(31, 2, 7, 49, 14, 19, AIR)
    c.fill(39, 1, 12, 41, 1, 14, PC_T)
    c.set(40, 1, 13, EAU)
    for (x, z) in ((34, 10), (46, 10), (34, 16), (46, 16)):
        tree(c, x, 1, z, 4, 1)
    garden_bed(c, 32, 1, 18, 48, 18, "les_limbes:fleur_de_rosee")
    c.fill(31, 2, 20, 49, 7, 20, PC_T)
    c.fill(38, 2, 20, 42, 4, 20, AIR)
    # toiture : dalles et lanternes éteintes
    c.fill(x1, 15, z1, x2, 15, z2, PC_P)
    for x in range(x1 + 4, x2, 8):
        for z in (z1 + 1, z2 - 1):
            c.set(x, 16, z, LANTERNE_RE)
    # tours d'angle (9×9, y 1..30)
    for (tx, tz) in ((6, 36), (W - 15, 36)):
        c.fill(tx, 1, tz, tx + 8, 1, tz + 8, PC_T)
        c.walls(tx, 2, tz, tx + 8, 28, tz + 8, PC_T)
        c.fill(tx + 1, 2, tz + 1, tx + 7, 28, tz + 7, AIR)
        for y in range(2, 28, 4):
            c.fill(tx + 1, y, tz + 1, tx + 7, y, tz + 7, PLANCHES)
            c.fill(tx + 4, y + 1, tz + 4, tx + 4, y + 4, tz + 4, "minecraft:ladder[facing=north,waterlogged=false]")
        for y in (6, 12, 18, 24):
            window(c, tx + 4, y, tz + 8, "south", 1)
            window(c, tx, y, tz + 4, "west", 1)
            window(c, tx + 8, y, tz + 4, "east", 1)
        c.fill(tx, 29, tz, tx + 8, 29, tz + 8, PC_P)
        c.fill(tx + 1, 30, tz + 1, tx + 7, 30, tz + 7, PC_T)
        c.set(tx + 4, 31, tz + 4, LANTERNE_RE)
        c.fill(tx + 3, 2, tz + 8, tx + 5, 4, tz + 8, AIR)
    # passerelles du parvis vers les tours
    c.fill(10, 1, 40, 13, 1, 42, PC_P)
    c.fill(W - 14, 1, 40, W - 11, 1, 42, PC_P)
    data_marker(c, cx - 3, 1, 26, "pnj:conseiller_royal")
    return c


# ----------------------------------------------------------------------------- quartiers

def build_marche_royal() -> Canvas:
    W, H, D = 41, 16, 41
    c = Canvas("marche_royal", W, H, D)
    c.note("Grand marché de la capitale : deux comptoirs couverts, six étals, lampadaires royaux éteints.")
    c.fill(0, 0, 0, W - 1, 0, D - 1, PC_T)
    batiment_royal(c, 3, 1, 3, 13, 11, 5, "marchand_royal_1", "south", ["Comptoir", "des routes"])
    batiment_royal(c, 25, 1, 3, 13, 11, 5, "marchand_royal_2", "south", ["Épices et", "lanternes"])
    for i, x in enumerate((5, 12, 19, 26, 33)):
        counter(c, x, 1, 24, 3, "x")
        c.fill(x, 4, 23, x + 2, 4, 25, "minecraft:red_wool" if i % 2 == 0 else "minecraft:blue_wool")
        c.column(x - 1, 1, 3, 23, "minecraft:oak_fence[east=false,north=false,south=false,waterlogged=false,west=false]")
        c.column(x + 3, 1, 3, 25, "minecraft:oak_fence[east=false,north=false,south=false,waterlogged=false,west=false]")
    for (x, z) in ((8, 20), (32, 20), (20, 34), (20, 16)):
        lampadaire_royal(c, x, 1, z, 4)
    data_marker(c, 20, 1, 28, "pnj:habitant_3")
    return c


def build_ambassades() -> Canvas:
    W, H, D = 41, 18, 41
    c = Canvas("ambassades", W, H, D)
    c.note("Quartier des ambassades : trois maisons des régions (Mer, Jardins, Verre) autour d'une cour, bannières et lanternes éteintes.")
    c.fill(0, 0, 0, W - 1, 0, D - 1, PC_T)
    batiment_royal(c, 3, 1, 3, 15, 11, 6, "ambassadeur_mer", "south", ["Ambassade", "de la Mer"])
    batiment_royal(c, 23, 1, 3, 15, 11, 6, "ambassadrice_jardins", "south", ["Ambassade", "des Jardins"])
    batiment_royal(c, 13, 1, 26, 15, 11, 6, "ambassadeur_verre", "north", ["Ambassade", "de Verre"])
    c.fill(5, 0, 4, 16, 0, 12, "les_limbes:sable_de_nacre")
    c.fill(25, 0, 4, 36, 0, 12, "les_limbes:planches_de_geant")
    c.fill(15, 0, 27, 26, 0, 35, "les_limbes:pierre_de_verre_taillee")
    for (x, z) in ((10, 18), (30, 18), (20, 22)):
        lampadaire_royal(c, x, 1, z, 4)
        c.set(x, 2, z + 1, BANNIERE)
    return c


def build_archives() -> Canvas:
    W, H, D = 31, 24, 31
    c = Canvas("archives", W, H, D)
    c.note("Tour des archives royales : trois niveaux de rayonnages, lutrins, la chronique du roi disparu ; l'archiviste royale veille.")
    c.fill(0, 0, 0, W - 1, 0, D - 1, PC_T)
    x1, z1, x2, z2 = 8, 8, 22, 22
    c.fill(x1, 1, z1, x2, 1, z2, PC_P)
    c.walls(x1, 2, z1, x2, 18, z2, PC_T)
    c.fill(x1 + 1, 2, z1 + 1, x2 - 1, 18, z2 - 1, AIR)
    for y in (7, 13):
        c.fill(x1 + 1, y, z1 + 1, x2 - 1, y, z2 - 1, PLANCHES)
        c.fill(x1 + 1, y, z1 + 1, x1 + 2, y, z1 + 2, AIR)
    c.fill(x1 + 1, 2, z1 + 1, x1 + 1, 17, z1 + 1, "minecraft:ladder[facing=south,waterlogged=false]")
    for y0 in (2, 8, 14):
        bookshelves(c, x1 + 1, y0, z2 - 1, x2 - 1, y0 + 3, z2 - 1)
        bookshelves(c, x2 - 1, y0, z1 + 1, x2 - 1, y0 + 3, z2 - 2)
        bookshelves(c, x1 + 3, y0, z1 + 1, x2 - 2, y0 + 3, z1 + 1)
        window(c, x1, y0 + 2, 15, "west", 1)
        hanging_lantern(c, 15, y0 + 5, 15)
    c.lectern(15, 2, 15, "south", Canvas.written_book("Chronique du roi disparu", "Archiviste royale", [
        "Le dernier roi a régné trente ans. Sous lui, les routes de lumière reliaient six régions et la capitale.\n\nIl parlait aux Arrivées comme à des invités.",
        "Quand les brumes ont gagné les frontières, il est parti les tenir, avec son sceau pour seule escorte.\n\nIl n'est jamais revenu ; les routes se sont éteintes.",
        "Ceux qui portent un fragment de sa couronne sentent les Arrivées avant les autres.\n\nLa couronne appelle ce qui se perd. Elle appelle aussi son héritier."]))
    door(c, 15, 2, z2, "south")
    c.fill(x1, 19, z1, x2, 19, z2, PC_P)
    c.fill(x1 + 2, 20, z1 + 2, x2 - 2, 20, z2 - 2, PC_T)
    c.set(15, 21, 15, LANTERNE_RE)
    data_marker(c, 15, 2, 12, "pnj:archiviste_royale")
    lampadaire_royal(c, 15, 1, 26, 4)
    return c


def build_ateliers() -> Canvas:
    W, H, D = 41, 18, 41
    c = Canvas("ateliers", W, H, D)
    c.note("Quartier des ateliers : forge royale de la capitale, atelier de cristal, auvent de travail, lampadaires éteints.")
    c.fill(0, 0, 0, W - 1, 0, D - 1, PC_T)
    batiment_royal(c, 3, 1, 5, 15, 11, 5, "artisan_royal_1", "south", ["Forge de", "la capitale"])
    batiment_royal(c, 23, 1, 5, 15, 11, 5, "artisan_royal_2", "south", ["Atelier", "de cristal"])
    c.set(10, 1, 9, "minecraft:anvil[facing=east]")
    c.set(8, 1, 9, "minecraft:blast_furnace[facing=south,lit=false]")
    c.set(30, 1, 9, "minecraft:grindstone[face=floor,facing=south]")
    c.set(32, 1, 9, "les_limbes:cristal_resonnant")
    c.fill(12, 1, 24, 28, 1, 30, PC_P)
    for x in (12, 28):
        for z in (24, 30):
            c.column(x, 1, 4, z, "les_limbes:buche_d_aube[axis=y]")
    c.fill(12, 5, 24, 28, 5, 30, "les_limbes:planches_d_aube")
    c.set(20, 1, 27, "minecraft:smithing_table")
    c.set(16, 1, 27, "minecraft:stonecutter[facing=south]")
    c.set(24, 1, 27, "minecraft:loom[facing=south]")
    lampadaire_royal(c, 6, 1, 34, 4)
    lampadaire_royal(c, 34, 1, 34, 4)
    return c


def build_residences() -> Canvas:
    W, H, D = 41, 16, 41
    c = Canvas("residences", W, H, D)
    c.note("Quartier des résidences : six maisons de pierre de couronne, jardins, lampadaires éteints.")
    c.fill(0, 0, 0, W - 1, 0, D - 1, PC_T)
    roles = ["habitant_1", "habitant_2", "habitant_3", None, None, None]
    i = 0
    for z0 in (3, 23):
        for x0 in (3, 16, 29):
            batiment_royal(c, x0, 1, z0, 9, 9, 4, roles[i], "south" if z0 == 3 else "north")
            i += 1
    c.fill(2, 0, 14, 38, 0, 20, HERBE)
    garden_bed(c, 4, 1, 15, 12, 19, "les_limbes:fleur_de_rosee")
    garden_bed(c, 28, 1, 15, 36, 19, "les_limbes:herbe_de_l_aube")
    tree(c, 20, 1, 17, 5, 2)
    lampadaire_royal(c, 14, 1, 17, 4)
    lampadaire_royal(c, 26, 1, 17, 4)
    return c


CAPITALE = {
    "capitale/place": build_place,
    "capitale/palais": build_palais,
    "capitale/marche_royal": build_marche_royal,
    "capitale/ambassades": build_ambassades,
    "capitale/archives": build_archives,
    "capitale/ateliers": build_ateliers,
    "capitale/residences": build_residences,
}
