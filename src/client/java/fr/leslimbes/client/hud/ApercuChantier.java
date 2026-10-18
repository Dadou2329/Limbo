package fr.leslimbes.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** Aperçu de l'emprise d'un projet : contour doré de la boîte et petits cadres rouges sur les blocs occupés, dix secondes. */
public final class ApercuChantier {
	private static final long DUREE_MS = 10_000;
	private static long fin;
	private static BlockPos min = BlockPos.ORIGIN;
	private static BlockPos max = BlockPos.ORIGIN;
	private static final List<BlockPos> CONFLITS = new ArrayList<>();
	private static String titre = "";

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ApercuChantier::rendre);
	}

	public static void recevoir(NbtCompound nbt) {
		int[] a = nbt.getIntArray("min");
		int[] b = nbt.getIntArray("max");
		if (a.length < 3 || b.length < 3) {
			return;
		}
		min = new BlockPos(a[0], a[1], a[2]);
		max = new BlockPos(b[0], b[1], b[2]);
		titre = nbt.getString("titre");
		CONFLITS.clear();
		NbtList l = nbt.getList("conflits", NbtElement.INT_ARRAY_TYPE);
		for (int i = 0; i < l.size(); i++) {
			int[] c = l.getIntArray(i);
			if (c.length >= 3) {
				CONFLITS.add(new BlockPos(c[0], c[1], c[2]));
			}
		}
		fin = System.currentTimeMillis() + DUREE_MS;
	}

	public static boolean actif() {
		return System.currentTimeMillis() < fin;
	}

	public static String titre() {
		return titre;
	}

	private static void rendre(WorldRenderContext context) {
		if (!actif() || context.consumers() == null) {
			return;
		}
		Vec3d cam = context.camera().getPos();
		MatrixStack matrices = context.matrixStack();
		matrices.push();
		matrices.translate(-cam.x, -cam.y, -cam.z);
		VertexConsumer vc = context.consumers().getBuffer(RenderLayer.getLines());
		float pulse = 0.75F + 0.25F * (float) Math.sin(System.currentTimeMillis() / 180.0);
		WorldRenderer.drawBox(matrices, vc, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), 1.0F, 0.85F, 0.45F, pulse);
		for (BlockPos c : CONFLITS) {
			WorldRenderer.drawBox(matrices, vc, c.getX() + 0.05, c.getY() + 0.05, c.getZ() + 0.05, c.getX() + 0.95, c.getY() + 0.95, c.getZ() + 0.95, 1.0F, 0.3F, 0.25F, 1.0F);
		}
		matrices.pop();
	}

	private ApercuChantier() {
	}
}
