package net.eonz.functional.either;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An {@link Either} that holds a left-handed value.
 *
 * @param <L> Left-handed type
 * @param <R> Unrealized right-handed type
 */
public final class Left<L, R> extends Either<L, R> implements Supplier<L> {

  /**
   * Returns a new left-handed {@link Either}
   *
   * @param <L> the left-handed type
   * @param <R> the unrealized right-handed type
   * @param value the described left-handed value
   * @return a left-handed either that describes the given value
   * @throws NullPointerException if {@code value} is null.
   */
  public static <L, R> Left<L, R> of(final L value) {
    final L valid = Objects.requireNonNull(value);
    return new Left<>(valid);
  }

  private final L value;

  /**
   * @hidden
   */
  Left(final L value) {
    this.value = value;
  }

  @Override
  public L get() {
    return value;
  }

  /**
   * Convenience method to safely recast the unrealized right-handed type parameter.
   *
   * @param <T> the new right-handed type
   * @return this instance cast to a {@code Left} with a different right-handed type
   */
  @SuppressWarnings("unchecked")
  public final <T> Left<L, T> cast() {
    // Could be implemented without warnings as:
    // return Either.ofLeft(this.get());
    return (Left<L, T>) this;
  }

  /**
   * Returns false.
   *
   * @return false
   */
  @Override
  public final boolean isRight() {
    return false;
  }

  /** Performs the given action with the described left-handed value. */
  @Override
  public void ifLeft(final Consumer<? super L> action) {
    action.accept(get());
  }

  /** Does nothing. */
  @Override
  public void ifRight(final Consumer<? super R> action) {}

  /**
   * Returns the described value.
   *
   * @param <X> unused
   * @param supplier unused
   * @return the described value
   */
  @Override
  public <X extends Throwable> L orElseThrowLeft(final Supplier<? extends X> supplier) {
    return get();
  }

  @Override
  public <L2> Left<L2, R> mapLeft(final Function<? super L, ? extends L2> mapping) {
    return new Left<L2, R>(mapping.apply(get()));
  }

  @Override
  public <T> Either<T, R> flatMapLeft(final Function<? super L, Either<T, R>> mapping) {
    return mapping.apply(get());
  }

  @Override
  public Optional<L> optionalLeft() {
    return Optional.of(get());
  }

  @Override
  public <V> V bifold(
      final Function<? super L, ? extends V> foldLeft,
      final Function<? super R, ? extends V> foldRight) {
    return foldLeft.apply(get());
  }

  @Override
  public Stream<L> streamLeft() {
    return Stream.of(get());
  }

  @Override
  public Right<R, L> swap() {
    return new Right<R, L>(get());
  }

  @Override
  public final <X extends Throwable> R orElseThrow(final Supplier<? extends X> supplier) throws X {
    throw Objects.requireNonNull(supplier.get());
  }

  @Override
  public final <R2> Left<L, R2> map(final Function<? super R, ? extends R2> mapping) {
    return cast();
  }

  @Override
  public final <T> Either<L, T> flatMap(final Function<? super R, Either<L, T>> mapping) {
    return cast();
  }

  @Override
  public final Optional<R> optional() {
    return Optional.empty();
  }

  @Override
  public final Stream<R> stream() {
    return Stream.empty();
  }

  @Override
  public R fold(final Function<? super L, ? extends R> mapping) {
    return mapping.apply(get());
  }

  @Override
  public L foldLeft(final Function<? super R, ? extends L> mapping) {
    return get();
  }

  @Override
  public int hashCode() {
    return ~Objects.hashCode(get());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj instanceof final Left other) return other.get().equals(get());
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Left[");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }
}
