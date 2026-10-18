package fr.leslimbes.gametest;

import fr.leslimbes.block.PorteDesLimbesBlock;
import fr.leslimbes.npc.HabitantEntity;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.dimension.LimbesSites;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.Optional;

/**
 * Tests de jeu exécutés sur un serveur dédié headless (./gradlew runGametest).
 * Ils valident ce que les tests unitaires ne peuvent pas : génération réelle de la dimension et
 * des structures à position fixe, apparition des personnages, logique de la porte et des quêtes
 * avec un joueur factice.
 */
public final class LesLimbesGameTests implements FabricGameTest {

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void laPorteSeReveilleAvecSonVoile(TestContext context) {
		BlockState dormante = LimbesBlocks.PORTE_DES_LIMBES.getDefaultState().with(PorteDesLimbesBlock.ACTIVE, false).with(PorteDesLimbesBlock.AXIS, Direction.Axis.X);
		for (int x = 0; x < 3; x++) {
			for (int y = 1; y < 4; y++) {
				context.setBlockState(new BlockPos(x, y, 1), dormante);
			}
		}
		// Un bloc isolé (non connecté) ne doit pas être réveillé.
		context.setBlockState(new BlockPos(5, 1, 5), dormante);
		BlockPos origine = context.getAbsolutePos(new BlockPos(1, 2, 1));
		List<BlockPos> voile = PorteDesLimbesBlock.reveiller(context.getWorld(), origine, null);
		context.assertEquals(voile.size(), 9, "taille du voile");
		for (int x = 0; x < 3; x++) {
			for (int y = 1; y < 4; y++) {
				BlockPos p = new BlockPos(x, y, 1);
				context.checkBlockState(p, s -> s.getBlock() instanceof PorteDesLimbesBlock && s.get(PorteDesLimbesBlock.ACTIVE), () -> "bloc de porte non réveillé en " + p);
			}
		}
		context.checkBlockState(new BlockPos(5, 1, 5), s -> !s.get(PorteDesLimbesBlock.ACTIVE), () -> "le bloc isolé ne doit pas être réveillé");
		// Idempotence : réveiller à nouveau ne change rien.
		context.assertEquals(PorteDesLimbesBlock.reveiller(context.getWorld(), origine, null).size(), 9, "second réveil");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void laQueteDuPassageAvanceAvecLesSignaux(TestContext context) {
		ServerWorld world = context.getWorld();
		FakePlayer player = FakePlayer.get(world);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		data.quetes.clear();
		data.drapeaux.clear();
		context.assertTrue(QuestManager.demarrer(player, "le_passage"), "la quête doit démarrer");
		context.assertFalse(QuestManager.demarrer(player, "le_passage"), "pas de double démarrage");
		context.assertEquals(data.quete("le_passage").orElseThrow().etape, 0, "étape initiale");
		player.getInventory().insertStack(new ItemStack(LimbesItems.BOUSSOLE_NACREE));
		QuestManager.tick(player);
		context.assertEquals(data.quete("le_passage").orElseThrow().etape, 1, "la boussole fait passer à l'étape 1");
		// Étape 1 (atteindre le vestige) : forcée ici, car le monde de test est plat et sans vestige.
		QuestManager.allerEtape(player, "le_passage", 2);
		player.getInventory().insertStack(new ItemStack(LimbesItems.FRAGMENT_DE_NACRE, 3));
		QuestManager.tick(player);
		context.assertEquals(data.quete("le_passage").orElseThrow().etape, 3, "trois éclats font passer à l'étape 3");
		QuestManager.signaler(player, QuestSignal.drapeau("porte_reveillee"));
		context.assertEquals(data.quete("le_passage").orElseThrow().etape, 4, "le réveil de la porte fait passer à l'étape 4");
		QuestManager.signaler(player, QuestSignal.drapeau("arrivee"));
		context.assertTrue(data.queteTerminee("le_passage"), "la quête doit être terminée après l'arrivée");
		context.assertTrue(data.queteActive("l_etranger"), "la quête L'Étranger doit démarrer à la fin du Passage");
		// Idempotence des signaux : rejouer un signal ne casse rien.
		QuestManager.signaler(player, QuestSignal.drapeau("arrivee"));
		context.assertTrue(data.queteTerminee("le_passage"), "toujours terminée");
		// Objectif manuel : un signal « parler » ne suffit pas, l'action de dialogue est nécessaire.
		QuestManager.allerEtape(player, "l_etranger", 4);
		QuestManager.signaler(player, QuestSignal.parler("gaspard"));
		context.assertEquals(data.quete("l_etranger").orElseThrow().etape, 4, "objectif manuel non validé par simple conversation");
		QuestManager.marquerObjectif(player, "l_etranger", "gaspard");
		context.assertEquals(data.quete("l_etranger").orElseThrow().etape, 5, "l'action de dialogue valide l'objectif");
		context.assertTrue(data.possedePouvoir("epee_du_souverain"), "le fragment éveille l'Épée du Souverain");
		context.assertTrue(data.possedePouvoir("pas_court"), "le fragment éveille le Pas court");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void lesGabaritsDeStructuresSeChargent(TestContext context) {
		ServerWorld world = context.getWorld();
		for (String id : List.of("vestige/vestige", "sanctuaire/ile", "sanctuaire/pont", "clairval/sol", "clairval/auberge", "clairval/forge",
				"clairval/maison_1", "clairval/maison_2", "clairval/maison_3", "clairval/moulin", "manoir/manoir", "relais/relais_royal")) {
			Optional<StructureTemplate> t = world.getStructureTemplateManager().getTemplate(Identifier.of("les_limbes", id));
			context.assertTrue(t.isPresent(), "gabarit introuvable : " + id);
			Vec3i size = t.get().getSize();
			context.assertTrue(size.getX() > 0 && size.getY() > 0 && size.getZ() > 0, "gabarit vide : " + id);
		}
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 1200)
	public void leSanctuaireEtClairvalSeGenerentAPositionFixe(TestContext context) {
		ServerWorld limbes = LimbesDimension.monde(context.getWorld().getServer());
		context.assertTrue(limbes != null, "la dimension des Limbes doit exister");
		// Chargement forcé des chunks des sites (génération synchrone).
		for (int cx = -2; cx <= 2; cx++) {
			for (int cz = -2; cz <= 2; cz++) {
				limbes.setChunkForced(cx, cz, true);
				limbes.getChunk(cx, cz);
			}
		}
		for (int cx = -1; cx <= 1; cx++) {
			for (int cz = 7; cz <= 9; cz++) {
				limbes.setChunkForced(cx, cz, true);
				limbes.getChunk(cx, cz);
			}
		}
		BlockPos arche = new BlockPos(0, 84, -8);
		BlockState s = limbes.getBlockState(arche);
		context.assertTrue(s.getBlock() instanceof PorteDesLimbesBlock && s.get(PorteDesLimbesBlock.ACTIVE), "arche de retour active attendue en " + arche + ", trouvé " + s);
		context.assertTrue(limbes.getBlockState(BlockPos.ofFloored(LimbesSites.ARRIVEE)).isAir(), "la dalle d'arrivée doit être libre");
		context.assertTrue(!limbes.getBlockState(BlockPos.ofFloored(LimbesSites.ARRIVEE).down()).isAir(), "sol attendu sous le point d'arrivée");
		// Clairval : la place est pavée de pierre nacrée à l'altitude des Prairies, et l'air est libre au-dessus.
		BlockPos place = new BlockPos(0, 65, 140);
		context.assertTrue(limbes.getBlockState(place).isOf(LimbesBlocks.PIERRE_NACREE_POLIE) || limbes.getBlockState(place).isOf(LimbesBlocks.PIERRE_NACREE_TAILLEE)
				|| limbes.getBlockState(place).isOf(LimbesBlocks.PIERRE_NACREE_SCULPTEE), "place de Clairval attendue en " + place + ", trouvé " + limbes.getBlockState(place));
		context.assertTrue(limbes.getBlockState(new BlockPos(0, 66, 140)).isAir() || limbes.getBlockState(new BlockPos(0, 66, 140)).getCollisionShape(limbes, new BlockPos(0, 66, 140)).isEmpty(), "l'air doit être libre au-dessus de la place");
		// Les personnages essentiels apparaissent une fois les entités chargées.
		context.runAtTick(200, () -> {
			List<HabitantEntity> pres = limbes.getEntitiesByClass(HabitantEntity.class, new Box(new BlockPos(0, 82, 0)).expand(24, 16, 24), h -> h.getRole().equals("maelle"));
			context.assertTrue(!pres.isEmpty(), "Maëlle doit se trouver au Sanctuaire");
			List<HabitantEntity> thibault = limbes.getEntitiesByClass(HabitantEntity.class, new Box(new BlockPos(14, 66, 126)).expand(24, 16, 24), h -> h.getRole().equals("thibault"));
			context.assertTrue(!thibault.isEmpty(), "Thibault doit se trouver à l'auberge de Clairval");
			for (int cx = -2; cx <= 2; cx++) {
				for (int cz = -2; cz <= 2; cz++) {
					limbes.setChunkForced(cx, cz, false);
				}
			}
			for (int cx = -1; cx <= 1; cx++) {
				for (int cz = 7; cz <= 9; cz++) {
					limbes.setChunkForced(cx, cz, false);
				}
			}
			context.complete();
		});
	}
}
