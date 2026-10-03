package com.simibubi.create.foundation.data;

import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.content.decoration.palettes.ConnectedGlassBlock;
import com.simibubi.create.content.decoration.palettes.ConnectedGlassPaneBlock;
import com.simibubi.create.content.decoration.palettes.GlassPaneBlock;
import com.simibubi.create.content.decoration.palettes.WindowBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.GlassPaneCTBehaviour;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.common.Tags;

public class WindowGen {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	private static Properties glassProperties(Properties p) {
		return p.isValidSpawn(WindowGen::never)
			.isRedstoneConductor(WindowGen::never)
			.isSuffocating(WindowGen::never)
			.isViewBlocking(WindowGen::never);
	}

	private static boolean never(BlockState p_235436_0_, BlockGetter p_235436_1_, BlockPos p_235436_2_) {
		return false;
	}

	private static Boolean never(BlockState p_235427_0_, BlockGetter p_235427_1_, BlockPos p_235427_2_,
								 EntityType<?> p_235427_3_) {
		return false;
	}

	public static BlockEntry<WindowBlock> woodenWindowBlock(WoodType woodType, Block planksBlock) {
		return woodenWindowBlock(woodType, planksBlock, () -> () -> ChunkSectionLayer.CUTOUT_MIPPED, false);
	}

	public static BlockBuilder<WindowBlock, CreateRegistrate> randomisedWindowBlock(String name,
																					Supplier<? extends ItemLike> ingredient, Supplier<Supplier<ChunkSectionLayer>> renderType, boolean translucent,
																					Supplier<MapColor> color) {
		ResourceLocation end_texture = Create.asResource(palettesDir() + name + "_end");
		ResourceLocation side_texture = Create.asResource(palettesDir() + name);
		Function<Integer, ResourceLocation> ends = i -> Create.asResource(palettesDir() + name + "_" + i + "_end");
		return windowBlock(name, ingredient, null, renderType, translucent, n -> end_texture, n -> side_texture, color)
			.blockstate((c, p) -> p.create(c.get(), BlockModelGenerators.variants(
				cubeColumnVariant(p, c.getName() + "_1", side_texture, ends.apply(1)),
				cubeColumnVariant(p, c.getName() + "_2", side_texture, ends.apply(2)),
				cubeColumnVariant(p, c.getName() + "_3", side_texture, ends.apply(3)),
				cubeColumnVariant(p, c.getName() + "_4", side_texture, ends.apply(4)))))
			.item()
			.model(() -> (c, p) -> p.generateWithTemplate(c.get(), ModelTemplates.CUBE_COLUMN,
				TextureMapping.column(side_texture, ends.apply(1))))
			.build();
	}

	private static Variant cubeColumnVariant(RegistrateBlockModelGenerator p, String name, ResourceLocation side,
											   ResourceLocation end) {
		return BlockModelGenerators.plainModel(
			p.createModel(p.modLoc("block/" + name), ModelTemplates.CUBE_COLUMN, TextureMapping.column(side, end)));
	}

	public static BlockEntry<WindowBlock> customWindowBlock(String name, Supplier<? extends ItemLike> ingredient,
															Supplier<CTSpriteShiftEntry> ct, Supplier<Supplier<ChunkSectionLayer>> renderType, boolean translucent,
															Supplier<MapColor> color) {
		NonNullFunction<String, ResourceLocation> end_texture = n -> Create.asResource(palettesDir() + name + "_end");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, ingredient, ct, renderType, translucent, end_texture, side_texture, color).register();
	}

	public static BlockEntry<WindowBlock> woodenWindowBlock(WoodType woodType, Block planksBlock,
															Supplier<Supplier<ChunkSectionLayer>> renderType, boolean translucent) {
		String woodName = woodType.name();
		String name = woodName + "_window";
		NonNullFunction<String, ResourceLocation> end_texture =
			$ -> ResourceLocation.withDefaultNamespace("block/" + woodName + "_planks");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, () -> planksBlock, () -> AllSpriteShifts.getWoodenWindow(woodType), renderType,
			translucent, end_texture, side_texture, planksBlock::defaultMapColor).register();
	}

	public static BlockBuilder<WindowBlock, CreateRegistrate> windowBlock(String name,
																		  Supplier<? extends ItemLike> ingredient, Supplier<CTSpriteShiftEntry> ct,
																		  Supplier<Supplier<ChunkSectionLayer>> renderType, boolean translucent,
																		  NonNullFunction<String, ResourceLocation> endTexture, NonNullFunction<String, ResourceLocation> sideTexture,
																		  Supplier<MapColor> color) {
		return REGISTRATE.block(name, p -> new WindowBlock(p, translucent))
			.onRegister(ct == null ? $ -> {
			} : connectedTextures(() -> new HorizontalCTBehaviour(ct.get())))
			.addLayer(renderType)
			.recipe((c, p) -> p.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 2)
				.pattern(" # ")
				.pattern("#X#")
				.define('#', ingredient.get())
					.define('X', Tags.Items.GLASS_BLOCKS_COLORLESS)
				.unlockedBy("has_ingredient", p.has(ingredient.get()))
				.save(p))
			.initialProperties(() -> Blocks.GLASS)
			.properties(WindowGen::glassProperties)
			.properties(p -> p.mapColor(color.get()))
			.loot((t, g) -> t.dropWhenSilkTouch(g))
			.blockstate((c, p) -> p.create(c.get(),
				p.createModel(p.modLoc("block/" + c.getName()), ModelTemplates.CUBE_COLUMN,
					TextureMapping.column(sideTexture.apply(c.getName()), endTexture.apply(c.getName())))))
			.tag(BlockTags.IMPERMEABLE)
			.simpleItem();
	}

	public static BlockEntry<ConnectedGlassBlock> framedGlass(String name,
															  Supplier<ConnectedTextureBehaviour> behaviour) {
		return REGISTRATE.block(name, ConnectedGlassBlock::new)
			.onRegister(connectedTextures(behaviour))
			.addLayer(() -> () -> ChunkSectionLayer.CUTOUT)
			.initialProperties(() -> Blocks.GLASS)
			.properties(WindowGen::glassProperties)
			.loot((t, g) -> t.dropWhenSilkTouch(g))
				.recipe((c, p) -> p.stonecutting(DataIngredient.tag(Tags.Items.GLASS_BLOCKS_COLORLESS),
				RecipeCategory.BUILDING_BLOCKS, c::get))
			.blockstate((c, p) -> BlockStateGen.cubeAll(c, p, "palettes/", "framed_glass"))
				.tag(Tags.Blocks.GLASS_BLOCKS_COLORLESS, BlockTags.IMPERMEABLE)
			.item()
				.tag(Tags.Items.GLASS_BLOCKS_COLORLESS)
			.model(() -> (c, p) -> p.generateWithTemplate(c.get(), ModelTemplates.CUBE_COLUMN,
				TextureMapping.column(p.modLoc(palettesDir() + c.getName()), p.modLoc("block/palettes/framed_glass"))))
			.build()
			.register();
	}

	public static BlockEntry<ConnectedGlassPaneBlock> framedGlassPane(String name, Supplier<? extends Block> parent,
																	  Supplier<CTSpriteShiftEntry> ctshift) {
		ResourceLocation sideTexture = Create.asResource(palettesDir() + "framed_glass");
		ResourceLocation itemSideTexture = Create.asResource(palettesDir() + name);
		ResourceLocation topTexture = Create.asResource(palettesDir() + "framed_glass_pane_top");
		Supplier<Supplier<ChunkSectionLayer>> renderType = () -> () -> ChunkSectionLayer.CUTOUT_MIPPED;
		return connectedGlassPane(name, parent, ctshift, sideTexture, itemSideTexture, topTexture, renderType, true)
			.register();
	}

	public static BlockBuilder<ConnectedGlassPaneBlock, CreateRegistrate> customWindowPane(String name,
																						   Supplier<? extends Block> parent, Supplier<CTSpriteShiftEntry> ctshift,
																						   Supplier<Supplier<ChunkSectionLayer>> renderType) {
		ResourceLocation topTexture = Create.asResource(palettesDir() + name + "_pane_top");
		ResourceLocation sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, parent, ctshift, sideTexture, sideTexture, topTexture, renderType, false);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(WoodType woodType,
																	   Supplier<? extends Block> parent) {
		return woodenWindowPane(woodType, parent, () -> () -> ChunkSectionLayer.CUTOUT_MIPPED);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(WoodType woodType,
																	   Supplier<? extends Block> parent, Supplier<Supplier<ChunkSectionLayer>> renderType) {
		String woodName = woodType.name();
		String name = woodName + "_window";
		ResourceLocation topTexture = ResourceLocation.withDefaultNamespace("block/" + woodName + "_planks");
		ResourceLocation sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, parent, () -> AllSpriteShifts.getWoodenWindow(woodType), sideTexture,
			sideTexture, topTexture, renderType, false).register();
	}

	public static BlockEntry<GlassPaneBlock> standardGlassPane(String name, Supplier<? extends Block> parent,
															   ResourceLocation sideTexture, ResourceLocation topTexture, Supplier<Supplier<ChunkSectionLayer>> renderType) {
		NonNullBiConsumer<DataGenContext<Block, GlassPaneBlock>, RegistrateBlockModelGenerator> stateProvider =
			(c, p) -> p.generatePaneBlock(c.get(), sideTexture, topTexture);
		return glassPane(name, parent, sideTexture, topTexture, GlassPaneBlock::new, renderType, $ -> {
		}, stateProvider, true).register();
	}

	private static BlockBuilder<ConnectedGlassPaneBlock, CreateRegistrate> connectedGlassPane(String name,
																							  Supplier<? extends Block> parent, Supplier<CTSpriteShiftEntry> ctshift, ResourceLocation sideTexture,
																							  ResourceLocation itemSideTexture, ResourceLocation topTexture, Supplier<Supplier<ChunkSectionLayer>> renderType, boolean colorless) {
		NonNullConsumer<? super ConnectedGlassPaneBlock> connectedTextures = ctshift == null ? $ -> {
		} : connectedTextures(() -> new GlassPaneCTBehaviour(ctshift.get()));
		String CGPparents = "block/connected_glass_pane/";
		String prefix = name + "_pane_";

		Function<RegistrateBlockModelGenerator, ResourceLocation> post =
			getPaneModelProvider(CGPparents, prefix, "post", sideTexture, topTexture),
			side = getPaneModelProvider(CGPparents, prefix, "side", sideTexture, topTexture),
			sideAlt = getPaneModelProvider(CGPparents, prefix, "side_alt", sideTexture, topTexture),
			noSide = getPaneModelProvider(CGPparents, prefix, "noside", sideTexture, topTexture),
			noSideAlt = getPaneModelProvider(CGPparents, prefix, "noside_alt", sideTexture, topTexture);

		NonNullBiConsumer<DataGenContext<Block, ConnectedGlassPaneBlock>, RegistrateBlockModelGenerator> stateProvider =
			(c, p) -> p.generatePaneBlock(c.get(), p.variant(post.apply(p)), p.variant(side.apply(p)),
				p.variant(sideAlt.apply(p)), p.variant(noSide.apply(p)), p.variant(noSideAlt.apply(p)));

		return glassPane(name, parent, itemSideTexture, topTexture, ConnectedGlassPaneBlock::new, renderType,
			connectedTextures, stateProvider, colorless);
	}

	private static Function<RegistrateBlockModelGenerator, ResourceLocation> getPaneModelProvider(String CGPparents,
																							  String prefix, String partial, ResourceLocation sideTexture, ResourceLocation topTexture) {
		return p -> BlockStateGen.inherit(p, prefix + partial, Create.asResource(CGPparents + partial),
			b -> b.texture(TextureSlot.PANE, sideTexture)
				.texture(TextureSlot.EDGE, topTexture));
	}

	private static <G extends GlassPaneBlock> BlockBuilder<G, CreateRegistrate> glassPane(String name,
																						  Supplier<? extends Block> parent, ResourceLocation sideTexture, ResourceLocation topTexture,
																						  NonNullFunction<Properties, G> factory, Supplier<Supplier<ChunkSectionLayer>> renderType,
																						  NonNullConsumer<? super G> connectedTextures,
																						  NonNullBiConsumer<DataGenContext<Block, G>, RegistrateBlockModelGenerator> stateProvider, boolean colorless) {
		name += "_pane";


		ItemBuilder<BlockItem, BlockBuilder<G, CreateRegistrate>> itemBuilder = REGISTRATE.block(name, factory)
			.onRegister(connectedTextures)
			.addLayer(renderType)
			.initialProperties(() -> Blocks.GLASS_PANE)
			.properties(p -> p.mapColor(parent.get()
				.defaultMapColor()))
			.blockstate(stateProvider)
			.recipe((c, p) -> {
				p.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 16)
					.pattern("###")
					.pattern("###")
					.define('#', parent.get())
					.unlockedBy("has_ingredient", p.has(parent.get()))
					.save(p);
				if (colorless)
					p.stonecutting(DataIngredient.tag(Tags.Items.GLASS_PANES_COLORLESS), RecipeCategory.BUILDING_BLOCKS,
						c::get);
			})
			.loot((t, g) -> t.dropWhenSilkTouch(g))
			.item();

		if (colorless)
			itemBuilder.tag(Tags.Items.GLASS_PANES, Tags.Items.GLASS_PANES_COLORLESS);
		else
			itemBuilder.tag(Tags.Items.GLASS_PANES);

		BlockBuilder<G, CreateRegistrate> blockBuilder = itemBuilder
			.model(() -> (c, p) -> p.generateFlatItem(c.get(), sideTexture))
			.build();

		if (colorless)
			blockBuilder.tag(Tags.Blocks.GLASS_PANES, Tags.Blocks.GLASS_PANES_COLORLESS);
		else
			blockBuilder.tag(Tags.Blocks.GLASS_PANES);

		return blockBuilder;
	}

	private static String palettesDir() {
		return "block/palettes/";
	}

}
