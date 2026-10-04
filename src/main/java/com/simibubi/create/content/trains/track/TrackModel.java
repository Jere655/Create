package com.simibubi.create.content.trains.track;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.simibubi.create.foundation.model.BakedQuadHelper;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.model.data.ModelData;

public class TrackModel extends BakedModelWrapperWithData {

	public TrackModel(BlockStateModel originalModel) {
		super(originalModel);
	}

	@Override
	protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData blockEntityData) {
		// Track model doesn't need custom model data gathering
		return builder;
	}

	@Override
	protected void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random,
		ModelData data, List<BlockModelPart> parts) {
		// Delegate to the original model's collectParts
		collectOriginalParts(world, pos, state, random, parts);
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
											 @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
		List<BakedQuad> templateQuads = super.getQuads(state, side, rand, extraData, renderType);
		if (templateQuads.isEmpty())
			return templateQuads;
		if (!extraData.has(TrackBlockEntityTilt.ASCENDING_PROPERTY))
			return templateQuads;

		double angleIn = extraData.get(TrackBlockEntityTilt.ASCENDING_PROPERTY);
		double angle = Math.abs(angleIn);
		boolean flip = angleIn < 0;

		TrackShape trackShape = state.getValue(TrackBlock.SHAPE);
		double hAngle = switch (trackShape) {
			case XO -> 0;
			case PD -> 45;
			case ZO -> 90;
			case ND -> 135;
			default -> 0;
		};

		Vec3 verticalOffset = new Vec3(0, -0.25, 0);
		Vec3 diagonalRotationPoint =
			(trackShape == TrackShape.ND || trackShape == TrackShape.PD) ? new Vec3((Mth.SQRT_OF_TWO - 1) / 2, 0, 0)
				: Vec3.ZERO;

		UnaryOperator<Vec3> transform = v -> {
			v = v.add(verticalOffset);
			v = VecHelper.rotateCentered(v, hAngle, Axis.Y);
			v = v.add(diagonalRotationPoint);
			v = VecHelper.rotate(v, angle, Axis.Z);
			v = v.subtract(diagonalRotationPoint);
			v = VecHelper.rotateCentered(v, -hAngle + (flip ? 180 : 0), Axis.Y);
			v = v.subtract(verticalOffset);
			return v;
		};

		int size = templateQuads.size();
		List<BakedQuad> quads = new ArrayList<>();
		for (BakedQuad templateQuad : templateQuads) {
			BakedQuad quad = BakedQuadHelper.clone(templateQuad);
			int[] vertexData = quad.getVertices();
			for (int j = 0; j < 4; j++)
				BakedQuadHelper.setXYZ(vertexData, j, transform.apply(BakedQuadHelper.getXYZ(vertexData, j)));
			quads.add(quad);
		}

		return quads;
	}

}
