package com.simibubi.create.foundation.model;

import static net.createmod.catnip.render.SpriteShiftEntry.getUnInterpolatedU;
import static net.createmod.catnip.render.SpriteShiftEntry.getUnInterpolatedV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BakedModelHelper {

	public static int[] cropAndMove(int[] vertexData, TextureAtlasSprite sprite, AABB crop, Vec3 move) {
		vertexData = Arrays.copyOf(vertexData, vertexData.length);

		Vec3 xyz0 = BakedQuadHelper.getXYZ(vertexData, 0);
		Vec3 xyz1 = BakedQuadHelper.getXYZ(vertexData, 1);
		Vec3 xyz2 = BakedQuadHelper.getXYZ(vertexData, 2);
		Vec3 xyz3 = BakedQuadHelper.getXYZ(vertexData, 3);

		Vec3 uAxis = xyz3.add(xyz2)
			.scale(.5);
		Vec3 vAxis = xyz1.add(xyz2)
			.scale(.5);
		Vec3 center = xyz3.add(xyz2)
			.add(xyz0)
			.add(xyz1)
			.scale(.25);

		float u0 = BakedQuadHelper.getU(vertexData, 0);
		float u3 = BakedQuadHelper.getU(vertexData, 3);
		float v0 = BakedQuadHelper.getV(vertexData, 0);
		float v1 = BakedQuadHelper.getV(vertexData, 1);

		float uScale = (float) Math
			.round((getUnInterpolatedU(sprite, u3) - getUnInterpolatedU(sprite, u0)) / xyz3.distanceTo(xyz0));
		float vScale = (float) Math
			.round((getUnInterpolatedV(sprite, v1) - getUnInterpolatedV(sprite, v0)) / xyz1.distanceTo(xyz0));

		if (uScale == 0) {
			float v3 = BakedQuadHelper.getV(vertexData, 3);
			float u1 = BakedQuadHelper.getU(vertexData, 1);
			uAxis = xyz1.add(xyz2)
				.scale(.5);
			vAxis = xyz3.add(xyz2)
				.scale(.5);
			uScale = (float) Math
				.round((getUnInterpolatedU(sprite, u1) - getUnInterpolatedU(sprite, u0)) / xyz1.distanceTo(xyz0));
			vScale = (float) Math
				.round((getUnInterpolatedV(sprite, v3) - getUnInterpolatedV(sprite, v0)) / xyz3.distanceTo(xyz0));

		}

		uAxis = uAxis.subtract(center)
			.normalize();
		vAxis = vAxis.subtract(center)
			.normalize();

		Vec3 min = new Vec3(crop.minX, crop.minY, crop.minZ);
		Vec3 max = new Vec3(crop.maxX, crop.maxY, crop.maxZ);

		for (int vertex = 0; vertex < 4; vertex++) {
			Vec3 xyz = BakedQuadHelper.getXYZ(vertexData, vertex);
			Vec3 newXyz = VecHelper.componentMin(max, VecHelper.componentMax(xyz, min));
			Vec3 diff = newXyz.subtract(xyz);

			if (diff.lengthSqr() > 0) {
				float u = BakedQuadHelper.getU(vertexData, vertex);
				float v = BakedQuadHelper.getV(vertexData, vertex);
				float uDiff = (float) uAxis.dot(diff) * uScale;
				float vDiff = (float) vAxis.dot(diff) * vScale;
				BakedQuadHelper.setU(vertexData, vertex, sprite.getU(getUnInterpolatedU(sprite, u) + uDiff));
				BakedQuadHelper.setV(vertexData, vertex, sprite.getV(getUnInterpolatedV(sprite, v) + vDiff));
			}

			BakedQuadHelper.setXYZ(vertexData, vertex, newXyz.add(move));
		}

		return vertexData;
	}

	public static BlockStateModel generateModel(BlockStateModel template, UnaryOperator<TextureAtlasSprite> spriteSwapper) {
		RandomSource random = RandomSource.create();
		random.setSeed(42L);
		List<BlockModelPart> parts = new ArrayList<>();
		template.collectParts(random, parts);

		QuadCollection.Builder quads = new QuadCollection.Builder();
		TextureAtlasSprite particle = null;
		boolean ambientOcclusion = false;

		for (BlockModelPart part : parts) {
			ambientOcclusion |= part.useAmbientOcclusion();
			if (particle == null)
				particle = part.particleIcon();

			for (BakedQuad quad : part.getQuads(null))
				quads.addUnculledFace(swapSprite(quad, spriteSwapper));
			for (Direction direction : Iterate.directions)
				for (BakedQuad quad : part.getQuads(direction))
					quads.addCulledFace(direction, swapSprite(quad, spriteSwapper));
		}

		if (particle != null) {
			TextureAtlasSprite swappedParticle = spriteSwapper.apply(particle);
			if (swappedParticle != null)
				particle = swappedParticle;
		}

		return new SimpleBlockStateModel(new SimpleModelWrapper(quads.build(), ambientOcclusion, particle));
	}

	public static BakedQuad swapSprite(BakedQuad quad, UnaryOperator<TextureAtlasSprite> spriteSwapper) {
		TextureAtlasSprite sprite = quad.sprite();
		TextureAtlasSprite newSprite = spriteSwapper.apply(sprite);
		if (newSprite == null || sprite == newSprite)
			return quad;

		BakedQuad newQuad = BakedQuadHelper.clone(quad);
		int[] vertexData = newQuad.vertices();

		for (int vertex = 0; vertex < 4; vertex++) {
			float u = BakedQuadHelper.getU(vertexData, vertex);
			float v = BakedQuadHelper.getV(vertexData, vertex);
			BakedQuadHelper.setU(vertexData, vertex, newSprite.getU(getUnInterpolatedU(sprite, u)));
			BakedQuadHelper.setV(vertexData, vertex, newSprite.getV(getUnInterpolatedV(sprite, v)));
		}

		return newQuad;
	}

	public static List<BakedQuad> swapSprites(List<BakedQuad> quads, UnaryOperator<TextureAtlasSprite> spriteSwapper) {
		List<BakedQuad> newQuads = new ArrayList<>(quads.size());
		for (BakedQuad quad : quads)
			newQuads.add(swapSprite(quad, spriteSwapper));
		return newQuads;
	}
}
