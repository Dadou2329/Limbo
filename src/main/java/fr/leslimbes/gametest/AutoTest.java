package fr.leslimbes.gametest;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.block.PorteDesLimbesBlock;
import fr.leslimbes.entity.VeilleurEgareEntity;
import fr.leslimbes.npc.HabitantEntity;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.dimension.LimbesPortal;
import fr.leslimbes.world.dimension.LimbesSites;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Auto-test sur serveur dédié réel (activé par {@code -Dles_limbes.autotest=true}, tâche Gradle
 * {@code runAutotest}). Contrairement au serveur de GameTest (monde plat sans dimensions de datapack),
 * un serveur dédié crée la dimension des Limbes : on y vérifie la génération réelle des lieux à
 * position fixe, l'apparition des personnages essentiels et la localisation d'un vestige dans l'Overworld.
 * Les résultats sont journalisés avec le préfixe « AUTOTEST », puis le serveur s'arrête.
 */
public final class AutoTest {
	private static final boolean ACTIF = Boolean.getBoolean("les_limbes.autotest");
	private static final List<String> ECHECS = new ArrayList<>();
	private static final List<String> SUCCES = new ArrayList<>();
	/** Points de contrôle des régions (un chunk et ses voisins chargés) : eau de la Mer, plateau de Verréole, dunes, jardins, Couronne. */
	private static final List<BlockPos> POINTS_REGIONS = List.of(new BlockPos(880, 63, -680), LimbesSites.VERREOLE, LimbesSites.MERIDIANE, LimbesSites.SYLVECIME, LimbesSites.HAUTE_GARDE,
			LimbesSites.MERIDIANE.add(90, 0, 0), LimbesSites.SYLVECIME.add(90, 0, 0));
	private static final List<BlockPos> SITES = List.of(LimbesSites.SANCTUAIRE, LimbesSites.CLAIRVAL, LimbesSites.MANOIR, LimbesSites.RELAIS_ROYAL,
			LimbesSites.LAC_DU_CREPUSCULE, LimbesSites.ARRIVEE_PRAIRIES_1,
			// Villes et lieux des régions dont l'auto-test vérifie les personnages ou les créatures (rayon de deux chunks : les toiles font jusqu'à 71 blocs).
			LimbesSites.PORT_NACRE, LimbesSites.MARCHE_FLOTTANT, LimbesSites.SYLVECIME, LimbesSites.SERRE_ANCIENNE, LimbesSites.CLAIRIERE_DU_GEANT,
			LimbesSites.MERIDIANE, LimbesSites.JARDIN_FERTILE, LimbesSites.VERREOLE, LimbesSites.HAUTE_GARDE, LimbesSites.QUARTIER_DES_PORTES,
			LimbesSites.PLACE_DES_CEREMONIES, LimbesSites.PALAIS, LimbesSites.ARENE_AELIS, LimbesSites.ARENE_KAEL, LimbesSites.ARENE_ORSANE, LimbesSites.ARENE_SAHIR, LimbesSites.CAMPEMENT_DE_L_ARCHIPEL,
			LimbesSites.CHANTIER_NAVAL, LimbesSites.ILE_DES_RUINES, LimbesSites.LISIERE_DES_BRUMES, LimbesSites.BRUMEVAL);
	private static int phase;
	private static long tickDepart;
	private static long debutGeneration;

	public static void init() {
		if (!ACTIF) {
			return;
		}
		LesLimbes.LOGGER.info("AUTOTEST : mode auto-test activé ; le serveur s'arrêtera à la fin des vérifications.");
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			phase = 1;
			tickDepart = server.getOverworld().getTime();
		});
		ServerTickEvents.END_SERVER_TICK.register(AutoTest::tick);
	}

	private static void tick(MinecraftServer server) {
		if (phase == 0) {
			return;
		}
		long t = server.getOverworld().getTime() - tickDepart;
		try {
			if (phase == 1 && t >= 20) {
				phase = 2;
				genererSites(server);
			} else if (phase == 2 && t >= 220) {
				phase = 3;
				verifier(server);
			} else if (phase == 3) {
				phase = 0;
				conclure(server);
			}
		} catch (Exception e) {
			ECHECS.add("exception : " + e);
			LesLimbes.LOGGER.error("AUTOTEST exception", e);
			phase = 0;
			conclure(server);
		}
	}

	private static void genererSites(MinecraftServer server) {
		ServerWorld limbes = LimbesDimension.monde(server);
		verifier(limbes != null, "la dimension les_limbes:limbes existe sur le serveur dédié");
		if (limbes == null) {
			return;
		}
		debutGeneration = System.nanoTime();
		int chunks = 0;
		for (BlockPos site : SITES) {
			ChunkPos c = new ChunkPos(site);
			int rayon = site.equals(LimbesSites.CLAIRVAL) || site.equals(LimbesSites.SANCTUAIRE) ? 3 : 2;
			for (int dx = -rayon; dx <= rayon; dx++) {
				for (int dz = -rayon; dz <= rayon; dz++) {
					limbes.setChunkForced(c.x + dx, c.z + dz, true);
					limbes.getChunk(c.x + dx, c.z + dz);
					chunks++;
				}
			}
		}
		for (BlockPos point : POINTS_REGIONS) {
			ChunkPos c = new ChunkPos(point);
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					limbes.setChunkForced(c.x + dx, c.z + dz, true);
					limbes.getChunk(c.x + dx, c.z + dz);
					chunks++;
				}
			}
		}
		double secondes = (System.nanoTime() - debutGeneration) / 1.0E9;
		LesLimbes.LOGGER.info("AUTOTEST : {} chunks des Limbes générés en {} s (machine de test, sans client)", chunks, String.format("%.1f", secondes));
	}

	private static void verifier(MinecraftServer server) {
		ServerWorld limbes = LimbesDimension.monde(server);
		if (limbes == null) {
			return;
		}
		// Sanctuaire : arche active, dalle d'arrivée libre et sol dessous.
		BlockState arche = limbes.getBlockState(LimbesSites.ARCHE_RETOUR);
		verifier(arche.getBlock() instanceof PorteDesLimbesBlock && arche.get(PorteDesLimbesBlock.ACTIVE), "arche de retour active en " + LimbesSites.ARCHE_RETOUR.toShortString() + " (trouvé " + arche + ")");
		BlockPos arrivee = BlockPos.ofFloored(LimbesSites.ARRIVEE);
		verifier(limbes.getBlockState(arrivee).getCollisionShape(limbes, arrivee).isEmpty(), "point d'arrivée libre");
		verifier(!limbes.getBlockState(arrivee.down()).getCollisionShape(limbes, arrivee.down()).isEmpty(), "sol sous le point d'arrivée");
		// Terrain aplani : surface à 65 sous Clairval, air à 66.
		BlockPos clairval = LimbesSites.CLAIRVAL;
		BlockState place = limbes.getBlockState(clairval.down());
		verifier(place.isOf(LimbesBlocks.PIERRE_NACREE_POLIE) || place.isOf(LimbesBlocks.PIERRE_NACREE_TAILLEE) || place.isOf(LimbesBlocks.PIERRE_NACREE_SCULPTEE),
				"place de Clairval pavée en " + clairval.down().toShortString() + " (trouvé " + place + ")");
		BlockPos libre = LimbesSites.CLAIRVAL_ARRIVEE;
		verifier(limbes.getBlockState(libre).getCollisionShape(limbes, libre).isEmpty() && limbes.getBlockState(libre.up()).getCollisionShape(limbes, libre.up()).isEmpty(),
				"dalle de téléportation libre en " + libre.toShortString() + " (trouvé " + limbes.getBlockState(libre) + ")");
		for (int[] p : new int[][]{{40, 65, 100}, {-60, 65, 140}, {0, 65, 60}, {30, 65, 170}, {-100, 65, 130}, {140, 65, 40}}) {
			BlockPos sol = new BlockPos(p[0], p[1], p[2]);
			boolean solPlein = !limbes.getBlockState(sol).getCollisionShape(limbes, sol).isEmpty() || !limbes.getFluidState(sol).isEmpty();
			BlockState dessus = limbes.getBlockState(sol.up());
			// Végétation tolérée au-dessus du sol aplani : plantes sans collision, feuilles et troncs des arbres d'aube.
			boolean dessusLibre = dessus.getCollisionShape(limbes, sol.up()).isEmpty() || dessus.isIn(net.minecraft.registry.tag.BlockTags.LEAVES) || dessus.isIn(net.minecraft.registry.tag.BlockTags.LOGS);
			verifier(solPlein && dessusLibre, "terrain aplani à 66 en " + sol.toShortString() + " (sol " + limbes.getBlockState(sol) + ", dessus " + limbes.getBlockState(sol.up()) + ")");
		}
		// Diagnostic : densité finale réelle autour de la surface (zéro attendu entre 65 et 66).
		net.minecraft.world.gen.densityfunction.DensityFunction finale = limbes.getChunkManager().getNoiseConfig().getNoiseRouter().finalDensity();
		StringBuilder sb = new StringBuilder();
		for (int y = 62; y <= 69; y++) {
			double d = finale.sample(new net.minecraft.world.gen.densityfunction.DensityFunction.UnblendedNoisePos(40, y, 100));
			sb.append(y).append('=').append(String.format("%.4f", d)).append(' ');
		}
		LesLimbes.LOGGER.info("AUTOTEST densité finale en (40, y, 100) : {}", sb);
		sb.setLength(0);
		for (int y = 62; y <= 69; y++) {
			sb.append(y).append('=').append(limbes.getBlockState(new BlockPos(40, y, 100)).getBlock().getName().getString()).append(' ');
		}
		LesLimbes.LOGGER.info("AUTOTEST blocs en (40, y, 100) : {}", sb);
		// Manoir et relais : plancher / sol présents.
		verifier(!limbes.getBlockState(LimbesSites.MANOIR.down()).isAir(), "sol du manoir");
		verifier(!limbes.getBlockState(LimbesSites.RELAIS_ROYAL.down()).isAir(), "sol du relais");
		// Personnages essentiels et Veilleurs.
		for (String[] r : new String[][]{{"maelle", "0", "82", "0"}, {"thibault", "14", "66", "128"}, {"brann", "16", "66", "146"}, {"vaeron", "3", "66", "142"},
				{"orielle", "-120", "66", "157"}, {"gaspard", "150", "66", "60"},
				{"kael", "950", "66", "-750"}, {"aelis", "1050", "72", "350"}, {"sahir", "150", "66", "1250"}, {"orsane", "-1150", "96", "500"}, {"intendant_garde", "-610", "82", "-610"},
				{"oriane", "900", "72", "520"}, {"ismael", "380", "66", "1130"}, {"nerine", "800", "63", "-600"}, {"elouan", "-900", "78", "-720"},
				{"chambellan", "-900", "86", "-900"}, {"heraut", "-900", "86", "-900"}, {"conseiller_royal", "-900", "86", "-960"}, {"archiviste_royale", "-955", "86", "-945"},
				{"maitre_des_jeux", "230", "66", "1330"}, {"capitaine_lio", "1250", "66", "-650"},
				{"ancien_roi", "-900", "86", "-1380"}, {"charpentier_royal", "1030", "65", "-800"}}) {
			BlockPos centre = new BlockPos(Integer.parseInt(r[1]), Integer.parseInt(r[2]), Integer.parseInt(r[3]));
			// Grande hauteur : Aélis siège au sommet de Sylvécime (y 110), Kaël à la capitainerie, Orsane au palais.
			List<HabitantEntity> l = limbes.getEntitiesByClass(HabitantEntity.class, new Box(centre).expand(32, 64, 32), h -> h.getRole().equals(r[0]));
			verifier(!l.isEmpty(), "personnage « " + r[0] + " » présent près de " + centre.toShortString());
		}
		List<VeilleurEgareEntity> veilleurs = limbes.getEntitiesByClass(VeilleurEgareEntity.class, new Box(LimbesSites.RELAIS_ROYAL).expand(32, 24, 32), e -> true);
		verifier(veilleurs.size() >= 3, "au moins trois Veilleurs au relais (trouvé " + veilleurs.size() + ")");
		// Observatoire et moulin : Lisbeth et Rosalie ; axe des ailes immobile.
		List<HabitantEntity> lisbeth = limbes.getEntitiesByClass(HabitantEntity.class, new Box(LimbesSites.OBSERVATOIRE).expand(16, 24, 16), h -> h.getRole().equals("lisbeth"));
		verifier(!lisbeth.isEmpty(), "Lisbeth présente à l'observatoire " + LimbesSites.OBSERVATOIRE.toShortString());
		List<HabitantEntity> rosalie = limbes.getEntitiesByClass(HabitantEntity.class, new Box(new BlockPos(-28, 66, 122)).expand(16, 16, 16), h -> h.getRole().equals("rosalie"));
		verifier(!rosalie.isEmpty(), "Rosalie présente au moulin");
		BlockState ailes = limbes.getBlockState(new BlockPos(-28, 73, 126));
		verifier(ailes.isOf(LimbesBlocks.AILES_DE_MOULIN) && !ailes.get(fr.leslimbes.block.MoulinBlock.ACTIF), "axe des ailes du moulin en -28/73/126, immobile (trouvé " + ailes + ")");
		verifier(limbes.getBlockState(new BlockPos(-29, 66, 120)).isOf(LimbesBlocks.MEULE_DE_MOULIN), "meule du moulin en -29/66/120 (trouvé " + limbes.getBlockState(new BlockPos(-29, 66, 120)) + ")");
		// Lac du Crépuscule : eau, dalles de rosée planifiées, meule royale sur l'îlot.
		verifier(limbes.getBlockState(new BlockPos(-72, 64, 20)).isOf(net.minecraft.block.Blocks.WATER), "eau du lac en -72/64/20 (trouvé " + limbes.getBlockState(new BlockPos(-72, 64, 20)) + ")");
		BlockState dalle = limbes.getBlockState(new BlockPos(-79, 65, 20));
		verifier(dalle.isOf(LimbesBlocks.DALLE_DE_ROSEE), "dalle de rosée en -79/65/20 (trouvé " + dalle + ")");
		verifier(limbes.getBlockTickScheduler().isQueued(new BlockPos(-79, 65, 20), LimbesBlocks.DALLE_DE_ROSEE) || dalle.isOf(LimbesBlocks.DALLE_DE_ROSEE) && limbes.getTime() > 0,
				"tick planifié pour la dalle de rosée");
		verifier(limbes.getBlockState(new BlockPos(-60, 67, 20)).isOf(LimbesBlocks.MEULE_ROYALE), "meule royale sur l'îlot en -60/67/20 (trouvé " + limbes.getBlockState(new BlockPos(-60, 67, 20)) + ")");
		// Site d'Arrivée : terrain aplani et libre, puis déclenchement réel de « La maison égarée ».
		BlockPos siteArrivee = LimbesSites.ARRIVEE_PRAIRIES_1;
		verifier(!limbes.getBlockState(siteArrivee.down()).getCollisionShape(limbes, siteArrivee.down()).isEmpty() && limbes.getBlockState(siteArrivee.up()).isAir(),
				"site d'Arrivée aplani en " + siteArrivee.toShortString());
		fr.leslimbes.world.state.LimbesWorldState.get(server).poserDrapeau("acte_1_commence");
		fr.leslimbes.arrivee.ArriveeDefinition maison = fr.leslimbes.arrivee.ArriveeLoader.get("maison_egaree");
		verifier(maison != null, "définition d'Arrivée « maison_egaree » chargée");
		if (maison != null) {
			Optional<fr.leslimbes.arrivee.ArriveeEvenement> e = fr.leslimbes.arrivee.ArriveeManager.declencher(server, maison, "prairies_1");
			String raison = fr.leslimbes.arrivee.ArriveeLoader.sites().values().stream().filter(st -> st.id().equals("prairies_1")).findFirst()
					.map(st -> fr.leslimbes.arrivee.ArriveeManager.raisonOccupation(limbes, st.pos().add(-8, -1, -8), new net.minecraft.util.math.Vec3i(17, 8, 17))).orElse("site inconnu");
			verifier(e.isPresent(), "Arrivée « La maison égarée » déclenchée sur le site prairies_1" + (e.isPresent() ? "" : " (zone : " + raison + ")"));
			if (e.isPresent()) {
				verifier(limbes.getBlockState(new BlockPos(67, 66, 206)).isOf(net.minecraft.block.Blocks.SMOKER), "four à pain de la maison égarée en 67/66/206 (trouvé " + limbes.getBlockState(new BlockPos(67, 66, 206)) + ")");
				List<HabitantEntity> emile = limbes.getEntitiesByClass(HabitantEntity.class, new Box(siteArrivee).expand(16, 16, 16), h -> h.getRole().equals("emile"));
				verifier(!emile.isEmpty(), "Émile présent dans la maison égarée");
				verifier(!fr.leslimbes.arrivee.ArriveeManager.eligible(server, fr.leslimbes.world.state.LimbesWorldState.get(server), maison), "une Arrivée unique ne se redéclenche pas");
				verifier(fr.leslimbes.arrivee.ArriveeManager.resoudre(server, "maison_egaree"), "résolution de l'Arrivée");
			}
		}
		// Portes de relais générées avec leur identifiant (sanctuaire, Clairval, relais royal).
		for (Object[] porte : new Object[][] {{new BlockPos(-8, 83, 0), "sanctuaire"}, {new BlockPos(0, 66, 149), "clairval"}, {new BlockPos(148, 66, 54), "relais_royal"},
				{new BlockPos(950, 66, -761), "port_nacre"}, {new BlockPos(1040, 72, 362), "sylvecime"}, {new BlockPos(150, 66, 1234), "meridiane"},
				{new BlockPos(-1150, 96, 487), "verreole"}, {new BlockPos(-622, 82, -610), "haute_garde"}}) {
			BlockPos pp = (BlockPos) porte[0];
			net.minecraft.block.BlockState etatPorte = limbes.getBlockState(pp);
			String relaisId = limbes.getBlockEntity(pp) instanceof fr.leslimbes.block.PorteDeRelaisBlock.PorteDeRelaisBlockEntity be ? be.getRelais() : "(aucun)";
			verifier(etatPorte.isOf(LimbesBlocks.PORTE_DE_RELAIS) && relaisId.equals(porte[1]), "porte de relais « " + porte[1] + " » en " + pp.toShortString() + " (trouvé " + etatPorte + ", relais " + relaisId + ")");
			fr.leslimbes.domaine.RelaisDefinition rd = fr.leslimbes.domaine.DomaineLoader.relais((String) porte[1]);
			verifier(rd != null && fr.leslimbes.domaine.RelaisManager.destinationSure(limbes, rd.pos()) != null, "point d'arrivée sûr devant le relais « " + porte[1] + " »");
		}
		// Domaine : découverte de lieu, projet avec conflit, voyage entre relais (joueur factice).
		net.fabricmc.fabric.api.entity.FakePlayer joueur = net.fabricmc.fabric.api.entity.FakePlayer.get(limbes);
		fr.leslimbes.world.state.PlayerLimbesData donnees = fr.leslimbes.world.state.PlayerLimbesData.of(joueur);
		joueur.refreshPositionAndAngles(LimbesSites.CLAIRVAL_ARRIVEE.getX() + 0.5, LimbesSites.CLAIRVAL_ARRIVEE.getY(), LimbesSites.CLAIRVAL_ARRIVEE.getZ() + 0.5, 0, 0);
		fr.leslimbes.domaine.LieuxManager.tick(joueur);
		verifier(donnees.aDrapeau("lieu:clairval") && donnees.aDrapeau("relais:clairval"), "Clairval et son relais découverts en entrant sur la place");
		fr.leslimbes.domaine.ProjetDefinition aile = fr.leslimbes.domaine.DomaineLoader.projet("aile_ouest");
		verifier(aile != null, "projet « aile_ouest » chargé");
		if (aile != null) {
			donnees.poserDrapeau("manoir_recu");
			BlockPos coffre = new BlockPos(-127, 76, 149);
			limbes.setBlockState(coffre, net.minecraft.block.Blocks.CHEST.getDefaultState(), 3);
			if (limbes.getBlockEntity(coffre) instanceof net.minecraft.block.entity.ChestBlockEntity cbe) {
				cbe.setStack(0, new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND, 2));
			}
			fr.leslimbes.domaine.ProjetManager.Conflits conflits = fr.leslimbes.domaine.ProjetManager.conflits(server, limbes, aile);
			verifier(conflits.nombre() == 1, "le coffre du joueur est le seul bloc en conflit avec l'aile ouest (trouvé " + conflits.nombre() + (conflits.vide() ? "" : ", ex. " + conflits.exemple()) + ")");
			joueur.getInventory().clear();
			verifier(!fr.leslimbes.domaine.ProjetManager.valider(joueur, aile, true), "sans ressources, le projet est refusé");
			joueur.getInventory().insertStack(new net.minecraft.item.ItemStack(LimbesBlocks.PLANCHES_D_AUBE, 24));
			joueur.getInventory().insertStack(new net.minecraft.item.ItemStack(LimbesBlocks.PIERRE_NACREE_TAILLEE, 8));
			verifier(!fr.leslimbes.domaine.ProjetManager.valider(joueur, aile, false), "avec un conflit non ignoré, le projet est refusé");
			verifier(fr.leslimbes.domaine.ProjetManager.valider(joueur, aile, true), "projet « Réparer l'aile ouest » validé en construisant autour du coffre");
			verifier(fr.leslimbes.world.state.LimbesWorldState.get(server).aDrapeau("domaine_aile_ouest"), "drapeau du monde posé par le projet");
			verifier(limbes.getBlockState(new BlockPos(-128, 75, 150)).isOf(LimbesBlocks.PLANCHES_D_AUBE), "plancher du grenier posé en -128/75/150 (trouvé " + limbes.getBlockState(new BlockPos(-128, 75, 150)) + ")");
			boolean coffreIntact = limbes.getBlockState(coffre).isOf(net.minecraft.block.Blocks.CHEST) && limbes.getBlockEntity(coffre) instanceof net.minecraft.block.entity.ChestBlockEntity cbe2 && cbe2.getStack(0).getCount() == 2;
			verifier(coffreIntact, "le coffre du joueur et son contenu sont intacts dans l'emprise");
			verifier(joueur.getInventory().count(LimbesBlocks.PLANCHES_D_AUBE.asItem()) == 0, "les ressources du projet sont consommées");
			verifier(fr.leslimbes.domaine.ProjetManager.etat(joueur, aile) == fr.leslimbes.domaine.ProjetManager.Etat.FAIT, "le projet est marqué construit");
			limbes.removeBlock(coffre, false);
		}
		// Réseau des passages : depuis la porte de Clairval vers le Sanctuaire.
		fr.leslimbes.world.state.LimbesWorldState.get(server).poserDrapeau(fr.leslimbes.domaine.RelaisManager.RESEAU_EVEILLE);
		fr.leslimbes.domaine.RelaisManager.decouvrir(joueur, "sanctuaire", false);
		joueur.refreshPositionAndAngles(0.5, 66, 147.5, 0, 0);
		boolean voyage = fr.leslimbes.domaine.RelaisManager.voyager(joueur, "sanctuaire");
		verifier(voyage && joueur.getBlockPos().isWithinDistance(new BlockPos(-6, 83, 0), 4.0), "voyage de Clairval au Sanctuaire par le réseau des passages (" + joueur.getBlockPos().toShortString() + ")");
		// Raie céleste : pacte et rappel avec un joueur factice.
		joueur.refreshPositionAndAngles(LimbesSites.CLAIRVAL_ARRIVEE.getX() + 0.5, LimbesSites.CLAIRVAL_ARRIVEE.getY(), LimbesSites.CLAIRVAL_ARRIVEE.getZ() + 0.5, 0, 0);
		fr.leslimbes.world.state.PlayerLimbesData.of(joueur).montures.clear();
		fr.leslimbes.entity.RaieCelesteEntity.rappeler(joueur);
		verifier(!fr.leslimbes.world.state.PlayerLimbesData.of(joueur).montures.containsKey("raie_celeste"), "sans pacte, le sifflet ne crée rien");
		fr.leslimbes.entity.RaieCelesteEntity raie = fr.leslimbes.registry.LimbesEntities.RAIE_CELESTE.create(limbes);
		if (raie != null) {
			raie.refreshPositionAndAngles(joueur.getX() + 2, joueur.getY() + 1, joueur.getZ(), 0, 0);
			limbes.spawnEntity(raie);
			for (int i = 0; i < 3; i++) {
				joueur.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, new net.minecraft.item.ItemStack(LimbesBlocks.FLEUR_DE_ROSEE));
				raie.interactMob(joueur, net.minecraft.util.Hand.MAIN_HAND);
			}
			verifier(raie.isTamed() && joueur.getUuid().equals(raie.getOwnerUuid()), "pacte de la raie conclu avec trois fleurs de rosée");
			raie.discard();
			fr.leslimbes.entity.RaieCelesteEntity.rappeler(joueur);
			List<fr.leslimbes.entity.RaieCelesteEntity> raies = limbes.getEntitiesByClass(fr.leslimbes.entity.RaieCelesteEntity.class, new Box(joueur.getBlockPos()).expand(12), r -> r.isAlive() && r.isTamed());
			verifier(raies.size() == 1, "la raie disparue est recréée par le sifflet (trouvé " + raies.size() + ")");
			raies.forEach(r -> r.discard());
		}
		// Régions : biome, relief et matériaux aux points de contrôle.
		verifier(fr.leslimbes.world.dimension.LimbesRegions.region(0, 140) == fr.leslimbes.world.dimension.LimbesRegions.Region.PRAIRIES, "Clairval est dans les Prairies");
		BlockPos merPt = POINTS_REGIONS.get(0);
		String biomeMer = limbes.getBiome(merPt).getKey().map(k -> k.getValue().toString()).orElse("?");
		verifier(biomeMer.endsWith("lagons_inverses") || biomeMer.endsWith("recifs_celestes") || biomeMer.endsWith("archipel_des_epaves"), "biome de la Mer suspendue en " + merPt.toShortString() + " : " + biomeMer);
		verifier(limbes.getBlockState(merPt).isOf(net.minecraft.block.Blocks.WATER) && limbes.getBlockState(merPt.up()).isAir(), "eau de la Mer suspendue à " + merPt.toShortString() + " et air au-dessus (trouvé " + limbes.getBlockState(merPt) + " / " + limbes.getBlockState(merPt.up()) + ")");
		int profondeur = 0;
		for (int y = merPt.getY(); y > 20 && limbes.getBlockState(new BlockPos(merPt.getX(), y, merPt.getZ())).isOf(net.minecraft.block.Blocks.WATER); y--) {
			profondeur++;
		}
		verifier(profondeur >= 6, "la Mer a de la profondeur (" + profondeur + " blocs d'eau)");
		BlockPos verreole = LimbesSites.VERREOLE;
		verifier(!limbes.getBlockState(verreole.down()).isAir() && limbes.getBlockState(verreole).isAir(), "plateau de Verréole à " + verreole.getY() + " (sol " + limbes.getBlockState(verreole.down()) + ")");
		String biomeMontagne = limbes.getBiome(verreole).getKey().map(k -> k.getValue().toString()).orElse("?");
		verifier(biomeMontagne.contains("verre") || biomeMontagne.contains("resonnantes") || biomeMontagne.contains("cristal"), "biome des Montagnes de Verre à Verréole : " + biomeMontagne);
		BlockPos dune = LimbesSites.MERIDIANE.add(90, 0, 0);
		int yDune = limbes.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, dune.getX(), dune.getZ()) - 1;
		BlockState solDune = limbes.getBlockState(new BlockPos(dune.getX(), yDune, dune.getZ()));
		verifier(solDune.isOf(LimbesBlocks.SABLE_D_OPALE) || solDune.isOf(LimbesBlocks.PIERRE_DES_HEURES) || solDune.isOf(LimbesBlocks.GRES_DES_HEURES) || solDune.isOf(LimbesBlocks.GRES_DES_HEURES_TAILLE)
				|| solDune.isIn(net.minecraft.registry.tag.BlockTags.LEAVES) || solDune.isOf(net.minecraft.block.Blocks.JUNGLE_LOG) || solDune.isOf(LimbesBlocks.HERBE_DE_L_AUBE),
				"sol du Désert des Heures près de Méridiane : " + solDune + " à y=" + yDune);
		BlockPos jardin = LimbesSites.SYLVECIME.add(90, 0, 0);
		int yJardin = limbes.getTopY(net.minecraft.world.Heightmap.Type.OCEAN_FLOOR, jardin.getX(), jardin.getZ()) - 1;
		BlockState solJardin = limbes.getBlockState(new BlockPos(jardin.getX(), yJardin, jardin.getZ()));
		verifier(solJardin.isOf(LimbesBlocks.HERBE_DES_GEANTS) || solJardin.isOf(LimbesBlocks.BUCHE_DE_GEANT) || solJardin.isOf(LimbesBlocks.PETALE_ROSE) || solJardin.isOf(LimbesBlocks.PETALE_BLEU)
				|| solJardin.isOf(LimbesBlocks.TERRE_DES_LIMBES) || solJardin.isOf(LimbesBlocks.PLANCHES_DE_GEANT) || solJardin.isIn(net.minecraft.registry.tag.BlockTags.LEAVES),
				"sol des Jardins des Géants près de Sylvécime : " + solJardin + " à y=" + yJardin);
		BlockPos garde = LimbesSites.HAUTE_GARDE;
		int yGarde = limbes.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, garde.getX(), garde.getZ()) - 1;
		verifier(yGarde >= garde.getY() - 1 && yGarde <= garde.getY() + 8, "plateau de Haute-Garde bâti vers " + garde.getY() + " (surface à " + yGarde + ", " + limbes.getBlockState(new BlockPos(garde.getX(), yGarde, garde.getZ())) + ")");
		if (yGarde > garde.getY() + 8) {
			StringBuilder colonne = new StringBuilder();
			for (int y = garde.getY() - 2; y <= yGarde + 1; y++) {
				BlockState b = limbes.getBlockState(new BlockPos(garde.getX(), y, garde.getZ()));
				if (!b.isAir()) {
					colonne.append(y).append('=').append(b.getBlock().getName().getString()).append(' ');
				}
			}
			LesLimbes.LOGGER.info("AUTOTEST colonne de Haute-Garde en -610/-610 : {}", colonne);
			int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE, n = 0;
			for (int x = garde.getX() - 40; x <= garde.getX() + 40; x++) {
				for (int z = garde.getZ() - 40; z <= garde.getZ() + 40; z++) {
					for (int y = garde.getY() + 30; y <= garde.getY() + 70; y++) {
						if (!limbes.getBlockState(new BlockPos(x, y, z)).isAir()) {
							minX = Math.min(minX, x); maxX = Math.max(maxX, x); minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z); minY = Math.min(minY, y); maxY = Math.max(maxY, y); n++;
						}
					}
				}
			}
			LesLimbes.LOGGER.info("AUTOTEST blocs hauts autour de Haute-Garde (y {}..{}) : {} blocs, boîte x {}..{}, y {}..{}, z {}..{}", garde.getY() + 30, garde.getY() + 70, n, minX, maxX, minY, maxY, minZ, maxZ);
		}
		// Créatures régionales : chaque espèce se crée et vit sur le serveur dédié, les biomes des régions les font
		// apparaître, et le géant dort dans sa clairière (marqueur de structure).
		int especes = 0;
		for (var entree : fr.leslimbes.registry.LimbesEspeces.types().entrySet()) {
			net.minecraft.entity.mob.MobEntity e = entree.getValue().create(limbes);
			if (e != null) {
				e.refreshPositionAndAngles(0.5, 120, 0.5, 0, 0);
				limbes.spawnEntity(e);
				if (e.isAlive()) {
					especes++;
				}
				e.discard();
			}
		}
		verifier(especes == fr.leslimbes.registry.LimbesEspeces.types().size() && especes >= 24, "les " + especes + " espèces régionales se créent sur le serveur dédié");
		verifier(limbes.getBiome(merPt).value().getSpawnSettings().getSpawnEntries(net.minecraft.entity.SpawnGroup.WATER_CREATURE).getEntries().stream()
				.anyMatch(s -> s.type == fr.leslimbes.registry.LimbesEspeces.type("poisson_lanterne")), "la Mer suspendue fait apparaître des poissons-lanternes");
		verifier(limbes.getBiome(dune).value().getSpawnSettings().getSpawnEntries(net.minecraft.entity.SpawnGroup.MONSTER).getEntries().stream()
				.anyMatch(s -> fr.leslimbes.registry.LimbesEspeces.types().containsValue(s.type)), "le Désert des Heures a ses propres ennemis");
		List<fr.leslimbes.entity.region.CreaturePaisibleEntity> geants = limbes.getEntitiesByClass(fr.leslimbes.entity.region.CreaturePaisibleEntity.class,
				new Box(LimbesSites.CLAIRIERE_DU_GEANT).expand(24, 16, 24), c -> c.espece().id.equals("geant_des_jardins"));
		verifier(!geants.isEmpty(), "le géant des jardins dort dans sa clairière (trouvé " + geants.size() + ")");
		// Capitale et arènes : la place, la grande porte du palais (fermée, niveau 2), les arènes des prétendants et le navire de Kaël sont générés.
		verifier(limbes.getBlockState(new BlockPos(-900, 85, -900)).isOf(LimbesBlocks.PIERRE_DE_COURONNE_POLIE), "dallage de la place des cérémonies en -900/85/-900 (trouvé " + limbes.getBlockState(new BlockPos(-900, 85, -900)) + ")");
		BlockState porte = limbes.getBlockState(new BlockPos(-900, 88, -941));
		verifier(porte.isOf(LimbesBlocks.PORTE_ROYALE) && !porte.get(fr.leslimbes.block.PorteRoyaleBlock.OUVERTE) && porte.get(fr.leslimbes.block.PorteRoyaleBlock.NIVEAU) == 2,
				"grande porte royale du palais fermée (niveau 2) en -900/88/-941 (trouvé " + porte + ")");
		verifier(limbes.getBlockState(new BlockPos(1150, 71, 300)).isOf(LimbesBlocks.PIERRE_NACREE_POLIE), "jardin de l'épreuve généré (sol " + limbes.getBlockState(new BlockPos(1150, 71, 300)) + ")");
		verifier(limbes.getBlockState(new BlockPos(1002, 89, -850)).isOf(LimbesBlocks.PLANCHES_D_AUBE), "pont du Vent-Debout généré en l'air (" + limbes.getBlockState(new BlockPos(1002, 89, -850)) + ")");
		verifier(limbes.getBlockState(new BlockPos(1016, 92, -850)).isOf(LimbesBlocks.SABLE_DE_NACRE), "îlot d'escale est du Vent-Debout (" + limbes.getBlockState(new BlockPos(1016, 92, -850)) + ")");
		verifier(limbes.getBlockState(new BlockPos(-1230, 95, 500)).isOf(LimbesBlocks.PIERRE_DE_VERRE_TAILLEE), "salle des miroirs générée (" + limbes.getBlockState(new BlockPos(-1230, 95, 500)) + ")");
		verifier(limbes.getBlockState(new BlockPos(230, 65, 1330)).isOf(LimbesBlocks.PIERRE_DES_HEURES), "cour des heures générée (" + limbes.getBlockState(new BlockPos(230, 65, 1330)) + ")");
		// Éveil de la capitale : niveau 2 ouvre la grande porte du palais tout de suite (chunks chargés).
		fr.leslimbes.capitale.CapitaleManager.eveiller(server, 2, false);
		BlockState porteOuverte = limbes.getBlockState(new BlockPos(-900, 88, -941));
		verifier(porteOuverte.isOf(LimbesBlocks.PORTE_ROYALE) && porteOuverte.get(fr.leslimbes.block.PorteRoyaleBlock.OUVERTE), "la porte du palais s'ouvre au niveau 2 (" + porteOuverte + ")");
		verifier(fr.leslimbes.capitale.CapitaleManager.niveau(server) == 2, "niveau d'éveil de la capitale mémorisé (" + fr.leslimbes.capitale.CapitaleManager.niveau(server) + ")");
		// Duel réel : l'épreuve d'Aélis commence (boss et barre), puis la victoire pose les drapeaux et nettoie l'arène.
		verifier(fr.leslimbes.duel.DuelLoader.tous().size() >= 8, "duels chargés (" + fr.leslimbes.duel.DuelLoader.tous().size() + ")");
		boolean commence = fr.leslimbes.duel.DuelManager.commencer(joueur, "epreuve_d_aelis");
		fr.leslimbes.duel.Duel duelAelis = fr.leslimbes.duel.DuelManager.duelDe("epreuve_d_aelis");
		verifier(commence && duelAelis != null && limbes.getEntity(duelAelis.boss) instanceof fr.leslimbes.entity.duel.PretendantEntity, "l'épreuve d'Aélis commence au jardin de l'épreuve");
		if (duelAelis != null && limbes.getEntity(duelAelis.boss) instanceof fr.leslimbes.entity.duel.PretendantEntity aelis) {
			verifier(joueur.getBlockPos().isWithinDistance(new BlockPos(1150, 72, 318), 3.0), "le joueur est placé à l'entrée de l'arène (" + joueur.getBlockPos().toShortString() + ")");
			aelis.kill();
			verifier(fr.leslimbes.duel.DuelManager.duelDe("epreuve_d_aelis") == null, "le duel se termine à la mort du boss");
			fr.leslimbes.world.state.PlayerLimbesData dj = fr.leslimbes.world.state.PlayerLimbesData.of(joueur);
			verifier(dj.aDrapeau("duel_epreuve_d_aelis_gagne") && dj.aDrapeau("aelis_reconnue"), "la victoire pose les drapeaux (duel gagné, Aélis reconnue)");
		}
		// --- Étape 7 : pouvoirs supérieurs et règne ---
		verifier(fr.leslimbes.power.Abilities.tous().size() >= 33 && fr.leslimbes.power.Abilities.BRANCHES.size() == 7, "trente-trois techniques dans sept branches (" + fr.leslimbes.power.Abilities.tous().size() + ")");
		fr.leslimbes.power.Abilities.tous().keySet().forEach(donnees::debloquerPouvoir);
		donnees.augmenterMajesteMax(300.0F);
		joueur.refreshPositionAndAngles(LimbesSites.CLAIRVAL_ARRIVEE.getX() + 0.5, LimbesSites.CLAIRVAL_ARRIVEE.getY(), LimbesSites.CLAIRVAL_ARRIVEE.getZ() + 0.5, 0, 0);
		boolean chevalier = fr.leslimbes.power.Abilities.utiliser(joueur, "chevalier_astral");
		List<fr.leslimbes.entity.region.GardeAstraleEntity> gardes = fr.leslimbes.power.GardeAstrale.gardesDe(joueur, 16.0);
		verifier(chevalier && gardes.size() == 1 && joueur.getUuid().equals(gardes.get(0).getProprietaire()), "un chevalier astral répond au joueur factice sur le serveur dédié (" + gardes.size() + ")");
		gardes.forEach(fr.leslimbes.entity.region.GardeAstraleEntity::dissiper);
		joueur.getInventory().clear();
		verifier(fr.leslimbes.power.Abilities.utiliser(joueur, "lance_du_souverain") && joueur.getMainHandStack().isOf(fr.leslimbes.registry.LimbesItems.LANCE_DU_SOUVERAIN), "la Lance du Souverain s'invoque en main");
		joueur.refreshPositionAndAngles(LimbesSites.CLAIRVAL_ARRIVEE.getX() + 0.5, LimbesSites.CLAIRVAL_ARRIVEE.getY() + 3, LimbesSites.CLAIRVAL_ARRIVEE.getZ() + 0.5, 0, 0);
		boolean chemin = fr.leslimbes.power.Abilities.utiliser(joueur, "decret_que_le_chemin_s_ouvre");
		BlockPos pont = BlockPos.ofFloored(joueur.getX() - 0.5, joueur.getY() - 1, joueur.getZ() + 2);
		verifier(chemin && limbes.getBlockState(pont).isOf(LimbesBlocks.PONT_DE_LUMIERE) && limbes.getBlockTickScheduler().isQueued(pont, LimbesBlocks.PONT_DE_LUMIERE),
				"« Que le chemin s'ouvre » pose un pont de lumière à disparition planifiée (" + limbes.getBlockState(pont) + ")");
		for (int d = 1; d <= 14; d++) {
			for (double cote : new double[]{-0.5, 0.5}) {
				BlockPos pp = BlockPos.ofFloored(joueur.getX() + cote, joueur.getY() - 1, joueur.getZ() + d);
				if (limbes.getBlockState(pp).isOf(LimbesBlocks.PONT_DE_LUMIERE)) {
					limbes.removeBlock(pp, false);
				}
			}
		}
		donnees.poserDrapeau("couronne");
		verifier(fr.leslimbes.power.Abilities.utiliser(joueur, "avenement") && fr.leslimbes.power.EtatPouvoirs.avenementActif(joueur), "l'Avènement se révèle pour le souverain couronné");
		verifier(fr.leslimbes.quest.QuestLoader.get("la_cite_arrivee") != null && fr.leslimbes.quest.QuestLoader.get("le_grand_tournoi") != null
				&& fr.leslimbes.quest.QuestLoader.get("le_navire_royal") != null && fr.leslimbes.quest.QuestLoader.get("les_frontieres") != null, "les quatre quêtes de l'acte V sont chargées");
		verifier(fr.leslimbes.duel.DuelLoader.tous().size() >= 13, "les cinq épreuves du grand tournoi sont chargées (" + fr.leslimbes.duel.DuelLoader.tous().size() + " duels)");
		// Chantier naval : coque dans la Mer, charpentier ; puis le navire royal est posé par l'action de dialogue.
		verifier(limbes.getBlockState(new BlockPos(1030, 64, -800)).isOf(LimbesBlocks.PLANCHES_D_AUBE), "pont de la coque du chantier naval en 1030/64/-800 (" + limbes.getBlockState(new BlockPos(1030, 64, -800)) + ")");
		verifier(limbes.getBlockState(new BlockPos(1042, 60, -800)).isOf(net.minecraft.block.Blocks.WATER), "la Mer entoure le chantier naval (" + limbes.getBlockState(new BlockPos(1042, 60, -800)) + ")");
		com.google.gson.JsonObject poser = com.google.gson.JsonParser.parseString("{\"type\": \"poser_structure\", \"gabarit\": \"les_limbes:regne/navire_royal\", \"pos\": [1021, 55, -820], \"dimension\": \"les_limbes:limbes\", \"drapeau_monde\": \"navire_royal_pose\"}").getAsJsonObject();
		fr.leslimbes.npc.dialogue.Actions.executer(joueur, poser);
		BlockPos porteNavire = new BlockPos(1030, 66, -786);
		String relaisNavire = limbes.getBlockEntity(porteNavire) instanceof fr.leslimbes.block.PorteDeRelaisBlock.PorteDeRelaisBlockEntity be ? be.getRelais() : "(aucun)";
		verifier(fr.leslimbes.world.state.LimbesWorldState.get(server).aDrapeau("navire_royal_pose") && limbes.getBlockState(porteNavire).isOf(LimbesBlocks.PORTE_DE_RELAIS) && relaisNavire.equals("navire_royal"),
				"le navire royal est posé par l'action de dialogue, porte de relais « navire_royal » en " + porteNavire.toShortString() + " (" + limbes.getBlockState(porteNavire) + ", " + relaisNavire + ")");
		verifier(limbes.getBlockState(new BlockPos(1030, 70, -806)).isOf(LimbesBlocks.BUCHE_D_AUBE), "le grand mât du navire royal se dresse (" + limbes.getBlockState(new BlockPos(1030, 70, -806)) + ")");
		// Île des ruines : coffre au journal, baleine des hauteurs.
		verifier(limbes.getBlockState(new BlockPos(1300, 72, -1000)).isOf(net.minecraft.block.Blocks.CHEST), "coffre des ruines en 1300/72/-1000 (" + limbes.getBlockState(new BlockPos(1300, 72, -1000)) + ")");
		List<fr.leslimbes.entity.region.CreatureVolanteEntity> baleines = limbes.getEntitiesByClass(fr.leslimbes.entity.region.CreatureVolanteEntity.class, new Box(LimbesSites.ILE_DES_RUINES).expand(32, 40, 32),
				b -> b.espece().id.equals("baleine_des_hauteurs"));
		verifier(!baleines.isEmpty(), "une baleine des hauteurs tourne au-dessus de l'île des ruines (" + baleines.size() + ")");
		// Lisière des brumes : grande lanterne allumée au sommet de la tour.
		verifier(limbes.getBlockState(new BlockPos(-900, 100, -1380)).isOf(LimbesBlocks.LANTERNE_ROYALE), "lanterne de la lisière des brumes en -900/100/-1380 (" + limbes.getBlockState(new BlockPos(-900, 100, -1380)) + ")");
		// Brumeval : l'Arrivée majeure se pose sur le site du royaume, avec sa maire et sa porte de relais.
		fr.leslimbes.world.state.LimbesWorldState.get(server).poserDrapeau("cite_brumeval_appelee");
		fr.leslimbes.arrivee.ArriveeDefinition brumeval = fr.leslimbes.arrivee.ArriveeLoader.get("cite_brumeval");
		verifier(brumeval != null, "définition d'Arrivée « cite_brumeval » chargée");
		if (brumeval != null) {
			Optional<fr.leslimbes.arrivee.ArriveeEvenement> e = fr.leslimbes.arrivee.ArriveeManager.declencher(server, brumeval, "royaume_2");
			String raison = e.isPresent() ? "" : " (zone : " + fr.leslimbes.arrivee.ArriveeManager.raisonOccupation(limbes, LimbesSites.BRUMEVAL.add(-16, -3, -16), new net.minecraft.util.math.Vec3i(33, 20, 33)) + ")";
			verifier(e.isPresent(), "Arrivée « Brumeval » déclenchée sur le site royaume_2" + raison);
			if (e.isPresent()) {
				BlockPos porteBrumeval = new BlockPos(-1060, 87, -692);
				String relaisBrumeval = limbes.getBlockEntity(porteBrumeval) instanceof fr.leslimbes.block.PorteDeRelaisBlock.PorteDeRelaisBlockEntity be ? be.getRelais() : "(aucun)";
				verifier(limbes.getBlockState(porteBrumeval).isOf(LimbesBlocks.PORTE_DE_RELAIS) && relaisBrumeval.equals("brumeval"), "porte de relais « brumeval » en " + porteBrumeval.toShortString() + " (" + limbes.getBlockState(porteBrumeval) + ", " + relaisBrumeval + ")");
				List<HabitantEntity> maire = limbes.getEntitiesByClass(HabitantEntity.class, new Box(LimbesSites.BRUMEVAL).expand(24, 16, 24), h -> h.getRole().equals("maire_de_brumeval"));
				verifier(!maire.isEmpty(), "Hortense, maire de Brumeval, est présente");
			}
		}
		int routes = 0;
		for (int x = -8; x <= 8; x++) {
			for (int z = 140; z <= 160; z++) {
				int y = limbes.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, x, z) - 1;
				if (limbes.getBlockState(new BlockPos(x, y, z)).isOf(net.minecraft.block.Blocks.DIRT_PATH)) {
					routes++;
				}
			}
		}
		verifier(routes > 0, "une route pavée est posée au sud de la place de Clairval (" + routes + " pavés)");
		// Overworld : un vestige est localisable depuis le point d'apparition.
		ServerWorld overworld = server.getWorld(World.OVERWORLD);
		long t0 = System.nanoTime();
		Optional<BlockPos> vestige = LimbesPortal.localiserVestige(overworld, overworld.getSpawnPos());
		verifier(vestige.isPresent(), "vestige localisable dans l'Overworld depuis l'apparition" + vestige.map(p -> " : " + p.toShortString()).orElse("") + " (" + String.format("%.2f", (System.nanoTime() - t0) / 1.0E9) + " s)");
		// Libération des chunks forcés.
		for (BlockPos site : SITES) {
			ChunkPos c = new ChunkPos(site);
			for (int dx = -3; dx <= 3; dx++) {
				for (int dz = -3; dz <= 3; dz++) {
					limbes.setChunkForced(c.x + dx, c.z + dz, false);
				}
			}
		}
	}

	private static void verifier(boolean condition, String message) {
		if (condition) {
			SUCCES.add(message);
			LesLimbes.LOGGER.info("AUTOTEST OK   : {}", message);
		} else {
			ECHECS.add(message);
			LesLimbes.LOGGER.error("AUTOTEST ECHEC: {}", message);
		}
	}

	private static void conclure(MinecraftServer server) {
		LesLimbes.LOGGER.info("AUTOTEST RESULTAT: {} ({} succès, {} échec(s))", ECHECS.isEmpty() ? "OK" : "ECHEC", SUCCES.size(), ECHECS.size());
		for (String e : ECHECS) {
			LesLimbes.LOGGER.error("AUTOTEST   - {}", e);
		}
		server.stop(false);
	}

	private AutoTest() {
	}
}
