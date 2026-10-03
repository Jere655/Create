package com.simibubi.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import com.mojang.math.Quadrant;

public abstract class SpecialBlockStateGen {

	protected Property<?>[] getIgnoredProperties() {
		return new Property<?>[0];
	}

	public final <T extends Block> void generate(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov) {
		java.util.Set<Property<?>> ignored = java.util.Set.of(getIgnoredProperties());
		prov.createVariants(ctx.getEntry(), ignored::contains, state -> prov.variant(getModel(ctx, prov, state))
			.with(rotation(getXRotation(state), getYRotation(state))));
	}

	private static VariantMutator rotation(int x, int y) {
		VariantMutator xRotation = switch ((x % 360 + 360) % 360) {
			case 90 -> VariantMutator.X_ROT.withValue(Quadrant.R90);
			case 180 -> VariantMutator.X_ROT.withValue(Quadrant.R180);
			case 270 -> VariantMutator.X_ROT.withValue(Quadrant.R270);
			default -> variant -> variant;
		};
		return xRotation.then(switch ((y % 360 + 360) % 360) {
			case 90 -> VariantMutator.Y_ROT.withValue(Quadrant.R90);
			case 180 -> VariantMutator.Y_ROT.withValue(Quadrant.R180);
			case 270 -> VariantMutator.Y_ROT.withValue(Quadrant.R270);
			default -> variant -> variant;
		});
	}

	protected int horizontalAngle(Direction direction) {
		if (direction.getAxis()
			.isVertical())
			return 0;
		return (int) direction.toYRot();
	}

	protected abstract int getXRotation(BlockState state);

	protected abstract int getYRotation(BlockState state);

	public abstract <T extends Block> net.minecraft.resources.ResourceLocation getModel(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov, BlockState state);

}
