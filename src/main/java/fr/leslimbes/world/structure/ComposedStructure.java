package fr.leslimbes.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.leslimbes.registry.LimbesWorldgen;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.List;
import java.util.Optional;

/**
 * Structure « les_limbes:composee » : un ensemble de gabarits placés à des décalages fixes autour d'un
 * point d'ancrage. Permet d'écrire des villes et des lieux scénarisés avec une disposition maîtrisée,
 * contrairement au jigsaw aléatoire.
 *
 * <pre>
 * {"type": "les_limbes:composee", "biomes": "#les_limbes:limbes", "step": "surface_structures",
 *  "terrain_adaptation": "beard_thin", "ancre": [8, 0, 8], "y": 66,
 *  "pieces": [{"gabarit": "les_limbes:clairval/place", "decalage": [0, 0, 0]},
 *             {"gabarit": "les_limbes:clairval/auberge", "decalage": [14, 0, -9], "rotation": "clockwise_90"}]}
 * </pre>
 * Sans « y », l'altitude est celle de la surface au point d'ancrage (heightmap de génération).
 */
public class ComposedStructure extends Structure {

	public record Piece(Identifier gabarit, Vec3i decalage, BlockRotation rotation) {
		public static final Codec<Piece> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("gabarit").forGetter(Piece::gabarit),
				Vec3i.CODEC.optionalFieldOf("decalage", Vec3i.ZERO).forGetter(Piece::decalage),
				BlockRotation.CODEC.optionalFieldOf("rotation", BlockRotation.NONE).forGetter(Piece::rotation)
		).apply(instance, Piece::new));
	}

	public static final MapCodec<ComposedStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			configCodecBuilder(instance),
			Piece.CODEC.listOf().fieldOf("pieces").forGetter(s -> s.pieces),
			Vec3i.CODEC.optionalFieldOf("ancre", new Vec3i(8, 0, 8)).forGetter(s -> s.ancre),
			Codec.INT.optionalFieldOf("y").forGetter(s -> s.y),
			Heightmap.Type.CODEC.optionalFieldOf("heightmap", Heightmap.Type.WORLD_SURFACE_WG).forGetter(s -> s.heightmap)
	).apply(instance, ComposedStructure::new));

	private final List<Piece> pieces;
	private final Vec3i ancre;
	private final Optional<Integer> y;
	private final Heightmap.Type heightmap;

	public ComposedStructure(Config config, List<Piece> pieces, Vec3i ancre, Optional<Integer> y, Heightmap.Type heightmap) {
		super(config);
		this.pieces = pieces;
		this.ancre = ancre;
		this.y = y;
		this.heightmap = heightmap;
	}

	@Override
	protected Optional<StructurePosition> getStructurePosition(Context context) {
		ChunkPos chunkPos = context.chunkPos();
		int x = chunkPos.getStartX() + ancre.getX();
		int z = chunkPos.getStartZ() + ancre.getZ();
		int baseY = y.orElseGet(() -> context.chunkGenerator().getHeight(x, z, heightmap, context.world(), context.noiseConfig()));
		BlockPos origine = new BlockPos(x, baseY + ancre.getY(), z);
		return Optional.of(new StructurePosition(origine, collector -> {
			for (Piece piece : pieces) {
				BlockPos pos = origine.add(piece.decalage());
				collector.addPiece(new ComposedPiece(context.structureTemplateManager(), piece.gabarit(), pos, piece.rotation()));
			}
		}));
	}

	@Override
	public StructureType<?> getType() {
		return LimbesWorldgen.STRUCTURE_COMPOSEE;
	}

	public List<Piece> pieces() {
		return pieces;
	}
}
