package net.eonz.proposition;

import java.util.function.Predicate;

import net.eonz.proposition.Propose.Unary;
import net.eonz.proposition.Propose.Unit;

public sealed interface Proposition<T, U extends Predicate<T>> permits Unary, Associative {

  public static <T> Unit<T, Predicate<T>> of(Predicate<T> predicate) {
    return new Unit<>(predicate);
  }

  public static <T, U extends Predicate<T>> Proposition<T, U> bind(U predicate) {
    return new Unit<>(predicate);
  }

  boolean test(T value);

  boolean isNegated();

  Proposition<T, U> negate();

  Proposition<T, U> and(Proposition<T, U> other);

  Proposition<T, U> or(Proposition<T, U> other);

  Proposition<T, U> doInterchange();

  /**
   * Apply De Morgan's laws to the proposition tree.
   * 
   * @return
   */
  default Proposition<T, U> interchange(Predicate<Associative<T, U>> filter) {
    return switch (this) {
      case Unary<T, U> _unary -> this;
      case Conjunction<T, U> conjunction -> filter.test(conjunction) ? conjunction.doInterchange() : conjunction;
      case Disjunction<T, U> disjunction -> filter.test(disjunction) ? disjunction.doInterchange() : disjunction;
    };

  }

  default Proposition<T, U> interchange() {
    return interchange(x -> true);
  }

  default Proposition<T, U> and(U other) {
    return and(bind(other));
  }

  default Proposition<T, U> or(U other) {
    return or(bind(other));
  }

  int size();
}