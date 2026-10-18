package fr.leslimbes.item;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Épée du Souverain : arme invoquée par la magie royale. Elle ne s'use pas, ne peut pas être lâchée
 * (voir {@link fr.leslimbes.power.ArsenalDuSouverain}) et frappe plus fort au troisième coup d'un enchaînement.
 */
public class EpeeDuSouverainItem extends SwordItem {
	public static final ToolMaterial MATERIAU = new ToolMaterial() {
		@Override
		public int getDurability() {
			return 2000;
		}

		@Override
		public float getMiningSpeedMultiplier() {
			return 8.0F;
		}

		@Override
		public float getAttackDamage() {
			return 5.0F;
		}

		@Override
		public TagKey<Block> getInverseTag() {
			return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
		}

		@Override
		public int getEnchantability() {
			return 0;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(Items.AIR);
		}
	};

	public EpeeDuSouverainItem(Settings settings) {
		super(MATERIAU, settings.attributeModifiers(SwordItem.createAttributeModifiers(MATERIAU, 3, -2.2F)));
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		// Une arme invoquée ne s'use pas.
		return true;
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canBeNested() {
		return false;
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.les_limbes.epee_du_souverain.description").formatted(Formatting.AQUA));
		tooltip.add(Text.translatable("item.les_limbes.epee_du_souverain.combo").formatted(Formatting.GRAY));
	}
}
