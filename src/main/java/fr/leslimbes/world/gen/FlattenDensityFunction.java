package fr.leslimbes.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.List;

/**
 * Fonction de densité « les_limbes:aplanir » : autour de sites donnés (x, z, altitude, rayon), la
 * densité est remplacée par un terrain plat à l'altitude demandée, avec une transition douce.
 *
 * <p>Permet de garantir, quelle que soit la graine, un sol praticable sous les villes et les lieux
 * critiques (voir {@link fr.leslimbes.world.dimension.LimbesSites}).</p>
 *
 * <pre>
 * {"type": "les_limbes:aplanir", "argument": &lt;densité&gt;, "pente": 0.03,
 *  "sites": [{"x": 0, "z": 140, "y": 66, "rayon": 70, "transition": 40}]}
 * </pre>
 */
public record FlattenDensityFunction(DensityFunction argument, List<Site> sites, double pente) implements DensityFunction {

	public record Site(int x, int z, int y, int rayon, int transition) {
		public static final Codec<Site> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("x").forGetter(Site::x),
				Codec.INT.fieldOf("z").forGetter(Site::z),
				Codec.INT.fieldOf("y").forGetter(Site::y),
				Codec.intRange(1, 4096).fieldOf("rayon").forGetter(Site::rayon),
				Codec.intRange(1, 4096).optionalFieldOf("transition", 32).forGetter(Site::transition)
		).apply(instance, Site::new));

		/** 1 à l'intérieur du rayon, 0 au-delà de rayon + transition, lissé entre les deux. */
		public double masque(int bx, int bz) {
			double dx = bx - x;
			double dz = bz - z;
			double d = Math.sqrt(dx * dx + dz * dz);
			if (d <= rayon) {
				return 1.0;
			}
			if (d >= rayon + transition) {
				return 0.0;
			}
			double t = (d - rayon) / transition;
			return 1.0 - t * t * (3.0 - 2.0 * t);
		}
	}

	public static final MapCodec<FlattenDensityFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			DensityFunction.FUNCTION_CODEC.fieldOf("argument").forGetter(FlattenDensityFunction::argument),
			Site.CODEC.listOf().fieldOf("sites").forGetter(FlattenDensityFunction::sites),
			Codec.DOUBLE.optionalFieldOf("pente", 0.03).forGetter(FlattenDensityFunction::pente)
	).apply(instance, FlattenDensityFunction::new));
	public static final CodecHolder<FlattenDensityFunction> CODEC_HOLDER = CodecHolder.of(MAP_CODEC);

	@Override
	public double sample(NoisePos pos) {
		double base = argument.sample(pos);
		return appliquer(base, pos.blockX(), pos.blockY(), pos.blockZ());
	}

	/** Mélange la densité d'entrée avec le terrain plat selon les masques des sites. Pur, testable. */
	public double appliquer(double base, int bx, int by, int bz) {
		double resultat = base;
		for (Site site : sites) {
			double m = site.masque(bx, bz);
			if (m <= 0.0) {
				continue;
			}
			// Le bloc de surface est à site.y - 1 et l'air commence à site.y : le zéro est placé à y - 0,5
			// pour éviter toute ambiguïté de signe (interpolation, flottants) au niveau exact de la surface.
			double plat = (site.y() - 0.5 - by) * pente;
			resultat = MathHelper.lerp(m, resultat, plat);
		}
		return resultat;
	}

	@Override
	public void fill(double[] densities, EachApplier applier) {
		argument.fill(densities, applier);
		for (int i = 0; i < densities.length; i++) {
			NoisePos pos = applier.at(i);
			densities[i] = appliquer(densities[i], pos.blockX(), pos.blockY(), pos.blockZ());
		}
	}

	@Override
	public DensityFunction apply(DensityFunctionVisitor visitor) {
		return visitor.apply(new FlattenDensityFunction(argument.apply(visitor), sites, pente));
	}

	@Override
	public double minValue() {
		return Math.min(argument.minValue(), -2048.0 * pente);
	}

	@Override
	public double maxValue() {
		return Math.max(argument.maxValue(), 2048.0 * pente);
	}

	@Override
	public CodecHolder<? extends DensityFunction> getCodecHolder() {
		return CODEC_HOLDER;
	}
}
