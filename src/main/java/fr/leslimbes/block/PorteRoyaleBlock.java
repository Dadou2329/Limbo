package fr.leslimbes.block;

import fr.leslimbes.capitale.CapitaleManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * Porte royale de la capitale endormie : un voile de pierre et de lumière qui ferme un passage tant que la
 * capitale n'est pas éveillée au niveau requis ({@code niveau} 1 : quartiers extérieurs, 2 : palais, 3 : cérémonie).
 * Ouverte, elle disparaît (sans collision) ; elle lit le niveau d'éveil du monde à ses ticks aléatoires.
 */
public class PorteRoyaleBlock extends Block {
	public static final IntProperty NIVEAU = IntProperty.of("niveau", 1, 3);
	public static final BooleanProperty OUVERTE = BooleanProperty.of("ouverte");
	public static final com.mojang.serialization.MapCodec<PorteRoyaleBlock> CODEC = createCodec(PorteRoyaleBlock::new);

	public PorteRoyaleBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(NIVEAU, 1).with(OUVERTE, false));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(NIVEAU, OUVERTE);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(OUVERTE) ? VoxelShapes.empty() : VoxelShapes.fullCube();
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(OUVERTE) ? VoxelShapes.empty() : VoxelShapes.fullCube();
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		CapitaleManager.appliquer(world, pos, state, CapitaleManager.niveau(world.getServer()));
	}
}
