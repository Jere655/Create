package com.simibubi.create.infrastructure.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create's declarative test metadata. Vanilla's old annotation was replaced by
 * registry-backed test instances in 1.21.7, which CreateGameTests installs.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameTest {
	String template();
	String batch() default "default";
	int timeoutTicks() default 200;
	int setupTicks() default 0;
	boolean required() default true;
	int attempts() default 1;
	int requiredSuccesses() default 1;
	int rotationSteps() default 0;
}
