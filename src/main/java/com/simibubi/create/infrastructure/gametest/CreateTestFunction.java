package com.simibubi.create.infrastructure.gametest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

/** Adapts Create's annotated methods to 1.21.7's registry-backed game tests. */
public class CreateTestFunction {
	public static final Map<String, CreateTestFunction> NAMES_TO_FUNCTIONS = new HashMap<>();

	public final String fullName;
	public final String simpleName;
	public final ResourceLocation testId;
	public final ResourceKey<Consumer<GameTestHelper>> functionKey;
	public final Consumer<GameTestHelper> function;
	private final ResourceLocation structure;
	private final Rotation rotation;
	private final int maxTicks;
	private final int setupTicks;
	private final boolean required;
	private final int maxAttempts;
	private final int requiredSuccesses;

	private CreateTestFunction(String fullName, String simpleName, ResourceLocation testId, ResourceLocation structure,
		Rotation rotation, int maxTicks, int setupTicks, boolean required, int maxAttempts, int requiredSuccesses,
		Consumer<GameTestHelper> function) {
		this.fullName = fullName;
		this.simpleName = simpleName;
		this.testId = testId;
		this.functionKey = ResourceKey.create(Registries.TEST_FUNCTION, testId);
		this.function = function;
		this.structure = structure;
		this.rotation = rotation;
		this.maxTicks = maxTicks;
		this.setupTicks = setupTicks;
		this.required = required;
		this.maxAttempts = maxAttempts;
		this.requiredSuccesses = requiredSuccesses;
		NAMES_TO_FUNCTIONS.put(fullName, this);
	}

	public TestData<Holder<TestEnvironmentDefinition>> createTestData(Holder<TestEnvironmentDefinition> environment) {
		return new TestData<>(environment, structure, maxTicks, setupTicks, required, rotation, false, maxAttempts,
			requiredSuccesses, true);
	}

	public static Collection<CreateTestFunction> getTestsFrom(Class<?>... classes) {
		return Stream.of(classes)
			.map(Class::getDeclaredMethods)
			.flatMap(Stream::of)
			.map(CreateTestFunction::of)
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(test -> test.testId.toString()))
			.toList();
	}

	@Nullable
	public static CreateTestFunction of(Method method) {
		GameTest test = method.getAnnotation(GameTest.class);
		if (test == null)
			return null;
		Class<?> owner = method.getDeclaringClass();
		GameTestGroup group = owner.getAnnotation(GameTestGroup.class);
		String simpleName = owner.getSimpleName() + '.' + method.getName();
		validateTestMethod(method, test, owner, group, simpleName);

		ResourceLocation structure = ResourceLocation.fromNamespaceAndPath(group.namespace(),
			"gametest/" + group.path() + "/" + test.template());
		ResourceLocation testId = Create.asResource("gametest/" + group.path() + "/" + method.getName());
		Rotation rotation = StructureUtils.getRotationForRotationSteps(test.rotationSteps());
		String fullName = owner.getName() + '.' + method.getName();
		return new CreateTestFunction(fullName, simpleName, testId, structure, rotation, test.timeoutTicks(),
			test.setupTicks(), test.required(), test.attempts(), test.requiredSuccesses(), run(fullName, asConsumer(method)));
	}

	private static void validateTestMethod(Method method, GameTest test, Class<?> owner, GameTestGroup group,
		String simpleName) {
		if (test.template().isEmpty())
			throw new IllegalArgumentException(simpleName + " must provide a template structure");
		if (!Modifier.isStatic(method.getModifiers()))
			throw new IllegalArgumentException(simpleName + " must be static");
		if (method.getReturnType() != void.class)
			throw new IllegalArgumentException(simpleName + " must return void");
		if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != CreateGameTestHelper.class)
			throw new IllegalArgumentException(simpleName + " must take 1 parameter of type CreateGameTestHelper");
		if (group == null)
			throw new IllegalArgumentException(owner.getName() + " must be annotated with @GameTestGroup");
	}

	private static Consumer<GameTestHelper> asConsumer(Method method) {
		return helper -> {
			try {
				method.invoke(null, helper);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static Consumer<GameTestHelper> run(String fullName, @NotNull Consumer<GameTestHelper> helper) {
		return consumer -> helper.andThen(gameTestHelper -> {
			StructureBlockEntity blockEntity = gameTestHelper.getBlockEntity(BlockPos.ZERO, StructureBlockEntity.class);
			blockEntity.getPersistentData().putString("CreateTestFunction", fullName);
		}).accept(CreateGameTestHelper.of(consumer));
	}
}
