package net.eonz.propositions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.eonz.proposition.Conjunction;
import net.eonz.proposition.Disjunction;
import net.eonz.proposition.Propose.Unit;
import net.eonz.proposition.Proposition;
import net.eonz.proposition.Associative;

public class PropositionTest {

  /**
   * Contains a truth table for a certain construction of proposition.
   */
  record PropositionTestCaseGroup(
      String label,
      List<Row> table,
      Function<Boolean, Proposition<Object, Tautology>> binding,
      BinaryOperator<Proposition<Object, Tautology>> reduction,
      Function<? super Proposition<Object, Tautology>, ? extends Proposition<Object, Tautology>> finisher) {

    Stream<PropositionTestCase> expand() {
      return table.stream().map(row -> {
        return new PropositionTestCase(row.inputs, row.output, this);
      });
    }
  }

  /**
   * A single row of a truth table from a {@link PropositionTestCaseGroup}
   */
  record PropositionTestCase(List<Boolean> inputs, boolean expected, PropositionTestCaseGroup construction) {

  }

  /**
   * Mock {@link Predicate} that returns a fixed value.
   */
  record Tautology(boolean value) implements Predicate<Object> {
    public static Proposition<Object, Tautology> positive(boolean value) {
      return Proposition.bind(new Tautology(value));
    }

    public static Proposition<Object, Tautology> negative(boolean value) {
      return Proposition.bind(new Tautology(value)).negate();
    }

    @Override
    public boolean test(Object any) {
      return this.value;
    }

    @Override
    public final String toString() {
      return String.valueOf(value).toUpperCase();
    }
  }

  record Variable(String name) implements Predicate<Map<String, Boolean>> {

    @Override
    public boolean test(Map<String, Boolean> env) {
      return env.getOrDefault(env, false);
    }

    @Override
    public final String toString() {
        return name;
    }

  }

  /* These are truth tables. Last column is the truth value of the row. */

  record Row(List<Boolean> inputs, boolean output) {
  }

  private static List<Row> AND = List.of(
      new Row(List.of(true), true),
      new Row(List.of(false), false),
      new Row(List.of(true, true), true),
      new Row(List.of(true, false), false),
      new Row(List.of(false, true), false),
      new Row(List.of(false, false), false),
      new Row(List.of(true, true, true), true),
      new Row(List.of(true, true, false), false),
      new Row(List.of(true, false, true), false),
      new Row(List.of(true, false, false), false),
      new Row(List.of(false, true, true), false),
      new Row(List.of(false, true, false), false),
      new Row(List.of(false, false, true), false),
      new Row(List.of(false, false, false), false));

  private static List<Row> OR = List.of(
      new Row(List.of(true), true),
      new Row(List.of(false), false),
      new Row(List.of(true, true), true),
      new Row(List.of(true, false), true),
      new Row(List.of(false, true), true),
      new Row(List.of(false, false), false),
      new Row(List.of(true, true, true), true),
      new Row(List.of(true, true, false), true),
      new Row(List.of(true, false, true), true),
      new Row(List.of(true, false, false), true),
      new Row(List.of(false, true, true), true),
      new Row(List.of(false, true, false), true),
      new Row(List.of(false, false, true), true),
      new Row(List.of(false, false, false), false));

  private static List<Row> NAND = List.of(
      new Row(List.of(true), false),
      new Row(List.of(false), true),
      new Row(List.of(true, true), false),
      new Row(List.of(true, false), true),
      new Row(List.of(false, true), true),
      new Row(List.of(false, false), true),
      new Row(List.of(true, true, true), false),
      new Row(List.of(true, true, false), true),
      new Row(List.of(true, false, true), true),
      new Row(List.of(true, false, false), true),
      new Row(List.of(false, true, true), true),
      new Row(List.of(false, true, false), true),
      new Row(List.of(false, false, true), true),
      new Row(List.of(false, false, false), true));

  private static List<Row> NOR = List.of(
      new Row(List.of(true), false),
      new Row(List.of(false), true),
      new Row(List.of(true, true), false),
      new Row(List.of(true, false), false),
      new Row(List.of(false, true), false),
      new Row(List.of(false, false), true),
      new Row(List.of(true, true, true), false),
      new Row(List.of(true, true, false), false),
      new Row(List.of(true, false, true), false),
      new Row(List.of(true, false, false), false),
      new Row(List.of(false, true, true), false),
      new Row(List.of(false, true, false), false),
      new Row(List.of(false, false, true), false),
      new Row(List.of(false, false, false), true));

  public static final PropositionTestCaseGroup[] constructions = new PropositionTestCaseGroup[] {
      new PropositionTestCaseGroup("AND", AND, Tautology::positive, Proposition::and, x -> x),
      new PropositionTestCaseGroup("OR", OR, Tautology::positive, Proposition::or, x -> x),
      new PropositionTestCaseGroup("(NOT ∘ AND)", NAND, Tautology::positive, Proposition::and, Proposition::negate),
      new PropositionTestCaseGroup("(OR  ∘ NOT)", NAND, Tautology::negative, Proposition::or, x -> x),
      new PropositionTestCaseGroup("(NOT ∘ OR )", NOR, Tautology::positive, Proposition::or, Proposition::negate),
      new PropositionTestCaseGroup("(AND ∘ NOT)", NOR, Tautology::negative, Proposition::and, x -> x),
  };

  /**
   * @return the constructions
   */
  public static List<PropositionTestCase> getConstructions() {
    return Stream.of(constructions).flatMap(each -> each.expand()).toList();
  }

  @Test
  public void testOf() {
    var blank = Proposition.of(String::isBlank);
    var empty = Proposition.of(String::isEmpty);
    assertInstanceOf(Unit.class, blank);
    var and = blank.and(empty);
    var or = blank.or(empty);
    assertInstanceOf(Associative.class, and);
    assertInstanceOf(Conjunction.class, and);
    assertInstanceOf(Associative.class, or);
    assertInstanceOf(Disjunction.class, or);
    assertEquals(List.of(blank, empty), ((Associative<?, ?>) and).getChildren());
    assertEquals(List.of(blank, empty), ((Associative<?, ?>) and).getChildren());
    assertTrue(and.test(""));
    assertFalse(and.test(" "));
    assertFalse(and.test("X"));
    assertTrue(or.test(""));
    assertTrue(or.test(" "));
    assertFalse(or.test("X"));
  }

  public void testAnd() {
    var blank = Proposition.of(String::isBlank);
    var empty = Proposition.of(String::isEmpty);
    assertInstanceOf(Unit.class, blank);
    var and = blank.and(empty);
    assertInstanceOf(Associative.class, and);
    assertEquals(List.of(blank, empty), ((Associative<?, ?>) and).getChildren());
    assertTrue(and.test(""));
    assertFalse(and.test(" "));
    assertFalse(and.test("X"));

  }

  public void testOr() {
    var blank = Proposition.of(String::isBlank);
    var empty = Proposition.of(String::isEmpty);
    assertInstanceOf(Unit.class, blank);
    var or = blank.or(empty);
    assertInstanceOf(Associative.class, or);
    assertEquals(List.of(blank, empty), ((Associative<?, ?>) or).getChildren());
    assertTrue(or.test(""));
    assertTrue(or.test(" "));
    assertFalse(or.test("X"));
  }

  @ParameterizedTest
  @MethodSource("getConstructions")
  public void testConstruction(PropositionTestCase row) {
    var construction = row.construction;
    var proposition = row.inputs.stream()
        .map(construction.binding())
        .reduce(construction.reduction())
        .map(construction.finisher())
        .orElseThrow();
    assertEquals(row.inputs.size(), proposition.size());
    assertEquals(row.expected, proposition.test(null));
    System.out.println(proposition);

    var equivalent = proposition.interchange();
    assertEquals(row.inputs.size(), equivalent.size());
    assertEquals(row.expected, equivalent.test(null));
    System.out.println(equivalent);

    if (row.inputs.size() > 1) {
      System.out.println("size >1");
      assertNotSame(proposition, equivalent);
      assertNotEquals(proposition.isNegated(), equivalent);
    } else {
      System.out.println("size <=1");
      assertSame(proposition, equivalent);
    }
  }

  // @Test
  // public void simplify() {
  //   // fail("unimplemented");
  // }
}
