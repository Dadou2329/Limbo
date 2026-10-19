package fr.leslimbes.world.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.leslimbes.LesLimbes;
import fr.leslimbes.world.dimension.LimbesRegions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Source de biomes « les_limbes:regions » : la région vient de la position (voir {@link LimbesRegions}),
 * le sous-biome des bruits de température et d'humidité du routeur et de la distance au centre. Les
 * villes et lieux à position fixe sont ainsi toujours dans la région prévue, quelle que soit la graine.
 *
 * <pre>{"type": "les_limbes:regions", "biomes": {"prairies": ["les_limbes:plaines_nacrees", "...", "..."], "mer": [...], ...}}</pre>
 */
public class RegionBiomeSource extends BiomeSource {
	public static final MapCodec<RegionBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Biome.REGISTRY_CODEC.listOf()).fieldOf("biomes").forGetter(s -> s.biomes)
	).apply(instance, RegionBiomeSource::new));

	private final Map<String, List<RegistryEntry<Biome>>> biomes;
	private final RegistryEntry<Biome> defaut;

	public RegionBiomeSource(Map<String, List<RegistryEntry<Biome>>> biomes) {
		this.biomes = biomes;
		List<RegistryEntry<Biome>> prairies = biomes.get("prairies");
		this.defaut = prairies != null && !prairies.isEmpty() ? prairies.get(0) : biomes.values().iterator().next().get(0);
		for (LimbesRegions.Region r : LimbesRegions.Region.values()) {
			List<RegistryEntry<Biome>> l = biomes.get(r.id);
			if (l == null || l.size() != 3) {
				LesLimbes.LOGGER.warn("Source de biomes régionale : la région {} doit déclarer exactement trois biomes.", r.id);
			}
		}
	}

	@Override
	protected MapCodec<? extends BiomeSource> getCodec() {
		return CODEC;
	}

	@Override
	protected Stream<RegistryEntry<Biome>> biomeStream() {
		return biomes.values().stream().flatMap(List::stream);
	}

	@Override
	public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
		int bx = x << 2;
		int bz = z << 2;
		LimbesRegions.Point p = LimbesRegions.evaluer(bx + 2, bz + 2);
		MultiNoiseUtil.NoiseValuePoint v = noise.sample(x, y, z);
		float temperature = MultiNoiseUtil.toFloat(v.temperatureNoise());
		float humidite = MultiNoiseUtil.toFloat(v.humidityNoise());
		int sous = LimbesRegions.sousBiome(p.region(), p.n(), temperature, humidite);
		List<RegistryEntry<Biome>> l = biomes.get(p.region().id);
		if (l == null || l.isEmpty()) {
			return defaut;
		}
		return l.get(Math.min(sous, l.size() - 1));
	}
}
