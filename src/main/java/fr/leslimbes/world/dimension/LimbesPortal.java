package fr.leslimbes.world.dimension;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;

import java.util.Optional;

/**
 * Voyages entre l'Overworld et les Limbes. Toute décision est prise ici, côté serveur.
 *
 * <ul>
 *   <li>Overworld → Limbes : arrivée au Sanctuaire (position fixe), point de retour mémorisé.</li>
 *   <li>Limbes → Overworld : retour exactement là où le joueur se tenait avant son départ, sinon au point d'apparition.</li>
 * </ul>
 */
public final class LimbesPortal {
	public static final TagKey<Structure> VESTIGES = TagKey.of(RegistryKeys.STRUCTURE, LesLimbes.id("vestiges"));
	private static final int COOLDOWN_TICKS = 60;

	public static void traverser(ServerPlayerEntity player, BlockPos portePos) {
		ServerWorld origine = player.getServerWorld();
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (LimbesDimension.estDansLesLimbes(origine)) {
			retournerDansLOverworld(player, data);
		} else {
			entrerDansLesLimbes(player, data, origine, portePos);
		}
	}

	private static void entrerDansLesLimbes(ServerPlayerEntity player, PlayerLimbesData data, ServerWorld origine, BlockPos portePos) {
		ServerWorld limbes = LimbesDimension.monde(player.server);
		if (limbes == null) {
			player.sendMessage(Text.translatable("message.les_limbes.porte.indisponible").formatted(Formatting.RED), false);
			return;
		}
		// Point de retour : la case où le joueur se tenait (toujours libre, puisqu'il y était).
		data.retourOverworld = Optional.of(GlobalPos.create(origine.getRegistryKey(), positionDeRetour(player, portePos)));
		boolean premiereFois = !data.aDrapeau("arrivee");
		origine.playSound(null, player.getBlockPos(), LimbesSounds.PORTE_PASSAGE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		player.setPortalCooldown(COOLDOWN_TICKS);
		player.teleportTo(new TeleportTarget(limbes, LimbesSites.ARRIVEE, Vec3d.ZERO, 180.0F, 0.0F, TeleportTarget.NO_OP));
		player.setPortalCooldown(COOLDOWN_TICKS);
		player.fallDistance = 0.0F;
		limbes.playSound(null, BlockPos.ofFloored(LimbesSites.ARRIVEE), LimbesSounds.PORTE_PASSAGE, SoundCategory.PLAYERS, 1.0F, 1.1F);
		data.lieuSur = Optional.of(GlobalPos.create(LimbesDimension.MONDE, BlockPos.ofFloored(LimbesSites.ARRIVEE)));
		if (premiereFois) {
			data.poserDrapeau("arrivee");
			titre(player, Text.translatable("titre.les_limbes.arrivee").formatted(Formatting.GOLD),
					Text.translatable("titre.les_limbes.arrivee.sous_titre").formatted(Formatting.AQUA));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, false, false, false));
		}
		QuestManager.signaler(player, QuestSignal.drapeau("arrivee"));
		PlayerDataSync.envoyer(player);
	}

	private static void retournerDansLOverworld(ServerPlayerEntity player, PlayerLimbesData data) {
		ServerWorld cible = null;
		Vec3d pos = null;
		if (data.retourOverworld.isPresent()) {
			GlobalPos gp = data.retourOverworld.get();
			cible = player.server.getWorld(gp.dimension());
			if (cible != null) {
				pos = Vec3d.ofBottomCenter(gp.pos());
			}
		}
		if (cible == null) {
			cible = player.server.getWorld(World.OVERWORLD);
			BlockPos spawn = cible.getSpawnPos();
			int y = cible.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
			pos = new Vec3d(spawn.getX() + 0.5, y, spawn.getZ() + 0.5);
		}
		player.getServerWorld().playSound(null, player.getBlockPos(), LimbesSounds.PORTE_PASSAGE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		player.setPortalCooldown(COOLDOWN_TICKS);
		player.teleportTo(new TeleportTarget(cible, pos, Vec3d.ZERO, player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
		player.setPortalCooldown(COOLDOWN_TICKS);
		player.fallDistance = 0.0F;
		cible.playSound(null, BlockPos.ofFloored(pos), LimbesSounds.PORTE_PASSAGE, SoundCategory.PLAYERS, 1.0F, 0.9F);
		QuestManager.signaler(player, QuestSignal.drapeau("retour_overworld"));
		PlayerDataSync.envoyer(player);
	}

	/** Case de retour : la position du joueur, reculée d'un bloc par rapport à la porte si possible. */
	private static BlockPos positionDeRetour(ServerPlayerEntity player, BlockPos portePos) {
		BlockPos ici = player.getBlockPos();
		int dx = Integer.signum(ici.getX() - portePos.getX());
		int dz = Integer.signum(ici.getZ() - portePos.getZ());
		BlockPos recul = ici.add(dx, 0, dz);
		World world = player.getWorld();
		if (dx != 0 || dz != 0) {
			if (estLibre(world, recul)) {
				return recul;
			}
		}
		return ici;
	}

	private static boolean estLibre(World world, BlockPos pos) {
		BlockState bas = world.getBlockState(pos);
		BlockState haut = world.getBlockState(pos.up());
		BlockState sol = world.getBlockState(pos.down());
		return bas.getCollisionShape(world, pos).isEmpty() && haut.getCollisionShape(world, pos.up()).isEmpty()
				&& !sol.getCollisionShape(world, pos.down()).isEmpty();
	}

	/** Appelé lorsqu'un joueur réveille une porte dormante. */
	public static void onPorteReveillee(ServerPlayerEntity player, BlockPos pos) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		data.poserDrapeau("porte_reveillee");
		player.sendMessage(Text.translatable("message.les_limbes.porte.reveillee").formatted(Formatting.AQUA), false);
		QuestManager.signaler(player, QuestSignal.drapeau("porte_reveillee"));
		PlayerDataSync.envoyer(player);
	}

	/** Recherche le vestige le plus proche (position de sa structure), sans charger de chunks. */
	public static Optional<BlockPos> localiserVestige(ServerWorld world, BlockPos depuis) {
		BlockPos found = world.locateStructure(VESTIGES, depuis, 48, false);
		return Optional.ofNullable(found);
	}

	public static void titre(ServerPlayerEntity player, Text titre, Text sousTitre) {
		player.networkHandler.sendPacket(new TitleFadeS2CPacket(15, 60, 20));
		player.networkHandler.sendPacket(new SubtitleS2CPacket(sousTitre));
		player.networkHandler.sendPacket(new TitleS2CPacket(titre));
	}

	private LimbesPortal() {
	}
}
