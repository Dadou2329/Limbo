package fr.leslimbes.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/** Fibres nacrées : hautes herbes lumineuses, ressource de base des Prairies. */
public class FibresNacreesBlock extends PlantBlock {
	public static final MapCodec<FibresNacreesBlock> CODEC = createCodec(FibresNacreesBlock::new);
	private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

	public FibresNacreesBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends PlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}
}
