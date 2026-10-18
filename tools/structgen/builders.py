"""Aides de construction de haut niveau (maisons, toits, mobilier) pour les structures des Limbes."""
from __future__ import annotations

from canvas import AIR, Canvas, DIRS, OPPOSITE

# Palette de Clairval : pierre nacrée et bois d'aube, touches de cuivre et de lanternes nacrées.
PIERRE = "les_limbes:pierre_nacree"
PIERRE_T = "les_limbes:pierre_nacree_taillee"
PIERRE_P = "les_limbes:pierre_nacree_polie"
PIERRE_S = "les_limbes:pierre_nacree_sculptee"
MURET = "les_limbes:muret_de_pierre_nacree"
PLANCHES = "les_limbes:planches_d_aube"
BUCHE = "les_limbes:buche_d_aube"
FEUILLES = "les_limbes:feuilles_d_aube[distance=1,persistent=true,waterlogged=false]"
HERBE = "les_limbes:herbe_argentee"
TERRE = "les_limbes:terre_des_limbes"
FIBRES = "les_limbes:fibres_nacrees"
HERBE_AUBE = "les_limbes:herbe_de_l_aube"
FLEUR = "les_limbes:fleur_de_rosee"
CRISTAL = "les_limbes:cristal_de_nacre"
LANTERNE = "les_limbes:lanterne_nacree[hanging=false,waterlogged=false]"
LANTERNE_H = "les_limbes:lanterne_nacree[hanging=true,waterlogged=false]"
PORTE = "les_limbes:porte_des_limbes"
STAIR_PIERRE = "les_limbes:escalier_de_pierre_nacree"
STAIR_BOIS = "les_limbes:escalier_d_aube"
DALLE_PIERRE = "les_limbes:dalle_de_pierre_nacree"
DALLE_BOIS = "les_limbes:dalle_d_aube"
BARRIERE = "les_limbes:barriere_d_aube"
VERRE = "minecraft:glass_pane"
CHEMIN = "minecraft:dirt_path"
GRAVIER = "minecraft:gravel"
COBBLE = "minecraft:cobblestone"
MOUSSE = "minecraft:mossy_cobblestone"


def stair(material: str, facing: str, half: str = "bottom", shape: str = "straight") -> str:
    return f"{material}[facing={facing},half={half},shape={shape},waterlogged=false]"


def slab(material: str, kind: str = "bottom") -> str:
    return f"{material}[type={kind},waterlogged=false]"


def log(axis: str = "y") -> str:
    return f"{BUCHE}[axis={axis}]"


def data_marker(c: Canvas, x: int, y: int, z: int, metadata: str) -> None:
    """Bloc de structure en mode DATA interprété par ComposedPiece (pnj:<role>, entite:<id>)."""
    c.set(x, y, z, "minecraft:structure_block[mode=data]", {
        "id": "minecraft:structure_block", "mode": "DATA", "metadata": metadata, "name": "", "author": "les_limbes",
        "ignoreEntities": True, "posX": 0, "posY": 0, "posZ": 0, "sizeX": 0, "sizeY": 0, "sizeZ": 0,
        "rotation": "NONE", "mirror": "NONE", "powered": False, "showair": False, "showboundingbox": False, "integrity": 1.0, "seed": 0})


def ground(c: Canvas, x1: int, z1: int, x2: int, z2: int, y: int, depth: int = 2, top: str = HERBE, under: str = TERRE) -> None:
    """Couche de sol : surface `top` à y, `under` en dessous sur `depth` blocs."""
    c.fill(x1, y, z1, x2, y, z2, top)
    if depth > 0:
        c.fill(x1, y - depth, z1, x2, y - 1, z2, under)


def path(c: Canvas, x1: int, z1: int, x2: int, z2: int, y: int, material: str = CHEMIN) -> None:
    c.fill(x1, y, z1, x2, y, z2, material)


def lamppost(c: Canvas, x: int, y: int, z: int, height: int = 3) -> None:
    """Lampadaire : poteau de barrière surmonté d'une lanterne nacrée."""
    for i in range(height):
        c.set(x, y + i, z, BARRIERE)
    c.set(x, y + height, z, LANTERNE)


def tree(c: Canvas, x: int, y: int, z: int, height: int = 5, radius: int = 2) -> None:
    """Arbre d'aube simple (tronc + boule de feuilles) pour les jardins."""
    for i in range(height):
        c.set(x, y + i, z, log())
    top = y + height - 1
    for dy in range(-2, 2):
        r = radius if dy < 1 else radius - 1
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                if abs(dx) + abs(dz) <= r + (1 if dy in (-1, 0) else 0) and not (dx == 0 and dz == 0 and dy < 1):
                    if c.inside(x + dx, top + dy, z + dz) and c.get(x + dx, top + dy, z + dz) in ("minecraft:structure_void", AIR):
                        c.set(x + dx, top + dy, z + dz, FEUILLES)
    c.set(x, top + 2, z, FEUILLES)


def box_room(c: Canvas, x1: int, y: int, z1: int, x2: int, z2: int, height: int, wall: str, floor: str, ceiling: str | None = None) -> None:
    """Pièce : sol à y, murs de y+1 à y+height, plafond optionnel à y+height+1."""
    c.fill(x1, y, z1, x2, y, z2, floor)
    c.walls(x1, y + 1, z1, x2, y + height, z2, wall)
    c.fill(x1 + 1, y + 1, z1 + 1, x2 - 1, y + height, z2 - 1, AIR)
    if ceiling:
        c.fill(x1, y + height + 1, z1, x2, y + height + 1, z2, ceiling)


def corner_logs(c: Canvas, x1: int, y: int, z1: int, x2: int, z2: int, height: int) -> None:
    for x, z in [(x1, z1), (x1, z2), (x2, z1), (x2, z2)]:
        c.column(x, y, y + height, z, log())


def window(c: Canvas, x: int, y: int, z: int, facing: str, wide: int = 1, pane: str = VERRE) -> None:
    """Fenêtre : vitre(s) dans le mur, appui en dalle à l'extérieur."""
    dx, dz = DIRS[facing]
    if facing in ("north", "south"):
        for i in range(wide):
            c.set(x + i, y, z, pane)
            c.set(x + i, y + 1, z, pane)
    else:
        for i in range(wide):
            c.set(x, y, z + i, pane)
            c.set(x, y + 1, z + i, pane)


def shutters(c: Canvas, x: int, y: int, z: int, facing: str, wood: str = "les_limbes:porte_d_aube") -> None:
    pass


def door(c: Canvas, x: int, y: int, z: int, facing: str) -> None:
    c.door(x, y, z, "les_limbes:porte_d_aube", facing)


def table(c: Canvas, x: int, y: int, z: int, length: int = 2, axis: str = "x") -> None:
    """Table : barrières + tapis clair par-dessus."""
    for i in range(length):
        xx, zz = (x + i, z) if axis == "x" else (x, z + i)
        c.set(xx, y, zz, BARRIERE)
        c.set(xx, y + 1, zz, "minecraft:white_carpet")


def chair(c: Canvas, x: int, y: int, z: int, facing: str) -> None:
    c.set(x, y, z, stair(STAIR_BOIS, OPPOSITE[facing]))


def bookshelves(c: Canvas, x1: int, y: int, z1: int, x2: int, y2: int, z2: int) -> None:
    c.fill(x1, y, z1, x2, y2, z2, "minecraft:bookshelf")


def hanging_lantern(c: Canvas, x: int, y: int, z: int) -> None:
    c.set(x, y, z, LANTERNE_H)


def counter(c: Canvas, x: int, y: int, z: int, length: int, axis: str = "x") -> None:
    for i in range(length):
        xx, zz = (x + i, z) if axis == "x" else (x, z + i)
        c.set(xx, y, zz, slab(DALLE_PIERRE, "top"))


def fountain(c: Canvas, x: int, y: int, z: int) -> None:
    """Fontaine 5x5 : bassin en pierre polie, eau, colonne centrale avec cristal."""
    c.fill(x - 2, y, z - 2, x + 2, y, z + 2, PIERRE_P)
    c.walls(x - 2, y + 1, z - 2, x + 2, y + 1, z + 2, slab(DALLE_PIERRE, "bottom"))
    c.fill(x - 1, y + 1, z - 1, x + 1, y + 1, z + 1, "minecraft:water[level=0]")
    c.set(x, y + 1, z, PIERRE_S)
    c.set(x, y + 2, z, PIERRE_S)
    c.set(x, y + 3, z, CRISTAL)


def garden_bed(c: Canvas, x1: int, y: int, z1: int, x2: int, z2: int, crop: str = HERBE_AUBE) -> None:
    c.fill(x1, y, z1, x2, y, z2, TERRE)
    for x in range(x1, x2 + 1):
        for z in range(z1, z2 + 1):
            c.set(x, y + 1, z, crop if (x + z) % 2 == 0 else FLEUR)


def fence_rect(c: Canvas, x1: int, y: int, z1: int, x2: int, z2: int, gate_pos=None) -> None:
    for x in range(x1, x2 + 1):
        c.set(x, y, z1, BARRIERE)
        c.set(x, y, z2, BARRIERE)
    for z in range(z1, z2 + 1):
        c.set(x1, y, z, BARRIERE)
        c.set(x2, y, z, BARRIERE)
    if gate_pos:
        gx, gz, facing = gate_pos
        c.set(gx, y, gz, f"minecraft:oak_fence_gate[facing={facing},in_wall=false,open=false,powered=false]")


def fill_void_with(c: Canvas, state: str, x1: int, y1: int, z1: int, x2: int, y2: int, z2: int) -> None:
    for x in range(x1, x2 + 1):
        for y in range(y1, y2 + 1):
            for z in range(z1, z2 + 1):
                if c.get(x, y, z) == "minecraft:structure_void":
                    c.set(x, y, z, state)
