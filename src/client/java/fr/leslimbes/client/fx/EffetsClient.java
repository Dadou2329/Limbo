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
		switch (type) {
			case "epee_invocation" -> {
				for (int i = 0; i < 24; i++) {
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
					for (int i = 0; i < 8; i++) {
						world.addParticle(LimbesParticles.ECLAT_DE_NACRE, pos.x, pos.y - 0.5, pos.z, (random.nextDouble() - 0.5) * 0.1, 0.05, (random.nextDouble() - 0.5) * 0.1);
					}
				}
			}
			case "decret_reculez" -> {
				for (int ring = 1; ring <= 3; ring++) {
					int n = 16 * ring;
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
				for (int i = 0; i < 20; i++) {
					double a = random.nextDouble() * Math.PI * 2;
					double r = random.nextDouble() * 2.5;
					world.addParticle(ParticleTypes.CLOUD, pos.x + Math.cos(a) * r, pos.y + 0.1, pos.z + Math.sin(a) * r, Math.cos(a) * 0.08, 0.02, Math.sin(a) * 0.08);
				}
				world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.3, pos.z, 0, 0, 0);
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
