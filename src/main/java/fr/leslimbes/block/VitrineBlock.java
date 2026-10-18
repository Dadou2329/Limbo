package fr.leslimbes.block;

import fr.leslimbes.registry.LimbesBlockEntities;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Vitrine de la galerie des merveilles : expose un objet (un seul) sur un socle de verre. Un objet posé
 * est rendu en train de tourner ; en le regardant, le joueur lit son nom, qui l'a exposé et son histoire
 * (textes {@code vitrine.les_limbes.<objet>} pour les objets des Limbes). Clic droit avec un objet : exposer ;
 * main vide : reprendre.
 */
public class VitrineBlock extends HorizontalFacingBlock implements BlockEntityProvider {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final com.mojang.serialization.MapCodec<VitrineBlock> CODEC = createCodec(VitrineBlock::new);
	private static final VoxelShape FORME = Block.createCuboidShape(1, 0, 1, 15, 16, 15);

	public VitrineBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getDefaultState().with(FACING, Direction.SOUTH));
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends HorizontalFacingBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return FORME;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new VitrineBlockEntity(pos, state);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof VitrineBlockEntity vitrine) || stack.isEmpty()) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!vitrine.getObjet().isEmpty()) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}
		ItemStack expose = stack.copyWithCount(1);
		if (!player.isCreative()) {
			stack.decrement(1);
		}
		vitrine.exposer(expose, player.getName().getString());
		world.playSound(null, pos, LimbesSounds.FRAGMENT, SoundCategory.BLOCKS, 0.6F, 1.4F);
		player.sendMessage(Text.translatable("message.les_limbes.vitrine.expose", expose.getName()).formatted(Formatting.AQUA), true);
		return ItemActionResult.CONSUME;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof VitrineBlockEntity vitrine) || vitrine.getObjet().isEmpty()) {
			return ActionResult.PASS;
		}
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}
		ItemStack objet = vitrine.retirer();
		if (!player.giveItemStack(objet)) {
			player.dropItem(objet, false);
		}
		world.playSound(null, pos, LimbesSounds.FRAGMENT, SoundCategory.BLOCKS, 0.5F, 0.8F);
		return ActionResult.CONSUME;
	}

	@Override
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof VitrineBlockEntity vitrine && !vitrine.getObjet().isEmpty()) {
			ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, vitrine.getObjet());
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	/** Objet exposé, nom de l'exposant et moment de l'exposition ; synchronisé au client pour le rendu. */
	public static final class VitrineBlockEntity extends BlockEntity {
		private ItemStack objet = ItemStack.EMPTY;
		private String exposant = "";
		private long depuis;

		public VitrineBlockEntity(BlockPos pos, BlockState state) {
			super(LimbesBlockEntities.VITRINE, pos, state);
		}

		public ItemStack getObjet() {
			return objet;
		}

		public String getExposant() {
			return exposant;
		}

		public long getDepuis() {
			return depuis;
		}

		public void exposer(ItemStack stack, String nom) {
			this.objet = stack;
			this.exposant = nom;
			this.depuis = world == null ? 0 : world.getTime();
			synchroniser();
		}

		public ItemStack retirer() {
			ItemStack s = objet;
			objet = ItemStack.EMPTY;
			exposant = "";
			synchroniser();
			return s;
		}

		private void synchroniser() {
			markDirty();
			if (world != null) {
				world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
			}
		}

		@Override
		protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
			super.readNbt(nbt, registryLookup);
			objet = nbt.contains("objet") ? ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("objet")) : ItemStack.EMPTY;
			exposant = nbt.getString("exposant");
			depuis = nbt.getLong("depuis");
		}

		@Override
		protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
			super.writeNbt(nbt, registryLookup);
			if (!objet.isEmpty()) {
				nbt.put("objet", objet.encode(registryLookup));
			}
			nbt.putString("exposant", exposant);
			nbt.putLong("depuis", depuis);
		}

		@Override
		public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
			return createNbt(registryLookup);
		}

		@Nullable
		@Override
		public Packet<ClientPlayPacketListener> toUpdatePacket() {
			return BlockEntityUpdateS2CPacket.create(this);
		}
	}
}
