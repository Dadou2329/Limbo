package fr.leslimbes.client.render.block;

import fr.leslimbes.block.VitrineBlock;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

/** Objet exposé dans une vitrine : flotte et tourne lentement au centre du socle de verre. */
public class VitrineRenderer implements BlockEntityRenderer<VitrineBlock.VitrineBlockEntity> {
	private final ItemRenderer itemRenderer;

	public VitrineRenderer(BlockEntityRendererFactory.Context ctx) {
		this.itemRenderer = ctx.getItemRenderer();
	}

	@Override
	public void render(VitrineBlock.VitrineBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemStack stack = entity.getObjet();
		if (stack.isEmpty()) {
			return;
		}
		float temps = entity.getWorld() == null ? 0.0F : (entity.getWorld().getTime() % 24000L) + tickDelta;
		matrices.push();
		matrices.translate(0.5, 0.58 + Math.sin(temps / 14.0) * 0.03, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(temps * 1.5F));
		matrices.scale(0.6F, 0.6F, 0.6F);
		itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
		matrices.pop();
	}
}
