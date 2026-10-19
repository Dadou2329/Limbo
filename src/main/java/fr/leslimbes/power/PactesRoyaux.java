package fr.leslimbes.power;

import fr.leslimbes.entity.region.MontureEntity;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

/**
 * Branche « Pactes royaux » : le lien avec les montures. L'appel fait venir la monture active où que l'on
 * soit dans les Limbes ; le pacte de combat la rend plus forte et la soigne.
 */
public final class PactesRoyaux {
	public static boolean appelDeMonture(ServerPlayerEntity player) {
		if (MontureEntity.cleActive(fr.leslimbes.world.state.PlayerLimbesData.of(player)) == null) {
			player.sendMessage(Text.translatable("message.les_limbes.monture.aucune").formatted(Formatting.RED), true);
			return false;
		}
		MontureEntity.rappelerActive(player);
		return true;
	}

	public static boolean pacteDeCombat(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		TameableEntity monture = null;
		if (player.getVehicle() instanceof TameableEntity t && t.isTamed()) {
			monture = t;
		} else {
			List<TameableEntity> proches = world.getEntitiesByClass(TameableEntity.class, player.getBoundingBox().expand(16.0),
					t -> t.isAlive() && t.isTamed() && player.getUuid().equals(t.getOwnerUuid()));
			monture = proches.stream().min(Comparator.comparingDouble(t -> t.squaredDistanceTo(player))).orElse(null);
		}
		if (monture == null) {
			player.sendMessage(Text.translatable("message.les_limbes.pactes.aucune_monture").formatted(Formatting.RED), true);
			return false;
		}
		monture.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 400, 0), player);
		monture.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0), player);
		monture.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 0), player);
		monture.heal(10.0F);
		if (monture instanceof MontureEntity m) {
			m.setEndurance(MontureEntity.ENDURANCE_MAX);
		}
		world.playSound(null, monture.getBlockPos(), LimbesSounds.RAIE_PACTE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		Abilities.effet(world, "pacte_combat", monture.getPos().add(0, monture.getHeight() * 0.5, 0), Vec3d.ZERO, 0);
		player.sendMessage(Text.translatable("message.les_limbes.pactes.combat", monture.getDisplayName()).formatted(Formatting.GOLD), true);
		return true;
	}

	private PactesRoyaux() {
	}
}
