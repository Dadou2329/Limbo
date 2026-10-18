package fr.leslimbes.client;

import fr.leslimbes.client.dev.CaptureAutomatique;
import fr.leslimbes.client.hud.LimbesHud;
import fr.leslimbes.client.hud.MarqueurMonde;
import fr.leslimbes.client.input.LimbesKeys;
import fr.leslimbes.client.network.ClientPayloadHandlers;
import fr.leslimbes.client.render.dimension.LimbesSky;
import fr.leslimbes.client.render.entity.CerfLanterneModel;
import fr.leslimbes.client.render.entity.CerfLanterneRenderer;
import fr.leslimbes.client.render.entity.FragmentDeCouronneFeature;
import fr.leslimbes.client.render.entity.HabitantRenderer;
import fr.leslimbes.client.render.entity.PoissonLuneModel;
import fr.leslimbes.client.render.entity.PoissonLuneRenderer;
import fr.leslimbes.client.render.entity.VeilleurEgareModel;
import fr.leslimbes.client.render.entity.VeilleurEgareRenderer;
import fr.leslimbes.client.render.modele.ModeleDonneesRegistry;
import fr.leslimbes.client.render.modele.RenduDonneesRenderer;
import fr.leslimbes.client.render.particle.LimbesParticleFactories;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.registry.LimbesEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EntityType;

/** Point d'entrée client : rendu, interface, touches. Jamais chargé sur un serveur dédié. */
public final class LesLimbesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), LimbesBlocks.FIBRES_NACREES, LimbesBlocks.HERBE_DE_L_AUBE,
				LimbesBlocks.FLEUR_DE_ROSEE, LimbesBlocks.POUSSE_D_AUBE, LimbesBlocks.LANTERNE_NACREE, LimbesBlocks.PORTE_D_AUBE);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(), LimbesBlocks.FEUILLES_D_AUBE);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), LimbesBlocks.PORTE_DES_LIMBES, LimbesBlocks.CRISTAL_DE_NACRE, LimbesBlocks.DALLE_DE_ROSEE);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), LimbesBlocks.AILES_DE_MOULIN);
		net.minecraft.client.render.block.entity.BlockEntityRendererFactories.register(fr.leslimbes.registry.LimbesBlockEntities.AILES_DE_MOULIN, fr.leslimbes.client.render.block.MoulinRenderer::new);

		EntityModelLayerRegistry.registerModelLayer(PoissonLuneRenderer.LAYER, PoissonLuneModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(CerfLanterneRenderer.LAYER, CerfLanterneModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(VeilleurEgareRenderer.LAYER, VeilleurEgareModel::getTexturedModelData);
		EntityRendererRegistry.register(LimbesEntities.HABITANT, HabitantRenderer::new);
		EntityRendererRegistry.register(LimbesEntities.POISSON_LUNE, PoissonLuneRenderer::new);
		EntityRendererRegistry.register(LimbesEntities.CERF_LANTERNE, CerfLanterneRenderer::new);
		EntityRendererRegistry.register(LimbesEntities.VEILLEUR_EGARE, VeilleurEgareRenderer::new);
		ModeleDonneesRegistry.init();
		EntityRendererRegistry.register(LimbesEntities.RAIE_CELESTE, ctx -> new RenduDonneesRenderer<>(ctx, "raie_celeste"));
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityType.PLAYER && entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
				registrationHelper.register(new FragmentDeCouronneFeature(playerRenderer));
			}
		});

		LimbesParticleFactories.init();
		LimbesSky.init();
		ClientPayloadHandlers.init();
		LimbesKeys.init();
		LimbesHud.init();
		MarqueurMonde.init();
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientLimbesData.reinitialiser());
		CaptureAutomatique.init();
	}
}
