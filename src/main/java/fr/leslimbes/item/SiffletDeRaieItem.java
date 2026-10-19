package fr.leslimbes.item;

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
			if (player.isSneaking()) {
				changerDeMonture(player);
			} else {
				fr.leslimbes.entity.region.MontureEntity.rappelerActive(player);
				player.getItemCooldownManager().set(this, 200);
			}
		}
		return TypedActionResult.success(stack, world.isClient);
	}

	/** Accroupi : la monture suivante parmi celles liées (ordre alphabétique des clés), annoncée en barre d'action. */
	private static void changerDeMonture(ServerPlayerEntity player) {
		fr.leslimbes.world.state.PlayerLimbesData data = fr.leslimbes.world.state.PlayerLimbesData.of(player);
		java.util.List<String> cles = data.montures.keySet().stream().filter(k -> !k.equals(fr.leslimbes.entity.region.MontureEntity.CLE_ACTIVE)).sorted().toList();
		if (cles.isEmpty()) {
			player.sendMessage(Text.translatable("message.les_limbes.monture.aucune").formatted(Formatting.RED), true);
			return;
		}
		String actuelle = fr.leslimbes.entity.region.MontureEntity.cleActive(data);
		int i = actuelle == null ? -1 : cles.indexOf(actuelle);
		String suivante = cles.get((i + 1) % cles.size());
		data.montures.put(fr.leslimbes.entity.region.MontureEntity.CLE_ACTIVE, suivante);
		fr.leslimbes.world.state.PlayerDataSync.envoyer(player);
		player.sendMessage(Text.translatable("message.les_limbes.monture.choisie", Text.translatable("entity.les_limbes." + suivante)), true);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.les_limbes.sifflet_de_raie.description").formatted(Formatting.GRAY));
	}
}
