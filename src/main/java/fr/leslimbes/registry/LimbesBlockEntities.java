package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.block.MoulinBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Blocs-entités du mod. */
public final class LimbesBlockEntities {
	public static final BlockEntityType<MoulinBlock.MoulinBlockEntity> AILES_DE_MOULIN = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			LesLimbes.id("ailes_de_moulin"), BlockEntityType.Builder.create(MoulinBlock.MoulinBlockEntity::new, LimbesBlocks.AILES_DE_MOULIN).build());

	public static void init() {
	}

	private LimbesBlockEntities() {
	}
}
