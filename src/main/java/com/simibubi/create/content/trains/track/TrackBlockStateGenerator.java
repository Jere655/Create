package com.simibubi.create.content.trains.track;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TrackBlockStateGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return state.getValue(TrackBlock.SHAPE)
			.getModelRotation();
	}

	@Override
	public <T extends Block> ResourceLocation getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		TrackShape value = state.getValue(TrackBlock.SHAPE);
		if (value == TrackShape.NONE)
			return prov.mcLoc("block/air");
		return provCreate.asResource("block/track/" + value.getModel());
	}

}
