package com.simibubi.create.foundation.utility;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

public class RecipeGenericsUtil {
	public static List<Ingredient> getIngredients(Recipe<?> recipe) {
		return recipe.placementInfo().ingredients();
	}

	public static ItemStack getResultItem(Recipe<?> recipe, Level level) {
		if (recipe instanceof ProcessingRecipe<?, ?> processingRecipe)
			return processingRecipe.getResultItem(level.registryAccess());
		if (recipe instanceof SequencedAssemblyRecipe sequencedAssemblyRecipe)
			return sequencedAssemblyRecipe.getResultItem(level.registryAccess());
		return recipe.display().stream()
			.findFirst()
			.map(display -> display.result().resolveForFirstStack(SlotDisplayContext.fromLevel(level)).copy())
			.orElse(ItemStack.EMPTY);
	}

	@SuppressWarnings("unchecked")
	public static <P extends Recipe<?>, C extends P> List<RecipeHolder<P>> cast(List<RecipeHolder<C>> list) {
		return (List<RecipeHolder<P>>) (List<?>) list;
	}

	public static Collection<RecipeHolder<? extends Recipe<?>>> specify(Collection<RecipeHolder<?>> list) {
		return list;
	}
}
