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

public class BrassDiodeGenerator extends AbstractDiodeGenerator {

	@Override
	protected <T extends Block> List<ResourceLocation> createModels(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov) {
		List<ResourceLocation> models = new ArrayList<>(4);
		String name = ctx.getName();
		ResourceLocation template = existing(name);

		models.add(template);
		models.add(BlockStateGen.inherit(prov, name + "_powered", template,
			b -> b.texture(AssetLookup.slot("top"), texture(ctx, "powered"))));
		models.add(BlockStateGen.inherit(prov, name + "_powering", template, b -> b
			.texture(AssetLookup.slot("torch"), poweredTorch())
			.texture(AssetLookup.slot("top"), texture(ctx, "powering"))));
		models.add(BlockStateGen.inherit(prov, name + "_powered_powering", template, b -> b
			.texture(AssetLookup.slot("torch"), poweredTorch())
			.texture(AssetLookup.slot("top"), texture(ctx, "powered_powering"))));

		return models;
	}

	@Override
	protected int getModelIndex(BlockState state) {
		return (state.getValue(BrassDiodeBlock.POWERING) ^ state.getValue(BrassDiodeBlock.INVERTED) ? 2 : 0)
			+ (state.getValue(BrassDiodeBlock.POWERED) ? 1 : 0);
	}

}
