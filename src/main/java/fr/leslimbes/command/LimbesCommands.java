package fr.leslimbes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.leslimbes.block.PorteDesLimbesBlock;
import fr.leslimbes.npc.EssentialNpcs;
import fr.leslimbes.npc.dialogue.DialogueLoader;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.quest.QuestLoader;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestProgress;
import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.dimension.LimbesSites;
import fr.leslimbes.world.state.LimbesWorldState;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;

/**
 * Commandes d'administration et de diagnostic : /limbes ... (niveau 2). Elles servent au développement
 * et à la réparation ; elles ne remplacent jamais les interactions de survie.
 */
public final class LimbesCommands {
	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("limbes").requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.literal("tp")
						.then(CommandManager.literal("sanctuaire").executes(ctx -> tp(ctx, LimbesDimension.MONDE, LimbesSites.ARRIVEE)))
						.then(CommandManager.literal("clairval").executes(ctx -> tp(ctx, LimbesDimension.MONDE, Vec3d.ofBottomCenter(LimbesSites.CLAIRVAL_ARRIVEE))))
						.then(CommandManager.literal("manoir").executes(ctx -> tp(ctx, LimbesDimension.MONDE, Vec3d.ofBottomCenter(LimbesSites.MANOIR.add(0, 0, 10)))))
						.then(CommandManager.literal("relais").executes(ctx -> tp(ctx, LimbesDimension.MONDE, Vec3d.ofBottomCenter(LimbesSites.RELAIS_ROYAL.add(0, 0, 6)))))
						.then(CommandManager.literal("overworld").executes(ctx -> tpOverworld(ctx))))
				.then(CommandManager.literal("quete")
						.then(CommandManager.literal("liste").executes(LimbesCommands::listerQuetes))
						.then(CommandManager.literal("demarrer").then(CommandManager.argument("id", StringArgumentType.word())
								.executes(ctx -> {
									ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
									boolean ok = QuestManager.demarrer(p, StringArgumentType.getString(ctx, "id"));
									ctx.getSource().sendFeedback(() -> Text.literal(ok ? "Quête démarrée." : "Quête inconnue ou déjà commencée."), false);
									return ok ? 1 : 0;
								})))
						.then(CommandManager.literal("etape").then(CommandManager.argument("id", StringArgumentType.word())
								.then(CommandManager.argument("etape", IntegerArgumentType.integer(0)).executes(ctx -> {
									ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
									boolean ok = QuestManager.allerEtape(p, StringArgumentType.getString(ctx, "id"), IntegerArgumentType.getInteger(ctx, "etape"));
									ctx.getSource().sendFeedback(() -> Text.literal(ok ? "Étape forcée." : "Quête inconnue."), false);
									return ok ? 1 : 0;
								}))))
						.then(CommandManager.literal("reinit").then(CommandManager.argument("id", StringArgumentType.word()).executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							QuestManager.reinitialiser(p, StringArgumentType.getString(ctx, "id"));
							ctx.getSource().sendFeedback(() -> Text.literal("Quête réinitialisée."), false);
							return 1;
						}))))
				.then(CommandManager.literal("pouvoir")
						.then(CommandManager.literal("tout").executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							PlayerLimbesData d = PlayerLimbesData.of(p);
							Abilities.tous().keySet().forEach(d::debloquerPouvoir);
							PlayerDataSync.envoyer(p);
							ctx.getSource().sendFeedback(() -> Text.literal("Tous les pouvoirs débloqués."), false);
							return 1;
						}))
						.then(CommandManager.argument("id", StringArgumentType.word()).executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							String id = StringArgumentType.getString(ctx, "id");
							if (Abilities.get(id) == null) {
								ctx.getSource().sendError(Text.literal("Pouvoir inconnu : " + id));
								return 0;
							}
							PlayerLimbesData.of(p).debloquerPouvoir(id);
							PlayerDataSync.envoyer(p);
							ctx.getSource().sendFeedback(() -> Text.literal("Pouvoir débloqué : " + id), false);
							return 1;
						})))
				.then(CommandManager.literal("majeste").then(CommandManager.argument("valeur", FloatArgumentType.floatArg(0)).executes(ctx -> {
					ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
					PlayerLimbesData d = PlayerLimbesData.of(p);
					d.majesteMax = Math.max(d.majesteMax, FloatArgumentType.getFloat(ctx, "valeur"));
					d.majeste = FloatArgumentType.getFloat(ctx, "valeur");
					PlayerDataSync.envoyer(p);
					return 1;
				})))
				.then(CommandManager.literal("drapeau")
						.then(CommandManager.literal("poser").then(CommandManager.argument("nom", StringArgumentType.word()).executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							PlayerLimbesData.of(p).poserDrapeau(StringArgumentType.getString(ctx, "nom"));
							PlayerDataSync.envoyer(p);
							return 1;
						})))
						.then(CommandManager.literal("retirer").then(CommandManager.argument("nom", StringArgumentType.word()).executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							PlayerLimbesData.of(p).drapeaux.remove(StringArgumentType.getString(ctx, "nom"));
							PlayerDataSync.envoyer(p);
							return 1;
						}))))
				.then(CommandManager.literal("pnj").then(CommandManager.literal("reapparaitre").then(CommandManager.argument("role", StringArgumentType.word()).executes(ctx -> {
					ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
					String role = StringArgumentType.getString(ctx, "role");
					Optional<BlockPos> anc = LimbesWorldState.get(p.server).ancragePnj(role);
					BlockPos pos = anc.orElse(p.getBlockPos());
					EssentialNpcs.reapparaitre(p.getServerWorld(), role, pos);
					ctx.getSource().sendFeedback(() -> Text.literal("Personnage « " + role + " » apparu en " + pos.toShortString()), false);
					return 1;
				}))))
				.then(CommandManager.literal("porte").then(CommandManager.literal("reveiller").executes(ctx -> {
					ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
					HitResult hit = p.raycast(8.0, 0.0F, false);
					if (hit instanceof BlockHitResult bhr && p.getServerWorld().getBlockState(bhr.getBlockPos()).getBlock() instanceof PorteDesLimbesBlock) {
						int n = PorteDesLimbesBlock.reveiller(p.getServerWorld(), bhr.getBlockPos(), p).size();
						ctx.getSource().sendFeedback(() -> Text.literal("Voile réveillé (" + n + " blocs)."), false);
						return 1;
					}
					ctx.getSource().sendError(Text.literal("Visez un bloc de Porte des Limbes."));
					return 0;
				})))
				.then(CommandManager.literal("structure").then(CommandManager.argument("id", IdentifierArgumentType.identifier())
						.executes(ctx -> placerStructure(ctx, null))
						.then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(ctx -> placerStructure(ctx, BlockPosArgumentType.getLoadedBlockPos(ctx, "pos"))))))
				.then(CommandManager.literal("arrivee")
						.then(CommandManager.literal("liste").executes(LimbesCommands::listerArrivees))
						.then(CommandManager.literal("declencher").then(CommandManager.argument("id", StringArgumentType.word()).executes(ctx -> {
							String id = StringArgumentType.getString(ctx, "id");
							fr.leslimbes.arrivee.ArriveeDefinition def = fr.leslimbes.arrivee.ArriveeLoader.get(id);
							if (def == null) {
								ctx.getSource().sendError(Text.literal("Arrivée inconnue : " + id + " (" + String.join(", ", fr.leslimbes.arrivee.ArriveeLoader.definitions().keySet()) + ")"));
								return 0;
							}
							var e = fr.leslimbes.arrivee.ArriveeManager.declencher(ctx.getSource().getServer(), def, null);
							if (e.isEmpty()) {
								ctx.getSource().sendError(Text.literal("Aucun site libre ou vérifié pour cette Arrivée."));
								return 0;
							}
							ctx.getSource().sendFeedback(() -> Text.literal("Arrivée « " + def.titre() + " » en " + e.get().pos.toShortString()), false);
							return 1;
						})))
						.then(CommandManager.literal("resoudre").then(CommandManager.argument("id", StringArgumentType.word()).executes(ctx -> {
							boolean ok = fr.leslimbes.arrivee.ArriveeManager.resoudre(ctx.getSource().getServer(), StringArgumentType.getString(ctx, "id"));
							ctx.getSource().sendFeedback(() -> Text.literal(ok ? "Arrivée résolue." : "Aucune Arrivée active de ce type."), false);
							return ok ? 1 : 0;
						}))))
				.then(CommandManager.literal("projet")
						.then(CommandManager.literal("liste").executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							for (fr.leslimbes.domaine.ProjetDefinition def : fr.leslimbes.domaine.DomaineLoader.projets().values()) {
								fr.leslimbes.domaine.ProjetManager.Etat etat = fr.leslimbes.domaine.ProjetManager.etat(p, def);
								ctx.getSource().sendFeedback(() -> Text.literal(def.id() + " — " + def.titre() + " [" + etat + "] origine " + def.origine().toShortString()), false);
							}
							return fr.leslimbes.domaine.DomaineLoader.projets().size();
						}))
						.then(CommandManager.literal("construire").then(CommandManager.argument("id", StringArgumentType.word()).executes(ctx -> construireProjet(ctx, false))
								.then(CommandManager.literal("ignorer").executes(ctx -> construireProjet(ctx, true)))))
						.then(CommandManager.literal("conflits").then(CommandManager.argument("id", StringArgumentType.word()).executes(ctx -> {
							fr.leslimbes.domaine.ProjetDefinition def = fr.leslimbes.domaine.DomaineLoader.projet(StringArgumentType.getString(ctx, "id"));
							ServerWorld monde = LimbesDimension.monde(ctx.getSource().getServer());
							if (def == null || monde == null) {
								ctx.getSource().sendError(Text.literal("Projet inconnu."));
								return 0;
							}
							fr.leslimbes.domaine.ProjetManager.Conflits c = fr.leslimbes.domaine.ProjetManager.conflits(ctx.getSource().getServer(), monde, def);
							ctx.getSource().sendFeedback(() -> Text.literal(c.nombre() + " bloc(s) en conflit" + (c.vide() ? "" : " (ex. " + c.exemple() + ")")), false);
							return c.nombre();
						}))))
				.then(CommandManager.literal("relais")
						.then(CommandManager.literal("decouvrir").then(CommandManager.argument("id", StringArgumentType.word()).executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							boolean ok = fr.leslimbes.domaine.RelaisManager.decouvrir(p, StringArgumentType.getString(ctx, "id"), true);
							ctx.getSource().sendFeedback(() -> Text.literal(ok ? "Relais découvert." : "Relais inconnu ou déjà découvert."), false);
							return ok ? 1 : 0;
						})))
						.then(CommandManager.literal("tout").executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							int n = 0;
							for (String id : fr.leslimbes.domaine.DomaineLoader.relais().keySet()) {
								n += fr.leslimbes.domaine.RelaisManager.decouvrir(p, id, false) ? 1 : 0;
							}
							int total = n;
							ctx.getSource().sendFeedback(() -> Text.literal(total + " relais découvert(s)."), false);
							return n;
						})))
				.then(CommandManager.literal("lieu")
						.then(CommandManager.literal("tout").executes(ctx -> {
							ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
							int n = 0;
							for (fr.leslimbes.domaine.LieuDefinition lieu : fr.leslimbes.domaine.DomaineLoader.lieux().values()) {
								n += fr.leslimbes.domaine.LieuxManager.decouvrir(p, lieu, false) ? 1 : 0;
							}
							int total = n;
							ctx.getSource().sendFeedback(() -> Text.literal(total + " lieu(x) découvert(s)."), false);
							return n;
						})))
				.then(CommandManager.literal("diag").executes(LimbesCommands::diag)));
	}

	private static int construireProjet(CommandContext<ServerCommandSource> ctx, boolean ignorer) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
		fr.leslimbes.domaine.ProjetDefinition def = fr.leslimbes.domaine.DomaineLoader.projet(StringArgumentType.getString(ctx, "id"));
		if (def == null) {
			ctx.getSource().sendError(Text.literal("Projet inconnu : " + String.join(", ", fr.leslimbes.domaine.DomaineLoader.projets().keySet())));
			return 0;
		}
		boolean ok = fr.leslimbes.domaine.ProjetManager.valider(p, def, ignorer);
		ctx.getSource().sendFeedback(() -> Text.literal(ok ? "Projet construit." : "Projet non construit (voir le message)."), false);
		return ok ? 1 : 0;
	}

	private static int tp(CommandContext<ServerCommandSource> ctx, net.minecraft.registry.RegistryKey<World> monde, Vec3d pos) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
		ServerWorld world = p.server.getWorld(monde);
		if (world == null) {
			ctx.getSource().sendError(Text.literal("Dimension indisponible : " + monde.getValue()));
			return 0;
		}
		p.teleportTo(new TeleportTarget(world, pos, Vec3d.ZERO, p.getYaw(), p.getPitch(), TeleportTarget.NO_OP));
		return 1;
	}

	private static int tpOverworld(CommandContext<ServerCommandSource> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
		ServerWorld world = p.server.getWorld(World.OVERWORLD);
		BlockPos spawn = world.getSpawnPos();
		return tp(ctx, World.OVERWORLD, new Vec3d(spawn.getX() + 0.5, world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ()), spawn.getZ() + 0.5));
	}

	private static int listerQuetes(CommandContext<ServerCommandSource> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
		PlayerLimbesData d = PlayerLimbesData.of(p);
		StringBuilder sb = new StringBuilder("Quêtes connues : " + QuestLoader.toutes().size() + "\n");
		for (String id : QuestLoader.toutes().keySet()) {
			QuestProgress prog = d.quetes.get(id);
			String etat = prog == null ? "non commencée" : prog.terminee ? "terminée" : "étape " + prog.etape + " faits=" + prog.faits + " compteurs=" + prog.compteurs;
			sb.append("- ").append(id).append(" : ").append(etat).append('\n');
		}
		ctx.getSource().sendFeedback(() -> Text.literal(sb.toString().trim()), false);
		return 1;
	}

	private static int placerStructure(CommandContext<ServerCommandSource> ctx, BlockPos pos) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
		Identifier id = IdentifierArgumentType.getIdentifier(ctx, "id");
		ServerWorld world = p.getServerWorld();
		Optional<StructureTemplate> template = world.getStructureTemplateManager().getTemplate(id);
		if (template.isEmpty()) {
			ctx.getSource().sendError(Text.literal("Gabarit introuvable : " + id));
			return 0;
		}
		BlockPos origine = pos != null ? pos : p.getBlockPos().add(2, 0, 2);
		template.get().place(world, origine, origine, new StructurePlacementData().setIgnoreEntities(false), world.getRandom(), 2);
		ctx.getSource().sendFeedback(() -> Text.literal("Gabarit " + id + " placé en " + origine.toShortString() + " (taille " + template.get().getSize().toShortString() + ")"), false);
		return 1;
	}

	private static int listerArrivees(CommandContext<ServerCommandSource> ctx) {
		LimbesWorldState etat = LimbesWorldState.get(ctx.getSource().getServer());
		var toutes = etat.toutesArrivees();
		ctx.getSource().sendFeedback(() -> Text.literal("Types : " + String.join(", ", fr.leslimbes.arrivee.ArriveeLoader.definitions().keySet())
				+ " ; sites : " + String.join(", ", fr.leslimbes.arrivee.ArriveeLoader.sites().keySet())), false);
		if (toutes.isEmpty()) {
			ctx.getSource().sendFeedback(() -> Text.literal("Aucune Arrivée survenue."), false);
		}
		for (var e : toutes) {
			ctx.getSource().sendFeedback(() -> Text.literal(e.instance + " : " + e.etat + " en " + e.pos.toShortString() + " (site " + e.site + ")"), false);
		}
		return toutes.size();
	}

	private static int diag(CommandContext<ServerCommandSource> ctx) {
		ServerCommandSource src = ctx.getSource();
		ServerWorld limbes = LimbesDimension.monde(src.getServer());
		StringBuilder sb = new StringBuilder();
		sb.append("Dimension des Limbes : ").append(limbes == null ? "ABSENTE" : "chargée (" + limbes.getChunkManager().getLoadedChunkCount() + " chunks)").append('\n');
		sb.append("Quêtes chargées : ").append(QuestLoader.toutes().size()).append(" ; dialogues : ").append(DialogueLoader.tous().size()).append('\n');
		sb.append("Pouvoirs enregistrés : ").append(String.join(", ", Abilities.tous().keySet())).append('\n');
		LimbesWorldState state = LimbesWorldState.get(src.getServer());
		sb.append("Ancrages PNJ : ");
		for (Map.Entry<String, BlockPos> e : state.ancrages().entrySet()) {
			sb.append(e.getKey()).append('@').append(e.getValue().toShortString()).append(' ');
		}
		sb.append('\n');
		if (src.getEntity() instanceof ServerPlayerEntity p) {
			PlayerLimbesData d = PlayerLimbesData.of(p);
			sb.append("Joueur : majesté ").append((int) d.majeste).append('/').append((int) d.majesteMax)
					.append(" ; pouvoirs ").append(d.pouvoirs).append(" ; équipés ").append(d.equipes)
					.append(" ; drapeaux ").append(d.drapeaux).append(" ; quête suivie ").append(d.queteSuivie)
					.append(" ; retour ").append(d.retourOverworld.map(gp -> gp.pos().toShortString()).orElse("aucun"));
		}
		src.sendFeedback(() -> Text.literal(sb.toString().trim()), false);
		return 1;
	}

	private LimbesCommands() {
	}
}
