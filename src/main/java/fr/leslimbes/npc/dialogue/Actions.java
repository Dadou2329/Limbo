package fr.leslimbes.npc.dialogue;

import com.google.gson.JsonObject;
import fr.leslimbes.LesLimbes;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestProgress;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.util.JsonUtil;
import fr.leslimbes.world.dimension.LimbesPortal;
import fr.leslimbes.world.state.LimbesWorldState;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

/**
 * Actions exécutées côté serveur par les dialogues et les quêtes. Chaque action est idempotente
 * lorsqu'un garde-fou existe (drapeau « une_fois ») afin de ne jamais dupliquer une récompense.
 *
 * <pre>
 * {"type": "demarrer_quete", "quete": "l_etranger"}
 * {"type": "objectif", "quete": "l_etranger", "objectif": "maelle"}
 * {"type": "etape", "quete": "l_etranger", "etape": 3}
 * {"type": "drapeau", "nom": "manoir_recu"}          {"type": "drapeau_monde", "nom": "..."}
 * {"type": "donner", "objet": "les_limbes:cle_du_manoir", "nombre": 1, "une_fois": "cle_manoir_donnee"}
 * {"type": "retirer", "objet": "...", "nombre": 1}
 * {"type": "pouvoir", "id": "epee_du_souverain"}
 * {"type": "majeste_max", "valeur": 40}
 * {"type": "message", "texte": "..."}                  {"type": "titre", "titre": "...", "sous_titre": "..."}
 * {"type": "son", "id": "les_limbes:quete.terminee"}
 * {"type": "experience", "niveaux": 2}
 * {"type": "lieu_quete", "quete": "le_passage", "cle": "vestige", "localiser": "vestige"}
 * </pre>
 */
public final class Actions {
	public static void executer(ServerPlayerEntity player, List<JsonObject> actions) {
		for (JsonObject a : actions) {
			try {
				executer(player, a);
			} catch (Exception e) {
				LesLimbes.LOGGER.error("Action invalide {} : {}", a, e.toString());
			}
		}
	}

	public static void executer(ServerPlayerEntity player, JsonObject a) {
		String type = JsonUtil.requireString(a, "type", "action");
		PlayerLimbesData data = PlayerLimbesData.of(player);
		String uneFois = JsonUtil.string(a, "une_fois", null);
		if (uneFois != null && data.aDrapeau(uneFois)) {
			return;
		}
		switch (type) {
			case "demarrer_quete" -> QuestManager.demarrer(player, JsonUtil.requireString(a, "quete", type));
			case "objectif" -> QuestManager.marquerObjectif(player, JsonUtil.requireString(a, "quete", type), JsonUtil.requireString(a, "objectif", type));
			case "etape" -> QuestManager.allerEtape(player, JsonUtil.requireString(a, "quete", type), JsonUtil.integer(a, "etape", 0));
			case "drapeau" -> {
				String nom = JsonUtil.requireString(a, "nom", type);
				data.poserDrapeau(nom);
				QuestManager.signaler(player, QuestSignal.drapeau(nom));
			}
			case "drapeau_monde" -> LimbesWorldState.get(player.server).poserDrapeau(JsonUtil.requireString(a, "nom", type));
			case "donner" -> {
				Item item = Registries.ITEM.get(Identifier.of(JsonUtil.requireString(a, "objet", type)));
				int nombre = JsonUtil.integer(a, "nombre", 1);
				ItemStack stack = new ItemStack(item, nombre);
				if (!player.giveItemStack(stack)) {
					player.dropItem(stack, false);
				}
				player.getServerWorld().playSound(null, player.getBlockPos(), LimbesSounds.FRAGMENT, SoundCategory.PLAYERS, 0.7F, 1.2F);
			}
			case "retirer" -> {
				Identifier id = Identifier.of(JsonUtil.requireString(a, "objet", type));
				int reste = JsonUtil.integer(a, "nombre", 1);
				for (int i = 0; i < player.getInventory().size() && reste > 0; i++) {
					ItemStack stack = player.getInventory().getStack(i);
					if (!stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).equals(id)) {
						int pris = Math.min(reste, stack.getCount());
						stack.decrement(pris);
						reste -= pris;
					}
				}
			}
			case "pouvoir" -> {
				String id = JsonUtil.requireString(a, "id", type);
				if (data.debloquerPouvoir(id)) {
					player.sendMessage(Text.translatable("message.les_limbes.pouvoir.debloque", Text.translatable("pouvoir.les_limbes." + id)).formatted(Formatting.GOLD), false);
					player.getServerWorld().playSound(null, player.getBlockPos(), LimbesSounds.QUETE_TERMINEE, SoundCategory.PLAYERS, 0.8F, 1.3F);
				}
			}
			case "majeste_max" -> data.augmenterMajesteMax(JsonUtil.floating(a, "valeur", 20.0F));
			case "message" -> player.sendMessage(Text.literal(JsonUtil.requireString(a, "texte", type)).formatted(Formatting.AQUA), JsonUtil.bool(a, "barre", false));
			case "titre" -> LimbesPortal.titre(player, Text.literal(JsonUtil.string(a, "titre", "")).formatted(Formatting.GOLD),
					Text.literal(JsonUtil.string(a, "sous_titre", "")).formatted(Formatting.AQUA));
			case "son" -> {
				SoundEvent son = Registries.SOUND_EVENT.get(Identifier.of(JsonUtil.requireString(a, "id", type)));
				if (son != null) {
					player.getServerWorld().playSound(null, player.getBlockPos(), son, SoundCategory.PLAYERS, JsonUtil.floating(a, "volume", 1.0F), JsonUtil.floating(a, "hauteur", 1.0F));
				}
			}
			case "experience" -> player.addExperienceLevels(JsonUtil.integer(a, "niveaux", 1));
			case "lieu_quete" -> {
				String quete = JsonUtil.requireString(a, "quete", type);
				String cle = JsonUtil.requireString(a, "cle", type);
				Optional<BlockPos> pos = JsonUtil.blockPos(a, "pos");
				if ("vestige".equals(JsonUtil.string(a, "localiser", ""))) {
					pos = LimbesPortal.localiserVestige(player.getServerWorld(), player.getBlockPos());
					data.vestigeConnu = pos;
				}
				QuestProgress p = data.quetes.get(quete);
				if (p != null && pos.isPresent()) {
					p.lieux.put(cle, pos.get());
				}
			}
			case "apparition" -> apparition(player, a);
			default -> LesLimbes.LOGGER.warn("Action inconnue ignorée : {}", type);
		}
		if (uneFois != null) {
			data.poserDrapeau(uneFois);
		}
		PlayerDataSync.envoyer(player);
	}

	/**
	 * Garantit la présence d'au moins {@code nombre} entités d'un type autour d'une position (apparition
	 * des manquantes). Permet de rejouer un combat scénarisé quel que soit l'ordre d'exploration.
	 * <pre>{"type": "apparition", "entite": "les_limbes:veilleur_egare", "nombre": 3, "pos": [150, 66, 60], "rayon": 10}</pre>
	 */
	private static void apparition(ServerPlayerEntity player, JsonObject a) {
		Identifier id = Identifier.of(JsonUtil.requireString(a, "entite", "apparition"));
		int nombre = JsonUtil.integer(a, "nombre", 1);
		int rayon = JsonUtil.integer(a, "rayon", 8);
		Optional<BlockPos> centre = JsonUtil.blockPos(a, "pos");
		net.minecraft.server.world.ServerWorld world = player.getServerWorld();
		String dim = JsonUtil.string(a, "dimension", null);
		if (dim != null) {
			net.minecraft.server.world.ServerWorld autre = player.server.getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(dim)));
			if (autre == null) {
				return;
			}
			world = autre;
		}
		BlockPos c = centre.orElse(player.getBlockPos());
		net.minecraft.entity.EntityType<?> type = Registries.ENTITY_TYPE.getOrEmpty(id).orElse(null);
		if (type == null) {
			LesLimbes.LOGGER.warn("Apparition : entité inconnue {}", id);
			return;
		}
		net.minecraft.util.math.Box zone = new net.minecraft.util.math.Box(c).expand(rayon + 8, 16, rayon + 8);
		final net.minecraft.entity.EntityType<?> t = type;
		int presents = world.getEntitiesByType(net.minecraft.util.TypeFilter.instanceOf(net.minecraft.entity.Entity.class), zone, e -> e.getType() == t && e.isAlive()).size();
		net.minecraft.util.math.random.Random random = world.getRandom();
		for (int i = presents; i < nombre; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double dist = 2 + random.nextDouble() * rayon;
			int x = (int) Math.round(c.getX() + Math.cos(angle) * dist);
			int z = (int) Math.round(c.getZ() + Math.sin(angle) * dist);
			int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
			net.minecraft.entity.Entity e = type.create(world);
			if (e == null) {
				break;
			}
			e.refreshPositionAndAngles(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
			if (e instanceof net.minecraft.entity.mob.MobEntity mob) {
				mob.setPersistent();
				mob.initialize(world, world.getLocalDifficulty(e.getBlockPos()), net.minecraft.entity.SpawnReason.EVENT, null);
			}
			world.spawnEntity(e);
		}
	}

	private Actions() {
	}
}
