package fr.leslimbes.client.render.dimension;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.leslimbes.LesLimbes;
import fr.leslimbes.world.dimension.LimbesDimension;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Random;

/**
 * Ciel des Limbes : voûte nacrée (dégradé or pâle / bleu profond), constellations la nuit et
 * silhouette céleste du palais de la Couronne Immobile, toujours au même cap (nord-ouest).
 *
 * <p>Le palais n'est pas une structure chargée : c'est une représentation lointaine cohérente avec
 * sa position réelle, dessinée dans le ciel pour qu'on le perçoive dès les premières heures.</p>
 */
public final class LimbesSky {
	/** Cap (radians) du palais depuis les Prairies : nord-ouest. */
	public static final double CAP_PALAIS = Math.toRadians(-45.0);
	private static final int SEGMENTS = 32;
	private static final float[][] ETOILES = etoiles(220, 4242L);

	public static void init() {
		DimensionRenderingRegistry.registerDimensionEffects(LesLimbes.id("limbes"), new Effets());
		DimensionRenderingRegistry.registerSkyRenderer(LimbesDimension.MONDE, LimbesSky::rendre);
	}

	/** Effets de dimension : nuages bas (sous les terres), brouillard nacré, pas de brouillard épais. */
	private static final class Effets extends DimensionEffects {
		Effets() {
			super(22.0F, true, SkyType.NORMAL, false, false);
		}

		@Override
		public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
			float jour = MathHelper.clamp(sunHeight * 0.95F + 0.05F, 0.0F, 1.0F);
			Vec3d nacre = new Vec3d(0.88, 0.89, 0.95);
			Vec3d nuit = new Vec3d(0.10, 0.13, 0.26);
			Vec3d cible = nuit.lerp(nacre, jour);
			return color.lerp(cible, 0.55);
		}

		@Override
		public boolean useThickFog(int camX, int camY) {
			return false;
		}

		@Override
		public float[] getFogColorOverride(float skyAngle, float tickDelta) {
			return null;
		}
	}

	private static void rendre(WorldRenderContext context) {
		ClientWorld world = context.world();
		MatrixStack matrices = context.matrixStack();
		if (world == null || matrices == null) {
			return;
		}
		float tickDelta = context.tickCounter().getTickDelta(false);
		float angle = world.getSkyAngle(tickDelta);
		float jour = MathHelper.clamp(MathHelper.cos(angle * ((float) Math.PI * 2)) * 2.0F + 0.5F, 0.0F, 1.0F);
		Matrix4f m = matrices.peek().getPositionMatrix();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.setShaderFogStart(1.0E5F);
		RenderSystem.setShaderFogEnd(2.0E5F);
		dessinerVoute(m, jour);
		dessinerEtoiles(m, world.getStarBrightness(tickDelta), world.getTime() + tickDelta);
		dessinerPalais(m, jour);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
	}

	private static void dessinerVoute(Matrix4f m, float jour) {
		float[] zenithJour = {0.56F, 0.68F, 0.92F};
		float[] bandeJour = {0.86F, 0.82F, 0.90F};
		float[] horizonJour = {0.96F, 0.90F, 0.80F};
		float[] basJour = {0.90F, 0.92F, 0.97F};
		float[] zenithNuit = {0.05F, 0.07F, 0.18F};
		float[] bandeNuit = {0.10F, 0.13F, 0.30F};
		float[] horizonNuit = {0.22F, 0.20F, 0.38F};
		float[] basNuit = {0.12F, 0.14F, 0.24F};
		float[] zenith = mix(zenithNuit, zenithJour, jour);
		float[] bande = mix(bandeNuit, bandeJour, jour);
		float[] horizon = mix(horizonNuit, horizonJour, jour);
		float[] bas = mix(basNuit, basJour, jour);
		float r = 100.0F;
		BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		anneau(b, m, r, 90.0F, 35.0F, zenith, bande);
		anneau(b, m, r, 35.0F, 8.0F, bande, horizon);
		anneau(b, m, r, 8.0F, -4.0F, horizon, horizon);
		anneau(b, m, r, -4.0F, -90.0F, horizon, bas);
		BufferRenderer.drawWithGlobalProgram(b.end());
	}

	/** Bande de la voûte entre deux élévations (degrés), avec couleurs haute et basse. */
	private static void anneau(BufferBuilder b, Matrix4f m, float r, float elevHaut, float elevBas, float[] cHaut, float[] cBas) {
		float eh = (float) Math.toRadians(elevHaut);
		float eb = (float) Math.toRadians(elevBas);
		float yh = MathHelper.sin(eh) * r, rh = MathHelper.cos(eh) * r;
		float yb = MathHelper.sin(eb) * r, rb = MathHelper.cos(eb) * r;
		for (int i = 0; i < SEGMENTS; i++) {
			float a0 = (float) (i * Math.PI * 2 / SEGMENTS);
			float a1 = (float) ((i + 1) * Math.PI * 2 / SEGMENTS);
			b.vertex(m, MathHelper.cos(a0) * rb, yb, MathHelper.sin(a0) * rb).color(cBas[0], cBas[1], cBas[2], 1.0F);
			b.vertex(m, MathHelper.cos(a1) * rb, yb, MathHelper.sin(a1) * rb).color(cBas[0], cBas[1], cBas[2], 1.0F);
			b.vertex(m, MathHelper.cos(a1) * rh, yh, MathHelper.sin(a1) * rh).color(cHaut[0], cHaut[1], cHaut[2], 1.0F);
			b.vertex(m, MathHelper.cos(a0) * rh, yh, MathHelper.sin(a0) * rh).color(cHaut[0], cHaut[1], cHaut[2], 1.0F);
		}
	}

	private static void dessinerEtoiles(Matrix4f m, float luminosite, float temps) {
		if (luminosite <= 0.01F) {
			return;
		}
		BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		for (float[] e : ETOILES) {
			float scintille = 0.7F + 0.3F * MathHelper.sin(temps * 0.05F + e[3] * 10.0F);
			float a = luminosite * scintille * e[4];
			float taille = e[3];
			float x = e[0], y = e[1], z = e[2];
			// Quad orienté face au centre : base orthonormée approximative.
			float ux = -z, uz = x, len = (float) Math.sqrt(ux * ux + uz * uz);
			if (len < 1.0E-4F) {
				continue;
			}
			ux /= len;
			uz /= len;
			float vx = y * uz, vy = z * ux - x * uz, vz = -y * ux;
			float[] c = e[5] > 0.5F ? new float[]{0.97F, 0.90F, 0.70F} : new float[]{0.90F, 0.94F, 1.0F};
			b.vertex(m, x - ux * taille - vx * taille, y - vy * taille, z - uz * taille - vz * taille).color(c[0], c[1], c[2], a);
			b.vertex(m, x + ux * taille - vx * taille, y - vy * taille, z + uz * taille - vz * taille).color(c[0], c[1], c[2], a);
			b.vertex(m, x + ux * taille + vx * taille, y + vy * taille, z + uz * taille + vz * taille).color(c[0], c[1], c[2], a);
			b.vertex(m, x - ux * taille + vx * taille, y + vy * taille, z - uz * taille + vz * taille).color(c[0], c[1], c[2], a);
		}
		BufferRenderer.drawWithGlobalProgram(b.end());
	}

	/** Silhouette du palais au cap fixe, légèrement au-dessus de l'horizon : tours et dôme central. */
	private static void dessinerPalais(Matrix4f m, float jour) {
		float r = 98.0F;
		float dx = (float) Math.sin(CAP_PALAIS), dz = (float) -Math.cos(CAP_PALAIS); // cap mesuré depuis le nord vers l'est
		float lx = -dz, lz = dx; // vecteur latéral
		float[] teinte = mix(new float[]{0.16F, 0.18F, 0.36F}, new float[]{0.40F, 0.44F, 0.62F}, jour);
		float alpha = 0.55F + 0.25F * jour;
		BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		float base = (float) Math.toRadians(1.5);
		float yBase = MathHelper.sin(base) * r;
		// [décalage latéral, demi-largeur, hauteur] en « degrés » de ciel
		float[][] tours = {{0.0F, 1.2F, 7.5F}, {-2.6F, 0.6F, 4.5F}, {2.6F, 0.6F, 4.5F}, {-4.6F, 0.45F, 3.2F}, {4.6F, 0.45F, 3.2F}, {-1.5F, 3.6F, 2.4F}};
		for (float[] t : tours) {
			float cx = t[0], hw = t[1], h = t[2];
			float x0 = (float) Math.toRadians(cx - hw) * r, x1 = (float) Math.toRadians(cx + hw) * r;
			float yTop = MathHelper.sin(base + (float) Math.toRadians(h)) * r;
			quadHorizon(b, m, dx, dz, lx, lz, r, x0, x1, yBase, yTop, teinte, alpha);
			// flèche / dôme
			float xm = (x0 + x1) / 2;
			float yPic = MathHelper.sin(base + (float) Math.toRadians(h + hw * 1.6F)) * r;
			quadHorizon(b, m, dx, dz, lx, lz, r, xm - 0.08F, xm + 0.08F, yTop, yPic, teinte, alpha);
		}
		// Halo doré derrière le palais
		float[] halo = {0.98F, 0.88F, 0.55F};
		float hx0 = (float) Math.toRadians(-6.5) * r, hx1 = (float) Math.toRadians(6.5) * r;
		float yHalo = MathHelper.sin(base + (float) Math.toRadians(10.5)) * r;
		quadHorizon(b, m, dx, dz, lx, lz, r + 1.0F, hx0, hx1, yBase - 2.0F, yHalo, halo, 0.10F + 0.12F * (1.0F - jour));
		BufferRenderer.drawWithGlobalProgram(b.end());
	}

	private static void quadHorizon(BufferBuilder b, Matrix4f m, float dx, float dz, float lx, float lz, float r,
									float x0, float x1, float y0, float y1, float[] c, float a) {
		float ax = dx * r + lx * x0, az = dz * r + lz * x0;
		float bx = dx * r + lx * x1, bz = dz * r + lz * x1;
		b.vertex(m, ax, y0, az).color(c[0], c[1], c[2], a);
		b.vertex(m, bx, y0, bz).color(c[0], c[1], c[2], a);
		b.vertex(m, bx, y1, bz).color(c[0], c[1], c[2], a);
		b.vertex(m, ax, y1, az).color(c[0], c[1], c[2], a);
	}

	private static float[] mix(float[] a, float[] b, float t) {
		return new float[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
	}

	/** Étoiles réparties sur la voûte (x, y, z, taille, intensité, teinte). */
	private static float[][] etoiles(int n, long seed) {
		Random random = new Random(seed);
		float[][] out = new float[n][6];
		for (int i = 0; i < n; i++) {
			double u = random.nextDouble() * Math.PI * 2;
			double v = Math.asin(0.08 + random.nextDouble() * 0.9);
			float r = 99.0F;
			out[i][0] = (float) (Math.cos(u) * Math.cos(v) * r);
			out[i][1] = (float) (Math.sin(v) * r);
			out[i][2] = (float) (Math.sin(u) * Math.cos(v) * r);
			out[i][3] = 0.15F + random.nextFloat() * 0.25F;
			out[i][4] = 0.5F + random.nextFloat() * 0.5F;
			out[i][5] = random.nextFloat();
		}
		return out;
	}

	private LimbesSky() {
	}
}
