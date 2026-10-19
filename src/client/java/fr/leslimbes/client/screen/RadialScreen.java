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
		int rb = Math.min(70, Math.min(width, height) / 4);
		for (int i = 0; i < branches.size(); i++) {
			final String b = branches.get(i);
			double a = -Math.PI / 2 + i * Math.PI * 2 / branches.size();
			int bx = cx + (int) Math.round(Math.cos(a) * rb) - 40;
			int by = cy + (int) Math.round(Math.sin(a) * rb) - 9;
			ButtonWidget bouton = ButtonWidget.builder(Text.translatable("branche.les_limbes." + b), bt -> {
				branche = b;
				clearChildren();
				init();
			}).dimensions(bx, by, 80, 18).build();
			bouton.active = !b.equals(branche);
			addDrawableChild(bouton);
		}
		List<String> pouvoirs = new ArrayList<>();
		for (String id : data.pouvoirs) {
			Ability a = Abilities.get(id);
			if (a != null && a.branche().equals(branche)) {
				pouvoirs.add(id);
			}
		}
		pouvoirs.sort(String::compareTo);
		int rp = Math.min(rb + 70, Math.min(width, height) / 2 - 24);
		for (int i = 0; i < pouvoirs.size(); i++) {
			final String id = pouvoirs.get(i);
			double a = -Math.PI / 2 + i * Math.PI * 2 / Math.max(1, pouvoirs.size());
			int px = cx + (int) Math.round(Math.cos(a) * rp) - 55;
			int py = cy + (int) Math.round(Math.sin(a) * rp) - 9;
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
			}).dimensions(px, py, 110, 18).build());
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		PlayerLimbesData data = ClientLimbesData.get();
		context.fill(0, 0, width, height, 0x70000000);
		int cx = width / 2;
		int cy = height / 2;
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.radial"), cx, 12, OR);
		context.drawCenteredTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.radial.aide"), cx, 24, GRIS);
		if (data.pouvoirs.isEmpty()) {
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable("ecran.les_limbes.journal.aucun_pouvoir"), cx, cy - 4, GRIS);
		} else {
			context.drawCenteredTextWithShadow(textRenderer, Text.translatable("branche.les_limbes." + branche), cx, cy - 4, OR);
		}
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
