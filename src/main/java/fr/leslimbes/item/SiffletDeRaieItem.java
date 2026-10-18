package fr.leslimbes.item;

import fr.leslimbes.entity.RaieCelesteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Sifflet de raie : rappelle la raie céleste liée au joueur (ou la recrée si elle a disparu). */
public class SiffletDeRaieItem extends Item {
	public SiffletDeRaieItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient && user instanceof ServerPlayerEntity player) {
			RaieCelesteEntity.rappeler(player);
			player.getItemCooldownManager().set(this, 200);
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.les_limbes.sifflet_de_raie.description").formatted(Formatting.GRAY));
	}
}
