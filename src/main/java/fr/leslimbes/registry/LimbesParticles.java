package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Particules originales : le serveur ne déclenche que des « effets » ; le client les décline. */
public final class LimbesParticles {
	public static final SimpleParticleType ETOILE_ROYALE = register("etoile_royale", FabricParticleTypes.simple());
	public static final SimpleParticleType ECLAT_DE_NACRE = register("eclat_de_nacre", FabricParticleTypes.simple());
	public static final SimpleParticleType LUEUR = register("lueur", FabricParticleTypes.simple());
	public static final SimpleParticleType BRUME = register("brume", FabricParticleTypes.simple());

	private static SimpleParticleType register(String path, SimpleParticleType type) {
		return Registry.register(Registries.PARTICLE_TYPE, LesLimbes.id(path), type);
	}

	public static void init() {
	}

	private LimbesParticles() {
	}
}
