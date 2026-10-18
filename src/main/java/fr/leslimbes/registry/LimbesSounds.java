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

	private static SoundEvent register(String path) {
		Identifier id = LesLimbes.id(path);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static void init() {
	}

	private LimbesSounds() {
	}
}
