package com.simibubi.create.content.decoration.girder;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.createmod.catnip.data.Iterate;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class GirderBlockStateGenerator {

	public static void blockStateWithShaft(DataGenContext<Block, GirderEncasedShaftBlock> c,
		RegistrateBlockModelGenerator p) {
		MultiPartGenerator parts = MultiPartGenerator.multiPart(c.get());
		ResourceLocation base = AssetLookup.partialBaseModel(c, p);

		parts.with(BlockStateGen.when()
			.term(GirderEncasedShaftBlock.HORIZONTAL_AXIS, Axis.Z), BlockStateGen.variant(p, base));
		parts.with(BlockStateGen.when()
			.term(GirderEncasedShaftBlock.HORIZONTAL_AXIS, Axis.X),
			BlockStateGen.variant(p, base, BlockStateGen.rot(0, 90, false)));
		parts.with(BlockStateGen.when()
			.term(GirderEncasedShaftBlock.TOP, true),
			BlockStateGen.variant(p, AssetLookup.partialBaseModel(c, p, "top")));
		parts.with(BlockStateGen.when()
			.term(GirderEncasedShaftBlock.BOTTOM, true),
			BlockStateGen.variant(p, AssetLookup.partialBaseModel(c, p, "bottom")));

		p.accept(parts);
	}

	public static void blockState(DataGenContext<Block, GirderBlock> c, RegistrateBlockModelGenerator p) {
		MultiPartGenerator parts = MultiPartGenerator.multiPart(c.get());
		ResourceLocation top = AssetLookup.partialBaseModel(c, p, "top");
		ResourceLocation bottom = AssetLookup.partialBaseModel(c, p, "bottom");

		parts.with(BlockStateGen.when()
			.term(GirderBlock.X, false)
			.term(GirderBlock.Z, false), BlockStateGen.variant(p, AssetLookup.partialBaseModel(c, p, "pole")));
		parts.with(BlockStateGen.when()
			.term(GirderBlock.X, true), BlockStateGen.variant(p, AssetLookup.partialBaseModel(c, p, "x")));
		parts.with(BlockStateGen.when()
			.term(GirderBlock.Z, true), BlockStateGen.variant(p, AssetLookup.partialBaseModel(c, p, "z")));

		for (boolean x : Iterate.trueAndFalse) {
			parts.with(BlockStateGen.when()
				.term(GirderBlock.TOP, true)
				.term(GirderBlock.X, x)
				.term(GirderBlock.Z, !x), BlockStateGen.variant(p, top));
			parts.with(BlockStateGen.when()
				.term(GirderBlock.BOTTOM, true)
				.term(GirderBlock.X, x)
				.term(GirderBlock.Z, !x), BlockStateGen.variant(p, bottom));
		}

		parts.with(BlockStateGen.when()
			.term(GirderBlock.X, true)
			.term(GirderBlock.Z, true), BlockStateGen.variant(p, AssetLookup.partialBaseModel(c, p, "cross")));

		p.accept(parts);
	}

}
