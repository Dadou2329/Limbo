package fr.leslimbes;

import fr.leslimbes.item.BoussoleNacreeItem;
import fr.leslimbes.npc.dialogue.Conditions;
import fr.leslimbes.quest.QuestProgress;
import fr.leslimbes.world.gen.FlattenDensityFunction;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Logique pure : progression, comparaisons, aplanissement du terrain, sérialisation des données joueur. */
class LogiqueTest {
	@Test
	void lesRegionsPlacentLesSitesEtGardentLesPrairiesInchangees() {
		Object[][] sites = {
				{fr.leslimbes.world.dimension.LimbesSites.CLAIRVAL, fr.leslimbes.world.dimension.LimbesRegions.Region.PRAIRIES},
				{fr.leslimbes.world.dimension.LimbesSites.RELAIS_ROYAL, fr.leslimbes.world.dimension.LimbesRegions.Region.PRAIRIES},
				{fr.leslimbes.world.dimension.LimbesSites.LAC_DU_CREPUSCULE, fr.leslimbes.world.dimension.LimbesRegions.Region.PRAIRIES},
				{fr.leslimbes.world.dimension.LimbesSites.ARRIVEE_PRAIRIES_3, fr.leslimbes.world.dimension.LimbesRegions.Region.PRAIRIES},
				{fr.leslimbes.world.dimension.LimbesSites.PORT_NACRE, fr.leslimbes.world.dimension.LimbesRegions.Region.MER},
				{fr.leslimbes.world.dimension.LimbesSites.NAVIRE_PERDU, fr.leslimbes.world.dimension.LimbesRegions.Region.MER},
				{fr.leslimbes.world.dimension.LimbesSites.PHARE_DU_DESSOUS, fr.leslimbes.world.dimension.LimbesRegions.Region.MER},
				{fr.leslimbes.world.dimension.LimbesSites.SYLVECIME, fr.leslimbes.world.dimension.LimbesRegions.Region.JARDINS},
				{fr.leslimbes.world.dimension.LimbesSites.CLAIRIERE_DU_GEANT, fr.leslimbes.world.dimension.LimbesRegions.Region.JARDINS},
				{fr.leslimbes.world.dimension.LimbesSites.MERIDIANE, fr.leslimbes.world.dimension.LimbesRegions.Region.DESERT},
				{fr.leslimbes.world.dimension.LimbesSites.OBSERVATOIRE_DES_HEURES, fr.leslimbes.world.dimension.LimbesRegions.Region.DESERT},
				{fr.leslimbes.world.dimension.LimbesSites.VERREOLE, fr.leslimbes.world.dimension.LimbesRegions.Region.MONTAGNES},
				{fr.leslimbes.world.dimension.LimbesSites.PONT_QUI_CHANTE, fr.leslimbes.world.dimension.LimbesRegions.Region.MONTAGNES},
				{fr.leslimbes.world.dimension.LimbesSites.HAUTE_GARDE, fr.leslimbes.world.dimension.LimbesRegions.Region.COURONNE},
				{fr.leslimbes.world.dimension.LimbesSites.CAPITALE, fr.leslimbes.world.dimension.LimbesRegions.Region.COURONNE}};
		for (Object[] s : sites) {
			BlockPos p = (BlockPos) s[0];
			fr.leslimbes.world.dimension.LimbesRegions.Point pt = fr.leslimbes.world.dimension.LimbesRegions.evaluer(p.getX(), p.getZ());
			assertEquals(s[1], pt.region(), "région de " + p.toShortString());
			assertEquals(1.0, pt.poids(), 1.0E-9, "site trop près d'une frontière : " + p.toShortString());
		}
		// Prairies : même relief qu'avant les régions (64,5 + 13,44 × collines), pente 1/32.
		for (double c : new double[] {-1.0, -0.3, 0.0, 0.5, 1.0}) {
			assertEquals(64.5 + 13.44 * c, fr.leslimbes.world.dimension.LimbesRegions.hauteurMelangee(0, 140, c, 0.0, 0.0, 0.0), 1.0E-9);
		}
		assertEquals(1.0 / 32.0, fr.leslimbes.world.dimension.LimbesRegions.penteMelangee(0, 140), 1.0E-12);
		// Mer : au-delà de la zone remplie, le rebord dépasse toujours le niveau de l'eau.
		fr.leslimbes.world.dimension.LimbesRegions.Region mer = fr.leslimbes.world.dimension.LimbesRegions.Region.MER;
		for (double n = fr.leslimbes.world.dimension.LimbesRegions.MER_REMPLISSAGE_MAX; n <= 1.05; n += 0.01) {
			for (double c : new double[] {-1.0, 0.0, 1.0}) {
				for (double i : new double[] {-1.0, 0.0, 1.0}) {
					double h = fr.leslimbes.world.dimension.LimbesRegions.hauteur(mer, n, c, i, 0.0, 0.0);
					assertTrue(h >= fr.leslimbes.world.dimension.LimbesRegions.NIVEAU_MER + 1, "rebord de la Mer sous le niveau de l'eau à n=" + n + " (h=" + h + ")");
				}
			}
		}
		assertTrue(fr.leslimbes.world.dimension.LimbesRegions.hauteur(mer, 0.3, 0.0, -1.0, 0.0, 0.0) < 60);
		assertTrue(fr.leslimbes.world.dimension.LimbesRegions.dansLaMer(950, -750));
		assertFalse(fr.leslimbes.world.dimension.LimbesRegions.dansLaMer(1440, -750));
		for (fr.leslimbes.world.dimension.LimbesRegions.Region r : fr.leslimbes.world.dimension.LimbesRegions.Region.values()) {
			for (double n = 0; n <= 1.2; n += 0.1) {
				for (double t = -1; t <= 1; t += 0.5) {
					int s = fr.leslimbes.world.dimension.LimbesRegions.sousBiome(r, n, t, -t);
					assertTrue(s >= 0 && s <= 2);
				}
			}
		}
	}

	@BeforeAll
	static void bootstrap() {
		net.minecraft.SharedConstants.createGameVersion();
		net.minecraft.Bootstrap.initialize();
	}

	@Test
	void comparaisonsDEtapes() {
		assertTrue(Conditions.comparer(3, ">=2"));
		assertTrue(Conditions.comparer(2, "=2"));
		assertTrue(Conditions.comparer(2, "2"));
		assertFalse(Conditions.comparer(1, ">=2"));
		assertTrue(Conditions.comparer(1, "<2"));
		assertTrue(Conditions.comparer(5, "<=5"));
		assertFalse(Conditions.comparer(5, ">5"));
	}

	@Test
	void directionsDeLaBoussole() {
		assertEquals("nord", BoussoleNacreeItem.direction(0, -10));
		assertEquals("sud", BoussoleNacreeItem.direction(0, 10));
		assertEquals("est", BoussoleNacreeItem.direction(10, 0));
		assertEquals("ouest", BoussoleNacreeItem.direction(-10, 0));
		assertEquals("nord_est", BoussoleNacreeItem.direction(10, -10));
		assertEquals("sud_ouest", BoussoleNacreeItem.direction(-10, 10));
	}

	@Test
	void aplanissementDuTerrain() {
		FlattenDensityFunction f = new FlattenDensityFunction(DensityFunctionTypes.constant(0.5),
				List.of(new FlattenDensityFunction.Site(0, 0, 66, 20, 10)), 0.03125);
		// Au centre du site : bloc de surface à 65 (solide), air dès 66, sans ambiguïté de signe.
		assertTrue(f.appliquer(0.5, 0, 65, 0) > 0.01);
		assertTrue(f.appliquer(0.5, 0, 66, 0) < -0.01);
		assertTrue(f.appliquer(0.5, 0, 60, 0) > 0);
		assertTrue(f.appliquer(0.5, 0, 70, 0) < 0);
		// Hors de la zone de transition : densité d'origine inchangée.
		assertEquals(0.5, f.appliquer(0.5, 100, 70, 0), 1.0E-9);
		// Dans la transition : valeur intermédiaire.
		double milieu = f.appliquer(0.5, 25, 70, 0);
		assertTrue(milieu < 0.5 && milieu > -0.125, "transition douce attendue, obtenu " + milieu);
		assertEquals(1.0, new FlattenDensityFunction.Site(0, 0, 66, 20, 10).masque(10, 10), 1.0E-9);
		assertEquals(0.0, new FlattenDensityFunction.Site(0, 0, 66, 20, 10).masque(40, 0), 1.0E-9);
	}

	@Test
	void progressionDeQuete() {
		QuestProgress p = new QuestProgress("test");
		assertEquals(0, p.etape);
		assertEquals(1, p.incrementer("veilleurs", 1));
		assertEquals(2, p.incrementer("veilleurs", 1));
		p.faits.add("parler");
		assertTrue(p.estFait("parler"));
		p.nouvelleEtape(1);
		assertEquals(1, p.etape);
		assertFalse(p.estFait("parler"));
		assertEquals(0, p.compteur("veilleurs"));
		p.lieux.put("vestige", new BlockPos(10, 64, -20));
		QuestProgress copie = p.copier();
		assertEquals(new BlockPos(10, 64, -20), copie.lieux.get("vestige"));
	}

	@Test
	void donneesJoueurSerialisablesEtPouvoirs() {
		PlayerLimbesData d = new PlayerLimbesData();
		assertTrue(d.debloquerPouvoir("epee_du_souverain"));
		assertFalse(d.debloquerPouvoir("epee_du_souverain"));
		assertEquals("epee_du_souverain", d.equipes.get(0));
		assertTrue(d.debloquerPouvoir("pas_court"));
		assertEquals("pas_court", d.equipes.get(1));
		assertTrue(d.equiper(2, "epee_du_souverain"));
		assertEquals("", d.equipes.get(0), "un pouvoir n'occupe qu'un emplacement");
		assertEquals("epee_du_souverain", d.equipes.get(2));
		assertFalse(d.equiper(0, "inconnu"), "impossible d'équiper un pouvoir non débloqué");
		d.poserDrapeau("arrivee");
		d.quetes.put("le_passage", new QuestProgress("le_passage"));
		d.queteSuivie = "le_passage";
		d.majeste = 12.5F;
		NbtElement nbt = PlayerLimbesData.CODEC.encodeStart(NbtOps.INSTANCE, d).getOrThrow();
		assertTrue(nbt instanceof NbtCompound);
		PlayerLimbesData relu = PlayerLimbesData.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow();
		assertEquals(12.5F, relu.majeste, 1.0E-6);
		assertTrue(relu.aDrapeau("arrivee"));
		assertTrue(relu.possedePouvoir("pas_court"));
		assertEquals("epee_du_souverain", relu.equipes.get(2));
		assertTrue(relu.queteActive("le_passage"));
		assertEquals("le_passage", relu.queteSuivie);
		// Majesté
		assertTrue(relu.payer(10));
		assertFalse(relu.payer(100));
		relu.regenerer(1000);
		assertEquals(relu.majesteMax, relu.majeste, 1.0E-6);
	}
}
