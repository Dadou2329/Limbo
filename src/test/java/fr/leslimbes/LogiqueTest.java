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
		// Au centre du site : surface exactement à 66 (densité nulle), solide dessous, air dessus.
		assertEquals(0.0, f.appliquer(0.5, 0, 66, 0), 1.0E-9);
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
