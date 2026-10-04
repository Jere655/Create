package com.simibubi.create.content.kinetics.belt;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class BeltModel extends BakedModelWrapperWithData {

	public static final ModelProperty<CasingType> CASING_PROPERTY = new ModelProperty<>();
	public static final ModelProperty<Boolean> COVER_PROPERTY = new ModelProperty<>();

	private static final SpriteShiftEntry SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;

	public BeltModel(BlockStateModel template) {
		super(template);
	}

	@Override
	protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData blockEntityData) {
		// Belt model doesn't need custom model data gathering
		return builder;
	}

	@Override
	protected void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
		ModelData data, List<BlockModelPart> parts) {
		// Delegate to the original model's collectParts
		collectOriginalParts(world, pos, state, random, parts);
	}

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		if (!data.has(CASING_PROPERTY))
			return super.getParticleIcon(data);
		CasingType type = data.get(CASING_PROPERTY);
		if (type == CasingType.NONE || type == CasingType.BRASS)
			return super.getParticleIcon(data);
		return AllSpriteShifts.ANDESITE_CASING.getOriginal();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
		List<BakedQuad> quads = super.getQuads(state, side, rand, extraData, renderType);
		if (!extraData.has(CASING_PROPERTY))
			return quads;

		boolean cover = extraData.get(COVER_PROPERTY);
		CasingType type = extraData.get(CASING_PROPERTY);
		boolean brassCasing = type == CasingType.BRASS;

		if (type == CasingType.NONE || brassCasing && !cover)
			return quads;

		quads = new ArrayList<>(quads);

		if (cover) {
			boolean alongX = state.getValue(BeltBlock.HORIZONTAL_FACING)
				.getAxis() == Axis.X;
			BlockStateModel coverModel =
				(brassCasing ? alongX ? AllPartialModels.BRASS_BELT_COVER_X : AllPartialModels.BRASS_BELT_COVER_Z
					: alongX ? AllPartialModels.ANDESITE_BELT_COVER_X : AllPartialModels.ANDESITE_BELT_COVER_Z).get();
			quads.addAll(coverModel.getQuads(state, side, rand, extraData, renderType));
		}

		if (brassCasing)
			return quads;

		for (int i = 0; i < quads.size(); i++) {
			BakedQuad quad = quads.get(i);
			TextureAtlasSprite original = quad.getSprite();
			if (original != SPRITE_SHIFT.getOriginal())
				continue;

			BakedQuad newQuad = BakedQuadHelper.clone(quad);
			int[] vertexData = newQuad.getVertices();

			for (int vertex = 0; vertex < 4; vertex++) {
				float u = BakedQuadHelper.getU(vertexData, vertex);
				float v = BakedQuadHelper.getV(vertexData, vertex);
				BakedQuadHelper.setU(vertexData, vertex, SPRITE_SHIFT.getTargetU(u));
				BakedQuadHelper.setV(vertexData, vertex, SPRITE_SHIFT.getTargetV(v));
			}

			quads.set(i, newQuad);
		}

		return quads;
	}

}
