package net.eonz.proposition;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class Propose {

  public static enum LogicOperator {
    AND, OR;

    // public LogicOperator negate() {
    // return switch (this) {
    // case AND -> LogicOperator.NAND;
    // case NAND -> LogicOperator.AND;
    // case OR -> LogicOperator.NOR;
    // case NOR -> LogicOperator.OR;
    // };
    // }

    // /**
    // *
    // * @return
    // */
    // public LogicOperator rewritable() {
    // return switch (this) {
    // case AND -> LogicOperator.NOR;
    // case NAND -> LogicOperator.OR;
    // case OR -> LogicOperator.NAND;
    // case NOR -> LogicOperator.AND;
    // };
    // }

  }

  public sealed interface Unary<T, P extends Predicate<T>> extends Proposition<T, P> {
    public default Associative<T, P> and(Proposition<T, P> other) {
      return associativePP(LogicOperator.AND, this, other);
    }

    public default Associative<T, P> or(Proposition<T, P> other) {
      return associativePP(LogicOperator.OR, this, other);
    }
  }

  public static final class Unit<T, P extends Predicate<T>> implements Unary<T, P> {

    private final P child;
    private final boolean negated;
    private final Unit<T, P> inverse;

    /**
     * @param child
     */
    public Unit(boolean negate, P child) {
      this.negated = negate;
      this.child = child;
      this.inverse = new Unit<>(this);
    }

    public Unit(Unit<T, P> negation) {
      this.negated = !negation.negated;
      this.child = negation.child;
      this.inverse = negation;
    }

    /**
     * @param child
     */
    public Unit(P child) {
      this(false, child);
    }

    public P getChild() {
      return child;
    }

    @Override
    public Unit<T, P> negate() {
      return inverse;
    }

    @Override
    public boolean test(T value) {
      return negated ^ child.test(value);
    }

    public int size() {
      return 1;
    }

    @Override
    public boolean isNegated() {
      return negated;
    }

    @Override
    public Proposition<T, P> doInterchange() {
      return negate();
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (isNegated())
        builder.append("(not ");
      builder.append(child);
      if (isNegated())
        builder.append(")");
      return builder.toString();
    }

  }

  private static <T, U extends Predicate<T>> Associative<T, U> create(LogicOperator operator,
      Stream<Proposition<T, U>> stream) {
    switch (operator) {
      case AND:
        return new Conjunction<T, U>(stream.toList());
      case OR:
        return new Disjunction<T, U>(stream.toList());
      default:
        throw new IllegalArgumentException();
    }
  }

  private static <T, U extends Predicate<T>> Associative<T, U> associativeVV(
      LogicOperator operator,
      Associative<T, U> left,
      Associative<T, U> right) {
    return create(operator, Stream.concat(left.children.stream(), right.children.stream()));
  }

  private static <T, U extends Predicate<T>> Associative<T, U> associativeUV(
      LogicOperator operator,
      Unary<T, U> left,
      Associative<T, U> right) {
    return create(operator, Stream.<Proposition<T, U>>concat(Stream.of(left), right.children.stream()));
  }

  private static <T, U extends Predicate<T>> Associative<T, U> associativeVU(
      LogicOperator operator,
      Associative<T, U> left,
      Unary<T, U> right) {
    return create(operator, Stream.<Proposition<T, U>>concat(left.children.stream(), Stream.of(right)));
  }

  private static <T, U extends Predicate<T>> Associative<T, U> associativeUU(
      LogicOperator operator,
      Unary<T, U> left,
      Unary<T, U> right) {
    return create(operator, Stream.<Proposition<T, U>>of(left, right));
  }

  static <T, P extends Predicate<T>> Associative<T, P> associativePP(
      LogicOperator operator,
      Proposition<T, P> left,
      Proposition<T, P> right) {
    return switch (left) {
      case Associative<T, P> lv -> switch (right) {
        case Associative<T, P> rv -> associativeVV(operator, lv, rv);
        case Unary<T, P> rm -> associativeVU(operator, lv, rm);
      };
      case Unary<T, P> lm -> switch (right) {
        case Associative<T, P> rv -> associativeUV(operator, lm, rv);
        case Unary<T, P> rm -> associativeUU(operator, lm, rm);
      };
    };
  }

  public static <T, P extends Predicate<T>, L extends Proposition<T, P>, R extends Proposition<T, P>> Associative<T, P> and(
      L left,
      R right) {
    return associativePP(LogicOperator.AND, left, right);
  }

  public static <T, P extends Predicate<T>, L extends Proposition<T, P>, R extends Proposition<T, P>> Associative<T, P> or(
      L left,
      R right) {
    return associativePP(LogicOperator.OR, left, right);
  }
}
