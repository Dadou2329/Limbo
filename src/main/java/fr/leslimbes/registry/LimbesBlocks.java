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

	// --- Domaine ---------------------------------------------------------------------------------
	public static final Block VITRINE = register("vitrine", new fr.leslimbes.block.VitrineBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).strength(1.0F, 3.0F).sounds(BlockSoundGroup.GLASS).nonOpaque()
					.luminance(state -> 6).suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never)));
	public static final Block PORTE_DE_RELAIS = register("porte_de_relais", new fr.leslimbes.block.PorteDeRelaisBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).strength(-1.0F, 3600000.0F).sounds(BlockSoundGroup.AMETHYST_BLOCK).nonOpaque()
					.luminance(state -> state.get(fr.leslimbes.block.PorteDeRelaisBlock.EVEILLEE) ? 13 : 4).pistonBehavior(PistonBehavior.BLOCK)));

	// --- Mer suspendue ----------------------------------------------------------------------------
	public static final Block SABLE_DE_NACRE = register("sable_de_nacre", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).strength(0.5F).sounds(BlockSoundGroup.SAND)));
	public static final Block CORAIL_DE_NACRE = register("corail_de_nacre", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.PINK).strength(0.6F).sounds(BlockSoundGroup.CORAL).luminance(state -> 5)));
	public static final Block CORAIL_AZUR = register("corail_azur", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).strength(0.6F).sounds(BlockSoundGroup.CORAL).luminance(state -> 5)));

	// --- Jardins des Géants ----------------------------------------------------------------------
	public static final Block BUCHE_DE_GEANT = register("buche_de_geant", new PillarBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.BROWN).instrument(NoteBlockInstrument.BASS).strength(2.5F).sounds(BlockSoundGroup.WOOD).burnable()));
	public static final Block PLANCHES_DE_GEANT = register("planches_de_geant", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.BROWN).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sounds(BlockSoundGroup.WOOD).burnable()));
	public static final Block FEUILLES_DE_GEANT = register("feuilles_de_geant", new LeavesBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.GREEN).strength(0.2F).ticksRandomly().sounds(BlockSoundGroup.AZALEA_LEAVES).nonOpaque()
					.allowsSpawning(LimbesBlocks::canSpawnOnLeaves).suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never).burnable()
					.pistonBehavior(PistonBehavior.DESTROY).solidBlock(LimbesBlocks::never)));
	public static final Block HERBE_DES_GEANTS = register("herbe_des_geants", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.GREEN).strength(0.6F).sounds(BlockSoundGroup.GRASS)));
	public static final Block PETALE_ROSE = register("petale_rose", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.PINK).strength(0.4F).sounds(BlockSoundGroup.MOSS_BLOCK).luminance(state -> 7)));
	public static final Block PETALE_BLEU = register("petale_bleu", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).strength(0.4F).sounds(BlockSoundGroup.MOSS_BLOCK).luminance(state -> 7)));
	public static final Block SEVE_ASCENDANTE = register("seve_ascendante", new fr.leslimbes.block.SeveAscendanteBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.LIME).strength(0.3F).sounds(BlockSoundGroup.HONEY).nonOpaque().luminance(state -> 8)
					.suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never).pistonBehavior(PistonBehavior.DESTROY), 0.42));

	// --- Désert des Heures -----------------------------------------------------------------------
	public static final Block SABLE_D_OPALE = register("sable_d_opale", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.OFF_WHITE).strength(0.5F).sounds(BlockSoundGroup.SAND)));
	public static final Block GRES_DES_HEURES = register("gres_des_heures", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(0.8F).sounds(BlockSoundGroup.STONE)));
	public static final Block GRES_DES_HEURES_TAILLE = register("gres_des_heures_taille", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(0.8F).sounds(BlockSoundGroup.STONE)));
	public static final Block PIERRE_DES_HEURES = register("pierre_des_heures", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.5F, 6.0F).sounds(BlockSoundGroup.STONE)));
	public static final Block CADRAN_DES_HEURES = register("cadran_des_heures", new fr.leslimbes.block.CadranBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.GOLD).requiresTool().strength(2.0F, 6.0F).sounds(BlockSoundGroup.STONE).luminance(state -> 6)));
	public static final Block PAVE_HORAIRE = register("pave_horaire", new fr.leslimbes.block.PaveHoraireBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(-1.0F, 3600000.0F).dropsNothing().nonOpaque().sounds(BlockSoundGroup.AMETHYST_BLOCK)
					.luminance(state -> state.get(fr.leslimbes.block.PaveHoraireBlock.VISIBLE) ? 9 : 2).suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never)
					.pistonBehavior(PistonBehavior.BLOCK)));
	public static final Block PONT_DE_LUMIERE = register("pont_de_lumiere", new fr.leslimbes.block.PontDeLumiereBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(-1.0F, 3600000.0F).dropsNothing().nonOpaque().noBlockBreakParticles().sounds(BlockSoundGroup.GLASS)
					.luminance(state -> 13).suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never).pistonBehavior(PistonBehavior.BLOCK)));

	// --- Montagnes de Verre ----------------------------------------------------------------------
	public static final Block PIERRE_DE_VERRE = register("pierre_de_verre", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.8F, 6.0F).sounds(BlockSoundGroup.DEEPSLATE)));
	public static final Block PIERRE_DE_VERRE_TAILLEE = register("pierre_de_verre_taillee", new Block(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.8F, 6.0F).sounds(BlockSoundGroup.DEEPSLATE)));
	public static final Block VERRE_DE_MONTAGNE = register("verre_de_montagne", new net.minecraft.block.TransparentBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).requiresTool().strength(1.2F, 4.0F).sounds(BlockSoundGroup.GLASS).nonOpaque()
					.allowsSpawning(LimbesBlocks::canSpawnOnLeaves).solidBlock(LimbesBlocks::never).suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never)));
	public static final Block CRISTAL_RESONNANT = register("cristal_resonnant", new fr.leslimbes.block.CristalResonnantBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.CYAN).requiresTool().strength(1.5F, 4.0F).sounds(BlockSoundGroup.AMETHYST_BLOCK).luminance(state -> 10).nonOpaque()));
	public static final Block PONT_DE_VERRE = register("pont_de_verre", new fr.leslimbes.block.PontDeVerreBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).strength(-1.0F, 3600000.0F).dropsNothing().nonOpaque().sounds(BlockSoundGroup.GLASS)
					.luminance(state -> state.get(fr.leslimbes.block.PontDeVerreBlock.FORME) ? 8 : 0).suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never)
					.pistonBehavior(PistonBehavior.BLOCK)));
	public static final Block COURANT_CRISTALLIN = register("courant_cristallin", new fr.leslimbes.block.SeveAscendanteBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.LIGHT_BLUE).strength(0.3F).sounds(BlockSoundGroup.AMETHYST_CLUSTER).nonOpaque().luminance(state -> 9)
					.suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never).pistonBehavior(PistonBehavior.DESTROY), 0.55));

	// --- Couronne Immobile -----------------------------------------------------------------------
	public static final Block PIERRE_DE_COURONNE = register("pierre_de_couronne", new Block(couronne()));
	public static final Block PIERRE_DE_COURONNE_TAILLEE = register("pierre_de_couronne_taillee", new Block(couronne()));
	public static final Block PIERRE_DE_COURONNE_POLIE = register("pierre_de_couronne_polie", new Block(couronne()));
	public static final Block PIERRE_DE_COURONNE_SCULPTEE = register("pierre_de_couronne_sculptee", new Block(couronne()));
	public static final Block LANTERNE_ROYALE = register("lanterne_royale", new LanternBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.GOLD).solid().requiresTool().strength(3.5F)
					.sounds(BlockSoundGroup.LANTERN).luminance(state -> 15).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block LANTERNE_ROYALE_ETEINTE = register("lanterne_royale_eteinte", new fr.leslimbes.block.LanterneRoyaleEteinteBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.GOLD).solid().requiresTool().strength(3.5F)
					.sounds(BlockSoundGroup.LANTERN).nonOpaque().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)));
	public static final Block PORTE_ROYALE = register("porte_royale", new fr.leslimbes.block.PorteRoyaleBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(-1.0F, 3600000.0F).dropsNothing().nonOpaque().ticksRandomly().sounds(BlockSoundGroup.AMETHYST_BLOCK)
					.luminance(state -> state.get(fr.leslimbes.block.PorteRoyaleBlock.OUVERTE) ? 0 : 10).suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never)
					.pistonBehavior(PistonBehavior.BLOCK)));
	public static final Block PONT_ROYAL = register("pont_royal", new fr.leslimbes.block.PontRoyalBlock(
			AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(-1.0F, 3600000.0F).dropsNothing().nonOpaque().ticksRandomly().sounds(BlockSoundGroup.GLASS)
					.luminance(state -> state.get(fr.leslimbes.block.PontRoyalBlock.FORME) ? 12 : 0).suffocates(LimbesBlocks::never).blockVision(LimbesBlocks::never)
					.pistonBehavior(PistonBehavior.BLOCK)));

	private static AbstractBlock.Settings couronne() {
		return AbstractBlock.Settings.create().mapColor(MapColor.WHITE).instrument(NoteBlockInstrument.BASEDRUM)
				.requiresTool().strength(1.8F, 6.0F).sounds(BlockSoundGroup.STONE);
	}

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
