package fr.leslimbes.client.fx;

import fr.leslimbes.client.OptionsLimbes;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;

/**
 * Secousses de caméra et flashes d'écran, courts et désactivables dans les options. La secousse est
 * un décalage d'angle appliqué à la caméra après son calcul (mixin), jamais à l'orientation du joueur.
 */
public final class Secousses {
	private static final long DUREE_SECOUSSE_MS = 420;
	private static final long DUREE_FLASH_MS = 320;
	private static long finSecousse;
	private static float force;
	private static long finFlash;
	private static float intensiteFlash;

	public static void secouer(float f) {
		if (!OptionsLimbes.get().secousses) {
			return;
		}
		long now = System.currentTimeMillis();
		force = finSecousse > now ? Math.max(force, f) : f;
		finSecousse = now + DUREE_SECOUSSE_MS;
	}

	public static void flash(float intensite) {
		if (!OptionsLimbes.get().flashes) {
			return;
		}
		long now = System.currentTimeMillis();
		intensiteFlash = finFlash > now ? Math.max(intensiteFlash, intensite) : intensite;
		finFlash = now + DUREE_FLASH_MS;
	}

	/** Décalage (lacet, tangage) en degrés à appliquer à la caméra, ou null si rien ne bouge. */
	@Nullable
	public static float[] decalage() {
		long reste = finSecousse - System.currentTimeMillis();
		if (reste <= 0) {
			return null;
		}
		float k = force * reste / (float) DUREE_SECOUSSE_MS;
		double t = System.nanoTime() / 1.0E7;
		return new float[] {(float) Math.sin(t * 1.9) * k * 1.6F, (float) Math.cos(t * 2.3) * k * 1.1F};
	}

	/** Voile blanc qui s'estompe (flash), dessiné par l'interface. */
	public static void dessinerFlash(DrawContext context) {
		long reste = finFlash - System.currentTimeMillis();
		if (reste <= 0) {
			return;
		}
		int alpha = (int) (255 * intensiteFlash * reste / (float) DUREE_FLASH_MS);
		if (alpha <= 2) {
			return;
		}
		context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), (alpha << 24) | 0xF4F1EA);
	}

	private Secousses() {
	}
}
