package fr.leslimbes.item;

import fr.leslimbes.entity.region.EclatProjectileEntity;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * Arc du Souverain : arme invoquée qui ne demande aucune flèche. Chaque tir est un éclat de lumière dont la force
 * dépend de la tension ; l'affûtage de la forge s'y ajoute.
 */
public class ArcDuSouverainItem extends BowItem {
	public ArcDuSouverainItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		user.setCurrentHand(hand);
		return TypedActionResult.consume(stack);
	}

	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!(user instanceof ServerPlayerEntity player) || !(world instanceof ServerWorld serverWorld)) {
			return;
		}
		int useTicks = this.getMaxUseTime(stack, user) - remainingUseTicks;
		float tension = getPullProgress(useTicks);
		if (tension < 0.1F) {
			return;
		}
		float degats = 2.0F + 6.0F * tension + EpeeDuSouverainItem.affutage(player);
		EclatProjectileEntity eclat = new EclatProjectileEntity(world, player, degats, null, 0, 0.3 * tension);
		eclat.setPosition(player.getX(), player.getEyeY() - 0.1, player.getZ());
		eclat.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, tension * 3.0F, 1.0F);
		world.spawnEntity(eclat);
		world.playSound(null, player.getBlockPos(), LimbesSounds.POUVOIR_ARC, SoundCategory.PLAYERS, 1.0F, 0.9F + tension * 0.4F);
		Abilities.effet(serverWorld, "arc_tir", player.getEyePos().add(player.getRotationVector().multiply(0.8)), player.getRotationVector(), (int) (tension * 10));
	}

	@Override
	public int getMaxUseTime(ItemStack stack, LivingEntity user) {
		return 72000;
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}
}
