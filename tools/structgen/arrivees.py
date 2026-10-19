"""Arrivées de l'étape 8 : huit petites et moyennes structures qui tombent des brumes sur les sites réservés de chaque région."""
from __future__ import annotations

from builders import (BARRIERE, LANTERNE, LANTERNE_H, PIERRE_P, PIERRE_T, PLANCHES, TERRE, bookshelves, chair, data_marker, hanging_lantern, lamppost, table)
from canvas import AIR, Canvas
from arenes import SABLE_N, PLANCHES_A, BARRIERE_A, LAINE_B, LAINE_BL, FEU, TONNEAU
from regions_couronne import BANNIERE, PC, PC_P, PC_T

TERRE_RETOURNEE = "minecraft:coarse_dirt"
EPICEA = "minecraft:spruce_planks"
BUCHE_EPICEA = "minecraft:spruce_log[axis=y]"
BUCHE_EPICEA_X = "minecraft:spruce_log[axis=x]"
FOIN = "minecraft:hay_block[axis=y]"
PAVES = "minecraft:cobblestone"
EAU = "minecraft:water[level=0]"
CHAINE = "minecraft:chain[axis=y,waterlogged=false]"
VERRE_M = "les_limbes:verre_de_montagne"
VITRE = "minecraft:glass_pane[east=false,north=false,south=false,waterlogged=false,west=false]"
LAINE_O = "minecraft:orange_wool"
TAPIS_R = "minecraft:red_carpet"
TAPIS_B = "minecraft:blue_carpet"
BRIQUES = "minecraft:stone_bricks"
BRIQUES_M = "minecraft:mossy_stone_bricks"
DALLE_BRIQUES = "minecraft:stone_brick_slab[type=bottom,waterlogged=false]"
NOTE = "minecraft:note_block[instrument=harp,note=0,powered=false]"


def coffre(c: Canvas, x: int, y: int, z: int, facing: str, table: str) -> None:
    c.set(x, y, z, f"minecraft:chest[facing={facing},type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/" + table})


def roue(c: Canvas, x: int, y: int, z: int, facing: str) -> None:
    c.set(x, y, z, f"minecraft:spruce_trapdoor[facing={facing},half=bottom,open=true,powered=false,waterlogged=false]")


def build_charrette_renversee() -> Canvas:
    """Petite Arrivée : charrette de ferme renversée, roues en l'air, foin répandu, coffre de voyage."""
    c = Canvas("charrette_renversee", 7, 5, 7)
    c.note("Arrivée « La charrette renversée » : charrette d'épicéa couchée, roues, bottes de foin, coffre de voyage ; disparaît après deux jours.")
    c.fill(0, 0, 0, 6, 0, 6, TERRE_RETOURNEE)
    c.fill(1, 1, 2, 5, 1, 4, EPICEA)
    c.fill(1, 2, 2, 1, 2, 4, EPICEA)
    c.fill(5, 2, 2, 5, 2, 4, EPICEA)
    c.set(0, 1, 3, BUCHE_EPICEA_X)
    roue(c, 2, 2, 1, "north"); roue(c, 4, 2, 1, "north"); roue(c, 2, 2, 5, "south"); roue(c, 4, 2, 5, "south")
    c.set(2, 1, 1, FOIN); c.set(5, 1, 5, FOIN); c.set(6, 1, 1, FOIN); c.set(1, 1, 5, FOIN)
    coffre(c, 3, 2, 3, "south", "charrette")
    c.set(3, 1, 6, LANTERNE)
    return c


def build_puits_d_ailleurs() -> Canvas:
    """Petite Arrivée : margelle de pavés, eau claire, potence et chaîne, petit coffre de pèlerin."""
    c = Canvas("puits_d_ailleurs", 5, 7, 5)
    c.note("Arrivée « Le puits d'ailleurs » : margelle de pavés, eau qui n'est pas d'ici, potence, chaîne et seau ; disparaît après deux jours.")
    c.fill(0, 0, 0, 4, 0, 4, TERRE_RETOURNEE)
    c.fill(1, 0, 1, 3, 0, 3, PAVES)
    c.walls(1, 1, 1, 3, 1, 3, PAVES)
    c.set(2, 1, 2, EAU)
    c.set(2, 0, 2, EAU)
    c.set(1, 2, 1, BARRIERE); c.set(3, 2, 3, BARRIERE)
    c.column(1, 3, 3, 1, BARRIERE); c.column(3, 3, 3, 3, BARRIERE)
    c.fill(1, 4, 1, 3, 4, 3, "minecraft:spruce_slab[type=bottom,waterlogged=false]")
    c.set(2, 3, 2, CHAINE)
    c.set(2, 2, 2, "minecraft:cauldron")
    coffre(c, 4, 1, 2, "east", "puits")
    c.sign_standing(0, 1, 2, "minecraft:spruce", 4, ["Puits", "d'ailleurs", "L'eau est", "bonne"])
    return c


def build_kiosque_a_musique() -> Canvas:
    """Petite Arrivée : kiosque rond à colonnes, blocs de note, phonographe ; les Limbes entendent une musique d'ailleurs."""
    c = Canvas("kiosque_a_musique", 9, 7, 9)
    c.note("Arrivée « Le kiosque à musique » : estrade de pierre nacrée, huit colonnes, toit de planches d'aube, blocs de note et phonographe ; coffre de partitions.")
    cx, cz = 4, 4
    c.fill(0, 0, 0, 8, 0, 8, TERRE_RETOURNEE)
    for x in range(9):
        for z in range(9):
            if (x - cx) ** 2 + (z - cz) ** 2 <= 16.5:
                c.set(x, 0, z, PIERRE_P)
            if (x - cx) ** 2 + (z - cz) ** 2 <= 6.5:
                c.set(x, 1, z, "les_limbes:dalle_de_pierre_nacree[type=bottom,waterlogged=false]")
    for x, z in ((1, 4), (7, 4), (4, 1), (4, 7), (2, 2), (6, 6), (2, 6), (6, 2)):
        c.column(x, 1, 4, z, BARRIERE_A)
    for x in range(1, 8):
        for z in range(1, 8):
            if (x - cx) ** 2 + (z - cz) ** 2 <= 11:
                c.set(x, 5, z, PLANCHES_A)
    c.fill(3, 6, 3, 5, 6, 5, "les_limbes:dalle_d_aube[type=bottom,waterlogged=false]")
    c.set(cx, 4, cz, LANTERNE_H)
    c.set(cx, 2, cz, "minecraft:jukebox[has_record=true]", {"id": "minecraft:jukebox", "RecordItem": {"id": "minecraft:music_disc_otherside", "count": 1}})
    c.set(3, 2, 3, NOTE); c.set(5, 2, 5, NOTE); c.set(3, 2, 5, NOTE)
    coffre(c, 5, 2, 3, "west", "kiosque")
    return c


def build_radeau_des_naufrages() -> Canvas:
    """Moyenne Arrivée (Mer) : radeau de rondins échoué sur l'îlot, tente, feu, tonneaux ; Siméon, le naufragé."""
    c = Canvas("radeau_des_naufrages", 11, 6, 9)
    c.note("Arrivée « Le radeau des naufragés » : rondins liés, tente de toile, feu, tonneaux, caisse ; Siméon raconte la tempête d'ailleurs.")
    c.fill(0, 0, 0, 10, 0, 8, SABLE_N)
    for z in range(2, 7):
        c.fill(1, 1, z, 7, 1, z, BUCHE_EPICEA_X)
    c.fill(2, 2, 3, 6, 2, 5, EPICEA)
    c.set(8, 1, 1, BUCHE_EPICEA)
    c.set(8, 2, 1, LAINE_B); c.set(8, 3, 1, BANNIERE)
    # tente sur le sable
    c.fill(8, 1, 5, 10, 1, 7, PLANCHES)
    c.fill(8, 2, 5, 10, 2, 5, LAINE_B); c.fill(8, 2, 7, 10, 2, 7, LAINE_B); c.fill(10, 2, 6, 10, 3, 6, LAINE_B)
    c.fill(8, 3, 5, 10, 3, 7, LAINE_B)
    c.bed(9, 2, 6, "white", "west")
    c.set(6, 1, 1, FEU)
    c.set(4, 3, 4, TONNEAU); c.set(5, 3, 4, TONNEAU)
    coffre(c, 3, 3, 4, "north", "radeau")
    c.sign_standing(1, 2, 7, "minecraft:spruce", 12, ["Naufragés", "de la mer", "d'en bas", "Vivants !"])
    data_marker(c, 7, 2, 6, "pnj:naufrage")
    return c


def build_serre_tombee() -> Canvas:
    """Petite Arrivée (Jardins) : serre de verre et de chêne arrachée, carrés de légumes d'ailleurs, pots, coffre à graines."""
    c = Canvas("serre_tombee", 7, 6, 7)
    c.note("Arrivée « La serre tombée » : vitres de chêne, carrés de blé, carottes et pommes de terre d'un autre monde, pots de fleurs, coffre de graines.")
    c.fill(0, 0, 0, 6, 0, 6, TERRE_RETOURNEE)
    c.fill(1, 0, 1, 5, 0, 5, "minecraft:oak_planks")
    for x, z in ((1, 1), (5, 1), (1, 5), (5, 5)):
        c.column(x, 1, 3, z, "minecraft:oak_log[axis=y]")
    for x in range(2, 5):
        c.set(x, 1, 1, VITRE); c.set(x, 2, 1, VITRE); c.set(x, 1, 5, VITRE); c.set(x, 2, 5, VITRE)
        c.set(1, 1, x, VITRE); c.set(1, 2, x, VITRE); c.set(5, 1, x, VITRE); c.set(5, 2, x, VITRE)
    c.set(3, 1, 5, AIR); c.set(3, 2, 5, AIR)
    c.fill(1, 3, 1, 5, 3, 5, "minecraft:oak_slab[type=bottom,waterlogged=false]")
    c.fill(2, 4, 2, 4, 4, 4, "minecraft:glass")
    c.fill(2, 0, 2, 4, 0, 4, "minecraft:farmland[moisture=7]")
    c.set(3, 0, 3, EAU)
    c.set(2, 1, 2, "minecraft:wheat[age=7]"); c.set(4, 1, 2, "minecraft:carrots[age=7]"); c.set(2, 1, 4, "minecraft:potatoes[age=7]"); c.set(4, 1, 4, "minecraft:wheat[age=5]")
    c.flower_pot(1, 1, 3, "minecraft:potted_poppy"); c.flower_pot(5, 1, 3, "minecraft:potted_azure_bluet")
    coffre(c, 3, 1, 2, "south", "serre_tombee")
    c.set(3, 2, 3, LANTERNE_H)
    return c


def build_tente_du_voyageur() -> Canvas:
    """Petite Arrivée (Désert) : tente de toile orange, tapis, feu, coffre, outre ; le voyageur n'est plus là."""
    c = Canvas("tente_du_voyageur", 7, 5, 7)
    c.note("Arrivée « La tente du voyageur » : toile orange et blanche, tapis, feu éteint, coffre et carnet de route d'un marcheur d'ailleurs.")
    c.fill(0, 0, 0, 6, 0, 6, "les_limbes:sable_d_opale")
    c.fill(1, 1, 1, 5, 1, 5, TAPIS_R)
    c.fill(1, 2, 1, 5, 2, 1, LAINE_O); c.fill(1, 2, 5, 5, 2, 5, LAINE_O)
    c.fill(1, 2, 2, 1, 2, 4, LAINE_B)
    c.fill(2, 3, 1, 4, 3, 5, LAINE_O)
    c.set(3, 4, 3, BARRIERE)
    c.set(3, 1, 3, AIR); c.set(3, 1, 3, TAPIS_B)
    coffre(c, 2, 2, 3, "east", "tente")
    c.set(4, 2, 3, TONNEAU)
    c.set(6, 1, 3, "minecraft:campfire[facing=north,lit=false,signal_fire=false,waterlogged=false]")
    c.lectern(2, 2, 4, "east", Canvas.written_book("Carnet de route", "Un marcheur", [
        "Jour 9. Le sable a changé de couleur pendant la nuit. Il brille. Le ciel aussi.",
        "Jour 10. Il y a une ville au sud avec des horloges partout. Je laisse la tente : là où je vais, il n'y a pas de nuit."]))
    return c


def build_cloche_de_verre() -> Canvas:
    """Petite Arrivée (Montagnes) : cloche suspendue entre quatre colonnes de verre de montagne ; elle sonne pour qui la frappe."""
    c = Canvas("cloche_de_verre", 5, 8, 5)
    c.note("Arrivée « La cloche de verre » : quatre colonnes de verre de montagne, linteaux, cloche de bronze, coffre de l'offrande.")
    c.fill(0, 0, 0, 4, 0, 4, PIERRE_T)
    for x, z in ((0, 0), (4, 0), (0, 4), (4, 4)):
        c.column(x, 1, 5, z, VERRE_M)
    c.fill(0, 6, 0, 4, 6, 0, "les_limbes:pierre_de_verre_taillee"); c.fill(0, 6, 4, 4, 6, 4, "les_limbes:pierre_de_verre_taillee")
    c.fill(0, 6, 1, 0, 6, 3, "les_limbes:pierre_de_verre_taillee"); c.fill(4, 6, 1, 4, 6, 3, "les_limbes:pierre_de_verre_taillee")
    c.fill(1, 6, 1, 3, 6, 3, "les_limbes:pierre_de_verre_taillee")
    c.set(2, 5, 2, "minecraft:bell[attachment=ceiling,facing=north,powered=false]")
    c.set(2, 7, 2, LANTERNE)
    coffre(c, 2, 1, 4, "north", "cloche")
    c.sign_wall(2, 2, 0, "minecraft:birch", "south", ["Qui sonne", "la cloche", "est entendu", "d'ailleurs"])
    return c


def build_bibliotheque_perdue() -> Canvas:
    """Moyenne Arrivée (Couronne) : salle de lecture de briques, rayonnages, lutrins et chroniques d'ailleurs, lustre, coffre."""
    c = Canvas("bibliotheque_perdue", 11, 8, 11)
    c.note("Arrivée « La bibliothèque perdue » : salle de briques moussues arrachée à une autre ville, rayonnages, trois lutrins et leurs livres, lustre, coffre de reliures.")
    c.fill(0, 0, 0, 10, 0, 10, TERRE_RETOURNEE)
    c.fill(1, 0, 1, 9, 0, 9, BRIQUES)
    c.fill(2, 0, 2, 8, 0, 8, "minecraft:dark_oak_planks")
    c.walls(1, 1, 1, 9, 4, 9, BRIQUES)
    for x, z in ((1, 1), (9, 1), (1, 9), (9, 9), (5, 1), (1, 5), (9, 5)):
        c.column(x, 1, 4, z, BRIQUES_M)
    c.fill(2, 1, 2, 8, 4, 8, AIR)
    c.fill(1, 5, 1, 9, 5, 9, DALLE_BRIQUES)
    c.fill(2, 5, 2, 8, 5, 8, BRIQUES)
    c.fill(4, 1, 9, 6, 2, 9, AIR)
    bookshelves(c, 2, 1, 2, 8, 3, 2)
    bookshelves(c, 2, 1, 3, 2, 3, 7)
    bookshelves(c, 8, 1, 3, 8, 3, 7)
    c.fill(3, 1, 3, 7, 1, 7, TAPIS_B)
    table(c, 4, 1, 5, 3, "x")
    chair(c, 5, 1, 6, "north"); chair(c, 5, 1, 4, "south")
    c.lectern(3, 1, 7, "east", Canvas.written_book("Chronique d'une ville sans nom", "Bibliothécaire", [
        "Notre ville a commencé à se perdre par les rues les moins fréquentées. Un jour la bibliothèque n'avait plus de porte sur la place.",
        "Si quelqu'un lit ceci ailleurs, qu'il sache que les livres, au moins, sont arrivés quelque part."]))
    c.lectern(7, 1, 7, "west", Canvas.written_book("Traité des plis du ciel", "Auteur inconnu", [
        "Le ciel se plisse là où quelque chose se perd. Les plis descendent ; ce qui se perd tombe le long des plis.",
        "Certains plis mènent à un royaume qui recueille. On dit qu'il a un roi, et que le roi appelle."]))
    c.lectern(5, 1, 2, "south", Canvas.written_book("Registre des prêts", "Bibliothécaire", ["Emprunté et jamais rendu : « Les routes du ciel », par un voyageur en manteau bleu nuit."]))
    coffre(c, 7, 1, 3, "west", "bibliotheque")
    c.set(5, 4, 5, LANTERNE_H); c.set(3, 4, 3, LANTERNE_H); c.set(7, 4, 7, LANTERNE_H)
    c.set(5, 6, 5, BANNIERE)
    return c


ARRIVEES = {
    "arrivees/charrette_renversee": build_charrette_renversee,
    "arrivees/puits_d_ailleurs": build_puits_d_ailleurs,
    "arrivees/kiosque_a_musique": build_kiosque_a_musique,
    "arrivees/radeau_des_naufrages": build_radeau_des_naufrages,
    "arrivees/serre_tombee": build_serre_tombee,
    "arrivees/tente_du_voyageur": build_tente_du_voyageur,
    "arrivees/cloche_de_verre": build_cloche_de_verre,
    "arrivees/bibliotheque_perdue": build_bibliotheque_perdue,
}
