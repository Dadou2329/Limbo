package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.entity.CerfLanterneEntity;
import fr.leslimbes.entity.PoissonLuneEntity;
import fr.leslimbes.entity.RaieCelesteEntity;
import fr.leslimbes.entity.VeilleurEgareEntity;
import fr.leslimbes.npc.HabitantEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

/** Entités des Limbes. */
public final class LimbesEntities {
	public static final EntityType<HabitantEntity> HABITANT = register("habitant",
			EntityType.Builder.create(HabitantEntity::new, SpawnGroup.MISC).dimensions(0.6F, 1.95F).eyeHeight(1.62F).maxTrackingRange(10));
	public static final EntityType<PoissonLuneEntity> POISSON_LUNE = register("poisson_lune",
			EntityType.Builder.create(PoissonLuneEntity::new, SpawnGroup.AMBIENT).dimensions(0.5F, 0.35F).eyeHeight(0.2F).maxTrackingRange(8));
	public static final EntityType<CerfLanterneEntity> CERF_LANTERNE = register("cerf_lanterne",
			EntityType.Builder.create(CerfLanterneEntity::new, SpawnGroup.CREATURE).dimensions(0.9F, 1.7F).eyeHeight(1.5F).maxTrackingRange(10));
	public static final EntityType<VeilleurEgareEntity> VEILLEUR_EGARE = register("veilleur_egare",
			EntityType.Builder.create(VeilleurEgareEntity::new, SpawnGroup.MONSTER).dimensions(0.9F, 2.4F).eyeHeight(2.0F).maxTrackingRange(10));
	public static final EntityType<RaieCelesteEntity> RAIE_CELESTE = register("raie_celeste",
			EntityType.Builder.create(RaieCelesteEntity::new, SpawnGroup.CREATURE).dimensions(1.5F, 0.6F).eyeHeight(0.4F)
					.passengerAttachments(0.75F).maxTrackingRange(10));

	private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String path, EntityType.Builder<T> builder) {
		return Registry.register(Registries.ENTITY_TYPE, LesLimbes.id(path), builder.build(path));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(HABITANT, HabitantEntity.creerAttributs());
		FabricDefaultAttributeRegistry.register(POISSON_LUNE, PoissonLuneEntity.creerAttributs());
		FabricDefaultAttributeRegistry.register(CERF_LANTERNE, CerfLanterneEntity.creerAttributs());
		FabricDefaultAttributeRegistry.register(VEILLEUR_EGARE, VeilleurEgareEntity.creerAttributs());
		SpawnRestriction.register(POISSON_LUNE, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PoissonLuneEntity::peutApparaitre);
		SpawnRestriction.register(CERF_LANTERNE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
		SpawnRestriction.register(VEILLEUR_EGARE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnIgnoreLightLevel);
		FabricDefaultAttributeRegistry.register(RAIE_CELESTE, RaieCelesteEntity.creerAttributs());
		SpawnRestriction.register(RAIE_CELESTE, SpawnLocationTypes.UNRESTRICTED, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, RaieCelesteEntity::peutApparaitre);
	}

	private LimbesEntities() {
	}
}
