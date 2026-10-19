package fr.leslimbes.item;

import fr.leslimbes.LesLimbes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Lance du Souverain : arme invoquée à longue allonge (un bloc et demi de plus), plus lente que l'épée.
 * Elle permet la charge de lance et profite de l'affûtage de la forge royale.
 */
public class LanceDuSouverainItem extends SwordItem {
	public LanceDuSouverainItem(Settings settings) {
		super(EpeeDuSouverainItem.MATERIAU, settings.attributeModifiers(attributs(0)));
	}

	public static AttributeModifiersComponent attributs(int affutage) {
		return AttributeModifiersComponent.builder()
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 2.0 + EpeeDuSouverainItem.MATERIAU.getAttackDamage() + affutage,
						EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
				.add(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.6, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
				.add(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(LesLimbes.id("portee_de_lance"), 1.5, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
				.build();
	}

	public static void appliquerAffutage(ItemStack stack, int niveau) {
		stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributs(niveau));
		if (niveau > 0) {
			stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("item.les_limbes.epee_du_souverain.affutee", niveau).formatted(Formatting.AQUA))));
		}
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		return true;
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}
}
