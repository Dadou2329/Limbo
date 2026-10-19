"""Gabarits des Jardins des Géants : Sylvécime (ville perchée dans un arbre géant), serre ancienne, pépinière royale, arbre creux, clairière du géant."""
from __future__ import annotations

import math

from builders import (BARRIERE, FIBRES, FLEUR, HERBE, LANTERNE, LANTERNE_H, PIERRE_P, PIERRE_T, PLANCHES, STAIR_BOIS, TERRE, chair, counter, data_marker, door,
                      hanging_lantern, lamppost, table, window)
from canvas import AIR, VOID, Canvas

BUCHE_G = "les_limbes:buche_de_geant[axis=y]"
BUCHE_G_X = "les_limbes:buche_de_geant[axis=x]"
BUCHE_G_Z = "les_limbes:buche_de_geant[axis=z]"
PLANCHES_G = "les_limbes:planches_de_geant"
FEUILLES_G = "les_limbes:feuilles_de_geant[distance=1,persistent=true,waterlogged=false]"
HERBE_G = "les_limbes:herbe_des_geants"
PETALE_ROSE = "les_limbes:petale_rose"
PETALE_BLEU = "les_limbes:petale_bleu"
SEVE = "les_limbes:seve_ascendante"
PORTE_RELAIS = "les_limbes:porte_de_relais"
STAIR_G = "minecraft:dark_oak"
VERRE = "minecraft:glass"


def relais(c: Canvas, x: int, y: int, z: int, facing: str, ident: str) -> None:
    c.set(x, y, z, f"{PORTE_RELAIS}[eveillee=false,facing={facing}]", {"id": PORTE_RELAIS, "relais": ident})
    c.set(x, y + 1, z, AIR)


def tronc(c: Canvas, cx: int, cz: int, y0: int, y1: int, rayon: float) -> None:
    for x in range(c.sx):
        for z in range(c.sz):
            if math.hypot(x - cx, z - cz) <= rayon + 0.3:
                for y in range(y0, y1 + 1):
                    c.set(x, y, z, BUCHE_G)


def feuillage(c: Canvas, cx: int, cy: int, cz: int, rayon: int, epaisseur: int) -> None:
    for x in range(c.sx):
        for z in range(c.sz):
            for dy in range(epaisseur):
                y = cy + dy
                r = rayon - dy * 1.3
                if math.hypot(x - cx, z - cz) <= r and c.inside(x, y, z) and c.get(x, y, z) in (VOID, AIR):
                    c.set(x, y, z, FEUILLES_G)


def racine(c: Canvas, cx: int, cz: int, y: int, dx: int, dz: int, longueur: int) -> None:
    """Racine horizontale partant du tronc, qui descend doucement vers le sol : route praticable."""
    x, z, yy = cx, cz, y
    for i in range(longueur):
        x += dx
        z += dz
        if i % 3 == 2 and yy > 1:
            yy -= 1
        if c.inside(x, yy, z):
            c.set(x, yy, z, BUCHE_G_X if dx else BUCHE_G_Z)
            if dx:
                c.set(x, yy, z + 1, PLANCHES_G) if c.inside(x, yy, z + 1) else None
            else:
                c.set(x + 1, yy, z, PLANCHES_G) if c.inside(x + 1, yy, z) else None


def cabane_perchee(c: Canvas, x0: int, y: int, z0: int, w: int, d: int, role: str | None, facing: str = "south", enseigne: list[str] | None = None) -> None:
    """Maison de planches de géant sur plate-forme, toit de pétales, garde-corps."""
    c.fill(x0 - 1, y - 1, z0 - 1, x0 + w, y - 1, z0 + d, PLANCHES_G)
    c.walls(x0, y, z0, x0 + w - 1, y + 2, z0 + d - 1, PLANCHES_G)
    c.fill(x0 + 1, y, z0 + 1, x0 + w - 2, y + 2, z0 + d - 2, AIR)
    c.fill(x0, y + 3, z0, x0 + w - 1, y + 3, z0 + d - 1, PLANCHES_G)
    c.gable_roof(x0, z0, x0 + w - 1, z0 + d - 1, y + 4, STAIR_G, axis="x" if w >= d else "z", overhang=1)
    dz = z0 + d - 1 if facing == "south" else z0
    door(c, x0 + w // 2, y, dz, facing)
    window(c, x0 + 1, y + 1, dz, facing, 1)
    window(c, x0 + w - 2, y + 1, dz, facing, 1)
    window(c, x0, y + 1, z0 + d // 2, "west", 1)
    window(c, x0 + w - 1, y + 1, z0 + d // 2, "east", 1)
    c.bed(x0 + 1, y, z0 + 1, "lime", "south" if facing == "south" else "north")
    c.set(x0 + w - 2, y, z0 + 1, "minecraft:chest[facing=south,type=single,waterlogged=false]")
    c.flower_pot(x0 + w - 2, y, z0 + d - 2, "minecraft:potted_pink_tulip")
    hanging_lantern(c, x0 + w // 2, y + 2, z0 + d // 2)
    for x in range(x0 - 1, x0 + w + 1):
        for z in (z0 - 1, z0 + d):
            if c.get(x, y, z) in (VOID, AIR):
                c.set(x, y, z, BARRIERE)
    for z in range(z0 - 1, z0 + d + 1):
        for x in (x0 - 1, x0 + w):
            if c.get(x, y, z) in (VOID, AIR):
                c.set(x, y, z, BARRIERE)
    c.set(x0 + w // 2, y, dz + (1 if facing == "south" else -1), AIR)
    if enseigne:
        c.sign_wall(x0 + w // 2 + 1, y + 1, dz, "minecraft:dark_oak", facing, enseigne)
    if role:
        data_marker(c, x0 + w // 2, y, z0 + d // 2, "pnj:" + role)


def passerelle(c: Canvas, x1: int, z1: int, x2: int, z2: int, y: int) -> None:
    n = max(abs(x2 - x1), abs(z2 - z1))
    for i in range(n + 1):
        x = x1 + round((x2 - x1) * i / n)
        z = z1 + round((z2 - z1) * i / n)
        if c.get(x, y - 1, z) in (VOID, AIR):
            c.set(x, y - 1, z, PLANCHES_G)
        if i % 4 == 0:
            for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                if abs(x2 - x1) >= abs(z2 - z1) and dz == 0:
                    continue
                if abs(x2 - x1) < abs(z2 - z1) and dx == 0:
                    continue
                if c.get(x + dx, y, z + dz) in (VOID, AIR) and c.get(x + dx, y - 1, z + dz) in (VOID, AIR):
                    c.set(x + dx, y - 1, z + dz, PLANCHES_G)
                    c.set(x + dx, y, z + dz, BARRIERE)
                    if i % 8 == 0:
                        c.set(x + dx, y + 1, z + dz, LANTERNE)


def build_sylvecime() -> Canvas:
    """Sylvécime : arbre géant creux (ascenseur de sève), trois niveaux de plates-formes, maisons perchées, halle d'Aélis au sommet, pépinière au sol."""
    W, H, D = 61, 60, 61
    c = Canvas("sylvecime", W, H, D)
    c.note("Sylvécime : ville perchée des Jardins des Géants. Tronc creux avec colonne de sève, trois plates-formes, cabanes, halle d'Aélis, racines-routes, porte de relais au pied.")
    cx, cz = 30, 30
    # sol : herbe des géants (y local 0 = monde 71, on marche à 72 = y1)
    for x in range(W):
        for z in range(D):
            if math.hypot(x - cx, z - cz) <= 30:
                c.set(x, 0, z, HERBE_G)
    # tronc : rayon 4,5 de y 1 à 48 ; creux (rayon 2,5) avec colonne de sève au centre
    tronc(c, cx, cz, 1, 48, 4.5)
    for y in range(1, 47):
        for x in range(cx - 3, cx + 4):
            for z in range(cz - 3, cz + 4):
                if math.hypot(x - cx, z - cz) <= 2.6:
                    c.set(x, y, z, AIR)
        c.set(cx, y, cz, SEVE)
    # entrée du tronc au sud
    c.set(cx, 1, cz + 4, AIR); c.set(cx, 2, cz + 4, AIR); c.set(cx, 1, cz + 3, AIR); c.set(cx, 2, cz + 3, AIR)
    # plates-formes annulaires à y 14, 26, 38 (monde 85, 97, 109) avec sorties du tronc
    for i, py in enumerate((14, 26, 38)):
        r = 11 - i
        for x in range(W):
            for z in range(D):
                d = math.hypot(x - cx, z - cz)
                if 4.3 < d <= r + 0.5:
                    c.set(x, py, z, PLANCHES_G)
                    if r < d <= r + 0.5 or (r - 0.5 < d <= r + 0.5):
                        c.set(x, py + 1, z, BARRIERE)
        for dx, dz in ((0, 4), (0, -4), (4, 0), (-4, 0)):
            c.set(cx + dx, py + 1, cz + dz, AIR); c.set(cx + dx, py + 2, cz + dz, AIR)
        for a in range(0, 360, 90):
            lx = int(round(cx + math.cos(math.radians(a + 45)) * (r - 1)))
            lz = int(round(cz + math.sin(math.radians(a + 45)) * (r - 1)))
            c.set(lx, py + 1, lz, BARRIERE); c.set(lx, py + 2, lz, LANTERNE)
    # branches porteuses et cabanes
    branches = [((cx + 12, cz + 14), 14, "boulangere_sylve", ["Four des", "cimes"]), ((cx - 14, cz + 10), 14, "tisserande", ["Tissage", "de fibres"]),
                ((cx + 14, cz - 10), 26, "herboriste", ["Herboriste", "de rosée"]), ((cx - 12, cz - 14), 26, "guetteur_sylve", None)]
    for (bx, bz), by, role, ens in branches:
        # branche : bûche horizontale de la plate-forme vers la cabane, puis plate-forme
        passerelle(c, cx + (4 if bx > cx else -4), cz + (4 if bz > cz else -4), bx, bz, by + 1)
        cabane_perchee(c, bx - 3, by + 1, bz - 3, 7, 7, role, facing="south" if bz > cz else "north", enseigne=ens)
    # feuillage : couronne large au-dessus de la dernière plate-forme, cime
    feuillage(c, cx, 44, cz, 22, 7)
    tronc(c, cx, cz, 49, 52, 3.5)
    feuillage(c, cx, 50, cz, 12, 6)
    # halle d'Aélis : au sommet de la troisième plate-forme (nord) ; salle ouverte, trône de racines, table du conseil
    hx, hz = cx, cz - 7
    c.fill(hx - 5, 38, hz - 4, hx + 5, 38, hz + 2, PLANCHES_G)
    for x, z in [(hx - 5, hz - 4), (hx + 5, hz - 4), (hx - 5, hz + 2), (hx + 5, hz + 2)]:
        c.column(x, 39, 43, z, BUCHE_G)
    c.fill(hx - 5, 44, hz - 4, hx + 5, 44, hz + 2, PETALE_ROSE)
    c.fill(hx - 4, 44, hz - 3, hx + 4, 44, hz + 1, PETALE_BLEU)
    c.set(hx, 39, hz - 3, "minecraft:dark_oak_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]")
    c.set(hx - 1, 39, hz - 3, PLANCHES_G); c.set(hx + 1, 39, hz - 3, PLANCHES_G)
    c.set(hx - 1, 40, hz - 3, FEUILLES_G); c.set(hx + 1, 40, hz - 3, FEUILLES_G)
    table(c, hx - 1, 39, hz, 3, "x")
    for x in (hx - 2, hx + 2):
        chair(c, x, 39, hz, "east" if x < hx else "west")
    hanging_lantern(c, hx - 3, 43, hz - 1); hanging_lantern(c, hx + 3, 43, hz - 1)
    data_marker(c, hx, 39, hz - 2, "pnj:aelis")
    data_marker(c, hx + 3, 39, hz + 1, "pnj:intendante_sylve")
    c.hanging_sign_wall(hx, 42, hz + 2, "minecraft:dark_oak", "south", ["Halle de", "Sylvécime"])
    # racines-routes partant du tronc vers les quatre directions (surface)
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        racine(c, cx + dx * 4, cz + dz * 4, 2, dx, dz, 24)
    # pépinière au sol (sud-est) : bacs de terre, jeunes pousses, fleurs ; porte de relais au pied (sud-ouest)
    for x in range(cx + 8, cx + 20, 3):
        for z in range(cz + 8, cz + 18, 3):
            c.set(x, 1, z, TERRE); c.set(x, 2, z, "minecraft:azalea") if (x + z) % 2 else c.set(x, 2, z, FLEUR)
    data_marker(c, cx + 14, 1, cz + 12, "pnj:pepinieriste")
    relais(c, cx - 10, 1, cz + 12, "east", "sylvecime")
    for x, z in [(cx - 20, cz + 20), (cx + 22, cz - 18), (cx - 22, cz - 6)]:
        c.set(x, 1, z, FIBRES)
    for x, z in [(cx - 16, cz - 20), (cx + 18, cz + 22)]:
        lamppost(c, x, 1, z, 3)
    c.sign_standing(cx + 2, 1, cz + 7, "minecraft:dark_oak", 8, ["Sylvécime", "entrez dans", "le tronc :", "la sève vous porte"])
    return c


def build_serre_ancienne() -> Canvas:
    """Serre ancienne : grande verrière de verre et bois de géant, bacs de cultures, fontaine tarie à restaurer (quête)."""
    W, H, D = 19, 18, 25
    c = Canvas("serre_ancienne", W, H, D)
    c.note("Serre ancienne des Jardins : verrière à armature de bois de géant, cultures extraordinaires, bassin tari (quête « La source tarie »), jardinière Oriane.")
    c.fill(0, 0, 0, W - 1, 0, D - 1, PIERRE_T)
    c.fill(1, 0, 1, W - 2, 0, D - 2, PIERRE_P)
    for x in range(0, W, 3):
        c.column(x, 1, 6, 0, BUCHE_G); c.column(x, 1, 6, D - 1, BUCHE_G)
    for z in range(0, D, 3):
        c.column(0, 1, 6, z, BUCHE_G); c.column(W - 1, 1, 6, z, BUCHE_G)
    c.walls(0, 1, 0, W - 1, 6, D - 1, VERRE)
    for x in range(0, W, 3):
        c.column(x, 1, 6, 0, BUCHE_G); c.column(x, 1, 6, D - 1, BUCHE_G)
    for z in range(0, D, 3):
        c.column(0, 1, 6, z, BUCHE_G); c.column(W - 1, 1, 6, z, BUCHE_G)
    c.fill(1, 1, 1, W - 2, 6, D - 2, AIR)
    c.gable_roof(0, 0, W - 1, D - 1, 7, STAIR_G, axis="z", overhang=0)
    for y in range(7, 12):
        for x in range(W):
            for z in range(D):
                if c.get(x, y, z) and "stairs" in c.get(x, y, z) and (x + z) % 2 == 0:
                    c.set(x, y, z, VERRE)
    door(c, W // 2, 1, D - 1, "south")
    c.set(W // 2 - 1, 1, D - 1, AIR); c.set(W // 2 - 1, 2, D - 1, AIR)
    # bacs de culture
    for x in (2, 6, 12, 16):
        for z in range(3, D - 4, 1):
            if z % 6 in (3, 4):
                c.set(x, 1, z, "minecraft:farmland[moisture=7]")
                c.set(x, 2, z, ["minecraft:wheat[age=7]", "minecraft:carrots[age=7]", "minecraft:beetroots[age=3]", "minecraft:sweet_berry_bush[age=3]"][(x // 4) % 4])
    # bassin central tari : pierre polie, à remplir d'eau par la quête (le bloc central est une « source » marquée)
    c.fill(8, 1, 9, 10, 1, 15, PIERRE_P)
    c.fill(9, 1, 10, 9, 1, 14, "minecraft:cauldron")
    c.set(9, 2, 12, "les_limbes:cristal_de_nacre") if False else None
    c.set(9, 1, 12, PIERRE_P)
    c.sign_standing(9, 2, 16, "minecraft:dark_oak", 0, ["Source tarie", "la rosée ne", "monte plus", ""])
    hanging_lantern(c, 4, 6, 6); hanging_lantern(c, 14, 6, 6); hanging_lantern(c, 4, 6, 18); hanging_lantern(c, 14, 6, 18)
    c.set(2, 1, D - 3, "minecraft:composter[level=0]"); c.set(3, 1, D - 3, "minecraft:barrel[facing=up,open=false]")
    c.set(W - 3, 1, D - 3, "minecraft:chest[facing=north,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/sylvecime"})
    data_marker(c, 9, 1, 5, "pnj:oriane")
    return c


def build_pepiniere_royale() -> Canvas:
    """Pépinière royale : rangées de jeunes arbres géants sous abri, pousses, panneau d'herboriste ; pépiniériste royal."""
    W, H, D = 23, 14, 17
    c = Canvas("pepiniere_royale", W, H, D)
    c.note("Pépinière royale : abri ouvert de bois de géant, rangées de pousses et de jeunes géants, pépiniériste Lioran (quête des graines).")
    c.fill(0, 0, 0, W - 1, 0, D - 1, HERBE_G)
    for x in (0, 11, W - 1):
        for z in (0, D - 1):
            c.column(x, 1, 5, z, BUCHE_G)
    c.fill(0, 6, 0, W - 1, 6, D - 1, PETALE_BLEU)
    c.fill(1, 6, 1, W - 2, 6, D - 2, FEUILLES_G)
    for x in range(2, W - 2, 4):
        for z in range(3, D - 3, 4):
            c.set(x, 0, z, TERRE)
            c.set(x, 1, z, "minecraft:dark_oak_sapling[stage=0]" if (x + z) % 3 else "minecraft:flowering_azalea")
    c.column(5, 1, 7, 8, BUCHE_G); feuillage(c, 5, 8, 8, 3, 3)
    c.column(17, 1, 8, 8, BUCHE_G); feuillage(c, 17, 9, 8, 3, 3)
    counter(c, 9, 1, 1, 4, "x")
    c.set(9, 1, 2, "minecraft:composter[level=3]"); c.set(12, 1, 2, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/sylvecime"})
    c.sign_wall(10, 2, 1, "minecraft:dark_oak", "south", ["Pépinière", "royale", "graines", "d'ailleurs"])
    data_marker(c, 11, 1, 6, "pnj:lioran")
    return c


def build_arbre_creux() -> Canvas:
    """Arbre creux : tronc géant évidé sur quatre étages aménagés (refuge, bibliothèque, atelier, belvédère), sève ascendante au centre."""
    W, H, D = 17, 40, 17
    c = Canvas("arbre_creux", W, H, D)
    c.note("Arbre creux : géant évidé, quatre étages aménagés (foyer, bibliothèque de l'arbre, atelier, belvédère), colonne de sève ; l'ermite Baruch.")
    cx, cz = 8, 8
    c.fill(0, 0, 0, W - 1, 0, D - 1, HERBE_G)
    tronc(c, cx, cz, 1, 32, 6.5)
    for y in range(1, 31):
        for x in range(W):
            for z in range(D):
                if math.hypot(x - cx, z - cz) <= 4.6:
                    c.set(x, y, z, AIR)
        c.set(cx, y, cz, SEVE)
    for py in (8, 16, 24):
        for x in range(W):
            for z in range(D):
                d = math.hypot(x - cx, z - cz)
                if 1.5 < d <= 4.6:
                    c.set(x, py, z, PLANCHES_G)
    c.set(cx, 1, cz + 6, AIR); c.set(cx, 2, cz + 6, AIR); c.set(cx, 1, cz + 5, AIR); c.set(cx, 2, cz + 5, AIR)
    for y in (4, 12, 20, 28):
        window(c, cx, y, cz - 6, "north", 1)
        window(c, cx - 6, y, cz, "west", 1)
        window(c, cx + 6, y, cz, "east", 1)
    # foyer (rez), bibliothèque (1), atelier (2), belvédère (3)
    c.bed(cx - 3, 1, cz - 2, "green", "south")
    c.set(cx + 3, 1, cz - 3, "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]")
    c.set(cx + 3, 1, cz + 2, "minecraft:chest[facing=west,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/sylvecime"})
    for x in range(cx - 3, cx + 4):
        if abs(x - cx) > 1:
            c.set(x, 9, cz - 3, "minecraft:bookshelf"); c.set(x, 10, cz - 3, "minecraft:bookshelf")
    c.set(cx, 9, cz - 3, "minecraft:lectern[facing=south,has_book=true,powered=false]", {"id": "minecraft:lectern", "Page": 0, "Book": Canvas.written_book(
        "Mémoire de l'arbre", "Baruch", [
            "Cet arbre était déjà géant quand Sylvécime n'était qu'un campement. Il s'est creusé seul, dit-on ; moi je crois que la sève l'a voulu.",
            "La sève monte toujours. Posez un pied dedans, détendez-vous, et montez. Accroupissez-vous pour descendre. Les Jardins ont des ascenseurs depuis mille ans.",
            "Aélis vient parfois lire ici. Elle dit que les racines savent où l'eau manque. La source de la serre ancienne, par exemple."])})
    c.set(cx - 3, 17, cz + 3, "minecraft:crafting_table"); c.set(cx - 2, 17, cz + 3, "minecraft:loom[facing=north]"); c.set(cx + 3, 17, cz + 3, "minecraft:barrel[facing=up,open=false]")
    for y in (1, 9, 17, 25):
        hanging_lantern(c, cx - 3, y + 5, cz)
    # belvédère : ouverture large à l'est au niveau 25
    c.fill(cx + 4, 25, cz - 1, cx + 6, 27, cz + 1, AIR)
    c.fill(cx + 7, 24, cz - 2, cx + 8, 24, cz + 2, PLANCHES_G)
    for z in range(cz - 2, cz + 3):
        c.set(cx + 8, 25, z, BARRIERE)
    feuillage(c, cx, 32, cz, 9, 6)
    data_marker(c, cx - 2, 9, cz + 1, "pnj:baruch")
    c.sign_wall(cx + 1, 2, cz + 6, "minecraft:dark_oak", "south", ["L'arbre", "creux", "la sève", "vous porte"])
    return c


def build_clairiere_du_geant() -> Canvas:
    """Clairière du géant : cercle de pétales géants, fleurs monumentales, et le Géant des jardins assis au centre (créature paisible)."""
    W, H, D = 33, 16, 33
    c = Canvas("clairiere_du_geant", W, H, D)
    c.note("Clairière du géant : anneau de pétales, quatre fleurs monumentales, offrandes ; le Géant des jardins (créature paisible) y repose.")
    cx, cz = 16, 16
    for x in range(W):
        for z in range(D):
            d = math.hypot(x - cx, z - cz)
            if d <= 16:
                c.set(x, 0, z, HERBE_G if d > 6 else PETALE_ROSE if int(d) % 2 else PETALE_BLEU)
    for a in range(0, 360, 90):
        fx = int(round(cx + math.cos(math.radians(a + 45)) * 11)); fz = int(round(cz + math.sin(math.radians(a + 45)) * 11))
        c.column(fx, 1, 7, fz, BUCHE_G)
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                if abs(dx) + abs(dz) <= 3 and c.inside(fx + dx, 8, fz + dz):
                    c.set(fx + dx, 8, fz + dz, PETALE_ROSE if a % 180 == 0 else PETALE_BLEU)
                    if abs(dx) + abs(dz) <= 1:
                        c.set(fx + dx, 9, fz + dz, PETALE_ROSE if a % 180 == 0 else PETALE_BLEU)
        c.set(fx, 10, fz, LANTERNE)
    c.set(cx, 1, cz + 7, "minecraft:chest[facing=south,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/sylvecime"})
    c.sign_standing(cx - 3, 1, cz + 9, "minecraft:dark_oak", 0, ["Clairière", "du géant", "ne le réveillez", "pas trop vite"])
    data_marker(c, cx, 1, cz, "entite:les_limbes:geant_des_jardins")
    for x, z in [(cx - 12, cz), (cx + 12, cz + 2), (cx + 3, cz - 13), (cx - 5, cz + 13)]:
        c.set(x, 1, z, FLEUR if (x + z) % 2 else FIBRES)
    return c


JARDINS = {
    "jardins/sylvecime": build_sylvecime,
    "jardins/serre_ancienne": build_serre_ancienne,
    "jardins/pepiniere_royale": build_pepiniere_royale,
    "jardins/arbre_creux": build_arbre_creux,
    "jardins/clairiere_du_geant": build_clairiere_du_geant,
}
