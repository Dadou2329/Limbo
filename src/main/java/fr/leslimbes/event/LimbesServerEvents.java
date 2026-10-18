package fr.leslimbes.event;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.npc.EssentialNpcs;
import fr.leslimbes.npc.dialogue.DialogueSession;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.power.ArsenalDuSouverain;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.dimension.BrumesProtection;
import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

/** Branchement des événements serveur sur les systèmes du mod. */
public final class LimbesServerEvents {
	public static void init() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			PremiereConnexion.accueillir(player);
			PlayerDataSync.envoyer(player);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			DialogueSession.onDeconnexion(player.getUuid());
			Abilities.onDeconnexion(player.getUuid());
			ArsenalDuSouverain.onDeconnexion(player.getUuid());
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PlayerDataSync.envoyer(newPlayer));
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> PlayerDataSync.envoyer(player));

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long time = server.getOverworld().getTime();
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				Abilities.tick(player);
				if ((time + player.getId()) % 20L == 0L) {
					QuestManager.tick(player);
				}
			}
		});
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (LimbesDimension.estDansLesLimbes(world)) {
				BrumesProtection.tick(world);
				EssentialNpcs.tick(world);
			}
		});

		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed) -> {
			if (entity instanceof ServerPlayerEntity player) {
				String id = Registries.ENTITY_TYPE.getId(killed.getType()).toString();
				QuestManager.signaler(player, QuestSignal.vaincre(id));
			}
		});
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (player instanceof ServerPlayerEntity serverPlayer && hand == net.minecraft.util.Hand.MAIN_HAND) {
				ArsenalDuSouverain.onCoup(serverPlayer, entity);
			}
			return ActionResult.PASS;
		});
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (player instanceof ServerPlayerEntity serverPlayer && hand == net.minecraft.util.Hand.MAIN_HAND) {
				String id = Registries.BLOCK.getId(world.getBlockState(hitResult.getBlockPos()).getBlock()).toString();
				QuestManager.signaler(serverPlayer, QuestSignal.interagir(id, hitResult.getBlockPos()));
			}
			return ActionResult.PASS;
		});
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> ArsenalDuSouverain.dissiperSiInvoquee(entity));
	}

	private LimbesServerEvents() {
	}
}
