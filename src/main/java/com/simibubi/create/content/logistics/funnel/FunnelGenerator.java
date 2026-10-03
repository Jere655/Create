package com.simibubi.create.content.logistics.funnel;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.Map;

import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FunnelGenerator extends SpecialBlockStateGen {

	private String type;
	private ResourceLocation blockTexture;
	private boolean hasFilter;

	public FunnelGenerator(String type, boolean hasFilter) {
		this.type = type;
		this.hasFilter = hasFilter;
		this.blockTexture = Create.asResource("block/" + type + "_block");
	}

	@Override
	protected int getXRotation(BlockState state) {
		return state.getValue(FunnelBlock.FACING) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(FunnelBlock.FACING)) + 180;
	}

	@Override
	public <T extends Block> ResourceLocation getModel(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p,
		BlockState s) {
		String prefix = "block/funnel/";
		String powered = s.getValue(FunnelBlock.POWERED) ? "_powered" : "_unpowered";
		String closed = s.getValue(FunnelBlock.POWERED) ? "_closed" : "_open";
		String extracting = s.getValue(FunnelBlock.EXTRACTING) ? "_push" : "_pull";
		Direction facing = s.getValue(FunnelBlock.FACING);
		boolean horizontal = facing.getAxis()
			.isHorizontal();
		String parent = horizontal ? "horizontal" : hasFilter ? "vertical" : "vertical_filterless";

		return p.modLoc(prefix + type + "_funnel_" + parent + extracting + powered);
	}

	public static NonNullBiConsumer<DataGenContext<Item, FunnelItem>, RegistrateItemModelGenerator> itemModel(
		String type) {
		String prefix = "block/funnel/";
		ResourceLocation blockTexture = Create.asResource("block/" + type + "_block");
		return (c, p) -> AssetLookup.itemInherit(p, c.get(), AssetLookup.itemLoc(p, "item/" + type + "_funnel"),
			p.modLoc("block/funnel/item"), Map.of(
				TextureSlot.PARTICLE, blockTexture,
				AssetLookup.slot("block"), blockTexture,
				AssetLookup.slot("base"), p.modLoc(prefix + type + "_funnel"),
				AssetLookup.slot("direction"), p.modLoc(prefix + type + "_funnel_neutral"),
				AssetLookup.slot("redstone"), p.modLoc(prefix + type + "_funnel_unpowered")));
	}

}
