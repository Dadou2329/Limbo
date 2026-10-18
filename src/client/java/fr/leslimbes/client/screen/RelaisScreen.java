package fr.leslimbes.client.screen;

import fr.leslimbes.network.LimbesPayloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Salle des passages : liste des relais connus, un bouton « Y aller » par destination. */
public class RelaisScreen extends Screen {
	private record Relais(String id, String nom, String description, int[] pos, boolean ici) {
	}

	private final List<Relais> relais = new ArrayList<>();
	private int x0;
	private int y0;
	private int largeur;
	private int hauteur;

	public RelaisScreen(NbtCompound contenu) {
		super(Text.translatable("ecran.les_limbes.relais"));
		NbtList l = contenu.getList("relais", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < l.size(); i++) {
			NbtCompound r = l.getCompound(i);
			relais.add(new Relais(r.getString("id"), r.getString("nom"), r.getString("description"), r.getIntArray("pos"), r.getBoolean("ici")));
		}
	}

	@Override
	protected void init() {
		largeur = Math.min(360, width - 20);
		hauteur = Math.min(60 + relais.size() * 30, height - 20);
		x0 = (width - largeur) / 2;
		y0 = (height - hauteur) / 2;
		addDrawableChild(ButtonWidget.builder(Text.translatable("ecran.les_limbes.dialogue.fermer"), b -> close())
				.dimensions(x0 + largeur - 68, y0 + 6, 60, 18).build());
		int y = y0 + 32;
		for (Relais r : relais) {
			ButtonWidget b = ButtonWidget.builder(Text.translatable(r.ici() ? "ecran.les_limbes.relais.ici" : "ecran.les_limbes.relais.aller"), bb -> {
				ClientPlayNetworking.send(new LimbesPayloads.VoyagerRelais(r.id()));
				close();
			}).dimensions(x0 + largeur - 88, y, 80, 18).build();
			b.active = !r.ici();
			addDrawableChild(b);
			y += 30;
			if (y > y0 + hauteur - 20) {
				break;
			}
		}
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		context.fill(x0, y0, x0 + largeur, y0 + hauteur, 0xE0141A33);
		context.drawBorder(x0, y0, largeur, hauteur, 0xFFB4934A);
		context.drawBorder(x0 + 1, y0 + 1, largeur - 2, hauteur - 2, 0xFFE3C97E);
		context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.relais"), x0 + 8, y0 + 10, 0xFFE3C97E);
		if (relais.isEmpty()) {
			context.drawTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.relais.aucun"), x0 + 8, y0 + 36, 0xFFB8B4B0);
			return;
		}
		int y = y0 + 32;
		for (Relais r : relais) {
			context.drawTextWithShadow(textRenderer, r.nom(), x0 + 8, y + 1, r.ici() ? 0xFF8A8A9A : 0xFFF4F1EA);
			String sous = r.description().isEmpty() && r.pos().length >= 3 ? r.pos()[0] + ", " + r.pos()[2] : r.description();
			context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(sous, largeur - 110), x0 + 8, y + 11, 0xFFB8B4B0);
			y += 30;
			if (y > y0 + hauteur - 20) {
				break;
			}
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
