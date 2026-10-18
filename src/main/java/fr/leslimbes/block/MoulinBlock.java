package fr.leslimbes.block;

import fr.leslimbes.registry.LimbesBlockEntities;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Axe des ailes du moulin. Les ailes elles-mêmes sont rendues côté client (bloc-entité) et tournent
 * quand {@code actif} est vrai : la transformation est visible de loin, sans entité coûteuse.
 */
public class MoulinBlock extends HorizontalFacingBlock implements BlockEntityProvider {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty ACTIF = BooleanProperty.of("actif");
	public static final com.mojang.serialization.MapCodec<MoulinBlock> CODEC = createCodec(MoulinBlock::new);

	public MoulinBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(FACING, net.minecraft.util.math.Direction.SOUTH).with(ACTIF, false));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, ACTIF);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new MoulinBlockEntity(pos, state);
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (state.get(ACTIF)) {
			world.scheduleBlockTick(pos, this, 200);
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(ACTIF)) {
			world.playSound(null, pos, LimbesSounds.MOULIN_GRINCE, SoundCategory.BLOCKS, 0.5F, 0.9F + random.nextFloat() * 0.2F);
			world.scheduleBlockTick(pos, this, 200 + random.nextInt(100));
		}
	}

	/** Bloc-entité minimal : il ne porte aucune donnée, il existe pour le rendu des ailes. */
	public static final class MoulinBlockEntity extends BlockEntity {
		public MoulinBlockEntity(BlockPos pos, BlockState state) {
			super(LimbesBlockEntities.AILES_DE_MOULIN, pos, state);
		}
	}
}
