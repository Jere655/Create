package com.simibubi.create.foundation.block.connected;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;
import net.neoforged.neoforge.model.data.ModelProperty;

public class CTModel extends BakedModelWrapperWithData {

	private static final ModelProperty<CTData> CT_PROPERTY = new ModelProperty<>();

	private final ConnectedTextureBehaviour behaviour;

	public CTModel(BlockStateModel originalModel, ConnectedTextureBehaviour behaviour) {
		super(originalModel);
		this.behaviour = behaviour;
	}

	@Override
	protected ModelData.Builder gatherModelData(Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state,
		ModelData blockEntityData) {
		return builder.with(CT_PROPERTY, createCTData(world, pos, state));
	}

	protected CTData createCTData(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		CTData data = new CTData();
		MutableBlockPos mutablePos = new MutableBlockPos();
		for (Direction face : Iterate.directions) {
			BlockState actualState = world.getBlockState(pos);
			if (!behaviour.buildContextForOccludedDirections()
				&& !Block.shouldRenderFace(world, pos, state, world.getBlockState(mutablePos.setWithOffset(pos, face)), face)
				&& !(actualState.getBlock()instanceof CopycatBlock ufb
					&& !ufb.canFaceBeOccluded(actualState, face)))
				continue;
			CTType dataType = behaviour.getDataType(world, pos, state, face);
			if (dataType == null)
				continue;
			CTContext context = behaviour.buildContext(world, pos, state, face, dataType.getContextRequirement());
			data.put(face, dataType.getTextureIndex(context));
		}
		return data;
	}

	@Override
	protected void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, ModelData data,
		List<BlockModelPart> parts) {
		List<BlockModelPart> originalParts = new ArrayList<>();
		collectOriginalParts(world, pos, state, random, originalParts);
		if (!data.has(CT_PROPERTY)) {
			parts.addAll(originalParts);
			return;
		}

		CTData ctData = data.get(CT_PROPERTY);
		for (BlockModelPart part : originalParts)
			parts.add(transformPart(part, quad -> transformQuad(state, random, ctData, quad)));
	}

	private BakedQuad transformQuad(BlockState state, RandomSource random, CTData data, BakedQuad quad) {
		int index = data.get(quad.direction());
		if (index == -1)
			return quad;
		CTSpriteShiftEntry spriteShift = behaviour.getShift(state, random, quad.direction(), quad.sprite());
		if (spriteShift == null || quad.sprite() != spriteShift.getOriginal())
			return quad;
		BakedQuad newQuad = BakedQuadHelper.clone(quad);
		int[] vertexData = newQuad.vertices();
		for (int vertex = 0; vertex < 4; vertex++) {
			BakedQuadHelper.setU(vertexData, vertex, spriteShift.getTargetU(BakedQuadHelper.getU(vertexData, vertex), index));
			BakedQuadHelper.setV(vertexData, vertex, spriteShift.getTargetV(BakedQuadHelper.getV(vertexData, vertex), index));
		}
		return newQuad;
	}

	private static class CTData {
		private final int[] indices;

		public CTData() {
			indices = new int[6];
			Arrays.fill(indices, -1);
		}

		public void put(Direction face, int texture) {
			indices[face.get3DDataValue()] = texture;
		}

		public int get(Direction face) {
			return indices[face.get3DDataValue()];
		}
	}

}
