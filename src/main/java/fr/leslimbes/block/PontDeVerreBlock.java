package fr.leslimbes.block;

import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TransparentBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Pont de verre des Montagnes : ses éléments ne se forment qu'au passage. Invisible et traversable tant
 * qu'aucun joueur n'est à moins de six blocs ; dès qu'on approche, le verre se reforme avec un tintement.
 * Vérification planifiée toutes les dix ticks, sur les seuls blocs posés par les gabarits.
 */
public class PontDeVerreBlock extends TransparentBlock {
	public static final BooleanProperty FORME = BooleanProperty.of("forme");
	public static final com.mojang.serialization.MapCodec<PontDeVerreBlock> CODEC = createCodec(PontDeVerreBlock::new);
	private static final double PORTEE = 6.0;

	public PontDeVerreBlock(Settings settings) {
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
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		world.scheduleBlockTick(pos, this, 10);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		boolean proche = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, PORTEE, false) != null;
		if (proche != state.get(FORME)) {
			world.setBlockState(pos, state.with(FORME, proche), Block.NOTIFY_ALL);
			if (proche && random.nextInt(3) == 0) {
				world.playSound(null, pos, LimbesSounds.CRISTAL_RESONNE, SoundCategory.BLOCKS, 0.6F, 1.2F + random.nextFloat() * 0.4F);
			}
		}
		world.scheduleBlockTick(pos, this, 10);
	}

	/** Les gabarits posent le pont sans onBlockAdded : premier tick planifié par la génération. */
	public static void planifierDansBoite(net.minecraft.world.WorldAccess world, BlockPos origine, net.minecraft.util.math.Vec3i taille) {
		for (BlockPos p : BlockPos.iterate(origine, origine.add(taille.getX() - 1, taille.getY() - 1, taille.getZ() - 1))) {
			if (world.getBlockState(p).getBlock() instanceof PontDeVerreBlock pont) {
				world.scheduleBlockTick(p, pont, 1);
			}
		}
	}
}
