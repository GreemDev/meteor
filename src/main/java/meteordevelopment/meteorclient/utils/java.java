/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils;

import kotlin.jvm.functions.Function1;
import net.greemdev.meteor.utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class java {
    @SuppressWarnings("unused") // explicit getters & setters are required to enable property-like (.value) getting & setting from kotlin
    public abstract static class Loop<T extends Number> {
        protected T start, incrementBy = null;
        protected Predicate<T> condition = null;
        protected Consumer<T> body = null;

        @Nullable
        public T getStartAt() {
            return start;
        }

        @NotNull
        public Loop<@NotNull T> setStartAt(T start) {
            this.start = start;
            return this;
        }

        @Nullable
        public T getIncrement() {
            return incrementBy;
        }

        @NotNull
        public Loop<@NotNull T> setIncrement(T increment) {
            this.incrementBy = increment;
            return this;
        }

        @Nullable
        public Function1<@NotNull T, @NotNull Boolean> getCondition() {
            return condition != null ? utils.kotlin(condition) : null;
        }

        @NotNull
        public Loop<@NotNull T> setCondition(Function1<@NotNull T, @NotNull Boolean> condition) {
            this.condition = utils.java(condition);
            return this;
        }

        @Nullable
        public Function1<@NotNull T, kotlin.@NotNull Unit> getBody() {
            return body != null ? utils.kotlin(body) : null;
        }

        @NotNull
        public Loop<@NotNull T> setBody(Function1<@NotNull T, kotlin.@NotNull Unit> body) {
            this.body = utils.java(body);
            return this;
        }

        protected void validate() {
            Objects.requireNonNull(start, "Cannot create a loop without a defined starting point.");
            Objects.requireNonNull(incrementBy, "Cannot create a loop without a defined increment.");
            Objects.requireNonNull(condition, "Cannot create a loop without a condition for execution of each iteration.");
            Objects.requireNonNull(body, "Cannot create a loop without a task to run on each iteration.");
        }

        public final void run() {
            validate();
            runImpl();
        }

        protected abstract void runImpl();

        public static Loop<Integer> ofInt() {
            return new Loop<>() {
                @Override
                protected void runImpl() {
                    // getCondition & getBody will not be null at this point
                    //noinspection DataFlowIssue
                    loop(start, incrementBy, getCondition(), getBody());
                }
            };
        }

        public static Loop<Float> ofFloat() {
            return new Loop<>() {
                @Override
                protected void runImpl() {
                    //noinspection DataFlowIssue
                    loop(start, incrementBy, getCondition(), getBody());
                }
            };
        }

        public static Loop<Double> ofDouble() {
            return new Loop<>() {
                @Override
                protected void runImpl() {
                    //noinspection DataFlowIssue
                    loop(start, incrementBy, getCondition(), getBody());
                }
            };
        }
    }

    public static void loop(Integer start, Integer incr, Function1<@NotNull Integer, @NotNull Boolean> loopCondition, Function1<@NotNull Integer, kotlin.@NotNull Unit> loopBody) {
        for (Integer i = start; loopCondition.invoke(i); i += incr)
            loopBody.invoke(i);
    }

    public static void loop(Double start, Double incr, Function1<@NotNull Double, @NotNull Boolean> loopCondition, Function1<@NotNull Double, kotlin.@NotNull Unit> loopBody) {
        for (Double i = start; loopCondition.invoke(i); i += incr)
            loopBody.invoke(i);
    }

    public static void loop(Float start, Float incr, Function1<@NotNull Float, @NotNull Boolean> loopCondition, Function1<@NotNull Float, kotlin.@NotNull Unit> loopBody) {
        for (Float i = start; loopCondition.invoke(i); i += incr)
            loopBody.invoke(i);
    }

    //im a fucking psycho; a maniac, even. i cant believe i wrote a hundred-line API around a language feature

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object value) {
        return (T)value;
    }
}
