package com.simibubi.create.infrastructure.gametest;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.simibubi.create.Create;
import com.simibubi.create.infrastructure.gametest.tests.TestContraptions;
import com.simibubi.create.infrastructure.gametest.tests.TestFluids;
import com.simibubi.create.infrastructure.gametest.tests.TestItems;
import com.simibubi.create.infrastructure.gametest.tests.TestMisc;
import com.simibubi.create.infrastructure.gametest.tests.TestProcessing;
import com.simibubi.create.infrastructure.gametest.tests.TestRegressions;

import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.gametest.framework.TestFunctionLoader;
import net.minecraft.resources.ResourceKey;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber
public class CreateGameTests {
	private static final Class<?>[] testHolders = {
			TestContraptions.class,
			TestFluids.class,
			TestItems.class,
			TestMisc.class,
			TestProcessing.class,
			TestRegressions.class
	};

	/** Registers functions before the 1.21.7 test-function registry is bootstrapped. */
	public static void bootstrap() {
		TestFunctionLoader.registerLoader(new TestFunctionLoader() {
			@Override
			public void load(BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> registrar) {
				CreateTestFunction.getTestsFrom(testHolders)
					.forEach(test -> registrar.accept(test.functionKey, test.function));
			}
		});
	}

	@SubscribeEvent
	public static void registerTests(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(Create.asResource("gametest"));
		CreateTestFunction.getTestsFrom(testHolders).forEach(test -> {
			TestData<Holder<TestEnvironmentDefinition>> data = test.createTestData(environment);
			event.registerTest(test.testId, testData -> new FunctionGameTestInstance(test.functionKey, testData), data);
		});
	}
}
