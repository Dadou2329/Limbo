#!/usr/bin/env python3
"""Synthétise les sons originaux du mod (WAV via numpy, puis OGG via ffmpeg).

Usage : python3 tools/sounds/gen_sounds.py
Les sons sont courts, discrets et identifiables : carillons de nacre, souffle de porte, pas royal,
décret, chocs de pierre. Aucun échantillon externe n'est utilisé.
"""
from __future__ import annotations

import math
import os
import shutil
import struct
import subprocess
import sys
import tempfile
import wave

import numpy as np

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "les_limbes", "sounds")
SR = 44100


def env(n: int, attack: float, decay: float, sustain: float = 0.0, release: float = 0.0) -> np.ndarray:
    t = np.linspace(0, 1, n, endpoint=False)
    a = int(n * attack)
    d = int(n * decay)
    r = int(n * release)
    e = np.ones(n)
    if a > 0:
        e[:a] = np.linspace(0, 1, a)
    if d > 0:
        e[a:a + d] = np.linspace(1, sustain if sustain > 0 else 0, d)
    if sustain > 0:
        e[a + d:n - r] = sustain
    if r > 0:
        e[n - r:] = np.linspace(e[n - r - 1] if n - r - 1 >= 0 else sustain, 0, r)
    return e


def tone(freq: float, dur: float, vol: float = 0.5, harmonics=((1, 1.0), (2, 0.35), (3, 0.15)), vibrato: float = 0.0) -> np.ndarray:
    n = int(SR * dur)
    t = np.arange(n) / SR
    out = np.zeros(n)
    for mult, amp in harmonics:
        f = freq * mult
        if vibrato:
            f = f * (1 + vibrato * np.sin(2 * math.pi * 5.5 * t))
        out += amp * np.sin(2 * math.pi * f * t)
    return vol * out / max(1.0, sum(a for _, a in harmonics))


def noise(dur: float, vol: float = 0.3, lowpass: float = 0.0, seed: int = 1) -> np.ndarray:
    rng = np.random.default_rng(seed)
    n = int(SR * dur)
    x = rng.standard_normal(n)
    if lowpass > 0:
        k = int(max(1, lowpass))
        x = np.convolve(x, np.ones(k) / k, mode="same")
    return vol * x / (np.max(np.abs(x)) + 1e-9)


def chime(notes, dur_each: float, gap: float, vol: float = 0.4, shimmer: bool = True) -> np.ndarray:
    total = int(SR * (dur_each + gap * (len(notes) - 1) + 0.4))
    out = np.zeros(total)
    for i, f in enumerate(notes):
        start = int(SR * gap * i)
        n = int(SR * dur_each)
        s = tone(f, dur_each, vol, harmonics=((1, 1.0), (2.76, 0.3), (5.4, 0.12))) * env(n, 0.01, 0.9)
        if shimmer:
            s += tone(f * 2, dur_each, vol * 0.3, harmonics=((1, 1.0),)) * env(n, 0.05, 0.95)
        out[start:start + n] += s
    return out


def mix(*parts) -> np.ndarray:
    n = max(len(p) for p in parts)
    out = np.zeros(n)
    for p in parts:
        out[:len(p)] += p
    return out


def normalize(x: np.ndarray, peak: float = 0.85) -> np.ndarray:
    m = np.max(np.abs(x)) + 1e-9
    return x / m * peak


def save(name: str, samples: np.ndarray) -> None:
    samples = normalize(samples)
    path = os.path.join(OUT, name + ".ogg")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        wav_path = tmp.name
    with wave.open(wav_path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        data = (samples * 32767).astype("<i2").tobytes()
        w.writeframes(data)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav_path, "-c:a", "libvorbis", "-q:a", "4", "-fflags", "+bitexact", "-flags:a", "+bitexact", path], check=True)
    os.unlink(wav_path)
    print("  ", path.replace(ROOT + "/", ""))


def sweep(f0: float, f1: float, dur: float, vol: float = 0.4) -> np.ndarray:
    n = int(SR * dur)
    t = np.arange(n) / SR
    f = f0 + (f1 - f0) * (t / dur)
    phase = 2 * math.pi * np.cumsum(f) / SR
    return vol * np.sin(phase)


def note_freq(midi: float) -> float:
    return 440.0 * 2 ** ((midi - 69) / 12.0)


def pad(midis, dur: float, vol: float = 0.18) -> np.ndarray:
    """Nappe d'accord : sinus empilés avec battement léger et enveloppe lente."""
    n = int(SR * dur)
    t = np.arange(n) / SR
    out = np.zeros(n)
    for m in midis:
        f = note_freq(m)
        out += np.sin(2 * math.pi * f * t) + 0.5 * np.sin(2 * math.pi * f * 1.003 * t) + 0.25 * np.sin(2 * math.pi * 2 * f * t)
    return vol * out / max(1, len(midis)) * env(n, 0.35, 0.3, 0.75, 0.3)


def cloche(midi: float, dur: float, vol: float = 0.3) -> np.ndarray:
    """Note de cloche : fondamentale et partiels inharmoniques décroissants."""
    n = int(SR * dur)
    t = np.arange(n) / SR
    f = note_freq(midi)
    out = np.sin(2 * math.pi * f * t) + 0.5 * np.sin(2 * math.pi * f * 2.76 * t) * np.exp(-3 * t) + 0.3 * np.sin(2 * math.pi * f * 5.4 * t) * np.exp(-6 * t)
    return vol * out * np.exp(-2.2 * t) * env(n, 0.005, 0.9)


def place(piste: np.ndarray, son: np.ndarray, debut: float) -> None:
    i = int(debut * SR)
    fin = min(len(piste), i + len(son))
    if fin > i:
        piste[i:fin] += son[: fin - i]


def musique(nom: str, gamme, accords, tempo: float, graine: int, duree: float = 72.0, timbre: str = "cloche", grave: int = 0) -> None:
    """Pièce courte et bouclable : nappe d'accords, mélodie pentatonique tirée au sort (déterministe), basse légère."""
    rng = np.random.default_rng(graine)
    n = int(SR * duree)
    piste = np.zeros(n)
    battement = 60.0 / tempo
    mesure = battement * 4
    nb_mesures = int(duree / mesure)
    for k in range(nb_mesures):
        acc = accords[k % len(accords)]
        place(piste, pad([m + grave for m in acc], mesure * 1.05, 0.16), k * mesure)
        basse = tone(note_freq(acc[0] + grave - 12), mesure * 0.9, 0.12, harmonics=((1, 1.0), (2, 0.3)))
        place(piste, basse * env(len(basse), 0.05, 0.6), k * mesure)
        temps = 0.0
        while temps < mesure - 0.01:
            longueur = battement * rng.choice([0.5, 1.0, 1.0, 2.0])
            if rng.random() < 0.72:
                degre = int(rng.integers(0, len(gamme)))
                midi = gamme[degre] + 12 * int(rng.integers(0, 2))
                if timbre == "cloche":
                    son = cloche(midi, min(2.5, longueur * 1.8), 0.22)
                else:
                    corps = tone(note_freq(midi), longueur * 0.95, 0.2, harmonics=((1, 1.0), (2, 0.2), (3, 0.08)), vibrato=0.01)
                    son = corps * env(len(corps), 0.04, 0.5, 0.5, 0.3)
                place(piste, son, k * mesure + temps)
            temps += longueur
    # queue : dernier accord tenu, pour une boucle douce
    place(piste, pad([m + grave for m in accords[0]], 4.0, 0.14), duree - 4.0)
    save("musique/" + nom, piste)


def musiques() -> None:
    """Six thèmes régionaux (gammes et accords propres), joués par les biomes à la place des pistes vanilla."""
    print("Génération des musiques :")
    musique("prairies", [60, 62, 64, 67, 69], [[60, 64, 67], [65, 69, 72], [57, 60, 64], [67, 71, 74]], 66, 1)
    musique("mer", [62, 64, 65, 67, 69, 71], [[62, 65, 69], [60, 64, 67], [65, 69, 72], [57, 62, 65]], 58, 2, timbre="voix", grave=-2)
    musique("jardins", [65, 67, 69, 72, 74], [[65, 69, 72], [67, 71, 74], [62, 65, 69], [60, 65, 67]], 76, 3)
    musique("desert", [64, 65, 68, 71, 72], [[64, 68, 71], [60, 64, 67], [65, 68, 72], [59, 64, 67]], 54, 4, timbre="voix", grave=-5)
    musique("montagnes", [69, 72, 74, 76, 79], [[69, 72, 76], [65, 69, 72], [67, 71, 74], [62, 65, 69]], 62, 5)
    musique("couronne", [55, 57, 59, 62, 64], [[55, 59, 62], [50, 55, 57], [52, 55, 59], [53, 57, 60]], 50, 6, timbre="voix", grave=-7)


def main() -> None:
    if shutil.which("ffmpeg") is None:
        print("ffmpeg introuvable : impossible d'encoder les sons.", file=sys.stderr)
        sys.exit(1)
    if os.path.isdir(OUT):
        shutil.rmtree(OUT)
    print("Génération des sons :")
    musiques()
    # Porte : réveil = carillon ascendant + souffle ; passage = souffle + accord ; ambiance = bourdon doux
    save("bloc/porte_eveil", mix(chime([523.25, 659.25, 783.99, 1046.5, 1318.5], 1.2, 0.18, 0.45),
                                 noise(1.8, 0.12, 40, 3) * env(int(SR * 1.8), 0.3, 0.6)))
    save("bloc/porte_passage", mix(sweep(300, 1200, 0.5, 0.3) * env(int(SR * 0.5), 0.05, 0.9),
                                   noise(0.9, 0.25, 25, 4) * env(int(SR * 0.9), 0.02, 0.9),
                                   chime([783.99, 1174.66], 0.8, 0.12, 0.3)))
    n = int(SR * 2.5)
    t = np.arange(n) / SR
    bourdon = 0.18 * np.sin(2 * math.pi * 110 * t) * (1 + 0.3 * np.sin(2 * math.pi * 0.7 * t)) + 0.08 * np.sin(2 * math.pi * 220.5 * t)
    save("bloc/porte_ambiance", bourdon * env(n, 0.3, 0.2, 0.8, 0.4))
    # Pouvoirs
    save("pouvoir/epee", mix(sweep(900, 2400, 0.35, 0.35) * env(int(SR * 0.35), 0.02, 0.9),
                             chime([1318.5, 1760.0], 0.6, 0.08, 0.35),
                             noise(0.3, 0.15, 6, 5) * env(int(SR * 0.3), 0.01, 0.95)))
    save("pouvoir/epee_final", mix(sweep(1800, 400, 0.4, 0.45) * env(int(SR * 0.4), 0.01, 0.95),
                                   noise(0.45, 0.35, 4, 6) * env(int(SR * 0.45), 0.0, 0.9),
                                   tone(130.8, 0.5, 0.35) * env(int(SR * 0.5), 0.0, 0.9)))
    save("pouvoir/pas", mix(sweep(1500, 500, 0.25, 0.3) * env(int(SR * 0.25), 0.01, 0.9),
                            chime([987.77, 1480.0], 0.5, 0.06, 0.3, False),
                            noise(0.35, 0.2, 30, 7) * env(int(SR * 0.35), 0.02, 0.9)))
    save("pouvoir/decret", mix(tone(98.0, 0.9, 0.5, harmonics=((1, 1.0), (2, 0.5), (3, 0.3), (4, 0.2))) * env(int(SR * 0.9), 0.01, 0.9),
                               sweep(400, 120, 0.6, 0.35) * env(int(SR * 0.6), 0.0, 0.9),
                               chime([392.0, 587.33, 783.99], 0.8, 0.05, 0.3, False)))
    save("pouvoir/refus", tone(220, 0.25, 0.4, harmonics=((1, 1.0), (1.5, 0.6))) * env(int(SR * 0.25), 0.01, 0.9))
    # Pouvoirs supérieurs (étape 7)
    save("pouvoir/parade", mix(sweep(2200, 3200, 0.18, 0.4) * env(int(SR * 0.18), 0.0, 0.9), chime([2093.0, 2637.0], 0.35, 0.05, 0.3, False),
                               noise(0.2, 0.2, 5, 31) * env(int(SR * 0.2), 0.0, 0.9)))
    save("pouvoir/lance", mix(sweep(600, 1900, 0.4, 0.4) * env(int(SR * 0.4), 0.02, 0.9), noise(0.5, 0.25, 12, 32) * env(int(SR * 0.5), 0.05, 0.8),
                              tone(196.0, 0.4, 0.25) * env(int(SR * 0.4), 0.0, 0.9)))
    save("pouvoir/arc", mix(sweep(1600, 900, 0.22, 0.35) * env(int(SR * 0.22), 0.0, 0.9), noise(0.3, 0.3, 20, 33) * env(int(SR * 0.3), 0.0, 0.85),
                            chime([1318.5], 0.3, 0.05, 0.25, False)))
    save("pouvoir/garde", mix(chime([659.25, 987.77, 1318.5, 1975.5], 0.9, 0.12, 0.35), tone(164.8, 1.0, 0.3, harmonics=((1, 1.0), (2, 0.4), (3, 0.2))) * env(int(SR * 1.0), 0.1, 0.8),
                              noise(0.8, 0.1, 30, 34) * env(int(SR * 0.8), 0.2, 0.7)))
    save("pouvoir/main", mix(tone(73.4, 0.7, 0.5, harmonics=((1, 1.0), (2, 0.5), (3, 0.25))) * env(int(SR * 0.7), 0.02, 0.9), sweep(500, 160, 0.5, 0.35) * env(int(SR * 0.5), 0.0, 0.9),
                             noise(0.5, 0.3, 6, 35) * env(int(SR * 0.5), 0.0, 0.9)))
    save("pouvoir/constellation", mix(chime([1046.5, 1318.5, 1567.98, 2093.0, 2637.0, 3135.96, 3951.07], 1.4, 0.11, 0.35),
                                      sweep(3000, 1200, 1.2, 0.2) * env(int(SR * 1.2), 0.05, 0.8), noise(1.0, 0.08, 50, 36) * env(int(SR * 1.0), 0.2, 0.7)))
    save("pouvoir/avenement", mix(chime([261.63, 329.63, 392.0, 523.25, 659.25, 783.99, 1046.5], 2.2, 0.2, 0.4),
                                  tone(130.8, 2.4, 0.4, harmonics=((1, 1.0), (2, 0.6), (3, 0.3), (4, 0.15))) * env(int(SR * 2.4), 0.3, 0.6, 0.7, 0.3),
                                  noise(2.0, 0.12, 35, 37) * env(int(SR * 2.0), 0.4, 0.6)))
    save("creature/baleine_chant", mix(tone(110.0, 2.0, 0.45, harmonics=((1, 1.0), (2, 0.5), (3, 0.2)), vibrato=0.04) * env(int(SR * 2.0), 0.3, 0.6, 0.7, 0.4),
                                       sweep(160, 95, 1.8, 0.3) * env(int(SR * 1.8), 0.2, 0.7), noise(1.6, 0.06, 80, 38) * env(int(SR * 1.6), 0.3, 0.6)))
    # Objets / quêtes
    save("objet/fragment", chime([1567.98, 2093.0, 2637.0], 0.5, 0.07, 0.35))
    save("quete/etape", chime([659.25, 987.77], 0.6, 0.14, 0.4))
    save("quete/terminee", chime([523.25, 659.25, 783.99, 1046.5], 1.0, 0.16, 0.42))
    save("monde/brumes", mix(noise(2.0, 0.3, 60, 8) * env(int(SR * 2.0), 0.3, 0.6), chime([440.0, 554.37, 659.25], 1.4, 0.3, 0.25)))
    # Créatures
    save("creature/cerf_appel", mix(tone(196.0, 0.9, 0.5, harmonics=((1, 1.0), (2, 0.6), (3, 0.4), (4, 0.2)), vibrato=0.02) * env(int(SR * 0.9), 0.08, 0.8),
                                    sweep(260, 180, 0.9, 0.2) * env(int(SR * 0.9), 0.1, 0.8)))
    save("creature/poisson_scintille", chime([2093.0, 2637.0, 3135.96], 0.3, 0.05, 0.25))
    save("creature/veilleur_prepare", mix(sweep(80, 220, 0.8, 0.5) * env(int(SR * 0.8), 0.05, 0.3, 0.8, 0.2),
                                          noise(0.8, 0.15, 10, 9) * env(int(SR * 0.8), 0.1, 0.5)))
    save("creature/veilleur_frappe", mix(noise(0.5, 0.6, 3, 10) * env(int(SR * 0.5), 0.0, 0.9),
                                         tone(55.0, 0.6, 0.6, harmonics=((1, 1.0), (2, 0.4))) * env(int(SR * 0.6), 0.0, 0.9)))
    save("creature/veilleur_blesse", mix(noise(0.3, 0.5, 2, 11) * env(int(SR * 0.3), 0.0, 0.9), tone(180, 0.3, 0.3) * env(int(SR * 0.3), 0.0, 0.9)))
    save("creature/veilleur_mort", mix(noise(1.2, 0.6, 3, 12) * env(int(SR * 1.2), 0.0, 0.9),
                                       sweep(160, 40, 1.2, 0.5) * env(int(SR * 1.2), 0.0, 0.9),
                                       chime([659.25, 523.25], 0.9, 0.3, 0.2, False)))
    save("interface/dialogue", chime([880.0, 1108.73], 0.35, 0.08, 0.3, False))
    save("creature/raie_appel", mix(tone(392.0, 1.1, 0.4, harmonics=((1, 1.0), (2, 0.3), (3, 0.12)), vibrato=0.03) * env(int(SR * 1.1), 0.15, 0.7),
                                    sweep(620, 940, 1.1, 0.14) * env(int(SR * 1.1), 0.2, 0.7)))
    save("creature/raie_pacte", chime([523.25, 783.99, 1046.5, 1318.5], 1.0, 0.15, 0.4))
    n = int(SR * 3.0)
    t = np.arange(n) / SR
    pli = 0.25 * np.sin(2 * math.pi * (180 + 60 * np.sin(2 * math.pi * 0.5 * t)) * t) * env(n, 0.4, 0.3, 0.6, 0.3)
    save("monde/arrivee", mix(pli, sweep(200, 1600, 2.2, 0.2) * env(int(SR * 2.2), 0.3, 0.6), chime([880.0, 1318.5, 1760.0, 2637.0], 1.6, 0.35, 0.35)))
    save("bloc/moulin_grince", mix(sweep(140, 90, 1.4, 0.35) * env(int(SR * 1.4), 0.1, 0.8), noise(1.4, 0.18, 50, 13) * env(int(SR * 1.4), 0.2, 0.7),
                                   tone(70, 1.4, 0.25, harmonics=((1, 1.0), (3, 0.3))) * env(int(SR * 1.4), 0.05, 0.9)))
    save("bloc/cristal_resonne", mix(chime([1046.5, 1318.5, 1568.0], 0.5, 0.06, 0.4), tone(2093.0, 0.4, 0.08)))
    save("bloc/dalle_emerge", mix(chime([659.25, 880.0, 1174.66], 0.7, 0.1, 0.35), noise(0.5, 0.15, 30, 14) * env(int(SR * 0.5), 0.05, 0.9)))
    # Créatures régionales (étape 5)
    save("creature/crabe_clic", mix(noise(0.12, 0.5, 2, 21) * env(int(SR * 0.12), 0.0, 0.9), tone(1800, 0.08, 0.2) * env(int(SR * 0.08), 0.0, 0.9),
                                    noise(0.1, 0.4, 2, 22) * env(int(SR * 0.1), 0.0, 0.9)))
    n = int(SR * 1.2)
    t = np.arange(n) / SR
    bourdon = 0.3 * np.sin(2 * math.pi * (240 + 12 * np.sin(2 * math.pi * 9 * t)) * t) * (0.7 + 0.3 * np.sin(2 * math.pi * 25 * t))
    save("creature/insecte_bourdon", bourdon * env(n, 0.1, 0.3, 0.7, 0.3))
    save("creature/oiseau_chant", mix(chime([1760.0, 2093.0, 1975.5, 2349.3], 0.18, 0.05, 0.3, False), sweep(1500, 2400, 0.25, 0.2) * env(int(SR * 0.25), 0.02, 0.9)))
    save("creature/geant_pas", mix(noise(0.6, 0.6, 3, 23) * env(int(SR * 0.6), 0.0, 0.9), tone(45.0, 0.8, 0.7, harmonics=((1, 1.0), (2, 0.5))) * env(int(SR * 0.8), 0.0, 0.9),
                                   sweep(120, 40, 0.5, 0.3) * env(int(SR * 0.5), 0.0, 0.9)))
    save("creature/monture_appel", mix(tone(330.0, 0.8, 0.45, harmonics=((1, 1.0), (2, 0.4), (3, 0.15)), vibrato=0.02) * env(int(SR * 0.8), 0.1, 0.7),
                                       sweep(300, 420, 0.8, 0.15) * env(int(SR * 0.8), 0.1, 0.7)))
    save("creature/tireur_tir", mix(sweep(1400, 500, 0.3, 0.4) * env(int(SR * 0.3), 0.0, 0.9), noise(0.25, 0.3, 8, 24) * env(int(SR * 0.25), 0.0, 0.9),
                                    chime([1046.5], 0.25, 0.05, 0.2, False)))
    save("creature/petit_cri", mix(sweep(900, 1500, 0.18, 0.35) * env(int(SR * 0.18), 0.02, 0.8), tone(1200, 0.15, 0.2, vibrato=0.05) * env(int(SR * 0.15), 0.02, 0.8)))
    print("Terminé.")


if __name__ == "__main__":
    main()
