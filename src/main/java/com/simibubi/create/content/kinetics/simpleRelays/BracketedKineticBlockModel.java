package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.Collections;
import java.util.List;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;

import net.createmod.ponder.render.VirtualRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

public class BracketedKineticBlockModel extends BakedModelWrapperWithData {

	private static final ModelProperty<BracketedModelData> BRACKET_PROPERTY = new ModelProperty<>();

	public BracketedKineticBlockModel(BlockStateModel template) {
		super(template);
	}

	@Override
	protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData blockEntityData) {
		if (VirtualRenderHelper.isVirtual(blockEntityData))
			return builder;
		BracketedModelData data = new BracketedModelData();
		BracketedBlockEntityBehaviour attachmentBehaviour =
			BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
		if (attachmentBehaviour != null)
			data.putBracket(attachmentBehaviour.getBracket());
		return builder.with(BRACKET_PROPERTY, data);
	}

	@Override
	protected void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
		ModelData data, List<BlockModelPart> parts) {
		// Delegate to the original model's collectParts
		collectOriginalParts(world, pos, state, random, parts);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType) {
		if (!VirtualRenderHelper.isVirtual(data)) {
			if (data.has(BRACKET_PROPERTY)) {
				BracketedModelData pipeData = data.get(BRACKET_PROPERTY);
				BlockStateModel bracket = pipeData.getBracket();
				if (bracket != null)
					return bracket.getQuads(state, side, rand, data, renderType);
			}
			return Collections.emptyList();
		}
		return super.getQuads(state, side, rand, data, renderType);
	}

	private static class BracketedModelData {
		private BlockStateModel bracket;

		public void putBracket(BlockState state) {
			if (state != null) {
				this.bracket = Minecraft.getInstance()
					.getBlockRenderer()
					.getBlockModel(state);
			}
		}

		public BakedModel getBracket() {
			return bracket;
		}
	}

}
