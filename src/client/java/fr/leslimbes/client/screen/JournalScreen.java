package fr.leslimbes.client.screen;

import fr.leslimbes.client.ClientLimbesData;
import fr.leslimbes.client.OptionsLimbes;
import fr.leslimbes.client.input.LimbesKeys;
import fr.leslimbes.domaine.DomaineLoader;
import fr.leslimbes.domaine.LieuDefinition;
import fr.leslimbes.domaine.RelaisDefinition;
import fr.leslimbes.network.LimbesPayloads;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.power.Ability;
import fr.leslimbes.quest.QuestDefinition;
import fr.leslimbes.quest.QuestLoader;
import fr.leslimbes.quest.QuestProgress;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Journal : Quêtes (liste, détails, suivi), Pouvoirs (débloqués, emplacements), Domaine (projets, compagnons,
 * relais), Carte (lieux découverts, position du joueur, lieu de la quête suivie) et Options.
 * Tout ce qui est affiché provient de la copie synchronisée ; les changements passent par le serveur.
 */
public class JournalScreen extends Screen {
	private enum Onglet { QUETES, POUVOIRS, DOMAINE, CARTE, OPTIONS }

	/** Compagnons recrutables du domaine : rôle et drapeau de recrutement. */
	private static final String[][] COMPAGNONS = {
			{"sabine", "sabine_recrutee"}, {"loic", "loic_recrute"}, {"hanna", "hanna_recrutee"}, {"theo", "theo_recrute"},
			{"marguerite", "marguerite_recrutee"}, {"ysolde", "ysolde_recrutee"}, {"emile", "emile_a_clairval"}};

	private Onglet onglet = Onglet.QUETES;
	private int defilement;
	private int hauteurContenu;
	private String selection = "";
	private int x0;
	private int y0;
	private int largeur;
	private int hauteur;

	public JournalScreen() {
		super(Text.translatable("ecran.les_limbes.journal"));
	}

	/** Journal ouvert directement sur l'onglet des pouvoirs. */
	public static JournalScreen pouvoirs() {
		JournalScreen ecran = new JournalScreen();
		ecran.onglet = Onglet.POUVOIRS;
		return ecran;
	}

	@Override
	protected void init() {
		largeur = Math.min(440, width - 20);
		hauteur = Math.min(250, height - 20);
		x0 = (width - largeur) / 2;
		y0 = (height - hauteur) / 2;
		PlayerLimbesData data = ClientLimbesData.get();
		if (selection.isEmpty()) {
			selection = data.queteSuivie().map(p -> p.questId).orElse(quetesTriees(data).stream().findFirst().map(p -> p.questId).orElse(""));
		}
		String[] cles = {"ecran.les_limbes.journal.quetes", "ecran.les_limbes.journal.pouvoirs", "ecran.les_limbes.journal.domaine",
				"ecran.les_limbes.journal.carte", "ecran.les_limbes.journal.options"};
		int lo = Math.min(66, (largeur - 84) / cles.length - 2);
		for (int i = 0; i < cles.length; i++) {
			final Onglet o = Onglet.values()[i];
			ButtonWidget b = ButtonWidget.builder(Text.translatable(cles[i]), bb -> basculer(o)).dimensions(x0 + 6 + i * (lo + 2), y0 + 6, lo, 18).build();
			b.active = o != onglet;
			addDrawableChild(b);
		}
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.dialogue.fermer"), b -> close())
				.dimensions(x0 + largeur - 66, y0 + 6, 60, 18).build());
		switch (onglet) {
			case QUETES -> initQuetes(data);
			case POUVOIRS -> initPouvoirs(data);
			case OPTIONS -> initOptions();
			default -> {
			}
		}
	}

	private void initQuetes(PlayerLimbesData data) {
		int y = y0 + 32;
		for (QuestProgress p : quetesTriees(data)) {
			QuestDefinition def = QuestLoader.get(p.questId);
			if (def == null || def.cachee()) {
				continue;
			}
			String titre = (p.terminee ? "✓ " : "") + def.titre();
			final String id = p.questId;
			addDrawableChild(ButtonWidget.builder(Text.literal(titre), b -> {
				selection = id;
				clearChildren();
				init();
			}).dimensions(x0 + 8, y, 150, 18).build());
			y += 20;
			if (y > y0 + hauteur - 24) {
				break;
			}
		}
		Optional<QuestProgress> sel = data.quete(selection);
		if (sel.isPresent() && !sel.get().terminee) {
			boolean suivie = data.queteSuivie.equals(selection);
			addDrawableChild(ButtonWidget.builder(Text.translatable(suivie ? "ecran.les_limbes.journal.ne_plus_suivre" : "ecran.les_limbes.journal.suivre"), b -> {
				ClientPlayNetworking.send(new LimbesPayloads.SuivreQuete(suivie ? "" : selection));
				data.queteSuivie = suivie ? "" : selection;
				clearChildren();
				init();
			}).dimensions(x0 + largeur - 110, y0 + hauteur - 26, 102, 18).build());
		}
	}

	private void initPouvoirs(PlayerLimbesData data) {
		int y = y0 + 32 - defilement;
		int bas = y0 + hauteur - 26;
		for (String branche : Abilities.BRANCHES) {
			List<String> pouvoirs = pouvoirsDe(data, branche);
			if (pouvoirs.isEmpty()) {
				continue;
			}
			y += 12;
			for (String id : pouvoirs) {
				final String pid = id;
				if (y >= y0 + 30 && y + 18 <= bas) {
					for (int slot = 0; slot < PlayerLimbesData.SLOTS; slot++) {
						final int s = slot;
						boolean deja = data.equipes.get(slot).equals(id);
						addDrawableChild(ButtonWidget.builder(Text.literal((deja ? "■ " : "") + LimbesKeys.nomTouche(slot)), b -> {
							ClientPlayNetworking.send(new LimbesPayloads.EquiperPouvoir(s, deja ? "" : pid));
							data.equiper(s, deja ? "" : pid);
							clearChildren();
							init();
						}).dimensions(x0 + largeur - 8 - (PlayerLimbesData.SLOTS - slot) * 32, y, 30, 18).build());
					}
				}
				y += hauteurEntree(id);
			}
			y += 4;
		}
		hauteurContenu = y + defilement - (y0 + 32);
	}

	/** Pouvoirs débloqués d'une branche, dans l'ordre du registre (ordre d'apprentissage naturel). */
	private static List<String> pouvoirsDe(PlayerLimbesData data, String branche) {
		List<String> liste = new ArrayList<>();
		for (String id : Abilities.tous().keySet()) {
			Ability a = Abilities.get(id);
			if (a != null && a.branche().equals(branche) && data.pouvoirs.contains(id)) {
				liste.add(id);
			}
		}
		return liste;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (onglet == Onglet.POUVOIRS) {
			int max = Math.max(0, hauteurContenu - (hauteur - 60));
			int nouveau = (int) Math.max(0, Math.min(max, defilement - verticalAmount * 24));
			if (nouveau != defilement) {
				defilement = nouveau;
				clearChildren();
				init();
			}
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void initOptions() {
		OptionsLimbes o = OptionsLimbes.get();
		int y = y0 + 36;
		int w = Math.min(260, largeur - 24);
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.options.effets", o.densiteEffets), b -> {
			o.cyclerDensite();
			rafraichir();
		}).dimensions(x0 + 12, y, w, 18).build());
		y += 22;
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.options.secousses", OptionsLimbes.ouiNon(o.secousses)), b -> {
			o.secousses = !o.secousses;
			o.sauvegarder();
			rafraichir();
		}).dimensions(x0 + 12, y, w, 18).build());
		y += 22;
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.options.flashes", OptionsLimbes.ouiNon(o.flashes)), b -> {
			o.flashes = !o.flashes;
			o.sauvegarder();
			rafraichir();
		}).dimensions(x0 + 12, y, w, 18).build());
		y += 22;
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.options.marqueurs", OptionsLimbes.ouiNon(o.marqueurs)), b -> {
			o.marqueurs = !o.marqueurs;
			o.sauvegarder();
			rafraichir();
		}).dimensions(x0 + 12, y, w, 18).build());
		y += 22;
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.options.discret", OptionsLimbes.ouiNon(o.discret)), b -> {
			o.discret = !o.discret;
			o.sauvegarder();
			rafraichir();
		}).dimensions(x0 + 12, y, w, 18).build());
		y += 22;
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.options.tutoriel"), b -> {
			o.indicesVus.clear();
			o.sauvegarder();
			rafraichir();
		}).dimensions(x0 + 12, y, w, 18).build());
	}

	private void rafraichir() {
		clearChildren();
		init();
	}

	/** Hauteur d'une entrée de l'onglet Pouvoirs : titre, coût, description repliée, marge. */
	private int hauteurEntree(String id) {
		int lignes = textRenderer.wrapLines(Text.translatable("pouvoir.les_limbes." + id + ".description"), largeur - 24 - PlayerLimbesData.SLOTS * 32).size();
		return 20 + lignes * 9 + 8;
	}

	private void basculer(Onglet o) {
		onglet = o;
		defilement = 0;
		clearChildren();
		init();
	}

	private static List<QuestProgress> quetesTriees(PlayerLimbesData data) {
		List<QuestProgress> liste = new ArrayList<>(data.quetes.values());
		liste.sort(Comparator.comparing((QuestProgress p) -> p.terminee).thenComparing(p -> p.questId));
		return liste;
	}

	/**
	 * Le panneau et son contenu sont dessinés ici, après le flou et l'assombrissement vanilla du fond :
	 * tout ce qui est dessiné avant {@code super.renderBackground} serait flouté avec le monde.
	 */
	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		context.fill(x0, y0, x0 + largeur, y0 + hauteur, 0xE0141A33);
		context.drawBorder(x0, y0, largeur, hauteur, 0xFFB4934A);
		context.drawBorder(x0 + 1, y0 + 1, largeur - 2, hauteur - 2, 0xFFE3C97E);
		PlayerLimbesData data = ClientLimbesData.get();
		switch (onglet) {
			case QUETES -> dessinerQuetes(context, data);
			case POUVOIRS -> dessinerPouvoirs(context, data);
			case DOMAINE -> dessinerDomaine(context, data);
			case CARTE -> dessinerCarte(context, data);
			case OPTIONS -> dessinerOptions(context);
		}
	}

	private void dessinerQuetes(DrawContext context, PlayerLimbesData data) {
		int dx = x0 + 166;
		int dw = largeur - 174;
		context.fill(dx - 4, y0 + 30, x0 + largeur - 6, y0 + hauteur - 30, 0x40000000);
		Optional<QuestProgress> sel = data.quete(selection);
		QuestDefinition def = sel.map(p -> QuestLoader.get(p.questId)).orElse(null);
		if (def == null) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.aucune"), dx, y0 + 36, 0xFFB8B4B0);
			return;
		}
		int y = y0 + 34;
		context.drawTextWithShadow(textRenderer, def.titre(), dx, y, 0xFFE3C97E);
		y += 12;
		for (OrderedText l : textRenderer.wrapLines(Text.literal(def.description()), dw)) {
			context.drawTextWithShadow(textRenderer, l, dx, y, 0xFFD8D4CC);
			y += 10;
		}
		y += 6;
		QuestProgress progress = sel.get();
		if (progress.terminee) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.terminee"), dx, y, 0xFF8FA589);
			return;
		}
		Optional<QuestDefinition.Etape> etape = def.etapeCourante(progress);
		if (etape.isEmpty()) {
			return;
		}
		for (OrderedText l : textRenderer.wrapLines(Text.literal(etape.get().resume()), dw)) {
			context.drawTextWithShadow(textRenderer, l, dx, y, 0xFFF4F1EA);
			y += 10;
		}
		y += 2;
		for (QuestDefinition.Objectif o : etape.get().objectifs()) {
			boolean fait = o.estAtteint(progress);
			String ligne = (fait ? "✓ " : "• ") + o.texteAffiche(progress).getString();
			for (OrderedText l : textRenderer.wrapLines(Text.literal(ligne), dw)) {
				context.drawTextWithShadow(textRenderer, l, dx + 4, y, fait ? 0xFF8A8A9A : 0xFFF4F1EA);
				y += 10;
			}
		}
		Optional<QuestDefinition.Lieu> lieu = def.lieuPour(progress);
		if (lieu.isPresent()) {
			y += 4;
			String l = Text.translatable("ecran.les_limbes.journal.lieu", lieu.get().nom(), lieu.get().pos().toShortString()).getString();
			context.drawTextWithShadow(textRenderer, l, dx, y, 0xFFE3C97E);
		}
	}

	private void dessinerPouvoirs(DrawContext context, PlayerLimbesData data) {
		if (data.pouvoirs.isEmpty()) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.aucun_pouvoir"), x0 + 12, y0 + 34, 0xFFB8B4B0);
			return;
		}
		int haut = y0 + 30;
		int bas = y0 + hauteur - 26;
		context.enableScissor(x0 + 2, haut, x0 + largeur - 2, bas);
		int y = y0 + 32 - defilement;
		int largeurTexte = largeur - 24 - PlayerLimbesData.SLOTS * 32;
		for (String branche : Abilities.BRANCHES) {
			List<String> pouvoirs = pouvoirsDe(data, branche);
			if (pouvoirs.isEmpty()) {
				continue;
			}
			context.drawTextWithShadow(textRenderer, Text.translatable("branche.les_limbes." + branche).formatted(net.minecraft.util.Formatting.BOLD), x0 + 12, y, 0xFFB4934A);
			y += 12;
			for (String id : pouvoirs) {
				Ability a = Abilities.get(id);
				context.drawTextWithShadow(textRenderer, Text.translatable("pouvoir.les_limbes." + id), x0 + 12, y, 0xFFE3C97E);
				String info = a == null ? "" : Text.translatable("ecran.les_limbes.journal.cout", (int) a.cout(), a.recharge() / 20.0).getString();
				context.drawTextWithShadow(textRenderer, info, x0 + 12, y + 10, 0xFFB8B4B0);
				List<OrderedText> desc = textRenderer.wrapLines(Text.translatable("pouvoir.les_limbes." + id + ".description"), largeurTexte);
				int yy = y + 20;
				for (OrderedText l : desc) {
					context.drawTextWithShadow(textRenderer, l, x0 + 12, yy, 0xFFD8D4CC);
					yy += 9;
				}
				y += hauteurEntree(id);
			}
			y += 4;
		}
		context.disableScissor();
		int max = Math.max(0, hauteurContenu - (hauteur - 60));
		if (max > 0) {
			int piste = bas - haut;
			int curseur = Math.max(12, piste * (hauteur - 60) / Math.max(1, hauteurContenu));
			int yc = haut + (piste - curseur) * defilement / max;
			context.fill(x0 + largeur - 5, haut, x0 + largeur - 3, bas, 0x40000000);
			context.fill(x0 + largeur - 5, yc, x0 + largeur - 3, yc + curseur, 0xFFB4934A);
		}
		String maj = Text.translatable("ecran.les_limbes.journal.majeste", (int) data.majeste, (int) data.majesteMax).getString();
		context.drawTextWithShadow(textRenderer, maj, x0 + 12, y0 + hauteur - 20, 0xFFE3C97E);
		context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.radial_aide", LimbesKeys.RADIAL.getBoundKeyLocalizedText().getString()),
				x0 + 140, y0 + hauteur - 20, 0xFF8A8A9A);
	}

	private void dessinerDomaine(DrawContext context, PlayerLimbesData data) {
		int colonne = (largeur - 24) / 2;
		int y = y0 + 34;
		// Projets
		context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.projets"), x0 + 12, y, 0xFFE3C97E);
		y += 12;
		List<ClientLimbesData.ProjetResume> projets = ClientLimbesData.projets();
		if (projets.isEmpty()) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.chantier.aucun"), x0 + 12, y, 0xFFB8B4B0);
			y += 10;
		}
		int faits = 0;
		for (ClientLimbesData.ProjetResume p : projets) {
			if (p.fait()) {
				faits++;
			}
			String ligne = (p.fait() ? "✓ " : "• ") + p.titre();
			context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(ligne, colonne - 4), x0 + 12, y, p.fait() ? 0xFF8FA589 : 0xFFD8D4CC);
			y += 10;
			if (y > y0 + hauteur - 36) {
				break;
			}
		}
		if (!projets.isEmpty()) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.projets.total", faits, projets.size()), x0 + 12, y0 + hauteur - 22, 0xFFB8B4B0);
		}
		// Compagnons et relais
		int dx = x0 + 12 + colonne;
		y = y0 + 34;
		context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.recrues"), dx, y, 0xFFE3C97E);
		y += 12;
		for (String[] c : COMPAGNONS) {
			boolean recrute = data.aDrapeau(c[1]);
			String nom = Text.translatable("pnj.les_limbes." + c[0]).getString();
			String metier = Text.translatable("metier.les_limbes." + c[0]).getString();
			String ligne = (recrute ? "✓ " : "• ") + nom + ", " + metier;
			context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(ligne, colonne - 4), dx, y, recrute ? 0xFF8FA589 : 0xFF8A8A9A);
			y += 10;
		}
		y += 6;
		context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.relais"), dx, y, 0xFFE3C97E);
		y += 12;
		int connus = 0;
		for (RelaisDefinition r : DomaineLoader.relaisTries()) {
			if (data.aDrapeau(r.drapeau())) {
				connus++;
				context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth("• " + r.nom(), colonne - 4), dx, y, 0xFFD8D4CC);
				y += 10;
			}
		}
		if (connus == 0) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.relais.aucun"), dx, y, 0xFFB8B4B0);
		}
	}

	private void dessinerCarte(DrawContext context, PlayerLimbesData data) {
		int cx0 = x0 + 8, cy0 = y0 + 30, cw = largeur - 16, ch = hauteur - 38;
		context.fill(cx0, cy0, cx0 + cw, cy0 + ch, 0xFF1B2446);
		if (client == null || client.player == null || client.world == null) {
			return;
		}
		String dim = client.world.getRegistryKey().getValue().toString();
		List<LieuDefinition> lieux = new ArrayList<>();
		for (LieuDefinition l : DomaineLoader.lieux().values()) {
			if (l.dimension().equals(dim)) {
				lieux.add(l);
			}
		}
		if (lieux.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.carte.vide"), cx0 + cw / 2, cy0 + ch / 2 - 4, 0xFFB8B4B0);
			return;
		}
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (LieuDefinition l : lieux) {
			minX = Math.min(minX, l.pos().getX() - 40);
			maxX = Math.max(maxX, l.pos().getX() + 40);
			minZ = Math.min(minZ, l.pos().getZ() - 40);
			maxZ = Math.max(maxZ, l.pos().getZ() + 40);
		}
		int px = (int) client.player.getX(), pz = (int) client.player.getZ();
		minX = Math.min(minX, px - 20);
		maxX = Math.max(maxX, px + 20);
		minZ = Math.min(minZ, pz - 20);
		maxZ = Math.max(maxZ, pz + 20);
		double echelle = Math.min((cw - 20) / (double) (maxX - minX), (ch - 20) / (double) (maxZ - minZ));
		int ox = cx0 + (cw - (int) ((maxX - minX) * echelle)) / 2;
		int oz = cy0 + (ch - (int) ((maxZ - minZ) * echelle)) / 2;
		// Quadrillage tous les 50 blocs
		for (int gx = Math.floorDiv(minX, 50) * 50; gx <= maxX; gx += 50) {
			int sx = ox + (int) ((gx - minX) * echelle);
			if (sx >= cx0 && sx <= cx0 + cw) {
				context.fill(sx, cy0, sx + 1, cy0 + ch, 0x30FFFFFF);
			}
		}
		for (int gz = Math.floorDiv(minZ, 50) * 50; gz <= maxZ; gz += 50) {
			int sz = oz + (int) ((gz - minZ) * echelle);
			if (sz >= cy0 && sz <= cy0 + ch) {
				context.fill(cx0, sz, cx0 + cw, sz + 1, 0x30FFFFFF);
			}
		}
		int decouverts = 0;
		for (LieuDefinition l : lieux) {
			boolean connu = data.aDrapeau(l.drapeau());
			if (!connu) {
				continue;
			}
			decouverts++;
			int sx = ox + (int) ((l.pos().getX() - minX) * echelle);
			int sz = oz + (int) ((l.pos().getZ() - minZ) * echelle);
			int r = Math.max(2, (int) (l.rayon() * echelle));
			context.fill(sx - r, sz - r, sx + r + 1, sz + r + 1, 0x40E3C97E);
			context.fill(sx - 1, sz - 1, sx + 2, sz + 2, 0xFFE3C97E);
			String nom = textRenderer.trimToWidth(l.nom(), 90);
			int tx = Math.min(cx0 + cw - textRenderer.getWidth(nom) - 2, sx + 4);
			context.drawTextWithShadow(textRenderer, nom, tx, Math.max(cy0 + 2, sz - 10), 0xFFF4F1EA);
		}
		// Lieu de la quête suivie
		Optional<QuestProgress> suivie = data.queteSuivie();
		if (suivie.isPresent()) {
			QuestDefinition def = QuestLoader.get(suivie.get().questId);
			Optional<QuestDefinition.Lieu> lieu = def == null ? Optional.empty() : def.lieuPour(suivie.get());
			if (lieu.isPresent() && lieu.get().dimension().toString().equals(dim)) {
				BlockPos p = lieu.get().pos();
				int sx = ox + (int) ((p.getX() - minX) * echelle);
				int sz = oz + (int) ((p.getZ() - minZ) * echelle);
				if (sx >= cx0 && sx <= cx0 + cw && sz >= cy0 && sz <= cy0 + ch) {
					context.fill(sx - 3, sz, sx + 4, sz + 1, 0xFFF2C14E);
					context.fill(sx, sz - 3, sx + 1, sz + 4, 0xFFF2C14E);
					context.drawTextWithShadow(textRenderer, "◆ " + lieu.get().nom().getString(), Math.min(cx0 + cw - 90, sx + 5), sz + 3, 0xFFF2C14E);
				}
			}
		}
		// Joueur : point blanc et petite flèche d'orientation
		int sx = ox + (int) ((px - minX) * echelle);
		int sz = oz + (int) ((pz - minZ) * echelle);
		context.fill(sx - 2, sz - 2, sx + 3, sz + 3, 0xFFF4F1EA);
		double yaw = Math.toRadians(client.player.getYaw());
		int fx = sx + (int) Math.round(-Math.sin(yaw) * 6), fz = sz + (int) Math.round(Math.cos(yaw) * 6);
		context.fill(Math.min(sx, fx), Math.min(sz, fz), Math.max(sx, fx) + 1, Math.max(sz, fz) + 1, 0xA0F4F1EA);
		context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.carte.vous"), sx + 5, sz + 3, 0xFFF4F1EA);
		String compte = Text.translatable("ecran.les_limbes.journal.lieux", decouverts, lieux.size()).getString();
		context.drawTextWithShadow(textRenderer, compte, cx0 + 4, cy0 + ch - 11, 0xFFB8B4B0);
		String coords = px + ", " + pz;
		context.drawTextWithShadow(textRenderer, coords, cx0 + cw - 4 - textRenderer.getWidth(coords), cy0 + ch - 11, 0xFFB8B4B0);
	}

	private void dessinerOptions(DrawContext context) {
		int y = y0 + 36 + 6 * 22 + 4;
		for (OrderedText l : textRenderer.wrapLines(Text.translatable("ecran.les_limbes.options.aide"), largeur - 24)) {
			context.drawTextWithShadow(textRenderer, l, x0 + 12, y, 0xFFB8B4B0);
			y += 10;
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
