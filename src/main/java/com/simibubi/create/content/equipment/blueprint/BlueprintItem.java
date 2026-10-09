package com.simibubi.create.content.equipment.blueprint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute.ItemAttributeEntry;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.RecipeGenericsUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BlueprintItem extends Item {

	public BlueprintItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Direction face = ctx.getClickedFace();
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		BlockPos pos = ctx.getClickedPos()
			.relative(face);

		if (player != null && !player.mayUseItemAt(pos, face, stack))
			return InteractionResult.FAIL;

		Level world = ctx.getLevel();
		HangingEntity hangingentity = new BlueprintEntity(world, pos, face, face.getAxis()
			.isHorizontal() ? Direction.DOWN : ctx.getHorizontalDirection());
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

		if (customData != null)
			EntityType.updateCustomEntityTag(world, player, hangingentity, customData);
		if (!hangingentity.survives())
			return InteractionResult.CONSUME;
		if (!world.isClientSide) {
			hangingentity.playPlacementSound();
			world.addFreshEntity(hangingentity);
		}

		stack.shrink(1);
		return world.isClientSide ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
	}

	public static void assignCompleteRecipe(Level level, ItemStackHandler inv, Recipe<?> recipe) {
		List<Ingredient> ingredients = RecipeGenericsUtil.getIngredients(recipe);

		for (int i = 0; i < 9; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		inv.setStackInSlot(9, RecipeGenericsUtil.getResultItem(recipe, level));

		if (recipe instanceof ShapedRecipe shapedRecipe) {
			List<java.util.Optional<Ingredient>> shapedIngredients = shapedRecipe.getIngredients();
			for (int row = 0; row < shapedRecipe.getHeight(); row++)
				for (int col = 0; col < shapedRecipe.getWidth(); col++) {
					java.util.Optional<Ingredient> ingredient =
						shapedIngredients.get(row * shapedRecipe.getWidth() + col);
					if (ingredient.isPresent())
						inv.setStackInSlot(row * 3 + col, convertIngredientToFilter(ingredient.get()));
				}
		} else {
			for (int i = 0; i < ingredients.size(); i++)
				inv.setStackInSlot(i, convertIngredientToFilter(ingredients.get(i)));
		}
	}

	private static ItemStack convertIngredientToFilter(Ingredient ingredient) {
		boolean isCompoundIngredient = ingredient.getCustomIngredient() instanceof CompoundIngredient;
		if (!ingredient.isCustom()) {
			var tag = ingredient.getValues().unwrapKey();
			if (tag.isPresent())
				return tagFilter(tag.get());
		}
		List<Holder<Item>> acceptedItems = ingredient.items().toList();
		if (acceptedItems.isEmpty() || acceptedItems.size() > 18)
			return ItemStack.EMPTY;
		if (acceptedItems.size() == 1)
			return new ItemStack(acceptedItems.getFirst());

		ItemStack result = AllItems.FILTER.asStack();
		ItemStackHandler filterItems = AllItems.FILTER.get().getFilterItemHandler(result);
		for (int i = 0; i < acceptedItems.size(); i++)
			filterItems.setStackInSlot(i, new ItemStack(acceptedItems.get(i)));
		result.set(AllDataComponents.FILTER_ITEMS, ItemHelper.containerContentsFromHandler(filterItems));
		if (isCompoundIngredient)
			result.set(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, true);
		return result;
	}

	private static ItemStack tagFilter(net.minecraft.tags.TagKey<Item> tag) {
		ItemStack filterItem = AllItems.ATTRIBUTE_FILTER.asStack();
		filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
		List<ItemAttributeEntry> attributes = new ArrayList<>();
		ItemAttribute at = new InTagAttribute(ItemTags.create(tag.location()));
		attributes.add(new ItemAttribute.ItemAttributeEntry(at, false));
		filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, attributes);
		return filterItem;
	}

}
