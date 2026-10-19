package fr.leslimbes.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.TransparentBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;

/**
 * Pont de lumière posé par le décret « Que le chemin s'ouvre » : un bloc translucide et lumineux qui s'efface
 * de lui-même au tick planifié lors de sa pose (la planification survit aux sauvegardes).
 */
public class PontDeLumiereBlock extends TransparentBlock {
	public PontDeLumiereBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		world.removeBlock(pos, false);
		world.spawnParticles(fr.leslimbes.registry.LimbesParticles.LUEUR, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.2, 0.3, 0.0);
	}

	@Override
	protected void onBlockAdded(BlockState state, net.minecraft.world.World world, BlockPos pos, BlockState oldState, boolean notify) {
		// Un pont posé sans planification (commande, structure) s'efface tout de même.
		if (!world.getBlockTickScheduler().isQueued(pos, this)) {
			world.scheduleBlockTick(pos, this, fr.leslimbes.power.DecretsRoyaux.DUREE_PONT);
		}
	}

	@Override
	protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
		return 1.0F;
	}

	@Override
	protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
		return true;
	}
}
