package fr.leslimbes.block;

import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.LimbesWorldState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Meule du moulin de Clairval : quand le moulin tourne, un blé donne un sac de farine. */
public class MeuleDeMoulinBlock extends Block {
	public static final String DRAPEAU_MONDE = "moulin_repare";

	public MeuleDeMoulinBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!stack.isOf(Items.WHEAT)) {
			return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (world.isClient) {
			return ItemActionResult.SUCCESS;
		}
		if (!LimbesWorldState.get(world.getServer()).aDrapeau(DRAPEAU_MONDE)) {
			player.sendMessage(Text.translatable("message.les_limbes.moulin.immobile").formatted(Formatting.GRAY), true);
			return ItemActionResult.CONSUME;
		}
		int n = player.isSneaking() ? stack.getCount() : 1;
		if (!player.isCreative()) {
			stack.decrement(n);
		}
		ItemStack farine = new ItemStack(LimbesItems.FARINE, n);
		if (!player.giveItemStack(farine)) {
			player.dropItem(farine, false);
		}
		world.playSound(null, pos, LimbesSounds.MOULIN_GRINCE, SoundCategory.BLOCKS, 0.7F, 1.3F);
		if (player instanceof ServerPlayerEntity sp) {
			fr.leslimbes.quest.QuestManager.signaler(sp, fr.leslimbes.quest.QuestSignal.drapeau("farine_moulue"));
		}
		return ItemActionResult.CONSUME;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient) {
			boolean repare = LimbesWorldState.get(world.getServer()).aDrapeau(DRAPEAU_MONDE);
			player.sendMessage(Text.translatable(repare ? "message.les_limbes.moulin.pret" : "message.les_limbes.moulin.immobile").formatted(Formatting.GRAY), true);
		}
		return ActionResult.SUCCESS;
	}
}
