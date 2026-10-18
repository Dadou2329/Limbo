package fr.leslimbes.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/** Herbe de l'aube : plante aromatique utilisée en cuisine (bouillon de l'aube). */
public class HerbeDeLAubeBlock extends PlantBlock {
	public static final MapCodec<HerbeDeLAubeBlock> CODEC = createCodec(HerbeDeLAubeBlock::new);
	private static final VoxelShape SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 10.0, 13.0);

	public HerbeDeLAubeBlock(Settings settings) {
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
