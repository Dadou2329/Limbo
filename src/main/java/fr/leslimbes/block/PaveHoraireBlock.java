package fr.leslimbes.block;

import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Pavé horaire du Désert des Heures : une route qui n'existe qu'à un quart du jour donné ({@code phase}).
 * Hors de son heure, le pavé n'est qu'un reflet traversable ; à son heure, il porte. Les routes de midi
 * ou de nuit du désert sont faites de ces pavés.
 */
public class PaveHoraireBlock extends Block {
	public static final IntProperty PHASE = IntProperty.of("phase", 0, 3);
	public static final BooleanProperty VISIBLE = BooleanProperty.of("visible");
	public static final com.mojang.serialization.MapCodec<PaveHoraireBlock> CODEC = createCodec(PaveHoraireBlock::new);
	private static final VoxelShape MINCE = Block.createCuboidShape(0, 0, 0, 16, 1, 16);

	public PaveHoraireBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(PHASE, 1).with(VISIBLE, false));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(PHASE, VISIBLE);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(VISIBLE) ? VoxelShapes.fullCube() : VoxelShapes.empty();
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(VISIBLE) ? VoxelShapes.fullCube() : MINCE;
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		world.scheduleBlockTick(pos, this, 1);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		boolean visible = CadranBlock.quart(world.getTimeOfDay()) == state.get(PHASE);
		if (visible != state.get(VISIBLE)) {
			world.setBlockState(pos, state.with(VISIBLE, visible), Block.NOTIFY_ALL);
			if (visible && random.nextInt(4) == 0) {
				world.playSound(null, pos, LimbesSounds.DALLE_EMERGE, SoundCategory.BLOCKS, 0.5F, 1.1F);
			}
		}
		world.scheduleBlockTick(pos, this, 100);
	}

	@Override
	protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
		return !state.get(VISIBLE);
	}
}
