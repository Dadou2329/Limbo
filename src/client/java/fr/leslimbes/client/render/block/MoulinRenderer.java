package fr.leslimbes.client.render.block;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.block.MoulinBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/** Ailes du moulin : quatre pales en bois d'aube, tendues de toile, qui tournent quand le moulin est actif. */
public class MoulinRenderer implements BlockEntityRenderer<MoulinBlock.MoulinBlockEntity> {
	private static final Identifier BOIS = LesLimbes.id("textures/block/planches_d_aube.png");
	private static final Identifier TOILE = LesLimbes.id("textures/block/toile_de_moulin.png");

	public MoulinRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(MoulinBlock.MoulinBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		boolean actif = entity.getCachedState().get(MoulinBlock.ACTIF);
		Direction facing = entity.getCachedState().get(MoulinBlock.FACING);
		float angle = 0.0F;
		if (actif && entity.getWorld() != null) {
			angle = ((entity.getWorld().getTime() % 3600L) + tickDelta) * 1.6F;
		}
		matrices.push();
		matrices.translate(0.5, 0.5, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
		// Les ailes sont dans le plan perpendiculaire à la façade, légèrement devant l'axe.
		matrices.translate(0.0, 0.0, 0.62);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
		VertexConsumer bois = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(BOIS));
		VertexConsumer toile = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TOILE));
		for (int i = 0; i < 4; i++) {
			matrices.push();
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F * i));
			// bras : 0,3 x 5,5 blocs
			boite(matrices, bois, -0.12F, 0.0F, -0.06F, 0.12F, 5.4F, 0.06F, light);
			// toile : panneau 1,4 x 4,2 d'un côté du bras
			boite(matrices, toile, 0.12F, 1.0F, -0.02F, 1.5F, 5.2F, 0.02F, light);
			matrices.pop();
		}
		matrices.pop();
	}

	private static void boite(MatrixStack matrices, VertexConsumer vc, float x0, float y0, float z0, float x1, float y1, float z1, int light) {
		MatrixStack.Entry e = matrices.peek();
		Matrix4f m = e.getPositionMatrix();
		// avant / arrière
		quad(vc, m, e, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, light, 0, 0, 1);
		quad(vc, m, e, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, light, 0, 0, -1);
		// côtés
		quad(vc, m, e, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, light, -1, 0, 0);
		quad(vc, m, e, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, light, 1, 0, 0);
		// haut / bas
		quad(vc, m, e, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, light, 0, 1, 0);
		quad(vc, m, e, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, light, 0, -1, 0);
	}

	private static void quad(VertexConsumer vc, Matrix4f m, MatrixStack.Entry e, float ax, float ay, float az, float bx, float by, float bz,
							 float cx, float cy, float cz, float dx, float dy, float dz, int light, float nx, float ny, float nz) {
		vc.vertex(m, ax, ay, az).color(255, 255, 255, 255).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, nx, ny, nz);
		vc.vertex(m, bx, by, bz).color(255, 255, 255, 255).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, nx, ny, nz);
		vc.vertex(m, cx, cy, cz).color(255, 255, 255, 255).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, nx, ny, nz);
		vc.vertex(m, dx, dy, dz).color(255, 255, 255, 255).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, nx, ny, nz);
	}

	@Override
	public boolean rendersOutsideBoundingBox(MoulinBlock.MoulinBlockEntity blockEntity) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 128;
	}
}
