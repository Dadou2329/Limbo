package fr.leslimbes.world.dimension;

import fr.leslimbes.LesLimbes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

/** Clés et accès à la dimension des Limbes (définie dans data/les_limbes/dimension/limbes.json). */
public final class LimbesDimension {
	public static final RegistryKey<World> MONDE = RegistryKey.of(RegistryKeys.WORLD, LesLimbes.id("limbes"));
	public static final RegistryKey<DimensionType> TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, LesLimbes.id("limbes"));
	/** Sous cette altitude, les brumes ramènent les joueurs en lieu sûr. */
	public static final int ALTITUDE_BRUMES = 12;

	public static boolean estDansLesLimbes(World world) {
		return world.getRegistryKey() == MONDE;
	}

	@Nullable
	public static ServerWorld monde(MinecraftServer server) {
		return server.getWorld(MONDE);
	}

	private LimbesDimension() {
	}
}
