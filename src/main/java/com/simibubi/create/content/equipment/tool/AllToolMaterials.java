package com.simibubi.create.content.equipment.tool;

import com.simibubi.create.Create;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class AllToolMaterials {
	private static final TagKey<Item> CARDBOARD_REPAIR_ITEMS =
		TagKey.create(Registries.ITEM, Create.asResource("cardboard_tool_materials"));

	public static final ToolMaterial CARDBOARD = new ToolMaterial(
		BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0, 1, 2, 1, CARDBOARD_REPAIR_ITEMS
	);

	private AllToolMaterials() {}
}
