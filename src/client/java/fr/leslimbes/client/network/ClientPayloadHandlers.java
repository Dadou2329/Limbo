package fr.leslimbes.client.network;

import fr.leslimbes.client.ClientLimbesData;
import fr.leslimbes.client.fx.EffetsClient;
import fr.leslimbes.client.screen.DialogueScreen;
import fr.leslimbes.network.LimbesPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/** Réception des paquets serveur → client. Aucune décision de jeu ici : affichage et effets seulement. */
public final class ClientPayloadHandlers {
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.SyncJoueur.ID, (payload, context) -> ClientLimbesData.recevoir(payload.donnees()));
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.OuvrirDialogue.ID, (payload, context) -> {
			MinecraftClient client = context.client();
			if (client.currentScreen instanceof DialogueScreen screen) {
				screen.mettreAJour(payload.contenu());
			} else {
				client.setScreen(new DialogueScreen(payload.contenu()));
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.FermerDialogue.ID, (payload, context) -> {
			MinecraftClient client = context.client();
			if (client.currentScreen instanceof DialogueScreen screen) {
				screen.fermerSansPrevenir();
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.Effet.ID, (payload, context) ->
				EffetsClient.jouer(context.client(), payload.type(), payload.pos(), payload.direction(), payload.donnee()));
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.NotificationQuete.ID, (payload, context) ->
				ClientLimbesData.notifier(payload.type(), payload.texte()));
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.OuvrirChantier.ID, (payload, context) -> {
			MinecraftClient client = context.client();
			if (client.currentScreen instanceof fr.leslimbes.client.screen.ChantierScreen screen) {
				screen.mettreAJour(payload.contenu());
			} else {
				client.setScreen(new fr.leslimbes.client.screen.ChantierScreen(payload.contenu()));
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.ApercuProjet.ID, (payload, context) ->
				fr.leslimbes.client.hud.ApercuChantier.recevoir(payload.contenu()));
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.OuvrirRelais.ID, (payload, context) ->
				context.client().setScreen(new fr.leslimbes.client.screen.RelaisScreen(payload.contenu())));
		ClientPlayNetworking.registerGlobalReceiver(LimbesPayloads.SyncLieux.ID, (payload, context) -> ClientLimbesData.recevoirLieux(payload.contenu()));
	}

	private ClientPayloadHandlers() {
	}
}
