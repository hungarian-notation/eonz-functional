package net.eonz.proposition;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Conjunction<T, P extends Predicate<T>> extends Associative<T, P> {
    public Conjunction(boolean negation, List<? extends Proposition<T, P>> children) {
      super(negation, children);
    }

    public Disjunction<T, P> doInterchange() {
      return new Disjunction<>(!isNegated(),
          getChildren().stream().map(each -> each.doInterchange()).toList());
    }

    public Conjunction(List<? extends Proposition<T, P>> children) {
      super(false, children);
    }

    @Override
    public boolean test(T value) {
      for (var each : getChildren()) {
        if (each.test(value) == false) {
          return false ^ isNegated();
        }
      }
      return true ^ isNegated();
    }

    @Override
    public Conjunction<T, P> negate() {
      return new Conjunction<T, P>(!isNegated(), getChildren());
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (isNegated())
        builder.append("(not ");
      builder.append("(and ");
      builder.append(children.stream().map(Object::toString).collect(Collectors.joining(" ")));
      builder.append(")");
      if (isNegated())
        builder.append(")");
      return builder.toString();
    }

  }