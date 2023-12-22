/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils;

import kotlin.jvm.functions.Function1;
import net.greemdev.meteor.utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            if (start == null) throw new IllegalStateException("Cannot create a loop without a defined starting point.");
            if (incrementBy == null) throw new IllegalStateException("Cannot create a loop without a defined increment.");
            if (condition == null) throw new IllegalStateException("Cannot create a loop without a condition for execution of each iteration.");
            if (body == null) throw new IllegalStateException("Cannot create a loop without a task to run on each iteration.");
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
                    for (Integer i = start;
                         condition.test(i);
                         i += incrementBy
                    )
                        body.accept(i);
                }
            };
        }

        public static Loop<Float> ofFloat() {
            return new Loop<>() {
                @Override
                protected void runImpl() {
                    for (Float f = start;
                         condition.test(f);
                         f += incrementBy
                    )
                        body.accept(f);
                }
            };
        }

        public static Loop<Double> ofDouble() {
            return new Loop<>() {
                @Override
                protected void runImpl() {
                    for (Double d = start;
                         condition.test(d);
                         d += incrementBy
                    )
                        body.accept(d);
                }
            };
        }
    }

    public static void loop(int start, int incr, Function1<Integer, Boolean> loopCondition, Function1<Integer, kotlin.Unit> loopBody) {
        Loop.ofInt().setStartAt(start).setIncrement(incr).setCondition(loopCondition).setBody(loopBody).runImpl();
    }

    public static void loop(double start, double incr, Function1<Double, Boolean> loopCondition, Function1<Double, kotlin.Unit> loopBody) {
        Loop.ofDouble().setStartAt(start).setIncrement(incr).setCondition(loopCondition).setBody(loopBody).runImpl();
    }

    public static void loop(float start, float incr, Function1<Float, Boolean> loopCondition, Function1<Float, kotlin.Unit> loopBody) {
        Loop.ofFloat().setStartAt(start).setIncrement(incr).setCondition(loopCondition).setBody(loopBody).runImpl();
    }

    //im a fucking psycho; a maniac, even. i cant believe i wrote a hundred-line API around a language feature

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object value) {
        return (T)value;
    }
}
