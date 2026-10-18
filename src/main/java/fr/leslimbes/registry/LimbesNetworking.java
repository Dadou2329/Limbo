package fr.leslimbes.registry;

import fr.leslimbes.network.LimbesPayloads;
import fr.leslimbes.npc.dialogue.DialogueSession;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/** Enregistrement des paquets et des récepteurs serveur (bornes et validations incluses). */
public final class LimbesNetworking {
	public static void init() {
		PayloadTypeRegistry.playC2S().register(LimbesPayloads.UtiliserPouvoir.ID, LimbesPayloads.UtiliserPouvoir.CODEC);
		PayloadTypeRegistry.playC2S().register(LimbesPayloads.EquiperPouvoir.ID, LimbesPayloads.EquiperPouvoir.CODEC);
		PayloadTypeRegistry.playC2S().register(LimbesPayloads.ChoixDialogue.ID, LimbesPayloads.ChoixDialogue.CODEC);
		PayloadTypeRegistry.playC2S().register(LimbesPayloads.SuivreQuete.ID, LimbesPayloads.SuivreQuete.CODEC);
		PayloadTypeRegistry.playS2C().register(LimbesPayloads.SyncJoueur.ID, LimbesPayloads.SyncJoueur.CODEC);
		PayloadTypeRegistry.playS2C().register(LimbesPayloads.OuvrirDialogue.ID, LimbesPayloads.OuvrirDialogue.CODEC);
		PayloadTypeRegistry.playS2C().register(LimbesPayloads.FermerDialogue.ID, LimbesPayloads.FermerDialogue.CODEC);
		PayloadTypeRegistry.playS2C().register(LimbesPayloads.Effet.ID, LimbesPayloads.Effet.CODEC);
		PayloadTypeRegistry.playS2C().register(LimbesPayloads.NotificationQuete.ID, LimbesPayloads.NotificationQuete.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(LimbesPayloads.UtiliserPouvoir.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			int slot = payload.slot();
			if (slot < 0 || slot >= PlayerLimbesData.SLOTS) {
				return;
			}
			context.server().execute(() -> Abilities.utiliserSlot(player, slot));
		});
		ServerPlayNetworking.registerGlobalReceiver(LimbesPayloads.EquiperPouvoir.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			int slot = payload.slot();
			String pouvoir = payload.pouvoir();
			if (slot < 0 || slot >= PlayerLimbesData.SLOTS || pouvoir.length() > 64) {
				return;
			}
			context.server().execute(() -> {
				PlayerLimbesData data = PlayerLimbesData.of(player);
				if (data.equiper(slot, pouvoir)) {
					PlayerDataSync.envoyer(player);
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(LimbesPayloads.ChoixDialogue.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			int index = payload.index();
			if (index < -1 || index > 16) {
				return;
			}
			context.server().execute(() -> DialogueSession.choisir(player, index));
		});
		ServerPlayNetworking.registerGlobalReceiver(LimbesPayloads.SuivreQuete.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			String quete = payload.quete();
			if (quete.length() > 64) {
				return;
			}
			context.server().execute(() -> {
				PlayerLimbesData data = PlayerLimbesData.of(player);
				if (quete.isEmpty() || data.quetes.containsKey(quete)) {
					data.queteSuivie = quete;
					PlayerDataSync.envoyer(player);
				}
			});
		});
	}

	private LimbesNetworking() {
	}
}
