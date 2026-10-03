package com.simibubi.create.foundation.data;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.data.recipe.DatagenMod;
import com.simibubi.create.foundation.data.recipe.Mods;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

@Internal
public record SimpleDatagenIngredient(DatagenMod mod, String id) implements ICustomIngredient {
	public static final MapCodec<SimpleDatagenIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
		instance.group(ResourceLocation.CODEC.fieldOf("item").forGetter((SimpleDatagenIngredient i) -> i.mod.asResource(i.id)))
			.apply(instance, (location) -> {
				for (Mods mod : Mods.values()) {
					if (mod.getId().equals(location.getNamespace())) {
						return new SimpleDatagenIngredient(mod, location.getPath());
					}
				}
				throw new AssertionError("ID " + location.getNamespace() + " doesn't correspond to any compat mod." +
					" SimpleDatagenIngredient is not meant for deserialization anyway");
			}));

	public static final IngredientType<SimpleDatagenIngredient> TYPE = new IngredientType<>(MAP_CODEC);

	@Override
	public Stream<Holder<Item>> items() {
		return BuiltInRegistries.ITEM.getOptional(mod.asResource(id))
			.<Stream<Holder<Item>>>map(item -> Stream.of(item.builtInRegistryHolder()))
			.orElseGet(Stream::of);
	}

	@Override
	public boolean test(ItemStack stack) {
		return items().anyMatch(holder -> stack.is(holder));
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public IngredientType<?> getType() {
		return TYPE;
	}

	public static Ingredient of(DatagenMod mod, String id) {
		return new SimpleDatagenIngredient(mod, id).toVanilla();
	}
}
