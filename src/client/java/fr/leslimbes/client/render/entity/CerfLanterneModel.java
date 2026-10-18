package fr.leslimbes.client.render.entity;

import fr.leslimbes.entity.CerfLanterneEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

/** Modèle du cerf-lanterne (texture 64x64, voir gen_textures.py : tex_cerf_lanterne). */
public class CerfLanterneModel extends SinglePartEntityModel<CerfLanterneEntity> {
	private final ModelPart root;
	private final ModelPart cou;
	private final ModelPart tete;
	private final ModelPart patteAvG;
	private final ModelPart patteAvD;
	private final ModelPart patteArG;
	private final ModelPart patteArD;
	private final ModelPart queue;

	public CerfLanterneModel(ModelPart root) {
		this.root = root;
		ModelPart corps = root.getChild("corps");
		this.cou = corps.getChild("cou");
		this.tete = cou.getChild("tete");
		this.patteAvG = root.getChild("patte_av_g");
		this.patteAvD = root.getChild("patte_av_d");
		this.patteArG = root.getChild("patte_ar_g");
		this.patteArD = root.getChild("patte_ar_d");
		this.queue = corps.getChild("queue");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		ModelPartData corps = root.addChild("corps", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -9.0F, -7.0F, 8.0F, 9.0F, 14.0F), ModelTransform.pivot(0.0F, 13.0F, 0.0F));
		ModelPartData cou = corps.addChild("cou", ModelPartBuilder.create().uv(0, 23).cuboid(-2.5F, -6.0F, -2.5F, 5.0F, 7.0F, 5.0F), ModelTransform.of(0.0F, -7.0F, -6.0F, -0.45F, 0.0F, 0.0F));
		ModelPartData tete = cou.addChild("tete", ModelPartBuilder.create().uv(20, 23).cuboid(-2.5F, -3.0F, -6.0F, 5.0F, 6.0F, 7.0F), ModelTransform.of(0.0F, -5.0F, 0.0F, 0.45F, 0.0F, 0.0F));
		ModelPartData boisG = tete.addChild("bois_g", ModelPartBuilder.create().uv(0, 36).cuboid(-0.5F, -6.0F, -2.0F, 1.0F, 6.0F, 4.0F), ModelTransform.of(1.6F, -3.0F, -1.0F, -0.2F, 0.0F, 0.35F));
		ModelPartData boisD = tete.addChild("bois_d", ModelPartBuilder.create().uv(10, 36).cuboid(-0.5F, -6.0F, -2.0F, 1.0F, 6.0F, 4.0F), ModelTransform.of(-1.6F, -3.0F, -1.0F, -0.2F, 0.0F, -0.35F));
		boisG.addChild("lanterne_g", ModelPartBuilder.create().uv(40, 36).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F), ModelTransform.pivot(0.0F, -6.0F, -1.0F));
		boisD.addChild("lanterne_d", ModelPartBuilder.create().uv(48, 36).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F), ModelTransform.pivot(0.0F, -6.0F, -1.0F));
		corps.addChild("queue", ModelPartBuilder.create().uv(56, 36).cuboid(-1.0F, 0.0F, 0.0F, 2.0F, 3.0F, 1.0F), ModelTransform.of(0.0F, -8.0F, 7.0F, 0.6F, 0.0F, 0.0F));
		root.addChild("patte_av_g", ModelPartBuilder.create().uv(44, 0).cuboid(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F), ModelTransform.pivot(2.5F, 13.0F, -5.0F));
		root.addChild("patte_av_d", ModelPartBuilder.create().uv(44, 14).cuboid(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F), ModelTransform.pivot(-2.5F, 13.0F, -5.0F));
		root.addChild("patte_ar_g", ModelPartBuilder.create().uv(44, 28).cuboid(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F), ModelTransform.pivot(2.5F, 13.0F, 5.0F));
		root.addChild("patte_ar_d", ModelPartBuilder.create().uv(44, 42).cuboid(-1.5F, 0.0F, -1.5F, 3.0F, 11.0F, 3.0F), ModelTransform.pivot(-2.5F, 13.0F, 5.0F));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(CerfLanterneEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		float amplitude = 0.9F * limbDistance;
		patteAvG.pitch = MathHelper.cos(limbAngle * 0.6662F) * amplitude;
		patteArD.pitch = MathHelper.cos(limbAngle * 0.6662F) * amplitude;
		patteAvD.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * amplitude;
		patteArG.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * amplitude;
		tete.yaw = headYaw * ((float) Math.PI / 180F) * 0.7F;
		tete.pitch = 0.45F + headPitch * ((float) Math.PI / 180F) * 0.5F;
		// Au repos, la tête se baisse de temps en temps comme pour brouter.
		float repos = limbDistance < 0.05F ? MathHelper.clamp(MathHelper.sin(animationProgress * 0.03F) * 2.0F, 0.0F, 0.7F) : 0.0F;
		cou.pitch = -0.45F + repos;
		queue.pitch = 0.6F + MathHelper.sin(animationProgress * 0.2F) * 0.15F;
	}

	@Override
	public ModelPart getPart() {
		return root;
	}
}
