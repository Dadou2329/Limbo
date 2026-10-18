package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.block.CristalDeNacreBlock;
import fr.leslimbes.block.DalleDeRoseeBlock;
import fr.leslimbes.block.MeuleDeMoulinBlock;
import fr.leslimbes.block.MoulinBlock;
import fr.leslimbes.block.FibresNacreesBlock;
import fr.leslimbes.block.HerbeArgenteeBlock;
import fr.leslimbes.block.HerbeDeLAubeBlock;
import fr.leslimbes.block.PorteDesLimbesBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Blocs des Limbes. Les identifiants sont en français (snake_case) : ils apparaissent dans les données. */
public final class LimbesBlocks {
	/** Ordre d'enregistrement conservé pour l'onglet créatif. */
	public static final List<Block> TOUS = new ArrayList<>();

	public static final BlockSetType TYPE_AUBE = BlockSetTypeBuilder.copyOf(BlockSetType.CHERRY).register(LesLimbes.id("aube"));

	// --- Pierre nacrée ---------------------------------------------------------------
	public static final Block PIERRE_NACREE = register("pierre_nacree", new Block(pierre()));
	public static final Block PIERRE_NACREE_TAILLEE = register("pierre_nacree_taillee", new Block(pierre()));
	public static final Block PIERRE_NACREE_POLIE = register("pierre_nacree_polie", new Block(pierre()));
	public static final Block PIERRE_NACREE_SCULPTEE = register("pierre_nacree_sculptee", new Block(pierre()));
	public static final Block ESCALIER_DE_PIERRE_NACREE = register("escalier_de_pierre_nacree",
			new StairsBlock(PIERRE_NACREE_TAILLEE.getDefaultState(), pierre()));
	public static final Block DALLE_DE_PIERRE_NACREE = register("dalle_de_pierre_nacree", new SlabBlock(pierre()));
	public static final Block MURET_DE_PIERRE_NACREE = register("muret_de_pierre_nacree", new WallBlock(pierre().solid()));
	public static final Block CRISTAL_DE_NACRE = register("cristal_de_nacre", new CristalDeNacreBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE_GRAY).strength(1.2F, 4.0F)
					.sounds(BlockSoundGroup.AMETHYST_BLOCK).luminance(state -> 13).nonOpaque().requiresTool()));

	// --- Sol des Prairies de l'Aube -----------------------------------------------------
	public static final Block TERRE_DES_LIMBES = register("terre_des_limbes", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_PURPLE).strength(0.5F).sounds(BlockSoundGroup.GRAVEL)));
	public static final Block HERBE_ARGENTEE = register("herbe_argentee", new HerbeArgenteeBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.PALE_GREEN).ticksRandomly().strength(0.6F).sounds(BlockSoundGroup.GRASS)));

	// --- Bois d'aube ---------------------------------------------------------------------
	public static final Block BUCHE_D_AUBE = register("buche_d_aube", new PillarBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_PINK).instrument(NoteBlockInstrument.BASS)
					.strength(2.0F).sounds(BlockSoundGroup.CHERRY_WOOD).burnable()));
	public static final Block PLANCHES_D_AUBE = register("planches_d_aube", new Block(bois()));
	public static final Block ESCALIER_D_AUBE = register("escalier_d_aube", new StairsBlock(PLANCHES_D_AUBE.getDefaultState(), bois()));
	public static final Block DALLE_D_AUBE = register("dalle_d_aube", new SlabBlock(bois()));
	public static final Block BARRIERE_D_AUBE = register("barriere_d_aube", new FenceBlock(bois().solid()));
	public static final Block PORTE_D_AUBE = register("porte_d_aube", new DoorBlock(TYPE_AUBE,
			AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_PINK).instrument(NoteBlockInstrument.BASS)
					.strength(3.0F).nonOpaque().burnable().pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block FEUILLES_D_AUBE = register("feuilles_d_aube", new LeavesBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.PINK).strength(0.2F).ticksRandomly()
					.sounds(BlockSoundGroup.CHERRY_LEAVES).nonOpaque()
					.allowsSpawning(LimbesBlocks::canSpawnOnLeaves).suffocates(LimbesBlocks::never)
					.blockVision(LimbesBlocks::never).burnable().pistonBehavior(PistonBehavior.DESTROY)
					.solidBlock(LimbesBlocks::never)));
	public static final RegistryKey<net.minecraft.world.gen.feature.ConfiguredFeature<?, ?>> ARBRE_D_AUBE =
			RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, LesLimbes.id("arbre_d_aube"));
	public static final Block POUSSE_D_AUBE = register("pousse_d_aube", new SaplingBlock(
			new SaplingGenerator(LesLimbes.MOD_ID + ":aube", Optional.empty(), Optional.of(ARBRE_D_AUBE), Optional.empty()),
			AbstractBlock.Settings.create().mapColor(MapColor.PINK).noCollision().ticksRandomly().breakInstantly()
					.sounds(BlockSoundGroup.GRASS).pistonBehavior(PistonBehavior.DESTROY)));

	// --- Végétation ------------------------------------------------------------------------
	public static final Block FIBRES_NACREES = register("fibres_nacrees", new FibresNacreesBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).replaceable().noCollision().breakInstantly()
					.sounds(BlockSoundGroup.GRASS).offset(AbstractBlock.OffsetType.XYZ).luminance(state -> 6)
					.pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block HERBE_DE_L_AUBE = register("herbe_de_l_aube", new HerbeDeLAubeBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).replaceable().noCollision().breakInstantly()
					.sounds(BlockSoundGroup.GRASS).offset(AbstractBlock.OffsetType.XZ).pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block FLEUR_DE_ROSEE = register("fleur_de_rosee", new FlowerBlock(StatusEffects.REGENERATION, 6.0F,
			AbstractBlock.Settings.create().mapColor(MapColor.WHITE).noCollision().breakInstantly()
					.sounds(BlockSoundGroup.GRASS).offset(AbstractBlock.OffsetType.XZ).pistonBehavior(PistonBehavior.DESTROY)));

	// --- Lumière et passages ----------------------------------------------------------------
	public static final Block LANTERNE_NACREE = register("lanterne_nacree", new LanternBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).solid().requiresTool().strength(3.5F)
					.sounds(BlockSoundGroup.LANTERN).luminance(state -> 15).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block PORTE_DES_LIMBES = register("porte_des_limbes", new PorteDesLimbesBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.BLACK).strength(-1.0F, 3600000.0F).dropsNothing()
					.nonOpaque().noCollision().luminance(state -> state.get(PorteDesLimbesBlock.ACTIVE) ? 11 : 2)
					.sounds(BlockSoundGroup.AMETHYST_BLOCK).pistonBehavior(PistonBehavior.BLOCK)));

	// --- Lieux scénarisés des Prairies -------------------------------------------------------------
	public static final Block DALLE_DE_ROSEE = register("dalle_de_rosee", new DalleDeRoseeBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).strength(-1.0F, 3600000.0F).dropsNothing().nonOpaque()
					.luminance(state -> state.get(DalleDeRoseeBlock.REVELEE) ? 11 : 2).sounds(BlockSoundGroup.AMETHYST_BLOCK)
					.suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never).pistonBehavior(PistonBehavior.BLOCK)));
	public static final Block MEULE_ROYALE = register("meule_royale", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).requiresTool().strength(2.0F, 6.0F).sounds(BlockSoundGroup.STONE)));
	public static final Block MEULE_DE_MOULIN = register("meule_de_moulin", new MeuleDeMoulinBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).requiresTool().strength(2.5F, 6.0F).sounds(BlockSoundGroup.STONE)));
	public static final Block AILES_DE_MOULIN = register("ailes_de_moulin", new MoulinBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_PINK).strength(2.0F, 3.0F).sounds(BlockSoundGroup.CHERRY_WOOD).nonOpaque()));

	private static AbstractBlock.Settings pierre() {
		return AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).instrument(NoteBlockInstrument.BASEDRUM)
				.requiresTool().strength(1.5F, 6.0F).sounds(BlockSoundGroup.STONE);
	}

	private static AbstractBlock.Settings bois() {
		return AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_PINK).instrument(NoteBlockInstrument.BASS)
				.strength(2.0F, 3.0F).sounds(BlockSoundGroup.CHERRY_WOOD).burnable();
	}

	private static boolean never(BlockState state, BlockView world, BlockPos pos) {
		return false;
	}

	private static boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
		return type == EntityType.OCELOT || type == EntityType.PARROT;
	}

	private static <B extends Block> B register(String path, B block) {
		Identifier id = LesLimbes.id(path);
		Registry.register(Registries.BLOCK, id, block);
		TOUS.add(block);
		return block;
	}

	public static void init() {
	}

	private LimbesBlocks() {
	}
}
