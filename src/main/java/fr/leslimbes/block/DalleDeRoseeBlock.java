package fr.leslimbes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * Dalle de rosée : pavé immergé du Lac du Crépuscule. Elle n'offre un appui et ne s'éclaire qu'au
 * crépuscule et à l'aube ; le reste du temps on passe au travers. Elle se re-planifie elle-même.
 */
public class DalleDeRoseeBlock extends Block implements net.minecraft.block.FluidFillable {
	public static final BooleanProperty REVELEE = BooleanProperty.of("revelee");
	private static final int PERIODE = 40;

	public DalleDeRoseeBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(REVELEE, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(REVELEE);
	}

	/** Crépuscule (12 000 à 13 800) et aube (22 800 à 24 000) : les dalles émergent. */
	public static boolean estLHeureDesDalles(World world) {
		long t = world.getTimeOfDay() % 24000L;
		return (t >= 11800L && t <= 13800L) || t >= 22800L;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(REVELEE) ? VoxelShapes.fullCube() : VoxelShapes.empty();
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(REVELEE) ? VoxelShapes.fullCube() : Block.createCuboidShape(0, 0, 0, 16, 2, 16);
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		world.scheduleBlockTick(pos, this, 1);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		boolean cible = estLHeureDesDalles(world);
		if (state.get(REVELEE) != cible) {
			world.setBlockState(pos, state.with(REVELEE, cible), 3);
		}
		world.scheduleBlockTick(pos, this, PERIODE);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		scheduledTick(state, world, pos, random);
	}

	/** Planifie le premier tick d'une dalle posée par la génération (onBlockAdded n'y est pas appelé). */
	public static void planifier(WorldAccess world, BlockPos pos, Block block) {
		world.scheduleBlockTick(pos, block, 1);
	}

	@Override
	protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
		return !state.get(REVELEE);
	}

	/**
	 * Cachée, la dalle contient de l'eau : on y nage, et l'eau voisine ne cherche pas à la remplacer
	 * (un bloc sans collision serait « emporté » par le courant). Révélée, elle est sèche et porteuse.
	 */
	@Override
	protected net.minecraft.fluid.FluidState getFluidState(BlockState state) {
		return state.get(REVELEE) ? net.minecraft.fluid.Fluids.EMPTY.getDefaultState() : net.minecraft.fluid.Fluids.WATER.getStill(false);
	}

	@Override
	public boolean canFillWithFluid(@org.jetbrains.annotations.Nullable net.minecraft.entity.player.PlayerEntity player, BlockView world, BlockPos pos, BlockState state, net.minecraft.fluid.Fluid fluid) {
		return false;
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, net.minecraft.fluid.FluidState fluidState) {
		return false;
	}
}
