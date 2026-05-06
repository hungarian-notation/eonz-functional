package net.eonz.functional.either;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An {@link Either} that holds a right-handed value.
 *
 * @param <L> Unrealized left-handed type
 * @param <R> Right-handed type
 */
public final class Right<L, R> extends Either<L, R> implements Supplier<R> {

  /**
   * Returns a new right-handed {@link Either}
   *
   * @param <L> the unrealized left-handed type
   * @param <R> the right-handed type
   * @param value the described right-handed value
   * @return a right-handed either that describes the given value
   * @throws NullPointerException if {@code value} is null.
   */
  public static <L, R> Right<L, R> of(final R value) {
    final R valid = Objects.requireNonNull(value);
    return new Right<>(valid);
  }

  private final R value;

  Right(R value) {
    this.value = value;
  }

  @Override
  public R get() {
    return value;
  }

  /**
   * Convenience method to safely recast the unrealized left-handed type parameter.
   *
   * @param <T> the new left-handed type
   * @return this instance cast to a {@code Right} with a different left-handed type
   */
  @SuppressWarnings("unchecked")
  public final <T> Right<T, R> cast() {
    // Could be implemented without warnings as:
    // return Either.of(this.get());
    return (Right<T, R>) this;
  }

  /**
   * Returns true.
   *
   * @return true
   */
  @Override
  public final boolean isRight() {
    return true;
  }

  /** Performs the given action with the described right-handed value. */
  @Override
  public void ifRight(final Consumer<? super R> action) {
    action.accept(get());
  }

  /** Does nothing. */
  @Override
  public void ifLeft(final Consumer<? super L> action) {}

  /**
   * Returns the described value.
   *
   * @param <X> unused
   * @param supplier unused
   * @return the described value
   */
  @Override
  public <X extends Throwable> R orElseThrow(final Supplier<? extends X> supplier) {
    return get();
  }

  @Override
  public <R2> Right<L, R2> map(final Function<? super R, ? extends R2> mapping) {
    return new Right<L, R2>(mapping.apply(get()));
  }

  @Override
  public <T> Either<L, T> flatMap(final Function<? super R, Either<L, T>> mapping) {
    return mapping.apply(get());
  }

  @Override
  public Optional<R> optional() {
    return Optional.of(get());
  }

  @Override
  public <V> V bifold(
      final Function<? super L, ? extends V> foldLeft,
      final Function<? super R, ? extends V> foldRight) {
    return foldRight.apply(get());
  }

  @Override
  public Stream<R> stream() {
    return Stream.of(get());
  }

  @Override
  public Left<R, L> swap() {
    return new Left<R, L>(get());
  }

  /**
   * Throws the exception supplied by {@code throwable}.
   *
   * @param <X> the exception type thrown.
   * @throws X an exception supplied by {@code throwable}
   */
  @Override
  public final <X extends Throwable> L orElseThrowLeft(final Supplier<? extends X> throwable)
      throws X {
    throw Objects.requireNonNull(throwable.get());
  }

  @Override
  public final <T> Either<T, R> flatMapLeft(final Function<? super L, Either<T, R>> mapping) {
    return cast();
  }

  @Override
  public final <T> Right<T, R> mapLeft(final Function<? super L, ? extends T> mapping) {
    return this.cast();
  }

  @Override
  public final Optional<L> optionalLeft() {
    return Optional.empty();
  }

  @Override
  public final Stream<L> streamLeft() {
    return Stream.empty();
  }

  @Override
  public R fold(final Function<? super L, ? extends R> mapping) {
    return get();
  }

  @Override
  public L foldLeft(final Function<? super R, ? extends L> mapping) {
    return mapping.apply(get());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(get());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj instanceof final Right other) return other.get().equals(get());
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Right[");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }
}
