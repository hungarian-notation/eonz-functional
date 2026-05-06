package net.eonz.functional.either;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/** Utilities that enhance the interoperability of {@link Either} with the Java Standard Library. */
public class Eithers {

  private Eithers() {}

  record CollectorAdapter<T, A, R>(
      Supplier<A> supplier,
      BiConsumer<A, T> accumulator,
      BinaryOperator<A> combiner,
      Function<A, R> finisher,
      Set<Characteristics> characteristics)
      implements Collector<T, A, R> {}

  /**
   * Adapts a {@code Collector} accepting elements of type {@link L} to one accepting elements of
   * type {@link Either}{@code <L, ?>}.
   *
   * @param <L> left-handed type of collected {@link Either} type
   * @param <R> right-handed type of collected {@link Either} type
   * @param <A> the mutable accumulation type of the reduction operation (often hidden as an
   *     implementation detail)
   * @param <T> the result type of the reduction operation
   * @param downstream the downstream collector to adapt
   * @return a collector that passes unwrapped values to the downstream collector's accumulator
   */
  public static <L, R, A, T> Collector<Either<L, R>, ?, T> collectLeft(
      Collector<? super L, A, T> downstream) {
    final BiConsumer<A, ? super L> accumulator = downstream.accumulator();
    return new CollectorAdapter<>(
        downstream.supplier(),
        (container, either) ->
            either.ifLeft(
                left -> {
                  accumulator.accept(container, left);
                }),
        downstream.combiner(),
        downstream.finisher(),
        downstream.characteristics());
  }

  /**
   * Adapts a {@code Collector} accepting elements of type {@link R} to one accepting elements of
   * type {@link Either}{@code <?, R>}.
   *
   * @param <L> left-handed type of collected {@link Either} type
   * @param <R> right-handed type of collected {@link Either} type
   * @param <A> the mutable accumulation type of the reduction operation (often hidden as an
   *     implementation detail)
   * @param <T> the result type of the reduction operation
   * @param downstream the downstream collector to adapt
   * @return a collector that passes unwrapped values to the downstream collector's accumulator
   */
  public static <L, R, A, T> Collector<Either<L, R>, ?, T> collectRight(
      Collector<? super R, A, T> downstream) {
    final BiConsumer<A, ? super R> accumulator = downstream.accumulator();
    return new CollectorAdapter<>(
        downstream.supplier(),
        (container, either) ->
            either.ifRight(
                right -> {
                  accumulator.accept(container, right);
                }),
        downstream.combiner(),
        downstream.finisher(),
        downstream.characteristics());
  }

  /**
   * A generic representation of the result of a teeing collection operation on a stream of {@link
   * Either}
   *
   * @param <L> the reduced left-handed type
   * @param <R> the reduced right-handed type
   * @param left the reduced form of all {@link Left left-handed} instances.
   * @param right the reduced form of all {@link Right right-handed} instances.
   */
  public record Collected<L, R>(L left, R right) {}

  /**
   * Returns a {@code Collector} that is a composite of two downstream collectors, one for each of
   * the possible types of an {@link Either}. The downstream collectors will be adapted via {@link
   * #collectLeft(Collector)} and {@link #collectRight(Collector)} such that each downstream
   * collector will only process the unwrapped values of upstream {@code Either} instances of the
   * correct handedness.
   *
   * <p>The results of the downstream collectors will then be merged using the specified {@code
   * merger} function.
   *
   * @param <L> the left-handed type of the upstream {@code Either} type.
   * @param <R> the right-handed type of the upstream {@code Either} type.
   * @param <L2> the intermediate reduced form of all left-handed values
   * @param <R2> the intermediate reduced form of all right-handed values
   * @param <M> the merged form of all values as defined by {@code merger}
   * @param downstreamLeft the collector that will be used to reduce left-handed values
   * @param downstreamRight the collector that will be used to reduce right-handed values
   * @param merger the function that combines the reduced forms of the left and right-handed values
   * @return a collector that aggregates the results of the left and right-handed collectors
   */
  public static <L, R, L2, R2, M> Collector<Either<L, R>, ?, M> teeing(
      Collector<? super L, ?, L2> downstreamLeft,
      Collector<? super R, ?, R2> downstreamRight,
      BiFunction<? super L2, ? super R2, M> merger) {
    return Collectors.teeing(collectLeft(downstreamLeft), collectRight(downstreamRight), merger);
  }

  /**
   * Returns a {@code Collector} that is a composite of two downstream collectors, one for each of
   * the possible types of an {@link Either}. The downstream collectors will be adapted via {@link
   * #collectLeft(Collector)} and {@link #collectRight(Collector)} such that each downstream
   * collector will only process the unwrapped values of upstream {@code Either} instances of the
   * correct handedness.
   *
   * <p>The results of the downstream collectors will be combined as the {@link Collected#left()
   * left} and {@link Collected#right() right} fields of an instance of {@link Eithers.Collected}.
   *
   * @param <L> the left-handed type of the upstream {@code Either} type.
   * @param <R> the right-handed type of the upstream {@code Either} type.
   * @param <L2> the intermediate reduced form of all left-handed values
   * @param <R2> the intermediate reduced form of all right-handed values
   * @param downstreamLeft the collector that will be used to reduce left-handed values
   * @param downstreamRight the collector that will be used to reduce right-handed values
   * @return a collector that aggregates the results of the left and right-handed collectors
   */
  public static <L, R, L2, R2> Collector<Either<L, R>, ?, Collected<L2, R2>> teeing(
      Collector<? super L, ?, L2> downstreamLeft, Collector<? super R, ?, R2> downstreamRight) {
    return teeing(downstreamLeft, downstreamRight, Collected::new);
  }

  /**
   * Convenience method for a teeing collector that reduces a stream of {@code Either} instances
   * into a list of left-handed values and a list of right-handed values, then merges those lists
   * with the specified merger function.
   *
   * @param <L> the left-handed type of the upstream {@code Either}
   * @param <R> the right-handed type of the upstream {@code Either}
   * @param <T> the merged result type as returned by {@code merger}
   * @param merger a function that combines the two lists
   * @return a collector that maps and reduces a stream of eithers into the merged form of two lists
   */
  public static <L, R, T> Collector<Either<L, R>, ?, T> toList(
      BiFunction<? super List<L>, ? super List<R>, T> merger) {
    return teeing(Collectors.toList(), Collectors.toList(), merger);
  }

  /**
   * Convenience method for a teeing collector that reduces a stream of {@code Either} instances
   * into a list of left-handed values and a list of right-handed values, then merges those lists
   * using {@link Eithers.Collected#Collected(Object, Object) Eithers.Collected::new} as the merger
   * function.
   *
   * @param <L> the left-handed type of the upstream {@code Either}
   * @param <R> the right-handed type of the upstream {@code Either}
   * @return a collector that maps and reduces a stream of eithers into a {@code Collected<List<L>,
   *     List<R>>}
   */
  public static <L, R> Collector<Either<L, R>, ?, Collected<List<L>, List<R>>> toList() {
    return toList(Collected::new);
  }

  /**
   * Convenience method for a teeing collector that reduces a stream of {@code Either} instances
   * into a set of all left-handed values and a set of all right-handed values, then merges those
   * sets with the specified merger function.
   *
   * @param <L> the left-handed type of the upstream {@code Either}
   * @param <R> the right-handed type of the upstream {@code Either}
   * @param <T> the merged result type as returned by {@code merger}
   * @param merger a function that combines the two lists
   * @return a collector that maps and reduces a stream of eithers into the merged form of two sets
   */
  public static <L, R, T> Collector<Either<L, R>, ?, T> toSet(
      BiFunction<? super Set<L>, ? super Set<R>, T> merger) {
    return teeing(Collectors.toSet(), Collectors.toSet(), merger);
  }

  /**
   * Convenience method for a teeing collector that reduces a stream of {@code Either} instances
   * into a set of all left-handed values and a set of all right-handed values, then merges those
   * sets using {@link Eithers.Collected#Collected(Object, Object) Eithers.Collected::new} as the
   * merger function.
   *
   * @param <L> the left-handed type of the upstream {@code Either}
   * @param <R> the right-handed type of the upstream {@code Either}
   * @return a collector that maps and reduces a stream of eithers into the merged form of two sets
   */
  public static <L, R> Collector<Either<L, R>, ?, Collected<Set<L>, Set<R>>> toSet() {
    return toSet(Collected::new);
  }
}
