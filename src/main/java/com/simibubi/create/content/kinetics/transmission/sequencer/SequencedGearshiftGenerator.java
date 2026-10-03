package com.simibubi.create.content.kinetics.transmission.sequencer;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SequencedGearshiftGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.getValue(SequencedGearshiftBlock.VERTICAL) ? 90 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return state.getValue(SequencedGearshiftBlock.HORIZONTAL_AXIS) == Axis.X ? 90 : 0;
	}

	@Override
	public <T extends Block> ResourceLocation getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		String variant = "idle";
		int seq = state.getValue(SequencedGearshiftBlock.STATE);
		if (seq > 0)
			variant = "seq_" + seq;
		return prov.modLoc("block/" + ctx.getName() + "/" + variant);
	}

}
