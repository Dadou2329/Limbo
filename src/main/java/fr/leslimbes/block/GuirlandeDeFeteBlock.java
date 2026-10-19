package fr.leslimbes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

/** Guirlande de fête : cordon de petites lanternes tendu sous le plafond, orienté selon l'axe horizontal du regard. */
public class GuirlandeDeFeteBlock extends Block {
	public static final EnumProperty<Direction.Axis> AXE = Properties.HORIZONTAL_AXIS;
	private static final VoxelShape FORME_X = Block.createCuboidShape(0.0, 9.0, 6.0, 16.0, 16.0, 10.0);
	private static final VoxelShape FORME_Z = Block.createCuboidShape(6.0, 9.0, 0.0, 10.0, 16.0, 16.0);

	public GuirlandeDeFeteBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(AXE, Direction.Axis.X));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXE);
	}

	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(AXE, ctx.getHorizontalPlayerFacing().getAxis());
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(AXE) == Direction.Axis.X ? FORME_X : FORME_Z;
	}
}
