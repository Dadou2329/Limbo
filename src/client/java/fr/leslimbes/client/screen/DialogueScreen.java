package fr.leslimbes.client.screen;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.network.LimbesPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran de dialogue : panneau en bas de l'écran, nom du personnage en or, texte page par page,
 * puis choix sous forme de boutons. Le jeu reste visible derrière ; Échap ferme proprement.
 */
public class DialogueScreen extends Screen {
	private static final Identifier PANNEAU = LesLimbes.id("textures/gui/panneau.png");
	private String nom = "";
	private List<String> pages = new ArrayList<>();
	private List<String> choix = new ArrayList<>();
	private int page;
	private boolean prevenirServeur = true;
	private final List<ButtonWidget> boutons = new ArrayList<>();

	public DialogueScreen(NbtCompound contenu) {
		super(Text.translatable("ecran.les_limbes.dialogue"));
		lire(contenu);
	}

	private void lire(NbtCompound contenu) {
		nom = contenu.getString("nom");
		pages = new ArrayList<>();
		NbtList lignes = contenu.getList("texte", NbtElement.STRING_TYPE);
		for (int i = 0; i < lignes.size(); i++) {
			pages.add(lignes.getString(i));
		}
		if (pages.isEmpty()) {
			pages.add("...");
		}
		choix = new ArrayList<>();
		NbtList c = contenu.getList("choix", NbtElement.STRING_TYPE);
		for (int i = 0; i < c.size(); i++) {
			choix.add(c.getString(i));
		}
		page = 0;
	}

	public void mettreAJour(NbtCompound contenu) {
		lire(contenu);
		clearChildren();
		init();
	}

	@Override
	protected void init() {
		boutons.clear();
		int largeur = Math.min(360, width - 40);
		int x = (width - largeur) / 2;
		int bas = height - 20;
		boolean dernierePage = page >= pages.size() - 1;
		if (!dernierePage) {
			boutons.add(addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.dialogue.suite"), b -> {
				page++;
				clearChildren();
				init();
			}).dimensions(x + largeur - 90, bas - 22, 86, 20).build()));
			return;
		}
		if (choix.isEmpty()) {
			boutons.add(addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.dialogue.fermer"), b -> fermer())
					.dimensions(x + largeur - 90, bas - 22, 86, 20).build()));
			return;
		}
		int y = bas - 22 * choix.size();
		for (int i = 0; i < choix.size(); i++) {
			final int index = i;
			boutons.add(addDrawableChild(ButtonWidget.builder(Text.literal(choix.get(i)), b -> choisir(index))
					.dimensions(x + 10, y + i * 22, largeur - 20, 20).build()));
		}
	}

	private void choisir(int index) {
		ClientPlayNetworking.send(new LimbesPayloads.ChoixDialogue(index));
		// Le serveur répond par un nouveau nœud (mise à jour) ou par une fermeture.
	}

	private void fermer() {
		if (prevenirServeur) {
			ClientPlayNetworking.send(new LimbesPayloads.ChoixDialogue(-1));
		}
		prevenirServeur = false;
		close();
	}

	/** Fermeture demandée par le serveur : inutile de le prévenir. */
	public void fermerSansPrevenir() {
		prevenirServeur = false;
		close();
	}

	@Override
	public void close() {
		if (prevenirServeur) {
			ClientPlayNetworking.send(new LimbesPayloads.ChoixDialogue(-1));
			prevenirServeur = false;
		}
		super.close();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			fermer();
			return true;
		}
		if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9 && page >= pages.size() - 1) {
			int index = keyCode - GLFW.GLFW_KEY_1;
			if (index < choix.size()) {
				choisir(index);
				return true;
			}
		}
		if ((keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER) && page < pages.size() - 1) {
			page++;
			clearChildren();
			init();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		// Pas de flou : on garde le personnage visible derrière le panneau.
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int largeur = Math.min(360, width - 40);
		int x = (width - largeur) / 2;
		List<OrderedText> lignes = textRenderer.wrapLines(Text.literal(pages.get(page)), largeur - 24);
		int hauteurTexte = lignes.size() * 10;
		int hauteurChoix = page >= pages.size() - 1 && !choix.isEmpty() ? 22 * choix.size() + 4 : 26;
		int hauteur = 24 + hauteurTexte + 8 + hauteurChoix;
		int y = height - 20 - hauteur;
		context.fill(x, y, x + largeur, y + hauteur, 0xD0141A33);
		context.drawBorder(x, y, largeur, hauteur, 0xFFB4934A);
		context.drawBorder(x + 1, y + 1, largeur - 2, hauteur - 2, 0xFFE3C97E);
		context.drawTextWithShadow(textRenderer, Text.literal(nom), x + 12, y + 8, 0xFFE3C97E);
		int ty = y + 22;
		for (OrderedText l : lignes) {
			context.drawTextWithShadow(textRenderer, l, x + 12, ty, 0xFFF4F1EA);
			ty += 10;
		}
		if (pages.size() > 1) {
			String p = (page + 1) + "/" + pages.size();
			context.drawTextWithShadow(textRenderer, p, x + largeur - 12 - textRenderer.getWidth(p), y + 8, 0xFF8A8A9A);
		}
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
