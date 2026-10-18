package fr.leslimbes.item;

import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.dimension.LimbesPortal;
import fr.leslimbes.world.dimension.LimbesSites;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Boussole nacrée : indique en barre d'action la direction et la distance du prochain lieu utile.
 * Dans l'Overworld : le vestige le plus proche. Dans les Limbes : le lieu de la quête suivie, sinon le Sanctuaire.
 */
public class BoussoleNacreeItem extends Item {
	public BoussoleNacreeItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (user instanceof ServerPlayerEntity player) {
			PlayerLimbesData data = PlayerLimbesData.of(player);
			data.vestigeConnu = Optional.empty();
			indiquer(player, data, true);
		}
		return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!selected || world.isClient() || !(entity instanceof ServerPlayerEntity player) || world.getTime() % 20L != 0L) {
			return;
		}
		indiquer(player, PlayerLimbesData.of(player), false);
	}

	private static void indiquer(ServerPlayerEntity player, PlayerLimbesData data, boolean force) {
		Optional<Cible> cible = cible(player, data, force);
		if (cible.isEmpty()) {
			player.sendMessage(Text.translatable("message.les_limbes.boussole.aucune"), true);
			return;
		}
		BlockPos pos = cible.get().pos();
		double dx = pos.getX() + 0.5 - player.getX();
		double dz = pos.getZ() + 0.5 - player.getZ();
		int distance = (int) Math.sqrt(dx * dx + dz * dz);
		Text direction = Text.translatable("direction.les_limbes." + direction(dx, dz));
		player.sendMessage(Text.translatable("message.les_limbes.boussole.indication", cible.get().nom(), distance, direction)
				.formatted(Formatting.AQUA), true);
	}

	private static Optional<Cible> cible(ServerPlayerEntity player, PlayerLimbesData data, boolean force) {
		if (LimbesDimension.estDansLesLimbes(player.getServerWorld())) {
			Optional<Cible> lieuQuete = data.lieuQueteSuivie().map(l -> new Cible(l.nom(), l.pos()));
			if (lieuQuete.isPresent()) {
				return lieuQuete;
			}
			return Optional.of(new Cible(Text.translatable("lieu.les_limbes.sanctuaire"), LimbesSites.SANCTUAIRE));
		}
		if (player.getServerWorld().getRegistryKey() != World.OVERWORLD) {
			return Optional.empty();
		}
		if (data.vestigeConnu.isEmpty() || force) {
			data.vestigeConnu = LimbesPortal.localiserVestige(player.getServerWorld(), player.getBlockPos());
		}
		return data.vestigeConnu.map(p -> new Cible(Text.translatable("lieu.les_limbes.vestige"), p));
	}

	/** Point cardinal (8 directions) du vecteur (dx, dz) ; +z = sud, +x = est. */
	public static String direction(double dx, double dz) {
		double angle = Math.toDegrees(Math.atan2(dx, -dz)); // 0 = nord, 90 = est
		if (angle < 0) {
			angle += 360;
		}
		String[] noms = {"nord", "nord_est", "est", "sud_est", "sud", "sud_ouest", "ouest", "nord_ouest"};
		return noms[(int) Math.round(angle / 45.0) % 8];
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.les_limbes.boussole_nacree.description").formatted(Formatting.GRAY));
	}

	private record Cible(Text nom, BlockPos pos) {
	}
}
