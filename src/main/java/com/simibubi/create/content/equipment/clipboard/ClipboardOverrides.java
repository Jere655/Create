package com.simibubi.create.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.lang.Lang;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;

public class ClipboardOverrides {

	public enum ClipboardType implements StringRepresentable {
		EMPTY("empty_clipboard"), WRITTEN("clipboard"), EDITING("clipboard_and_quill");

		public static final Codec<ClipboardType> CODEC = StringRepresentable.fromValues(ClipboardType::values);
		public static final StreamCodec<ByteBuf, ClipboardType> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(ClipboardType.class);

		public final String file;
		public static ResourceLocation ID = Create.asResource("clipboard_type");

		ClipboardType(String file) {
			this.file = file;
		}

		@Override
		public @NotNull String getSerializedName() {
			return Lang.asId(name());
		}
	}

	/** Replaces the removed per-item float predicate; the selector reads the clipboard component directly. */
	@OnlyIn(Dist.CLIENT)
	public record ClipboardTypeProperty() implements SelectItemModelProperty<ClipboardType> {
		public static final SelectItemModelProperty.Type<ClipboardTypeProperty, ClipboardType> TYPE =
			SelectItemModelProperty.Type.create(MapCodec.unit(new ClipboardTypeProperty()), ClipboardType.CODEC);

		@Override
		public ClipboardType get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed,
			ItemDisplayContext displayContext) {
			return stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY)
				.type();
		}

		@Override
		public SelectItemModelProperty.Type<ClipboardTypeProperty, ClipboardType> type() {
			return TYPE;
		}

		@Override
		public Codec<ClipboardType> valueCodec() {
			return ClipboardType.CODEC;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerModelProperties(RegisterSelectItemModelPropertyEvent event) {
		event.register(ClipboardType.ID, ClipboardTypeProperty.TYPE);
	}

	@OnlyIn(Dist.CLIENT)
	public static void addOverrideModels(DataGenContext<Item, ClipboardBlockItem> c, RegistrateItemModelGenerator p) {
		List<SelectItemModel.SwitchCase<ClipboardType>> cases = new ArrayList<>(ClipboardType.values().length);
		for (ClipboardType type : ClipboardType.values()) {
			ResourceLocation model = p.createFlatModel(p.modLoc("item/" + c.getName() + "_" + type.ordinal()),
				Create.asResource("item/" + type.file));
			cases.add(ItemModelUtils.when(type, ItemModelUtils.plainModel(model)));
		}

		ResourceLocation fallback = p.createFlatModel(AssetLookup.itemLoc(p, c.getName()),
			TextureMapping.getItemTexture(c.get()));
		p.accept(c.get(),
			ItemModelUtils.select(new ClipboardTypeProperty(), ItemModelUtils.plainModel(fallback), cases));
	}

}
