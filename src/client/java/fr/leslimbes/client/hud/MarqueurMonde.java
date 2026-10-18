package fr.leslimbes.client.hud;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.client.ClientLimbesData;
import fr.leslimbes.quest.QuestDefinition;
import fr.leslimbes.quest.QuestLoader;
import fr.leslimbes.quest.QuestProgress;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Optional;

/** Marqueur dans le monde (losange doré et distance) au-dessus du lieu de la quête suivie. */
public final class MarqueurMonde {
	private static final Identifier MARQUEUR = LesLimbes.id("textures/gui/marqueur.png");
	private static final double DISTANCE_MAX = 320.0;

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(MarqueurMonde::rendre);
	}

	private static void rendre(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.world == null || context.consumers() == null) {
			return;
		}
		fr.leslimbes.client.OptionsLimbes options = fr.leslimbes.client.OptionsLimbes.get();
		if (!options.marqueurs || options.discret) {
			return;
		}
		Optional<QuestProgress> suivie = ClientLimbesData.get().queteSuivie();
		if (suivie.isEmpty()) {
			return;
		}
		QuestDefinition def = QuestLoader.get(suivie.get().questId);
		if (def == null) {
			return;
		}
		Optional<QuestDefinition.Lieu> lieu = def.lieuPour(suivie.get());
		if (lieu.isEmpty() || !client.world.getRegistryKey().getValue().equals(lieu.get().dimension())) {
			return;
		}
		BlockPos pos = lieu.get().pos();
		Vec3d cam = context.camera().getPos();
		Vec3d cible = new Vec3d(pos.getX() + 0.5, pos.getY() + 2.2, pos.getZ() + 0.5);
		double distance = cam.distanceTo(cible);
		if (distance < 2.5 || distance > DISTANCE_MAX) {
			return;
		}
		// Au-delà de 48 blocs, le marqueur est rapproché le long de la ligne de visée pour rester lisible.
		Vec3d rendu = cible;
		double affichee = Math.min(distance, 48.0);
		if (distance > 48.0) {
			rendu = cam.add(cible.subtract(cam).normalize().multiply(affichee));
		}
		float echelle = (float) (0.025 * Math.max(1.0, affichee / 8.0));
		MatrixStack matrices = context.matrixStack();
		matrices.push();
		matrices.translate(rendu.x - cam.x, rendu.y - cam.y, rendu.z - cam.z);
		matrices.multiply(context.camera().getRotation());
		matrices.scale(-echelle, -echelle, echelle);
		Matrix4f m = matrices.peek().getPositionMatrix();
		float flottement = (float) Math.sin((client.world.getTime() + context.tickCounter().getTickDelta(true)) * 0.15) * 2.0F;
		VertexConsumer vc = context.consumers().getBuffer(RenderLayer.getTextSeeThrough(MARQUEUR));
		float s = 12.0F;
		float y0 = -s - 14 + flottement;
		int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		vc.vertex(m, -s, y0 + 2 * s, 0).color(255, 255, 255, 230).texture(0, 1).light(light);
		vc.vertex(m, s, y0 + 2 * s, 0).color(255, 255, 255, 230).texture(1, 1).light(light);
		vc.vertex(m, s, y0, 0).color(255, 255, 255, 230).texture(1, 0).light(light);
		vc.vertex(m, -s, y0, 0).color(255, 255, 255, 230).texture(0, 0).light(light);
		TextRenderer tr = client.textRenderer;
		String texte = lieu.get().nom().getString() + " · " + (int) distance + " m";
		float tx = -tr.getWidth(texte) / 2.0F;
		tr.draw(texte, tx, y0 + 2 * s + 2, 0xFFE3C97E, true, m, context.consumers(), TextRenderer.TextLayerType.SEE_THROUGH, 0x40000000, light);
		matrices.pop();
	}

	private MarqueurMonde() {
	}
}
