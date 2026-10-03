package com.simibubi.create.content.equipment.goggles;

import javax.annotation.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

/** Item-model boundary for goggles; head-specific geometry is supplied by the equipment renderer. */
public class GogglesModel implements ItemModel {
	private final ItemModel template;

	public GogglesModel(ItemModel template) {
		this.template = template;
	}

	@Override
	public void update(ItemStackRenderState renderState, net.minecraft.world.item.ItemStack stack, ItemModelResolver resolver,
		ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
		template.update(renderState, stack, resolver, displayContext, level, entity, seed);
	}

}
