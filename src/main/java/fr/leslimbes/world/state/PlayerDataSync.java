package fr.leslimbes.world.state;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.network.LimbesPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;

/** Envoi des données du joueur vers son client (affichage de la majesté, du journal, des pouvoirs). */
public final class PlayerDataSync {
	public static void envoyer(ServerPlayerEntity player) {
		fr.leslimbes.registry.LimbesAttachments.publier(player);
		if (!ServerPlayNetworking.canSend(player, LimbesPayloads.SyncJoueur.ID)) {
			return;
		}
		PlayerLimbesData data = PlayerLimbesData.of(player);
		NbtElement nbt = PlayerLimbesData.CODEC.encodeStart(NbtOps.INSTANCE, data).resultOrPartial(LesLimbes.LOGGER::error).orElse(null);
		if (nbt instanceof NbtCompound compound) {
			ServerPlayNetworking.send(player, new LimbesPayloads.SyncJoueur(compound));
		}
	}

	private PlayerDataSync() {
	}
}
