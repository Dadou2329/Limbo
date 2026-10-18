package fr.leslimbes.power;

import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Branche « Arsenal du Souverain » : invocation de l'Épée du Souverain et enchaînement à trois coups.
 *
 * <p>L'épée est un objet invoqué : elle disparaît si elle est lâchée et se rappelle avec le même
 * pouvoir. Le troisième coup d'un enchaînement rapide frappe en arc autour de la cible.</p>
 */
public final class ArsenalDuSouverain {
	private static final int FENETRE_COMBO_TICKS = 30;
	private static final float DEGATS_ARC = 5.0F;
	private static final double RAYON_ARC = 3.0;
	private static final Map<UUID, Combo> COMBOS = new HashMap<>();

	private record Combo(int etape, long dernierCoup) {
	}

	public static boolean invoquerOuRappeler(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		int slot = trouverEpee(player);
		if (slot >= 0) {
			player.getInventory().removeStack(slot);
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.rappel").formatted(Formatting.AQUA), true);
			world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_EPEE, SoundCategory.PLAYERS, 0.8F, 0.7F);
			Abilities.effet(world, "epee_rappel", player.getPos().add(0, 1.0, 0), player.getRotationVector(), 0);
			return true;
		}
		ItemStack epee = new ItemStack(LimbesItems.EPEE_DU_SOUVERAIN);
		int affutage = fr.leslimbes.item.EpeeDuSouverainItem.affutage(player);
		if (affutage > 0) {
			fr.leslimbes.item.EpeeDuSouverainItem.appliquerAffutage(epee, affutage);
		}
		int main = player.getInventory().selectedSlot;
		ItemStack enMain = player.getInventory().getStack(main);
		if (enMain.isEmpty()) {
			player.getInventory().setStack(main, epee);
		} else if (!player.getInventory().insertStack(epee)) {
			player.sendMessage(Text.translatable("message.les_limbes.arsenal.inventaire_plein").formatted(Formatting.RED), true);
			return false;
		}
		player.sendMessage(Text.translatable("message.les_limbes.arsenal.invocation").formatted(Formatting.GOLD), true);
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_EPEE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		Abilities.effet(world, "epee_invocation", player.getPos().add(0, 1.0, 0), player.getRotationVector(), 0);
		return true;
	}

	private static int trouverEpee(PlayerEntity player) {
		for (int i = 0; i < player.getInventory().size(); i++) {
			if (player.getInventory().getStack(i).isOf(LimbesItems.EPEE_DU_SOUVERAIN)) {
				return i;
			}
		}
		return -1;
	}

	/** Appelé lorsqu'un joueur frappe une entité avec l'épée (serveur). */
	public static void onCoup(ServerPlayerEntity player, Entity cible) {
		if (!player.getMainHandStack().isOf(LimbesItems.EPEE_DU_SOUVERAIN) || !(cible instanceof LivingEntity vivant)) {
			return;
		}
		ServerWorld world = player.getServerWorld();
		long now = world.getTime();
		Combo combo = COMBOS.get(player.getUuid());
		int etape = (combo == null || now - combo.dernierCoup() > FENETRE_COMBO_TICKS) ? 1 : combo.etape() + 1;
		if (etape >= 3) {
			etape = 0;
			frapperEnArc(player, vivant, world);
		}
		COMBOS.put(player.getUuid(), new Combo(etape, now));
	}

	private static void frapperEnArc(ServerPlayerEntity player, LivingEntity cible, ServerWorld world) {
		Vec3d centre = cible.getPos();
		Box zone = new Box(centre.x - RAYON_ARC, centre.y - 1, centre.z - RAYON_ARC, centre.x + RAYON_ARC, centre.y + 2, centre.z + RAYON_ARC);
		List<LivingEntity> touches = world.getEntitiesByClass(LivingEntity.class, zone,
				e -> e != player && e.isAlive() && !(e instanceof PlayerEntity) && e.canTakeDamage() && !(e instanceof fr.leslimbes.npc.HabitantEntity));
		for (LivingEntity e : touches) {
			e.damage(world.getDamageSources().playerAttack(player), DEGATS_ARC);
			Vec3d pousse = e.getPos().subtract(player.getPos()).normalize();
			e.takeKnockback(0.6, -pousse.x, -pousse.z);
		}
		world.playSound(null, cible.getBlockPos(), LimbesSounds.POUVOIR_EPEE_FINAL, SoundCategory.PLAYERS, 1.0F, 1.0F);
		Abilities.effet(world, "epee_arc", player.getPos().add(0, 1.2, 0), player.getRotationVector(), touches.size());
	}

	/** Une épée invoquée lâchée au sol se dissipe. */
	public static boolean dissiperSiInvoquee(Entity entity) {
		if (entity instanceof ItemEntity item && item.getStack().isOf(LimbesItems.EPEE_DU_SOUVERAIN)) {
			if (entity.getWorld() instanceof ServerWorld world) {
				Abilities.effet(world, "epee_rappel", entity.getPos(), Vec3d.ZERO, 0);
			}
			entity.discard();
			return true;
		}
		return false;
	}

	public static void onDeconnexion(UUID uuid) {
		COMBOS.remove(uuid);
	}

	private ArsenalDuSouverain() {
	}
}
