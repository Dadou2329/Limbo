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

	public static final BlockEntityType<fr.leslimbes.block.VitrineBlock.VitrineBlockEntity> VITRINE = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			LesLimbes.id("vitrine"), BlockEntityType.Builder.create(fr.leslimbes.block.VitrineBlock.VitrineBlockEntity::new, LimbesBlocks.VITRINE).build());
	public static final BlockEntityType<fr.leslimbes.block.PorteDeRelaisBlock.PorteDeRelaisBlockEntity> PORTE_DE_RELAIS = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			LesLimbes.id("porte_de_relais"), BlockEntityType.Builder.create(fr.leslimbes.block.PorteDeRelaisBlock.PorteDeRelaisBlockEntity::new, LimbesBlocks.PORTE_DE_RELAIS).build());

	public static void init() {
	}

	private LimbesBlockEntities() {
	}
}
