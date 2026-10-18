package fr.leslimbes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/** Cristal de nacre : bloc lumineux translucide utilisé pour l'éclairage des villes. */
public class CristalDeNacreBlock extends Block {
	public CristalDeNacreBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
		return true;
	}

	@Override
	protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
		return 1.0F;
	}
}
