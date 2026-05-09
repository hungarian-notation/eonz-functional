package net.eonz.proposition;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Disjunction<T, P extends Predicate<T>> extends Associative<T, P> {

    public Disjunction(boolean negation, List<? extends Proposition<T, P>> children) {
      super(negation, children);
    }

    public Conjunction<T, P> doInterchange() {
      return new Conjunction<>(!isNegated(),
          getChildren().stream().map(each -> each.doInterchange()).toList());
    }

    public Disjunction(List<? extends Proposition<T, P>> children) {
      super(false, children);
    }

    @Override
    public boolean test(T value) {

      for (var each : getChildren()) {
        if (each.test(value) == true) {
          return true ^ isNegated();
        }
      }

      return false ^ isNegated();
    }

    @Override
    public Disjunction<T, P> negate() {
      return new Disjunction<T, P>(!isNegated(), getChildren());
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (isNegated())
        builder.append("(not ");
      builder.append("(or ");
      builder.append(children.stream().map(Object::toString).collect(Collectors.joining(" ")));
      builder.append(")");
      if (isNegated())
        builder.append(")");
      return builder.toString();
    }

  }