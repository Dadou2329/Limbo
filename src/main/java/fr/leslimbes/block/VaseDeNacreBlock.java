package fr.leslimbes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/** Vase de nacre : petit bloc décoratif posé sur une table ou un sol (silhouette réduite). */
public class VaseDeNacreBlock extends Block {
	private static final VoxelShape FORME = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 11.0, 12.0);

	public VaseDeNacreBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return FORME;
	}
}
