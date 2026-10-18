package fr.leslimbes.world.structure;

import fr.leslimbes.registry.LimbesWorldgen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

/**
 * Pièce d'une structure composée : un gabarit NBT. Les blocs de structure en mode DATA du gabarit
 * portent des métadonnées interprétées ici :
 * <ul>
 *   <li>{@code pnj:<role>} : fait apparaître un habitant avec ce rôle (persistant).</li>
 *   <li>{@code entite:<id>} : fait apparaître une entité quelconque (persistante si créature).</li>
 * </ul>
 */
public class ComposedPiece extends SimpleStructurePiece {
	public ComposedPiece(StructureTemplateManager manager, Identifier gabarit, BlockPos pos, BlockRotation rotation) {
		super(LimbesWorldgen.PIECE_COMPOSEE, 0, manager, gabarit, gabarit.toString(), placement(rotation), pos);
	}

	public ComposedPiece(StructureTemplateManager manager, NbtCompound nbt) {
		super(LimbesWorldgen.PIECE_COMPOSEE, nbt, manager, id -> placement(BlockRotation.valueOf(nbt.getString("Rot"))));
	}

	private static StructurePlacementData placement(BlockRotation rotation) {
		return new StructurePlacementData().setRotation(rotation).setMirror(BlockMirror.NONE)
				.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS).setIgnoreEntities(false);
	}

	@Override
	protected void writeNbt(net.minecraft.structure.StructureContext context, NbtCompound nbt) {
		super.writeNbt(context, nbt);
		nbt.putString("Rot", this.placementData.getRotation().name());
	}

	@Override
	public void generate(net.minecraft.world.StructureWorldAccess world, net.minecraft.world.gen.StructureAccessor structureAccessor,
						 net.minecraft.world.gen.chunk.ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox,
						 net.minecraft.util.math.ChunkPos chunkPos, BlockPos pivot) {
		super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
		// Les blocs qui s'animent seuls (dalles de rosée) reçoivent leur premier tick planifié ici :
		// onBlockAdded n'est pas appelé par la génération.
		BlockBox box = this.getBoundingBox();
		int x1 = Math.max(box.getMinX(), chunkBox.getMinX()), x2 = Math.min(box.getMaxX(), chunkBox.getMaxX());
		int z1 = Math.max(box.getMinZ(), chunkBox.getMinZ()), z2 = Math.min(box.getMaxZ(), chunkBox.getMaxZ());
		if (x1 > x2 || z1 > z2) {
			return;
		}
		for (BlockPos p : BlockPos.iterate(x1, box.getMinY(), z1, x2, box.getMaxY(), z2)) {
			if (world.getBlockState(p).getBlock() instanceof fr.leslimbes.block.DalleDeRoseeBlock dalle) {
				fr.leslimbes.block.DalleDeRoseeBlock.planifier(world, p, dalle);
			}
		}
	}

	@Override
	protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
		MarqueursDeStructure.appliquer(world, metadata, pos, random);
	}
}
