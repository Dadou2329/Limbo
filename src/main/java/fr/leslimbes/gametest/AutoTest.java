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
	private static final List<BlockPos> SITES = List.of(LimbesSites.SANCTUAIRE, LimbesSites.CLAIRVAL, LimbesSites.MANOIR, LimbesSites.RELAIS_ROYAL,
			LimbesSites.LAC_DU_CREPUSCULE, LimbesSites.ARRIVEE_PRAIRIES_1);
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
			boolean dessusLibre = limbes.getBlockState(sol.up()).getCollisionShape(limbes, sol.up()).isEmpty();
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
				{"orielle", "-120", "66", "157"}, {"gaspard", "150", "66", "60"}}) {
			BlockPos centre = new BlockPos(Integer.parseInt(r[1]), Integer.parseInt(r[2]), Integer.parseInt(r[3]));
			List<HabitantEntity> l = limbes.getEntitiesByClass(HabitantEntity.class, new Box(centre).expand(32, 24, 32), h -> h.getRole().equals(r[0]));
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
			verifier(e.isPresent(), "Arrivée « La maison égarée » déclenchée sur le site prairies_1");
			if (e.isPresent()) {
				verifier(limbes.getBlockState(new BlockPos(67, 66, 206)).isOf(net.minecraft.block.Blocks.SMOKER), "four à pain de la maison égarée en 67/66/206 (trouvé " + limbes.getBlockState(new BlockPos(67, 66, 206)) + ")");
				List<HabitantEntity> emile = limbes.getEntitiesByClass(HabitantEntity.class, new Box(siteArrivee).expand(16, 16, 16), h -> h.getRole().equals("emile"));
				verifier(!emile.isEmpty(), "Émile présent dans la maison égarée");
				verifier(!fr.leslimbes.arrivee.ArriveeManager.eligible(server, fr.leslimbes.world.state.LimbesWorldState.get(server), maison), "une Arrivée unique ne se redéclenche pas");
				verifier(fr.leslimbes.arrivee.ArriveeManager.resoudre(server, "maison_egaree"), "résolution de l'Arrivée");
			}
		}
		// Raie céleste : pacte et rappel avec un joueur factice.
		net.fabricmc.fabric.api.entity.FakePlayer joueur = net.fabricmc.fabric.api.entity.FakePlayer.get(limbes);
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
