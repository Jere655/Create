package com.simibubi.create.foundation.data;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.math.Quadrant;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.chassis.LinearChassisBlock;
import com.simibubi.create.content.contraptions.chassis.RadialChassisBlock;
import com.simibubi.create.content.contraptions.mounted.CartAssemblerBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.simibubi.create.content.decoration.steamWhistle.WhistleExtenderBlock;
import com.simibubi.create.content.decoration.steamWhistle.WhistleExtenderBlock.WhistleExtenderShape;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateLegacyBlockModelBuilder;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.createmod.catnip.data.Iterate;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;

/** Modern vanilla blockstate DSL used by Create registrations. */
public class BlockStateGen {
	private static final VariantMutator NOP = variant -> variant;
	private static final Predicate<Property<?>> WATERLOGGED_ONLY = property -> property == BlockStateProperties.WATERLOGGED;

	public static VariantMutator rot(int x, int y, boolean uvLock) {
		VariantMutator result = switch (x) {
			case 90 -> VariantMutator.X_ROT.withValue(Quadrant.R90);
			case 180 -> VariantMutator.X_ROT.withValue(Quadrant.R180);
			case 270 -> VariantMutator.X_ROT.withValue(Quadrant.R270);
			default -> NOP;
		};
		result = result.then(switch (y) {
			case 90 -> VariantMutator.Y_ROT.withValue(Quadrant.R90);
			case 180 -> VariantMutator.Y_ROT.withValue(Quadrant.R180);
			case 270 -> VariantMutator.Y_ROT.withValue(Quadrant.R270);
			default -> NOP;
		});
		return uvLock ? result.then(VariantMutator.UV_LOCK.withValue(true)) : result;
	}

	public static MultiVariant variant(RegistrateBlockModelGenerator p, ResourceLocation model) { return p.variant(model); }
	public static ConditionBuilder when() { return new ConditionBuilder(); }

	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> axisBlockProvider(boolean customItem) {
		return (c, p) -> axisBlock(c, p, getBlockModel(customItem, c, p));
	}
	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> directionalBlockProvider(boolean customItem) {
		return (c, p) -> directionalBlockIgnoresWaterlogged(c, p, getBlockModel(customItem, c, p));
	}
	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> directionalBlockProviderIgnoresWaterlogged(boolean customItem) {
		return (c, p) -> directionalBlockIgnoresWaterlogged(c, p, getBlockModel(customItem, c, p));
	}
	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> horizontalBlockProvider(boolean customItem) {
		return (c, p) -> p.generateHorizontalBlock(c.get(), variant(p, getBlockModel(customItem, c, p).apply(c.get().defaultBlockState())));
	}
	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> horizontalAxisBlockProvider(boolean customItem) {
		return (c, p) -> horizontalAxisBlock(c, p, getBlockModel(customItem, c, p));
	}
	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> simpleCubeAll(String path) {
		return (c, p) -> p.create(c.get(), p.withParent(ModelTemplates.CUBE_ALL, TextureMapping.cube(p.modLoc("block/" + path))).build(c.get()));
	}
	public static <T extends DirectionalAxisKineticBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> directionalAxisBlockProvider() {
		return (c, p) -> directionalAxisBlock(c, p, ($, vertical) -> p.modLoc("block/" + c.getName() + "/" + (vertical ? "vertical" : "horizontal")));
	}
	public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockModelGenerator> horizontalWheelProvider(boolean customItem) {
		return (c, p) -> horizontalWheel(c, p, getBlockModel(customItem, c, p));
	}

	private static <T extends Block> Function<BlockState, ResourceLocation> getBlockModel(boolean customItem, DataGenContext<Block, T> c, RegistrateBlockModelGenerator p) {
		return $ -> customItem ? AssetLookup.partialBaseModel(c, p) : AssetLookup.standardModel(c, p);
	}

	public static <T extends Block> void directionalBlockIgnoresWaterlogged(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, Function<BlockState, ResourceLocation> models) {
		p.createVariants(c.get(), WATERLOGGED_ONLY, state -> {
			Direction d = state.getValue(BlockStateProperties.FACING);
			return variant(p, models.apply(state)).with(rot(d == Direction.DOWN ? 180 : d.getAxis().isHorizontal() ? 90 : 0, d.getAxis().isVertical() ? 0 : ((int) d.toYRot() + 180) % 360, false));
		});
	}
	public static <T extends Block> void axisBlock(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, Function<BlockState, ResourceLocation> models) { axisBlock(c, p, models, false); }
	public static <T extends Block> void axisBlock(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, Function<BlockState, ResourceLocation> models, boolean uvLock) {
		p.createVariants(c.get(), WATERLOGGED_ONLY, state -> {
			Axis axis = state.getValue(BlockStateProperties.AXIS);
			return variant(p, models.apply(state)).with(rot(axis == Axis.Y ? 0 : 90, axis == Axis.X ? 90 : axis == Axis.Z ? 180 : 0, uvLock));
		});
	}
	public static <T extends Block> void simpleBlock(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, Function<BlockState, ResourceLocation> models) {
		p.createVariants(c.get(), $ -> false, state -> variant(p, models.apply(state)));
	}
	public static <T extends Block> void horizontalAxisBlock(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, Function<BlockState, ResourceLocation> models) {
		p.createVariants(c.get(), WATERLOGGED_ONLY, state -> {
			Axis axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
			return variant(p, models.apply(state)).with(rot(0, axis == Axis.X ? 90 : 0, false));
		});
	}
	public static <T extends Block> void horizontalBlock(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, Function<BlockState, ResourceLocation> models) {
		p.createVariants(c.get(), WATERLOGGED_ONLY, state -> variant(p, models.apply(state))
			.with(rot(0, horizontalYRot(state), false)));
	}
	public static <T extends Block> void horizontalFaceBlock(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, Function<BlockState, ResourceLocation> models) {
		p.createVariants(c.get(), WATERLOGGED_ONLY, state -> {
			AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
			return variant(p, models.apply(state))
				.with(rot(face == AttachFace.CEILING ? 180 : face == AttachFace.WALL ? 90 : 0, horizontalYRot(state), false));
		});
	}
	public static <T extends DirectionalAxisKineticBlock> void directionalAxisBlock(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, BiFunction<BlockState, Boolean, ResourceLocation> models) {
		p.createVariants(c.get(), WATERLOGGED_ONLY, state -> {
			Direction direction = state.getValue(DirectionalAxisKineticBlock.FACING);
			boolean along = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			boolean vertical = direction.getAxis().isHorizontal() && (direction.getAxis() == Axis.X) == along;
			return variant(p, models.apply(state, vertical)).with(rot(direction == Direction.DOWN ? 270 : direction == Direction.UP ? 90 : 0, direction.getAxis().isVertical() ? along ? 0 : 90 : (int) direction.toYRot(), false));
		});
	}
	public static <T extends Block> void horizontalWheel(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, Function<BlockState, ResourceLocation> models) {
		p.createVariants(c.get(), WATERLOGGED_ONLY, state -> variant(p, models.apply(state))
			.with(rot(90, horizontalYRot(state), false)));
	}

	/** Forge's horizontalBlock rotated models 180 degrees away from the facing's own yaw. */
	private static int horizontalYRot(BlockState state) {
		Direction facing = state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
			.or(() -> state.getOptionalValue(BlockStateProperties.FACING).filter(d -> d.getAxis().isHorizontal()))
			.orElse(null);
		return facing == null ? 0 : ((int) facing.toYRot() + 180) % 360;
	}
	public static <T extends Block> void cubeAll(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, String directory) { cubeAll(c, p, directory, c.getName()); }
	public static <T extends Block> void cubeAll(DataGenContext<Block, T> c, RegistrateBlockModelGenerator p, String directory, String name) {
		p.create(c.get(), p.withParent(ModelTemplates.CUBE_ALL, TextureMapping.cube(p.modLoc("block/" + directory + name))).build(p.modLoc("block/" + c.getName())));
	}

	public static NonNullBiConsumer<DataGenContext<Block, CartAssemblerBlock>, RegistrateBlockModelGenerator> cartAssembler() {
		return (c, p) -> p.accept(MultiVariantGenerator.dispatch(c.get()).with(PropertyDispatch.initial(CartAssemblerBlock.RAIL_TYPE, CartAssemblerBlock.POWERED, CartAssemblerBlock.BACKWARDS, CartAssemblerBlock.RAIL_SHAPE).generate((type, powered, backwards, shape) -> {
			int y = (shape == RailShape.EAST_WEST ? 270 : 0) + (backwards ? 180 : 0);
			return variant(p, p.modLoc("block/" + c.getName() + "/block_" + type.getSerializedName() + (powered ? "_powered" : ""))).with(rot(0, y % 360, false));
		})));
	}
	public static NonNullBiConsumer<DataGenContext<Block, BlazeBurnerBlock>, RegistrateBlockModelGenerator> blazeHeater() { return (c, p) -> p.create(c.get(), p.modLoc("block/" + c.getName() + "/block")); }
	public static <B extends LinearChassisBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> linearChassis() {
		return (c, p) -> axisBlock(c, p, state -> {
			boolean top = state.getValue(LinearChassisBlock.STICKY_TOP), bottom = state.getValue(LinearChassisBlock.STICKY_BOTTOM);
			return p.withParent(ModelTemplates.CUBE_BOTTOM_TOP).texture(TextureSlot.SIDE, p.modLoc("block/" + c.getName() + "_side"))
				.texture(TextureSlot.BOTTOM, p.modLoc("block/linear_chassis_end" + (bottom ? "_sticky" : ""))).texture(TextureSlot.TOP, p.modLoc("block/linear_chassis_end" + (top ? "_sticky" : "")))
				.build(p.modLoc("block/" + c.getName() + (top ? "_top" : "") + (bottom ? "_bottom" : "")));
		});
	}
	public static <B extends RadialChassisBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> radialChassis() {
		return (c, p) -> {
			MultiPartGenerator parts = MultiPartGenerator.multiPart(c.get());
			for (Axis axis : Iterate.axes) parts.with(when().term(RadialChassisBlock.AXIS, axis), variant(p, p.modLoc("block/radial_chassis/base")).with(rot(axis == Axis.Y ? 0 : 90, axis == Axis.X ? 90 : 0, false)));
			for (Direction face : Iterate.horizontalDirections) for (boolean sticky : Iterate.trueAndFalse) for (Axis axis : Iterate.axes)
				parts.with(when().term(RadialChassisBlock.AXIS, axis).term(c.get().getGlueableSide(c.get().defaultBlockState().setValue(RadialChassisBlock.AXIS, Axis.Y), face), sticky), variant(p, p.modLoc("block/" + c.getName() + "_side_" + axis.getSerializedName() + (sticky ? "_sticky" : ""))));
			p.accept(parts);
		};
	}
	public static <P extends Block> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> naturalStoneTypeBlock(String type) {
		return (c, p) -> p.create(c.get(), p.modLoc("block/palettes/stone_types/natural/" + type + "_0"));
	}
	public static <P extends EncasedPipeBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> encasedPipe() {
		return (c, p) -> {
			MultiPartGenerator parts = MultiPartGenerator.multiPart(c.get());
			for (boolean flat : Iterate.trueAndFalse) for (Direction d : Iterate.directions) parts.with(when().term(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(d), !flat), variant(p, AssetLookup.partialBaseModel(c, p, flat ? "flat" : "open")).with(rot(d == Direction.UP ? 90 : d == Direction.DOWN ? 270 : 0, ((int) d.toYRot() + (d.getAxis().isVertical() ? 90 : 0)) % 360, false)));
			p.accept(parts);
		};
	}
	public static <P extends TrapDoorBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> uvLockedTrapdoorBlock(P block, ResourceLocation bottom, ResourceLocation top, ResourceLocation open) {
		return (c, p) -> p.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(TrapDoorBlock.FACING, TrapDoorBlock.OPEN, TrapDoorBlock.HALF).generate((facing, isOpen, half) -> variant(p, isOpen ? open : half == Half.TOP ? top : bottom).with(rot(0, isOpen ? ((int) facing.toYRot() + 180) % 360 : 0, !isOpen)))));
	}
	public static <P extends WhistleExtenderBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> whistleExtender() {
		return (c, p) -> { MultiPartGenerator parts = MultiPartGenerator.multiPart(c.get()); for (WhistleSize size : WhistleSize.values()) { String path = "block/steam_whistle/extension/" + size.getSerializedName() + "_"; parts.with(when().term(WhistleExtenderBlock.SIZE, size).term(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.DOUBLE), variant(p, Create.asResource(path + "top_rim"))); parts.with(when().term(WhistleExtenderBlock.SIZE, size).term(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.SINGLE), variant(p, Create.asResource(path + "single"))); parts.with(when().term(WhistleExtenderBlock.SIZE, size).term(WhistleExtenderBlock.SHAPE, WhistleExtenderShape.DOUBLE, WhistleExtenderShape.DOUBLE_CONNECTED), variant(p, Create.asResource(path + "double"))); } p.accept(parts); };
	}
	public static <P extends FluidPipeBlock> NonNullBiConsumer<DataGenContext<Block, P>, RegistrateBlockModelGenerator> pipe() {
		return (c, p) -> { MultiPartGenerator parts = MultiPartGenerator.multiPart(c.get()); for (Direction d : Iterate.directions) parts.with(when().term(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(d), true), variant(p, p.modLoc("block/" + c.getName() + "/connection/" + d.getSerializedName()))); parts.with(variant(p, p.modLoc("block/" + c.getName() + "/core_y"))); p.accept(parts); };
	}
	public static Function<BlockState, ResourceLocation> mapToAir(@NonnullType RegistrateBlockModelGenerator p) { return state -> p.mcLoc("block/air"); }

	/** Forge's ModelProvider resolved bare names against models/block/; keep that convention for hand-written call sites. */
	public static ResourceLocation blockLoc(RegistrateBlockModelGenerator p, String path) {
		return p.modLoc(path.startsWith("block/") ? path : "block/" + path);
	}

	/** Emits models/block/<path>.json inheriting from {@code parent} and returns its location. */
	public static ResourceLocation inherit(RegistrateBlockModelGenerator p, String path, ResourceLocation parent) {
		return inherit(p, path, parent, b -> {});
	}

	public static ResourceLocation inherit(RegistrateBlockModelGenerator p, String path, ResourceLocation parent,
		Consumer<RegistrateLegacyBlockModelBuilder> config) {
		RegistrateLegacyBlockModelBuilder builder = p.getBuilder()
			.parent(parent);
		config.accept(builder);
		return builder.build(blockLoc(p, path));
	}

	public static MultiVariant variant(RegistrateBlockModelGenerator p, ResourceLocation model, VariantMutator rotation) {
		return variant(p, model).with(rotation);
	}
}
