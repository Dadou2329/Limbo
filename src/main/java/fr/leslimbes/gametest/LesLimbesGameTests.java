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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void unProjetRespecteLesBlocsDuJoueurEtRendLesInventaires(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos origine = context.getAbsolutePos(new BlockPos(1, 2, 1));
		Identifier gabarit = Identifier.of("les_limbes", "manoir/aile_ouest");
		StructureTemplate template = world.getStructureTemplateManager().getTemplate(gabarit).orElseThrow();
		fr.leslimbes.domaine.ProjetDefinition def = new fr.leslimbes.domaine.ProjetDefinition("test_aile", "Test", "d", "i", "manoir", 1, List.of(), List.of(),
				gabarit, origine, Optional.empty(), BlockPos.ORIGIN, "test_projet", List.of());
		// Un coffre posé par un joueur dans l'emprise, avec un contenu.
		BlockPos coffre = origine.add(2, 1, 2);
		world.setBlockState(coffre, net.minecraft.block.Blocks.CHEST.getDefaultState());
		((net.minecraft.block.entity.ChestBlockEntity) world.getBlockEntity(coffre)).setStack(0, new ItemStack(net.minecraft.item.Items.DIAMOND, 3));
		fr.leslimbes.domaine.ProjetManager.Conflits c = fr.leslimbes.domaine.ProjetManager.conflits(world.getServer(), world, def);
		context.assertTrue(c.nombre() == 1 && c.positions().get(0).equals(coffre), "le coffre du joueur est le seul conflit (trouvé " + c.nombre() + ")");
		// Construction autour : le coffre et son contenu restent, le plancher du gabarit est posé ailleurs.
		fr.leslimbes.domaine.ProjetManager.construire(world.getServer(), world, def, template, new java.util.HashSet<>(c.positions()));
		context.assertTrue(world.getBlockState(coffre).isOf(net.minecraft.block.Blocks.CHEST), "le coffre est resté en place");
		context.assertTrue(((net.minecraft.block.entity.ChestBlockEntity) world.getBlockEntity(coffre)).getStack(0).getCount() == 3, "le contenu du coffre est intact");
		context.assertTrue(world.getBlockState(origine.add(0, 1, 0)).isOf(LimbesBlocks.PLANCHES_D_AUBE), "le plancher du gabarit est posé");
		context.assertTrue(fr.leslimbes.domaine.ProjetManager.conflits(world.getServer(), world, def).nombre() >= 1, "le bloc épargné reste signalé comme occupé");
		// Un conteneur remplacé par le gabarit rend son contenu (au sol ou dans le nouveau conteneur).
		BlockPos coffre2 = origine.add(3, 1, 4);
		world.setBlockState(coffre2, net.minecraft.block.Blocks.CHEST.getDefaultState());
		((net.minecraft.block.entity.ChestBlockEntity) world.getBlockEntity(coffre2)).setStack(0, new ItemStack(net.minecraft.item.Items.GOLD_INGOT, 5));
		fr.leslimbes.domaine.ProjetManager.construire(world.getServer(), world, def, template, java.util.Set.of());
		int rendus = 0;
		for (net.minecraft.entity.ItemEntity e : world.getEntitiesByClass(net.minecraft.entity.ItemEntity.class, new Box(origine).expand(8), e -> e.getStack().isOf(net.minecraft.item.Items.GOLD_INGOT))) {
			rendus += e.getStack().getCount();
		}
		if (world.getBlockEntity(coffre2) instanceof net.minecraft.inventory.Inventory inv) {
			for (int i = 0; i < inv.size(); i++) {
				if (inv.getStack(i).isOf(net.minecraft.item.Items.GOLD_INGOT)) {
					rendus += inv.getStack(i).getCount();
				}
			}
		}
		context.assertTrue(rendus == 5, "les cinq lingots du coffre remplacé sont rendus (trouvé " + rendus + ")");
		context.assertTrue(world.getBlockState(coffre2).isOf(LimbesBlocks.PLANCHES_D_AUBE), "le gabarit a bien remplacé le coffre non épargné");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void lesLieuxLesRelaisEtLesPortesSeDecouvrent(TestContext context) {
		ServerWorld world = context.getWorld();
		net.minecraft.server.network.ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 2, 2));
		player.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		String dim = world.getRegistryKey().getValue().toString();
		// Découvrir un lieu inscrit son relais.
		fr.leslimbes.domaine.LieuDefinition lieu = new fr.leslimbes.domaine.LieuDefinition("test_lieu", "Lieu de test", "", pos, 8, "prairies", dim, "relais_royal");
		context.assertTrue(fr.leslimbes.domaine.LieuxManager.decouvrir(player, lieu, false), "première découverte du lieu");
		context.assertTrue(data.aDrapeau("lieu:test_lieu") && data.aDrapeau("relais:relais_royal"), "le lieu et son relais sont inscrits");
		context.assertTrue(!fr.leslimbes.domaine.LieuxManager.decouvrir(player, lieu, false), "une découverte ne compte qu'une fois");
		// Une porte de relais touchée découvre son relais ; éteinte, elle ne mène nulle part.
		BlockPos porte = context.getAbsolutePos(new BlockPos(4, 2, 2));
		world.setBlockState(porte, LimbesBlocks.PORTE_DE_RELAIS.getDefaultState());
		if (world.getBlockEntity(porte) instanceof fr.leslimbes.block.PorteDeRelaisBlock.PorteDeRelaisBlockEntity be) {
			be.setRelais("clairval");
		}
		fr.leslimbes.domaine.RelaisManager.utiliserPorte(player, porte, "clairval");
		context.assertTrue(data.aDrapeau("relais:clairval"), "la porte touchée inscrit le relais de Clairval");
		context.assertTrue(!fr.leslimbes.domaine.RelaisManager.voyager(player, "clairval"), "sans salle des passages, aucun voyage");
		// Réseau éveillé : voyage vers un relais de test dans ce monde (position sûre devant la cible).
		fr.leslimbes.world.state.LimbesWorldState.get(world.getServer()).poserDrapeau(fr.leslimbes.domaine.RelaisManager.RESEAU_EVEILLE);
		java.util.Map<String, fr.leslimbes.domaine.LieuDefinition> lieuxAvant = new java.util.LinkedHashMap<>(fr.leslimbes.domaine.DomaineLoader.lieux());
		java.util.Map<String, fr.leslimbes.domaine.RelaisDefinition> relaisAvant = new java.util.LinkedHashMap<>(fr.leslimbes.domaine.DomaineLoader.relais());
		java.util.Map<String, fr.leslimbes.domaine.RelaisDefinition> relais = new java.util.LinkedHashMap<>(relaisAvant);
		BlockPos cible = context.getAbsolutePos(new BlockPos(7, 1, 7));
		context.setBlockState(new BlockPos(7, 0, 7), LimbesBlocks.PIERRE_NACREE_TAILLEE.getDefaultState());
		relais.put("test_relais", new fr.leslimbes.domaine.RelaisDefinition("test_relais", "Relais de test", "", cible, dim, 9));
		fr.leslimbes.domaine.DomaineLoader.recevoir(lieuxAvant, relais);
		try {
			context.assertTrue(!fr.leslimbes.domaine.RelaisManager.voyager(player, "test_relais"), "un relais inconnu du joueur est refusé");
			fr.leslimbes.domaine.RelaisManager.decouvrir(player, "test_relais", false);
			boolean parti = fr.leslimbes.domaine.RelaisManager.voyager(player, "test_relais");
			context.assertTrue(parti, "le voyage vers un relais connu, depuis une porte, est accepté");
			context.assertTrue(player.getBlockPos().isWithinDistance(cible, 3.5), "le joueur arrive devant le relais (" + player.getBlockPos().toShortString() + " vs " + cible.toShortString() + ")");
			context.assertTrue(data.compteurs.getOrDefault("voyages_relais", 0) == 1, "le voyage est compté");
		} finally {
			fr.leslimbes.domaine.DomaineLoader.recevoir(lieuxAvant, relaisAvant);
		}
		// Le tick planifié éveille la porte (drapeau du monde posé).
		context.runAtTick(60, () -> {
			BlockState etat = world.getBlockState(porte);
			context.assertTrue(etat.isOf(LimbesBlocks.PORTE_DE_RELAIS) && etat.get(fr.leslimbes.block.PorteDeRelaisBlock.EVEILLEE), "la porte s'éveille avec le réseau (" + etat + ")");
			world.getServer().getPlayerManager().remove(player);
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void laVitrineExposeEtRendLObjet(TestContext context) {
		ServerWorld world = context.getWorld();
		FakePlayer player = FakePlayer.get(world);
		player.getInventory().clear();
		BlockPos p = new BlockPos(1, 1, 1);
		BlockPos abs = context.getAbsolutePos(p);
		context.setBlockState(p, LimbesBlocks.VITRINE.getDefaultState());
		ItemStack sifflet = new ItemStack(LimbesItems.SIFFLET_DE_RAIE);
		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, sifflet);
		net.minecraft.util.hit.BlockHitResult hit = new net.minecraft.util.hit.BlockHitResult(net.minecraft.util.math.Vec3d.ofCenter(abs), Direction.UP, abs, false);
		world.getBlockState(abs).onUseWithItem(sifflet, world, player, net.minecraft.util.Hand.MAIN_HAND, hit);
		fr.leslimbes.block.VitrineBlock.VitrineBlockEntity vitrine = (fr.leslimbes.block.VitrineBlock.VitrineBlockEntity) world.getBlockEntity(abs);
		context.assertTrue(vitrine != null && vitrine.getObjet().isOf(LimbesItems.SIFFLET_DE_RAIE), "le sifflet est exposé");
		context.assertTrue(sifflet.isEmpty() && player.getInventory().count(LimbesItems.SIFFLET_DE_RAIE) == 0, "l'objet a quitté la main du joueur");
		context.assertTrue(vitrine.getExposant().equals(player.getName().getString()), "l'exposant est mémorisé");
		// Main vide : l'objet revient au joueur et la vitrine se vide.
		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, ItemStack.EMPTY);
		world.getBlockState(abs).onUse(world, player, hit);
		context.assertTrue(vitrine.getObjet().isEmpty(), "la vitrine est vide après reprise");
		context.assertTrue(player.getInventory().count(LimbesItems.SIFFLET_DE_RAIE) == 1, "le sifflet est revenu dans l'inventaire");
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

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void lesEspecesRegionalesViventEtLeTireurLanceUnEclat(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 0, 2));
		List<net.minecraft.entity.mob.MobEntity> crees = new java.util.ArrayList<>();
		int i = 0;
		for (var entree : fr.leslimbes.registry.LimbesEspeces.types().entrySet()) {
			net.minecraft.entity.mob.MobEntity e = entree.getValue().create(world);
			context.assertTrue(e != null, "création de l'espèce " + entree.getKey());
			// Une grille serrée mais espacée : plus de 24 entités au même point déclenchent les dégâts d'entassement de
			// vanilla, et trop loin de la structure l'entité sort des chunks forcés du test (déchargée).
			// Sept colonnes, pas de 1,6 bloc : la grille tient dans la parcelle du test (les parcelles voisines sont à treize blocs).
			e.refreshPositionAndAngles(pos.getX() - 4.3 + (i % 7) * 1.6, pos.getY() + 0.5, pos.getZ() + 0.5 + (i / 7) * 1.6, 0, 0);
			i++;
			e.setPersistent(); // le monde de test n'a pas de joueur à moins de 128 blocs : sans cela, vanilla retire la créature
			e.setAiDisabled(true); // la grille mélange alliés, soutiens et hostiles : sans IA, personne ne frappe personne
			world.spawnEntity(e);
			crees.add(e);
		}
		context.assertTrue(crees.size() >= 24, "au moins 24 espèces régionales (trouvé " + crees.size() + ")");
		// Tireur : il annonce son tir (état 1) puis lance un éclat vers sa cible.
		fr.leslimbes.entity.region.EnnemiTireurEntity tireur = (fr.leslimbes.entity.region.EnnemiTireurEntity) fr.leslimbes.registry.LimbesEspeces.type("sentinelle_de_sable").create(world);
		tireur.refreshPositionAndAngles(pos.getX() - 1.5, pos.getY(), pos.getZ() - 1.5, 0, 0);
		tireur.setPersistent();
		world.spawnEntity(tireur);
		net.minecraft.entity.passive.SheepEntity cible = net.minecraft.entity.EntityType.SHEEP.create(world);
		cible.refreshPositionAndAngles(pos.getX() - 1.5, pos.getY(), pos.getZ() + 6.5, 0, 0);
		cible.setAiDisabled(true);
		cible.setPersistent();
		world.spawnEntity(cible);
		tireur.shootAt(cible, 1.0F);
		int preparation = tireur.getPreparation();
		context.assertTrue(preparation > 0 && tireur.etatAnimation() == 1, "le tireur annonce son tir");
		context.runAtTick(preparation + 3, () -> {
			List<fr.leslimbes.entity.region.EclatProjectileEntity> eclats = world.getEntitiesByClass(fr.leslimbes.entity.region.EclatProjectileEntity.class, new Box(pos).expand(32), e -> true);
			context.assertTrue(!eclats.isEmpty() || cible.getHealth() < cible.getMaxHealth(), "un éclat a été lancé après la préparation");
			for (net.minecraft.entity.mob.MobEntity e : crees) {
				context.assertTrue(e.isAlive(), "espèce encore vivante après quelques ticks : " + e.getType().getUntranslatedName() + " (vie " + e.getHealth() + ", dernière source "
						+ (e.getRecentDamageSource() == null ? "aucune" : e.getRecentDamageSource().getName() + " par " + e.getRecentDamageSource().getAttacker()) + ", pos " + e.getPos() + ")");
			}
			crees.forEach(Entity::discard);
			eclats.forEach(Entity::discard);
			tireur.discard();
			cible.discard();
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void uneMontureTerrestreConclutUnPacteSeMonteEtRevientAuSifflet(TestContext context) {
		ServerWorld world = context.getWorld();
		net.minecraft.server.network.ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 3, 2));
		player.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		data.montures.clear();
		player.getInventory().clear();
		fr.leslimbes.entity.region.MontureEntity tortue = (fr.leslimbes.entity.region.MontureEntity) fr.leslimbes.registry.LimbesEspeces.type("tortue_des_lagons").create(world);
		tortue.refreshPositionAndAngles(pos.getX() + 2.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		tortue.setPersistent();
		world.spawnEntity(tortue);
		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, ItemStack.EMPTY);
		context.assertTrue(tortue.interactMob(player, net.minecraft.util.Hand.MAIN_HAND) == net.minecraft.util.ActionResult.PASS && !tortue.hasPassengers(), "une tortue sauvage ne se laisse pas monter");
		for (int i = 0; i < 3; i++) {
			player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, new ItemStack(LimbesItems.POISSON_LANTERNE, 1));
			tortue.interactMob(player, net.minecraft.util.Hand.MAIN_HAND);
		}
		context.assertTrue(tortue.isTamed() && player.getUuid().equals(tortue.getOwnerUuid()), "pacte conclu avec trois poissons-lanternes");
		context.assertTrue(tortue.getUuidAsString().equals(data.montures.get("tortue_des_lagons")) && "tortue_des_lagons".equals(fr.leslimbes.entity.region.MontureEntity.cleActive(data)),
				"la tortue est liée et devient la monture active");
		context.assertTrue(player.getInventory().count(LimbesItems.SIFFLET_DE_RAIE) == 1, "le sifflet des montures a été donné");
		player.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, ItemStack.EMPTY);
		net.minecraft.util.ActionResult monte = tortue.interactMob(player, net.minecraft.util.Hand.MAIN_HAND);
		context.assertTrue(monte == net.minecraft.util.ActionResult.CONSUME && tortue.getControllingPassenger() == player && tortue.estMontee(), "le propriétaire monte la tortue (résultat=" + monte + ")");
		context.assertTrue(tortue.canJump() == tortue.isOnGround(), "la tortue saute depuis le sol");
		player.stopRiding();
		tortue.discard();
		fr.leslimbes.entity.region.MontureEntity.rappelerActive(player);
		context.runAtTick(5, () -> {
			List<fr.leslimbes.entity.region.MontureEntity> montures = world.getEntitiesByClass(fr.leslimbes.entity.region.MontureEntity.class, new Box(pos).expand(12), m -> m.isAlive() && m.isTamed());
			context.assertTrue(montures.size() == 1 && montures.get(0).isTamed() && montures.get(0).espece().id.equals("tortue_des_lagons"), "la tortue disparue est recréée par le sifflet (trouvé " + montures.size() + ")");
			context.assertTrue(montures.get(0).getUuidAsString().equals(data.montures.get("tortue_des_lagons")), "le lien pointe vers la nouvelle tortue");
			montures.forEach(Entity::discard);
			world.getServer().getPlayerManager().remove(player);
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void unEnnemiAnnonceFrappeEtEmpoisonneApresPreparation(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 0, 2));
		fr.leslimbes.entity.region.EnnemiAnnonceEntity scorpion = (fr.leslimbes.entity.region.EnnemiAnnonceEntity) fr.leslimbes.registry.LimbesEspeces.type("scorpion_des_heures").create(world);
		scorpion.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		scorpion.setPersistent();
		world.spawnEntity(scorpion);
		net.minecraft.entity.passive.SheepEntity mouton = net.minecraft.entity.EntityType.SHEEP.create(world);
		mouton.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 2.0, 0, 0);
		mouton.setAiDisabled(true);
		mouton.setPersistent();
		world.spawnEntity(mouton);
		float vie = mouton.getHealth();
		scorpion.preparer(mouton);
		context.assertTrue(scorpion.enPreparation() && scorpion.etatAnimation() == 1, "le scorpion annonce son coup");
		context.assertTrue(mouton.getHealth() == vie, "aucun dégât pendant l'annonce");
		context.runAtTick(scorpion.espece().preparation + 2, () -> {
			context.assertTrue(mouton.getHealth() < vie, "le coup annoncé a touché (" + mouton.getHealth() + " < " + vie + " ; préparation restante " + scorpion.getPreparation()
					+ ", coups " + scorpion.coupsPortes + ", touches " + scorpion.dernieresTouches + ", scorpion " + scorpion.getPos() + " vivant=" + scorpion.isAlive() + ", mouton " + mouton.getPos() + ")");
			context.assertTrue(mouton.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.POISON), "le venin du scorpion empoisonne");
			context.assertTrue(scorpion.etatAnimation() == 2, "le scorpion récupère après le coup");
			scorpion.discard();
			mouton.discard();
			context.complete();
		});
	}
	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 400)
	public void unDuelSeDerouleAvecSesPhasesEtSaVictoire(TestContext context) {
		ServerWorld world = context.getWorld();
		net.minecraft.server.network.ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 0, 2));
		player.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		// Un duel défini pour l'arène de test (en plus de ceux du jeu) : boss Aélis, une phase avec renfort et racines.
		String json = "{\"titre\": \"Duel de test\", \"pretendant\": \"aelis\", \"boss\": \"les_limbes:aelis_duel\", \"amical\": true,"
				+ "\"arene\": {\"centre\": [" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "], \"rayon\": 6, \"dimension\": \"" + world.getRegistryKey().getValue() + "\"},"
				+ "\"entree\": [" + pos.getX() + "," + pos.getY() + "," + (pos.getZ() + 4) + "], \"apparition\": [" + pos.getX() + "," + pos.getY() + "," + (pos.getZ() - 3) + "],"
				+ "\"phases\": [{\"seuil\": 0.6, \"message\": \"phase\", \"invocations\": [{\"entite\": \"les_limbes:fleur_de_vie\", \"nombre\": 1}], \"blocs\": \"racines\", \"duree_blocs\": 0}],"
				+ "\"victoire\": [{\"type\": \"drapeau\", \"nom\": \"test_duel_victoire\"}], \"echec\": []}";
		java.util.Map<String, String> defs = new java.util.LinkedHashMap<>();
		defs.put("test_duel", json);
		fr.leslimbes.duel.DuelLoader.chargerDepuisJson(defs);
		context.assertTrue(fr.leslimbes.duel.DuelManager.commencer(player, "test_duel"), "le duel commence");
		fr.leslimbes.duel.Duel duel = fr.leslimbes.duel.DuelManager.duelDe("test_duel");
		context.assertTrue(duel != null && duel.participants.contains(player.getUuid()), "le joueur participe au duel");
		Entity boss = world.getEntity(duel.boss);
		context.assertTrue(boss instanceof fr.leslimbes.entity.duel.PretendantEntity p && p.isAlive() && p.style() == fr.leslimbes.entity.duel.StyleDuel.AELIS, "le boss Aélis est apparu");
		context.assertTrue(player.getBlockPos().isWithinDistance(pos.add(0, 0, 4), 2.5), "le joueur est placé à l'entrée de l'arène");
		LivingEntity vivant = (LivingEntity) boss;
		vivant.setHealth(vivant.getMaxHealth() * 0.5F);
		context.runAtTick(6, () -> {
			context.assertTrue(duel.phase == 1, "la phase 1 est franchie (phase=" + duel.phase + ")");
			context.assertTrue(!duel.renforts.isEmpty(), "un renfort a été invoqué");
			context.assertTrue(duel.blocs.nombre() > 0, "des racines temporaires ont été posées (" + duel.blocs.nombre() + ")");
			context.assertTrue(duel.barre.getPercent() < 0.6F, "la barre de boss suit la vie");
			vivant.kill();
		});
		context.runAtTick(40, () -> {
			context.assertTrue(fr.leslimbes.duel.DuelManager.duelDe("test_duel") == null, "le duel est terminé");
			context.assertTrue(data.aDrapeau("duel_test_duel_gagne") && data.aDrapeau("test_duel_victoire"), "les drapeaux de victoire sont posés");
			context.assertTrue(duel.blocs.nombre() == 0, "les blocs temporaires sont retirés");
			List<fr.leslimbes.entity.region.SoutienEntity> restes = world.getEntitiesByClass(fr.leslimbes.entity.region.SoutienEntity.class, new Box(pos).expand(24), e -> e.isAlive());
			context.assertTrue(restes.isEmpty(), "les renforts sont retirés (" + restes.size() + ")");
			world.getServer().getPlayerManager().remove(player);
			context.complete();
		});
	}

	// ------------------------------------------------------------------ Étape 7 : pouvoirs supérieurs

	private static net.minecraft.server.network.ServerPlayerEntity souverainDeTest(TestContext context, BlockPos pos, String... pouvoirs) {
		net.minecraft.server.network.ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		for (String p : pouvoirs) {
			data.debloquerPouvoir(p);
		}
		data.augmenterMajesteMax(200.0F);
		player.getInventory().clear(); // le carnet de la première connexion occupe sinon la main
		return player;
	}

	private static fr.leslimbes.entity.region.EnnemiAnnonceEntity scorpionDeTest(ServerWorld world, double x, double y, double z) {
		fr.leslimbes.entity.region.EnnemiAnnonceEntity scorpion = (fr.leslimbes.entity.region.EnnemiAnnonceEntity) fr.leslimbes.registry.LimbesEspeces.type("scorpion_des_heures").create(world);
		scorpion.refreshPositionAndAngles(x, y, z, 0, 0);
		scorpion.setPersistent();
		scorpion.setAiDisabled(true);
		world.spawnEntity(scorpion);
		return scorpion;
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void laParadeRenvoieUnCoupEtLaLanceCharge(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 0, 2));
		net.minecraft.server.network.ServerPlayerEntity player = souverainDeTest(context, pos, "epee_du_souverain", "parade_royale", "lance_du_souverain", "charge_de_lance", "arc_du_souverain", "changement_d_arme", "marque_royale");
		PlayerLimbesData data = PlayerLimbesData.of(player);
		fr.leslimbes.entity.region.EnnemiAnnonceEntity scorpion = scorpionDeTest(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 1.6);
		fr.leslimbes.power.EtatPouvoirs etat = fr.leslimbes.power.EtatPouvoirs.de(player);
		// Un joueur fraîchement connecté est invulnérable soixante ticks : la parade se teste après.
		context.runAtTick(70, () -> {
			context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "parade_royale"), "la parade royale s'exécute");
			context.assertTrue(etat.paradeActive(world.getTime()), "la fenêtre de parade est ouverte");
			player.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
			float vie = player.getHealth();
			boolean touche = player.damage(world.getDamageSources().mobAttack(scorpion), 4.0F);
			context.assertTrue(!touche && player.getHealth() == vie, "le coup paré ne blesse pas (" + player.getHealth() + " / " + vie + ")");
			context.assertTrue(scorpion.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.SLOWNESS), "l'assaillant paré est ralenti");
			context.assertTrue(data.compteur("parades_reussies") == 1, "la parade réussie est comptée");
			player.changeGameMode(net.minecraft.world.GameMode.CREATIVE);
		});
		// Lance : invoquée en main, puis la charge blesse ce qui est traversé.
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "lance_du_souverain"), "la lance s'invoque");
		StringBuilder inv = new StringBuilder();
		for (int k = 0; k < player.getInventory().size(); k++) {
			if (!player.getInventory().getStack(k).isEmpty()) {
				inv.append(k).append('=').append(player.getInventory().getStack(k)).append(' ');
			}
		}
		context.assertTrue(player.getMainHandStack().isOf(fr.leslimbes.registry.LimbesItems.LANCE_DU_SOUVERAIN), "la Lance du Souverain est en main (main=" + player.getMainHandStack()
				+ ", slot=" + player.getInventory().selectedSlot + ", inventaire=" + inv + ")");
		float vieScorpion = scorpion.getHealth();
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "charge_de_lance"), "la charge s'exécute lance en main");
		context.runAtTick(76, () -> {
			context.assertTrue(scorpion.getHealth() < vieScorpion, "la charge a blessé le scorpion (" + scorpion.getHealth() + " < " + vieScorpion + ")");
			// Changement d'arme : la lance laisse place à l'arc (ordre épée, lance, arc, bouclier parmi les armes connues).
			context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "changement_d_arme"), "le changement d'arme s'exécute");
			context.assertTrue(player.getMainHandStack().isOf(fr.leslimbes.registry.LimbesItems.ARC_DU_SOUVERAIN), "l'arc remplace la lance en main (" + player.getMainHandStack() + ")");
			context.assertTrue(player.getInventory().count(fr.leslimbes.registry.LimbesItems.LANCE_DU_SOUVERAIN) == 0, "la lance s'est dissipée");
			// Marque royale : la cible visée brille et porte la marque.
			player.lookAt(net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor.EYES, scorpion.getPos().add(0, 0.3, 0));
			context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "marque_royale"), "la marque royale s'exécute sur la cible visée");
			context.assertTrue(scorpion.getUuid().equals(etat.marque) && scorpion.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.GLOWING), "le scorpion porte la marque");
			scorpion.discard();
			world.getServer().getPlayerManager().remove(player);
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 300)
	public void laGardeAstraleRepondAuSouverain(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 0, 2));
		net.minecraft.server.network.ServerPlayerEntity player = souverainDeTest(context, pos, "chevalier_astral", "archer_astral", "protecteur_astral", "ordre_de_garde");
		PlayerLimbesData dg = PlayerLimbesData.of(player);
		for (String id : new String[]{"chevalier_astral", "archer_astral", "protecteur_astral"}) {
			boolean ok = fr.leslimbes.power.Abilities.utiliser(player, id);
			context.assertTrue(ok, "la garde « " + id + " » s'invoque (ability=" + (fr.leslimbes.power.Abilities.get(id) != null) + ", possede=" + dg.possedePouvoir(id)
					+ ", vivant=" + player.isAlive() + ", recharge=" + fr.leslimbes.power.Abilities.rechargeRestante(player, id) + ", majeste=" + dg.majeste + "/" + dg.majesteMax
					+ ", creatif=" + player.isCreative() + ", pos=" + player.getPos() + ", monde=" + player.getServerWorld().getRegistryKey().getValue() + ")");
		}
		// Une quatrième invocation (sans attendre la recharge) dissipe la plus ancienne : le budget tient.
		context.assertTrue(fr.leslimbes.power.GardeAstrale.invoquer(player, "archer_astral"), "une quatrième garde s'invoque");
		List<fr.leslimbes.entity.region.GardeAstraleEntity> gardes = fr.leslimbes.power.GardeAstrale.gardesDe(player, 16.0);
		context.assertTrue(gardes.size() == fr.leslimbes.power.GardeAstrale.BUDGET, "le budget de garde est tenu (" + gardes.size() + " / " + fr.leslimbes.power.GardeAstrale.BUDGET + ")");
		context.assertTrue(gardes.stream().allMatch(g -> player.getUuid().equals(g.getProprietaire())), "chaque garde connaît son souverain");
		context.assertTrue(gardes.stream().noneMatch(g -> g.espece().garde == 0) && gardes.stream().anyMatch(g -> g.espece().garde == 2), "le chevalier, plus ancien, s'est dissipé ; le protecteur est présent");
		fr.leslimbes.power.Abilities.utiliser(player, "ordre_de_garde");
		context.assertTrue(gardes.stream().filter(g -> g.isAlive()).allMatch(g -> g.getOrdre() == fr.leslimbes.entity.region.GardeAstraleEntity.ORDRE_TENIR), "l'ordre « tenir » est transmis à toute la garde");
		// Un ennemi hostile apparaît : la garde le prend pour cible et le frappe ; le souverain ne peut pas blesser sa garde.
		fr.leslimbes.entity.region.EnnemiAnnonceEntity scorpion = scorpionDeTest(world, pos.getX() + 0.5, pos.getY(), pos.getZ() - 2.5);
		fr.leslimbes.entity.region.GardeAstraleEntity premiere = gardes.stream().filter(g -> g.isAlive()).findFirst().orElseThrow();
		float vieGarde = premiere.getHealth();
		premiere.damage(world.getDamageSources().playerAttack(player), 5.0F);
		context.assertTrue(premiere.getHealth() == vieGarde, "le souverain ne blesse pas sa propre garde");
		float vieScorpion = scorpion.getHealth();
		context.runAtTick(120, () -> {
			context.assertTrue(!scorpion.isAlive() || scorpion.getHealth() < vieScorpion, "la garde a frappé l'ennemi (" + scorpion.getHealth() + " / " + vieScorpion + ")");
			premiere.dissiper();
			context.assertTrue(!premiere.isAlive(), "une garde dissipée disparaît sans mourir");
			fr.leslimbes.power.GardeAstrale.gardesDe(player, 32.0).forEach(fr.leslimbes.entity.region.GardeAstraleEntity::dissiper);
			scorpion.discard();
			world.getServer().getPlayerManager().remove(player);
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 200)
	public void lesDecretsImmobilisentDesarmentEtOuvrentLeChemin(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos sol = context.getAbsolutePos(new BlockPos(2, 0, 2));
		BlockPos pos = sol.up(2);
		net.minecraft.server.network.ServerPlayerEntity player = souverainDeTest(context, pos, "decret_restez", "decret_deposez_les_armes", "decret_que_le_chemin_s_ouvre", "decret_elevez_vous", "decret_tenez_bon");
		fr.leslimbes.entity.region.EnnemiAnnonceEntity scorpion = scorpionDeTest(world, sol.getX() + 2.5, sol.getY(), sol.getZ() + 0.5);
		scorpion.setTarget(player);
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "decret_restez"), "« Restez » s'exécute");
		net.minecraft.entity.effect.StatusEffectInstance lenteur = scorpion.getStatusEffect(net.minecraft.entity.effect.StatusEffects.SLOWNESS);
		context.assertTrue(lenteur != null && lenteur.getAmplifier() == 6, "le scorpion est cloué sur place (lenteur " + (lenteur == null ? "absente" : lenteur.getAmplifier()) + ")");
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "decret_deposez_les_armes"), "« Déposez les armes » s'exécute");
		context.assertTrue(scorpion.getTarget() == null && scorpion.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.WEAKNESS), "le scorpion lâche sa cible et faiblit");
		// Le chemin s'ouvre : un pont de lumière devant le souverain, qui s'effacera de lui-même.
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "decret_que_le_chemin_s_ouvre"), "« Que le chemin s'ouvre » s'exécute");
		BlockPos premier = BlockPos.ofFloored(player.getX(), player.getY() - 1, player.getZ() + 2);
		int poses = 0;
		for (int d = 1; d <= 3; d++) {
			BlockPos p = BlockPos.ofFloored(player.getX() - 0.5, player.getY() - 1, player.getZ() + d);
			if (world.getBlockState(p).isOf(fr.leslimbes.registry.LimbesBlocks.PONT_DE_LUMIERE)) {
				poses++;
			}
		}
		context.assertTrue(poses >= 2, "un pont de lumière est posé devant le souverain (" + poses + " blocs sur les trois premiers, " + world.getBlockState(premier) + ")");
		BlockPos unPont = BlockPos.ofFloored(player.getX() - 0.5, player.getY() - 1, player.getZ() + 1);
		context.assertTrue(world.getBlockTickScheduler().isQueued(unPont, fr.leslimbes.registry.LimbesBlocks.PONT_DE_LUMIERE), "la disparition du pont est planifiée");
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "decret_elevez_vous"), "« Élevez-vous » s'exécute");
		context.assertTrue(player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.SLOW_FALLING), "le souverain est porté par une chute lente");
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "decret_tenez_bon"), "« Tenez bon » s'exécute");
		context.assertTrue(player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.RESISTANCE) && player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.ABSORPTION), "le souverain tient bon");
		for (int d = 1; d <= 14; d++) {
			for (double cote : new double[]{-0.5, 0.5}) {
				BlockPos p = BlockPos.ofFloored(player.getX() + cote, player.getY() - 1, player.getZ() + d);
				if (world.getBlockState(p).isOf(fr.leslimbes.registry.LimbesBlocks.PONT_DE_LUMIERE)) {
					world.removeBlock(p, false);
				}
			}
		}
		scorpion.discard();
		world.getServer().getPlayerManager().remove(player);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 300)
	public void leJugementTombeEtLAvenementSeRevele(TestContext context) {
		ServerWorld world = context.getWorld();
		BlockPos pos = context.getAbsolutePos(new BlockPos(2, 0, 2));
		net.minecraft.server.network.ServerPlayerEntity player = souverainDeTest(context, pos, "pas_court", "decret_reculez", "jugement_des_constellations", "marche_du_palais", "avenement", "audience_du_roi");
		PlayerLimbesData data = PlayerLimbesData.of(player);
		fr.leslimbes.entity.region.EnnemiAnnonceEntity a = scorpionDeTest(world, pos.getX() + 3.5, pos.getY(), pos.getZ() + 0.5);
		fr.leslimbes.entity.region.EnnemiAnnonceEntity b = scorpionDeTest(world, pos.getX() - 2.5, pos.getY(), pos.getZ() + 2.5);
		float vieA = a.getHealth();
		float vieB = b.getHealth();
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "jugement_des_constellations"), "le Jugement des constellations s'exécute");
		fr.leslimbes.power.EtatPouvoirs etat = fr.leslimbes.power.EtatPouvoirs.de(player);
		context.assertTrue(etat.etoiles.size() == fr.leslimbes.power.Emblematiques.ETOILES, "sept étoiles sont en route (" + etat.etoiles.size() + ")");
		// Sans couronne, l'Avènement est refusé ; la Marche du Palais rend les Portes gratuites.
		context.assertTrue(!fr.leslimbes.power.Abilities.utiliser(player, "avenement"), "l'Avènement exige la couronne");
		context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "marche_du_palais"), "la Marche du Palais s'exécute");
		context.assertTrue(fr.leslimbes.power.Abilities.coutReel(player, fr.leslimbes.power.Abilities.PAS_COURT) == 0.0F
				&& fr.leslimbes.power.Abilities.rechargeReelle(player, fr.leslimbes.power.Abilities.PAS_COURT) == 0, "pendant la Marche, le Pas court ne coûte rien");
		context.runAtTick(90, () -> {
			context.assertTrue(!a.isAlive() || a.getHealth() < vieA, "une étoile a frappé le premier scorpion (" + a.getHealth() + " / " + vieA + ")");
			context.assertTrue(!b.isAlive() || b.getHealth() < vieB, "une étoile a frappé le second scorpion (" + b.getHealth() + " / " + vieB + ")");
			context.assertTrue(etat.etoiles.isEmpty(), "toutes les étoiles sont tombées");
			data.poserDrapeau("couronne");
			data.poserDrapeau("couronne_assemblee");
			context.assertTrue(fr.leslimbes.power.Abilities.utiliser(player, "avenement"), "l'Avènement se révèle avec la couronne");
			context.assertTrue(fr.leslimbes.power.EtatPouvoirs.avenementActif(player), "l'Avènement est en cours");
			context.assertTrue(fr.leslimbes.registry.LimbesAttachments.niveauCouronne(player) == 3, "l'état public passe au niveau 3 (couronne et manteau), trouvé " + fr.leslimbes.registry.LimbesAttachments.niveauCouronne(player));
			context.assertTrue(fr.leslimbes.power.Abilities.coutReel(player, fr.leslimbes.power.Abilities.DECRET_RECULEZ) == 10.0F
					&& fr.leslimbes.power.Abilities.rechargeReelle(player, fr.leslimbes.power.Abilities.DECRET_RECULEZ) == 50, "pendant l'Avènement, coût et recharge sont divisés par deux");
			context.assertTrue(etat.satellitesActifs(world.getTime()), "les lames satellites accompagnent l'Avènement");
			etat.avenementJusqua = world.getTime() + 1;
			context.runAtTick(96, () -> {
				context.assertTrue(fr.leslimbes.registry.LimbesAttachments.niveauCouronne(player) == 2, "l'Avènement terminé, la couronne assemblée reste (niveau " + fr.leslimbes.registry.LimbesAttachments.niveauCouronne(player) + ")");
				a.discard();
				b.discard();
				world.getServer().getPlayerManager().remove(player);
				context.complete();
			});
		});
	}
}
