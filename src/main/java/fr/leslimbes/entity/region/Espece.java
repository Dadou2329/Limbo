package fr.leslimbes.entity.region;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fiche d'une espèce régionale : archétype de comportement, dimensions, statistiques, sons, appât
 * d'apprivoisement, paramètres de combat. Renseignée une fois à la déclaration (voir
 * {@code LimbesEspeces}), puis lue seulement : une seule classe Java par archétype sert toutes les espèces.
 */
public final class Espece {
	public enum Archetype {
		/** Animal terrestre tranquille (fuit, se laisse appâter). */
		PAISIBLE,
		/** Petite créature qui nage dans l'air (ambiance). */
		VOLANT,
		/** Créature d'eau (bancs de la Mer suspendue, lacs). */
		AQUATIQUE,
		/** Ennemi au corps à corps dont chaque coup est annoncé. */
		ENNEMI_ANNONCE,
		/** Ennemi à distance : se prépare visiblement, puis lance un éclat. */
		ENNEMI_TIREUR,
		/** Monture : pacte par l'appât, rappel au sifflet, saut (sol) ou vol. */
		MONTURE
	}

	public final String id;
	public final Archetype archetype;
	public float largeur = 0.8F;
	public float hauteur = 0.8F;
	public float yeux = -1.0F;
	public float attachePassager = -1.0F;
	public double vie = 10.0;
	public double vitesse = 0.25;
	public double vitesseVol = 0.5;
	public double degats = 0.0;
	public double armure = 0.0;
	public double resistanceRecul = 0.0;
	public double pas = 0.6;
	public double chuteSure = 3.0;
	public int xp = 3;
	public int preparation = 18;
	public int recuperation = 24;
	public int nourritures = 3;
	public int dureeEffet = 0;
	public int intervalleTir = 50;
	public double portee = 3.0;
	public double forceRecul = 0.9;
	public double porteeTir = 14.0;
	public double degatsProjectile = 3.0;
	public double reculProjectile = 0.0;
	public boolean fuit = true;
	public boolean vol = false;
	public boolean geant = false;
	public boolean nage = false;
	public boolean seChauffe = false;
	@Nullable
	public SoundEvent ambiant;
	@Nullable
	public SoundEvent blesse;
	@Nullable
	public SoundEvent mort;
	@Nullable
	public Item appat;
	@Nullable
	public RegistryEntry<StatusEffect> effet;
	public int couleurOeuf1 = 0x888888;
	public int couleurOeuf2 = 0xFFFFFF;

	public Espece(String id, Archetype archetype) {
		this.id = id;
		this.archetype = archetype;
	}

	public Espece taille(float largeur, float hauteur) {
		this.largeur = largeur;
		this.hauteur = hauteur;
		return this;
	}

	public Espece yeux(float hauteur) {
		this.yeux = hauteur;
		return this;
	}

	public Espece passager(float hauteur) {
		this.attachePassager = hauteur;
		return this;
	}

	public Espece vie(double vie) {
		this.vie = vie;
		return this;
	}

	public Espece vitesse(double vitesse) {
		this.vitesse = vitesse;
		return this;
	}

	public Espece vitesseVol(double vitesseVol) {
		this.vitesseVol = vitesseVol;
		return this;
	}

	public Espece combat(double degats, double armure, double resistanceRecul) {
		this.degats = degats;
		this.armure = armure;
		this.resistanceRecul = resistanceRecul;
		return this;
	}

	public Espece annonce(int preparation, int recuperation, double portee, double forceRecul) {
		this.preparation = preparation;
		this.recuperation = recuperation;
		this.portee = portee;
		this.forceRecul = forceRecul;
		return this;
	}

	public Espece tir(int preparation, int intervalle, double portee, double degats, double recul) {
		this.preparation = preparation;
		this.intervalleTir = intervalle;
		this.porteeTir = portee;
		this.degatsProjectile = degats;
		this.reculProjectile = recul;
		return this;
	}

	public Espece effet(RegistryEntry<StatusEffect> effet, int duree) {
		this.effet = effet;
		this.dureeEffet = duree;
		return this;
	}

	public Espece sons(@Nullable SoundEvent ambiant, @Nullable SoundEvent blesse, @Nullable SoundEvent mort) {
		this.ambiant = ambiant;
		this.blesse = blesse;
		this.mort = mort;
		return this;
	}

	public Espece appat(Item appat, int nourritures) {
		this.appat = appat;
		this.nourritures = nourritures;
		return this;
	}

	public Espece pas(double pas, double chuteSure) {
		this.pas = pas;
		this.chuteSure = chuteSure;
		return this;
	}

	public Espece xp(int xp) {
		this.xp = xp;
		return this;
	}

	public Espece sansFuite() {
		this.fuit = false;
		return this;
	}

	public Espece vol() {
		this.vol = true;
		return this;
	}

	public Espece geant() {
		this.geant = true;
		this.fuit = false;
		return this;
	}

	public Espece nage() {
		this.nage = true;
		return this;
	}

	public Espece seChauffe() {
		this.seChauffe = true;
		return this;
	}

	public Espece oeuf(int couleur1, int couleur2) {
		this.couleurOeuf1 = couleur1;
		this.couleurOeuf2 = couleur2;
		return this;
	}
}
