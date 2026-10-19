package fr.leslimbes.world.gen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.leslimbes.world.dimension.LimbesRegions;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;

/**
 * Fonction de densité « les_limbes:regions » : relief des six régions. Pour chaque colonne, la hauteur de
 * surface est calculée par {@link LimbesRegions#hauteurMelangee} à partir des bruits fournis (collines,
 * îles, température, humidité), puis la densité vaut {@code (h - 0,5 - y) × pente} comme pour
 * {@code les_limbes:aplanir} : le zéro se trouve entre le dernier bloc plein et le premier bloc d'air.
 *
 * <pre>{"type": "les_limbes:regions", "collines": &lt;bruit&gt;, "iles": &lt;bruit&gt;, "temperature": &lt;bruit&gt;, "humidite": &lt;bruit&gt;}</pre>
 */
public record RegionsDensityFunction(DensityFunction collines, DensityFunction iles, DensityFunction temperature, DensityFunction humidite) implements DensityFunction {
	public static final MapCodec<RegionsDensityFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			DensityFunction.FUNCTION_CODEC.fieldOf("collines").forGetter(RegionsDensityFunction::collines),
			DensityFunction.FUNCTION_CODEC.fieldOf("iles").forGetter(RegionsDensityFunction::iles),
			DensityFunction.FUNCTION_CODEC.fieldOf("temperature").forGetter(RegionsDensityFunction::temperature),
			DensityFunction.FUNCTION_CODEC.fieldOf("humidite").forGetter(RegionsDensityFunction::humidite)
	).apply(instance, RegionsDensityFunction::new));
	public static final CodecHolder<RegionsDensityFunction> CODEC_HOLDER = CodecHolder.of(MAP_CODEC);

	@Override
	public double sample(NoisePos pos) {
		double h = LimbesRegions.hauteurMelangee(pos.blockX(), pos.blockZ(), collines.sample(pos), iles.sample(pos), temperature.sample(pos), humidite.sample(pos));
		double pente = LimbesRegions.penteMelangee(pos.blockX(), pos.blockZ());
		return (h - 0.5 - pos.blockY()) * pente;
	}

	@Override
	public void fill(double[] densities, EachApplier applier) {
		applier.fill(densities, this);
	}

	@Override
	public DensityFunction apply(DensityFunctionVisitor visitor) {
		return visitor.apply(new RegionsDensityFunction(collines.apply(visitor), iles.apply(visitor), temperature.apply(visitor), humidite.apply(visitor)));
	}

	@Override
	public double minValue() {
		return -256.0 / 14.0;
	}

	@Override
	public double maxValue() {
		return 256.0 / 14.0;
	}

	@Override
	public CodecHolder<? extends DensityFunction> getCodecHolder() {
		return CODEC_HOLDER;
	}
}
