package com.simibubi.create.foundation.data;

import com.simibubi.create.Create;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class ModelGen {

	private static final TextureSlot OVERLAY = TextureSlot.create("overlay");

	public static ResourceLocation createOvergrown(DataGenContext<Block, ? extends Block> ctx,
		RegistrateBlockModelGenerator prov, ResourceLocation block, ResourceLocation overlay) {
		return createOvergrown(ctx, prov, block, block, block, overlay);
	}

	public static ResourceLocation createOvergrown(DataGenContext<Block, ? extends Block> ctx,
		RegistrateBlockModelGenerator prov, ResourceLocation side, ResourceLocation top, ResourceLocation bottom,
		ResourceLocation overlay) {
		return prov.getBuilder()
			.parent(Create.asResource("block/overgrown"))
			.texture(TextureSlot.PARTICLE, side)
			.texture(TextureSlot.SIDE, side)
			.texture(TextureSlot.TOP, top)
			.texture(TextureSlot.BOTTOM, bottom)
			.texture(OVERLAY, overlay)
			.build(prov.modLoc("block/" + ctx.getName()));
	}

	public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> customItemModel() {
		return b -> b.model(() -> AssetLookup::customItemModel)
			.build();
	}

	public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> customItemModel(String... path) {
		return b -> b.model(() -> AssetLookup.customBlockItemModel(path))
			.build();
	}

}
