package com.simibubi.create.content.logistics.stockTicker;

import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.foundation.utility.RecipeGenericsUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public class CraftableBigItemStack extends BigItemStack {

	public Recipe<?> recipe;

	public CraftableBigItemStack(ItemStack stack, Recipe<?> recipe) {
		super(stack);
		this.recipe = recipe;
	}

	public List<Ingredient> getIngredients() {
		return RecipeGenericsUtil.getIngredients(recipe);
	}

	public int getOutputCount(Level level) {
		return RecipeGenericsUtil.getResultItem(recipe, level)
			.getCount();
	}

}
