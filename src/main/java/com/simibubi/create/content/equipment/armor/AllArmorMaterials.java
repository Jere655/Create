package com.simibubi.create.content.equipment.armor;

import java.util.Map;

import com.simibubi.create.Create;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/** 1.21.7 armor materials are values; visual equipment assets are data-driven. */
public class AllArmorMaterials {
	public static final ArmorMaterial COPPER = material(7, Map.of(
		ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 4, ArmorType.CHESTPLATE, 3, ArmorType.HELMET, 1, ArmorType.BODY, 4),
		7, SoundEvents.ARMOR_EQUIP_IRON, 0, 0, TagKey.create(Registries.ITEM, Create.asResource("copper_ingots")), "copper_diving");
	public static final ArmorMaterial CARDBOARD = material(4, Map.of(
		ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 1, ArmorType.CHESTPLATE, 1, ArmorType.HELMET, 1, ArmorType.BODY, 2),
		4, SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, TagKey.create(Registries.ITEM, Create.asResource("cardboard")), "cardboard");

	private static ArmorMaterial material(int durability, Map<ArmorType, Integer> defense, int enchantmentValue,
		Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, String asset) {
		return new ArmorMaterial(durability, defense, enchantmentValue, equipSound, toughness, knockbackResistance,
			repairIngredient, ResourceKey.create(EquipmentAssets.ROOT_ID, Create.asResource(asset)));
	}

	public static void register(net.neoforged.bus.api.IEventBus eventBus) {
	}
}
