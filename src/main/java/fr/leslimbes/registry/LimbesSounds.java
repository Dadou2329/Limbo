package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/** Sons originaux du mod (fichiers .ogg générés par tools/sounds/gen_sounds.py). */
public final class LimbesSounds {
	public static final SoundEvent PORTE_EVEIL = register("bloc.porte.eveil");
	public static final SoundEvent PORTE_PASSAGE = register("bloc.porte.passage");
	public static final SoundEvent PORTE_AMBIANCE = register("bloc.porte.ambiance");
	public static final SoundEvent POUVOIR_EPEE = register("pouvoir.epee");
	public static final SoundEvent POUVOIR_EPEE_FINAL = register("pouvoir.epee_final");
	public static final SoundEvent POUVOIR_PAS = register("pouvoir.pas");
	public static final SoundEvent POUVOIR_DECRET = register("pouvoir.decret");
	public static final SoundEvent POUVOIR_REFUS = register("pouvoir.refus");
	public static final SoundEvent FRAGMENT = register("objet.fragment");
	public static final SoundEvent QUETE_ETAPE = register("quete.etape");
	public static final SoundEvent QUETE_TERMINEE = register("quete.terminee");
	public static final SoundEvent BRUMES = register("monde.brumes");
	public static final SoundEvent CERF_APPEL = register("creature.cerf.appel");
	public static final SoundEvent POISSON_SCINTILLE = register("creature.poisson.scintille");
	public static final SoundEvent VEILLEUR_PREPARE = register("creature.veilleur.prepare");
	public static final SoundEvent VEILLEUR_FRAPPE = register("creature.veilleur.frappe");
	public static final SoundEvent VEILLEUR_BLESSE = register("creature.veilleur.blesse");
	public static final SoundEvent VEILLEUR_MORT = register("creature.veilleur.mort");
	public static final SoundEvent DIALOGUE_OUVRIR = register("interface.dialogue");
	public static final SoundEvent RAIE_APPEL = register("creature.raie.appel");
	public static final SoundEvent RAIE_PACTE = register("creature.raie.pacte");
	public static final SoundEvent ARRIVEE = register("monde.arrivee");
	public static final SoundEvent MOULIN_GRINCE = register("bloc.moulin.grince");
	public static final SoundEvent CRISTAL_RESONNE = register("bloc.cristal.resonne");
	public static final SoundEvent DALLE_EMERGE = register("bloc.dalle.emerge");
	public static final SoundEvent CRABE_CLIC = register("creature.crabe.clic");
	public static final SoundEvent INSECTE_BOURDON = register("creature.insecte.bourdon");
	public static final SoundEvent OISEAU_CHANT = register("creature.oiseau.chant");
	public static final SoundEvent GEANT_PAS = register("creature.geant.pas");
	public static final SoundEvent MONTURE_APPEL = register("creature.monture.appel");
	public static final SoundEvent TIREUR_TIR = register("creature.tireur.tir");
	public static final SoundEvent PETIT_CRI = register("creature.petit.cri");
	public static final SoundEvent POUVOIR_PARADE = register("pouvoir.parade");
	public static final SoundEvent POUVOIR_LANCE = register("pouvoir.lance");
	public static final SoundEvent POUVOIR_ARC = register("pouvoir.arc");
	public static final SoundEvent POUVOIR_GARDE = register("pouvoir.garde");
	public static final SoundEvent POUVOIR_MAIN = register("pouvoir.main");
	public static final SoundEvent POUVOIR_CONSTELLATION = register("pouvoir.constellation");
	public static final SoundEvent POUVOIR_AVENEMENT = register("pouvoir.avenement");
	public static final SoundEvent BALEINE_CHANT = register("creature.baleine.chant");

	private static SoundEvent register(String path) {
		Identifier id = LesLimbes.id(path);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static void init() {
	}

	private LimbesSounds() {
	}
}
