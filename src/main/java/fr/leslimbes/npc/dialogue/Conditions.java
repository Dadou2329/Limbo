package fr.leslimbes.npc.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.leslimbes.LesLimbes;
import fr.leslimbes.quest.QuestProgress;
import fr.leslimbes.util.JsonUtil;
import fr.leslimbes.world.state.LimbesWorldState;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Conditions évaluées côté serveur pour les dialogues et les quêtes.
 *
 * <pre>
 * {"drapeau": "arrivee"}            {"sans_drapeau": "manoir_recu"}
 * {"quete_active": "l_etranger"}    {"quete_terminee": "le_passage"}
 * {"quete_etape": {"l_etranger": ">=2"}}   (opérateurs : =, >=, <=, >, <)
 * {"objet": {"id": "les_limbes:fragment_de_nacre", "nombre": 3}}
 * {"drapeau_monde": "capitale_eveillee"}
 * {"tout": [c1, c2]}  {"un_de": [c1, c2]}  {"non": c}
 * </pre>
 * Un objet sans clé connue vaut vrai (avec avertissement), afin de ne jamais bloquer un dialogue.
 */
public final class Conditions {
	public static boolean toutes(ServerPlayerEntity player, List<JsonObject> conditions) {
		for (JsonObject c : conditions) {
			if (!verifier(player, c)) {
				return false;
			}
		}
		return true;
	}

	/** Conditions évaluables sans joueur (prérequis d'événements du monde) : drapeau_monde, tout, un_de, non. */
	public static boolean toutesMonde(net.minecraft.server.MinecraftServer server, List<JsonObject> conditions) {
		for (JsonObject c : conditions) {
			if (!verifierMonde(server, c)) {
				return false;
			}
		}
		return true;
	}

	public static boolean verifierMonde(net.minecraft.server.MinecraftServer server, JsonObject c) {
		for (Map.Entry<String, JsonElement> entry : c.entrySet()) {
			JsonElement v = entry.getValue();
			boolean ok = switch (entry.getKey()) {
				case "drapeau_monde" -> LimbesWorldState.get(server).aDrapeau(v.getAsString());
				case "sans_drapeau_monde" -> !LimbesWorldState.get(server).aDrapeau(v.getAsString());
				case "arrivee_active" -> fr.leslimbes.arrivee.ArriveeManager.plusProche(server, net.minecraft.util.math.BlockPos.ORIGIN, v.getAsString()) != null;
				case "tout" -> {
					for (JsonElement e : v.getAsJsonArray()) {
						if (!verifierMonde(server, e.getAsJsonObject())) {
							yield false;
						}
					}
					yield true;
				}
				case "un_de" -> {
					for (JsonElement e : v.getAsJsonArray()) {
						if (verifierMonde(server, e.getAsJsonObject())) {
							yield true;
						}
					}
					yield false;
				}
				case "non" -> !verifierMonde(server, v.getAsJsonObject());
				default -> true;
			};
			if (!ok) {
				return false;
			}
		}
		return true;
	}

	public static boolean verifier(ServerPlayerEntity player, JsonObject c) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		boolean reconnue = false;
		for (Map.Entry<String, JsonElement> entry : c.entrySet()) {
			String key = entry.getKey();
			JsonElement v = entry.getValue();
			reconnue = true;
			boolean ok = switch (key) {
				case "drapeau" -> data.aDrapeau(v.getAsString());
				case "sans_drapeau" -> !data.aDrapeau(v.getAsString());
				case "quete_active" -> data.queteActive(v.getAsString());
				case "quete_terminee" -> data.queteTerminee(v.getAsString());
				case "quete_inconnue" -> !data.quetes.containsKey(v.getAsString());
				case "quete_etape" -> queteEtape(data, v.getAsJsonObject());
				case "objet" -> possedeObjet(player, v.getAsJsonObject());
				case "drapeau_monde" -> LimbesWorldState.get(player.server).aDrapeau(v.getAsString());
				case "pouvoir" -> data.possedePouvoir(v.getAsString());
				case "sans_drapeau_monde" -> !LimbesWorldState.get(player.server).aDrapeau(v.getAsString());
				case "arrivee_active" -> fr.leslimbes.arrivee.ArriveeManager.plusProche(player.server, player.getBlockPos(), v.getAsString()) != null;
				case "monture" -> data.montures.containsKey(v.getAsString());
				case "compteur" -> compteur(data, v.getAsJsonObject());
				case "tout" -> {
					for (JsonElement e : v.getAsJsonArray()) {
						if (!verifier(player, e.getAsJsonObject())) {
							yield false;
						}
					}
					yield true;
				}
				case "un_de" -> {
					for (JsonElement e : v.getAsJsonArray()) {
						if (verifier(player, e.getAsJsonObject())) {
							yield true;
						}
					}
					yield false;
				}
				case "non" -> !verifier(player, v.getAsJsonObject());
				default -> {
					reconnue = false;
					yield true;
				}
			};
			if (!ok) {
				return false;
			}
		}
		if (!reconnue && !c.entrySet().isEmpty()) {
			LesLimbes.LOGGER.warn("Condition inconnue ignorée : {}", c);
		}
		return true;
	}

	/** {@code {"compteur": {"nom": "relation_kael", "min": 2}}} (bornes {@code min} et {@code max} facultatives). */
	private static boolean compteur(PlayerLimbesData data, com.google.gson.JsonObject o) {
		int valeur = data.compteur(fr.leslimbes.util.JsonUtil.requireString(o, "nom", "compteur"));
		int min = fr.leslimbes.util.JsonUtil.integer(o, "min", Integer.MIN_VALUE);
		int max = fr.leslimbes.util.JsonUtil.integer(o, "max", Integer.MAX_VALUE);
		return valeur >= min && valeur <= max;
	}

	private static boolean queteEtape(PlayerLimbesData data, JsonObject spec) {
		for (Map.Entry<String, JsonElement> e : spec.entrySet()) {
			Optional<QuestProgress> p = data.quete(e.getKey());
			if (p.isEmpty()) {
				return false;
			}
			if (!comparer(p.get().etape, e.getValue().getAsString().trim())) {
				return false;
			}
		}
		return true;
	}

	/** Compare une valeur à une expression comme ">=2", "<5", "3". */
	public static boolean comparer(int valeur, String expr) {
		if (expr.startsWith(">=")) {
			return valeur >= Integer.parseInt(expr.substring(2).trim());
		}
		if (expr.startsWith("<=")) {
			return valeur <= Integer.parseInt(expr.substring(2).trim());
		}
		if (expr.startsWith(">")) {
			return valeur > Integer.parseInt(expr.substring(1).trim());
		}
		if (expr.startsWith("<")) {
			return valeur < Integer.parseInt(expr.substring(1).trim());
		}
		if (expr.startsWith("=")) {
			return valeur == Integer.parseInt(expr.substring(1).trim());
		}
		return valeur == Integer.parseInt(expr);
	}

	private static boolean possedeObjet(ServerPlayerEntity player, JsonObject spec) {
		Identifier id = Identifier.tryParse(JsonUtil.requireString(spec, "id", "condition objet"));
		int nombre = JsonUtil.integer(spec, "nombre", 1);
		if (id == null) {
			return false;
		}
		int total = 0;
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (!stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).equals(id)) {
				total += stack.getCount();
			}
		}
		return total >= nombre;
	}

	private Conditions() {
	}
}
