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
		// Condition facultative « si » : l'action n'est exécutée que si toutes ses conditions sont vraies.
		if (a.has("si") && !Conditions.toutes(player, JsonUtil.objects(a, "si"))) {
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
				String localiser = JsonUtil.string(a, "localiser", "");
				if ("vestige".equals(localiser)) {
					pos = LimbesPortal.localiserVestige(player.getServerWorld(), player.getBlockPos());
					data.vestigeConnu = pos;
				} else if (localiser.startsWith("arrivee:")) {
					fr.leslimbes.arrivee.ArriveeEvenement e = fr.leslimbes.arrivee.ArriveeManager.plusProche(player.server, player.getBlockPos(), localiser.substring(8));
					pos = e == null ? Optional.empty() : Optional.of(e.pos);
				}
				QuestProgress p = data.quetes.get(quete);
				if (p != null && pos.isPresent()) {
					p.lieux.put(cle, pos.get());
				}
			}
			case "apparition" -> apparition(player, a);
			case "arrivee_resoudre" -> {
				if (fr.leslimbes.arrivee.ArriveeManager.resoudre(player.server, JsonUtil.requireString(a, "id", type))) {
					player.sendMessage(Text.translatable("message.les_limbes.arrivee.resolue").formatted(Formatting.LIGHT_PURPLE), false);
				}
			}
			case "arrivee_declencher" -> {
				fr.leslimbes.arrivee.ArriveeDefinition def = fr.leslimbes.arrivee.ArriveeLoader.get(JsonUtil.requireString(a, "id", type));
				if (def != null) {
					fr.leslimbes.arrivee.ArriveeManager.declencher(player.server, def, JsonUtil.string(a, "site", null));
				}
			}
			case "deplacer_pnj" -> deplacerPnj(player, a);
			case "modifier_bloc" -> modifierBloc(player, a);
			case "ouvrir_chantier" -> fr.leslimbes.domaine.ProjetManager.ouvrirChantier(player, JsonUtil.string(a, "lieu", "manoir"));
			case "affuter_epee" -> fr.leslimbes.item.EpeeDuSouverainItem.affuter(player);
			case "decouvrir_lieu" -> {
				fr.leslimbes.domaine.LieuDefinition lieu = fr.leslimbes.domaine.DomaineLoader.lieu(JsonUtil.requireString(a, "id", type));
				if (lieu != null) {
					fr.leslimbes.domaine.LieuxManager.decouvrir(player, lieu, JsonUtil.bool(a, "annoncer", true));
				}
			}
			case "decouvrir_relais" -> fr.leslimbes.domaine.RelaisManager.decouvrir(player, JsonUtil.requireString(a, "id", type), JsonUtil.bool(a, "annoncer", true));
			case "compteur" -> data.compteurs.merge(JsonUtil.requireString(a, "nom", type), JsonUtil.integer(a, "valeur", 1), Integer::sum);
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

	/**
	 * Déplace un personnage (par rôle) vers une position et en fait son nouveau domicile ; il y
	 * réapparaîtra désormais. <pre>{"type": "deplacer_pnj", "role": "emile", "pos": [20, 66, 126]}</pre>
	 */
	private static void deplacerPnj(ServerPlayerEntity player, JsonObject a) {
		String role = JsonUtil.requireString(a, "role", "deplacer_pnj");
		Optional<BlockPos> pos = JsonUtil.blockPos(a, "pos");
		if (pos.isEmpty()) {
			return;
		}
		net.minecraft.server.world.ServerWorld world = player.getServerWorld();
		String dim = JsonUtil.string(a, "dimension", null);
		if (dim != null) {
			net.minecraft.server.world.ServerWorld autre = player.server.getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(dim)));
			if (autre != null) {
				world = autre;
			}
		}
		BlockPos cible = pos.get();
		// Le personnage peut être loin du joueur (il est recruté à Clairval, le projet se valide au manoir) :
		// on cherche parmi toutes les entités chargées de la dimension, puis dans les autres mondes.
		fr.leslimbes.npc.HabitantEntity trouve = null;
		for (net.minecraft.server.world.ServerWorld w : player.server.getWorlds()) {
			for (net.minecraft.entity.Entity e : w.iterateEntities()) {
				if (e instanceof fr.leslimbes.npc.HabitantEntity h && h.isAlive() && h.getRole().equals(role)) {
					trouve = h;
					break;
				}
			}
			if (trouve != null) {
				break;
			}
		}
		LimbesWorldState.get(player.server).ancrerPnj(role, cible);
		if (trouve != null) {
			trouve.setDomicile(cible);
			trouve.teleport(world, cible.getX() + 0.5, cible.getY(), cible.getZ() + 0.5, java.util.Set.of(), trouve.getYaw(), trouve.getPitch());
			return;
		}
		// Personnage non chargé : l'ancrage est déplacé et la réapparition des essentiels fera le reste
		// (l'ancien exemplaire, s'il réapparaît dans un chunk rechargé, est écarté par EssentialNpcs).
		fr.leslimbes.npc.EssentialNpcs.reapparaitre(world, role, cible);
		LimbesWorldState.get(player.server).noterReapparition(role, world.getTime());
	}

	/**
	 * Change l'état d'un bloc scénarisé (transformation visible du monde). Le bloc doit être du type indiqué.
	 * <pre>{"type": "modifier_bloc", "pos": [-28, 73, 126], "bloc": "les_limbes:ailes_de_moulin", "proprietes": {"actif": "true"}}</pre>
	 */
	private static void modifierBloc(ServerPlayerEntity player, JsonObject a) {
		Optional<BlockPos> pos = JsonUtil.blockPos(a, "pos");
		if (pos.isEmpty()) {
			return;
		}
		net.minecraft.server.world.ServerWorld world = player.getServerWorld();
		String dim = JsonUtil.string(a, "dimension", null);
		if (dim != null) {
			net.minecraft.server.world.ServerWorld autre = player.server.getWorld(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(dim)));
			if (autre != null) {
				world = autre;
			}
		}
		Identifier bloc = Identifier.of(JsonUtil.requireString(a, "bloc", "modifier_bloc"));
		net.minecraft.block.BlockState etat = world.getBlockState(pos.get());
		if (!Registries.BLOCK.getId(etat.getBlock()).equals(bloc)) {
			LesLimbes.LOGGER.warn("modifier_bloc : {} attendu en {}, trouvé {}", bloc, pos.get().toShortString(), Registries.BLOCK.getId(etat.getBlock()));
			return;
		}
		JsonObject props = a.has("proprietes") ? a.getAsJsonObject("proprietes") : new JsonObject();
		for (String nom : props.keySet()) {
			net.minecraft.state.property.Property<?> prop = etat.getBlock().getStateManager().getProperty(nom);
			if (prop != null) {
				etat = avec(etat, prop, props.get(nom).getAsString());
			}
		}
		world.setBlockState(pos.get(), etat, 3);
	}

	private static <T extends Comparable<T>> net.minecraft.block.BlockState avec(net.minecraft.block.BlockState etat, net.minecraft.state.property.Property<T> prop, String valeur) {
		return prop.parse(valeur).map(v -> etat.with(prop, v)).orElse(etat);
	}

	private Actions() {
	}
}
