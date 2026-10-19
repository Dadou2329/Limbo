"""Structures de l'acte V, le règne (étape 7) : chantier naval et navire royal, île des ruines, lisière des brumes,
et la cité arrivée de Brumeval (Arrivée majeure)."""
from __future__ import annotations

import math

from builders import (BARRIERE, LANTERNE, LANTERNE_H, PIERRE_P, PIERRE_T, PLANCHES, TERRE, bookshelves, chair, counter, data_marker, door, hanging_lantern, lamppost,
                      table, window)
from canvas import AIR, Canvas
from regions_couronne import BANNIERE, LANTERNE_R, LANTERNE_RH, PC, PC_P, PC_S, PC_T, batiment_royal, relais, stair
from arenes import LANTERNE_RE, LANTERNE_REH, SABLE_N, BUCHE_A, BUCHE_AX, BUCHE_AZ, PLANCHES_A, BARRIERE_A, LAINE_B, LAINE_BL, FEU, TONNEAU, disque, anneau_points

EAU = "minecraft:water[level=0]"
CORAIL = "les_limbes:corail_de_nacre"
ECHAFAUD = "minecraft:scaffolding[bottom=false,distance=0,waterlogged=false]"
ECHELLE = "minecraft:ladder[facing=west,waterlogged=false]"
HERBE_AUBE = "les_limbes:herbe_de_l_aube"
HERBE = "les_limbes:herbe_argentee"
VERRE_B = "minecraft:light_blue_stained_glass_pane[east=false,north=false,south=false,waterlogged=false,west=false]"


# ----------------------------------------------------------------------------- navire royal : coque commune

def _coque(c: Canvas, cx: int, z0: int, z1: int, y_fond: int, y_pont: int, interieur: str = AIR) -> None:
    """Coque elliptique le long de z : fond à y_fond, pont (planches) à y_pont ; l'intérieur est vidé (cale sèche)."""
    for z in range(z0, z1 + 1):
        t = (z - z0) / max(1, z1 - z0)
        demi = int(round(7 * math.sin(math.pi * min(1.0, max(0.0, t))) ** 0.55)) if 0.03 < t < 0.97 else 1
        for y in range(y_fond, y_pont + 1):
            lx = int(round(demi * (0.35 + 0.65 * min(1.0, (y - y_fond) / 6.0))))
            for x in range(cx - lx, cx + lx + 1):
                bord = x in (cx - lx, cx + lx) or y == y_fond or y == y_pont
                if bord:
                    c.set(x, y, z, PLANCHES_A if y == y_pont else BUCHE_AZ)
                else:
                    c.set(x, y, z, interieur)


def build_chantier_naval() -> Canvas:
    """Coque nue du navire royal sur son chantier (monde : origine 1021/55/-820, pont à 64), échafaudages, ponton du charpentier."""
    W, H, D = 19, 30, 41
    c = Canvas("chantier_naval", W, H, D)
    c.note("Chantier naval au large de Port-Nacre : coque du navire royal en cours de construction, échafaudages, ponton et atelier du charpentier royal. Remplacé par le navire achevé (action poser_structure).")
    cx = 9
    _coque(c, cx, 3, 37, 1, 9, interieur=AIR)
    # échafaudages le long de la coque, encore sans mâts ni gréement
    for z in range(6, 36, 6):
        for x in (cx - 8, cx + 8):
            c.column(x, 1, 10, z, ECHAFAUD)
        c.fill(cx - 8, 10, z, cx + 8, 10, z, PLANCHES_A)
    # ponton d'accès (est) et atelier du charpentier, hors de l'eau
    c.fill(cx + 7, 9, 18, cx + 9, 9, 22, PLANCHES_A)
    for x in (cx + 7, cx + 9):
        for z in (18, 22):
            c.column(x, 1, 8, z, BUCHE_A)
    c.set(cx + 9, 10, 18, LANTERNE)
    c.set(cx + 9, 10, 22, LANTERNE)
    c.set(cx + 8, 10, 19, TONNEAU)
    c.set(cx + 8, 10, 21, "minecraft:crafting_table")
    c.sign_wall(cx + 8, 11, 20, "minecraft:birch", "east", ["Chantier", "naval royal", "", "Josse, charpentier"])
    data_marker(c, cx + 8, 10, 20, "pnj:charpentier_royal")
    return c


def build_navire_royal() -> Canvas:
    """Navire royal achevé, même emprise que le chantier : pont, cabine arrière avec porte de relais, deux mâts, voiles, lanternes."""
    W, H, D = 19, 30, 41
    c = Canvas("navire_royal", W, H, D)
    c.note("Navire royal achevé : coque de bois d'aube, pont, cabine arrière (porte de relais « navire_royal », cartes, coffre), deux mâts et voiles blanches et bleues, proue de nacre. Posé par le capitaine Lio une fois les matériaux réunis.")
    cx = 9
    _coque(c, cx, 3, 37, 1, 9, interieur=AIR)
    # les échafaudages disparaissent (air explicite aux anciens emplacements)
    for z in range(6, 36, 6):
        for x in (cx - 8, cx + 8):
            c.column(x, 1, 10, z, AIR)
        for x in range(cx - 8, cx + 9):
            if abs(x - cx) > 6:
                c.set(x, 10, z, AIR)
    # ponton conservé
    c.fill(cx + 7, 9, 18, cx + 9, 9, 22, PLANCHES_A)
    for x in (cx + 7, cx + 9):
        for z in (18, 22):
            c.column(x, 1, 8, z, BUCHE_A)
    c.set(cx + 9, 10, 18, LANTERNE)
    c.set(cx + 9, 10, 22, LANTERNE)
    c.set(cx + 8, 10, 19, TONNEAU)
    c.set(cx + 8, 10, 21, "minecraft:crafting_table")
    # rambardes du pont
    for z in range(5, 36):
        t = (z - 3) / 34
        demi = int(round(7 * math.sin(math.pi * min(1.0, max(0.0, t))) ** 0.55))
        if demi >= 3:
            c.set(cx - demi, 10, z, BARRIERE_A)
            c.set(cx + demi, 10, z, BARRIERE_A)
    # proue de nacre
    c.column(cx, 8, 12, 3, "les_limbes:pierre_nacree_sculptee")
    c.set(cx, 13, 3, "les_limbes:cristal_de_nacre")
    # mâts et voiles
    for mz, haut in ((14, 26), (26, 28)):
        c.column(cx, 10, haut, mz, BUCHE_A)
        c.fill(cx - 5, haut - 7, mz, cx + 5, haut - 7, mz, BUCHE_AX)
        for x in range(cx - 5, cx + 6):
            for y in range(haut - 6, haut - 1):
                c.set(x, y, mz, LAINE_BL if (y - (haut - 6)) % 4 == 0 else LAINE_B)
        c.set(cx, haut + 1, mz, BANNIERE)
    # cabine arrière : y 10..13, z 30..36
    c.fill(cx - 4, 10, 30, cx + 4, 10, 36, PLANCHES_A)
    c.walls(cx - 4, 11, 30, cx + 4, 13, 36, PLANCHES_A)
    c.fill(cx - 3, 11, 31, cx + 3, 13, 35, AIR)
    c.fill(cx - 4, 14, 30, cx + 4, 14, 36, PLANCHES_A)
    door(c, cx, 11, 30, "north")
    window(c, cx - 2, 12, 30, "north", 1, VERRE_B)
    window(c, cx + 2, 12, 30, "north", 1, VERRE_B)
    relais(c, cx, 11, 34, "north", "navire_royal")
    c.set(cx - 3, 11, 35, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/navire_royal"})
    table(c, cx + 2, 11, 32, 2, "z")
    c.lectern(cx + 3, 11, 31, "west", Canvas.written_book("Routes de l'archipel", "Capitaine Lio", [
        "Le navire royal est bâti. Les routes que Kaël rouvre, nous les tiendrons sur l'eau.\n\nPremière traversée : l'île des ruines, au nord-est, là où la baleine chante.",
        "On dit qu'une expédition du roi s'y est arrêtée autrefois et y a laissé son journal. Ramenez-le : les cartes de Mina en ont besoin."]))
    hanging_lantern(c, cx, 13, 33)
    c.set(cx - 4, 14, 30, LANTERNE)
    c.set(cx + 4, 14, 30, LANTERNE)
    c.set(cx, 15, 36, BANNIERE)
    data_marker(c, cx + 2, 10, 20, "pnj:exploratrice_mina")
    return c


# ----------------------------------------------------------------------------- île des ruines

def build_ile_des_ruines() -> Canvas:
    W, H, D = 41, 26, 41
    c = Canvas("ile_des_ruines", W, H, D)
    c.note("Île des ruines : îlot de sable de nacre et d'herbe, colonnes royales brisées en cercle, tour de guet, coffre au journal de l'expédition royale ; une baleine des hauteurs tourne au-dessus, deux gardiens des épaves rôdent.")
    cx, cz = 20, 20
    disque(c, cx, 0, cz, 19.5, SABLE_N)
    disque(c, cx, 0, cz, 14.0, TERRE)
    for x, z in anneau_points(cx, cz, 9, 24):
        c.set(x, 1, z, HERBE_AUBE)
    disque(c, cx, 1, cz, 6.5, PC_P)
    disque(c, cx, 1, cz, 6.5, PC_S, 5.6)
    for i, (x, z) in enumerate(anneau_points(cx, cz, 10, 8, math.pi / 8)):
        h = 3 + (i * 5) % 6
        c.column(x, 1, h, z, PC_P)
        if h >= 7:
            c.set(x, h + 1, z, PC_T)
    # tour de guet (nord-est) avec lanterne
    tx, tz = cx + 11, cz - 11
    c.walls(tx - 2, 1, tz - 2, tx + 2, 9, tz + 2, PC_T)
    c.fill(tx - 1, 1, tz - 1, tx + 1, 9, tz + 1, AIR)
    c.fill(tx - 2, 10, tz - 2, tx + 2, 10, tz + 2, PC_P)
    for y in range(1, 10):
        c.set(tx, y, tz, "minecraft:ladder[facing=north,waterlogged=false]")
    c.set(tx - 2, 2, tz, AIR)
    c.set(tx - 2, 3, tz, AIR)
    c.set(tx, 11, tz, LANTERNE_R)
    # oratoire des ruines : coffre au journal
    c.fill(cx - 2, 2, cz - 2, cx + 2, 2, cz + 2, PC_T)
    c.set(cx, 3, cz, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/ruines"})
    c.set(cx - 2, 3, cz - 2, LANTERNE_RE)
    c.set(cx + 2, 3, cz - 2, LANTERNE_RE)
    for x, z in anneau_points(cx, cz, 15, 5, 0.3):
        c.set(x, 1, z, CORAIL)
    c.sign_standing(cx, 3, cz + 3, "minecraft:birch", 0, ["Expédition", "royale", "an 12 du roi", "Nous attendons"])
    data_marker(c, cx, 14, cz, "entite:les_limbes:baleine_des_hauteurs")
    data_marker(c, cx - 13, 1, cz + 9, "entite:les_limbes:gardien_des_epaves")
    data_marker(c, cx + 9, 1, cz + 13, "entite:les_limbes:gardien_des_epaves")
    return c


# ----------------------------------------------------------------------------- lisière des brumes

def build_lisiere_des_brumes() -> Canvas:
    W, H, D = 41, 22, 41
    c = Canvas("lisiere_des_brumes", W, H, D)
    c.note("Lisière des brumes : mur de pierre de couronne percé d'une porte, grande tour-lanterne, braseros, tentes de garde ; l'ancien roi tient la lumière. Les vagues des brumes arrivent du nord.")
    cx = 20
    # sol : dalles et herbe argentée
    c.fill(0, 0, 0, W - 1, 0, D - 1, TERRE)
    for x in range(W):
        for z in range(D):
            if (x * 3 + z * 7) % 5 == 0:
                c.set(x, 1, z, HERBE)
    c.fill(cx - 12, 0, 14, cx + 12, 0, 26, PC_T)
    # mur est-ouest à z 10..11, haut de 6, porte au centre
    c.fill(0, 1, 10, W - 1, 6, 11, PC)
    for x in range(0, W, 4):
        c.column(x, 1, 7, 10, PC_P)
        c.column(x, 1, 7, 11, PC_P)
    c.fill(cx - 2, 1, 10, cx + 2, 4, 11, AIR)
    c.fill(cx - 2, 5, 10, cx + 2, 5, 11, PC_S)
    for x in range(0, W, 8):
        c.set(x, 8, 10, BANNIERE)
    # tour-lanterne au centre du camp
    tx, tz = cx, 20
    c.walls(tx - 2, 1, tz - 2, tx + 2, 11, tz + 2, PC_T)
    c.fill(tx - 1, 1, tz - 1, tx + 1, 11, tz + 1, AIR)
    for y in range(1, 12):
        c.set(tx, y, tz, "minecraft:ladder[facing=south,waterlogged=false]")
    c.fill(tx - 3, 12, tz - 3, tx + 3, 12, tz + 3, PC_P)
    c.walls(tx - 3, 13, tz - 3, tx + 3, 13, tz + 3, BARRIERE)
    c.fill(tx - 1, 13, tz - 1, tx + 1, 13, tz + 1, PC_S)
    c.set(tx, 14, tz, "les_limbes:cristal_de_nacre")
    c.set(tx, 15, tz, LANTERNE_R)
    c.set(tx, 2, tz + 2, AIR)
    c.set(tx, 3, tz + 2, AIR)
    for x, z in anneau_points(tx, tz, 7, 6):
        c.set(x, 1, z, "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]")
    # tentes de garde (sud-ouest, sud-est)
    for (ax, az, couleur) in ((cx - 9, 30, LAINE_B), (cx + 9, 30, LAINE_BL)):
        c.fill(ax - 2, 1, az - 2, ax + 2, 1, az + 2, PLANCHES)
        c.fill(ax - 2, 2, az - 2, ax + 2, 3, az - 2, couleur)
        c.fill(ax - 2, 2, az + 2, ax + 2, 3, az + 2, couleur)
        c.fill(ax - 2, 2, az - 1, ax - 2, 3, az + 1, couleur)
        c.fill(ax - 1, 4, az - 2, ax + 1, 4, az + 2, couleur)
        c.bed(ax, 1, az, "blue", "north")
    c.set(cx, 1, 34, TONNEAU)
    c.set(cx + 1, 1, 34, TONNEAU)
    table(c, cx - 1, 1, 36, 3, "x")
    lamppost(c, cx - 6, 1, 24, 3)
    lamppost(c, cx + 6, 1, 24, 3)
    c.sign_standing(cx, 2, 16, "minecraft:birch", 8, ["Ici finit", "le royaume.", "Ici tient", "la lumière."])
    data_marker(c, cx + 1, 1, 18, "pnj:ancien_roi")
    data_marker(c, cx - 8, 1, 6, "entite:les_limbes:sentinelle_de_la_couronne")
    return c


# ----------------------------------------------------------------------------- Arrivée majeure : la cité de Brumeval

def build_cite_brumeval() -> Canvas:
    W, H, D = 33, 20, 33
    c = Canvas("cite_brumeval", W, H, D)
    c.note("Brumeval, cité arrivée : socle de pierre arraché aux brumes, place dallée et fontaine, quatre maisons de pierre, porte de relais, lanternes éteintes ; la maire Hortense et ses administrés. Attirée par le règne.")
    cx, cz = 16, 16
    for y in range(0, 4):
        disque(c, cx, y, cz, 15.5 - (3 - y) * 2.0, PC)
    disque(c, cx, 3, cz, 15.5, PC_T)
    disque(c, cx, 3, cz, 6.0, PC_P)
    disque(c, cx, 3, cz, 6.0, PC_S, 5.2)
    # fontaine centrale
    c.walls(cx - 1, 4, cz - 1, cx + 1, 4, cz + 1, PC_T)
    c.set(cx, 4, cz, EAU)
    c.set(cx, 5, cz, PC_P)
    c.set(cx, 6, cz, LANTERNE_RE)
    # quatre maisons de pierre (sans habitants fixes : les personnages sont placés par marqueurs)
    for (x0, z0, facing) in ((3, 3, "south"), (22, 3, "south"), (3, 22, "north"), (22, 22, "north")):
        batiment_royal(c, x0, 3, z0, 8, 8, 4, None, facing=facing)
    # lampadaires éteints et bannières
    for x, z in anneau_points(cx, cz, 9, 8, math.pi / 8):
        c.column(x, 4, 6, z, PC_P)
        c.set(x, 7, z, LANTERNE_RE)
    for x, z in anneau_points(cx, cz, 13, 4):
        c.set(x, 4, z, BANNIERE)
    # porte de relais au sud de la fontaine, face au nord
    relais(c, cx, 4, cz + 8, "north", "brumeval")
    c.sign_wall(cx, 5, cz + 9, "minecraft:birch", "south", ["Brumeval", "cité des", "brumes", "- arrivée -"])
    # mobilier de place : bancs, étal
    table(c, cx - 5, 4, cz + 3, 2, "x")
    chair(c, cx - 6, 4, cz + 4, "north")
    counter(c, cx + 4, 4, cz - 4, 3, "x")
    data_marker(c, cx + 2, 4, cz + 2, "pnj:maire_de_brumeval")
    data_marker(c, cx - 4, 4, cz - 3, "pnj:brumevalois_1")
    data_marker(c, cx + 5, 4, cz + 5, "pnj:brumevaloise_2")
    data_marker(c, cx - 3, 4, cz + 6, "pnj:enfant_de_brumeval")
    return c


REGNE = {
    "regne/chantier_naval": build_chantier_naval,
    "regne/navire_royal": build_navire_royal,
    "regne/ile_des_ruines": build_ile_des_ruines,
    "regne/lisiere_des_brumes": build_lisiere_des_brumes,
    "arrivees/cite_brumeval": build_cite_brumeval,
}
