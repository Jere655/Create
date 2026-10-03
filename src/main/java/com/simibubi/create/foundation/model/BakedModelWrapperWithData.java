package com.simibubi.create.foundation.model;

import java.util.List;
import java.util.function.UnaryOperator;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;

/**
 * Shared world-aware wrapper for Create's dynamic block models.
 *
 * ModelData is still provided by NeoForge in 1.21.7, but is read from the
 * render-world in BlockStateModel.collectParts rather than passed to BakedModel.
 */
public abstract class BakedModelWrapperWithData extends DelegateBlockStateModel {

	public BakedModelWrapperWithData(BlockStateModel originalModel) {
		super(originalModel);
	}

	@Override
	public final void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
		List<BlockModelPart> parts) {
		ModelData blockEntityData = world.getModelData(pos);
		Builder builder = ModelData.builder();
		if (delegate instanceof BakedModelWrapperWithData)
			((BakedModelWrapperWithData) delegate).gatherModelData(builder, world, pos, state, blockEntityData);
		gatherModelData(builder, world, pos, state, blockEntityData);
		collectParts(world, pos, state, random, builder.build(), parts);
	}

	protected abstract ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world,
		BlockPos pos, BlockState state, ModelData blockEntityData);

	protected abstract void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
		ModelData data, List<BlockModelPart> parts);

	protected final void collectOriginalParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
		List<BlockModelPart> parts) {
		delegate.collectParts(world, pos, state, random, parts);
	}

	protected static BlockModelPart transformPart(BlockModelPart part, UnaryOperator<BakedQuad> transformer) {
		QuadCollection.Builder quads = new QuadCollection.Builder();
		for (BakedQuad quad : part.getQuads(null))
			quads.addUnculledFace(transformer.apply(quad));
		for (Direction direction : Direction.values())
			for (BakedQuad quad : part.getQuads(direction))
				quads.addCulledFace(direction, transformer.apply(quad));
		return new SimpleModelWrapper(quads.build(), part.useAmbientOcclusion(), part.particleIcon());
	}

}
