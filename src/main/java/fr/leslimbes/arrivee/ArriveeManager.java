package fr.leslimbes.arrivee;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.npc.dialogue.Conditions;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.registry.LimbesTags;
import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.state.LimbesWorldState;
import fr.leslimbes.world.structure.MarqueursDeStructure;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Les Arrivées : parfois, le ciel se plisse et quelque chose rejoint les Limbes. Le gestionnaire
 * choisit un type éligible (prérequis du monde, unicité), un site réservé libre et vérifié (aucune
 * construction, aucun joueur), place le gabarit, fait apparaître ses occupants, annonce l'événement
 * et conserve son état. Les grands événements restent jusqu'à leur résolution ; les petits expirent.
 */
public final class ArriveeManager {
	/** Période d'évaluation (ticks) et nombre maximal d'événements simultanés. */
	public static final int PERIODE = 1200;
	public static final int MAX_SIMULTANES = 2;
	/** Délai minimal entre deux déclenchements automatiques (ticks). */
	public static final long INTERVALLE_MIN = 12000;

	public static void tick(MinecraftServer server) {
		ServerWorld limbes = LimbesDimension.monde(server);
		if (limbes == null || limbes.getTime() % PERIODE != 0L) {
			return;
		}
		LimbesWorldState etat = LimbesWorldState.get(server);
		expirer(server, etat, limbes);
		if (limbes.getPlayers().isEmpty()) {
			return;
		}
		if (etat.arriveesActives().size() >= fr.leslimbes.LimbesConfig.get().arriveesSimultanees) {
			return;
		}
		if (limbes.getTime() - etat.derniereArrivee() < INTERVALLE_MIN) {
			return;
		}
		ArriveeDefinition def = choisir(server, etat, limbes.getRandom());
		if (def == null) {
			return;
		}
		declencher(server, def, null);
	}

	@Nullable
	private static ArriveeDefinition choisir(MinecraftServer server, LimbesWorldState etat, Random random) {
		List<ArriveeDefinition> eligibles = new ArrayList<>();
		int total = 0;
		for (ArriveeDefinition def : ArriveeLoader.definitions().values()) {
			if (!eligible(server, etat, def)) {
				continue;
			}
			eligibles.add(def);
			total += def.poids();
		}
		if (eligibles.isEmpty()) {
			return null;
		}
		int r = random.nextInt(total);
		for (ArriveeDefinition def : eligibles) {
			r -= def.poids();
			if (r < 0) {
				return def;
			}
		}
		return eligibles.get(eligibles.size() - 1);
	}

	public static boolean eligible(MinecraftServer server, LimbesWorldState etat, ArriveeDefinition def) {
		if (def.unique() && etat.arriveeDejaDeclenchee(def.id())) {
			return false;
		}
		for (ArriveeEvenement e : etat.arriveesActives()) {
			if (e.definition.equals(def.id())) {
				return false;
			}
		}
		return Conditions.toutesMonde(server, def.prerequis());
	}

	/** Déclenche une Arrivée sur un site donné (ou le premier site libre du groupe). @return l'événement ou vide. */
	public static Optional<ArriveeEvenement> declencher(MinecraftServer server, ArriveeDefinition def, @Nullable String siteId) {
		ServerWorld limbes = LimbesDimension.monde(server);
		if (limbes == null) {
			return Optional.empty();
		}
		LimbesWorldState etat = LimbesWorldState.get(server);
		StructureTemplate template = server.getStructureTemplateManager().getTemplate(def.gabarit()).orElse(null);
		if (template == null) {
			LesLimbes.LOGGER.warn("Arrivée {} : gabarit introuvable {}", def.id(), def.gabarit());
			return Optional.empty();
		}
		List<ArriveeSite> candidats = new ArrayList<>();
		for (ArriveeSite site : ArriveeLoader.sites().values()) {
			if (siteId != null ? site.id().equals(siteId) : site.groupe().equals(def.groupeSites())) {
				candidats.add(site);
			}
		}
		java.util.Collections.shuffle(candidats, new java.util.Random(limbes.getRandom().nextLong()));
		for (ArriveeSite site : candidats) {
			if (etat.siteOccupe(site.id())) {
				continue;
			}
			BlockPos origine = site.pos().subtract(def.ancre());
			if (!zoneLibre(limbes, origine, template.getSize())) {
				LesLimbes.LOGGER.info("Arrivée {} : site {} occupé par une construction ou un joueur, ignoré.", def.id(), site.id());
				continue;
			}
			placer(limbes, template, origine);
			ArriveeEvenement evenement = new ArriveeEvenement(def.id() + "#" + etat.prochainNumeroArrivee(), def.id(), site.id(), site.pos(), limbes.getTime());
			etat.ajouterArrivee(evenement);
			etat.noterArrivee(def.id(), limbes.getTime());
			annoncer(limbes, def, site.pos());
			LesLimbes.LOGGER.info("Arrivée « {} » en {} (site {}).", def.titre(), site.pos().toShortString(), site.id());
			return Optional.of(evenement);
		}
		return Optional.empty();
	}

	private static void placer(ServerWorld world, StructureTemplate template, BlockPos origine) {
		// Les trouvailles de la génération (lanternes perdues) tombent en objets plutôt que d'être écrasées.
		Vec3i taille = template.getSize();
		for (BlockPos p : BlockPos.iterate(origine, origine.add(taille.getX() - 1, taille.getY() - 1, taille.getZ() - 1))) {
			if (world.getBlockState(p).isIn(LimbesTags.TROUVAILLES)) {
				world.breakBlock(p, true);
			}
		}
		StructurePlacementData placement = new StructurePlacementData().setIgnoreEntities(false)
				.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
		template.place(world, origine, origine, placement, world.getRandom(), 2);
		MarqueursDeStructure.appliquerTous(world, template, placement, origine, world.getRandom());
	}

	/** Zone libre : rien d'autre que du terrain naturel (sol, plantes, arbres, eau) et aucun joueur. */
	public static boolean zoneLibre(ServerWorld world, BlockPos origine, Vec3i taille) {
		return raisonOccupation(world, origine, taille) == null;
	}

	/** @return null si la zone est libre, sinon la raison (diagnostic et journal). */
	@Nullable
	public static String raisonOccupation(ServerWorld world, BlockPos origine, Vec3i taille) {
		BlockBox box = BlockBox.create(origine, origine.add(taille.getX() - 1, taille.getY() - 1, taille.getZ() - 1));
		if (!world.getPlayers(p -> p.getBoundingBox().intersects(Box.from(box).expand(3.0))).isEmpty()) {
			return "un joueur se trouve dans la zone";
		}
		for (BlockPos p : BlockPos.iterate(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ())) {
			if (!world.isChunkLoaded(p)) {
				return "chunk non chargé en " + p.toShortString();
			}
			BlockState s = world.getBlockState(p);
			if (!naturel(s)) {
				return "bloc non naturel " + s.getBlock().getName().getString() + " en " + p.toShortString();
			}
		}
		return null;
	}

	public static boolean naturel(BlockState s) {
		return s.isAir() || s.isReplaceable() || !s.getFluidState().isEmpty() || s.isIn(BlockTags.LEAVES) || s.isIn(BlockTags.LOGS)
				|| s.isIn(BlockTags.DIRT) || s.isIn(BlockTags.FLOWERS) || s.isIn(BlockTags.SAPLINGS) || s.isOf(LimbesBlocks.PIERRE_NACREE)
				|| s.isOf(LimbesBlocks.CRISTAL_DE_NACRE) || s.isOf(Blocks.STONE) || s.isOf(Blocks.GRAVEL) || s.isIn(BlockTags.SAND)
				|| s.isIn(LimbesTags.TERRAIN_NATUREL) || s.isIn(LimbesTags.TROUVAILLES)
				|| s.isOf(Blocks.BARRIER); // jamais naturel en jeu ; présent autour des zones de test
	}

	private static void annoncer(ServerWorld world, ArriveeDefinition def, BlockPos pos) {
		Vec3d centre = Vec3d.ofCenter(pos);
		for (ServerPlayerEntity p : world.getPlayers()) {
			double dx = centre.x - p.getX();
			double dz = centre.z - p.getZ();
			int distance = (int) Math.sqrt(dx * dx + dz * dz);
			String direction = fr.leslimbes.item.BoussoleNacreeItem.direction(dx, dz);
			p.sendMessage(Text.translatable("message.les_limbes.arrivee.annonce", def.annonce(), direction, distance).formatted(Formatting.LIGHT_PURPLE), false);
			if (distance < 400) {
				p.playSoundToPlayer(LimbesSounds.ARRIVEE, SoundCategory.AMBIENT, 1.0F, 1.0F);
			}
		}
		Abilities.effet(world, "arrivee_ciel", centre.add(0, 1, 0), Vec3d.ZERO, 0);
	}

	/** Résout l'Arrivée active la plus récente d'un type (actions de quête). */
	public static boolean resoudre(MinecraftServer server, String definitionId) {
		LimbesWorldState etat = LimbesWorldState.get(server);
		ServerWorld limbes = LimbesDimension.monde(server);
		for (ArriveeEvenement e : etat.arriveesActives()) {
			if (e.definition.equals(definitionId)) {
				e.etat = ArriveeEvenement.Etat.RESOLUE;
				e.fin = limbes == null ? 0 : limbes.getTime();
				etat.markDirty();
				return true;
			}
		}
		return false;
	}

	private static void expirer(MinecraftServer server, LimbesWorldState etat, ServerWorld limbes) {
		for (ArriveeEvenement e : etat.arriveesActives()) {
			ArriveeDefinition def = ArriveeLoader.get(e.definition);
			if (def == null || def.duree() <= 0 || limbes.getTime() - e.debut < def.duree()) {
				continue;
			}
			retirerStructure(server, limbes, def, e);
			e.etat = ArriveeEvenement.Etat.EXPIREE;
			e.fin = limbes.getTime();
			etat.markDirty();
		}
	}

	/** Retire les blocs posés par le gabarit (seulement s'ils sont encore ceux du gabarit). */
	private static void retirerStructure(MinecraftServer server, ServerWorld world, ArriveeDefinition def, ArriveeEvenement e) {
		StructureTemplate template = server.getStructureTemplateManager().getTemplate(def.gabarit()).orElse(null);
		if (template == null) {
			return;
		}
		BlockPos origine = e.pos.subtract(def.ancre());
		StructurePlacementData placement = new StructurePlacementData();
		for (StructureTemplate.StructureBlockInfo info : template.getInfosForBlock(origine, placement, Blocks.CHEST)) {
			world.removeBlock(info.pos(), false);
		}
		Vec3i taille = template.getSize();
		for (BlockPos p : BlockPos.iterate(origine, origine.add(taille.getX() - 1, taille.getY() - 1, taille.getZ() - 1))) {
			BlockState s = world.getBlockState(p);
			if (!s.isAir() && !naturel(s)) {
				world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
			}
		}
	}

	/** Texte pour les dialogues : où en sont les Arrivées, vues depuis la position du joueur. */
	public static String resumePour(ServerPlayerEntity player) {
		LimbesWorldState etat = LimbesWorldState.get(player.server);
		List<ArriveeEvenement> actives = etat.arriveesActives();
		if (actives.isEmpty()) {
			return Text.translatable("arrivee.les_limbes.calme").getString();
		}
		StringBuilder sb = new StringBuilder();
		for (ArriveeEvenement e : actives) {
			ArriveeDefinition def = ArriveeLoader.get(e.definition);
			double dx = e.pos.getX() + 0.5 - player.getX();
			double dz = e.pos.getZ() + 0.5 - player.getZ();
			int distance = (int) Math.sqrt(dx * dx + dz * dz);
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(Text.translatable("arrivee.les_limbes.vue", def == null ? e.definition : def.titre(),
					fr.leslimbes.item.BoussoleNacreeItem.direction(dx, dz), distance).getString());
		}
		return sb.toString();
	}

	@Nullable
	public static ArriveeEvenement plusProche(MinecraftServer server, BlockPos depuis, @Nullable String definitionId) {
		ArriveeEvenement meilleure = null;
		double best = Double.MAX_VALUE;
		for (ArriveeEvenement e : LimbesWorldState.get(server).arriveesActives()) {
			if (definitionId != null && !e.definition.equals(definitionId)) {
				continue;
			}
			double d = e.pos.getSquaredDistance(depuis);
			if (d < best) {
				best = d;
				meilleure = e;
			}
		}
		return meilleure;
	}

	private ArriveeManager() {
	}
}
