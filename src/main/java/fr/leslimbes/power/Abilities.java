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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Registre des techniques royales (sept branches, trente-trois techniques) et gestion des recharges.
 *
 * <p>Toute exécution est serveur. La Marche du Palais rend les Portes gratuites et sans recharge ; l'Avènement
 * divise par deux le coût et la recharge de tout le reste.</p>
 */
public final class Abilities {
	private static final Map<String, Ability> REGISTRE = new LinkedHashMap<>();
	/** Recharges par joueur : id de pouvoir -> tick de disponibilité. */
	private static final Map<UUID, Map<String, Long>> RECHARGES = new HashMap<>();
	/** Régénération de majesté par tick. */
	public static final float REGEN_PAR_TICK = 0.4F;
	/** Ordre d'affichage des branches (clé de langue : branche.les_limbes.&lt;id&gt;). */
	public static final List<String> BRANCHES = List.of("arsenal", "portes", "decrets", "garde", "main", "pactes", "emblematiques");

	// --- Arsenal du Souverain ---
	public static final Ability EPEE_DU_SOUVERAIN = register(new Ability("epee_du_souverain", "arsenal", 10.0F, 20, ArsenalDuSouverain::invoquerOuRappeler));
	public static final Ability PARADE_ROYALE = register(new Ability("parade_royale", "arsenal", 8.0F, 60, ArsenalDuSouverain::parade));
	public static final Ability LANCE_DU_SOUVERAIN = register(new Ability("lance_du_souverain", "arsenal", 12.0F, 20, ArsenalDuSouverain::invoquerLance));
	public static final Ability CHARGE_DE_LANCE = register(new Ability("charge_de_lance", "arsenal", 18.0F, 120, ArsenalDuSouverain::charge));
	public static final Ability ARC_DU_SOUVERAIN = register(new Ability("arc_du_souverain", "arsenal", 12.0F, 20, ArsenalDuSouverain::invoquerArc));
	public static final Ability MARQUE_ROYALE = register(new Ability("marque_royale", "arsenal", 10.0F, 160, ArsenalDuSouverain::marquer));
	public static final Ability BOUCLIER_DU_SOUVERAIN = register(new Ability("bouclier_du_souverain", "arsenal", 12.0F, 20, ArsenalDuSouverain::invoquerBouclier));
	public static final Ability CHANGEMENT_D_ARME = register(new Ability("changement_d_arme", "arsenal", 5.0F, 10, ArsenalDuSouverain::changerDArme));
	public static final Ability ARMES_SATELLITES = register(new Ability("armes_satellites", "arsenal", 30.0F, 400, ArsenalDuSouverain::satellites));
	// --- Portes du Palais ---
	public static final Ability PAS_COURT = register(new Ability("pas_court", "portes", 15.0F, 30, PortesDuPalais::pasCourt));
	public static final Ability PORTE_DIRECTIONNELLE = register(new Ability("porte_directionnelle", "portes", 15.0F, 40, PortesDuPalais::porteDirectionnelle));
	public static final Ability ATTAQUE_D_ANGLE = register(new Ability("attaque_d_angle", "portes", 20.0F, 80, PortesDuPalais::attaqueDAngle));
	public static final Ability SAUVETAGE = register(new Ability("sauvetage", "portes", 20.0F, 200, PortesDuPalais::sauvetage));
	public static final Ability LIAISON_ROYALE = register(new Ability("liaison_royale", "portes", 25.0F, 600, PortesDuPalais::liaisonRoyale));
	// --- Décrets royaux ---
	public static final Ability DECRET_RECULEZ = register(new Ability("decret_reculez", "decrets", 20.0F, 100, DecretsRoyaux::reculez));
	public static final Ability DECRET_DEPOSEZ_LES_ARMES = register(new Ability("decret_deposez_les_armes", "decrets", 25.0F, 300, DecretsRoyaux::deposezLesArmes));
	public static final Ability DECRET_TENEZ_BON = register(new Ability("decret_tenez_bon", "decrets", 20.0F, 400, DecretsRoyaux::tenezBon));
	public static final Ability DECRET_QUE_LE_CHEMIN_S_OUVRE = register(new Ability("decret_que_le_chemin_s_ouvre", "decrets", 30.0F, 600, DecretsRoyaux::queLeCheminSOuvre));
	public static final Ability DECRET_RESTEZ = register(new Ability("decret_restez", "decrets", 20.0F, 200, DecretsRoyaux::restez));
	public static final Ability DECRET_ELEVEZ_VOUS = register(new Ability("decret_elevez_vous", "decrets", 20.0F, 200, DecretsRoyaux::elevezVous));
	// --- Garde astrale ---
	public static final Ability CHEVALIER_ASTRAL = register(new Ability("chevalier_astral", "garde", 25.0F, 200, p -> GardeAstrale.invoquer(p, "chevalier_astral")));
	public static final Ability ARCHER_ASTRAL = register(new Ability("archer_astral", "garde", 25.0F, 200, p -> GardeAstrale.invoquer(p, "archer_astral")));
	public static final Ability PROTECTEUR_ASTRAL = register(new Ability("protecteur_astral", "garde", 30.0F, 300, p -> GardeAstrale.invoquer(p, "protecteur_astral")));
	public static final Ability ORDRE_DE_GARDE = register(new Ability("ordre_de_garde", "garde", 5.0F, 20, GardeAstrale::ordre));
	// --- Main des Limbes ---
	public static final Ability MAIN_SAISIR = register(new Ability("main_saisir", "main", 15.0F, 60, MainDesLimbes::saisir));
	public static final Ability MAIN_PASSAGE = register(new Ability("main_passage", "main", 20.0F, 100, MainDesLimbes::passage));
	public static final Ability MAIN_COURONNEE = register(new Ability("main_couronnee", "main", 35.0F, 300, MainDesLimbes::couronnee));
	// --- Pactes royaux ---
	public static final Ability APPEL_DE_MONTURE = register(new Ability("appel_de_monture", "pactes", 10.0F, 100, PactesRoyaux::appelDeMonture));
	public static final Ability PACTE_DE_COMBAT = register(new Ability("pacte_de_combat", "pactes", 20.0F, 300, PactesRoyaux::pacteDeCombat));
	// --- Techniques emblématiques ---
	public static final Ability JUGEMENT_DES_CONSTELLATIONS = register(new Ability("jugement_des_constellations", "emblematiques", 50.0F, 1200, Emblematiques::jugementDesConstellations));
	public static final Ability MARCHE_DU_PALAIS = register(new Ability("marche_du_palais", "emblematiques", 40.0F, 900, Emblematiques::marcheDuPalais));
	public static final Ability AUDIENCE_DU_ROI = register(new Ability("audience_du_roi", "emblematiques", 45.0F, 1200, Emblematiques::audienceDuRoi));
	public static final Ability AVENEMENT = register(new Ability("avenement", "emblematiques", 80.0F, 6000, Emblematiques::avenement));

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

	/** Coût réel d'une technique pour ce joueur à cet instant (Marche du Palais, Avènement). */
	public static float coutReel(ServerPlayerEntity player, Ability ability) {
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		long now = player.getServerWorld().getTime();
		if (ability.branche().equals("portes") && etat.marcheActive(now)) {
			return 0.0F;
		}
		return etat.avenementActif(now) && !ability.id().equals("avenement") ? ability.cout() * 0.5F : ability.cout();
	}

	public static int rechargeReelle(ServerPlayerEntity player, Ability ability) {
		EtatPouvoirs etat = EtatPouvoirs.de(player);
		long now = player.getServerWorld().getTime();
		if (ability.branche().equals("portes") && etat.marcheActive(now)) {
			return 0;
		}
		return etat.avenementActif(now) && !ability.id().equals("avenement") ? ability.recharge() / 2 : ability.recharge();
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
		float cout = coutReel(player, ability);
		if (!data.peutPayer(cout) && !player.isCreative()) {
			refuser(player, Text.translatable("message.les_limbes.pouvoir.majeste_insuffisante", (int) cout));
			return false;
		}
		if (!ability.execution().executer(player)) {
			return false;
		}
		if (!player.isCreative()) {
			data.payer(cout);
		}
		RECHARGES.get(player.getUuid()).put(id, now + rechargeReelle(player, ability));
		QuestManager.signaler(player, QuestSignal.pouvoir(id));
		PlayerDataSync.envoyer(player);
		return true;
	}

	/** Utilisation par identifiant (sélection radiale) : mêmes règles, message si le pouvoir n'est pas connu. */
	public static void utiliserParId(ServerPlayerEntity player, String id) {
		Ability ability = get(id);
		if (ability == null || !PlayerLimbesData.of(player).possedePouvoir(id)) {
			refuser(player, Text.translatable("message.les_limbes.pouvoir.inconnu"));
			return;
		}
		utiliser(player, id);
	}

	static void refuser(ServerPlayerEntity player, Text raison) {
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

	/** Régénération de majesté, effets continus des techniques et synchronisation périodique. Appelé chaque tick par joueur. */
	public static void tick(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		EtatPouvoirs etat = EtatPouvoirs.existant(player.getUuid());
		long now = player.getServerWorld().getTime();
		boolean avenement = etat != null && etat.avenementActif(now);
		if (data.majeste < data.majesteMax) {
			data.regenerer(avenement ? REGEN_PAR_TICK * 4.0F : REGEN_PAR_TICK);
			if (now % 10L == 0L || data.majeste >= data.majesteMax) {
				PlayerDataSync.envoyer(player);
			}
		}
		if (etat != null) {
			ArsenalDuSouverain.tick(player, etat, now);
			Emblematiques.tick(player, etat, now);
		}
	}

	public static void onDeconnexion(UUID uuid) {
		RECHARGES.remove(uuid);
		EtatPouvoirs.retirer(uuid);
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
