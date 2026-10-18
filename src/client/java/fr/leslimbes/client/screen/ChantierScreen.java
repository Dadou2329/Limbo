package fr.leslimbes.client.screen;

import fr.leslimbes.network.LimbesPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran des projets du domaine : liste à gauche (état), détails à droite (description, prérequis, coût
 * avec ce que le joueur possède, blocs occupés dans l'emprise), boutons Aperçu / Lancer / Lancer en
 * ignorant la zone occupée. Le serveur revérifie tout ; cet écran n'envoie que des identifiants.
 */
public class ChantierScreen extends Screen {
	private record Cout(String objet, int nombre, int possede) {
	}

	private record Projet(String id, String titre, String description, String indice, String etat, int conflits, String exemple, List<Cout> couts) {
	}

	private final List<Projet> projets = new ArrayList<>();
	private String lieu = "";
	private String selection = "";
	private int x0;
	private int y0;
	private int largeur;
	private int hauteur;

	public ChantierScreen(NbtCompound contenu) {
		super(Text.translatable("ecran.les_limbes.chantier"));
		lire(contenu);
	}

	private void lire(NbtCompound contenu) {
		lieu = contenu.getString("lieu");
		projets.clear();
		NbtList l = contenu.getList("projets", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < l.size(); i++) {
			NbtCompound p = l.getCompound(i);
			List<Cout> couts = new ArrayList<>();
			NbtList c = p.getList("cout", NbtElement.COMPOUND_TYPE);
			for (int j = 0; j < c.size(); j++) {
				NbtCompound cc = c.getCompound(j);
				couts.add(new Cout(cc.getString("objet"), cc.getInt("nombre"), cc.getInt("possede")));
			}
			projets.add(new Projet(p.getString("id"), p.getString("titre"), p.getString("description"), p.getString("indice"), p.getString("etat"),
					p.getInt("conflits"), p.getString("exemple"), couts));
		}
		if (selection.isEmpty() || projets.stream().noneMatch(p -> p.id().equals(selection))) {
			selection = projets.stream().filter(p -> p.etat().equals("disponible")).findFirst().map(Projet::id)
					.orElse(projets.isEmpty() ? "" : projets.get(0).id());
		}
	}

	/** Mise à jour envoyée par le serveur après un lancement. */
	public void mettreAJour(NbtCompound contenu) {
		lire(contenu);
		clearChildren();
		init();
	}

	private Projet selectionne() {
		return projets.stream().filter(p -> p.id().equals(selection)).findFirst().orElse(null);
	}

	@Override
	protected void init() {
		largeur = Math.min(440, width - 20);
		hauteur = Math.min(250, height - 20);
		x0 = (width - largeur) / 2;
		y0 = (height - hauteur) / 2;
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.dialogue.fermer"), b -> close())
				.dimensions(x0 + largeur - 68, y0 + 6, 60, 18).build());
		int y = y0 + 30;
		for (Projet p : projets) {
			String prefixe = switch (p.etat()) {
				case "fait" -> "✓ ";
				case "verrouille" -> "🔒 ";
				default -> "• ";
			};
			final String id = p.id();
			ButtonWidget b = ButtonWidget.builder(Text.literal(prefixe + p.titre()), bb -> {
				selection = id;
				clearChildren();
				init();
			}).dimensions(x0 + 8, y, 160, 18).build();
			b.active = !id.equals(selection);
			addDrawableChild(b);
			y += 20;
			if (y > y0 + hauteur - 24) {
				break;
			}
		}
		Projet sel = selectionne();
		if (sel != null && sel.etat().equals("disponible")) {
			int by = y0 + hauteur - 26;
			addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.chantier.apercu"), b ->
					ClientPlayNetworking.send(new LimbesPayloads.DemanderApercu(sel.id()))).dimensions(x0 + 176, by, 70, 18).build());
			boolean ressources = sel.couts().stream().allMatch(c -> c.possede() >= c.nombre()) || (client != null && client.player != null && client.player.isCreative());
			if (sel.conflits() == 0) {
				ButtonWidget lancer = ButtonWidget.builder(Text.translatable("ecran.les_limbes.chantier.lancer"), b -> {
					ClientPlayNetworking.send(new LimbesPayloads.LancerProjet(sel.id(), false));
				}).dimensions(x0 + 250, by, 80, 18).build();
				lancer.active = ressources;
				addDrawableChild(lancer);
			} else {
				ButtonWidget ignorer = ButtonWidget.builder(Text.translatable("ecran.les_limbes.chantier.ignorer"), b -> {
					ClientPlayNetworking.send(new LimbesPayloads.LancerProjet(sel.id(), true));
				}).dimensions(x0 + 250, by, largeur - 258, 18).build();
				ignorer.active = ressources;
				addDrawableChild(ignorer);
			}
		}
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		context.fill(x0, y0, x0 + largeur, y0 + hauteur, 0xE0141A33);
		context.drawBorder(x0, y0, largeur, hauteur, 0xFFB4934A);
		context.drawBorder(x0 + 1, y0 + 1, largeur - 2, hauteur - 2, 0xFFE3C97E);
		context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.chantier"), x0 + 8, y0 + 10, 0xFFE3C97E);
		int dx = x0 + 176;
		int dw = largeur - 184;
		context.fill(dx - 4, y0 + 28, x0 + largeur - 6, y0 + hauteur - 30, 0x40000000);
		Projet p = selectionne();
		if (p == null) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.chantier.aucun"), dx, y0 + 34, 0xFFB8B4B0);
			return;
		}
		int y = y0 + 32;
		context.drawTextWithShadow(textRenderer, p.titre(), dx, y, 0xFFE3C97E);
		String etat = switch (p.etat()) {
			case "fait" -> Text.translatable("ecran.les_limbes.chantier.fait").getString();
			case "verrouille" -> Text.translatable("ecran.les_limbes.chantier.verrouille").getString();
			default -> "";
		};
		if (!etat.isEmpty()) {
			context.drawTextWithShadow(textRenderer, etat, x0 + largeur - 10 - textRenderer.getWidth(etat), y, p.etat().equals("fait") ? 0xFF8FA589 : 0xFFB8B4B0);
		}
		y += 12;
		for (OrderedText l : textRenderer.wrapLines(Text.literal(p.description()), dw)) {
			context.drawTextWithShadow(textRenderer, l, dx, y, 0xFFD8D4CC);
			y += 10;
		}
		y += 4;
		if (p.etat().equals("verrouille") && !p.indice().isEmpty()) {
			for (OrderedText l : textRenderer.wrapLines(Text.translatable("ecran.les_limbes.chantier.indice", p.indice()), dw)) {
				context.drawTextWithShadow(textRenderer, l, dx, y, 0xFFE07A5A);
				y += 10;
			}
			y += 4;
		}
		if (!p.couts().isEmpty() && !p.etat().equals("fait")) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.chantier.cout"), dx, y, 0xFFE3C97E);
			y += 10;
			for (Cout c : p.couts()) {
				Item item = Registries.ITEM.get(Identifier.of(c.objet()));
				String ligne = "  " + c.nombre() + " × " + item.getName().getString() + "  (" + c.possede() + ")";
				context.drawTextWithShadow(textRenderer, ligne, dx, y, c.possede() >= c.nombre() ? 0xFF8FA589 : 0xFFE07A5A);
				y += 10;
			}
			y += 4;
		}
		if (p.etat().equals("disponible")) {
			Text zone = p.conflits() == 0 ? Text.translatable("ecran.les_limbes.chantier.libre")
					: Text.translatable("ecran.les_limbes.chantier.conflits", p.conflits(), p.exemple());
			for (OrderedText l : textRenderer.wrapLines(zone, dw)) {
				context.drawTextWithShadow(textRenderer, l, dx, y, p.conflits() == 0 ? 0xFF8FA589 : 0xFFF2C14E);
				y += 10;
			}
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
