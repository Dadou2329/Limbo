package fr.leslimbes.domaine;

import com.mojang.serialization.MapCodec;
import fr.leslimbes.LesLimbes;
import fr.leslimbes.arrivee.ArriveeManager;
import fr.leslimbes.network.LimbesPayloads;
import fr.leslimbes.npc.dialogue.Actions;
import fr.leslimbes.npc.dialogue.Conditions;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.state.LimbesWorldState;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Projets du domaine : aperçu de l'emprise, détection des constructions du joueur dans la zone,
 * validation (prérequis, ressources), pose du gabarit en respectant les blocs occupés si demandé,
 * préservation des inventaires, drapeau du monde puis actions de suite.
 *
 * <p>Tout est décidé ici, côté serveur ; l'écran de chantier n'envoie qu'un identifiant et un booléen.</p>
 */
public final class ProjetManager {
	public enum Etat { DISPONIBLE, VERROUILLE, FAIT }

	/** Constructions étrangères au gabarit d'origine trouvées dans l'emprise. */
	public record Conflits(List<BlockPos> positions, String exemple) {
		public int nombre() {
			return positions.size();
		}

		public boolean vide() {
			return positions.isEmpty();
		}
	}

	private record ChantierOuvert(String lieu, long tick) {
	}

	private static final long VALIDITE_CHANTIER = 20L * 60L * 20L;
	private static final double DISTANCE_MAX = 160.0;
	private static final Map<UUID, ChantierOuvert> CHANTIERS = new HashMap<>();

	/** Processeur qui laisse intacts les blocs occupés par le joueur (positions absolues). */
	public static final class ProcesseurDeConflits extends StructureProcessor {
		public static final StructureProcessorType<ProcesseurDeConflits> TYPE = Registry.register(Registries.STRUCTURE_PROCESSOR,
				LesLimbes.id("conflits"), () -> MapCodec.unit(() -> new ProcesseurDeConflits(Set.of())));
		private final Set<BlockPos> ignorees;

		public ProcesseurDeConflits(Set<BlockPos> ignorees) {
			this.ignorees = ignorees;
		}

		@Nullable
		@Override
		public StructureTemplate.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, StructureTemplate.StructureBlockInfo original,
															StructureTemplate.StructureBlockInfo current, StructurePlacementData data) {
			return ignorees.contains(current.pos()) ? null : current;
		}

		@Override
		protected StructureProcessorType<?> getType() {
			return TYPE;
		}
	}

	public static void init() {
		// Force l'enregistrement du type de processeur.
		Object ignore = ProcesseurDeConflits.TYPE;
	}

	// --- état --------------------------------------------------------------------------------
	public static Etat etat(ServerPlayerEntity player, ProjetDefinition def) {
		if (LimbesWorldState.get(player.server).aDrapeau(def.drapeauMonde())) {
			return Etat.FAIT;
		}
		return Conditions.toutes(player, def.prerequis()) ? Etat.DISPONIBLE : Etat.VERROUILLE;
	}

	public static int compter(ServerPlayerEntity player, Identifier item) {
		int n = 0;
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack s = player.getInventory().getStack(i);
			if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).equals(item)) {
				n += s.getCount();
			}
		}
		return n;
	}

	public static boolean possedeCouts(ServerPlayerEntity player, ProjetDefinition def) {
		if (player.isCreative()) {
			return true;
		}
		for (ProjetDefinition.Cout c : def.cout()) {
			if (compter(player, c.objet()) < c.nombre()) {
				return false;
			}
		}
		return true;
	}

	private static void consommer(ServerPlayerEntity player, ProjetDefinition def) {
		if (player.isCreative()) {
			return;
		}
		for (ProjetDefinition.Cout c : def.cout()) {
			int reste = c.nombre();
			for (int i = 0; i < player.getInventory().size() && reste > 0; i++) {
				ItemStack s = player.getInventory().getStack(i);
				if (!s.isEmpty() && Registries.ITEM.getId(s.getItem()).equals(c.objet())) {
					int pris = Math.min(reste, s.getCount());
					s.decrement(pris);
					reste -= pris;
				}
			}
		}
	}

	// --- conflits ----------------------------------------------------------------------------
	/**
	 * Blocs de l'emprise qui ne sont ni du terrain naturel ni des blocs du gabarit antérieur ({@code avant}) :
	 * ce sont des constructions de quelqu'un, qu'on ne doit jamais écraser sans accord.
	 */
	public static Conflits conflits(MinecraftServer server, ServerWorld world, ProjetDefinition def) {
		StructureTemplate template = server.getStructureTemplateManager().getTemplate(def.gabarit()).orElse(null);
		if (template == null) {
			return new Conflits(List.of(), "");
		}
		Vec3i taille = template.getSize();
		BlockPos o = def.origine();
		StructureTemplate avant = def.avant().flatMap(id -> server.getStructureTemplateManager().getTemplate(id)).orElse(null);
		Map<Block, Set<BlockPos>> originaux = new HashMap<>();
		List<BlockPos> conflits = new ArrayList<>();
		String exemple = "";
		for (BlockPos p : BlockPos.iterate(o, o.add(taille.getX() - 1, taille.getY() - 1, taille.getZ() - 1))) {
			BlockState s = world.getBlockState(p);
			if (ArriveeManager.naturel(s)) {
				continue;
			}
			if (avant != null) {
				Set<BlockPos> positions = originaux.computeIfAbsent(s.getBlock(), b -> {
					Set<BlockPos> set = new HashSet<>();
					for (StructureTemplate.StructureBlockInfo info : avant.getInfosForBlock(def.origineAvant(), new StructurePlacementData(), b)) {
						set.add(info.pos());
					}
					return set;
				});
				if (positions.contains(p)) {
					continue;
				}
			}
			conflits.add(p.toImmutable());
			if (exemple.isEmpty()) {
				exemple = s.getBlock().getName().getString() + " en " + p.toShortString();
			}
		}
		return new Conflits(List.copyOf(conflits), exemple);
	}

	// --- écran de chantier -------------------------------------------------------------------
	/** Ouvre l'écran des projets d'un lieu (depuis un dialogue). */
	public static void ouvrirChantier(ServerPlayerEntity player, String lieu) {
		CHANTIERS.put(player.getUuid(), new ChantierOuvert(lieu, player.server.getOverworld().getTime()));
		envoyerChantier(player, lieu);
	}

	public static void fermerChantier(ServerPlayerEntity player) {
		CHANTIERS.remove(player.getUuid());
	}

	public static void onDeconnexion(UUID uuid) {
		CHANTIERS.remove(uuid);
	}

	private static void envoyerChantier(ServerPlayerEntity player, String lieu) {
		if (!ServerPlayNetworking.canSend(player, LimbesPayloads.OuvrirChantier.ID)) {
			return;
		}
		ServerWorld monde = LimbesDimension.monde(player.server);
		NbtCompound contenu = new NbtCompound();
		contenu.putString("lieu", lieu);
		NbtList projets = new NbtList();
		for (ProjetDefinition def : DomaineLoader.projets(lieu)) {
			NbtCompound p = new NbtCompound();
			p.putString("id", def.id());
			p.putString("titre", def.titre());
			p.putString("description", def.description());
			p.putString("indice", def.indice());
			Etat etat = etat(player, def);
			p.putString("etat", etat.name().toLowerCase());
			int[] min = {def.origine().getX(), def.origine().getY(), def.origine().getZ()};
			p.putIntArray("origine", min);
			if (etat == Etat.DISPONIBLE && monde != null) {
				Conflits c = conflits(player.server, monde, def);
				p.putInt("conflits", c.nombre());
				p.putString("exemple", c.exemple());
			}
			NbtList couts = new NbtList();
			for (ProjetDefinition.Cout c : def.cout()) {
				NbtCompound cc = new NbtCompound();
				cc.putString("objet", c.objet().toString());
				cc.putInt("nombre", c.nombre());
				cc.putInt("possede", compter(player, c.objet()));
				couts.add(cc);
			}
			p.put("cout", couts);
			projets.add(p);
		}
		contenu.put("projets", projets);
		ServerPlayNetworking.send(player, new LimbesPayloads.OuvrirChantier(contenu));
	}

	@Nullable
	private static ProjetDefinition projetAutorise(ServerPlayerEntity player, String id) {
		ChantierOuvert ouvert = CHANTIERS.get(player.getUuid());
		ProjetDefinition def = DomaineLoader.projet(id);
		if (def == null) {
			return null;
		}
		if (ouvert == null || !ouvert.lieu().equals(def.lieu()) || player.server.getOverworld().getTime() - ouvert.tick() > VALIDITE_CHANTIER) {
			if (!player.hasPermissionLevel(2)) {
				return null;
			}
		}
		ServerWorld monde = LimbesDimension.monde(player.server);
		if (monde == null || player.getServerWorld() != monde || !player.getBlockPos().isWithinDistance(def.origine(), DISTANCE_MAX)) {
			player.sendMessage(Text.translatable("message.les_limbes.projet.loin", def.titre()).formatted(Formatting.RED), false);
			return null;
		}
		return def;
	}

	/** Montre l'emprise au joueur (contour pendant dix secondes) avec les blocs en conflit. */
	public static void apercu(ServerPlayerEntity player, String id) {
		ProjetDefinition def = projetAutorise(player, id);
		if (def == null) {
			return;
		}
		StructureTemplate template = player.server.getStructureTemplateManager().getTemplate(def.gabarit()).orElse(null);
		if (template == null || !ServerPlayNetworking.canSend(player, LimbesPayloads.ApercuProjet.ID)) {
			return;
		}
		Vec3i taille = template.getSize();
		Conflits c = conflits(player.server, player.getServerWorld(), def);
		NbtCompound nbt = new NbtCompound();
		nbt.putString("titre", def.titre());
		nbt.putIntArray("min", new int[] {def.origine().getX(), def.origine().getY(), def.origine().getZ()});
		nbt.putIntArray("max", new int[] {def.origine().getX() + taille.getX(), def.origine().getY() + taille.getY(), def.origine().getZ() + taille.getZ()});
		NbtList conflits = new NbtList();
		for (int i = 0; i < Math.min(64, c.positions().size()); i++) {
			BlockPos p = c.positions().get(i);
			conflits.add(new net.minecraft.nbt.NbtIntArray(new int[] {p.getX(), p.getY(), p.getZ()}));
		}
		nbt.put("conflits", conflits);
		ServerPlayNetworking.send(player, new LimbesPayloads.ApercuProjet(nbt));
		player.sendMessage(Text.translatable("message.les_limbes.projet.apercu", def.titre()).formatted(Formatting.AQUA), true);
	}

	/** Demande de lancement depuis l'écran (vérifications complètes côté serveur). */
	public static boolean lancer(ServerPlayerEntity player, String id, boolean ignorerOccupe) {
		ProjetDefinition def = projetAutorise(player, id);
		if (def == null) {
			return false;
		}
		boolean ok = valider(player, def, ignorerOccupe);
		ChantierOuvert ouvert = CHANTIERS.get(player.getUuid());
		if (ouvert != null) {
			envoyerChantier(player, ouvert.lieu());
		}
		return ok;
	}

	/**
	 * Valide et construit un projet : prérequis, ressources, conflits (sauf si {@code ignorerOccupe}),
	 * pose du gabarit, inventaires préservés, drapeau du monde, actions de suite.
	 */
	public static boolean valider(ServerPlayerEntity player, ProjetDefinition def, boolean ignorerOccupe) {
		MinecraftServer server = player.server;
		ServerWorld monde = LimbesDimension.monde(server);
		if (monde == null) {
			return false;
		}
		Etat etat = etat(player, def);
		if (etat == Etat.FAIT) {
			player.sendMessage(Text.translatable("message.les_limbes.projet.deja", def.titre()).formatted(Formatting.GRAY), false);
			return false;
		}
		if (etat == Etat.VERROUILLE) {
			player.sendMessage(Text.translatable("message.les_limbes.projet.verrouille", def.titre()).formatted(Formatting.RED), false);
			return false;
		}
		StructureTemplate template = server.getStructureTemplateManager().getTemplate(def.gabarit()).orElse(null);
		if (template == null) {
			LesLimbes.LOGGER.error("Projet {} : gabarit introuvable {}", def.id(), def.gabarit());
			return false;
		}
		if (!possedeCouts(player, def)) {
			player.sendMessage(Text.translatable("message.les_limbes.projet.ressources", def.titre()).formatted(Formatting.RED), false);
			return false;
		}
		Conflits conflits = conflits(server, monde, def);
		if (!conflits.vide() && !ignorerOccupe) {
			player.sendMessage(Text.translatable("message.les_limbes.projet.conflit", def.titre(), conflits.nombre(), conflits.exemple()).formatted(Formatting.YELLOW), false);
			return false;
		}
		consommer(player, def);
		construire(server, monde, def, template, new HashSet<>(conflits.positions()));
		LimbesWorldState.get(server).poserDrapeau(def.drapeauMonde());
		Actions.executer(player, def.apres());
		QuestManager.signaler(player, QuestSignal.drapeau(def.drapeauMonde()));
		player.sendMessage(Text.translatable("message.les_limbes.projet.fait", def.titre()).formatted(Formatting.GOLD), false);
		Vec3i taille = template.getSize();
		Vec3d centre = Vec3d.ofCenter(def.origine()).add(taille.getX() / 2.0, taille.getY() / 2.0, taille.getZ() / 2.0);
		monde.playSound(null, BlockPos.ofFloored(centre), LimbesSounds.QUETE_TERMINEE, SoundCategory.BLOCKS, 1.0F, 0.9F);
		Abilities.effet(monde, "chantier", centre, new Vec3d(taille.getX(), taille.getY(), taille.getZ()), 0);
		PlayerDataSync.envoyer(player);
		SyncLieux.envoyerATous(server);
		LesLimbes.LOGGER.info("Projet « {} » construit par {} ({} bloc(s) occupé(s) laissé(s) en place).", def.titre(), player.getName().getString(), conflits.nombre());
		return true;
	}

	/** Pose le gabarit en laissant intactes les positions {@code ignorees} ; les inventaires remplacés sont rendus. */
	public static void construire(MinecraftServer server, ServerWorld world, ProjetDefinition def, StructureTemplate template, Set<BlockPos> ignorees) {
		Vec3i taille = template.getSize();
		BlockPos o = def.origine();
		// Instantané des inventaires présents dans l'emprise (hors blocs épargnés), puis vidage : vanilla
		// disperse lui-même le contenu d'un conteneur remplacé, sauf quand le gabarit porte des données de
		// bloc-entité à la même position (le conteneur est alors vidé avant d'être remplacé). On reprend la
		// main pour rendre le contenu dans tous les cas, sans jamais le dupliquer.
		Map<BlockPos, List<ItemStack>> contenus = new HashMap<>();
		for (BlockPos p : BlockPos.iterate(o, o.add(taille.getX() - 1, taille.getY() - 1, taille.getZ() - 1))) {
			if (ignorees.contains(p)) {
				continue;
			}
			BlockEntity be = world.getBlockEntity(p);
			if (be instanceof Inventory inv) {
				List<ItemStack> stacks = new ArrayList<>();
				for (int i = 0; i < inv.size(); i++) {
					if (!inv.getStack(i).isEmpty()) {
						stacks.add(inv.getStack(i).copy());
					}
				}
				if (!stacks.isEmpty()) {
					contenus.put(p.toImmutable(), stacks);
					inv.clear();
					inv.markDirty();
				}
			}
		}
		StructurePlacementData placement = new StructurePlacementData().setIgnoreEntities(false)
				.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
		if (!ignorees.isEmpty()) {
			placement.addProcessor(new ProcesseurDeConflits(ignorees));
		}
		template.place(world, o, o, placement, world.getRandom(), Block.NOTIFY_LISTENERS);
		fr.leslimbes.world.structure.MarqueursDeStructure.appliquerTous(world, template, placement, o, world.getRandom());
		fr.leslimbes.block.DalleDeRoseeBlock.planifierDansBoite(world, o, taille);
		// Contenus rendus : dans le conteneur qui occupe désormais la position, sinon au sol.
		for (Map.Entry<BlockPos, List<ItemStack>> entry : contenus.entrySet()) {
			BlockPos p = entry.getKey();
			List<ItemStack> restants = new ArrayList<>(entry.getValue());
			if (world.getBlockEntity(p) instanceof Inventory inv) {
				for (int i = 0; i < inv.size() && !restants.isEmpty(); i++) {
					if (inv.getStack(i).isEmpty()) {
						inv.setStack(i, restants.remove(0));
					}
				}
				inv.markDirty();
			}
			for (ItemStack s : restants) {
				ItemScatterer.spawn(world, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, s);
			}
		}
	}

	private ProjetManager() {
	}
}
