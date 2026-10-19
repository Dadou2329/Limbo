package fr.leslimbes.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;

/**
 * Bouclier du Souverain : bouclier invoqué, indestructible. Quand il bloque un coup, l'assaillant est repoussé
 * (rempart, voir {@link fr.leslimbes.power.ArsenalDuSouverain}).
 */
public class BouclierDuSouverainItem extends ShieldItem {
	public BouclierDuSouverainItem(Settings settings) {
		super(settings);
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return this.getTranslationKey();
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}
}
