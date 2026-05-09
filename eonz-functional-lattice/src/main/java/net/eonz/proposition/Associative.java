package net.eonz.proposition;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.eonz.proposition.Propose.LogicOperator;

public abstract sealed class Associative<T, U extends Predicate<T>> implements Proposition<T, U>
    permits Conjunction, Disjunction {

  protected final List<? extends Proposition<T, U>> children;
  protected final boolean negation;

  /**
   * @return the negation
   */
  public boolean isNegated() {
    return negation;
  }

  /**
   * @param operator
   * @param children
   */
  public Associative(boolean negation, List<? extends Proposition<T, U>> children) {
    this.negation = negation;
    this.children = List.copyOf(children);
  }

  /**
   * @param operator
   * @param children
   */
  public Associative(boolean negation, Stream<? extends Proposition<T, U>> children) {
    this.negation = negation;
    this.children = children.toList();
  }

  public List<? extends Proposition<T, U>> getChildren() {
    return children;
  }

  public Associative<T, U> and(Proposition<T, U> other) {
    return Propose.associativePP(LogicOperator.AND, this, other);
  }

  public Associative<T, U> or(Proposition<T, U> other) {
    return Propose.associativePP(LogicOperator.OR, this, other);
  }

  public int size() {
    return children.stream().mapToInt(Proposition::size).sum();
  }

}