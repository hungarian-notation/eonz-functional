package net.eonz.functional.either;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A container which holds a single non-null value of one of two possible types.
 *
 * <p>Instances of {@code Either} can either be {@link Left} or {@link Right}, depending on the type
 * of the held value.
 *
 * <p>In a conscious continuation of a cultural bias stretching back to at least ancient Rome, the
 * semantics of this type conflate right-handed values as proper or desired, and left-handed values
 * as adverse or exceptional. Rather than applying "Right" and "Left" suffixes to all methods, we
 * generally leave methods that operate on or expect right-handed values un-suffixed, except where
 * the lack of a suffix would be overly ambiguous ({@link #isRight()} vs {@code is()}).
 * <!-- part of me wanted to call them dexter and sinister, or at least order the generics in
 * heraldic order, but I have restrained myself -->
 *
 * <p>Eithers are considered equal to each other when they have the equal values and handedness.
 *
 * <p>As with {@link Optional}, the hash code of a right-handed either is the hash code of its
 * value. On the other hand, the hash code of a left-handed either is the bitwise complement of the
 * hash code of its value.
 *
 * @implNote Either only has two concrete internal implementations, meaning that invocations of its
 *     methods will always be bimorphic. This allows HotSpot to inline of its methods. Introducing a
 *     third implementation could have significant performance implications.
 * @param <L> the left-handed (adverse, exceptional) type
 * @param <R> the right-handed (expected, proper) type
 */
public abstract sealed class Either<L, R> permits Left, Right {

  /**
   * Trivially empty constructor.
   *
   * @hidden
   */
  protected Either() {}

  /**
   * Returns an {@code Either} that describes the non-null value of {@code expected}, or a value
   * supplied by {@code alternative} if {@code expected} is empty.
   *
   * <p>This function can be used to construct an {@code Either} from two optionals when you know
   * that at least one of them must be present:
   *
   * {@snippet lang=java :
   * Optional<R>  a = ...;
   * Optional<L>  b = ...;
   * Either<L, R> either = Either.of(a, b::orElseThrow);
   * }
   *
   * @param <R> the right-handed type
   * @param <L> the left-handed type
   * @param expected an optional describing a right-hand value
   * @param alternative the supplier or left-hand values to draw from if {@code expected} is empty.
   * @throws NullPointerException when an argument to this method is {@code null}. Validation of
   *     {@code alternative`} <strong>may</strong> be deferred when {@code expected} is non-empty,
   *     but this is an implementation detail.
   * @return an either that describes the value of {@code expected} if it is present, or else a
   *     value returned by {@code alternative}
   */
  public static <L, R> Either<L, R> of(Optional<R> expected, Supplier<L> alternative) {
    Objects.requireNonNull(expected);
    Objects.requireNonNull(alternative);
    Either<L, R> result =
        expected.map(Either::<L, R>ofRight).orElseGet(() -> Left.of(alternative.get()));
    return result;
  }

  /**
   * Returns an {@code Either} that describes the non-null value of {@code expected}, or a value
   * supplied by {@code alternative} if {@code expected} is {@code null}.
   *
   * @param <R> the right-handed type
   * @param <L> the left-handed type
   * @param expected the right-hand value or {@code null}
   * @param alternative the supplier or left-hand values to draw from if {@code expected} is {@code
   *     null}.
   * @throws NullPointerException if alternative is {@code null}
   * @throws NullPointerException if {@code alternative::get} returns {@code null}
   * @return an either that describes the value of {@code expected} if it is present, or else a
   *     value returned by {@code alternative}
   */
  public static <L, R> Either<L, R> ofNullable(R expected, Supplier<L> alternative) {
    if (expected != null) {
      return Right.of(expected);
    } else {
      return Left.of(alternative.get());
    }
  }

  /**
   * Creates a {@link Right} describing the given non-null value.
   *
   * @see Right#of(Object)
   * @param <L> the related left-handed type
   * @param <R> the type of {@code value}
   * @param rightValue the value to describe, which must be non-null
   * @return An {@link Right} describing the value.
   * @throws NullPointerException if {@code rightValue} is null.
   */
  public static <L, R> Either<L, R> ofRight(final R rightValue) {
    return new Right<>(Objects.requireNonNull(rightValue));
  }

  /**
   * Creates a {@link Left} describing the given non-null value.
   *
   * @see Left#of(Object)
   * @param <L> the type of {@code value}
   * @param <R> the related right-handed type.
   * @param leftValue the value to describe, which must be non-null
   * @return An {@link Left} describing the value.
   * @throws NullPointerException if {@code rightValue} is null.
   */
  public static <L, R> Either<L, R> ofLeft(final L leftValue) {
    return new Left<>(Objects.requireNonNull(leftValue));
  }

  /**
   * Test if this instance is {@link Right right-handed}.
   *
   * <p>This method is the complement of {@link #isLeft()}
   *
   * <p>This method is provided for completeness, but it should only be used in those rare cases
   * where the described value will be discarded irrespective of its type. In most cases, you should
   * prefer to use pattern matching or one of the functional accessors.
   *
   * {@snippet lang=java :
   *  if (either instanceof Right right) {
   *    right.get();
   *  }
   * }
   *
   * @return true if this instance is a {@link Right}
   */
  public abstract boolean isRight();

  /**
   * Test if this instance is {@link Left left-handed}.
   *
   * <p>This method is provided for completeness, but its use is discouraged. You should prefer to
   * use pattern matching or one of the stream transformations.
   *
   * <p>This method is the complement of {@link #isRight()}
   *
   * @return true if this instance is an {@link Left}
   */
  public final boolean isLeft() {
    return !isRight();
  }

  /**
   * If this instance is right-handed, performs the given action with the described value, otherwise
   * does nothing.
   *
   * <p>Analogous to {@link Optional#ifPresent(Consumer)}
   *
   * @param action the action to be performed if this instance is right-handed
   */
  public abstract void ifRight(Consumer<? super R> action);

  /**
   * If this instance is left-handed, performs the given action with the described value, otherwise
   * does nothing.
   *
   * <p>Equivalent to {@link Either#optionalLeft()}{@link Optional#ifPresent(Consumer)
   * .ifPresent(action)}
   *
   * @param action the action to be performed if this instance is left-handed
   */
  public abstract void ifLeft(Consumer<? super L> action);

  /**
   * Returns the value described by a {@link Right right-handed instance}, or throws {@link
   * NoSuchElementException} if invoked on a {@link Left left-handed instance}.
   *
   * <p>The following expressions are equivalent for all {@code Either<?,?>} {@code either},
   * excluding the value of any thrown exception's stacktrace.
   *
   * <ol>
   *   <li>{@code either.orElseThrow()}
   *   <li>{@code either.}{@link #optional()}{@link Optional#orElseThrow() .orElseThrow()}
   * </ol>
   *
   * @return The value described by a right-handed instance.
   * @throws NoSuchElementException if this is a left-handed instance.
   */
  public R orElseThrow() {
    return orElseThrow(NoSuchElementException::new);
  }

  /**
   * Returns the value described by a {@link Left left-handed instance}, or throws {@link
   * NoSuchElementException} if invoked on a {@link Right right-handed instance}.
   *
   * <p>The following expressions are equivalent for all {@code Either<?,?>} {@code either},
   * excluding the value of any thrown exception's stacktrace.
   *
   * <ol>
   *   <li>{@code either.orElseThrowLeft()}
   *   <li>{@code either.}{@link #optionalLeft()}{@code .}{@link Optional#orElseThrow()
   *       orElseThrow()}
   *   <li>{@code either.}{@link #swap()}{@code .}{@link #orElseThrow()}
   *   <li>{@code either.}{@link #swap()}{@code .}{@link #optional() }{@code .}{@link
   *       Optional#orElseThrow() orElseThrow()}
   * </ol>
   *
   * @return The value described by a left-handed instance.
   * @throws NoSuchElementException if this is a {@link Right}
   */
  public L orElseThrowLeft() {
    return orElseThrowLeft(NoSuchElementException::new);
  }

  /**
   * If this is a {@link Right}, returns the value, otherwise throws the supplied exception.
   *
   * @param <X> The type of exception supplied by {@code throwable}
   * @param throwableSupplier the supplying function that produces an exception to be thrown
   * @throws X Throws an supplied exception when called on an instance of {@link Left}
   * @return The wrapped value, assuming this is a {@link Right}
   */
  public abstract <X extends Throwable> R orElseThrow(Supplier<? extends X> throwableSupplier)
      throws X;

  /**
   * If this is a {@link Left}, returns the value, otherwise throws the supplied exception.
   *
   * @param <X> The type of exception supplied by {@code throwable}
   * @param throwableSupplier the supplying function that produces an exception to be thrown
   * @throws X Throws a supplied exception when called on an instance of {@link Right}
   * @return The wrapped value, assuming this is a {@link Left}
   */
  public abstract <X extends Throwable> L orElseThrowLeft(Supplier<? extends X> throwableSupplier)
      throws X;

  /**
   * Returns an {@code Either} that reflects the result of applying the given right-handed mapping
   * to this instance.
   *
   * <p>If this is a {@link Left}, returns an either that describes the same value as this instance,
   * but with a unrealized right-hand type that reflects the mapping.
   *
   * <p>If this is a {@link Right}, returns a new {@code Right} that describes the result of the
   * application of the given mapping to the value described by this instance.
   *
   * @param <T> The type that results from applying the transformation to the right-hand type of
   *     this instance.
   * @param mapping A {@link Function} that maps {@code R} to {@code T}.
   * @return A new either with a mapped value if this was a {@link Right}, or {@code this} if this
   *     is a {@link Left}
   */
  public abstract <T> Either<L, T> map(Function<? super R, ? extends T> mapping);

  /**
   * Returns an {@code Either} that reflects the result of applying the given left-handed mapping to
   * this instance.
   *
   * <p>If this is a {@link Left}, returns a new {@code Left} that describes the result of the
   * application of the given mapping to the value described by this instance.
   *
   * <p>If this is a {@link Right}, returns an either that describes the same value as this
   * instance, but with a unrealized right-hand type that reflects the mapping.
   *
   * @param <T> The type that results from applying the transformation to the left-hand type of this
   *     instance.
   * @param mapping A {@link Function} that maps {@code L} to {@code T}.
   * @return A new either with a mapped value if this was a {@link Right}, or {@code this} if this
   *     is a {@link Left}
   */
  public abstract <T> Either<T, R> mapLeft(Function<? super L, ? extends T> mapping);

  /**
   * Returns the result of applying the given {@code Either}-bearing mapping function to the value
   * described by a right-handed instance, or an {@code Either} describing the same value as a
   * left-handed instance but with a unrealized right-hand type that reflects the mapping.
   *
   * @param <T> the type to which right-handed values will be mapped
   * @param mapping the function which maps right-handed values to new eithers
   * @return if this instance is right-handed: the value returned by {@code mapping}; otherwise: an
   *     either that describes the same left-handed value as this instance, but with an unrealized
   *     right-handed type that reflects the given mapping
   */
  public abstract <T> Either<L, T> flatMap(Function<? super R, Either<L, T>> mapping);

  /**
   * Returns the result of applying the given {@code Either}-bearing mapping function to the value
   * described by a right-handed instance, or an {@code Either} describing the same value as a
   * left-handed instance but with a unrealized right-hand type that reflects the mapping.
   *
   * <p>Mirror of {@link #flatMap(Function)}
   *
   * @param <T> the type to which left-handed values will be mapped
   * @param mapping the function with which to map left-handed values
   * @return if this instance is left-handed: the value returned by {@code mapping}; otherwise: an
   *     either that describes the same right-handed value as this instance, but with an unrealized
   *     left-handed type that reflects the given mapping
   */
  public abstract <T> Either<T, R> flatMapLeft(Function<? super L, Either<T, R>> mapping);

  /**
   * Folds the value described by this instance into a value of this instance's right-hand type.
   *
   * @param mapping the mapping from a left-hand value to a right-hand value
   * @return Either the value of a right-handed instance, or the result of applying the mapping
   *     function to the value of a left-handed instance.
   */
  public abstract R fold(final Function<? super L, ? extends R> mapping); /*{
    return Either.upcastFold(this.mapLeft(mapping));
  }*/

  /**
   * Folds the value described by this instance into a value of this instance's left-hand type.
   *
   * @param mapping the mapping from a right-hand value to a left-hand value
   * @return Either the value of a right-handed instance, or the result of applying the mapping
   *     function to the value of a left-handed instance.
   */
  public abstract L foldLeft(final Function<? super R, ? extends L> mapping); /* {
    return Either.upcastFold(this.map(mapping));
  }*/

  /**
   * Folds the value described by this instance into a value of type {@code V}.
   *
   * @param <V> the type to fold the described value into
   * @param foldLeft the mapping from a left-hand value to a value of type {@code V}
   * @param foldRight the mapping from a right-hand value to a value of type {@code V}
   * @return the result of mapping the described value into an instance of type {@code V}
   */
  public abstract <V> V bifold(
      Function<? super L, ? extends V> foldLeft, Function<? super R, ? extends V> foldRight);

  /**
   * Folds the value described by this instance into a value of type {@code V}, discarding
   * left-handed values in favor of a value returned by a supplier.
   *
   * @param <V> the type to fold the described value into
   * @param foldLeft the supplier to use to replace left-handed values
   * @param foldRight the mapping from a right-hand value to a value of type {@code V}
   * @return the result of mapping the described value into an instance of type {@code V}
   */
  public <V> V bifold(Supplier<? extends V> foldLeft, Function<? super R, ? extends V> foldRight) {
    return bifold((_left) -> foldLeft.get(), foldRight);
  }

  /**
   * Folds the value described by this instance into a value of type {@code V}, discarding
   * right-handed values in favor of a value returned by a supplier.
   *
   * @param <V> the type to fold the described value into
   * @param foldLeft the mapping from a left-hand value to a value of type {@code V}
   * @param foldRight the supplier to use to replace right-handed values.
   * @return the result of mapping the described value into an instance of type {@code V}
   */
  public <V> V bifold(Function<? super L, ? extends V> foldLeft, Supplier<? extends V> foldRight) {
    return bifold(foldLeft, (_right) -> foldRight.get());
  }

  /**
   * Get an optional of this either's right-handed type, discarding the left.
   *
   * @return an optional describing the right-handed value of this instance
   */
  public abstract Optional<R> optional(); /*{
    return bifold(left -> Optional.empty(), Optional::of);
  }*/

  /**
   * Get an optional of this either's right-handed type, discarding the right.
   *
   * @return an optional describing the left-handed value of this instance
   */
  public abstract Optional<L> optionalLeft(); /*{
    return bifold(Optional::of, right -> Optional.empty());
  }*/

  /**
   * Get's a stream of the right-handed value of this instance, or an empty stream if this instance
   * is left-handed.
   *
   * @return a stream of the right-handed value of this instance, or an empty stream if this
   *     instance is left-handed.
   */
  public abstract Stream<? extends R> stream();

  /**
   * Get's a stream of the left-handed value of this instance, or an empty stream if this instance
   * is right-handed.
   *
   * @return a stream of the left-handed value of this instance, or an empty stream if this instance
   *     is right-handed.
   */
  public abstract Stream<? extends L> streamLeft();

  /**
   * Returns a new either that is the mirror image of this instance.
   *
   * @return a view of this instance with swapped left and right handedness
   */
  public abstract Either<R, L> swap();

  /**
   * Widen the types of an either.
   *
   * @implNote This method is implemented as an unchecked cast of {@code either} to {@code
   *     Either<L2, R2>}
   * @param <L2> the widened left-handed type; a supertype of {@code L}
   * @param <R2> the widened right-handed type; a supertype of {@code R}
   * @param <L> the original left-handed type
   * @param <R> the original right-handed type
   * @param either a reference to the same instance of {@code Either} with widened generics.
   * @return the widened {@code Either}
   */
  @SuppressWarnings("unchecked")
  public static <L2, R2, L extends L2, R extends R2> Either<L2, R2> upcast(Either<L, R> either) {
    return (Either<L2, R2>) either;
  }

  /**
   * Maps an {@code Either} to a value of type {@code U}, where {@code U} is some common supertype
   * of the left and right-handed types described by the {@code Either}.
   *
   * @param either an {@code Either} whose left and right hand forms describe values that are
   *     mutually assignable to {@code U}
   * @param <U> the common supertype
   * @return the value described by {@code either}
   */
  public static <U> U upcastFold(Either<? extends U, ? extends U> either) {
    return switch (either) {
      case Left<? extends U, ? extends U> left -> left.get();
      case Right<? extends U, ? extends U> right -> right.get();
    };
  }

  /**
   * Apply the given mapping function to the value described by {@code either}.
   *
   * <p>Equivalent to: {@code mapping.apply(} {@link #upcastFold(Either) upcastFold(either)}{@code
   * ))}
   *
   * @param <U> a common supertype of the left and right-handed types of {@code either}
   * @param <V> the return type of {@code mapping}
   * @param either the either to fold
   * @param mapping a mapping from {@code U} to {@code V}
   * @return the result of applying {@code mapping`} to the value described by {@code `either}
   */
  public static <U, V> V bifold(Either<? extends U, ? extends U> either, Function<U, V> mapping) {
    return mapping.apply(upcastFold(either));
  }
}
