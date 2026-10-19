package fr.leslimbes.client.fx;

import fr.leslimbes.registry.LimbesParticles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/**
 * Déclinaison client des effets envoyés par le serveur : formes architecturales royales (portes,
 * arcs, cercles) dessinées avec les particules du mod. Le serveur n'envoie qu'un type et une position.
 */
public final class EffetsClient {
	public static void jouer(MinecraftClient client, String type, Vec3d pos, Vec3d dir, int donnee) {
		ClientWorld world = client.world;
		if (world == null) {
			return;
		}
		Random random = world.getRandom();
		fr.leslimbes.client.OptionsLimbes options = fr.leslimbes.client.OptionsLimbes.get();
		double distanceJoueur = client.player == null ? 64.0 : client.player.getPos().distanceTo(pos);
		switch (type) {
			case "chantier" -> {
				// Arêtes de la boîte du chantier (dir = taille) parcourues d'étoiles, puis poussière de nacre au centre.
				double sx = dir.x / 2.0, sy = dir.y / 2.0, sz = dir.z / 2.0;
				int pas = options.quantite(10);
				for (int i = 0; i <= pas; i++) {
					double t = -1.0 + 2.0 * i / pas;
					for (int k = 0; k < 4; k++) {
						double ax = (k & 1) == 0 ? -sx : sx, az = (k & 2) == 0 ? -sz : sz;
						world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + ax, pos.y + t * sy, pos.z + az, 0, 0.02, 0);
						world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + t * sx, pos.y + ((k & 1) == 0 ? -sy : sy), pos.z + az, 0, 0.02, 0);
						world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + ax, pos.y + ((k & 1) == 0 ? -sy : sy), pos.z + t * sz, 0, 0.02, 0);
					}
				}
				for (int i = 0; i < options.quantite(40); i++) {
					world.addParticle(LimbesParticles.ECLAT_DE_NACRE, pos.x + (random.nextDouble() - 0.5) * dir.x, pos.y + (random.nextDouble() - 0.5) * dir.y,
							pos.z + (random.nextDouble() - 0.5) * dir.z, 0, 0.04, 0);
				}
				if (distanceJoueur < 40) {
					Secousses.secouer(0.35F);
				}
			}
			case "epee_invocation" -> {
				for (int i = 0; i < options.quantite(24); i++) {
					double a = i / 24.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 0.8, pos.y - 0.6 + i * 0.07, pos.z + Math.sin(a) * 0.8, 0, 0.02, 0);
				}
				for (int i = 0; i < 10; i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x, pos.y + random.nextDouble() * 1.2 - 0.6, pos.z, (random.nextDouble() - 0.5) * 0.05, 0.02, (random.nextDouble() - 0.5) * 0.05);
				}
			}
			case "epee_rappel" -> {
				for (int i = 0; i < 12; i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x + (random.nextDouble() - 0.5) * 0.6, pos.y + (random.nextDouble() - 0.5) * 0.8, pos.z + (random.nextDouble() - 0.5) * 0.6, 0, 0.03, 0);
				}
			}
			case "epee_arc" -> {
				// Arc lumineux devant le joueur, dans la direction du regard.
				Vec3d d = new Vec3d(dir.x, 0, dir.z).normalize();
				double base = Math.atan2(d.z, d.x);
				for (int i = -14; i <= 14; i++) {
					double a = base + i * (Math.PI / 28);
					double r = 2.2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * r, pos.y + Math.abs(i) * 0.02, pos.z + Math.sin(a) * r, Math.cos(a) * 0.05, 0.01, Math.sin(a) * 0.05);
				}
				world.addParticle(ParticleTypes.SWEEP_ATTACK, pos.x + d.x * 1.5, pos.y, pos.z + d.z * 1.5, 0, 0, 0);
			}
			case "porte_depart", "porte_arrivee" -> {
				// Cadre de porte vertical (2 x 3) perpendiculaire à la direction.
				Vec3d d = new Vec3d(dir.x, 0, dir.z);
				if (d.lengthSquared() < 1.0E-4) {
					d = new Vec3d(0, 0, 1);
				}
				d = d.normalize();
				Vec3d lat = new Vec3d(-d.z, 0, d.x);
				for (int i = 0; i <= 10; i++) {
					double t = i / 10.0;
					double y = pos.y - 0.9 + t * 2.1;
					world.addParticle(LimbesParticles.LUEUR, pos.x + lat.x * 0.6, y, pos.z + lat.z * 0.6, 0, 0, 0);
					world.addParticle(LimbesParticles.LUEUR, pos.x - lat.x * 0.6, y, pos.z - lat.z * 0.6, 0, 0, 0);
				}
				for (int i = 0; i <= 6; i++) {
					double t = -0.6 + i * 0.2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + lat.x * t, pos.y + 1.25, pos.z + lat.z * t, 0, 0.01, 0);
				}
				if (type.equals("porte_arrivee")) {
					if (distanceJoueur < 3) {
						Secousses.flash(0.22F);
					}
					for (int i = 0; i < options.quantite(8); i++) {
						world.addParticle(LimbesParticles.ECLAT_DE_NACRE, pos.x, pos.y - 0.5, pos.z, (random.nextDouble() - 0.5) * 0.1, 0.05, (random.nextDouble() - 0.5) * 0.1);
					}
				}
			}
			case "decret_reculez" -> {
				if (distanceJoueur < 12) {
					Secousses.secouer(0.5F);
				}
				for (int ring = 1; ring <= 3; ring++) {
					int n = options.quantite(16 * ring);
					for (int i = 0; i < n; i++) {
						double a = i / (double) n * Math.PI * 2;
						double r = ring * 1.8;
						world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 0.5, pos.y + 0.1, pos.z + Math.sin(a) * 0.5, Math.cos(a) * 0.12 * ring, 0.01, Math.sin(a) * 0.12 * ring);
						if (ring == 3) {
							world.addParticle(LimbesParticles.LUEUR, pos.x + Math.cos(a) * r, pos.y + 0.2, pos.z + Math.sin(a) * r, 0, 0.02, 0);
						}
					}
				}
			}
			case "veilleur_prepare" -> {
				for (int i = 0; i < 6; i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x + (random.nextDouble() - 0.5), pos.y + random.nextDouble() * 0.5, pos.z + (random.nextDouble() - 0.5), 0, 0.03, 0);
				}
			}
			case "veilleur_frappe" -> {
				if (distanceJoueur < 8) {
					Secousses.secouer(0.8F);
				}
				for (int i = 0; i < options.quantite(20); i++) {
					double a = random.nextDouble() * Math.PI * 2;
					double r = random.nextDouble() * 2.5;
					world.addParticle(ParticleTypes.CLOUD, pos.x + Math.cos(a) * r, pos.y + 0.1, pos.z + Math.sin(a) * r, Math.cos(a) * 0.08, 0.02, Math.sin(a) * 0.08);
				}
				world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.3, pos.z, 0, 0, 0);
			}
			case "arrivee_ciel" -> {
				// Colonne de lumière du sol vers le ciel, anneaux qui se « plissent » en altitude.
				if (distanceJoueur < 120) {
					Secousses.flash(0.18F);
				}
				for (int i = 0; i < options.quantite(90); i++) {
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + (random.nextDouble() - 0.5) * 1.5, pos.y + i * 0.9, pos.z + (random.nextDouble() - 0.5) * 1.5, 0, 0.04, 0);
				}
				for (int ring = 0; ring < 3; ring++) {
					double r = 4 + ring * 3;
					for (int i = 0; i < 24; i++) {
						double a = i / 24.0 * Math.PI * 2;
						world.addParticle(LimbesParticles.LUEUR, pos.x + Math.cos(a) * r, pos.y + 40 + ring * 8, pos.z + Math.sin(a) * r, -Math.cos(a) * 0.03, -0.02, -Math.sin(a) * 0.03);
					}
				}
				for (int i = 0; i < options.quantite(30); i++) {
					world.addParticle(LimbesParticles.ECLAT_DE_NACRE, pos.x + (random.nextDouble() - 0.5) * 6, pos.y + 1 + random.nextDouble() * 3, pos.z + (random.nextDouble() - 0.5) * 6, 0, 0.05, 0);
				}
			}
			case "pacte", "pacte_appel" -> {
				for (int i = 0; i < 32; i++) {
					double a = i / 32.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 1.4, pos.y + 0.2, pos.z + Math.sin(a) * 1.4, 0, 0.08, 0);
				}
				for (int i = 0; i < 12; i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x + (random.nextDouble() - 0.5) * 2, pos.y + random.nextDouble() * 1.5, pos.z + (random.nextDouble() - 0.5) * 2, 0, 0.03, 0);
				}
			}
			case "parade" -> {
				// Demi-cercle de lumière devant le joueur, levé comme une garde.
				Vec3d d = new Vec3d(dir.x, 0, dir.z).normalize();
				double base = Math.atan2(d.z, d.x);
				for (int i = -8; i <= 8; i++) {
					double a = base + i * (Math.PI / 16);
					world.addParticle(LimbesParticles.LUEUR, pos.x + Math.cos(a) * 1.1, pos.y - 0.4 + Math.abs(i) * 0.06, pos.z + Math.sin(a) * 1.1, 0, 0.01, 0);
				}
			}
			case "parade_reussie" -> {
				if (distanceJoueur < 6) {
					Secousses.flash(0.2F);
				}
				for (int i = 0; i < options.quantite(30); i++) {
					double a = random.nextDouble() * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x, pos.y, pos.z, Math.cos(a) * 0.25, random.nextDouble() * 0.2, Math.sin(a) * 0.25);
				}
				world.addParticle(ParticleTypes.SWEEP_ATTACK, pos.x + dir.x, pos.y, pos.z + dir.z, 0, 0, 0);
			}
			case "lance_invocation", "arc_invocation", "bouclier_invocation", "garde_invocation" -> {
				for (int i = 0; i < options.quantite(20); i++) {
					double a = i / 20.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 0.7, pos.y - 0.8 + i * 0.09, pos.z + Math.sin(a) * 0.7, 0, 0.03, 0);
				}
				for (int i = 0; i < 8; i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x, pos.y + random.nextDouble() * 1.6 - 0.8, pos.z, 0, 0.02, 0);
				}
			}
			case "garde_dissipation", "avenement_fin" -> {
				for (int i = 0; i < options.quantite(24); i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x + (random.nextDouble() - 0.5) * 0.8, pos.y + (random.nextDouble() - 0.5) * 1.8, pos.z + (random.nextDouble() - 0.5) * 0.8,
							0, 0.06, 0);
				}
			}
			case "charge" -> {
				// Traînée en cône derrière le joueur pendant la charge.
				Vec3d d = new Vec3d(dir.x, 0, dir.z).normalize();
				for (int i = 0; i < options.quantite(16); i++) {
					double r = random.nextDouble() * 0.6;
					double a = random.nextDouble() * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x - d.x * i * 0.3 + Math.cos(a) * r, pos.y - 0.5 + random.nextDouble(), pos.z - d.z * i * 0.3 + Math.sin(a) * r,
							-d.x * 0.1, 0.01, -d.z * 0.1);
				}
				if (distanceJoueur < 4) {
					Secousses.secouer(0.3F);
				}
			}
			case "charge_impact", "coup_bonus" -> {
				for (int i = 0; i < 8; i++) {
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x, pos.y, pos.z, (random.nextDouble() - 0.5) * 0.3, random.nextDouble() * 0.2, (random.nextDouble() - 0.5) * 0.3);
				}
				if (type.equals("charge_impact")) {
					world.addParticle(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 0, 0, 0);
				}
			}
			case "arc_tir" -> {
				for (int i = 0; i < 6; i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x + dir.x * i * 0.3, pos.y + dir.y * i * 0.3, pos.z + dir.z * i * 0.3, dir.x * 0.1, dir.y * 0.1, dir.z * 0.1);
				}
			}
			case "marque" -> {
				// Couronne d'étoiles au-dessus de la cible.
				for (int i = 0; i < 12; i++) {
					double a = i / 12.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 0.5, pos.y, pos.z + Math.sin(a) * 0.5, 0, 0.02, 0);
				}
				world.addParticle(LimbesParticles.LUEUR, pos.x, pos.y + 0.3, pos.z, 0, 0.05, 0);
			}
			case "bouclier_rempart" -> {
				Vec3d d = new Vec3d(dir.x, 0, dir.z).normalize();
				Vec3d lat = new Vec3d(-d.z, 0, d.x);
				for (int i = -4; i <= 4; i++) {
					for (int j = 0; j <= 4; j++) {
						world.addParticle(LimbesParticles.LUEUR, pos.x + d.x * 0.8 + lat.x * i * 0.25, pos.y - 0.6 + j * 0.3, pos.z + d.z * 0.8 + lat.z * i * 0.25, d.x * 0.05, 0, d.z * 0.05);
					}
				}
				if (distanceJoueur < 4) {
					Secousses.secouer(0.25F);
				}
			}
			case "changement" -> {
				for (int i = 0; i < 16; i++) {
					double a = i / 16.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 0.6, pos.y, pos.z + Math.sin(a) * 0.6, -Math.cos(a) * 0.08, 0.03, -Math.sin(a) * 0.08);
				}
			}
			case "satellites" -> {
				// Trois lames en orbite (dir.x = angle courant) : trois petits traits d'étoiles.
				for (int k = 0; k < Math.max(1, donnee); k++) {
					double a = dir.x + k * Math.PI * 2 / Math.max(1, donnee);
					double r = 2.2;
					for (int j = 0; j < 3; j++) {
						world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * r, pos.y - 0.3 + j * 0.3, pos.z + Math.sin(a) * r, -Math.sin(a) * 0.05, 0, Math.cos(a) * 0.05);
					}
				}
			}
			case "porte_longue", "attaque_d_angle", "sauvetage", "liaison" -> {
				Vec3d d = new Vec3d(dir.x, 0, dir.z);
				if (d.lengthSquared() < 1.0E-4) {
					d = new Vec3d(0, 0, 1);
				}
				d = d.normalize();
				Vec3d lat = new Vec3d(-d.z, 0, d.x);
				for (int i = 0; i <= 12; i++) {
					double t = i / 12.0;
					double y = pos.y - 0.9 + t * 2.3;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + lat.x * 0.7, y, pos.z + lat.z * 0.7, 0, 0, 0);
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x - lat.x * 0.7, y, pos.z - lat.z * 0.7, 0, 0, 0);
				}
				for (int i = 0; i <= 8; i++) {
					double t = -0.7 + i * 0.175;
					world.addParticle(LimbesParticles.LUEUR, pos.x + lat.x * t, pos.y + 1.4, pos.z + lat.z * t, 0, 0.01, 0);
				}
				if (type.equals("liaison")) {
					for (int i = 0; i < options.quantite(20); i++) {
						double a = i / 20.0 * Math.PI * 2;
						world.addParticle(LimbesParticles.ECLAT_DE_NACRE, pos.x + Math.cos(a) * 1.2, pos.y, pos.z + Math.sin(a) * 1.2, 0, 0.04, 0);
					}
				}
				if (type.equals("attaque_d_angle") && distanceJoueur < 3) {
					Secousses.flash(0.2F);
				}
			}
			case "decret_deposez", "decret_restez" -> {
				if (distanceJoueur < 12) {
					Secousses.secouer(0.3F);
				}
				boolean restez = type.equals("decret_restez");
				for (int ring = 1; ring <= 2; ring++) {
					int n = options.quantite(20 * ring);
					for (int i = 0; i < n; i++) {
						double a = i / (double) n * Math.PI * 2;
						double r = restez ? ring * 3.5 : ring * 4.5;
						world.addParticle(restez ? LimbesParticles.ECLAT_DE_NACRE : LimbesParticles.LUEUR, pos.x + Math.cos(a) * r, pos.y + 0.1, pos.z + Math.sin(a) * r,
								restez ? 0 : -Math.cos(a) * 0.04, restez ? 0.03 : 0.1, restez ? 0 : -Math.sin(a) * 0.04);
					}
				}
			}
			case "decret_tenez_bon" -> {
				for (int i = 0; i < options.quantite(40); i++) {
					double a = i / 40.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.LUEUR, pos.x + Math.cos(a) * 3.0, pos.y, pos.z + Math.sin(a) * 3.0, 0, 0.12, 0);
				}
				for (int i = 0; i < 12; i++) {
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + (random.nextDouble() - 0.5) * 6, pos.y + 2.5, pos.z + (random.nextDouble() - 0.5) * 6, 0, -0.04, 0);
				}
			}
			case "decret_chemin" -> {
				Vec3d d = new Vec3d(dir.x, 0, dir.z).normalize();
				for (int i = 1; i <= donnee; i++) {
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + d.x * i, pos.y + 0.2, pos.z + d.z * i, 0, 0.05, 0);
					world.addParticle(LimbesParticles.LUEUR, pos.x + d.x * i + (random.nextDouble() - 0.5), pos.y + 0.4, pos.z + d.z * i + (random.nextDouble() - 0.5), 0, 0.02, 0);
				}
			}
			case "decret_elevez" -> {
				for (int i = 0; i < options.quantite(36); i++) {
					double a = i / 36.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 1.5, pos.y, pos.z + Math.sin(a) * 1.5, 0, 0.25, 0);
				}
			}
			case "ordre_de_garde" -> {
				for (int i = 0; i < 10; i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x, pos.y + 0.5 + i * 0.1, pos.z, 0, 0.05, 0);
				}
			}
			case "main_saisir", "main_passage" -> {
				// Doigts de lumière : cinq traits convergents.
				Vec3d d = dir.lengthSquared() < 1.0E-4 ? new Vec3d(0, 1, 0) : dir.normalize();
				for (int f = 0; f < 5; f++) {
					double a = f / 5.0 * Math.PI * 2;
					for (int j = 0; j < 5; j++) {
						double t = j / 5.0;
						world.addParticle(LimbesParticles.LUEUR, pos.x + Math.cos(a) * (0.9 - t * 0.6) + d.x * t, pos.y + 0.2 * j, pos.z + Math.sin(a) * (0.9 - t * 0.6) + d.z * t,
								d.x * 0.05, 0.02, d.z * 0.05);
					}
				}
			}
			case "main_couronnee" -> {
				if (distanceJoueur < 10) {
					Secousses.secouer(0.8F);
				}
				for (int i = 0; i < options.quantite(40); i++) {
					double a = random.nextDouble() * Math.PI * 2;
					double r = random.nextDouble() * 3.0;
					world.addParticle(ParticleTypes.CLOUD, pos.x + Math.cos(a) * r, pos.y + 0.1, pos.z + Math.sin(a) * r, Math.cos(a) * 0.08, 0.02, Math.sin(a) * 0.08);
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * r * 0.6, pos.y + 0.5 + random.nextDouble(), pos.z + Math.sin(a) * r * 0.6, 0, 0.05, 0);
				}
				world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.3, pos.z, 0, 0, 0);
			}
			case "pacte_combat" -> {
				for (int i = 0; i < 24; i++) {
					double a = i / 24.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 1.2, pos.y + Math.sin(a * 2) * 0.4, pos.z + Math.sin(a) * 1.2, 0, 0.05, 0);
				}
			}
			case "constellation" -> {
				// Constellation : étoiles reliées haut dans le ciel au-dessus du souverain.
				if (distanceJoueur < 24) {
					Secousses.flash(0.15F);
				}
				double[][] etoiles = new double[Math.max(3, donnee)][2];
				for (int i = 0; i < etoiles.length; i++) {
					double a = i / (double) etoiles.length * Math.PI * 2 + 0.4;
					double r = 3.0 + (i % 2) * 2.0;
					etoiles[i][0] = Math.cos(a) * r;
					etoiles[i][1] = Math.sin(a) * r;
				}
				for (int i = 0; i < etoiles.length; i++) {
					double[] e1 = etoiles[i];
					double[] e2 = etoiles[(i + 1) % etoiles.length];
					for (int j = 0; j <= 8; j++) {
						double t = j / 8.0;
						world.addParticle(LimbesParticles.LUEUR, pos.x + e1[0] + (e2[0] - e1[0]) * t, pos.y + 9.0, pos.z + e1[1] + (e2[1] - e1[1]) * t, 0, 0, 0);
					}
					for (int j = 0; j < 4; j++) {
						world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + e1[0], pos.y + 9.0 + j * 0.1, pos.z + e1[1], 0, 0.01, 0);
					}
				}
			}
			case "constellation_etoile" -> {
				if (distanceJoueur < 12) {
					Secousses.secouer(0.3F);
				}
				for (int i = 0; i < options.quantite(30); i++) {
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + (random.nextDouble() - 0.5) * 0.5, pos.y + 8.0 - i * 0.28, pos.z + (random.nextDouble() - 0.5) * 0.5, 0, -0.3, 0);
				}
				for (int i = 0; i < 16; i++) {
					double a = i / 16.0 * Math.PI * 2;
					world.addParticle(LimbesParticles.LUEUR, pos.x, pos.y, pos.z, Math.cos(a) * 0.2, 0.05, Math.sin(a) * 0.2);
				}
			}
			case "marche_du_palais" -> {
				for (int k = 0; k < 4; k++) {
					double a = k * Math.PI / 2;
					Vec3d lat = new Vec3d(Math.cos(a), 0, Math.sin(a));
					for (int i = 0; i <= 8; i++) {
						double y = pos.y - 0.9 + i * 0.28;
						world.addParticle(LimbesParticles.LUEUR, pos.x + lat.x * 1.6 + lat.z * 0.6, y, pos.z + lat.z * 1.6 - lat.x * 0.6, 0, 0, 0);
						world.addParticle(LimbesParticles.LUEUR, pos.x + lat.x * 1.6 - lat.z * 0.6, y, pos.z + lat.z * 1.6 + lat.x * 0.6, 0, 0, 0);
					}
				}
			}
			case "audience" -> {
				if (distanceJoueur < 20) {
					Secousses.secouer(0.6F);
					Secousses.flash(0.2F);
				}
				Vec3d d = new Vec3d(dir.x, 0, dir.z).normalize();
				double base = Math.atan2(d.z, d.x);
				for (int i = -12; i <= 12; i++) {
					double a = base + i * (Math.PI / 24);
					for (int j = 0; j < 3; j++) {
						world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * 4.5, pos.y + j * 0.4, pos.z + Math.sin(a) * 4.5, 0, 0.02, 0);
					}
				}
				for (int i = 0; i < options.quantite(30); i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x + (random.nextDouble() - 0.5) * 2, pos.y + random.nextDouble() * 3, pos.z + (random.nextDouble() - 0.5) * 2, 0, 0.03, 0);
				}
			}
			case "avenement" -> {
				if (distanceJoueur < 30) {
					Secousses.flash(0.35F);
					Secousses.secouer(0.5F);
				}
				for (int i = 0; i < options.quantite(80); i++) {
					double a = i / 80.0 * Math.PI * 6;
					double r = 0.4 + i * 0.03;
					world.addParticle(LimbesParticles.ETOILE_ROYALE, pos.x + Math.cos(a) * r, pos.y - 1.0 + i * 0.04, pos.z + Math.sin(a) * r, 0, 0.04, 0);
				}
				for (int i = 0; i < options.quantite(40); i++) {
					world.addParticle(LimbesParticles.ECLAT_DE_NACRE, pos.x + (random.nextDouble() - 0.5) * 3, pos.y + random.nextDouble() * 2, pos.z + (random.nextDouble() - 0.5) * 3, 0, 0.08, 0);
				}
			}
			default -> {
				for (int i = 0; i < 6; i++) {
					world.addParticle(LimbesParticles.LUEUR, pos.x, pos.y, pos.z, MathHelper.nextDouble(random, -0.05, 0.05), 0.02, MathHelper.nextDouble(random, -0.05, 0.05));
				}
			}
		}
	}

	private EffetsClient() {
	}
}
