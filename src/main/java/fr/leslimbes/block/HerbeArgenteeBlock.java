package fr.leslimbes.block;

import fr.leslimbes.registry.LimbesBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;

/**
 * Herbe argentée des Prairies de l'Aube. Elle redevient terre des Limbes lorsqu'elle est recouverte
 * et s'étend lentement sur la terre voisine éclairée, sans jamais se transformer en blocs vanilla.
 */
public class HerbeArgenteeBlock extends Block {
	public HerbeArgenteeBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
			return;
		}
		BlockPos above = pos.up();
		BlockState aboveState = world.getBlockState(above);
		if (aboveState.isOpaqueFullCube(world, above) || world.getLightLevel(LightType.SKY, above) < 4 && aboveState.getOpacity(world, above) > 2) {
			world.setBlockState(pos, LimbesBlocks.TERRE_DES_LIMBES.getDefaultState());
			return;
		}
		if (world.getLightLevel(above) >= 9) {
			for (int i = 0; i < 4; i++) {
				BlockPos target = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
				if (world.getBlockState(target).isOf(LimbesBlocks.TERRE_DES_LIMBES)
						&& !world.getBlockState(target.up()).isOpaqueFullCube(world, target.up())
						&& world.getLightLevel(target.up()) >= 4) {
					world.setBlockState(target, this.getDefaultState());
				}
			}
		}
	}
}
