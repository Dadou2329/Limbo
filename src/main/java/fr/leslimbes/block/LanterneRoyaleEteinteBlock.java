package fr.leslimbes.block;

import fr.leslimbes.capitale.CapitaleManager;
import fr.leslimbes.registry.LimbesBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

/** Lanterne royale éteinte de la capitale endormie ; elle se rallume au couronnement. */
public class LanterneRoyaleEteinteBlock extends LanternBlock {
	public LanterneRoyaleEteinteBlock(Settings settings) {
		super(settings);
	}

	/** La lanterne allumée correspondante (même accroche). */
	public static BlockState allumee(BlockState eteinte) {
		return LimbesBlocks.LANTERNE_ROYALE.getDefaultState().with(HANGING, eteinte.get(HANGING)).with(WATERLOGGED, eteinte.get(WATERLOGGED));
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		CapitaleManager.appliquer(world, pos, state, CapitaleManager.niveau(world.getServer()));
	}
}
