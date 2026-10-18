"""Écriture minimale du format NBT (Named Binary Tag) de Minecraft, sans dépendance externe.

Seule l'écriture est implémentée : elle suffit à produire des fichiers de structure (.nbt)
lisibles par Minecraft 1.21.1. Les types sont représentés par de petites classes enveloppes
afin de distinguer, par exemple, un Byte d'un Int ou une List d'un IntArray.
"""
from __future__ import annotations

import gzip
import struct
from dataclasses import dataclass
from typing import Any

TAG_END, TAG_BYTE, TAG_SHORT, TAG_INT, TAG_LONG, TAG_FLOAT, TAG_DOUBLE = range(7)
TAG_BYTE_ARRAY, TAG_STRING, TAG_LIST, TAG_COMPOUND, TAG_INT_ARRAY, TAG_LONG_ARRAY = range(7, 13)


@dataclass(frozen=True)
class Byte:
    value: int


@dataclass(frozen=True)
class Short:
    value: int


@dataclass(frozen=True)
class Int:
    value: int


@dataclass(frozen=True)
class Long:
    value: int


@dataclass(frozen=True)
class Float:
    value: float


@dataclass(frozen=True)
class Double:
    value: float


@dataclass(frozen=True)
class IntArray:
    values: list


@dataclass(frozen=True)
class LongArray:
    values: list


@dataclass(frozen=True)
class ByteArray:
    values: bytes


class List(list):
    """Liste NBT homogène. Le type d'élément est déduit du premier élément (TAG_END si vide)."""


def tag_type(value: Any) -> int:
    if isinstance(value, Byte) or isinstance(value, bool):
        return TAG_BYTE
    if isinstance(value, Short):
        return TAG_SHORT
    if isinstance(value, Int) or isinstance(value, int):
        return TAG_INT
    if isinstance(value, Long):
        return TAG_LONG
    if isinstance(value, Float):
        return TAG_FLOAT
    if isinstance(value, Double) or isinstance(value, float):
        return TAG_DOUBLE
    if isinstance(value, ByteArray):
        return TAG_BYTE_ARRAY
    if isinstance(value, str):
        return TAG_STRING
    if isinstance(value, list):
        return TAG_LIST
    if isinstance(value, dict):
        return TAG_COMPOUND
    if isinstance(value, IntArray):
        return TAG_INT_ARRAY
    if isinstance(value, LongArray):
        return TAG_LONG_ARRAY
    raise TypeError(f"Type NBT non pris en charge : {type(value)!r}")


def _write_string(out: bytearray, text: str) -> None:
    data = text.encode("utf-8")
    out += struct.pack(">H", len(data))
    out += data


def write_payload(out: bytearray, value: Any) -> None:
    t = tag_type(value)
    if t == TAG_BYTE:
        v = int(value.value) if isinstance(value, Byte) else int(value)
        out += struct.pack(">b", v)
    elif t == TAG_SHORT:
        out += struct.pack(">h", value.value)
    elif t == TAG_INT:
        out += struct.pack(">i", value.value if isinstance(value, Int) else value)
    elif t == TAG_LONG:
        out += struct.pack(">q", value.value)
    elif t == TAG_FLOAT:
        out += struct.pack(">f", value.value)
    elif t == TAG_DOUBLE:
        out += struct.pack(">d", value.value if isinstance(value, Double) else value)
    elif t == TAG_BYTE_ARRAY:
        out += struct.pack(">i", len(value.values))
        out += bytes(value.values)
    elif t == TAG_STRING:
        _write_string(out, value)
    elif t == TAG_LIST:
        elem_type = tag_type(value[0]) if value else TAG_END
        out += struct.pack(">bi", elem_type, len(value))
        for item in value:
            if tag_type(item) != elem_type:
                raise TypeError("Liste NBT hétérogène")
            write_payload(out, item)
    elif t == TAG_COMPOUND:
        for key, item in value.items():
            out += struct.pack(">b", tag_type(item))
            _write_string(out, key)
            write_payload(out, item)
        out += struct.pack(">b", TAG_END)
    elif t == TAG_INT_ARRAY:
        out += struct.pack(">i", len(value.values))
        for v in value.values:
            out += struct.pack(">i", v)
    elif t == TAG_LONG_ARRAY:
        out += struct.pack(">i", len(value.values))
        for v in value.values:
            out += struct.pack(">q", v)


def dumps(root: dict, root_name: str = "") -> bytes:
    out = bytearray()
    out += struct.pack(">b", TAG_COMPOUND)
    _write_string(out, root_name)
    write_payload(out, root)
    return bytes(out)


def dump_gzip(root: dict, path: str) -> None:
    with gzip.open(path, "wb") as fh:
        fh.write(dumps(root))


# ---------------------------------------------------------------- lecture (pour inspection)

def _read_string(data: bytes, pos: int):
    (n,) = struct.unpack_from(">H", data, pos)
    pos += 2
    return data[pos:pos + n].decode("utf-8"), pos + n


def _read_payload(data: bytes, pos: int, t: int):
    if t == TAG_BYTE:
        return struct.unpack_from(">b", data, pos)[0], pos + 1
    if t == TAG_SHORT:
        return struct.unpack_from(">h", data, pos)[0], pos + 2
    if t == TAG_INT:
        return struct.unpack_from(">i", data, pos)[0], pos + 4
    if t == TAG_LONG:
        return struct.unpack_from(">q", data, pos)[0], pos + 8
    if t == TAG_FLOAT:
        return struct.unpack_from(">f", data, pos)[0], pos + 4
    if t == TAG_DOUBLE:
        return struct.unpack_from(">d", data, pos)[0], pos + 8
    if t == TAG_BYTE_ARRAY:
        (n,) = struct.unpack_from(">i", data, pos)
        pos += 4
        return list(struct.unpack_from(f">{n}b", data, pos)), pos + n
    if t == TAG_STRING:
        return _read_string(data, pos)
    if t == TAG_LIST:
        et, n = struct.unpack_from(">bi", data, pos)
        pos += 5
        items = []
        for _ in range(n):
            item, pos = _read_payload(data, pos, et)
            items.append(item)
        return items, pos
    if t == TAG_COMPOUND:
        result = {}
        while True:
            (ct,) = struct.unpack_from(">b", data, pos)
            pos += 1
            if ct == TAG_END:
                return result, pos
            name, pos = _read_string(data, pos)
            result[name], pos = _read_payload(data, pos, ct)
    if t == TAG_INT_ARRAY:
        (n,) = struct.unpack_from(">i", data, pos)
        pos += 4
        return list(struct.unpack_from(f">{n}i", data, pos)), pos + 4 * n
    if t == TAG_LONG_ARRAY:
        (n,) = struct.unpack_from(">i", data, pos)
        pos += 4
        return list(struct.unpack_from(f">{n}q", data, pos)), pos + 8 * n
    raise ValueError(f"Type NBT inconnu : {t}")


def load_gzip(path: str) -> dict:
    with gzip.open(path, "rb") as fh:
        data = fh.read()
    (t,) = struct.unpack_from(">b", data, 0)
    assert t == TAG_COMPOUND
    _, pos = _read_string(data, 1)
    root, _ = _read_payload(data, pos, TAG_COMPOUND)
    return root
