"""Modèles des créatures et montures régionales (étape 5), construits par gabarits paramétrés.

Même convention que modeles.py (y vers le bas, sol à y = 24, pivots relatifs au parent). Les cases UV sont
attribuées automatiquement par un atlas « étagères » : impossible de superposer deux cubes dans la texture.
Chaque gabarit (quadrupède, bipède, insecte, oiseau, poisson, méduse) décrit une famille ; les espèces
n'indiquent que leurs dimensions, leurs ajouts et leurs peintures.
"""
from __future__ import annotations

PI = 3.14159265


def _peinture(base, sombre=None, clair=None, motif="uni", variation=0.06, lueur=False, alpha=255, yeux=None, faces=None):
    p = {"base": base, "motif": motif, "variation": variation, "lueur": lueur, "alpha": alpha}
    if sombre:
        p["sombre"] = sombre
    if clair:
        p["clair"] = clair
    if yeux:
        p["yeux"] = yeux
    if faces:
        p["faces"] = faces
    return p


def _pow2(n: int) -> int:
    p = 16
    while p < n:
        p *= 2
    return p


class Modele:
    """Constructeur d'un modèle : os, cubes (UV automatiques), animations, peintures."""

    def __init__(self, largeur_texture: int = 64, echelle: float = 1.0, ombre: float = 0.5):
        self.largeur = largeur_texture
        self.echelle = echelle
        self.ombre = ombre
        self.x = 0
        self.y = 0
        self.h_ligne = 0
        self.hauteur = 0
        self.os_ = []
        self.animations = []
        self.peintures = {}

    def _uv(self, taille):
        sx, sy, sz = [int(round(t)) for t in taille]
        w, h = 2 * (sz + sx), sz + sy
        if w > self.largeur:
            raise ValueError(f"cube {taille} trop large pour une texture de {self.largeur}")
        if self.x + w > self.largeur:
            self.x = 0
            self.y += self.h_ligne
            self.h_ligne = 0
        uv = (self.x, self.y)
        self.x += w
        self.h_ligne = max(self.h_ligne, h)
        self.hauteur = max(self.hauteur, self.y + h)
        return uv

    def cube(self, origine, taille, peinture, gonflement=0.0, miroir=False):
        taille = [int(round(t)) for t in taille]
        return {"origine": [float(v) for v in origine], "taille": taille, "uv": list(self._uv(taille)), "peinture": peinture,
                "gonflement": gonflement, "miroir": miroir}

    def os(self, nom, pivot, cubes, parent="", rotation=(0.0, 0.0, 0.0)):
        self.os_.append({"nom": nom, "parent": parent, "pivot": [float(v) for v in pivot], "rotation": list(rotation), "cubes": cubes})
        return nom

    def anim(self, os_, propriete, pilote, amplitude, frequence=1.0, phase=0.0, valeur=0, base=0.0):
        self.animations.append({"os": os_, "propriete": propriete, "pilote": pilote, "amplitude": amplitude, "frequence": frequence,
                                "phase": phase, "valeur": valeur, "base": base})

    def peint(self, nom, base, **kw):
        self.peintures[nom] = _peinture(base, **kw)

    def build(self):
        return {"texture": (self.largeur, _pow2(self.hauteur)), "echelle": self.echelle, "ombre": self.ombre,
                "os": self.os_, "animations": self.animations, "peintures": self.peintures}


# ----------------------------------------------------------------------------- gabarits

def quadrupede(m: Modele, corps, tete, patte, *, cou=None, queue=None, tete_decalage=None, ecart_pattes=None, amplitude=0.8,
               peau="peau", peau_tete=None, peau_pattes=None, peau_queue=None, regard=True):
    """Corps sur quatre pattes ; tête à l'avant (−z). Retourne les noms d'os utiles."""
    bw, bh, bl = corps
    tw, th, tl = tete
    pw, ph = patte
    peau_tete = peau_tete or peau
    peau_pattes = peau_pattes or peau
    peau_queue = peau_queue or peau
    m.os("corps", (0, 24 - ph, 0), [m.cube((-bw / 2, -bh, -bl / 2), (bw, bh, bl), peau)])
    parent_tete = "corps"
    pivot_tete = (0, -bh + 2, -bl / 2)
    if cou:
        cw, ch, cl = cou
        m.os("cou", (0, -bh + 2, -bl / 2 + 1), [m.cube((-cw / 2, -ch, -cl), (cw, ch, cl), peau)], parent="corps", rotation=(0.6, 0, 0))
        parent_tete = "cou"
        pivot_tete = (0, -ch, -cl / 2)
    dec = tete_decalage or (-tw / 2, -th + 2, -tl)
    m.os("tete", pivot_tete, [m.cube(dec, (tw, th, tl), peau_tete)], parent=parent_tete)
    if regard:
        m.anim("tete", "ry", "regard_yaw", 0.6)
        m.anim("tete", "rx", "regard_pitch", 0.5)
    ex, ez = ecart_pattes or (bw / 2 - pw / 2, bl / 2 - pw / 2)
    for nom, sx, sz, phase in (("patte_avg", 1, -1, 0.0), ("patte_avd", -1, -1, PI), ("patte_arg", 1, 1, PI), ("patte_ard", -1, 1, 0.0)):
        m.os(nom, (sx * ex, 0, sz * ez), [m.cube((-pw / 2, 0, -pw / 2), (pw, ph, pw), peau_pattes, miroir=sx < 0)], parent="corps")
        m.anim(nom, "rx", "marche", amplitude, 1.0, phase)
    if queue:
        ql = queue
        m.os("queue", (0, -bh + 1.5, bl / 2), [m.cube((-1, -1, 0), (2, 2, ql), peau_queue)], parent="corps", rotation=(-0.4, 0, 0))
        m.anim("queue", "ry", "temps", 0.25, 0.12)
    return m


def bipede(m: Modele, corps, tete, bras, jambe, *, peau="peau", peau_tete=None, peau_bras=None, peau_jambes=None, amplitude=0.7,
           bras_leves=True, ecart_jambes=None):
    """Humanoïde : les bras se lèvent quand l'état d'animation vaut 1 (préparation d'un coup) et retombent à 2."""
    bw, bh, bl = corps
    tw, th, tl = tete
    aw, ah = bras
    jw, jh = jambe
    peau_tete = peau_tete or peau
    peau_bras = peau_bras or peau
    peau_jambes = peau_jambes or peau
    m.os("corps", (0, 24 - jh, 0), [m.cube((-bw / 2, -bh, -bl / 2), (bw, bh, bl), peau)])
    m.os("tete", (0, -bh, 0), [m.cube((-tw / 2, -th, -tl / 2), (tw, th, tl), peau_tete)], parent="corps")
    m.anim("tete", "ry", "regard_yaw", 1.0)
    m.anim("tete", "rx", "regard_pitch", 1.0)
    for nom, sx, phase in (("bras_g", 1, PI), ("bras_d", -1, 0.0)):
        m.os(nom, (sx * (bw / 2 + aw / 2), -bh + 1, 0), [m.cube((-aw / 2, -1, -aw / 2), (aw, ah, aw), peau_bras, miroir=sx < 0)], parent="corps")
        m.anim(nom, "rx", "marche", amplitude, 0.6, phase)
        if bras_leves:
            m.anim(nom, "rx", "etat", -2.7, valeur=1)
            m.anim(nom, "rz", "etat", -0.35 * sx, valeur=1)
            m.anim(nom, "rx", "etat", 0.9, valeur=2)
    ej = ecart_jambes or bw / 4
    for nom, sx, phase in (("jambe_g", 1, 0.0), ("jambe_d", -1, PI)):
        m.os(nom, (sx * ej, 0, 0), [m.cube((-jw / 2, 0, -jw / 2), (jw, jh, jw), peau_jambes, miroir=sx < 0)], parent="corps")
        m.anim(nom, "rx", "marche", amplitude, 0.6, phase)
    return m


def insecte(m: Modele, corps, tete, patte, *, pattes=6, peau="peau", peau_tete=None, peau_pattes=None, amplitude=0.6, hauteur_corps=None):
    """Corps bas sur plusieurs pattes fines ; la tête est à l'avant."""
    bw, bh, bl = corps
    pw, ph = patte
    peau_tete = peau_tete or peau
    peau_pattes = peau_pattes or peau
    y_corps = 24 - ph if hauteur_corps is None else hauteur_corps
    m.os("corps", (0, y_corps, 0), [m.cube((-bw / 2, -bh, -bl / 2), (bw, bh, bl), peau)])
    if tete:
        tw, th, tl = tete
        m.os("tete", (0, -bh / 2, -bl / 2), [m.cube((-tw / 2, -th / 2, -tl), (tw, th, tl), peau_tete)], parent="corps")
        m.anim("tete", "ry", "regard_yaw", 0.4)
    n = pattes // 2
    for i in range(n):
        z = -bl / 2 + (i + 0.5) * bl / n
        for nom, sx in ((f"patte_g{i}", 1), (f"patte_d{i}", -1)):
            m.os(nom, (sx * bw / 2, -bh / 2 + 1, z), [m.cube((0 if sx > 0 else -pw, 0, -pw / 2), (pw, ph, pw), peau_pattes)], parent="corps",
                 rotation=(0, 0, -sx * 0.35))
            m.anim(nom, "rx", "marche", amplitude, 1.3, PI * ((i + (0 if sx > 0 else 1)) % 2))
    return m


def oiseau(m: Modele, corps, tete, aile, queue, patte, *, peau="peau", peau_tete=None, peau_ailes=None, bec=True, flap=0.9):
    bw, bh, bl = corps
    tw, th, tl = tete
    aw, al = aile
    pw, ph = patte
    peau_tete = peau_tete or peau
    peau_ailes = peau_ailes or peau
    m.os("corps", (0, 24 - ph - bh / 2, 0), [m.cube((-bw / 2, -bh / 2, -bl / 2), (bw, bh, bl), peau)])
    m.anim("corps", "y", "vol", 0.6, 0.6)
    m.anim("corps", "rx", "vitesse_verticale", -0.4)
    cubes_tete = [m.cube((-tw / 2, -th, -tl / 2), (tw, th, tl), peau_tete)]
    if bec:
        cubes_tete.append(m.cube((-0.5, -th / 2, -tl / 2 - 2), (1, 1, 2), "bec"))
    m.os("tete", (0, -bh / 2, -bl / 2 + 1), cubes_tete, parent="corps")
    m.anim("tete", "ry", "regard_yaw", 0.6)
    for nom, sx in (("aile_g", 1), ("aile_d", -1)):
        m.os(nom, (sx * bw / 2, -bh / 2 + 0.5, 0), [m.cube((0 if sx > 0 else -aw, 0, -al / 2), (aw, 1, al), peau_ailes, miroir=sx < 0)], parent="corps")
        m.anim(nom, "rz", "vol", -sx * flap, 0.9, 0.0, base=-sx * 0.15)
    m.os("queue", (0, 0, bl / 2), [m.cube((-queue[0] / 2, -0.5, 0), (queue[0], 1, queue[1]), peau)], parent="corps", rotation=(0.3, 0, 0))
    for nom, sx in (("patte_g", 1), ("patte_d", -1)):
        m.os(nom, (sx * bw / 4, bh / 2, 0), [m.cube((-pw / 2, 0, -pw / 2), (pw, ph, pw), "bec")], parent="corps")
        m.anim(nom, "rx", "vol", 0.6, 0.0, 0.0)
    return m


def poisson(m: Modele, corps, queue, *, dorsale=True, peau="peau", peau_queue=None):
    bw, bh, bl = corps
    qh, ql = queue
    peau_queue = peau_queue or peau
    m.os("corps", (0, 24 - bh / 2 - 2, 0), [m.cube((-bw / 2, -bh / 2, -bl / 2), (bw, bh, bl), peau)])
    m.anim("corps", "y", "temps", 0.4, 0.1)
    m.os("queue", (0, 0, bl / 2), [m.cube((-0.5, -qh / 2, 0), (1, qh, ql), peau_queue)], parent="corps")
    m.anim("queue", "ry", "temps", 0.5, 0.35)
    m.anim("queue", "ry", "marche", 0.4, 1.0)
    if dorsale:
        m.os("dorsale", (0, -bh / 2, 0), [m.cube((-0.5, -3, -bl / 4), (1, 3, bl / 2), peau_queue)], parent="corps")
    for nom, sx in (("nageoire_g", 1), ("nageoire_d", -1)):
        m.os(nom, (sx * bw / 2, 0, -bl / 4), [m.cube((0 if sx > 0 else -3, -0.5, 0), (3, 1, 2), peau_queue, miroir=sx < 0)], parent="corps")
        m.anim(nom, "ry", "temps", sx * 0.4, 0.3, PI / 2)
    return m


# ----------------------------------------------------------------------------- espèces

def crabe_de_nacre():
    m = Modele(64, 1.0, 0.4)
    insecte(m, (10, 4, 8), None, (1, 4), pattes=6, peau="carapace", peau_pattes="pattes")
    for nom, sx in (("pince_g", 1), ("pince_d", -1)):
        m.os(nom, (sx * 4, -1, -4), [m.cube((-1.5 if sx > 0 else -1.5, -1, -4), (3, 2, 4), "pattes", miroir=sx < 0)], parent="corps", rotation=(0, sx * 0.4, 0))
        m.anim(nom, "rx", "marche", 0.3, 1.3, 0.0 if sx > 0 else PI)
        m.anim(nom, "ry", "temps", sx * 0.15, 0.08)
    m.os("yeux", (0, -4, -4), [m.cube((-3, -2, -1), (1, 2, 1), "lueur"), m.cube((2, -2, -1), (1, 2, 1), "lueur")], parent="corps")
    m.peint("carapace", "E8D6E4", sombre="B98FB4", clair="FFF2FA", motif="taches", variation=0.05)
    m.peint("pattes", "C9A3C4", sombre="8F6A8C", motif="uni", variation=0.08)
    m.peint("lueur", "FFF6D2", clair="FFFFFF", lueur=True, variation=0.02)
    return m.build()


def meduse_celeste():
    m = Modele(64, 1.0, 0.5)
    m.os("cloche", (0, 10, 0), [m.cube((-4, -5, -4), (8, 6, 8), "cloche"), m.cube((-3, -7, -3), (6, 2, 6), "cloche")])
    m.anim("cloche", "y", "temps", 1.2, 0.08)
    m.anim("cloche", "echelle", "temps", 0.06, 0.16)
    m.os("coeur", (0, -2, 0), [m.cube((-2, -2, -2), (4, 3, 4), "lueur")], parent="cloche")
    for i in range(6):
        a = i * 2 * PI / 6
        import math
        x, z = 3 * math.cos(a), 3 * math.sin(a)
        m.os(f"tentacule_{i}", (x, 1, z), [m.cube((-0.5, 0, -0.5), (1, 9, 1), "tentacule")], parent="cloche")
        m.anim(f"tentacule_{i}", "rx", "temps", 0.25, 0.13, a)
        m.anim(f"tentacule_{i}", "rz", "temps", 0.25, 0.11, a + 1.0)
    m.peint("cloche", "9FD3F0", sombre="5FA6D2", clair="E6F7FF", motif="uni", variation=0.05, alpha=255)
    m.peint("tentacule", "C6E8F8", sombre="86B9DA", variation=0.1)
    m.peint("lueur", "FFF1B8", clair="FFFFFF", lueur=True, variation=0.02)
    return m.build()


def poisson_lanterne():
    m = Modele(64, 1.0, 0.3)
    poisson(m, (3, 5, 8), (4, 4), peau="ecailles", peau_queue="nageoires")
    m.os("tige", (0, -2.5, -3), [m.cube((-0.5, -4, -1), (1, 4, 1), "nageoires"), m.cube((-1, -5.5, -2), (2, 2, 2), "lueur")], parent="corps", rotation=(-0.5, 0, 0))
    m.anim("tige", "rx", "temps", 0.15, 0.1)
    m.peint("ecailles", "4F7FB8", sombre="2E5283", clair="A9CBEE", motif="ecailles", variation=0.06,
            yeux={"face": "front", "positions": [[0, 1], [2, 1]], "couleur": "FFFFFF"})
    m.peint("nageoires", "7FA9D6", sombre="4F7FB8", variation=0.1)
    m.peint("lueur", "FFF3B0", clair="FFFFFF", lueur=True, variation=0.02)
    return m.build()


def gardien_des_epaves():
    m = Modele(128, 1.0, 0.7)
    bipede(m, (12, 14, 7), (8, 8, 8), (4, 14), (5, 12), peau="bois", peau_tete="casque", peau_bras="bois", peau_jambes="algues")
    m.os("epaulettes", (0, -14, 0), [m.cube((-8, -1, -4), (4, 3, 8), "casque"), m.cube((4, -1, -4), (4, 3, 8), "casque")], parent="corps")
    m.os("algues", (0, 0, 0), [m.cube((-6, -4, 3.5), (12, 6, 1), "algues")], parent="corps")
    m.anim("corps", "y", "temps", 0.3, 0.05)
    m.peint("bois", "5B4A3A", sombre="3A2E22", clair="7D6A55", motif="bois", variation=0.08)
    m.peint("casque", "7F8C8D", sombre="4F5B5C", clair="B8C4C4", motif="pierre", variation=0.06,
            yeux={"face": "front", "positions": [[2, 3], [5, 3]], "couleur": "7FE6FF"})
    m.peint("algues", "3F7A58", sombre="2A5239", clair="6FA07A", motif="taches", variation=0.1)
    return m.build()


def tortue_des_lagons():
    m = Modele(128, 1.0, 0.8)
    quadrupede(m, (14, 6, 18), (6, 5, 7), (4, 5), tete_decalage=(-3, -2.5, -7), queue=4, peau="peau", peau_tete="peau", amplitude=0.5)
    m.os("carapace", (0, -6, 0), [m.cube((-8, -5, -10), (16, 5, 20), "carapace"), m.cube((-6, -7, -8), (12, 2, 16), "carapace_haut")], parent="corps")
    m.os("selle", (0, -13, 0), [m.cube((-4, -1, -4), (8, 1, 8), "selle")], parent="corps")
    m.anim("corps", "rz", "marche", 0.08, 1.0)
    m.peint("peau", "7FB58A", sombre="4E7F5A", clair="B4DDB9", motif="ecailles", variation=0.07,
            yeux={"face": "front", "positions": [[1, 1], [4, 1]], "couleur": "1C2B2E"})
    m.peint("carapace", "3F6F7F", sombre="2A4B57", clair="6FA3B1", motif="ecailles", variation=0.08)
    m.peint("carapace_haut", "5F93A3", sombre="3F6F7F", clair="A9D4DE", motif="taches", variation=0.06)
    m.peint("selle", "8B5A2B", sombre="5E3A18", variation=0.08)
    return m.build()


def geant_des_jardins():
    m = Modele(256, 1.0, 1.4)
    bipede(m, (24, 30, 12), (14, 14, 14), (8, 30), (10, 30), peau="ecorce", peau_tete="ecorce", peau_bras="ecorce", peau_jambes="racines", amplitude=0.35,
           bras_leves=False, ecart_jambes=6)
    m.os("mousse", (0, -30, 0), [m.cube((-13, -2, -7), (26, 3, 14), "mousse"), m.cube((-16, 0, -5), (6, 4, 10), "mousse"), m.cube((10, 0, -5), (6, 4, 10), "mousse")], parent="corps")
    m.os("fleurs", (0, -14, 0), [m.cube((-3, -4, -3), (2, 4, 2), "fleur"), m.cube((2, -5, 1), (2, 5, 2), "fleur"), m.cube((-1, -3, 4), (2, 3, 2), "fleur")], parent="tete")
    m.anim("corps", "y", "marche", 0.6, 2.0)
    m.anim("bras_g", "rx", "temps", 0.05, 0.03)
    m.anim("bras_d", "rx", "temps", 0.05, 0.03, 1.5)
    m.peint("ecorce", "6B5540", sombre="443325", clair="8E7760", motif="bois", variation=0.07,
            yeux={"face": "front", "positions": [[4, 6], [9, 6]], "couleur": "DFF7B0"})
    m.peint("racines", "594331", sombre="3A2B1E", clair="7A6148", motif="bois", variation=0.08)
    m.peint("mousse", "5F9A4E", sombre="3E6B33", clair="8FC779", motif="fourrure", variation=0.1)
    m.peint("fleur", "F2A5C8", sombre="C9719F", clair="FFE1F0", variation=0.08)
    return m.build()


def libellule_de_rosee():
    m = Modele(64, 1.0, 0.3)
    m.os("corps", (0, 14, 0), [m.cube((-1, -1, -5), (2, 2, 10), "corps")])
    m.anim("corps", "y", "temps", 0.6, 0.2)
    m.os("tete", (0, 0, -5), [m.cube((-1.5, -1.5, -2), (3, 3, 2), "tete")], parent="corps")
    m.os("abdomen", (0, 0, 5), [m.cube((-0.5, -0.5, 0), (1, 1, 6), "lueur")], parent="corps")
    m.anim("abdomen", "rx", "temps", 0.1, 0.15)
    for nom, sx, z in (("aile_avg", 1, -2), ("aile_avd", -1, -2), ("aile_arg", 1, 2), ("aile_ard", -1, 2)):
        m.os(nom, (sx * 1, -1, z), [m.cube((0 if sx > 0 else -8, 0, -1.5), (8, 1, 3), "aile", miroir=sx < 0)], parent="corps")
        m.anim(nom, "rz", "temps", -sx * 0.5, 1.6, 0.0 if z < 0 else 0.8)
    m.peint("corps", "4FB8A8", sombre="2F7F72", clair="A6E8DD", variation=0.08)
    m.peint("tete", "2F7F72", sombre="1F5A50", variation=0.08, yeux={"face": "front", "positions": [[0, 0], [2, 0]], "couleur": "F4FFB0"})
    m.peint("aile", "DDF6FF", clair="FFFFFF", sombre="A9D8EE", variation=0.03, alpha=255)
    m.peint("lueur", "DFFFB0", clair="FFFFFF", lueur=True, variation=0.02)
    return m.build()


def escargot_des_racines():
    m = Modele(64, 1.0, 0.4)
    m.os("corps", (0, 24, 0), [m.cube((-2, -3, -6), (4, 3, 12), "chair")])
    m.anim("corps", "echelle", "marche", 0.04, 1.0)
    m.os("tete", (0, -3, -6), [m.cube((-1.5, -2, -2), (3, 2, 3), "chair"), m.cube((-1.5, -5, -1), (1, 3, 1), "chair"), m.cube((0.5, -5, -1), (1, 3, 1), "chair")], parent="corps")
    m.anim("tete", "ry", "regard_yaw", 0.4)
    m.os("coquille", (0, -3, 2), [m.cube((-4, -8, -4), (8, 8, 8), "coquille"), m.cube((-3, -9, -3), (6, 1, 6), "coquille_clair")], parent="corps", rotation=(0.15, 0, 0))
    m.peint("chair", "A48B6E", sombre="6F5A44", clair="D4C0A5", variation=0.08, yeux={"face": "front", "positions": [[0, 0], [2, 0]], "couleur": "2A1F14"})
    m.peint("coquille", "7A5A3C", sombre="4E3824", clair="A98762", motif="rayures", variation=0.08)
    m.peint("coquille_clair", "C9A77E", sombre="A98762", variation=0.06)
    return m.build()


def rodeur_des_racines():
    m = Modele(64, 1.0, 0.6)
    bipede(m, (8, 12, 5), (7, 7, 7), (3, 14), (3, 12), peau="ecorce", peau_tete="ecorce", peau_bras="ronces", peau_jambes="ronces", amplitude=0.9, ecart_jambes=2.5)
    m.os("ronces", (0, -12, 0), [m.cube((-5, -2, -1), (2, 4, 2), "ronces"), m.cube((3, -3, -1), (2, 5, 2), "ronces")], parent="corps")
    m.anim("corps", "rx", "constante", 0.25)
    m.peint("ecorce", "4A3B2C", sombre="2E2318", clair="6B5742", motif="bois", variation=0.09,
            yeux={"face": "front", "positions": [[1, 3], [5, 3]], "couleur": "FF8A3C"})
    m.peint("ronces", "35502F", sombre="21331E", clair="5A7F4F", motif="taches", variation=0.1)
    return m.build()


def scarabee_de_seve():
    m = Modele(128, 1.0, 0.8)
    insecte(m, (14, 8, 20), (8, 6, 6), (2, 6), pattes=6, peau="carapace", peau_tete="tete", peau_pattes="pattes", amplitude=0.7)
    m.os("elytres", (0, -8, 0), [m.cube((-7, -1, -9), (7, 1, 18), "elytre"), m.cube((0, -1, -9), (7, 1, 18), "elytre", miroir=True)], parent="corps")
    m.os("mandibules", (0, 0, -6), [m.cube((-4, -1, -4), (1, 2, 4), "pattes"), m.cube((3, -1, -4), (1, 2, 4), "pattes")], parent="tete")
    m.os("selle", (0, -9, 0), [m.cube((-4, -1, -4), (8, 1, 8), "selle")], parent="corps")
    m.anim("elytres", "rx", "monte", 0.0)
    m.peint("carapace", "4E8A3A", sombre="2F5A22", clair="86C46A", motif="uni", variation=0.07)
    m.peint("elytre", "7FC25A", sombre="4E8A3A", clair="DFF7A0", motif="rayures", variation=0.06, lueur=False)
    m.peint("tete", "35602A", sombre="22401A", yeux={"face": "front", "positions": [[1, 1], [6, 1]], "couleur": "F6FFB0"})
    m.peint("pattes", "2F4A22", sombre="1C2E14", variation=0.1)
    m.peint("selle", "8B5A2B", sombre="5E3A18", variation=0.08)
    return m.build()


def fennec_des_dunes():
    m = Modele(64, 1.0, 0.35)
    quadrupede(m, (5, 5, 10), (5, 4, 5), (1, 5), queue=7, tete_decalage=(-2.5, -3, -5), peau="pelage", peau_tete="pelage", amplitude=1.1)
    m.os("oreilles", (0, -3, -3), [m.cube((-3, -5, 0), (2, 5, 1), "pelage"), m.cube((1, -5, 0), (2, 5, 1), "pelage")], parent="tete")
    m.os("museau", (0, -1, -5), [m.cube((-1, 0, -2), (2, 1.5, 2), "museau")], parent="tete")
    m.peint("pelage", "E9CFA3", sombre="C4A574", clair="FFF2DC", motif="fourrure", variation=0.06,
            yeux={"face": "front", "positions": [[1, 1], [3, 1]], "couleur": "2B1E12"})
    m.peint("museau", "2B1E12", clair="4A3521", variation=0.05)
    return m.build()


def scorpion_des_heures():
    m = Modele(64, 1.0, 0.5)
    insecte(m, (8, 4, 12), None, (1, 4), pattes=8, peau="carapace", peau_pattes="pattes", amplitude=0.7)
    m.os("queue1", (0, -3, 6), [m.cube((-1.5, -1.5, 0), (3, 3, 4), "carapace")], parent="corps", rotation=(-0.9, 0, 0))
    m.os("queue2", (0, 0, 4), [m.cube((-1.2, -1.2, 0), (2.4, 2.4, 4), "carapace")], parent="queue1", rotation=(-0.7, 0, 0))
    m.os("dard", (0, 0, 4), [m.cube((-1, -1, 0), (2, 2, 3), "dard")], parent="queue2", rotation=(-0.9, 0, 0))
    m.anim("queue1", "rx", "etat", -0.5, valeur=1)
    m.anim("queue2", "rx", "etat", -0.6, valeur=1)
    m.anim("queue1", "rx", "etat", 1.3, valeur=2)
    m.anim("queue1", "rx", "temps", 0.08, 0.1)
    for nom, sx in (("pince_g", 1), ("pince_d", -1)):
        m.os(nom, (sx * 3, -2, -6), [m.cube((-1, -1, -5), (2, 2, 5), "pattes", miroir=sx < 0)], parent="corps", rotation=(0, sx * 0.5, 0))
        m.anim(nom, "ry", "etat", -sx * 0.5, valeur=1)
    m.peint("carapace", "C98B3C", sombre="8E5C22", clair="EEC27E", motif="rayures", variation=0.07)
    m.peint("pattes", "8E5C22", sombre="5E3B14", variation=0.1)
    m.peint("dard", "3A2A1A", clair="7A5A3A", variation=0.06)
    return m.build()


def lezard_cadran():
    m = Modele(64, 1.0, 0.4)
    quadrupede(m, (6, 3, 12), (5, 3, 5), (2, 3), queue=10, tete_decalage=(-2.5, -1.5, -5), peau="ecailles", amplitude=0.9)
    m.os("crete", (0, -3, 0), [m.cube((-0.5, -3, -4), (1, 3, 8), "crete")], parent="corps")
    m.anim("tete", "rx", "etat", -0.6, valeur=1)
    m.anim("crete", "echelle", "etat", 0.3, valeur=1)
    m.peint("ecailles", "C9B07E", sombre="8E7A4F", clair="F2E3B8", motif="ecailles", variation=0.07,
            yeux={"face": "front", "positions": [[1, 0], [3, 0]], "couleur": "1F2A4D"})
    m.peint("crete", "E07A3C", sombre="A8521F", clair="FFB07A", motif="rayures", variation=0.08)
    return m.build()


def sentinelle_de_sable():
    m = Modele(64, 1.0, 0.6)
    bipede(m, (10, 12, 6), (8, 8, 8), (3, 12), (4, 10), peau="gres", peau_tete="cadran", peau_bras="gres", peau_jambes="gres", amplitude=0.5)
    m.os("aiguille", (0, -4, -4.5), [m.cube((-0.5, -3, -0.5), (1, 3, 1), "aiguille")], parent="tete")
    m.anim("aiguille", "rz", "temps", 3.1, 0.02)
    m.anim("corps", "y", "temps", 0.4, 0.07)
    m.peint("gres", "D9C08C", sombre="A68E5C", clair="F2E3B8", motif="pierre", variation=0.07)
    m.peint("cadran", "F2E3B8", sombre="A68E5C", clair="FFFFFF", motif="uni", variation=0.03,
            yeux={"face": "front", "positions": [[2, 2], [5, 2]], "couleur": "1F2A4D"})
    m.peint("aiguille", "3A2A1A", clair="7A5A3A", variation=0.05)
    return m.build()


def gazelle_d_opale():
    m = Modele(128, 1.0, 0.7)
    quadrupede(m, (8, 8, 16), (5, 5, 7), (2, 12), cou=(4, 8, 4), queue=4, tete_decalage=(-2.5, -3, -6), peau="pelage", peau_pattes="pattes", amplitude=1.0)
    m.os("cornes", (0, -3, -2), [m.cube((-2.5, -6, -0.5), (1, 6, 1), "corne"), m.cube((1.5, -6, -0.5), (1, 6, 1), "corne")], parent="tete", rotation=(-0.3, 0, 0))
    m.os("selle", (0, -8, 1), [m.cube((-3.5, -1, -3), (7, 1, 6), "selle")], parent="corps")
    m.peint("pelage", "E3CDA6", sombre="B89A6C", clair="FFF6E6", motif="fourrure", variation=0.06, faces={"bottom": "FFF6E6"},
            yeux={"face": "front", "positions": [[1, 1], [3, 1]], "couleur": "2B1E12"})
    m.peint("pattes", "B89A6C", sombre="7F6744", variation=0.08)
    m.peint("corne", "DDE8F0", sombre="9FB6C8", clair="FFFFFF", motif="cristal", variation=0.05)
    m.peint("selle", "8B5A2B", sombre="5E3A18", variation=0.08)
    return m.build()


def bouquetin_de_verre():
    m = Modele(128, 1.0, 0.7)
    quadrupede(m, (9, 8, 14), (6, 6, 7), (3, 10), queue=3, tete_decalage=(-3, -4, -7), peau="pelage", peau_tete="pelage", peau_pattes="pattes", amplitude=0.9)
    m.os("cornes", (0, -4, -3), [m.cube((-3.5, -7, -1), (2, 7, 2), "corne"), m.cube((1.5, -7, -1), (2, 7, 2), "corne"),
                                m.cube((-4.5, -9, 1), (2, 3, 2), "corne"), m.cube((2.5, -9, 1), (2, 3, 2), "corne")], parent="tete", rotation=(-0.6, 0, 0))
    m.os("barbe", (0, 1, -5), [m.cube((-1, 0, -1), (2, 3, 2), "pattes")], parent="tete")
    m.peint("pelage", "C9D3DE", sombre="8E9BA8", clair="F2F6FA", motif="fourrure", variation=0.06,
            yeux={"face": "front", "positions": [[1, 1], [4, 1]], "couleur": "2B2E3A"})
    m.peint("pattes", "6F7A86", sombre="4A535C", variation=0.08)
    m.peint("corne", "BFE6F2", sombre="7FB7CC", clair="FFFFFF", motif="cristal", variation=0.05)
    return m.build()


def papillon_de_cristal():
    m = Modele(64, 1.0, 0.25)
    m.os("corps", (0, 14, 0), [m.cube((-1, -1, -3), (2, 2, 6), "corps"), m.cube((-1.5, -3, -3), (1, 3, 1), "corps"), m.cube((0.5, -3, -3), (1, 3, 1), "corps")])
    m.anim("corps", "y", "temps", 0.8, 0.18)
    for nom, sx in (("aile_g", 1), ("aile_d", -1)):
        m.os(nom, (sx * 1, 0, 0), [m.cube((0 if sx > 0 else -7, 0, -3), (7, 1, 6), "aile", miroir=sx < 0)], parent="corps")
        m.anim(nom, "rz", "temps", -sx * 0.9, 0.9, 0.0, base=-sx * 0.3)
    m.peint("corps", "6F7A86", sombre="4A535C", variation=0.08)
    m.peint("aile", "C8F0FF", sombre="7FC8EE", clair="FFFFFF", motif="cristal", variation=0.05, lueur=True)
    return m.build()


def colosse_de_cristal():
    m = Modele(128, 1.0, 1.0)
    bipede(m, (16, 16, 10), (10, 10, 10), (6, 18), (6, 14), peau="roche", peau_tete="cristal", peau_bras="roche", peau_jambes="roche", amplitude=0.5, ecart_jambes=4.5)
    m.os("cristaux", (0, -16, 0), [m.cube((-9, -6, -3), (4, 7, 4), "cristal"), m.cube((5, -5, -2), (4, 6, 4), "cristal"), m.cube((-2, -4, 3), (4, 5, 3), "cristal")], parent="corps")
    m.os("poings", (0, 0, 0), [], parent="corps")
    m.anim("corps", "y", "marche", 0.5, 1.2)
    m.peint("roche", "6B7A8A", sombre="46525F", clair="98A8B8", motif="pierre", variation=0.07)
    m.peint("cristal", "AEE6F8", sombre="6FB6D6", clair="FFFFFF", motif="cristal", variation=0.05, lueur=True,
            yeux={"face": "front", "positions": [[3, 4], [6, 4]], "couleur": "2B2E3A"})
    return m.build()


def echo_resonnant():
    m = Modele(64, 1.0, 0.5)
    m.os("corps", (0, 12, 0), [m.cube((-3, -5, -3), (6, 10, 6), "cristal"), m.cube((-2, -8, -2), (4, 3, 4), "cristal"), m.cube((-2, 5, -2), (4, 3, 4), "cristal")])
    m.anim("corps", "y", "temps", 1.0, 0.1)
    m.anim("corps", "ry", "temps", 0.4, 0.05)
    m.anim("corps", "echelle", "etat", 0.15, valeur=1)
    import math
    for i in range(4):
        a = i * PI / 2
        m.os(f"eclat_{i}", (0, 0, 0), [m.cube((5 * math.cos(a) - 1, -2, 5 * math.sin(a) - 1), (2, 4, 2), "eclat")], parent="corps")
        m.anim(f"eclat_{i}", "ry", "temps", 3.14, 0.07, a)
        m.anim(f"eclat_{i}", "y", "temps", 1.5, 0.15, a)
    m.peint("cristal", "B8E8F6", sombre="7FC8E6", clair="FFFFFF", motif="cristal", variation=0.05, lueur=True,
            yeux={"face": "front", "positions": [[1, 3], [4, 3]], "couleur": "2B2E3A"})
    m.peint("eclat", "DFF6FF", clair="FFFFFF", sombre="9FD6EE", motif="cristal", variation=0.04, lueur=True)
    return m.build()


def griffon_de_verre():
    m = Modele(128, 1.0, 0.9)
    quadrupede(m, (10, 9, 18), (7, 7, 8), (3, 10), queue=10, tete_decalage=(-3.5, -4, -8), peau="pelage", peau_tete="plumes", peau_pattes="pattes", peau_queue="pelage", amplitude=0.8)
    m.os("bec", (0, -1, -8), [m.cube((-1.5, 0, -3), (3, 2, 3), "bec")], parent="tete")
    for nom, sx in (("aile_g", 1), ("aile_d", -1)):
        m.os(nom, (sx * 5, -9, -3), [m.cube((0 if sx > 0 else -14, 0, -4), (14, 1, 10), "plumes", miroir=sx < 0)], parent="corps")
        m.anim(nom, "rz", "vol", -sx * 0.7, 0.25, 0.0, base=-sx * 0.2)
        m.anim(nom, "rz", "temps", -sx * 0.05, 0.05)
    m.os("selle", (0, -9, 2), [m.cube((-4, -1, -3), (8, 1, 6), "selle")], parent="corps")
    m.anim("corps", "rx", "vitesse_verticale", -0.5)
    m.peint("pelage", "D9C7A8", sombre="A8946F", clair="FFF2DC", motif="fourrure", variation=0.06)
    m.peint("plumes", "E6F2F8", sombre="A9C8DA", clair="FFFFFF", motif="plumes", variation=0.05,
            yeux={"face": "front", "positions": [[1, 2], [5, 2]], "couleur": "C98B3C"})
    m.peint("pattes", "A8946F", sombre="6F5F44", variation=0.08)
    m.peint("bec", "E0B04A", sombre="A8762A", variation=0.06)
    m.peint("selle", "8B5A2B", sombre="5E3A18", variation=0.08)
    return m.build()


def colombe_astrale():
    m = Modele(64, 1.0, 0.3)
    oiseau(m, (4, 4, 7), (3, 3, 3), (6, 5), (3, 4), (1, 2), peau="plumes", peau_ailes="ailes")
    m.os("etoile", (0, -2, 3.5), [m.cube((-1, -1, 0), (2, 2, 1), "lueur")], parent="corps")
    m.peint("plumes", "F2F4FA", sombre="BFC6D8", clair="FFFFFF", motif="plumes", variation=0.04,
            yeux={"face": "front", "positions": [[0, 1], [2, 1]], "couleur": "1F2A4D"})
    m.peint("ailes", "DDE4F2", sombre="A9B5CF", clair="FFFFFF", motif="plumes", variation=0.05)
    m.peint("bec", "E0B04A", sombre="A8762A", variation=0.06)
    m.peint("lueur", "FFF3C8", clair="FFFFFF", lueur=True, variation=0.02)
    return m.build()


def chat_de_palais():
    m = Modele(64, 1.0, 0.35)
    quadrupede(m, (4, 5, 10), (5, 4, 5), (1, 6), queue=8, tete_decalage=(-2.5, -3, -4), peau="pelage", amplitude=1.0)
    m.os("oreilles", (0, -3, -1.5), [m.cube((-2.5, -2, 0), (1, 2, 1), "pelage"), m.cube((1.5, -2, 0), (1, 2, 1), "pelage")], parent="tete")
    m.os("collier", (0, -5 + 3, -4), [m.cube((-2.5, 0, -0.5), (5, 1, 1), "collier")], parent="corps")
    m.anim("queue", "rx", "constante", -0.9)
    m.peint("pelage", "8E8FA6", sombre="5D5E73", clair="C9CAD9", motif="rayures", variation=0.06,
            yeux={"face": "front", "positions": [[1, 1], [3, 1]], "couleur": "E0B04A"})
    m.peint("collier", "C9A227", clair="FFE08A", variation=0.05)
    return m.build()


def sentinelle_de_la_couronne():
    m = Modele(128, 1.0, 0.7)
    bipede(m, (10, 14, 6), (8, 8, 8), (4, 14), (4, 12), peau="armure", peau_tete="casque", peau_bras="armure", peau_jambes="armure", amplitude=0.5)
    m.os("casque", (0, -8, 0), [m.cube((-5, -2, -5), (10, 3, 10), "casque"), m.cube((-1, -6, -1), (2, 4, 2), "plume")], parent="tete")
    m.os("bouclier", (-2, 6, 0), [m.cube((-1, -4, -4), (1, 8, 8), "bouclier")], parent="bras_g")
    m.os("cape", (0, -14, 3), [m.cube((-5, 0, 0), (10, 14, 1), "cape")], parent="corps", rotation=(0.1, 0, 0))
    m.anim("cape", "rx", "marche", 0.3, 0.6)
    m.peint("armure", "D8DDE6", sombre="9BA4B3", clair="FFFFFF", motif="pierre", variation=0.05)
    m.peint("casque", "C9A227", sombre="8E7119", clair="FFE08A", motif="uni", variation=0.05,
            yeux={"face": "front", "positions": [[2, 4], [5, 4]], "couleur": "7FE6FF"})
    m.peint("plume", "B23A48", clair="E06A78", variation=0.06)
    m.peint("bouclier", "2E3A6E", sombre="1C2447", clair="C9A227", motif="uni", variation=0.05)
    m.peint("cape", "6E2E3A", sombre="471C25", clair="A04A5A", variation=0.06)
    return m.build()


def spectre_de_la_cour():
    m = Modele(64, 1.0, 0.5)
    m.os("corps", (0, 10, 0), [m.cube((-4, -14, -2.5), (8, 14, 5), "voile")])
    m.anim("corps", "y", "temps", 1.2, 0.09)
    m.os("robe", (0, 0, 0), [m.cube((-5, 0, -3.5), (10, 10, 7), "voile_sombre")], parent="corps")
    m.anim("robe", "echelle", "temps", 0.05, 0.14)
    m.os("tete", (0, -14, 0), [m.cube((-3.5, -7, -3.5), (7, 7, 7), "visage"), m.cube((-4, -9, -4), (8, 2, 8), "couronne")], parent="corps")
    m.anim("tete", "ry", "regard_yaw", 1.0)
    for nom, sx in (("bras_g", 1), ("bras_d", -1)):
        m.os(nom, (sx * 5.5, -13, 0), [m.cube((-1.5, -1, -1.5), (3, 12, 3), "voile", miroir=sx < 0)], parent="corps")
        m.anim(nom, "rx", "temps", 0.15, 0.1, 0.0 if sx > 0 else 1.5)
        m.anim(nom, "rx", "etat", -2.6, valeur=1)
    m.peint("voile", "C9D0E8", sombre="8F99B8", clair="F2F4FA", motif="uni", variation=0.05)
    m.peint("voile_sombre", "8F99B8", sombre="5D6684", variation=0.07)
    m.peint("visage", "E6E9F2", clair="FFFFFF", sombre="B8BFD1", variation=0.03,
            yeux={"face": "front", "positions": [[1, 3], [4, 3]], "couleur": "7FE6FF"})
    m.peint("couronne", "C9A227", clair="FFE08A", sombre="8E7119", variation=0.05, lueur=True)
    return m.build()


def cerf_royal():
    m = Modele(128, 1.0, 0.8)
    quadrupede(m, (9, 9, 18), (6, 6, 8), (3, 12), cou=(5, 8, 5), queue=3, tete_decalage=(-3, -3.5, -7), peau="pelage", peau_pattes="pattes", amplitude=0.9)
    bois = []
    for sx in (1, -1):
        bois.append(m.cube((sx * 2 - 0.5, -8, -1), (1, 8, 1), "bois"))
        bois.append(m.cube((sx * 4 - 0.5, -10, -1), (1, 4, 1), "bois"))
        bois.append(m.cube((sx * 2 - 0.5 + (sx * 1), -8, -1), (sx * 2 if sx > 0 else 2, 1, 1), "bois"))
    m.os("bois", (0, -5, -2), bois, parent="tete", rotation=(-0.3, 0, 0))
    m.os("selle", (0, -9, 2), [m.cube((-4, -1, -3), (8, 1, 6), "selle")], parent="corps")
    m.peint("pelage", "F2EBDD", sombre="C9BEA7", clair="FFFFFF", motif="fourrure", variation=0.05, faces={"bottom": "FFFFFF"},
            yeux={"face": "front", "positions": [[1, 1], [4, 1]], "couleur": "2B1E12"})
    m.peint("pattes", "C9BEA7", sombre="8E8470", variation=0.08)
    m.peint("bois", "E6C76A", sombre="B8952E", clair="FFF2B0", motif="uni", variation=0.05, lueur=True)
    m.peint("selle", "6E2E3A", sombre="471C25", clair="C9A227", variation=0.06)
    return m.build()


MODELES_REGIONS = {
    "crabe_de_nacre": crabe_de_nacre(),
    "meduse_celeste": meduse_celeste(),
    "poisson_lanterne": poisson_lanterne(),
    "gardien_des_epaves": gardien_des_epaves(),
    "tortue_des_lagons": tortue_des_lagons(),
    "geant_des_jardins": geant_des_jardins(),
    "libellule_de_rosee": libellule_de_rosee(),
    "escargot_des_racines": escargot_des_racines(),
    "rodeur_des_racines": rodeur_des_racines(),
    "scarabee_de_seve": scarabee_de_seve(),
    "fennec_des_dunes": fennec_des_dunes(),
    "scorpion_des_heures": scorpion_des_heures(),
    "lezard_cadran": lezard_cadran(),
    "sentinelle_de_sable": sentinelle_de_sable(),
    "gazelle_d_opale": gazelle_d_opale(),
    "bouquetin_de_verre": bouquetin_de_verre(),
    "papillon_de_cristal": papillon_de_cristal(),
    "colosse_de_cristal": colosse_de_cristal(),
    "echo_resonnant": echo_resonnant(),
    "griffon_de_verre": griffon_de_verre(),
    "colombe_astrale": colombe_astrale(),
    "chat_de_palais": chat_de_palais(),
    "sentinelle_de_la_couronne": sentinelle_de_la_couronne(),
    "spectre_de_la_cour": spectre_de_la_cour(),
    "cerf_royal": cerf_royal(),
}
