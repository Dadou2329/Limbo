package fr.leslimbes.block;

import fr.leslimbes.domaine.RelaisManager;
import fr.leslimbes.registry.LimbesBlockEntities;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.LimbesWorldState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Porte de relais : arche de pierre nacrée d'un relais royal. Éteinte tant que la salle des passages du
 * domaine n'est pas creusée ; éveillée ensuite, elle mène à chaque relais que le joueur a découvert.
 * Le bloc-entité porte l'identifiant du relais (défini dans les gabarits).
 */
public class PorteDeRelaisBlock extends HorizontalFacingBlock implements BlockEntityProvider {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty EVEILLEE = BooleanProperty.of("eveillee");
	public static final com.mojang.serialization.MapCodec<PorteDeRelaisBlock> CODEC = createCodec(PorteDeRelaisBlock::new);

	public PorteDeRelaisBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(EVEILLEE, false));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, EVEILLEE);
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
		return new PorteDeRelaisBlockEntity(pos, state);
	}

	@Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		world.scheduleBlockTick(pos, this, 20);
	}

	/** Suit l'éveil du réseau (drapeau du monde) : la porte s'allume quand la salle des passages existe. */
	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		boolean eveillee = LimbesWorldState.get(world.getServer()).aDrapeau(RelaisManager.RESEAU_EVEILLE);
		if (eveillee != state.get(EVEILLEE)) {
			world.setBlockState(pos, state.with(EVEILLEE, eveillee), Block.NOTIFY_ALL);
			if (eveillee) {
				world.playSound(null, pos, LimbesSounds.PORTE_EVEIL, SoundCategory.BLOCKS, 0.8F, 1.0F);
			}
		}
		world.scheduleBlockTick(pos, this, 200 + random.nextInt(40));
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}
		if (player instanceof ServerPlayerEntity sp) {
			String relais = world.getBlockEntity(pos) instanceof PorteDeRelaisBlockEntity be ? be.getRelais() : "";
			RelaisManager.utiliserPorte(sp, pos, relais);
		}
		return ActionResult.CONSUME;
	}

	/** Identifiant du relais que cette porte représente (vide pour une porte posée à la main). */
	public static final class PorteDeRelaisBlockEntity extends BlockEntity {
		private String relais = "";

		public PorteDeRelaisBlockEntity(BlockPos pos, BlockState state) {
			super(LimbesBlockEntities.PORTE_DE_RELAIS, pos, state);
		}

		public String getRelais() {
			return relais;
		}

		public void setRelais(String relais) {
			this.relais = relais;
			markDirty();
		}

		@Override
		protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
			super.readNbt(nbt, registryLookup);
			relais = nbt.getString("relais");
		}

		@Override
		protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
			super.writeNbt(nbt, registryLookup);
			nbt.putString("relais", relais);
		}
	}
}
