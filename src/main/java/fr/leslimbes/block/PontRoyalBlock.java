package fr.leslimbes.block;

import fr.leslimbes.capitale.CapitaleManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TransparentBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/** Pont de lumière de la capitale : absent (traversable, à peine visible) jusqu'au couronnement, où il se reforme pour de bon. */
public class PontRoyalBlock extends TransparentBlock {
	public static final BooleanProperty FORME = BooleanProperty.of("forme");
	public static final com.mojang.serialization.MapCodec<PontRoyalBlock> CODEC = createCodec(PontRoyalBlock::new);

	public PontRoyalBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(FORME, false));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends TransparentBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FORME);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(FORME) ? VoxelShapes.fullCube() : VoxelShapes.empty();
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(FORME) ? VoxelShapes.fullCube() : Block.createCuboidShape(0, 0, 0, 16, 1, 16);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		CapitaleManager.appliquer(world, pos, state, CapitaleManager.niveau(world.getServer()));
	}
}
