package fr.leslimbes.client.screen;

import fr.leslimbes.client.ClientLimbesData;
import fr.leslimbes.client.input.LimbesKeys;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Journal : onglet Quêtes (liste, détails, suivi) et onglet Pouvoirs (débloqués, emplacements).
 * Tout ce qui est affiché provient de la copie synchronisée ; les changements passent par le serveur.
 */
public class JournalScreen extends Screen {
	private enum Onglet { QUETES, POUVOIRS }

	private Onglet onglet = Onglet.QUETES;
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
		largeur = Math.min(420, width - 20);
		hauteur = Math.min(240, height - 20);
		x0 = (width - largeur) / 2;
		y0 = (height - hauteur) / 2;
		PlayerLimbesData data = ClientLimbesData.get();
		if (selection.isEmpty()) {
			selection = data.queteSuivie().map(p -> p.questId).orElse(quetesTriees(data).stream().findFirst().map(p -> p.questId).orElse(""));
		}
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.journal.quetes"), b -> basculer(Onglet.QUETES))
				.dimensions(x0 + 8, y0 + 6, 80, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.journal.pouvoirs"), b -> basculer(Onglet.POUVOIRS))
				.dimensions(x0 + 92, y0 + 6, 80, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.dialogue.fermer"), b -> close())
				.dimensions(x0 + largeur - 68, y0 + 6, 60, 18).build());
		if (onglet == Onglet.QUETES) {
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
		} else {
			int y = y0 + 32;
			List<String> pouvoirs = new ArrayList<>(data.pouvoirs);
			pouvoirs.sort(Comparator.naturalOrder());
			for (String id : pouvoirs) {
				final String pid = id;
				for (int slot = 0; slot < PlayerLimbesData.SLOTS; slot++) {
					final int s = slot;
					boolean deja = data.equipes.get(slot).equals(id);
					addDrawableChild(ButtonWidget.builder(Text.literal((deja ? "■ " : "") + LimbesKeys.nomTouche(slot)), b -> {
						ClientPlayNetworking.send(new LimbesPayloads.EquiperPouvoir(s, deja ? "" : pid));
						data.equiper(s, deja ? "" : pid);
						clearChildren();
						init();
					}).dimensions(x0 + largeur - 8 - (PlayerLimbesData.SLOTS - slot) * 34, y, 30, 18).build());
				}
				y += hauteurEntree(id);
			}
		}
	}

	/** Hauteur d'une entrée de l'onglet Pouvoirs : titre, coût, description repliée, marge. */
	private int hauteurEntree(String id) {
		int lignes = textRenderer.wrapLines(Text.translatable("pouvoir.les_limbes." + id + ".description"), largeur - 140).size();
		return 20 + lignes * 9 + 8;
	}

	private void basculer(Onglet o) {
		onglet = o;
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
		if (onglet == Onglet.QUETES) {
			int dx = x0 + 166;
			int dw = largeur - 174;
			context.fill(dx - 4, y0 + 30, x0 + largeur - 6, y0 + hauteur - 30, 0x40000000);
			Optional<QuestProgress> sel = data.quete(selection);
			QuestDefinition def = sel.map(p -> QuestLoader.get(p.questId)).orElse(null);
			if (def == null) {
				context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.aucune"), dx, y0 + 36, 0xFFB8B4B0);
			} else {
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
				} else {
					Optional<QuestDefinition.Etape> etape = def.etapeCourante(progress);
					if (etape.isPresent()) {
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
				}
			}
		} else {
			int y = y0 + 34;
			if (data.pouvoirs.isEmpty()) {
				context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.aucun_pouvoir"), x0 + 12, y, 0xFFB8B4B0);
			}
			List<String> pouvoirs = new ArrayList<>(data.pouvoirs);
			pouvoirs.sort(Comparator.naturalOrder());
			for (String id : pouvoirs) {
				Ability a = Abilities.get(id);
				context.drawTextWithShadow(textRenderer, Text.translatable("pouvoir.les_limbes." + id), x0 + 12, y, 0xFFE3C97E);
				String info = a == null ? "" : Text.translatable("ecran.les_limbes.journal.cout", (int) a.cout(), a.recharge() / 20.0).getString();
				context.drawTextWithShadow(textRenderer, info, x0 + 12, y + 10, 0xFFB8B4B0);
				List<OrderedText> desc = textRenderer.wrapLines(Text.translatable("pouvoir.les_limbes." + id + ".description"), largeur - 140);
				int yy = y + 20;
				for (OrderedText l : desc) {
					context.drawTextWithShadow(textRenderer, l, x0 + 12, yy, 0xFFD8D4CC);
					yy += 9;
				}
				y += hauteurEntree(id);
			}
			String maj = Text.translatable("ecran.les_limbes.journal.majeste", (int) data.majeste, (int) data.majesteMax).getString();
			context.drawTextWithShadow(textRenderer, maj, x0 + 12, y0 + hauteur - 22, 0xFFE3C97E);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
