package fr.leslimbes.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Fragment de couronne : objet de quête qui révèle la magie royale du joueur. */
public class FragmentDeCouronneItem extends Item {
	public FragmentDeCouronneItem(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.les_limbes.fragment_de_couronne.description").formatted(Formatting.GOLD));
		tooltip.add(Text.translatable("item.les_limbes.fragment_de_couronne.conseil").formatted(Formatting.GRAY));
	}
}
