package com.simibubi.create.api.data.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.Create;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

/**
 * A class containing some basic setup for other recipe generators to use.
 * Addons should extend this if they add a custom recipe type that is not
 * a processing recipe type and want to use Create's helpers.
 * For processing recipes extend {@link StandardProcessingRecipeGen}.
 */
public abstract class BaseRecipeProvider extends RecipeProvider.Runner {
	protected final String modid;
	protected final List<GeneratedRecipe> all = new ArrayList<>();

	protected HolderLookup.Provider registries;

	public BaseRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String defaultNamespace) {
		super(output, registries);
		this.modid = defaultNamespace;
	}

	protected ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(modid, path);
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	/** Only valid while recipes are being built; tag ingredients and criteria resolve through it. */
	protected HolderGetter<Item> items() {
		return registries.lookupOrThrow(Registries.ITEM);
	}

	/** Only valid while recipes are being built; fluid tag ingredients resolve through it. */
	protected HolderGetter<Fluid> fluids() {
		return registries.lookupOrThrow(Registries.FLUID);
	}

	protected static Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {
		return CriteriaTriggers.INVENTORY_CHANGED
			.createCriterion(new InventoryChangeTrigger.TriggerInstance(Optional.empty(),
				InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(predicates)));
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		this.registries = registries;
		return new RecipeProvider(registries, output) {
			@Override
			protected void buildRecipes() {
				all.forEach(recipe -> recipe.register(output));
				Create.LOGGER.info("{} registered {} recipe{}", getName(), all.size(), all.size() == 1 ? "" : "s");
			}
		};
	}

	@Override
	public String getName() {
		return "Create's Recipes";
	}

	@FunctionalInterface
	public interface GeneratedRecipe {
		void register(RecipeOutput recipeOutput);
	}
}
