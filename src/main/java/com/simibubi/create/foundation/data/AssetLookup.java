package com.simibubi.create.foundation.data;

import java.util.Map;
import java.util.function.Function;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class AssetLookup {

	/**
	 * Custom block models packaged with other partials. Example:
	 * models/block/schematicannon/block.json <br>
	 * <br>
	 * Adding "powered", "vertical" will look for /block_powered_vertical.json
	 */
	public static ResourceLocation partialBaseModel(DataGenContext<?, ?> ctx, RegistrateBlockModelGenerator prov,
		String... suffix) {
		String string = "/block";
		for (String suf : suffix)
			if (!suf.isEmpty())
				string += "_" + suf;
		final String location = "block/" + ctx.getName() + string;
		return prov.modLoc(location);
	}

	/**
	 * Custom block model from models/block/x.json
	 */
	public static ResourceLocation standardModel(DataGenContext<?, ?> ctx, RegistrateBlockModelGenerator prov) {
		return prov.modLoc("block/" + ctx.getName());
	}

	/**
	 * Generate item model inheriting from a seperate model in
	 * models/block/x/item.json
	 */
	public static <I extends BlockItem> void customItemModel(DataGenContext<Item, I> ctx,
		RegistrateItemModelGenerator prov) {
		prov.generateBlockItem(ctx.get(), "/item");
	}

	/**
	 * Generate item model inheriting from a seperate model in
	 * models/block/folders[0]/folders[1]/.../item.json "_" will be replaced by the
	 * item name
	 */
	public static <I extends BlockItem> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelGenerator> customBlockItemModel(
		String... folders) {
		return (c, p) -> p.createWithExistingModel(c.get(), partialPath(c, p, folders));
	}

	public static <I extends Item> NonNullBiConsumer<DataGenContext<Item, I>, RegistrateItemModelGenerator> customGenericItemModel(
		String... folders) {
		return (c, p) -> p.createWithExistingModel(c.get(), partialPath(c, p, folders));
	}

	private static ResourceLocation partialPath(DataGenContext<Item, ?> c, RegistrateItemModelGenerator p, String... folders) {
		String path = "block";
		for (String string : folders)
			path += "/" + ("_".equals(string) ? c.getName() : string);
		return p.modLoc(path);
	}

	public static Function<BlockState, ResourceLocation> forPowered(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov) {
		return state -> state.getValue(BlockStateProperties.POWERED) ? partialBaseModel(ctx, prov, "powered")
			: partialBaseModel(ctx, prov);
	}

	public static Function<BlockState, ResourceLocation> forPowered(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov, String path) {
		return state -> prov.modLoc("block/" + path + (state.getValue(BlockStateProperties.POWERED) ? "_powered" : ""));
	}

	public static Function<BlockState, ResourceLocation> withIndicator(DataGenContext<?, ?> ctx,
		RegistrateBlockModelGenerator prov, Function<BlockState, ResourceLocation> baseModelFunc, IntegerProperty property) {
		return state -> {
			ResourceLocation baseModel = baseModelFunc.apply(state);
			Integer integer = state.getValue(property);
			// The indicator parent already supplies its texture slots; model choice is
			// now expressed by its resource location rather than a Forge builder.
			return baseModel.withSuffix("_" + integer);
		};
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> existingItemModel() {
		return (c, p) -> p.createWithExistingModel(c.get(), p.modLoc("item/" + c.getName()));
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> itemModel(String name) {
		return (c, p) -> p.createWithExistingModel(c.get(), p.modLoc("item/" + name));
	}

	public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> itemModelWithPartials() {
		return (c, p) -> p.createWithExistingModel(c.get(), p.modLoc("item/" + c.getName() + "/item"));
	}

	/** Forge's item ModelProvider resolved bare names against models/item/; keep that convention. */
	public static ResourceLocation itemLoc(RegistrateItemModelGenerator prov, String path) {
		return prov.modLoc(path.startsWith("item/") ? path : "item/" + path);
	}

	/**
	 * Item model inheriting from {@code parent} and overriding the given texture slots. Emits
	 * models/item/&lt;name&gt;.json and points the item at it, matching the former
	 * {@code withExistingParent(...).texture(...)} chains.
	 */
	public static void itemInherit(DataGenContext<Item, ?> ctx, RegistrateItemModelGenerator prov, ResourceLocation parent,
		Map<TextureSlot, ResourceLocation> textures) {
		itemInherit(prov, ctx.get(), itemLoc(prov, ctx.getName()), parent, textures);
	}

	public static void itemInherit(RegistrateItemModelGenerator prov, Item item, ResourceLocation modelLocation,
		ResourceLocation parent, Map<TextureSlot, ResourceLocation> textures) {
		if (textures.isEmpty()) {
			prov.createWithExistingModel(item, parent);
			return;
		}
		TextureMapping mapping = new TextureMapping();
		textures.forEach(mapping::put);
		prov.createInheritingModel(modelLocation, parent, mapping, textures.keySet()
			.toArray(TextureSlot[]::new));
		prov.createWithExistingModel(item, modelLocation);
	}

	/** Texture slot lookup by Create's hand-written model keys ("bracket", "plate", ...). */
	public static TextureSlot slot(String id) {
		return TextureSlot.create(id);
	}

}
