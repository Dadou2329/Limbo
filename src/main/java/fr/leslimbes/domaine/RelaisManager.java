package fr.leslimbes.domaine;

import fr.leslimbes.block.PorteDeRelaisBlock;
import fr.leslimbes.network.LimbesPayloads;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.LimbesWorldState;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Réseau des passages : les relais se découvrent en voyage (porte touchée ou lieu découvert) ; une
 * fois la salle des passages du domaine creusée (drapeau du monde {@code domaine_passages}), toute
 * porte de relais mène à n'importe quel relais connu du joueur. Destination vérifiée : sol porteur,
 * espace libre, chunk chargé ; jamais dans un bloc ni au-dessus du vide.
 */
public final class RelaisManager {
	public static final String RESEAU_EVEILLE = "domaine_passages";
	private static final int PORTEE_PORTE = 6;
	private static final int DELAI_TICKS = 60;

	/** Découvre un relais pour un joueur. @return true si nouveau. */
	public static boolean decouvrir(ServerPlayerEntity player, String relaisId, boolean annoncer) {
		RelaisDefinition def = DomaineLoader.relais(relaisId);
		if (def == null) {
			return false;
		}
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (!data.poserDrapeau(def.drapeau())) {
			return false;
		}
		if (annoncer) {
			player.sendMessage(Text.translatable("message.les_limbes.relais.decouvert", def.nom()).formatted(Formatting.LIGHT_PURPLE), false);
		}
		QuestManager.signaler(player, QuestSignal.drapeau(def.drapeau()));
		PlayerDataSync.envoyer(player);
		return true;
	}

	public static boolean reseauEveille(ServerPlayerEntity player) {
		return LimbesWorldState.get(player.server).aDrapeau(RESEAU_EVEILLE);
	}

	/** Interaction avec une porte de relais : découverte, puis ouverture du choix si le réseau est éveillé. */
	public static void utiliserPorte(ServerPlayerEntity player, BlockPos porte, String relaisId) {
		if (!relaisId.isEmpty()) {
			decouvrir(player, relaisId, true);
		}
		if (!reseauEveille(player)) {
			player.sendMessage(Text.translatable("message.les_limbes.relais.eteinte").formatted(Formatting.GRAY), true);
			return;
		}
		ouvrir(player, relaisId);
	}

	/** Envoie au client la liste des relais connus (hors relais courant). */
	public static void ouvrir(ServerPlayerEntity player, String relaisCourant) {
		if (!ServerPlayNetworking.canSend(player, LimbesPayloads.OuvrirRelais.ID)) {
			return;
		}
		PlayerLimbesData data = PlayerLimbesData.of(player);
		NbtCompound contenu = new NbtCompound();
		contenu.putString("courant", relaisCourant);
		NbtList liste = new NbtList();
		for (RelaisDefinition def : DomaineLoader.relaisTries()) {
			if (!data.aDrapeau(def.drapeau())) {
				continue;
			}
			NbtCompound r = new NbtCompound();
			r.putString("id", def.id());
			r.putString("nom", def.nom());
			r.putString("description", def.description());
			r.putIntArray("pos", new int[] {def.pos().getX(), def.pos().getY(), def.pos().getZ()});
			r.putBoolean("ici", def.id().equals(relaisCourant));
			liste.add(r);
		}
		contenu.put("relais", liste);
		ServerPlayNetworking.send(player, new LimbesPayloads.OuvrirRelais(contenu));
	}

	/** Voyage demandé par le client : toutes les vérifications sont refaites ici. */
	public static boolean voyager(ServerPlayerEntity player, String relaisId) {
		RelaisDefinition def = DomaineLoader.relais(relaisId);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (def == null || !data.aDrapeau(def.drapeau())) {
			player.sendMessage(Text.translatable("message.les_limbes.relais.inconnu").formatted(Formatting.RED), true);
			return false;
		}
		if (!reseauEveille(player)) {
			player.sendMessage(Text.translatable("message.les_limbes.relais.eteinte").formatted(Formatting.GRAY), true);
			return false;
		}
		if (!presDUnePorte(player)) {
			player.sendMessage(Text.translatable("message.les_limbes.relais.sans_porte").formatted(Formatting.RED), true);
			return false;
		}
		long now = player.getServerWorld().getTime();
		int dernier = data.compteurs.getOrDefault("relais_dernier_voyage", Integer.MIN_VALUE);
		if (dernier != Integer.MIN_VALUE && now - dernier < DELAI_TICKS && now >= dernier) {
			return false;
		}
		ServerWorld cible = player.server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(def.dimension())));
		if (cible == null) {
			return false;
		}
		Vec3d destination = destinationSure(cible, def.pos());
		if (destination == null) {
			player.sendMessage(Text.translatable("message.les_limbes.relais.obstrue", def.nom()).formatted(Formatting.RED), true);
			return false;
		}
		ServerWorld depart = player.getServerWorld();
		Vec3d regard = player.getRotationVector();
		Abilities.effet(depart, "porte_depart", player.getPos().add(0, 1.0, 0), regard, 0);
		depart.playSound(null, player.getBlockPos(), LimbesSounds.PORTE_PASSAGE, SoundCategory.PLAYERS, 0.9F, 1.0F);
		player.teleportTo(new TeleportTarget(cible, destination, Vec3d.ZERO, player.getYaw(), player.getPitch(), TeleportTarget.NO_OP));
		player.fallDistance = 0.0F;
		Abilities.effet(cible, "porte_arrivee", destination.add(0, 1.0, 0), regard, 0);
		cible.playSound(null, BlockPos.ofFloored(destination), LimbesSounds.PORTE_PASSAGE, SoundCategory.PLAYERS, 0.9F, 0.9F);
		player.sendMessage(Text.translatable("message.les_limbes.relais.voyage", def.nom()).formatted(Formatting.LIGHT_PURPLE), true);
		data.compteurs.put("relais_dernier_voyage", (int) now);
		data.compteurs.merge("voyages_relais", 1, Integer::sum);
		PlayerDataSync.envoyer(player);
		return true;
	}

	private static boolean presDUnePorte(ServerPlayerEntity player) {
		BlockPos c = player.getBlockPos();
		for (BlockPos p : BlockPos.iterate(c.add(-PORTEE_PORTE, -3, -PORTEE_PORTE), c.add(PORTEE_PORTE, 3, PORTEE_PORTE))) {
			if (player.getServerWorld().getBlockState(p).getBlock() instanceof PorteDeRelaisBlock) {
				return true;
			}
		}
		return player.hasPermissionLevel(2) && player.isCreative();
	}

	/** Position debout sûre la plus proche de {@code pos} (chunk chargé à la demande, rayon 3, ±3 en hauteur). */
	@Nullable
	public static Vec3d destinationSure(ServerWorld world, BlockPos pos) {
		world.getChunk(pos);
		List<BlockPos> candidats = new ArrayList<>();
		candidats.add(pos);
		for (int r = 1; r <= 3; r++) {
			for (int dy = -3; dy <= 3; dy++) {
				for (int dx = -r; dx <= r; dx++) {
					for (int dz = -r; dz <= r; dz++) {
						if (Math.max(Math.abs(dx), Math.abs(dz)) == r) {
							candidats.add(pos.add(dx, dy, dz));
						}
					}
				}
			}
		}
		for (int dy = -3; dy <= 3; dy++) {
			if (dy != 0) {
				candidats.add(pos.add(0, dy, 0));
			}
		}
		for (BlockPos p : candidats) {
			if (estSure(world, p)) {
				return Vec3d.ofBottomCenter(p);
			}
		}
		return null;
	}

	private static boolean estSure(World world, BlockPos p) {
		return world.getBlockState(p).getCollisionShape(world, p).isEmpty() && world.getBlockState(p).getFluidState().isEmpty()
				&& world.getBlockState(p.up()).getCollisionShape(world, p.up()).isEmpty()
				&& !world.getBlockState(p.down()).getCollisionShape(world, p.down()).isEmpty();
	}

	private RelaisManager() {
	}
}
