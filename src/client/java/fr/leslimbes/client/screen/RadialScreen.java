package fr.leslimbes.client.screen;

import fr.leslimbes.client.ClientLimbesData;
import fr.leslimbes.client.input.LimbesKeys;
import fr.leslimbes.network.LimbesPayloads;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.power.Ability;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Sélection radiale facultative des pouvoirs (touche X) : un anneau de branches au centre, puis l'anneau des
 * techniques de la branche choisie. Clic : utiliser ; Maj + clic : équiper dans le premier emplacement libre
 * (ou le premier emplacement si tous sont pris). Le serveur valide tout.
 */
public class RadialScreen extends Screen {
	private static final int OR = 0xFFE3C97E;
	private static final int BLANC = 0xFFF4EFE6;
	private static final int GRIS = 0xFFB8B4B0;
	private String branche = "";
	private String survol = "";

	public RadialScreen() {
		super(Text.translatable("ecran.les_limbes.radial"));
	}

	@Override
	protected void init() {
		PlayerLimbesData data = ClientLimbesData.get();
		int cx = width / 2;
		int cy = height / 2;
		List<String> branches = new ArrayList<>();
		for (String b : Abilities.BRANCHES) {
			if (data.pouvoirs.stream().anyMatch(id -> Abilities.get(id) != null && Abilities.get(id).branche().equals(b))) {
				branches.add(b);
			}
		}
		if (branche.isEmpty() && !branches.isEmpty()) {
			branche = branches.get(0);
		}
		// Branches : une rangée d'onglets sous le titre (les noms des branches ne tiennent pas dans un anneau aux
		// petites résolutions ; les techniques, elles, ont un libellé court).
		if (!branches.isEmpty()) {
			int lo = Math.min(72, (width - 16) / branches.size() - 2);
			int x = cx - (branches.size() * (lo + 2) - 2) / 2;
			for (String b : branches) {
				ButtonWidget bouton = ButtonWidget.builder(Text.translatable("branche.les_limbes." + b + ".court"), bt -> {
					branche = b;
					clearChildren();
					init();
				}).dimensions(x, 36, lo, 18).build();
				bouton.active = !b.equals(branche);
				addDrawableChild(bouton);
				x += lo + 2;
			}
		}
		List<String> pouvoirs = new ArrayList<>();
		for (String id : data.pouvoirs) {
			Ability a = Abilities.get(id);
			if (a != null && a.branche().equals(branche)) {
				pouvoirs.add(id);
			}
		}
		pouvoirs.sort(String::compareTo);
		// Techniques : une ellipse qui exploite la largeur de l'écran ; boutons à la taille du libellé le plus long,
		// jamais plus serrés que leur largeur (neuf techniques tiennent même en 427 × 240).
		int w = 56;
		for (String id : pouvoirs) {
			w = Math.max(w, textRenderer.getWidth(Text.translatable("pouvoir.les_limbes." + id + ".court")) + 12);
		}
		w = Math.min(w, 96);
		int rx = Math.min(170, width / 2 - w / 2 - 8);
		int ry = Math.min(95, Math.max(30, height / 2 - 9 - 58));
		for (int i = 0; i < pouvoirs.size(); i++) {
			final String id = pouvoirs.get(i);
			double a = -Math.PI / 2 + i * Math.PI * 2 / Math.max(1, pouvoirs.size());
			int px = cx + (int) Math.round(Math.cos(a) * rx) - w / 2;
			int py = cy + 6 + (int) Math.round(Math.sin(a) * ry) - 9;
			addDrawableChild(ButtonWidget.builder(Text.translatable("pouvoir.les_limbes." + id + ".court"), bt -> {
				if (hasShiftDown()) {
					int slot = 0;
					for (int s = 0; s < PlayerLimbesData.SLOTS; s++) {
						if (data.equipes.get(s).isEmpty()) {
							slot = s;
							break;
						}
					}
					ClientPlayNetworking.send(new LimbesPayloads.EquiperPouvoir(slot, id));
					data.equiper(slot, id);
					clearChildren();
					init();
				} else {
					ClientPlayNetworking.send(new LimbesPayloads.UtiliserPouvoirId(id));
					close();
				}
			}).dimensions(px, py, w, 18).build());
		}
	}

	/** Tout ce qui est dessiné avant {@code super.renderBackground} serait flouté avec le monde (1.21). */
	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		PlayerLimbesData data = ClientLimbesData.get();
		context.fill(0, 0, width, height, 0x70000000);
		int cx = width / 2;
		int cy = height / 2;
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.radial"), cx, 12, OR);
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.radial.aide"), cx, 24, GRIS);
		if (data.pouvoirs.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.aucun_pouvoir"), cx, cy - 4, GRIS);
		} else {
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable("branche.les_limbes." + branche), cx, cy + 2, OR);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		PlayerLimbesData data = ClientLimbesData.get();
		int cx = width / 2;
		super.render(context, mouseX, mouseY, delta);
		// Détail de la technique survolée : coût, recharge, description, emplacement.
		survol = "";
		for (var enfant : children()) {
			if (enfant instanceof ButtonWidget b && b.isHovered()) {
				for (String id : data.pouvoirs) {
					if (b.getMessage().getString().equals(Text.translatable("pouvoir.les_limbes." + id + ".court").getString())) {
						survol = id;
					}
				}
			}
		}
		if (!survol.isEmpty()) {
			Ability a = Abilities.get(survol);
			int y = height - 46;
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable("pouvoir.les_limbes." + survol), cx, y, OR);
			if (a != null) {
				int slot = data.equipes.indexOf(survol);
				String info = Text.translatable("ecran.les_limbes.journal.cout", (int) a.cout(), a.recharge() / 20.0).getString()
						+ (slot >= 0 ? "  ·  " + LimbesKeys.nomTouche(slot) : "");
				context.drawCenteredTextWithShadow(textRenderer, info, cx, y + 10, GRIS);
			}
			var lignes = textRenderer.wrapLines(Text.translatable("pouvoir.les_limbes." + survol + ".description"), Math.min(360, width - 40));
			int yy = y + 20;
			for (var l : lignes) {
				context.drawCenteredTextWithShadow(textRenderer, l, cx, yy, BLANC);
				yy += 9;
			}
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
