package fr.leslimbes;

import fr.leslimbes.command.LimbesCommands;
import fr.leslimbes.event.LimbesServerEvents;
import fr.leslimbes.gametest.AutoTest;
import fr.leslimbes.npc.dialogue.DialogueLoader;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.quest.QuestLoader;
import fr.leslimbes.registry.LimbesAttachments;
import fr.leslimbes.registry.LimbesBlockEntities;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.registry.LimbesEntities;
import fr.leslimbes.registry.LimbesItemGroups;
import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.registry.LimbesNetworking;
import fr.leslimbes.registry.LimbesParticles;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.registry.LimbesWorldgen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entrée commun (client + serveur dédié) du mod Les Limbes.
 *
 * <p>Tout ce qui concerne la logique de jeu (dégâts, coûts, téléportations, progression,
 * inventaires, constructions) est décidé côté serveur ; le client ne fait que l'affichage et
 * l'envoi d'intentions validées ensuite par le serveur.</p>
 */
public final class LesLimbes implements ModInitializer {
	public static final String MOD_ID = "les_limbes";
	public static final Logger LOGGER = LoggerFactory.getLogger("Les Limbes");

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Les Limbes s'éveillent.");
		LimbesSounds.init();
		LimbesParticles.init();
		LimbesBlocks.init();
		LimbesItems.init();
		LimbesBlockEntities.init();
		LimbesEntities.init();
		fr.leslimbes.registry.LimbesEspeces.init();
		LimbesItemGroups.init();
		LimbesAttachments.init();
		LimbesWorldgen.init();
		Abilities.init();
		DialogueLoader.init();
		QuestLoader.init();
		fr.leslimbes.arrivee.ArriveeLoader.init();
		fr.leslimbes.domaine.DomaineLoader.init();
		fr.leslimbes.domaine.ProjetManager.init();
		LimbesNetworking.init();
		LimbesServerEvents.init();
		fr.leslimbes.registry.LimbesLoot.init();
		LimbesCommands.init();
		AutoTest.init();
	}
}
