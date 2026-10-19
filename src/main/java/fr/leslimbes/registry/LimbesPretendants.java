package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.entity.duel.PretendantEntity;
import fr.leslimbes.entity.duel.StyleDuel;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Boss des duels : un type d'entité par prétendant (et le champion des épreuves), servi par {@link PretendantEntity}
 * avec son {@link StyleDuel}. Les modèles portent le même identifiant dans {@code tools/models/modeles_regions.py}.
 */
public final class LimbesPretendants {
	/** Fiche d'un boss : style, dimensions, statistiques. */
	public record Fiche(String id, StyleDuel style, float largeur, float hauteur, float yeux, double vie, double degats, double armure, double vitesse) {
	}

	private static final Map<String, Fiche> FICHES = new LinkedHashMap<>();
	private static final Map<String, EntityType<PretendantEntity>> TYPES = new LinkedHashMap<>();
	private static final Map<EntityType<?>, Fiche> PAR_TYPE = new HashMap<>();

	static {
		declarer(new Fiche("aelis_duel", StyleDuel.AELIS, 0.7F, 2.0F, 1.75F, 160.0, 7.0, 4.0, 0.3));
		declarer(new Fiche("kael_duel", StyleDuel.KAEL, 0.7F, 1.95F, 1.7F, 150.0, 7.0, 3.0, 0.34));
		declarer(new Fiche("orsane_duel", StyleDuel.ORSANE, 0.8F, 2.2F, 1.9F, 180.0, 8.0, 8.0, 0.28));
		declarer(new Fiche("sahir_duel", StyleDuel.SAHIR, 0.7F, 1.95F, 1.7F, 140.0, 6.0, 2.0, 0.26));
		declarer(new Fiche("vaeron_duel", StyleDuel.VAERON, 0.9F, 2.3F, 2.0F, 320.0, 9.0, 8.0, 0.3));
		declarer(new Fiche("champion_des_heures_duel", StyleDuel.CHAMPION, 0.8F, 2.2F, 1.9F, 90.0, 8.0, 6.0, 0.3));
	}

	private static void declarer(Fiche f) {
		FICHES.put(f.id(), f);
	}

	public static void init() {
		for (Fiche f : FICHES.values()) {
			EntityType<PretendantEntity> type = Registry.register(Registries.ENTITY_TYPE, LesLimbes.id(f.id()),
					EntityType.Builder.<PretendantEntity>create(PretendantEntity::new, SpawnGroup.MONSTER).dimensions(f.largeur(), f.hauteur()).eyeHeight(f.yeux())
							.maxTrackingRange(12).build(f.id()));
			FabricDefaultAttributeRegistry.register(type, PretendantEntity.creerAttributs(f));
			TYPES.put(f.id(), type);
			PAR_TYPE.put(type, f);
		}
		LesLimbes.LOGGER.info("{} boss de duel enregistrés", TYPES.size());
	}

	public static Map<String, EntityType<PretendantEntity>> types() {
		return Collections.unmodifiableMap(TYPES);
	}

	public static Map<String, Fiche> fiches() {
		return Collections.unmodifiableMap(FICHES);
	}

	public static EntityType<PretendantEntity> type(String id) {
		EntityType<PretendantEntity> t = TYPES.get(id);
		if (t == null) {
			throw new IllegalArgumentException("boss inconnu : " + id);
		}
		return t;
	}

	public static StyleDuel styleDe(EntityType<?> type) {
		Fiche f = PAR_TYPE.get(type);
		if (f == null) {
			throw new IllegalStateException("type sans fiche de boss : " + type);
		}
		return f.style();
	}

	private LimbesPretendants() {
	}
}
