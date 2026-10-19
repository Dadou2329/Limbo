package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

/** Étiquettes du mod (fichiers générés par tools/datagen/gen_data.py). */
public final class LimbesTags {
	/** Blocs que la génération des Limbes peut poser : sols, plafonds, plantes, coraux, cristaux, arbres. */
	public static final TagKey<Block> TERRAIN_NATUREL = TagKey.of(RegistryKeys.BLOCK, LesLimbes.id("terrain_naturel"));
	/** Objets posés par la génération (lanternes perdues) : une Arrivée peut se poser dessus, ils tombent en objets. */
	public static final TagKey<Block> TROUVAILLES = TagKey.of(RegistryKeys.BLOCK, LesLimbes.id("trouvailles"));

	private LimbesTags() {
	}
}
