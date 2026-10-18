package fr.leslimbes.gametest;

import fr.leslimbes.block.PorteDesLimbesBlock;
import fr.leslimbes.npc.HabitantEntity;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.world.structure.ComposedPiece;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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
				"clairval/maison_1", "clairval/maison_2", "clairval/maison_3", "clairval/moulin", "clairval/observatoire", "manoir/manoir", "relais/relais_royal",
				"arrivees/maison_egaree", "arrivees/caisse_tombee", "lac/lac_crepuscule")) {
			Optional<StructureTemplate> t = world.getStructureTemplateManager().getTemplate(Identifier.of("les_limbes", id));
			context.assertTrue(t.isPresent(), "gabarit introuvable : " + id);
			Vec3i size = t.get().getSize();
			context.assertTrue(size.getX() > 0 && size.getY() > 0 && size.getZ() > 0, "gabarit vide : " + id);
		}
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void lesPiecesComposeesSeGenerentAvecLeursMarqueurs(TestContext context) {
		// Le serveur de GameTest est un monde plat sans dimensions de datapack : on exerce ici la pièce
		// composée (gabarit + marqueurs pnj:/entite:) ; la génération réelle de la dimension est couverte par AutoTest.
		ServerWorld world = context.getWorld();
		BlockPos origine = context.getAbsolutePos(new BlockPos(0, 1, 0));
		ComposedPiece piece = new ComposedPiece(world.getStructureTemplateManager(), Identifier.of("les_limbes", "vestige/vestige"), origine, BlockRotation.NONE);
		BlockBox box = piece.getBoundingBox();
		ChunkPos chunkPos = new ChunkPos(origine);
		piece.generate(world, world.getStructureAccessor(), world.getChunkManager().getChunkGenerator(), world.getRandom(),
				new BlockBox(box.getMinX() - 16, world.getBottomY(), box.getMinZ() - 16, box.getMaxX() + 16, world.getTopY(), box.getMaxZ() + 16), chunkPos, origine);
		// Voile dormant 3x3 à (7..9, 5..7, 3) dans le gabarit.
		BlockState porte = world.getBlockState(origine.add(8, 6, 3));
		context.assertTrue(porte.getBlock() instanceof PorteDesLimbesBlock && !porte.get(PorteDesLimbesBlock.ACTIVE), "porte dormante attendue dans le vestige, trouvé " + porte);
		context.assertTrue(world.getBlockState(origine.add(5, 5, 10)).isOf(net.minecraft.block.Blocks.LECTERN), "pupitre du gardien attendu");
		// Pièce avec marqueur PNJ : la forge de Clairval fait apparaître Brann.
		BlockPos origineForge = context.getAbsolutePos(new BlockPos(0, 1, 20));
		ComposedPiece forge = new ComposedPiece(world.getStructureTemplateManager(), Identifier.of("les_limbes", "clairval/forge"), origineForge, BlockRotation.NONE);
		BlockBox fb = forge.getBoundingBox();
		forge.generate(world, world.getStructureAccessor(), world.getChunkManager().getChunkGenerator(), world.getRandom(),
				new BlockBox(fb.getMinX() - 16, world.getBottomY(), fb.getMinZ() - 16, fb.getMaxX() + 16, world.getTopY(), fb.getMaxZ() + 16), new ChunkPos(origineForge), origineForge);
		context.runAtTick(40, () -> {
			List<HabitantEntity> brann = world.getEntitiesByClass(HabitantEntity.class, new Box(origineForge).expand(16, 8, 16), h -> h.getRole().equals("brann"));
			context.assertTrue(!brann.isEmpty(), "Brann doit apparaître depuis le marqueur pnj:brann de la forge");
			context.assertTrue(brann.get(0).isInvulnerableTo(world.getDamageSources().generic()), "un personnage essentiel est invulnérable aux dégâts ordinaires");
			context.assertTrue(brann.get(0).getDomicile().isPresent(), "le personnage a un domicile");
			context.assertTrue(world.getBlockState(origineForge.add(6, 1, 5)).isAir(), "le bloc marqueur a été remplacé par de l'air");
			brann.forEach(h -> h.discard());
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void laRaieCelesteConclutUnPacteEtRevientAuSifflet(TestContext context) {
		ServerWorld world = context.getWorld();
		// Un vrai joueur serveur (connexion factice) : le FakePlayer de Fabric refuse toute monte.
		net.minecraft.server.network.ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 3, 2));
		player.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		data.montures.clear();
		player.getInventory().clear();
		fr.leslimbes.entity.RaieCelesteEntity raie = fr.leslimbes.registry.LimbesEntities.RAIE_CELESTE.create(world);
		context.assertTrue(raie != null, "création de la raie");
		raie.refreshPositionAndAngles(pos.getX() + 2.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
		world.spawnEntity(raie);
		// Un inconnu la main vide ne peut pas la monter (sauvage ou non).
		PlayerEntity inconnu = context.createMockPlayer(net.minecraft.world.GameMode.SURVIVAL);
		context.assertTrue(raie.interactMob(inconnu, net.minecraft.util.Hand.MAIN_HAND) == net.minecraft.util.ActionResult.PASS && !raie.hasPassengers(),
				"une raie sauvage ne se laisse pas monter");
		// Trois fleurs de rosée : pacte, sifflet reçu, monture liée.
		for (int i = 0; i < 3; i++) {
			player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, new ItemStack(LimbesBlocks.FLEUR_DE_ROSEE, 1));
			raie.interactMob(player, net.minecraft.util.Hand.MAIN_HAND);
		}
		context.assertTrue(raie.isTamed(), "la raie est apprivoisée après trois fleurs");
		context.assertTrue(player.getUuid().equals(raie.getOwnerUuid()), "le joueur est son propriétaire");
		context.assertTrue(raie.getUuidAsString().equals(data.montures.get(fr.leslimbes.entity.RaieCelesteEntity.CLE_MONTURE)), "la monture est liée au joueur");
		context.assertTrue(player.getInventory().count(LimbesItems.SIFFLET_DE_RAIE) == 1, "un sifflet de raie a été donné");
		context.assertTrue(raie.interactMob(inconnu, net.minecraft.util.Hand.MAIN_HAND) == net.minecraft.util.ActionResult.PASS && !raie.hasPassengers(),
				"seul le propriétaire peut monter la raie");
		// Le propriétaire la main vide monte ; la raie lit l'entrée du cavalier.
		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, ItemStack.EMPTY);
		net.minecraft.util.ActionResult monte = raie.interactMob(player, net.minecraft.util.Hand.MAIN_HAND);
		context.assertTrue(monte == net.minecraft.util.ActionResult.CONSUME && raie.hasPassengers() && raie.getControllingPassenger() == player && raie.estMontee(),
				"le propriétaire pilote la raie une fois monté (résultat=" + monte + ", passagers=" + raie.getPassengerList().size() + ", contrôle=" + raie.getControllingPassenger() + ")");
		player.stopRiding();
		context.assertTrue(!raie.hasPassengers() && player.getVehicle() == null, "descente de la raie");
		// Rappel : la raie disparue est recréée, liée et apprivoisée.
		raie.discard();
		fr.leslimbes.entity.RaieCelesteEntity.rappeler(player);
		context.runAtTick(5, () -> {
			List<fr.leslimbes.entity.RaieCelesteEntity> raies = world.getEntitiesByClass(fr.leslimbes.entity.RaieCelesteEntity.class, new Box(pos).expand(12), r -> r.isAlive());
			context.assertTrue(raies.size() == 1, "une raie recréée par le sifflet (trouvé " + raies.size() + ")");
			context.assertTrue(raies.get(0).isTamed() && player.getUuid().equals(raies.get(0).getOwnerUuid()), "la raie recréée est apprivoisée et liée");
			context.assertTrue(raies.get(0).getUuidAsString().equals(data.montures.get(fr.leslimbes.entity.RaieCelesteEntity.CLE_MONTURE)), "le lien pointe vers la nouvelle raie");
			raies.forEach(r -> r.discard());
			world.getServer().getPlayerManager().remove(player);
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 300)
	public void lesDallesDeRoseeEmergentAuCrepuscule(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos p = new BlockPos(1, 1, 1);
		BlockPos abs = context.getAbsolutePos(p);
		world.setTimeOfDay(6000L);
		context.setBlockState(p, LimbesBlocks.DALLE_DE_ROSEE.getDefaultState().with(fr.leslimbes.block.DalleDeRoseeBlock.REVELEE, true));
		context.runAtTick(60, () -> {
			context.checkBlockState(p, s -> !s.get(fr.leslimbes.block.DalleDeRoseeBlock.REVELEE), () -> "à midi la dalle doit se cacher");
			context.assertTrue(world.getBlockState(abs).getCollisionShape(world, abs).isEmpty(), "dalle cachée : pas d'appui");
			world.setTimeOfDay(12800L);
		});
		context.runAtTick(140, () -> {
			context.checkBlockState(p, s -> s.get(fr.leslimbes.block.DalleDeRoseeBlock.REVELEE), () -> "au crépuscule la dalle doit émerger");
			context.assertTrue(!world.getBlockState(abs).getCollisionShape(world, abs).isEmpty(), "dalle révélée : appui plein");
			world.setTimeOfDay(1000L);
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void leMoulinMoudQuandIlEstRepare(TestContext context) {
		ServerWorld world = context.getWorld();
		FakePlayer player = FakePlayer.get(world);
		BlockPos p = new BlockPos(1, 1, 1);
		context.setBlockState(p, LimbesBlocks.MEULE_DE_MOULIN.getDefaultState());
		BlockPos axe = new BlockPos(3, 1, 1);
		context.setBlockState(axe, LimbesBlocks.AILES_DE_MOULIN.getDefaultState());
		fr.leslimbes.world.state.LimbesWorldState etat = fr.leslimbes.world.state.LimbesWorldState.get(world.getServer());
		player.getInventory().clear();
		ItemStack ble = new ItemStack(net.minecraft.item.Items.WHEAT, 4);
		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, ble);
		net.minecraft.util.hit.BlockHitResult hit = new net.minecraft.util.hit.BlockHitResult(net.minecraft.util.math.Vec3d.ofCenter(context.getAbsolutePos(p)), Direction.UP, context.getAbsolutePos(p), false);
		// Moulin immobile : rien ne se passe.
		world.getBlockState(context.getAbsolutePos(p)).onUseWithItem(ble, world, player, net.minecraft.util.Hand.MAIN_HAND, hit);
		context.assertTrue(player.getInventory().count(LimbesItems.FARINE) == 0, "pas de farine tant que le moulin est immobile");
		// Réparation scénarisée : drapeau du monde + action modifier_bloc sur l'axe des ailes.
		etat.poserDrapeau(fr.leslimbes.block.MeuleDeMoulinBlock.DRAPEAU_MONDE);
		BlockPos axeAbs = context.getAbsolutePos(axe);
		com.google.gson.JsonObject action = com.google.gson.JsonParser.parseString("{\"type\":\"modifier_bloc\",\"pos\":[" + axeAbs.getX() + "," + axeAbs.getY() + "," + axeAbs.getZ()
				+ "],\"bloc\":\"les_limbes:ailes_de_moulin\",\"proprietes\":{\"actif\":\"true\"}}").getAsJsonObject();
		fr.leslimbes.npc.dialogue.Actions.executer(player, action);
		context.checkBlockState(axe, s -> s.get(fr.leslimbes.block.MoulinBlock.ACTIF), () -> "les ailes doivent être actives après modifier_bloc");
		world.getBlockState(context.getAbsolutePos(p)).onUseWithItem(ble, world, player, net.minecraft.util.Hand.MAIN_HAND, hit);
		context.assertTrue(player.getInventory().count(LimbesItems.FARINE) == 1, "un blé donne un sac de farine quand le moulin tourne");
		context.assertTrue(ble.getCount() == 3, "le blé est consommé");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void uneArriveeSePlaceAvecSesOccupants(TestContext context) {
		ServerWorld world = context.getWorld();
		// Posée en l'air (y 3) : la zone de test n'a que de l'air là ; le sol de test lui-même n'est pas « naturel ».
		BlockPos origine = context.getAbsolutePos(new BlockPos(2, 12, 2));
		StructureTemplate template = world.getStructureTemplateManager().getTemplate(Identifier.of("les_limbes", "arrivees/maison_egaree")).orElseThrow();
		String raison = fr.leslimbes.arrivee.ArriveeManager.raisonOccupation(world, origine, template.getSize());
		context.assertTrue(raison == null, "zone de test libre (" + raison + ")");
		net.minecraft.structure.StructurePlacementData placement = new net.minecraft.structure.StructurePlacementData().setIgnoreEntities(false)
				.addProcessor(net.minecraft.structure.processor.BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
		template.place(world, origine, origine, placement, world.getRandom(), 2);
		int marqueurs = fr.leslimbes.world.structure.MarqueursDeStructure.appliquerTous(world, template, placement, origine, world.getRandom());
		context.assertTrue(marqueurs == 1, "un marqueur pnj:emile dans la maison (trouvé " + marqueurs + ")");
		context.assertTrue(!fr.leslimbes.arrivee.ArriveeManager.zoneLibre(world, origine, template.getSize()), "la zone n'est plus libre une fois bâtie");
		context.runAtTick(20, () -> {
			List<HabitantEntity> emile = world.getEntitiesByClass(HabitantEntity.class, new Box(origine).expand(16, 8, 16), h -> h.getRole().equals("emile"));
			context.assertTrue(emile.size() == 1, "Émile apparaît avec sa maison");
			context.assertTrue(world.getBlockState(origine.add(3, 1, 2)).isOf(net.minecraft.block.Blocks.SMOKER), "le four à pain est en place");
			emile.forEach(h -> h.discard());
			context.complete();
		});
	}
}
