package fr.leslimbes.power;

import fr.leslimbes.network.LimbesPayloads;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Registre des techniques royales et gestion des recharges (transitoires, non sauvegardées). */
public final class Abilities {
	private static final Map<String, Ability> REGISTRE = new LinkedHashMap<>();
	/** Recharges par joueur : id de pouvoir -> tick de disponibilité. */
	private static final Map<UUID, Map<String, Long>> RECHARGES = new HashMap<>();
	/** Régénération de majesté par tick. */
	public static final float REGEN_PAR_TICK = 0.4F;

	public static final Ability EPEE_DU_SOUVERAIN = register(new Ability("epee_du_souverain", "arsenal", 10.0F, 20, ArsenalDuSouverain::invoquerOuRappeler));
	public static final Ability PAS_COURT = register(new Ability("pas_court", "portes", 15.0F, 30, PortesDuPalais::pasCourt));
	public static final Ability DECRET_RECULEZ = register(new Ability("decret_reculez", "decrets", 20.0F, 100, DecretsRoyaux::reculez));

	private static Ability register(Ability ability) {
		REGISTRE.put(ability.id(), ability);
		return ability;
	}

	public static void init() {
	}

	@Nullable
	public static Ability get(String id) {
		return REGISTRE.get(id);
	}

	public static Map<String, Ability> tous() {
		return Collections.unmodifiableMap(REGISTRE);
	}

	/** Utilise le pouvoir équipé dans un emplacement (appelé depuis le réseau, déjà borné). */
	public static void utiliserSlot(ServerPlayerEntity player, int slot) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		String id = data.equipes.get(slot);
		if (id.isEmpty()) {
			player.sendMessage(Text.translatable("message.les_limbes.pouvoir.emplacement_vide", slot + 1), true);
			return;
		}
		utiliser(player, id);
	}

	public static boolean utiliser(ServerPlayerEntity player, String id) {
		Ability ability = get(id);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (ability == null || !data.possedePouvoir(id)) {
			return false;
		}
		if (!player.isAlive() || player.isSpectator()) {
			return false;
		}
		long now = player.getServerWorld().getTime();
		long pret = RECHARGES.computeIfAbsent(player.getUuid(), u -> new HashMap<>()).getOrDefault(id, 0L);
		if (now < pret) {
			refuser(player, Text.translatable("message.les_limbes.pouvoir.recharge", Text.translatable("pouvoir.les_limbes." + id), (pret - now) / 20 + 1));
			return false;
		}
		if (!data.peutPayer(ability.cout()) && !player.isCreative()) {
			refuser(player, Text.translatable("message.les_limbes.pouvoir.majeste_insuffisante", (int) ability.cout()));
			return false;
		}
		if (!ability.execution().executer(player)) {
			return false;
		}
		if (!player.isCreative()) {
			data.payer(ability.cout());
		}
		RECHARGES.get(player.getUuid()).put(id, now + ability.recharge());
		QuestManager.signaler(player, QuestSignal.pouvoir(id));
		PlayerDataSync.envoyer(player);
		return true;
	}

	private static void refuser(ServerPlayerEntity player, Text raison) {
		player.sendMessage(raison.copy().formatted(Formatting.RED), true);
		player.playSoundToPlayer(LimbesSounds.POUVOIR_REFUS, SoundCategory.PLAYERS, 0.6F, 1.0F);
	}

	/** Ticks restants avant disponibilité (0 si prêt). */
	public static long rechargeRestante(ServerPlayerEntity player, String id) {
		Map<String, Long> m = RECHARGES.get(player.getUuid());
		if (m == null) {
			return 0L;
		}
		return Math.max(0L, m.getOrDefault(id, 0L) - player.getServerWorld().getTime());
	}

	/** Régénération de majesté et synchronisation périodique. Appelé chaque tick par joueur. */
	public static void tick(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (data.majeste < data.majesteMax) {
			data.regenerer(REGEN_PAR_TICK);
			if (player.getServerWorld().getTime() % 10L == 0L || data.majeste >= data.majesteMax) {
				PlayerDataSync.envoyer(player);
			}
		}
	}

	public static void onDeconnexion(UUID uuid) {
		RECHARGES.remove(uuid);
	}

	/** Diffuse un effet visuel/sonore aux joueurs proches (le client le décline). */
	public static void effet(ServerWorld world, String type, Vec3d pos, Vec3d direction, int donnee) {
		LimbesPayloads.Effet payload = new LimbesPayloads.Effet(type, pos, direction, donnee);
		for (ServerPlayerEntity p : PlayerLookup.around(world, pos, 64.0)) {
			if (ServerPlayNetworking.canSend(p, LimbesPayloads.Effet.ID)) {
				ServerPlayNetworking.send(p, payload);
			}
		}
	}

	public static void effet(ServerWorld world, String type, BlockPos pos) {
		effet(world, type, Vec3d.ofCenter(pos), Vec3d.ZERO, 0);
	}

	private Abilities() {
	}
}
