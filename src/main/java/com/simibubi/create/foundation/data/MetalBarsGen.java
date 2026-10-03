package com.simibubi.create.foundation.data;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST;

import java.util.function.Supplier;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.Create;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;


public class MetalBarsGen {

	public static <P extends IronBarsBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> barsBlockState(
		String name, boolean specialEdge) {
		return (c, p) -> {
			ResourceLocation post_ends = barsSubModel(p, name, "post_ends", specialEdge);
			ResourceLocation post = barsSubModel(p, name, "post", specialEdge);
			ResourceLocation cap = barsSubModel(p, name, "cap", specialEdge);
			ResourceLocation cap_alt = barsSubModel(p, name, "cap_alt", specialEdge);
			ResourceLocation side = barsSubModel(p, name, "side", specialEdge);
			ResourceLocation side_alt = barsSubModel(p, name, "side_alt", specialEdge);

			MultiPartGenerator parts = MultiPartGenerator.multiPart(c.get());
			parts.with(BlockStateGen.variant(p, post_ends));
			parts.with(noConnections(), BlockStateGen.variant(p, post));
			parts.with(only(NORTH), BlockStateGen.variant(p, cap));
			parts.with(only(EAST), BlockStateGen.variant(p, cap).with(BlockStateGen.rot(0, 90, false)));
			parts.with(only(SOUTH), BlockStateGen.variant(p, cap_alt));
			parts.with(only(WEST), BlockStateGen.variant(p, cap_alt).with(BlockStateGen.rot(0, 90, false)));
			parts.with(BlockStateGen.when().term(NORTH, true), BlockStateGen.variant(p, side));
			parts.with(BlockStateGen.when().term(EAST, true), BlockStateGen.variant(p, side).with(BlockStateGen.rot(0, 90, false)));
			parts.with(BlockStateGen.when().term(SOUTH, true), BlockStateGen.variant(p, side_alt));
			parts.with(BlockStateGen.when().term(WEST, true), BlockStateGen.variant(p, side_alt).with(BlockStateGen.rot(0, 90, false)));
			p.accept(parts);
		};
	}

	private static ConditionBuilder noConnections() {
		return BlockStateGen.when().term(NORTH, false).term(EAST, false).term(SOUTH, false).term(WEST, false);
	}

	private static ConditionBuilder only(
		BooleanProperty connected) {
		ConditionBuilder builder = BlockStateGen.when().term(connected, true);
		for (BooleanProperty other : new BooleanProperty[] { NORTH, EAST, SOUTH, WEST })
			if (other != connected)
				builder = builder.term(other, false);
		return builder;
	}

	private static ResourceLocation barsSubModel(RegistrateBlockModelGenerator p, String name, String suffix,
											  boolean specialEdge) {
		ResourceLocation barsTexture = p.modLoc("block/bars/" + name + "_bars");
		ResourceLocation edgeTexture = specialEdge ? p.modLoc("block/bars/" + name + "_bars_edge") : barsTexture;
		return BlockStateGen.inherit(p, name + "_" + suffix, p.modLoc("block/bars/" + suffix), mb -> mb
			.texture(AssetLookup.slot("bars"), barsTexture)
			.texture(TextureSlot.PARTICLE, barsTexture)
			.texture(AssetLookup.slot("edge"), edgeTexture));
	}

	public static BlockEntry<IronBarsBlock> createBars(String name, boolean specialEdge,
													   Supplier<DataIngredient> ingredient, MapColor color) {
		return Create.registrate().block(name + "_bars", IronBarsBlock::new)
			.addLayer(() -> () -> ChunkSectionLayer.CUTOUT_MIPPED)
			.initialProperties(() -> Blocks.IRON_BARS)
			.properties(p -> p.sound(SoundType.COPPER)
				.mapColor(color))
			.tag(AllBlockTags.WRENCH_PICKUP.tag)
			.tag(AllBlockTags.FAN_TRANSPARENT.tag)
			.transform(TagGen.pickaxeOnly())
			.blockstate(barsBlockState(name, specialEdge))
			.item()
			.model(() -> (c, p) -> {
				ResourceLocation barsTexture = p.modLoc("block/bars/" + name + "_bars");
				p.generateFlatItem(c.get(), barsTexture);
			})
			.recipe((c, p) -> p.stonecutting(ingredient.get(), RecipeCategory.DECORATIONS, c::get, 4))
			.build()
			.register();
	}

}
