package com.simibubi.create.foundation.model;

import java.util.Map;
import java.util.function.Function;

import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.item.render.CustomItemModels;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;

public class ModelSwapper {

	protected CustomBlockModels customBlockModels = new CustomBlockModels();
	protected CustomItemModels customItemModels = new CustomItemModels();

	public CustomBlockModels getCustomBlockModels() {
		return customBlockModels;
	}

	public CustomItemModels getCustomItemModels() {
		return customItemModels;
	}

	public void onModelBake(ModelEvent.ModifyBakingResult event) {
		Map<BlockState, BlockStateModel> blockModels = event.getBakingResult().blockStateModels();
		Map<ResourceLocation, ItemModel> itemModels = event.getBakingResult().itemStackModels();
		customBlockModels.forEach((block, modelFunc) -> block.getStateDefinition().getPossibleStates()
			.forEach(state -> swapBlockModel(blockModels, state, modelFunc)));
		customItemModels.forEach((item, modelFunc) -> swapItemModel(itemModels, getItemModelLocation(item), modelFunc));
		CustomRenderedItems.forEach(item -> swapItemModel(itemModels, getItemModelLocation(item), CustomRenderedItemModel::new));
	}

	public void registerListeners(IEventBus modEventBus) {
		modEventBus.addListener(this::onModelBake);
	}

	public static <T extends BlockStateModel> void swapBlockModel(Map<BlockState, BlockStateModel> modelRegistry,
		BlockState state, Function<BlockStateModel, T> factory) {
		BlockStateModel model = modelRegistry.get(state);
		if (model != null)
			modelRegistry.put(state, factory.apply(model));
	}

	public static <T extends ItemModel> void swapItemModel(Map<ResourceLocation, ItemModel> modelRegistry,
		ResourceLocation location, Function<ItemModel, T> factory) {
		ItemModel model = modelRegistry.get(location);
		if (model != null)
			modelRegistry.put(location, factory.apply(model));
	}

	public static ResourceLocation getItemModelLocation(Item item) {
		return item.builtInRegistryHolder().key().location();
	}

}
