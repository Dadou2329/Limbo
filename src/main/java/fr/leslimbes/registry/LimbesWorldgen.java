package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.world.gen.FlattenDensityFunction;
import fr.leslimbes.world.structure.ComposedPiece;
import fr.leslimbes.world.structure.ComposedStructure;
import fr.leslimbes.world.structure.FixedStructurePlacement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
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

	public static void init() {
		Registry.register(Registries.DENSITY_FUNCTION_TYPE, LesLimbes.id("aplanir"), FlattenDensityFunction.MAP_CODEC);
	}

	private LimbesWorldgen() {
	}
}
