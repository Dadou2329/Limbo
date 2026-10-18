"""DSL de construction voxel pour produire des structures Minecraft (.nbt).

Principes :
- Une `Canvas` est un volume (sx, sy, sz) rempli par défaut de `minecraft:structure_void`
  (le terrain existant est conservé là où rien n'est posé). On pose explicitement `air`
  pour creuser un intérieur.
- Les blocs sont des chaînes "namespace:nom[prop=val,prop2=val2]" ; des aides produisent
  les états courants (escaliers, dalles, portes, lits, bûches, lanternes...).
- Les entités de bloc (coffres avec table de butin, panneaux, pupitres, pots de fleurs,
  têtes...) reçoivent un dictionnaire NBT.

L'export respecte le format des structures 1.21.1 (DataVersion 3955).
"""
from __future__ import annotations

import json
import os
import re
from dataclasses import dataclass, field
from typing import Iterable

from nbt import Byte, Int, List, dump_gzip

DATA_VERSION = 3955  # Minecraft Java 1.21.1
VOID = "minecraft:structure_void"
AIR = "minecraft:air"

_STATE_RE = re.compile(r"^([a-z0-9_.-]+:)?([a-z0-9_/.-]+)(\[(.*)\])?$")


def parse_state(state: str) -> tuple[str, dict[str, str]]:
    m = _STATE_RE.match(state.strip())
    if not m:
        raise ValueError(f"État de bloc invalide : {state!r}")
    ns = m.group(1) or "minecraft:"
    name = ns + m.group(2)
    props: dict[str, str] = {}
    if m.group(4):
        for part in m.group(4).split(","):
            if not part:
                continue
            k, v = part.split("=", 1)
            props[k.strip()] = v.strip()
    return name, props


def with_props(state: str, **props) -> str:
    name, existing = parse_state(state)
    existing.update({k: str(v).lower() if isinstance(v, bool) else str(v) for k, v in props.items()})
    if not existing:
        return name
    return name + "[" + ",".join(f"{k}={v}" for k, v in sorted(existing.items())) + "]"


# Directions : (dx, dz)
DIRS = {"north": (0, -1), "south": (0, 1), "west": (-1, 0), "east": (1, 0)}
OPPOSITE = {"north": "south", "south": "north", "west": "east", "east": "west"}
ROT_CW = {"north": "east", "east": "south", "south": "west", "west": "north"}


def text_component(text: str, color: str | None = None, bold: bool = False) -> str:
    comp: dict = {"text": text}
    if color:
        comp["color"] = color
    if bold:
        comp["bold"] = True
    return json.dumps(comp, ensure_ascii=False)


@dataclass
class Entity:
    id: str
    pos: tuple[float, float, float]
    nbt: dict = field(default_factory=dict)


class Canvas:
    def __init__(self, name: str, sx: int, sy: int, sz: int):
        if sx <= 0 or sy <= 0 or sz <= 0:
            raise ValueError("Dimensions invalides")
        self.name = name
        self.sx, self.sy, self.sz = sx, sy, sz
        self.blocks: dict[tuple[int, int, int], str] = {}
        self.block_nbt: dict[tuple[int, int, int], dict] = {}
        self.entities: list[Entity] = []
        self.notes: list[str] = []  # commentaires d'inspection (pièces, fonctions)
        self.hors_volume = 0
        # Décalage logique -> physique (permet des débords de toit sans coordonnées négatives).
        self.shift = (0, 0, 0)

    # ------------------------------------------------------------------ primitives
    def inside(self, x: int, y: int, z: int) -> bool:
        x, y, z = x + self.shift[0], y + self.shift[1], z + self.shift[2]
        return 0 <= x < self.sx and 0 <= y < self.sy and 0 <= z < self.sz

    def set(self, x: int, y: int, z: int, state: str, nbt: dict | None = None) -> None:
        if not self.inside(x, y, z):
            # Écriture hors volume : ignorée mais comptée (voir stats()['hors_volume']) pour corriger les dimensions.
            self.hors_volume += 1
            return
        pos = (x + self.shift[0], y + self.shift[1], z + self.shift[2])
        if state == VOID:
            self.blocks.pop(pos, None)
            self.block_nbt.pop(pos, None)
            return
        self.blocks[pos] = state
        if nbt is not None:
            self.block_nbt[pos] = nbt
        else:
            self.block_nbt.pop(pos, None)

    def get(self, x: int, y: int, z: int) -> str:
        return self.blocks.get((x + self.shift[0], y + self.shift[1], z + self.shift[2]), VOID)

    def fill(self, x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, state: str) -> None:
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                for z in range(min(z1, z2), max(z1, z2) + 1):
                    self.set(x, y, z, state)

    def fill_if(self, x1, y1, z1, x2, y2, z2, state: str, only: Iterable[str]) -> None:
        only = set(only)
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                for z in range(min(z1, z2), max(z1, z2) + 1):
                    if self.get(x, y, z) in only:
                        self.set(x, y, z, state)

    def hollow_box(self, x1, y1, z1, x2, y2, z2, wall: str, inner: str = AIR) -> None:
        """Boîte creuse : parois `wall` (6 faces), intérieur `inner`."""
        self.fill(x1, y1, z1, x2, y2, z2, wall)
        if abs(x2 - x1) >= 2 and abs(y2 - y1) >= 2 and abs(z2 - z1) >= 2:
            self.fill(min(x1, x2) + 1, min(y1, y2) + 1, min(z1, z2) + 1,
                      max(x1, x2) - 1, max(y1, y2) - 1, max(z1, z2) - 1, inner)

    def walls(self, x1, y1, z1, x2, y2, z2, wall: str) -> None:
        """Quatre murs verticaux (sans sol ni plafond)."""
        xa, xb = min(x1, x2), max(x1, x2)
        za, zb = min(z1, z2), max(z1, z2)
        self.fill(xa, y1, za, xb, y2, za, wall)
        self.fill(xa, y1, zb, xb, y2, zb, wall)
        self.fill(xa, y1, za, xa, y2, zb, wall)
        self.fill(xb, y1, za, xb, y2, zb, wall)

    def column(self, x: int, y1: int, y2: int, z: int, state: str) -> None:
        self.fill(x, y1, z, x, y2, z, state)

    def line(self, x1, y, z1, x2, z2, state: str) -> None:
        """Ligne horizontale (axe x ou z)."""
        self.fill(x1, y, z1, x2, y, z2, state)

    # ------------------------------------------------------------------ éléments composés
    def stairs(self, x, y, z, material: str, facing: str, half: str = "bottom", shape: str = "straight") -> None:
        self.set(x, y, z, f"{material}_stairs[facing={facing},half={half},shape={shape},waterlogged=false]")

    def slab(self, x, y, z, material: str, kind: str = "bottom") -> None:
        self.set(x, y, z, f"{material}_slab[type={kind},waterlogged=false]")

    def log(self, x, y, z, wood: str, axis: str = "y") -> None:
        self.set(x, y, z, f"{wood}_log[axis={axis}]")

    def door(self, x, y, z, wood: str, facing: str, hinge: str = "left", open_: bool = False) -> None:
        base = f"{wood}_door[facing={facing},hinge={hinge},open={'true' if open_ else 'false'},powered=false"
        self.set(x, y, z, base + ",half=lower]")
        self.set(x, y + 1, z, base + ",half=upper]")

    def trapdoor(self, x, y, z, wood: str, facing: str, half: str = "top", open_: bool = False) -> None:
        self.set(x, y, z, f"{wood}_trapdoor[facing={facing},half={half},open={'true' if open_ else 'false'},powered=false,waterlogged=false]")

    def bed(self, x, y, z, color: str, facing: str) -> None:
        """Lit dont le pied est en (x,z) et la tête vers `facing`."""
        dx, dz = DIRS[facing]
        self.set(x, y, z, f"{color}_bed[facing={facing},part=foot,occupied=false]")
        self.set(x + dx, y, z + dz, f"{color}_bed[facing={facing},part=head,occupied=false]")

    def lantern(self, x, y, z, hanging: bool = False, soul: bool = False) -> None:
        name = "soul_lantern" if soul else "lantern"
        self.set(x, y, z, f"{name}[hanging={'true' if hanging else 'false'},waterlogged=false]")

    def torch_wall(self, x, y, z, facing: str) -> None:
        self.set(x, y, z, f"wall_torch[facing={facing}]")

    def fence(self, x, y, z, wood: str) -> None:
        self.set(x, y, z, f"{wood}_fence")

    def glass_pane(self, x, y, z, color: str | None = None) -> None:
        name = f"{color}_stained_glass_pane" if color else "glass_pane"
        self.set(x, y, z, name)

    def carpet(self, x, y, z, color: str) -> None:
        self.set(x, y, z, f"{color}_carpet")

    def flower_pot(self, x, y, z, plant: str = "potted_azure_bluet") -> None:
        self.set(x, y, z, plant)

    def chest(self, x, y, z, facing: str, loot_table: str | None = None, items: list[dict] | None = None) -> None:
        nbt: dict = {"id": "minecraft:chest"}
        if loot_table:
            nbt["LootTable"] = loot_table
        if items:
            nbt["Items"] = List(items)
        self.set(x, y, z, f"chest[facing={facing},type=single,waterlogged=false]", nbt)

    def barrel(self, x, y, z, facing: str = "up", loot_table: str | None = None) -> None:
        nbt: dict = {"id": "minecraft:barrel"}
        if loot_table:
            nbt["LootTable"] = loot_table
        self.set(x, y, z, f"barrel[facing={facing},open=false]", nbt)

    def sign_standing(self, x, y, z, wood: str, rotation: int, lines: list[str], color: str = "black", glowing: bool = False) -> None:
        self.set(x, y, z, f"{wood}_sign[rotation={rotation},waterlogged=false]", self._sign_nbt(lines, color, glowing))

    def sign_wall(self, x, y, z, wood: str, facing: str, lines: list[str], color: str = "black", glowing: bool = False) -> None:
        self.set(x, y, z, f"{wood}_wall_sign[facing={facing},waterlogged=false]", self._sign_nbt(lines, color, glowing))

    def hanging_sign_wall(self, x, y, z, wood: str, facing: str, lines: list[str], color: str = "black") -> None:
        self.set(x, y, z, f"{wood}_wall_hanging_sign[facing={facing},waterlogged=false]", self._sign_nbt(lines, color, False))

    @staticmethod
    def _sign_nbt(lines: list[str], color: str, glowing: bool) -> dict:
        msgs = [text_component(l) for l in (lines + [""] * 4)[:4]]
        face = {"messages": List(msgs), "color": color, "has_glowing_text": Byte(1 if glowing else 0)}
        back = {"messages": List([text_component("")] * 4), "color": "black", "has_glowing_text": Byte(0)}
        return {"id": "minecraft:sign", "front_text": face, "back_text": back, "is_waxed": Byte(1)}

    def lectern(self, x, y, z, facing: str, book: dict | None = None) -> None:
        nbt: dict = {"id": "minecraft:lectern"}
        has_book = book is not None
        if has_book:
            nbt["Book"] = book
            nbt["Page"] = Int(0)
        self.set(x, y, z, f"lectern[facing={facing},has_book={'true' if has_book else 'false'},powered=false]", nbt)

    @staticmethod
    def written_book(title: str, author: str, pages: list[str]) -> dict:
        """Objet livre écrit (format composants 1.21). `pages` : texte brut par page."""
        return {
            "id": "minecraft:written_book",
            "count": Int(1),
            "components": {
                "minecraft:written_book_content": {
                    "title": {"raw": title},
                    "author": author,
                    "pages": List([{"raw": text_component(p)} for p in pages]),
                    "generation": Int(0),
                    "resolved": Byte(1),
                }
            },
        }

    def item_frame(self, x, y, z, facing: str, item_id: str | None = None) -> None:
        """Cadre (entité) accroché sur la face `facing` du bloc voisin."""
        fdir = {"north": 2, "south": 3, "west": 4, "east": 5, "up": 1, "down": 0}[facing]
        nbt: dict = {"Facing": Byte(fdir), "Fixed": Byte(1)}
        if item_id:
            nbt["Item"] = {"id": item_id, "count": Int(1)}
        self.entities.append(Entity("minecraft:item_frame", (x + 0.5, y + 0.5, z + 0.5), nbt))

    def armor_stand(self, x, y, z, yaw: float = 0.0) -> None:
        self.entities.append(Entity("minecraft:armor_stand", (x + 0.5, y, z + 0.5), {"Rotation": List([float(yaw), 0.0])}))

    def custom_entity(self, entity_id: str, x: float, y: float, z: float, nbt: dict | None = None) -> None:
        self.entities.append(Entity(entity_id, (x, y, z), dict(nbt or {})))

    def gable_roof(self, x1: int, z1: int, x2: int, z2: int, y: int, material: str, axis: str = "x",
                   overhang: int = 1, ridge_slab: bool = True) -> None:
        """Toit à deux pans en escaliers. `axis` = direction de la faîtière."""
        xa, xb = min(x1, x2) - overhang, max(x1, x2) + overhang
        za, zb = min(z1, z2) - overhang, max(z1, z2) + overhang
        if axis == "x":
            depth = zb - za + 1
            levels = (depth + 1) // 2
            for lvl in range(levels):
                zn, zs = za + lvl, zb - lvl
                yy = y + lvl
                if zn > zs:
                    break
                if zn == zs:
                    if ridge_slab:
                        self.fill(xa, yy, zn, xb, yy, zn, f"{material}_slab[type=bottom,waterlogged=false]")
                    else:
                        self.fill(xa, yy, zn, xb, yy, zn, self._full(material))
                    break
                self.fill(xa, yy, zn, xb, yy, zn, f"{material}_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]")
                self.fill(xa, yy, zs, xb, yy, zs, f"{material}_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
                if zs - zn == 1 and ridge_slab:
                    pass
                # remplissage sous la faîtière pour fermer les pignons
                if zs - zn >= 2:
                    for zz in range(zn + 1, zs):
                        if self.get(xa, yy, zz) == VOID:
                            self.set(xa, yy, zz, self._full(material))
                        if self.get(xb, yy, zz) == VOID:
                            self.set(xb, yy, zz, self._full(material))
        else:
            width = xb - xa + 1
            levels = (width + 1) // 2
            for lvl in range(levels):
                xw, xe = xa + lvl, xb - lvl
                yy = y + lvl
                if xw > xe:
                    break
                if xw == xe:
                    if ridge_slab:
                        self.fill(xw, yy, za, xw, yy, zb, f"{material}_slab[type=bottom,waterlogged=false]")
                    else:
                        self.fill(xw, yy, za, xw, yy, zb, self._full(material))
                    break
                self.fill(xw, yy, za, xw, yy, zb, f"{material}_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]")
                self.fill(xe, yy, za, xe, yy, zb, f"{material}_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]")
                if xe - xw >= 2:
                    for xx in range(xw + 1, xe):
                        if self.get(xx, yy, za) == VOID:
                            self.set(xx, yy, za, self._full(material))
                        if self.get(xx, yy, zb) == VOID:
                            self.set(xx, yy, zb, self._full(material))

    @staticmethod
    def _full(material: str) -> str:
        """Bloc plein correspondant à un matériau d'escalier (oak -> oak_planks, stone_brick -> stone_bricks...)."""
        mapping = {
            "stone_brick": "stone_bricks", "mossy_stone_brick": "mossy_stone_bricks", "brick": "bricks",
            "nether_brick": "nether_bricks", "red_nether_brick": "red_nether_bricks", "end_stone_brick": "end_stone_bricks",
            "quartz": "quartz_block", "smooth_quartz": "smooth_quartz", "purpur": "purpur_block",
            "prismarine_brick": "prismarine_bricks", "dark_prismarine": "dark_prismarine", "prismarine": "prismarine",
            "polished_blackstone_brick": "polished_blackstone_bricks", "deepslate_brick": "deepslate_bricks",
            "deepslate_tile": "deepslate_tiles", "mud_brick": "mud_bricks", "tuff_brick": "tuff_bricks",
            "cobblestone": "cobblestone", "mossy_cobblestone": "mossy_cobblestone", "stone": "stone",
            "sandstone": "sandstone", "smooth_sandstone": "smooth_sandstone", "red_sandstone": "red_sandstone",
            "cut_copper": "cut_copper", "exposed_cut_copper": "exposed_cut_copper", "weathered_cut_copper": "weathered_cut_copper",
            "oxidized_cut_copper": "oxidized_cut_copper", "andesite": "andesite", "polished_andesite": "polished_andesite",
            "diorite": "diorite", "polished_diorite": "polished_diorite", "granite": "granite", "polished_granite": "polished_granite",
            "blackstone": "blackstone", "polished_blackstone": "polished_blackstone", "cobbled_deepslate": "cobbled_deepslate",
            "polished_deepslate": "polished_deepslate", "smooth_stone": "smooth_stone", "polished_tuff": "polished_tuff",
        }
        ns = ""
        if ":" in material:
            ns, material = material.split(":", 1)
            ns += ":"
        if material in mapping:
            return ns + mapping[material]
        # bois (oak, spruce, ... et bois personnalisés) -> planches
        return ns + material + "_planks"

    # ------------------------------------------------------------------ inspection
    def note(self, text: str) -> None:
        self.notes.append(text)

    def stats(self) -> dict:
        counts: dict[str, int] = {}
        for state in self.blocks.values():
            name, _ = parse_state(state)
            counts[name] = counts.get(name, 0) + 1
        return {
            "name": self.name,
            "size": [self.sx, self.sy, self.sz],
            "blocks": len(self.blocks),
            "block_entities": len(self.block_nbt),
            "entities": len(self.entities),
            "hors_volume": self.hors_volume,
            "palette": dict(sorted(counts.items(), key=lambda kv: -kv[1])),
        }

    def ascii_layers(self, legend: dict[str, str] | None = None) -> str:
        """Vue couche par couche (y croissant), un caractère par bloc, pour inspection rapide."""
        legend = dict(legend or {})
        auto = "#%&@+=*:;,.-~^!?$abcdefghijklmnopqrstuvwxyz"
        out = []
        for y in range(self.sy):
            out.append(f"--- y={y} ---")
            for z in range(self.sz):
                row = []
                for x in range(self.sx):
                    st = self.blocks.get((x, y, z), VOID)
                    if st == VOID:
                        row.append(" ")
                        continue
                    name, _ = parse_state(st)
                    if name == AIR:
                        row.append(".")
                        continue
                    if name not in legend:
                        legend[name] = auto[len(legend) % len(auto)]
                    row.append(legend[name])
                out.append("".join(row))
        out.append("Légende : " + ", ".join(f"{v}={k}" for k, v in legend.items()))
        return "\n".join(out)

    # ------------------------------------------------------------------ export
    def to_nbt(self) -> dict:
        palette: list[dict] = []
        index: dict[str, int] = {}
        blocks = List()
        for (x, y, z), state in sorted(self.blocks.items(), key=lambda kv: (kv[0][1], kv[0][2], kv[0][0])):
            if state not in index:
                name, props = parse_state(state)
                entry: dict = {"Name": name}
                if props:
                    entry["Properties"] = {k: v for k, v in props.items()}
                index[state] = len(palette)
                palette.append(entry)
            block: dict = {"pos": List([Int(x), Int(y), Int(z)]), "state": Int(index[state])}
            nbt = self.block_nbt.get((x, y, z))
            if nbt is not None:
                block["nbt"] = nbt
            blocks.append(block)
        entities = List()
        for e in self.entities:
            nbt = dict(e.nbt)
            nbt["id"] = e.id
            entities.append({
                "pos": List([float(e.pos[0]), float(e.pos[1]), float(e.pos[2])]),
                "blockPos": List([Int(int(e.pos[0] // 1)), Int(int(e.pos[1] // 1)), Int(int(e.pos[2] // 1))]),
                "nbt": nbt,
            })
        return {
            "DataVersion": Int(DATA_VERSION),
            "size": List([Int(self.sx), Int(self.sy), Int(self.sz)]),
            "palette": List(palette),
            "blocks": blocks,
            "entities": entities,
        }

    def save(self, out_dir: str) -> str:
        os.makedirs(out_dir, exist_ok=True)
        path = os.path.join(out_dir, self.name + ".nbt")
        dump_gzip(self.to_nbt(), path)
        return path
