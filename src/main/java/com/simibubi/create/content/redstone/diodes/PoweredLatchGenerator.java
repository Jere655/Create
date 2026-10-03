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

public class PoweredLatchGenerator extends AbstractDiodeGenerator {

	@Override
	protected <T extends Block> List<ResourceLocation> createModels(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov) {
		List<ResourceLocation> models = new ArrayList<>(2);
		String name = ctx.getName();
		ResourceLocation off = existing("latch_off");
		ResourceLocation on = existing("latch_on");

		models.add(BlockStateGen.inherit(prov, name, off,
			b -> b.texture(AssetLookup.slot("top"), texture(ctx, "idle"))));
		models.add(BlockStateGen.inherit(prov, name + "_powered", on,
			b -> b.texture(AssetLookup.slot("top"), texture(ctx, "powering"))));

		return models;
	}

	@Override
	protected int getModelIndex(BlockState state) {
		return state.getValue(PoweredLatchBlock.POWERING) ? 1 : 0;
	}

}
