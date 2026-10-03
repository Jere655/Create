package com.simibubi.create.content.equipment.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

/** Common component-backed armor item base for Create's custom armor behavior. */
public class BaseArmorItem extends Item {
	protected final ResourceLocation textureLoc;
	protected final ArmorMaterial material;
	protected final ArmorType type;

	public BaseArmorItem(ArmorMaterial material, ArmorType type, Properties properties, ResourceLocation textureLoc) {
		super(properties.humanoidArmor(material, type).stacksTo(1));
		this.textureLoc = textureLoc;
		this.material = material;
		this.type = type;
	}

	public ArmorMaterial getMaterial() {
		return material;
	}

	public ArmorType getType() {
		return type;
	}
}
