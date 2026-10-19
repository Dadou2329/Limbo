package fr.leslimbes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Cadran des heures (Désert des Heures) : suit le quart du jour (aube, midi, soir, nuit) et alimente la
 * redstone d'une puissance qui en dépend (4, 8, 12, 16). Les mécanismes scénarisés du désert (portes,
 * passages, ateliers) s'ouvrent ainsi à certaines heures, de façon locale et explicite.
 */
public class CadranBlock extends Block {
	/** 0 aube, 1 midi, 2 soir, 3 nuit. */
	public static final IntProperty HEURE = IntProperty.of("heure", 0, 3);
	public static final com.mojang.serialization.MapCodec<CadranBlock> CODEC = createCodec(CadranBlock::new);

	public CadranBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(HEURE, 0));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(HEURE);
	}

	/** Quart du jour pour un temps de monde (0..24000 : 23000-3000 aube, 3000-9000 midi, 9000-14000 soir, sinon nuit). */
	public static int quart(long temps) {
		long t = temps % 24000L;
		if (t >= 23000L || t < 3000L) {
			return 0;
		}
		if (t < 9000L) {
			return 1;
		}
		if (t < 14000L) {
			return 2;
		}
		return 3;
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		world.scheduleBlockTick(pos, this, 1);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int q = quart(world.getTimeOfDay());
		if (q != state.get(HEURE)) {
			world.setBlockState(pos, state.with(HEURE, q), Block.NOTIFY_ALL);
		}
		world.scheduleBlockTick(pos, this, 100);
	}

	@Override
	protected boolean emitsRedstonePower(BlockState state) {
		return true;
	}

	@Override
	protected int getWeakRedstonePower(BlockState state, net.minecraft.world.BlockView world, BlockPos pos, Direction direction) {
		return (state.get(HEURE) + 1) * 4;
	}

	public static void planifierDansBoite(net.minecraft.world.WorldAccess world, BlockPos origine, net.minecraft.util.math.Vec3i taille) {
		for (BlockPos p : BlockPos.iterate(origine, origine.add(taille.getX() - 1, taille.getY() - 1, taille.getZ() - 1))) {
			Block b = world.getBlockState(p).getBlock();
			if (b instanceof CadranBlock || b instanceof PaveHoraireBlock) {
				world.scheduleBlockTick(p, b, 1);
			}
		}
	}
}
