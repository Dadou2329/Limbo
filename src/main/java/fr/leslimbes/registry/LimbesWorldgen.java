package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.world.gen.FlattenDensityFunction;
import fr.leslimbes.world.gen.LacFeature;
import fr.leslimbes.world.structure.ComposedPiece;
import fr.leslimbes.world.structure.ComposedStructure;
import fr.leslimbes.world.structure.FixedStructurePlacement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.structure.StructureType;

/** Types de génération enregistrés en code (le contenu lui-même est décrit en JSON). */
public final class LimbesWorldgen {
	public static final StructurePlacementType<FixedStructurePlacement> PLACEMENT_FIXE =
			Registry.register(Registries.STRUCTURE_PLACEMENT, LesLimbes.id("fixed"), () -> FixedStructurePlacement.CODEC);
	public static final StructureType<ComposedStructure> STRUCTURE_COMPOSEE =
			Registry.register(Registries.STRUCTURE_TYPE, LesLimbes.id("composee"), () -> ComposedStructure.CODEC);
	public static final StructurePieceType PIECE_COMPOSEE =
			Registry.register(Registries.STRUCTURE_PIECE, LesLimbes.id("piece_composee"), (StructurePieceType.ManagerAware) ComposedPiece::new);

	/** Lac clair des Prairies (remplace minecraft:lake, dépréciée et instable en bordure de chunk). */
	public static final Feature<DefaultFeatureConfig> LAC = Registry.register(Registries.FEATURE, LesLimbes.id("lac"), new LacFeature(DefaultFeatureConfig.CODEC));
	/** Remplissage de la Mer suspendue (volumes d'eau stables dans la cuvette de la région). */
	public static final Feature<DefaultFeatureConfig> MER = Registry.register(Registries.FEATURE, LesLimbes.id("mer"), new fr.leslimbes.world.gen.MerFeature(DefaultFeatureConfig.CODEC));
	/** Routes royales procédurales entre les sites. */
	public static final Feature<fr.leslimbes.world.gen.RoutesFeature.Config> ROUTES = Registry.register(Registries.FEATURE, LesLimbes.id("routes"),
			new fr.leslimbes.world.gen.RoutesFeature(fr.leslimbes.world.gen.RoutesFeature.Config.CODEC));

	public static void init() {
		Registry.register(Registries.DENSITY_FUNCTION_TYPE, LesLimbes.id("aplanir"), FlattenDensityFunction.MAP_CODEC);
		Registry.register(Registries.DENSITY_FUNCTION_TYPE, LesLimbes.id("regions"), fr.leslimbes.world.gen.RegionsDensityFunction.MAP_CODEC);
		Registry.register(Registries.BIOME_SOURCE, LesLimbes.id("regions"), fr.leslimbes.world.biome.RegionBiomeSource.CODEC);
	}

	private LimbesWorldgen() {
	}
}
