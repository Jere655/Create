package com.simibubi.create.content.redstone.diodes;

import java.util.List;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDiodeGenerator extends SpecialBlockStateGen {

	private List<ResourceLocation> models;

	public static <I extends BlockItem> void diodeItemModel(DataGenContext<Item, I> c, RegistrateItemModelGenerator p) {
		String name = c.getName();
		String path = "block/diodes/";
		AssetLookup.itemInherit(p, c.get(), AssetLookup.itemLoc(p, name), p.modLoc(path + name),
			Map.of(AssetLookup.slot("top"), p.modLoc(path + name + "/item")));
	}

	@Override
	protected final int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected final int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(AbstractDiodeBlock.FACING));
	}

	protected abstract <T extends Block> List<ResourceLocation> createModels(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov);

	protected abstract int getModelIndex(BlockState state);

	@Override
	public final <T extends Block> ResourceLocation getModel(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov, BlockState state) {
		if (models == null)
			models = createModels(ctx, prov);
		return models.get(getModelIndex(state));
	}

	protected ResourceLocation existing(String name) {
		return Create.asResource("block/diodes/" + name);
	}

	protected <T extends Block> ResourceLocation texture(DataGenContext<Block, T> ctx, String name) {
		return Create.asResource("block/diodes/" + ctx.getName() + "/" + name);
	}

	protected ResourceLocation poweredTorch() {
		return ResourceLocation.withDefaultNamespace("block/redstone_torch");
	}

}
