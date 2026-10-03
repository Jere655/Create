package com.simibubi.create.content.redstone.diodes;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ToggleLatchGenerator extends AbstractDiodeGenerator {

	@Override
	protected <T extends Block> List<ResourceLocation> createModels(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov) {
		String name = ctx.getName();
		List<ResourceLocation> models = new ArrayList<>(4);
		ResourceLocation off = existing("latch_off");
		ResourceLocation on = existing("latch_on");

		models.add(off);
		models.add(BlockStateGen.inherit(prov, name + "_off_powered", off,
			b -> b.texture(AssetLookup.slot("top"), texture(ctx, "powered"))));
		models.add(on);
		models.add(BlockStateGen.inherit(prov, name + "_on_powered", on,
			b -> b.texture(AssetLookup.slot("top"), texture(ctx, "powered_powering"))));

		return models;
	}

	@Override
	protected int getModelIndex(BlockState state) {
		return (state.getValue(ToggleLatchBlock.POWERING) ? 2 : 0) + (state.getValue(ToggleLatchBlock.POWERED) ? 1 : 0);
	}

}
