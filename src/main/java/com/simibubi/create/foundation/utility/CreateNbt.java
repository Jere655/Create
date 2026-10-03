package com.simibubi.create.foundation.utility;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;
import java.util.UUID;

/**
 * Small compatibility boundary for persistent stack data.
 *
 * <p>Vanilla's former {@code saveOptional} helpers were replaced by codecs.
 * Keeping the codec conversion here makes the on-disk representation explicit
 * and keeps callers independent of the codec implementation.</p>
 */
public final class CreateNbt {

	private CreateNbt() {}

	public static CompoundTag writeItemStack(HolderLookup.Provider registries, ItemStack stack) {
		return write(ItemStack.OPTIONAL_CODEC, registries, stack);
	}

	public static CompoundTag writeFluidStack(HolderLookup.Provider registries, FluidStack stack) {
		return write(FluidStack.OPTIONAL_CODEC, registries, stack);
	}

	public static ItemStack readItemStack(HolderLookup.Provider registries, Tag tag) {
		return CatnipCodecUtils.decode(ItemStack.OPTIONAL_CODEC, registries, tag)
			.orElse(ItemStack.EMPTY);
	}

	public static ItemStack readItemStack(HolderLookup.Provider registries, Optional<CompoundTag> tag) {
		return tag.map(value -> readItemStack(registries, value))
			.orElse(ItemStack.EMPTY);
	}

	public static FluidStack readFluidStack(HolderLookup.Provider registries, Tag tag) {
		return CatnipCodecUtils.decode(FluidStack.OPTIONAL_CODEC, registries, tag)
			.orElse(FluidStack.EMPTY);
	}

	public static FluidStack readFluidStack(HolderLookup.Provider registries, Optional<CompoundTag> tag) {
		return tag.map(value -> readFluidStack(registries, value))
			.orElse(FluidStack.EMPTY);
	}

	/**
	 * Preserves the legacy JSON representation used by Create's saved custom text.
	 */
	public static String writeComponent(HolderLookup.Provider registries, Component component) {
		return ComponentSerialization.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), component)
			.getOrThrow()
			.toString();
	}

	public static Optional<Component> readComponent(HolderLookup.Provider registries, String serialized) {
		try {
			return ComponentSerialization.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registries), JsonParser.parseString(serialized))
				.result();
		} catch (RuntimeException ignored) {
			return Optional.empty();
		}
	}

	public static Tag writeBlockPos(BlockPos pos) {
		return BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow();
	}

	public static BlockPos readBlockPos(Tag tag) {
		return BlockPos.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
	}

	public static Tag writeUUID(UUID uuid) {
		return UUIDUtil.CODEC.encodeStart(NbtOps.INSTANCE, uuid).getOrThrow();
	}

	public static UUID readUUID(Tag tag) {
		return UUIDUtil.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
	}

	private static <T> CompoundTag write(Codec<T> codec, HolderLookup.Provider registries, T value) {
		return CatnipCodecUtils.encode(codec, registries, value)
			.filter(CompoundTag.class::isInstance)
			.map(CompoundTag.class::cast)
			.orElseGet(CompoundTag::new);
	}
}
