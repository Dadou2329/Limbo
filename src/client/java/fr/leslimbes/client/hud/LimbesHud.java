package fr.leslimbes.client.hud;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.client.ClientLimbesData;
import fr.leslimbes.client.input.LimbesKeys;
import fr.leslimbes.entity.RaieCelesteEntity;
import fr.leslimbes.item.BoussoleNacreeItem;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.quest.QuestDefinition;
import fr.leslimbes.quest.QuestLoader;
import fr.leslimbes.quest.QuestProgress;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * Interface en jeu : réserve de majesté et emplacements de pouvoirs (en bas à gauche), suivi de la
 * quête active (en haut à droite) avec distance et direction du lieu indiqué.
 */
public final class LimbesHud {
	private static final Identifier MAJESTE = LesLimbes.id("textures/gui/majeste.png");
	private static final int OR = 0xFFE3C97E;
	private static final int OR_SOMBRE = 0xFFB4934A;
	private static final int BLANC = 0xFFF4F1EA;
	private static final int GRIS = 0xFFB8B4B0;
	private static final int FOND = 0x90141A33;

	public static void init() {
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.options.hudHidden) {
				return;
			}
			PlayerLimbesData data = ClientLimbesData.get();
			if (!data.pouvoirs.isEmpty() || data.aDrapeau("arrivee")) {
				dessinerMajeste(context, client, data);
			}
			dessinerSuiviQuete(context, client, data);
			if (client.player.getVehicle() instanceof RaieCelesteEntity raie) {
				dessinerEndurance(context, client, raie);
			}
			dessinerVitrine(context, client);
			dessinerIndices(context, client, data);
			if (fr.leslimbes.client.hud.ApercuChantier.actif()) {
				String t = Text.translatable("hud.les_limbes.apercu", fr.leslimbes.client.hud.ApercuChantier.titre()).getString();
				context.drawCenteredTextWithShadow(client.textRenderer, t, context.getScaledWindowWidth() / 2, 64, OR);
			}
			fr.leslimbes.client.fx.Secousses.dessinerFlash(context);
		});
	}

	/** Indices de tutoriel progressifs : chacun n'apparaît qu'une fois, dix secondes, quand sa situation se présente. */
	private static final String[][] INDICES = {
			{"pouvoir", "pouvoirs"}, {"raie", "monture"}, {"projet", "manoir_recu"}, {"relais", "relais"}, {"lieu", "lieu"}};
	private static String indiceCourant = "";
	private static long indiceDepuis;

	private static boolean conditionIndice(String type, PlayerLimbesData data) {
		return switch (type) {
			case "pouvoirs" -> !data.pouvoirs.isEmpty();
			case "monture" -> !data.montures.isEmpty();
			case "manoir_recu" -> data.aDrapeau("manoir_recu");
			case "relais" -> data.drapeaux.stream().anyMatch(d -> d.startsWith("relais:"));
			case "lieu" -> data.drapeaux.stream().anyMatch(d -> d.startsWith("lieu:"));
			default -> false;
		};
	}

	private static void dessinerIndices(DrawContext context, MinecraftClient client, PlayerLimbesData data) {
		fr.leslimbes.client.OptionsLimbes options = fr.leslimbes.client.OptionsLimbes.get();
		long now = System.currentTimeMillis();
		if (indiceCourant.isEmpty()) {
			for (String[] indice : INDICES) {
				if (!options.indiceVu(indice[0]) && conditionIndice(indice[1], data)) {
					indiceCourant = indice[0];
					indiceDepuis = now;
					break;
				}
			}
			if (indiceCourant.isEmpty()) {
				return;
			}
		}
		if (now - indiceDepuis > 10_000) {
			options.marquerIndiceVu(indiceCourant);
			indiceCourant = "";
			return;
		}
		TextRenderer tr = client.textRenderer;
		String texte = Text.translatable("hud.les_limbes.tutoriel." + indiceCourant).getString();
		int largeur = Math.min(tr.getWidth(texte) + 12, context.getScaledWindowWidth() - 40);
		int x = (context.getScaledWindowWidth() - largeur) / 2;
		int y = context.getScaledWindowHeight() - 86;
		int lignes = tr.wrapLines(Text.literal(texte), largeur - 8).size();
		context.fill(x, y - 3, x + largeur, y + lignes * 10 + 2, FOND);
		int yy = y;
		for (net.minecraft.text.OrderedText l : tr.wrapLines(Text.literal(texte), largeur - 8)) {
			context.drawTextWithShadow(tr, l, x + 4, yy, OR);
			yy += 10;
		}
	}

	/** Vitrine regardée : nom de l'objet, qui l'a exposé, son histoire. */
	private static void dessinerVitrine(DrawContext context, MinecraftClient client) {
		if (!(client.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hit) || client.world == null) {
			return;
		}
		if (!(client.world.getBlockEntity(hit.getBlockPos()) instanceof fr.leslimbes.block.VitrineBlock.VitrineBlockEntity vitrine) || vitrine.getObjet().isEmpty()) {
			return;
		}
		TextRenderer tr = client.textRenderer;
		net.minecraft.item.ItemStack objet = vitrine.getObjet();
		String nom = objet.getName().getString();
		net.minecraft.util.Identifier id = net.minecraft.registry.Registries.ITEM.getId(objet.getItem());
		String cle = "vitrine.les_limbes." + id.getPath();
		String histoire = id.getNamespace().equals(LesLimbes.MOD_ID) && net.minecraft.client.resource.language.I18n.hasTranslation(cle)
				? Text.translatable(cle).getString() : Text.translatable("vitrine.les_limbes.inconnu").getString();
		String exposant = vitrine.getExposant().isEmpty() ? "" : Text.translatable("hud.les_limbes.vitrine.exposant", vitrine.getExposant()).getString();
		int largeur = Math.min(220, context.getScaledWindowWidth() - 40);
		java.util.List<net.minecraft.text.OrderedText> lignes = tr.wrapLines(Text.literal(histoire), largeur - 8);
		int hauteur = 14 + (exposant.isEmpty() ? 0 : 10) + lignes.size() * 10 + 4;
		int x = (context.getScaledWindowWidth() - largeur) / 2;
		int y = context.getScaledWindowHeight() / 2 + 14;
		context.fill(x, y, x + largeur, y + hauteur, FOND);
		context.drawBorder(x, y, largeur, hauteur, OR_SOMBRE);
		context.drawTextWithShadow(tr, nom, x + 4, y + 3, OR);
		int yy = y + 14;
		if (!exposant.isEmpty()) {
			context.drawTextWithShadow(tr, exposant, x + 4, yy, GRIS);
			yy += 10;
		}
		for (net.minecraft.text.OrderedText l : lignes) {
			context.drawTextWithShadow(tr, l, x + 4, yy, BLANC);
			yy += 10;
		}
	}

	/** Endurance de vol de la monture : barre centrée au-dessus de la barre d'objets. */
	private static void dessinerEndurance(DrawContext context, MinecraftClient client, RaieCelesteEntity raie) {
		int largeur = 120;
		int x = context.getScaledWindowWidth() / 2 - largeur / 2;
		int y = context.getScaledWindowHeight() - 48;
		float ratio = raie.getEndurance() / RaieCelesteEntity.ENDURANCE_MAX;
		context.fill(x - 1, y - 1, x + largeur + 1, y + 4, 0xA0141A33);
		context.fill(x, y, x + Math.round(largeur * ratio), y + 3, ratio > 0.25F ? 0xFF8FD8F0 : 0xFFE07A5A);
		String texte = Text.translatable("hud.les_limbes.endurance").getString();
		context.drawTextWithShadow(client.textRenderer, texte, x + largeur / 2 - client.textRenderer.getWidth(texte) / 2, y - 11, GRIS);
	}

	/**
	 * Colonne en bas à gauche, à gauche de la barre d'objets vanilla (dont le bord gauche est à
	 * largeur/2 − 91) : emplacements de pouvoirs sur deux lignes, puis réserve de majesté et son compteur.
	 */
	private static void dessinerMajeste(DrawContext context, MinecraftClient client, PlayerLimbesData data) {
		TextRenderer tr = client.textRenderer;
		int x = 8;
		int h = context.getScaledWindowHeight();
		int limite = context.getScaledWindowWidth() / 2 - 91 - 4;
		int largeurColonne = Math.max(96, Math.min(114, limite - x));
		int ecart = 2;
		int largeurSlot = (largeurColonne - ecart * (PlayerLimbesData.SLOTS - 1)) / PlayerLimbesData.SLOTS;
		int sy = h - 64;
		for (int i = 0; i < PlayerLimbesData.SLOTS; i++) {
			String id = data.equipes.get(i);
			int cx = x + i * (largeurSlot + ecart);
			context.fill(cx, sy, cx + largeurSlot, sy + 20, FOND);
			context.drawBorder(cx, sy, largeurSlot, 20, id.isEmpty() ? 0xFF4A4858 : OR_SOMBRE);
			String touche = LimbesKeys.nomTouche(i);
			texteAjuste(context, tr, touche, cx + 3, sy + 2, largeurSlot - 6, OR);
			if (!id.isEmpty()) {
				String nom = Text.translatable("pouvoir.les_limbes." + id + ".court").getString();
				texteAjuste(context, tr, nom, cx + 3, sy + 11, largeurSlot - 6, BLANC);
				var ability = Abilities.get(id);
				if (ability != null && data.majeste < ability.cout()) {
					context.fill(cx, sy, cx + largeurSlot, sy + 20, 0x60FF4040);
				}
			}
		}
		int y = h - 40;
		context.drawTexture(MAJESTE, x, y, 0, 16, 16, 16, 64, 32);
		int bx = x + 18;
		context.drawTexture(MAJESTE, bx, y + 4, 0, 0, 62, 8, 64, 32);
		float ratio = data.majesteMax <= 0 ? 0 : Math.max(0, Math.min(1, data.majeste / data.majesteMax));
		int largeur = Math.round(60 * ratio);
		if (largeur > 0) {
			context.drawTexture(MAJESTE, bx + 1, y + 5, 1, 9, largeur, 6, 64, 32);
		}
		String texte = (int) data.majeste + " / " + (int) data.majesteMax;
		context.drawTextWithShadow(tr, texte, bx, y + 15, OR);
	}

	/** Texte réduit (jusqu'à 60 %) s'il dépasse la largeur disponible, pour rester lisible dans un emplacement. */
	static void texteAjuste(DrawContext context, TextRenderer tr, String texte, int x, int y, int largeurMax, int couleur) {
		int largeur = tr.getWidth(texte);
		if (largeur <= largeurMax) {
			context.drawTextWithShadow(tr, texte, x, y, couleur);
			return;
		}
		float echelle = Math.max(0.6F, (float) largeurMax / largeur);
		String affiche = texte;
		if (largeur * echelle > largeurMax) {
			affiche = tr.trimToWidth(texte, Math.round(largeurMax / echelle));
		}
		context.getMatrices().push();
		context.getMatrices().translate(x, y + (1.0F - echelle) * 4.0F, 0);
		context.getMatrices().scale(echelle, echelle, 1.0F);
		context.drawTextWithShadow(tr, affiche, 0, 0, couleur);
		context.getMatrices().pop();
	}

	private static void dessinerSuiviQuete(DrawContext context, MinecraftClient client, PlayerLimbesData data) {
		Optional<QuestProgress> suivie = data.queteSuivie();
		TextRenderer tr = client.textRenderer;
		int droite = context.getScaledWindowWidth() - 8;
		int y = 8;
		long depuis = System.currentTimeMillis() - ClientLimbesData.derniereNotification();
		if (depuis < 4000 && !ClientLimbesData.texteNotification().isEmpty()) {
			String type = ClientLimbesData.typeNotification();
			String entete = switch (type) {
				case "debut" -> Text.translatable("hud.les_limbes.quete.nouvelle").getString();
				case "fin" -> Text.translatable("hud.les_limbes.quete.terminee").getString();
				default -> Text.translatable("hud.les_limbes.quete.etape").getString();
			};
			int cx = context.getScaledWindowWidth() / 2;
			context.drawCenteredTextWithShadow(tr, entete, cx, 40, OR);
			context.drawCenteredTextWithShadow(tr, ClientLimbesData.texteNotification(), cx, 52, BLANC);
		}
		if (suivie.isEmpty()) {
			return;
		}
		QuestProgress progress = suivie.get();
		QuestDefinition def = QuestLoader.get(progress.questId);
		if (def == null) {
			return;
		}
		Optional<QuestDefinition.Etape> etape = def.etapeCourante(progress);
		if (etape.isEmpty()) {
			return;
		}
		String titre = def.titre();
		context.drawTextWithShadow(tr, titre, droite - tr.getWidth(titre), y, OR);
		y += 11;
		boolean discret = fr.leslimbes.client.OptionsLimbes.get().discret;
		if (!discret) {
			for (QuestDefinition.Objectif o : etape.get().objectifs()) {
				boolean fait = o.estAtteint(progress);
				String ligne = (fait ? "✓ " : "• ") + o.texteAffiche(progress).getString();
				context.drawTextWithShadow(tr, ligne, droite - tr.getWidth(ligne), y, fait ? GRIS : BLANC);
				y += 10;
			}
		} else {
			// Mode discret : seulement la prochaine action, sans la liste des objectifs.
			for (QuestDefinition.Objectif o : etape.get().objectifs()) {
				if (!o.estAtteint(progress)) {
					String ligne = o.texteAffiche(progress).getString();
					context.drawTextWithShadow(tr, ligne, droite - tr.getWidth(ligne), y, GRIS);
					y += 10;
					break;
				}
			}
		}
		Optional<QuestDefinition.Lieu> lieu = def.lieuPour(progress);
		if (lieu.isPresent() && client.world != null && client.world.getRegistryKey().getValue().equals(lieu.get().dimension())) {
			BlockPos pos = lieu.get().pos();
			double dx = pos.getX() + 0.5 - client.player.getX();
			double dz = pos.getZ() + 0.5 - client.player.getZ();
			int distance = (int) Math.sqrt(dx * dx + dz * dz);
			String fleche = fleche(client.player.getYaw(), dx, dz);
			String ligne = fleche + " " + lieu.get().nom().getString() + " · " + distance + " m";
			context.drawTextWithShadow(tr, ligne, droite - tr.getWidth(ligne), y + 2, OR);
		} else if (lieu.isPresent()) {
			String ligne = Text.translatable("hud.les_limbes.lieu.autre_dimension", lieu.get().nom()).getString();
			context.drawTextWithShadow(tr, ligne, droite - tr.getWidth(ligne), y + 2, GRIS);
		}
	}

	/** Flèche relative à l'orientation du joueur (↑ = devant). */
	static String fleche(float yaw, double dx, double dz) {
		double cible = Math.toDegrees(Math.atan2(-dx, dz)); // convention Minecraft : yaw 0 = sud (+z)
		double rel = cible - yaw;
		while (rel < -180) {
			rel += 360;
		}
		while (rel > 180) {
			rel -= 360;
		}
		String[] fleches = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
		int idx = (int) Math.round(-rel / 45.0);
		idx = ((idx % 8) + 8) % 8;
		return fleches[idx];
	}

	public static String directionCardinale(double dx, double dz) {
		return BoussoleNacreeItem.direction(dx, dz);
	}

	private LimbesHud() {
	}
}
