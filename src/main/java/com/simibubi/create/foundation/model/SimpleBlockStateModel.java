package com.simibubi.create.foundation.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

/**
 * A {@link BlockStateModel} that always emits the same single part, regardless of context.
 */
public record SimpleBlockStateModel(BlockModelPart part) implements BlockStateModel {

	@Override
	public void collectParts(RandomSource random, List<BlockModelPart> parts) {
		parts.add(part);
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return part.particleIcon();
	}
}
