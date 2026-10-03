package com.simibubi.create.infrastructure.data;

import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.compat.curios.CuriosDataGenerator;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.data.CreateDatamapProvider;
import com.simibubi.create.foundation.data.DamageTypeTagGen;
import com.simibubi.create.foundation.data.recipe.CreateMechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import com.simibubi.create.foundation.data.recipe.CreateSequencedAssemblyRecipeGen;
import com.simibubi.create.foundation.data.recipe.CreateStandardRecipeGen;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class CreateDatagen {
	private static boolean extraRegistrateDataAdded;

	public static void gatherDataHighPriority(GatherDataEvent event) {
		if (!Create.ID.equals(event.getModContainer().getModId()) || extraRegistrateDataAdded)
			return;
		extraRegistrateDataAdded = true;
		addExtraRegistrateData();
	}

	public static void gatherData(GatherDataEvent event) {
		if (!Create.ID.equals(event.getModContainer().getModId()))
			return;

		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();

		if (event instanceof GatherDataEvent.Client) {
			event.addProvider(new CreateWikiBlockInfoProvider(output));
			return;
		}

		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		event.addProvider(AllSoundEvents.provider(generator));

		GeneratedEntriesProvider generatedEntriesProvider = new GeneratedEntriesProvider(output, lookupProvider);
		lookupProvider = generatedEntriesProvider.getRegistryProvider();
		event.addProvider(generatedEntriesProvider);

		event.addProvider(new CreateRecipeSerializerTagsProvider(output, lookupProvider));
		event.addProvider(new CreateContraptionTypeTagsProvider(output, lookupProvider));
		event.addProvider(new CreateMountedItemStorageTypeTagsProvider(output, lookupProvider));
		event.addProvider(new DamageTypeTagGen(output, lookupProvider));
		event.addProvider(new AllAdvancements(output, lookupProvider));
		event.addProvider(new CreateStandardRecipeGen(output, lookupProvider));
		event.addProvider(new CreateMechanicalCraftingRecipeGen(output, lookupProvider));
		event.addProvider(new CreateSequencedAssemblyRecipeGen(output, lookupProvider));
		event.addProvider(new CreateDatamapProvider(output, lookupProvider));
		event.addProvider(new VanillaHatOffsetGenerator(output, lookupProvider));
		event.addProvider(new CuriosDataGenerator(output, lookupProvider));
		event.addProvider(new CreateEnchantmentTagsProvider(output, lookupProvider));

		CreateRecipeProvider.registerAllProcessing(generator, output, lookupProvider);
	}

	private static void addExtraRegistrateData() {
		CreateRegistrateTags.addGenerators();

		Create.registrate().addDataGenerator(ProviderType.LANG, provider -> {
			BiConsumer<String, String> langConsumer = provider::add;

			provideDefaultLang("interface", langConsumer);
			provideDefaultLang("tooltips", langConsumer);
			AllAdvancements.provideLang(langConsumer);
			AllSoundEvents.provideLang(langConsumer);
			AllKeys.provideLang(langConsumer);
			providePonderLang(langConsumer);
			new TagLangGenerator(langConsumer).generate();
		});
	}

	private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
		String path = "assets/create/lang/default/" + fileName + ".json";
		JsonElement jsonElement = FilesHelper.loadJsonResource(path);
		if (jsonElement == null) {
			throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			consumer.accept(key, value);
		}
	}

	private static void providePonderLang(BiConsumer<String, String> consumer) {
		// Register this since FMLClientSetupEvent does not run during datagen
		PonderIndex.addPlugin(new CreatePonderPlugin());

		PonderIndex.getLangAccess().provideLang(Create.ID, consumer);
	}
}
