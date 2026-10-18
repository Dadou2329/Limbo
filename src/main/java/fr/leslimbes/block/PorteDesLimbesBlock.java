package fr.leslimbes.block;

import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.registry.LimbesParticles;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.dimension.LimbesPortal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Porte des Limbes : le passage entre l'Overworld et la dimension.
 *
 * <p>Dormante, c'est une pierre sombre gravée d'un glyphe. Réveillée avec trois éclats de nacre,
 * la porte devient un voile lumineux traversable. Un même « voile » est formé de plusieurs blocs
 * connectés ; en réveiller un réveille tout le voile.</p>
 *
 * <p>Aucune logique de téléportation n'est décidée côté client : voir {@link LimbesPortal}.</p>
 */
public class PorteDesLimbesBlock extends Block {
	public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
	public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
	public static final int ECLATS_REQUIS = 3;
	private static final int MAX_VOILE = 32;
	private static final VoxelShape VOILE_X = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
	private static final VoxelShape VOILE_Z = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

	public PorteDesLimbesBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(ACTIVE, false).with(AXIS, Direction.Axis.X));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE, AXIS);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(ACTIVE)) {
			return state.get(AXIS) == Direction.Axis.X ? VOILE_X : VOILE_Z;
		}
		return VoxelShapes.fullCube();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(ACTIVE) ? VoxelShapes.empty() : VoxelShapes.fullCube();
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (state.get(ACTIVE)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!stack.isOf(LimbesItems.FRAGMENT_DE_NACRE)) {
			if (!world.isClient) {
				player.sendMessage(Text.translatable("message.les_limbes.porte.dormante"), true);
			}
			return ItemActionResult.success(world.isClient);
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}
		if (stack.getCount() < ECLATS_REQUIS) {
			player.sendMessage(Text.translatable("message.les_limbes.porte.eclats_manquants", ECLATS_REQUIS - stack.getCount()), true);
			return ItemActionResult.CONSUME;
		}
		if (!player.isCreative()) {
			stack.decrement(ECLATS_REQUIS);
		}
		reveiller((ServerWorld) world, pos, player instanceof ServerPlayerEntity serverPlayer ? serverPlayer : null);
		return ItemActionResult.CONSUME;
	}

	/** Réveille tout le voile connecté à {@code origin}. Idempotent. */
	public static List<BlockPos> reveiller(ServerWorld world, BlockPos origin, ServerPlayerEntity activator) {
		List<BlockPos> voile = voileConnecte(world, origin);
		for (BlockPos p : voile) {
			BlockState s = world.getBlockState(p);
			if (!s.get(ACTIVE)) {
				world.setBlockState(p, s.with(ACTIVE, true), Block.NOTIFY_ALL);
			}
		}
		world.playSound(null, origin, LimbesSounds.PORTE_EVEIL, SoundCategory.BLOCKS, 1.2F, 1.0F);
		for (BlockPos p : voile) {
			world.spawnParticles(LimbesParticles.ECLAT_DE_NACRE, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.02);
			world.spawnParticles(LimbesParticles.ETOILE_ROYALE, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.01);
		}
		if (activator != null) {
			LimbesPortal.onPorteReveillee(activator, origin);
		}
		return voile;
	}

	/** Blocs de porte connectés orthogonalement (le voile), borné à {@link #MAX_VOILE}. */
	public static List<BlockPos> voileConnecte(World world, BlockPos origin) {
		List<BlockPos> result = new ArrayList<>();
		if (!(world.getBlockState(origin).getBlock() instanceof PorteDesLimbesBlock)) {
			return result;
		}
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(origin);
		seen.add(origin);
		while (!queue.isEmpty() && result.size() < MAX_VOILE) {
			BlockPos p = queue.poll();
			result.add(p);
			for (Direction d : Direction.values()) {
				BlockPos n = p.offset(d);
				if (seen.add(n) && world.getBlockState(n).getBlock() instanceof PorteDesLimbesBlock) {
					queue.add(n);
				}
			}
		}
		return result;
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!state.get(ACTIVE) || world.isClient) {
			return;
		}
		if (entity instanceof ServerPlayerEntity player && !player.hasPortalCooldown() && player.isAlive()) {
			LimbesPortal.traverser(player, pos);
		}
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (!state.get(ACTIVE)) {
			if (random.nextInt(12) == 0) {
				world.addParticle(LimbesParticles.LUEUR, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
						pos.getZ() + random.nextDouble(), 0.0, 0.01, 0.0);
			}
			return;
		}
		if (random.nextInt(60) == 0) {
			world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, LimbesSounds.PORTE_AMBIANCE,
					SoundCategory.BLOCKS, 0.5F, 0.9F + random.nextFloat() * 0.2F, false);
		}
		for (int i = 0; i < 2; i++) {
			double x = pos.getX() + random.nextDouble();
			double y = pos.getY() + random.nextDouble();
			double z = pos.getZ() + random.nextDouble();
			double vx = (random.nextDouble() - 0.5) * 0.02;
			double vz = (random.nextDouble() - 0.5) * 0.02;
			world.addParticle(random.nextBoolean() ? LimbesParticles.ETOILE_ROYALE : LimbesParticles.LUEUR, x, y, z, vx, 0.02, vz);
		}
	}
}
