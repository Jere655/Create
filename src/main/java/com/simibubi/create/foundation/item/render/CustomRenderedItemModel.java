package com.simibubi.create.foundation.item.render;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * The item-model counterpart to Create's old custom-renderer wrapper.
 *
 * The 1.21.7 item pipeline no longer exposes a baked model from the item renderer.
 * This wrapper deliberately stays at the ItemModel boundary; specialized Create item
 * renderers are migrated to fill an ItemStackRenderState in their own follow-up layer.
 */
public class CustomRenderedItemModel implements ItemModel {

	protected final ItemModel originalModel;

	public CustomRenderedItemModel(ItemModel originalModel) {
		this.originalModel = originalModel;
	}

	@Override
	public void update(ItemStackRenderState renderState, net.minecraft.world.item.ItemStack stack, ItemModelResolver resolver,
		ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		originalModel.update(renderState, stack, resolver, displayContext, level, entity, seed);
	}

	public ItemModel getOriginalModel() {
		return originalModel;
	}

}
