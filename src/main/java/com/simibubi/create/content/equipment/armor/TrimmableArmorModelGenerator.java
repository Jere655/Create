package com.simibubi.create.content.equipment.armor;

import com.simibubi.create.Create;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Cardboard armor ships its own trim textures under create:trims/items/card_&lt;slot&gt;_trim, so the trim
 * model id is redirected into Create's namespace and vanilla drives the material dispatch.
 */
public class TrimmableArmorModelGenerator {

	public static <T extends BaseArmorItem> void generate(DataGenContext<Item, T> c, RegistrateItemModelGenerator p) {
		T item = c.get();
		ResourceLocation modelId = Create.asResource("trims/items/card_" + item.getType()
			.getName() + "_trim");
		p.generateTrimmableItem(item, item.getMaterial()
			.assetId(), modelId, false);
	}

}
