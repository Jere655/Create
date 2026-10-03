package com.simibubi.create.content.fluids.tank;

import com.simibubi.create.content.fluids.tank.FluidTankBlock.Shape;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FluidTankGenerator extends SpecialBlockStateGen {

	private String prefix;

	public FluidTankGenerator() {
		this("");
	}

	public FluidTankGenerator(String prefix) {
		this.prefix = prefix;
	}

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return 0;
	}

	@Override
	public <T extends Block> ResourceLocation getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		Boolean top = state.getValue(FluidTankBlock.TOP);
		Boolean bottom = state.getValue(FluidTankBlock.BOTTOM);
		Shape shape = state.getValue(FluidTankBlock.SHAPE);

		String shapeName = "middle";
		if (top && bottom)
			shapeName = "single";
		else if (top)
			shapeName = "top";
		else if (bottom)
			shapeName = "bottom";

		String modelName = shapeName + (shape == Shape.PLAIN ? "" : "_" + shape.getSerializedName());

		if (!prefix.isEmpty())
			return BlockStateGen.inherit(prov, prefix + modelName, prov.modLoc("block/fluid_tank/block_" + modelName),
				b -> b.texture(AssetLookup.slot("0"), prov.modLoc("block/" + prefix + "casing"))
					.texture(AssetLookup.slot("1"), prov.modLoc("block/" + prefix + "fluid_tank"))
					.texture(AssetLookup.slot("3"), prov.modLoc("block/" + prefix + "fluid_tank_window"))
					.texture(AssetLookup.slot("4"), prov.modLoc("block/" + prefix + "casing"))
					.texture(AssetLookup.slot("5"), prov.modLoc("block/" + prefix + "fluid_tank_window_single"))
					.texture(TextureSlot.PARTICLE, prov.modLoc("block/" + prefix + "fluid_tank")));

		return AssetLookup.partialBaseModel(ctx, prov, modelName);
	}

}
