"""Gabarits des Montagnes de Verre : Verréole (grande forge, bains, palais de miroirs d'Orsane, atelier d'instruments), bains des sources, atelier d'instruments, pont qui chante."""
from __future__ import annotations

import math

from builders import (BARRIERE, CRISTAL, LANTERNE, LANTERNE_H, PIERRE_P, PIERRE_S, PIERRE_T, PLANCHES, chair, counter, data_marker, door, hanging_lantern, lamppost,
                      table, window)
from canvas import AIR, VOID, Canvas

PV = "les_limbes:pierre_de_verre"
PV_T = "les_limbes:pierre_de_verre_taillee"
VERRE_M = "les_limbes:verre_de_montagne"
CRISTAL_R = "les_limbes:cristal_resonnant"
PONT = "les_limbes:pont_de_verre[forme=false]"
COURANT = "les_limbes:courant_cristallin"
PORTE_RELAIS = "les_limbes:porte_de_relais"
VERRE = "minecraft:glass"
MIROIR = "minecraft:tinted_glass"
EAU = "minecraft:water[level=0]"
STAIR_PV = "minecraft:polished_deepslate"
DALLE_PV = "minecraft:polished_deepslate_slab[type=bottom,waterlogged=false]"
DALLE_PV_H = "minecraft:polished_deepslate_slab[type=top,waterlogged=false]"


def relais(c: Canvas, x: int, y: int, z: int, facing: str, ident: str) -> None:
    c.set(x, y, z, f"{PORTE_RELAIS}[eveillee=false,facing={facing}]", {"id": PORTE_RELAIS, "relais": ident})
    c.set(x, y + 1, z, AIR)


def stair(material: str, facing: str, half: str = "bottom") -> str:
    return f"{material}_stairs[facing={facing},half={half},shape=straight,waterlogged=false]"


def batiment_de_verre(c: Canvas, x0: int, y: int, z0: int, w: int, d: int, h: int, role: str | None, facing: str = "south", enseigne: list[str] | None = None) -> None:
    """Bâtiment de pierre de verre taillée, toit pentu en ardoise polie, fenêtres de verre de montagne, lanternes."""
    c.fill(x0, y, z0, x0 + w - 1, y, z0 + d - 1, PV_T)
    c.fill(x0 + 1, y, z0 + 1, x0 + w - 2, y, z0 + d - 2, PLANCHES)
    c.walls(x0, y + 1, z0, x0 + w - 1, y + h, z0 + d - 1, PV_T)
    c.fill(x0 + 1, y + 1, z0 + 1, x0 + w - 2, y + h, z0 + d - 2, AIR)
    for cx, cz in [(x0, z0), (x0 + w - 1, z0), (x0, z0 + d - 1), (x0 + w - 1, z0 + d - 1)]:
        c.column(cx, y + 1, y + h, cz, PV)
    c.fill(x0, y + h + 1, z0, x0 + w - 1, y + h + 1, z0 + d - 1, PV_T)
    c.gable_roof(x0, z0, x0 + w - 1, z0 + d - 1, y + h + 2, STAIR_PV, axis="x" if w >= d else "z", overhang=1)
    dz = z0 + d - 1 if facing == "south" else z0
    door(c, x0 + w // 2, y + 1, dz, facing)
    for wx in (x0 + 1, x0 + w - 2):
        c.set(wx, y + 2, dz, VERRE_M)
    c.set(x0, y + 2, z0 + d // 2, VERRE_M); c.set(x0 + w - 1, y + 2, z0 + d // 2, VERRE_M)
    hanging_lantern(c, x0 + w // 2, y + h, z0 + d // 2)
    if enseigne:
        c.sign_wall(x0 + w // 2 + 1, y + 2, dz, "minecraft:spruce", facing, enseigne)
    if role:
        data_marker(c, x0 + w // 2, y + 1, z0 + d // 2, "pnj:" + role)


def build_verreole() -> Canvas:
    """Verréole : plateau de pierre de verre, grande forge à deux halls, palais de miroirs d'Orsane, bains, atelier, maisons, remontée cristalline."""
    W, H, D = 65, 30, 65
    c = Canvas("verreole", W, H, D)
    c.note("Verréole : cité des Montagnes de Verre. Grande forge, palais de miroirs d'Orsane, bains de vapeur, atelier d'instruments, maisons de verriers, remontée cristalline, porte de relais.")
    cx, cz = 32, 32
    # sol : plateau (y local 0 = monde 95, on marche à 96 = y1) pavé au centre
    for x in range(W):
        for z in range(D):
            d = math.hypot(x - cx, z - cz)
            if d <= 32:
                c.set(x, 0, z, PV if d > 12 else (PV_T if int(d) % 3 else VERRE_M))
    # grande forge (nord) : hall à deux foyers, cheminées, enclumes, bassin de trempe
    fx, fz = cx - 9, cz - 30
    batiment_de_verre(c, fx, 1, fz, 19, 15, 6, None, facing="south", enseigne=["Grande forge", "de Verréole"])
    for x in (fx + 4, fx + 14):
        c.fill(x - 1, 1, fz + 2, x + 1, 1, fz + 4, PV)
        c.set(x, 1, fz + 3, "minecraft:lava[level=0]")
        c.column(x, 2, 12, fz + 3, PV_T); c.set(x, 2, fz + 3, "minecraft:campfire[facing=south,lit=true,signal_fire=false,waterlogged=false]")
        c.set(x, 13, fz + 3, "minecraft:campfire[facing=south,lit=true,signal_fire=true,waterlogged=false]")
    for x in (fx + 6, fx + 9, fx + 12):
        c.set(x, 1, fz + 7, "minecraft:anvil[facing=east]")
    c.fill(fx + 2, 1, fz + 10, fx + 4, 1, fz + 12, PV_T); c.set(fx + 3, 1, fz + 11, EAU)
    c.set(fx + 15, 1, fz + 11, "minecraft:grindstone[face=floor,facing=south]"); c.set(fx + 16, 1, fz + 11, "minecraft:smithing_table")
    c.set(fx + 1, 1, fz + 13, "minecraft:chest[facing=east,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/verreole"})
    for x in range(fx + 1, fx + 18, 4):
        c.set(x, 1, fz + 1, "minecraft:blast_furnace[facing=south,lit=false]")
    data_marker(c, fx + 9, 1, fz + 9, "pnj:maitre_forgeron")
    data_marker(c, fx + 5, 1, fz + 5, "pnj:apprentie_verriere")
    # palais de miroirs d'Orsane (est) : salle longue aux murs de verre teinté, colonnes de cristal, trône de verre
    px, pz = cx + 10, cz - 10
    c.fill(px, 1, pz, px + 20, 1, pz + 20, PV_T)
    c.fill(px + 1, 1, pz + 1, px + 19, 1, pz + 19, PIERRE_P)
    c.walls(px, 2, pz, px + 20, 7, pz + 20, PV_T)
    c.fill(px + 1, 2, pz + 1, px + 19, 7, pz + 19, AIR)
    for z in range(pz + 2, pz + 19, 2):
        c.set(px, 3, z, MIROIR); c.set(px, 4, z, MIROIR); c.set(px + 20, 3, z, MIROIR); c.set(px + 20, 4, z, MIROIR)
    for x in range(px + 2, px + 19, 2):
        c.set(x, 3, pz, MIROIR); c.set(x, 4, pz, MIROIR)
    for x in (px + 4, px + 10, px + 16):
        for z in (pz + 5, pz + 15):
            c.column(x, 2, 6, z, CRISTAL_R)
            c.set(x, 7, z, PV)
    c.fill(px, 8, pz, px + 20, 8, pz + 20, VERRE_M)
    c.fill(px + 2, 9, pz + 2, px + 18, 9, pz + 18, PV_T)
    c.fill(px + 6, 10, pz + 6, px + 14, 10, pz + 14, VERRE_M)
    c.set(px + 10, 11, pz + 10, CRISTAL)
    door(c, px + 10, 2, pz + 20, "south"); c.set(px + 9, 2, pz + 20, AIR); c.set(px + 9, 3, pz + 20, AIR)
    c.fill(px + 8, 2, pz + 2, px + 12, 2, pz + 3, PV_T)
    c.set(px + 10, 3, pz + 2, stair(STAIR_PV, "south")); c.set(px + 9, 3, pz + 2, VERRE_M); c.set(px + 11, 3, pz + 2, VERRE_M)
    c.set(px + 10, 4, pz + 2, VERRE_M)
    for x in range(px + 7, px + 14):
        c.carpet(x, 2, pz + 10, "light_blue")
    for i in range(pz + 4, pz + 19):
        c.carpet(px + 10, 2, i, "white")
    hanging_lantern(c, px + 5, 6, pz + 10); hanging_lantern(c, px + 15, 6, pz + 10)
    data_marker(c, px + 10, 3, pz + 3, "pnj:orsane")
    data_marker(c, px + 4, 2, pz + 10, "pnj:garde_de_verre")
    data_marker(c, px + 16, 2, pz + 10, "pnj:garde_de_verre_2")
    c.hanging_sign_wall(px + 12, 4, pz + 20, "minecraft:spruce", "south", ["Palais des", "miroirs"])
    # bains de vapeur (ouest) : bassins chauds, verre, bancs
    bx, bz = cx - 30, cz - 6
    batiment_de_verre(c, bx, 1, bz, 15, 15, 5, "baigneur", facing="east", enseigne=["Bains de", "Verréole"])
    c.fill(bx + 2, 1, bz + 2, bx + 12, 1, bz + 12, PV_T)
    for ox, oz in [(bx + 3, bz + 3), (bx + 8, bz + 3), (bx + 3, bz + 8), (bx + 8, bz + 8)]:
        c.fill(ox, 1, oz, ox + 3, 1, oz + 3, EAU)
        c.set(ox + 1, 0, oz + 1, "minecraft:magma_block"); c.set(ox + 2, 0, oz + 2, "minecraft:magma_block")
    c.set(bx + 7, 1, bz + 7, PV_T); c.set(bx + 7, 2, bz + 7, LANTERNE)
    # atelier d'instruments (sud) : établis, blocs musicaux, cristaux résonnants
    ax, az = cx - 4, cz + 16
    batiment_de_verre(c, ax, 1, az, 11, 9, 4, "luthiere", facing="north", enseigne=["Atelier", "d'instruments"])
    for i, x in enumerate(range(ax + 2, ax + 9, 2)):
        c.set(x, 1, az + 7, f"minecraft:note_block[instrument=harp,note={(i * 4) % 25},powered=false]")
        c.set(x, 1, az + 2, CRISTAL_R if i % 2 else "minecraft:crafting_table")
    c.set(ax + 1, 1, az + 4, "minecraft:jukebox[has_record=false]")
    # maisons de verriers (sud-ouest, sud-est), remontée cristalline vers un belvédère (ouest), porte de relais (centre nord)
    batiment_de_verre(c, cx - 26, 1, cz + 12, 9, 9, 4, "verrier_1", facing="east")
    batiment_de_verre(c, cx + 14, 1, cz + 14, 9, 9, 4, "verrier_2", facing="west")
    batiment_de_verre(c, cx + 16, 1, cz + 2, 9, 7, 4, "aubergiste_verreole", facing="west", enseigne=["Auberge du", "Cristal"])
    rx, rz = cx - 12, cz + 24
    c.fill(rx - 1, 1, rz - 1, rx + 1, 1, rz + 1, PV_T)
    for y in range(1, 20):
        c.set(rx, y, rz, COURANT)
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if y % 6 == 0:
                c.set(rx + dx, y, rz + dz, VERRE_M)
            elif c.get(rx + dx, y, rz + dz) == VOID:
                c.set(rx + dx, y, rz + dz, PV_T if y < 4 else VOID)
    c.fill(rx - 3, 20, rz - 3, rx + 3, 20, rz + 3, VERRE_M)
    for x in range(rx - 3, rx + 4):
        c.set(x, 21, rz - 3, BARRIERE); c.set(x, 21, rz + 3, BARRIERE)
    for z in range(rz - 3, rz + 4):
        c.set(rx - 3, 21, z, BARRIERE); c.set(rx + 3, 21, z, BARRIERE)
    c.set(rx, 20, rz, AIR); c.set(rx, 20, rz + 1, PV_T); c.set(rx, 21, rz + 3, AIR)
    c.set(rx + 3, 22, rz + 3, LANTERNE); c.set(rx - 3, 22, rz - 3, LANTERNE)
    c.sign_standing(rx + 2, 1, rz + 2, "minecraft:spruce", 0, ["Remontée", "cristalline", "entrez dans", "le courant"])
    relais(c, cx, 1, cz - 13, "south", "verreole")
    for a in range(0, 360, 60):
        x = int(round(cx + math.cos(math.radians(a + 30)) * 11)); z = int(round(cz + math.sin(math.radians(a + 30)) * 11))
        c.set(x, 1, z, PV_T); c.set(x, 2, z, CRISTAL_R)
    for x, z in [(cx - 24, cz - 24), (cx + 24, cz + 26), (cx + 26, cz - 26), (cx - 26, cz + 24)]:
        lamppost(c, x, 1, z, 3)
    c.sign_standing(cx + 3, 1, cz + 12, "minecraft:spruce", 8, ["Verréole", "forge : nord", "palais : est", "bains : ouest"])
    return c


def build_bains_des_sources() -> Canvas:
    """Bains alimentés par des sources chaudes : bassins en terrasse à l'air libre, vapeur, pavillon de bois."""
    W, H, D = 21, 10, 21
    c = Canvas("bains_des_sources", W, H, D)
    c.note("Bains des sources : trois bassins chauds en terrasse, pavillon de pierre de verre, baigneuse Nell.")
    for x in range(W):
        for z in range(D):
            if math.hypot(x - 10, z - 10) <= 10.5:
                c.set(x, 0, z, PV_T)
    for i, (ox, oz, r) in enumerate([(10, 10, 4), (4, 5, 2), (15, 14, 2)]):
        for x in range(W):
            for z in range(D):
                d = math.hypot(x - ox, z - oz)
                if d <= r + 0.5:
                    c.set(x, 0, z, EAU if d <= r - 0.5 else PV)
                    if d <= r - 0.5 and (x + z) % 3 == 0:
                        c.set(x, -1, z, "minecraft:magma_block") if c.inside(x, -1, z) else None
        c.set(ox, 0, oz, "minecraft:magma_block") if i else None
    c.fill(2, 1, 14, 8, 1, 18, PLANCHES)
    for x, z in [(2, 14), (8, 14), (2, 18), (8, 18)]:
        c.column(x, 2, 4, z, PV)
    c.fill(2, 5, 14, 8, 5, 18, DALLE_PV_H)
    c.set(5, 2, 16, "minecraft:chest[facing=north,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/verreole"})
    for x in (3, 7):
        c.set(x, 4, 16, LANTERNE_H)
    data_marker(c, 5, 2, 15, "pnj:nell")
    c.sign_standing(12, 1, 3, "minecraft:spruce", 0, ["Bains des", "sources", "l'eau chante", "le soir"])
    return c


def build_atelier_d_instruments() -> Canvas:
    """Atelier d'instruments : maison-atelier de luthier à cristaux, orgue de cristaux résonnants qu'on joue en marchant."""
    W, H, D = 17, 16, 15
    c = Canvas("atelier_d_instruments", W, H, D)
    c.note("Atelier d'instruments des Montagnes : établis de luthier, orgue de cristaux résonnants (on joue en marchant), luthier Tobie.")
    for x in range(W):
        for z in range(D):
            c.set(x, 0, z, PV)
    batiment_de_verre(c, 2, 1, 1, 13, 11, 4, "tobie", facing="south", enseigne=["Atelier", "d'instruments"])
    for i, x in enumerate(range(3, 14)):
        c.set(x, 1, 3, CRISTAL_R if i % 2 == 0 else PV_T)
    c.set(4, 1, 8, "minecraft:crafting_table"); c.set(5, 1, 8, "minecraft:fletching_table"); c.set(12, 1, 8, "minecraft:jukebox[has_record=false]")
    for i, x in enumerate((7, 9, 11)):
        c.set(x, 1, 9, f"minecraft:note_block[instrument=bell,note={5 + i * 5},powered=false]")
    c.set(12, 1, 2, "minecraft:chest[facing=west,type=single,waterlogged=false]", {"id": "minecraft:chest", "LootTable": "les_limbes:chests/verreole"})
    return c


def build_pont_qui_chante() -> Canvas:
    """Pont qui chante : tablier de pont de verre (ne se forme qu'au passage) entre deux culées de pierre, au-dessus de la gorge ; cristaux chantants aux piliers."""
    W, H, D = 9, 10, 55
    c = Canvas("pont_qui_chante", W, H, D)
    c.note("Pont qui chante : 45 blocs de pont de verre qui se reforment au passage au-dessus d'une gorge, culées de pierre de verre, cristaux résonnants.")
    # culées (y local 0..3 = monde 100..103 ; tablier à y 4 = monde 104 ; les sites aplanis mettent les rives à 104)
    for z0 in (0, D - 5):
        c.fill(0, 0, z0, W - 1, 3, z0 + 4, PV_T)
        c.fill(1, 4, z0, W - 2, 4, z0 + 4, PV_T)
        for x in (0, W - 1):
            c.column(x, 4, 6, z0 + 2, PV); c.set(x, 7, z0 + 2, CRISTAL_R)
    for z in range(5, D - 5):
        for x in range(2, W - 2):
            c.set(x, 4, z, PONT)
        c.set(1, 4, z, AIR); c.set(W - 2, 4, z, AIR)
        if z % 9 == 0:
            c.set(1, 4, z, PV_T); c.set(1, 5, z, CRISTAL_R)
            c.set(W - 2, 4, z, PV_T); c.set(W - 2, 5, z, CRISTAL_R)
    c.sign_standing(4, 5, 2, "minecraft:spruce", 0, ["Pont qui", "chante", "avancez :", "il se forme"])
    c.sign_standing(4, 5, D - 3, "minecraft:spruce", 8, ["Pont qui", "chante", "avancez :", "il se forme"])
    return c


MONTAGNES = {
    "montagnes/verreole": build_verreole,
    "montagnes/bains_des_sources": build_bains_des_sources,
    "montagnes/atelier_d_instruments": build_atelier_d_instruments,
    "montagnes/pont_qui_chante": build_pont_qui_chante,
}
