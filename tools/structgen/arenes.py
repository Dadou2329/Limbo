"""Gabarits de l'étape 6 : arènes des épreuves des prétendants, campement de l'archipel et fragment du belvédère (Arrivée majeure).

Convention des toiles : y local 0 = couche posée au niveau du sol aplani (world_min.y), on marche à y local 1.
"""
from __future__ import annotations

import math

from builders import (BARRIERE, HERBE, LANTERNE, LANTERNE_H, PIERRE_P, PIERRE_T, PLANCHES, TERRE, bookshelves, chair, counter, data_marker, door, hanging_lantern, lamppost,
                      table, window)
from canvas import AIR, VOID, Canvas
from regions_couronne import LANTERNE_R, PC, PC_P, PC_S, PC_T, stair

BUCHE_G = "les_limbes:buche_de_geant[axis=y]"
PLANCHES_G = "les_limbes:planches_de_geant"
HERBE_G = "les_limbes:herbe_des_geants"
PETALE_R = "les_limbes:petale_rose"
PETALE_B = "les_limbes:petale_bleu"
SABLE_N = "les_limbes:sable_de_nacre"
PV = "les_limbes:pierre_de_verre"
PV_T = "les_limbes:pierre_de_verre_taillee"
VERRE_M = "les_limbes:verre_de_montagne"
CRISTAL_R = "les_limbes:cristal_resonnant"
SABLE_O = "les_limbes:sable_d_opale"
GRES = "les_limbes:gres_des_heures"
GRES_T = "les_limbes:gres_des_heures_taille"
PIERRE_H = "les_limbes:pierre_des_heures"
CADRAN = "les_limbes:cadran_des_heures[heure=1]"
BUCHE_A = "les_limbes:buche_d_aube[axis=y]"
BUCHE_AX = "les_limbes:buche_d_aube[axis=x]"
BUCHE_AZ = "les_limbes:buche_d_aube[axis=z]"
PLANCHES_A = "les_limbes:planches_d_aube"
BARRIERE_A = "les_limbes:barriere_d_aube[east=false,north=false,south=false,waterlogged=false,west=false]"
LAINE_B = "minecraft:white_wool"
LAINE_R = "minecraft:red_wool"
LAINE_BL = "minecraft:blue_wool"
FEU = "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]"
TONNEAU = "minecraft:barrel[facing=up,open=false]"
LANTERNE_RE = "les_limbes:lanterne_royale_eteinte[hanging=false,waterlogged=false]"
LANTERNE_REH = "les_limbes:lanterne_royale_eteinte[hanging=true,waterlogged=false]"


def disque(c: Canvas, cx: int, y: int, cz: int, rayon: float, state: str, interieur: float = 0.0) -> None:
    for x in range(int(cx - rayon) - 1, int(cx + rayon) + 2):
        for z in range(int(cz - rayon) - 1, int(cz + rayon) + 2):
            d = math.hypot(x - cx, z - cz)
            if interieur <= d <= rayon and c.inside(x, y, z):
                c.set(x, y, z, state)


def anneau_points(cx: int, cz: int, rayon: float, n: int, decalage: float = 0.0):
    for i in range(n):
        a = decalage + i * 2 * math.pi / n
        yield int(round(cx + math.cos(a) * rayon)), int(round(cz + math.sin(a) * rayon))


# ----------------------------------------------------------------------------- Aélis : jardin de l'épreuve

def build_jardin_de_l_epreuve() -> Canvas:
    W, H, D = 41, 16, 41
    c = Canvas("jardin_de_l_epreuve", W, H, D)
    c.note("Jardin de l'épreuve (Aélis) : clairière circulaire, piliers-racines, parterres, arche d'entrée au sud. Les racines du duel sont posées par le boss.")
    cx, cz = 20, 20
    disque(c, cx, 0, cz, 19.5, HERBE_G)
    disque(c, cx, 0, cz, 18.5, PETALE_R, 17.2)
    disque(c, cx, 0, cz, 17.0, PETALE_B, 16.2)
    disque(c, cx, 0, cz, 6.0, PIERRE_P)
    disque(c, cx, 0, cz, 6.0, "les_limbes:pierre_nacree_sculptee", 5.2)
    for x, z in anneau_points(cx, cz, 14, 8, math.pi / 8):
        c.column(x, 1, 3, z, BUCHE_G)
        c.set(x, 4, z, "les_limbes:feuilles_de_geant[distance=1,persistent=true,waterlogged=false]")
    for x, z in anneau_points(cx, cz, 11, 4, math.pi / 4):
        lamppost(c, x, 1, z, 3)
    for x, z in anneau_points(cx, cz, 9, 6):
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                if c.inside(x + dx, 0, z + dz):
                    c.set(x + dx, 0, z + dz, TERRE)
                    c.set(x + dx, 1, z + dz, "les_limbes:fleur_de_rosee" if (dx + dz) % 2 == 0 else "les_limbes:herbe_de_l_aube")
    # arche d'entrée (sud)
    for x in (cx - 2, cx + 2):
        c.column(x, 1, 4, 39, BUCHE_G)
    c.fill(cx - 2, 5, 39, cx + 2, 5, 39, PLANCHES_G)
    c.set(cx - 1, 4, 39, LANTERNE_H)
    c.set(cx + 1, 4, 39, LANTERNE_H)
    c.sign_wall(cx, 3, 38, "minecraft:birch", "south", ["Jardin de", "l'épreuve", "Aélis vous", "attend"])
    return c


# ----------------------------------------------------------------------------- Kaël : pont du Vent-Debout (navire en l'air)

def build_pont_du_vent_debout() -> Canvas:
    W, H, D = 81, 20, 61
    c = Canvas("pont_du_vent_debout", W, H, D)
    c.note("Le Vent-Debout, navire volant de Kaël : coque, pont, mât et voiles, cabine arrière ; deux îlots d'escale (est +3, ouest +5). Tout flotte au-dessus de la Mer.")
    cx, cz = 40, 30
    # coque : y 0..2, se rétrécit vers la proue (nord, z petit) et la poupe
    for z in range(12, 49):
        t = (z - 12) / 36.0
        demi = 6 if 0.2 < t < 0.85 else 4 if 0.1 < t < 0.95 else 2
        for y in range(0, 3):
            dl = demi - (2 - y) if y < 2 else demi
            c.fill(cx - dl, y, z, cx + dl, y, z, BUCHE_AX)
        c.fill(cx - demi, 3, z, cx + demi, 3, z, PLANCHES_A)
        c.set(cx - demi, 4, z, BARRIERE_A)
        c.set(cx + demi, 4, z, BARRIERE_A)
    c.fill(cx - 2, 4, 12, cx + 2, 4, 12, BARRIERE_A)
    c.fill(cx - 2, 4, 48, cx + 2, 4, 48, BARRIERE_A)
    # proue : figure de nacre
    c.column(cx, 3, 6, 11, "les_limbes:pierre_nacree_sculptee")
    c.set(cx, 7, 11, "les_limbes:cristal_de_nacre")
    # mât et voiles
    c.column(cx, 4, 16, cz, BUCHE_A)
    c.fill(cx - 6, 10, cz, cx + 6, 10, cz, BUCHE_AX)
    c.fill(cx - 6, 15, cz, cx + 6, 15, cz, BUCHE_AX)
    for y in range(11, 15):
        c.fill(cx - 6, y, cz + 1, cx + 6, y, cz + 1, LAINE_B)
    for y in range(6, 10):
        c.fill(cx - 5, y, cz + 1, cx + 5, y, cz + 1, LAINE_B)
    c.fill(cx - 5, 5, cz, cx + 5, 5, cz, BUCHE_AX)
    c.set(cx, 17, cz, LAINE_R)
    # cabine arrière
    c.fill(cx - 4, 4, 41, cx + 4, 4, 47, PLANCHES_A)
    c.walls(cx - 4, 4, 41, cx + 4, 7, 47, PLANCHES_A)
    c.fill(cx - 3, 4, 42, cx + 3, 7, 46, AIR)
    c.fill(cx - 4, 8, 41, cx + 4, 8, 47, PLANCHES_A)
    door(c, cx, 4, 41, "north")
    window(c, cx - 4, 5, 44, "west", 1)
    window(c, cx + 4, 5, 44, "east", 1)
    table(c, cx - 2, 4, 44, 2, "x")
    c.chest(cx + 3, 4, 46, "west", "les_limbes:chests/capitainerie")
    c.lectern(cx - 3, 4, 46, "east", Canvas.written_book("Journal du Vent-Debout", "Kaël", [
        "Le Vent-Debout ne touche plus l'eau depuis que les portes se sont rouvertes sous sa coque.\n\nIl vole où je regarde.",
        "Règle du bord : quand on traverse une porte, on tient la rambarde.\n\nL'épreuve : tenir le pont, suivre les escales, revenir au mât."]))
    hanging_lantern(c, cx, 7, 44)
    # îlots d'escale : est (+16, +3) blocs à y local 6 ; ouest (−16, +5) blocs à y local 8
    for (ix, iy) in ((cx + 16, 6), (cx - 16, 8)):
        disque(c, ix, iy, cz, 4.2, SABLE_N)
        disque(c, ix, iy - 1, cz, 3.2, "les_limbes:pierre_nacree")
        disque(c, ix, iy - 2, cz, 2.0, "les_limbes:pierre_nacree")
        for x, z in anneau_points(ix, cz, 4, 8):
            c.set(x, iy + 1, z, BARRIERE_A)
        c.set(ix, iy + 1, cz + 3, LANTERNE)
        c.set(ix, iy + 1, cz - 3, LANTERNE)
    return c


# ----------------------------------------------------------------------------- Orsane : salle des miroirs

def build_salle_des_miroirs() -> Canvas:
    W, H, D = 41, 16, 41
    c = Canvas("salle_des_miroirs", W, H, D)
    c.note("Salle des miroirs de Verréole (Orsane) : rotonde de pierre de verre, alcôves de miroirs, piliers, verrière ; entrée au sud.")
    cx, cz = 20, 20
    disque(c, cx, 0, cz, 19.5, PV_T)
    disque(c, cx, 0, cz, 5.5, "les_limbes:pierre_de_verre_taillee")
    disque(c, cx, 0, cz, 5.5, "les_limbes:pierre_nacree_polie", 4.6)
    for y in range(1, 9):
        disque(c, cx, y, cz, 19.5, PV, 18.6)
    # alcôves de miroirs (verre de montagne) dans le mur
    for x, z in anneau_points(cx, cz, 19, 8, math.pi / 8):
        for dy in range(2, 7):
            for dx in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    if abs(math.hypot(x + dx - cx, z + dz - cz) - 19) < 0.8 and c.inside(x + dx, dy, z + dz):
                        c.set(x + dx, dy, z + dz, VERRE_M)
    for x, z in anneau_points(cx, cz, 12, 6):
        c.column(x, 1, 5, z, VERRE_M)
        c.set(x, 6, z, CRISTAL_R)
    disque(c, cx, 9, cz, 19.5, VERRE_M)
    disque(c, cx, 10, cz, 6.0, PV_T)
    c.set(cx, 11, cz, CRISTAL_R)
    for x, z in anneau_points(cx, cz, 16, 8):
        c.set(x, 8, z, LANTERNE_H)
    # entrée sud
    c.fill(cx - 1, 1, 39, cx + 1, 4, 39, AIR)
    c.fill(cx - 1, 1, 38, cx + 1, 4, 38, AIR)
    c.set(cx - 2, 3, 39, LANTERNE)
    c.set(cx + 2, 3, 39, LANTERNE)
    c.sign_wall(cx + 2, 2, 40, "minecraft:birch", "south", ["Salle des", "miroirs", "Orsane juge", "ici"])
    return c


# ----------------------------------------------------------------------------- Sahir : cour des heures

def build_cour_des_heures() -> Canvas:
    W, H, D = 41, 12, 41
    c = Canvas("cour_des_heures", W, H, D)
    c.note("Cour des heures de Méridiane (Sahir) : sable d'opale, cadrans aux quatre points, colonnade, gradins avec le maître des jeux et des spectateurs.")
    cx, cz = 20, 20
    disque(c, cx, 0, cz, 19.5, SABLE_O)
    disque(c, cx, 0, cz, 17.5, GRES_T, 16.7)
    disque(c, cx, 0, cz, 8.5, GRES_T, 7.8)
    disque(c, cx, 0, cz, 2.5, PIERRE_H)
    for x, z in anneau_points(cx, cz, 10, 4):
        c.set(x, 0, z, GRES_T)
        c.set(x, 1, z, CADRAN)
    for x, z in anneau_points(cx, cz, 17, 12, math.pi / 12):
        c.column(x, 1, 4, z, GRES_T)
        c.set(x, 5, z, "les_limbes:gres_des_heures_taille")
        if (x + z) % 2 == 0:
            c.set(x, 4, z, LANTERNE)
    # gradins au nord (z 2..6), trois rangs
    for rang in range(3):
        z = 6 - rang
        y = 1 + rang
        for x in range(cx - 10, cx + 11):
            c.set(x, y, z, GRES)
            c.fill(x, 0, z, x, y - 1, z, GRES)
    data_marker(c, cx, 4, 4, "pnj:maitre_des_jeux")
    data_marker(c, cx - 6, 3, 5, "pnj:habitant_1")
    data_marker(c, cx + 6, 3, 5, "pnj:habitant_2")
    data_marker(c, cx - 3, 2, 6, "pnj:habitant_3")
    c.sign_wall(cx, 3, 7, "minecraft:birch", "south", ["Tournoi", "des heures", "trois épreuves", "un vainqueur"])
    return c


# ----------------------------------------------------------------------------- Kaël : campement de l'archipel (expédition coincée)

def build_campement_de_l_archipel() -> Canvas:
    W, H, D = 21, 12, 21
    c = Canvas("campement_de_l_archipel", W, H, D)
    c.note("Campement de l'expédition coincée dans l'archipel : deux tentes, feu, caisses, le capitaine Lio et Mina ; des gardiens des épaves rôdent.")
    cx, cz = 10, 10
    disque(c, cx, 0, cz, 9.5, SABLE_N)
    c.set(cx, 1, cz, FEU)
    for (tx, tz, couleur) in ((cx - 5, cz - 4, LAINE_B), (cx + 5, cz - 4, LAINE_BL)):
        c.fill(tx - 2, 1, tz - 2, tx + 2, 1, tz + 2, PLANCHES_A)
        c.fill(tx - 2, 2, tz - 2, tx + 2, 3, tz - 2, couleur)
        c.fill(tx - 2, 2, tz + 2, tx + 2, 3, tz + 2, couleur)
        c.fill(tx - 2, 2, tz - 1, tx - 2, 3, tz + 1, couleur)
        c.fill(tx - 1, 4, tz - 2, tx + 1, 4, tz + 2, couleur)
        c.bed(tx, 1, tz, "white", "north")
    for (bx, bz) in ((cx + 4, cz + 3), (cx + 5, cz + 3), (cx + 4, cz + 4)):
        c.set(bx, 1, bz, TONNEAU)
    c.chest(cx + 5, 1, cz + 4, "west", "les_limbes:chests/quai")
    table(c, cx - 4, 1, cz + 3, 2, "x")
    chair(c, cx - 5, 1, cz + 4, "north")
    lamppost(c, cx, 1, cz + 6, 3)
    data_marker(c, cx - 2, 1, cz + 1, "pnj:capitaine_lio")
    data_marker(c, cx + 2, 1, cz - 1, "pnj:exploratrice_mina")
    data_marker(c, cx - 8, 1, cz + 7, "entite:les_limbes:gardien_des_epaves")
    data_marker(c, cx + 8, 1, cz - 7, "entite:les_limbes:gardien_des_epaves")
    data_marker(c, cx + 8, 1, cz + 8, "entite:les_limbes:gardien_des_epaves")
    return c


# ----------------------------------------------------------------------------- Arrivée majeure : fragment du belvédère

def build_fragment_belvedere() -> Canvas:
    W, H, D = 33, 22, 33
    c = Canvas("fragment_belvedere", W, H, D)
    c.note("Fragment du belvédère : terrasse royale arrachée au palais d'autrefois, colonnes brisées, oratoire du sceau, lanternes éteintes, deux Veilleurs. Attiré par le premier acte royal.")
    cx, cz = 16, 16
    for y in range(0, 4):
        disque(c, cx, y, cz, 15.5 - (3 - y) * 2.5, PC)
    disque(c, cx, 3, cz, 15.5, PC_T)
    disque(c, cx, 3, cz, 6.0, PC_P)
    disque(c, cx, 3, cz, 6.0, PC_S, 5.2)
    for x, z in anneau_points(cx, cz, 13, 10):
        h = 3 + ((x * 7 + z * 3) % 4)
        c.column(x, 4, 3 + h, z, PC_P)
        if h == 6:
            c.set(x, 10, z, LANTERNE_RE)
    # oratoire du sceau
    c.fill(cx - 3, 4, cz - 3, cx + 3, 4, cz + 3, PC_T)
    c.walls(cx - 3, 5, cz - 3, cx + 3, 8, cz + 3, PC_T)
    c.fill(cx - 2, 5, cz - 2, cx + 2, 8, cz + 2, AIR)
    c.fill(cx - 3, 9, cz - 3, cx + 3, 9, cz + 3, PC_P)
    c.fill(cx - 1, 5, cz + 3, cx + 1, 7, cz + 3, AIR)
    c.set(cx, 5, cz, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/belvedere"})
    c.set(cx, 8, cz, LANTERNE_REH)
    c.set(cx - 2, 6, cz - 2, LANTERNE_RE)
    c.set(cx + 2, 6, cz - 2, LANTERNE_RE)
    c.lectern(cx + 2, 5, cz + 1, "west", Canvas.written_book("Dernière page du roi", "Le roi", [
        "Les Arrivées ne cessent pas. Elles sont la fonction même de ce trône : recueillir ce qui se perd ailleurs.\n\nJ'ai compris que la couronne ne les commandait pas. Elle les appelait.",
        "Je vais aux frontières, là où les brumes se plissent, pour tenir la lumière qui guide ce qui tombe.\n\nQue mon sceau revienne à qui saura attirer sans briser."]))
    for x, z in anneau_points(cx, cz, 9, 4, math.pi / 4):
        c.set(x, 4, z, "minecraft:blue_banner[rotation=0]")
    data_marker(c, cx - 7, 4, cz + 6, "entite:les_limbes:veilleur_egare")
    data_marker(c, cx + 7, 4, cz - 6, "entite:les_limbes:veilleur_egare")
    return c


ARENES = {
    "arenes/jardin_de_l_epreuve": build_jardin_de_l_epreuve,
    "arenes/pont_du_vent_debout": build_pont_du_vent_debout,
    "arenes/salle_des_miroirs": build_salle_des_miroirs,
    "arenes/cour_des_heures": build_cour_des_heures,
    "arenes/campement_de_l_archipel": build_campement_de_l_archipel,
    "arrivees/fragment_belvedere": build_fragment_belvedere,
}
