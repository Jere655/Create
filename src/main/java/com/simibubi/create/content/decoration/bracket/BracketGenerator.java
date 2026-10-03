package com.simibubi.create.content.decoration.bracket;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.DirectionalAxisBlockStateGen;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class BracketGenerator extends DirectionalAxisBlockStateGen {

	private String material;

	public BracketGenerator(String material) {
		this.material = material;
	}

	@Override
	public <T extends Block> String getModelPrefix(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		return "";
	}

	@Override
	public <T extends Block> ResourceLocation getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		String type = state.getValue(BracketBlock.TYPE)
			.getSerializedName();
		boolean vertical = state.getValue(BracketBlock.FACING)
			.getAxis()
			.isVertical();

		String path = "block/bracket/" + type + "/" + (vertical ? "ground" : "wall");

		return BlockStateGen.inherit(prov, path + "_" + material, prov.modLoc(path), b -> b
			.texture(AssetLookup.slot("bracket"), prov.modLoc("block/bracket_" + material))
			.texture(AssetLookup.slot("plate"), prov.modLoc("block/bracket_plate_" + material)));
	}

	public static <I extends BlockItem, P> NonNullFunction<ItemBuilder<I, P>, P> itemModel(String material) {
		return b -> b.model(() -> (c, p) -> AssetLookup.itemInherit(c, p, p.modLoc("block/bracket/item"), Map.of(
			AssetLookup.slot("bracket"), p.modLoc("block/bracket_" + material),
			AssetLookup.slot("plate"), p.modLoc("block/bracket_plate_" + material))))
			.build();
	}

}
