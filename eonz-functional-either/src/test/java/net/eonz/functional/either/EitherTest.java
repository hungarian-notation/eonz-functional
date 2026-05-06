package net.eonz.functional.either;

import static net.eonz.functional.either.EithersUtil.*;
import static net.eonz.functional.either.LeftTest.assertIsLeft;
import static net.eonz.functional.either.RightTest.assertIsRight;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.eonz.functional.either.EithersUtil.Even;
import net.eonz.functional.either.EithersUtil.Odd;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class EitherTest {

  @Test
  public void ofStaticConstructor() {
    var ints =
        IntStream.range(0, 20)
            .boxed()
            .map(x -> x % 2 == 0 ? x : null)
            .map(Optional::ofNullable)
            .map(each -> Either.of(each, NullPointerException::new))
            .toList();

    for (int i = 0; i < 10; ++i) {
      var a = ints.get(i * 2);
      var b = ints.get(i * 2 + 1);

      assertEquals(Right.of(i * 2), a);
      assertInstanceOf(Left.class, b);
      assertInstanceOf(NullPointerException.class, b.orElseThrowLeft());
    }
  }

  @Test
  public void ofStaticConstructor_Snippet() {
    Optional<?> a = Optional.empty();
    Optional<String> b = Optional.of("B");
    Either<String, ?> either = Either.of(a, b::orElseThrow);
    assertIsLeft(either);
  }

  @Test
  public void ofNullableStaticConstructor() {
    var ints =
        IntStream.range(0, 20)
            .boxed()
            .map(x -> x % 2 == 0 ? x : null)
            .map(each -> Either.ofNullable(each, NullPointerException::new))
            .toList();

    for (int i = 0; i < 10; ++i) {
      var a = ints.get(i * 2);
      var b = ints.get(i * 2 + 1);

      assertEquals(Right.of(i * 2), a);
      assertInstanceOf(Left.class, b);
      assertInstanceOf(NullPointerException.class, b.orElseThrowLeft());
    }
  }

  @Test
  public void equalsTest() {
    assertEquals(Right.of(new Even(0)), parity(0));
    assertNotEquals(Left.of(new Even(0)), parity(0));
    assertEquals(Left.of(new Even(0)), parity(0).swap());
    assertEquals(Left.of(new Odd(1)), parity(1));
    assertNotEquals(Right.of(new Odd(1)), parity(1));
    assertEquals(Right.of(new Odd(1)), parity(1).swap());
  }

  @ParameterizedTest
  @MethodSource("net.eonz.testing.Generate#ints_64")
  public void hashCodeTest(Object value) {
    assertEquals(~Right.of(value).hashCode(), Left.of(value).hashCode());
  }

  @Test
  public void swapTest() {
    List<?> value = List.of(1);
    var original = Either.ofRight(value);
    var swapped = original.swap();
    var doubleSwapped = swapped.swap();
    assertIsRight(original);
    assertIsLeft(swapped);
    assertIsRight(doubleSwapped);
    assertSame(value, original.orElseThrow());
    assertSame(value, swapped.orElseThrowLeft());
    assertSame(value, doubleSwapped.orElseThrow());
    assertEquals(original, doubleSwapped);
  }

  @Test
  public void mapTest() {
    Either<Integer, Integer> a = Right.of(2);
    Either<Integer, Integer> b = Left.of(3);

    Function<? super Integer, ? extends Integer> timesTwo = (x) -> x * 2;

    assertEquals(Right.of(4), a.map(timesTwo));
    assertEquals(Right.of(2), a.mapLeft(timesTwo));
    assertEquals(Left.of(3), b.map(timesTwo));
    assertEquals(Left.of(6), b.mapLeft(timesTwo));
  }

  @Test
  public void flatMapTest() {
    var ints = IntStream.rangeClosed(0, 8).boxed().toList();
    var eithers =
        ints.stream()
            .map(each -> halfParity(each))
            .map(each -> each.map(x -> x / 2))
            .map(each -> each.flatMap(EitherTest::halfParity))
            .toList();
    var expected =
        List.of(
            Right.of(0),
            Left.of(1),
            Left.of(1),
            Left.of(3),
            Right.of(2),
            Left.of(5),
            Left.of(3),
            Left.of(7),
            Right.of(4));
    assertEquals(expected, eithers);
    var eithers2 =
        eithers.stream()
            .map(
                e ->
                    e.flatMapLeft(
                        (Integer each) -> {
                          /* double all lefts less than 5 */
                          if (each < 5) {
                            return Either.ofRight(each * 2);
                          } else {
                            return Either.ofLeft(each);
                          }
                        }))
            .toList();
    var expected2 =
        List.of(
            Right.of(0),
            Right.of(2),
            Right.of(2),
            Right.of(6),
            Right.of(2),
            Left.of(5),
            Right.of(6),
            Left.of(7),
            Right.of(4));
    assertEquals(expected2, eithers2);
  }

  private static Either<Integer, Integer> halfParity(Integer each) {
    if (each % 2 != 0) {
      return Either.<Integer, Integer>ofLeft(each);
    } else {
      return Either.<Integer, Integer>ofRight(each);
    }
  }

  @Test
  public void foldTest() {
    Either<Double, String> a = Right.of("2");
    Either<Double, String> b = Left.of(2.5);
    assertEquals("2", a.fold(String::valueOf));
    assertEquals("2.5", b.fold(String::valueOf));
    assertEquals(2.0, a.foldLeft(Double::parseDouble));
    assertEquals(2.5, b.foldLeft(Double::parseDouble));
  }

  @Test
  public void bifoldTest() {
    Either<Double, Integer> a = Right.of(2);
    Either<Double, Integer> b = Left.of(2.5);
    assertEquals(2, a.fold(Double::intValue));
    assertEquals(2, b.fold(Double::intValue));
    assertEquals(2.0, a.foldLeft(Integer::doubleValue));
    assertEquals(2.5, b.foldLeft(Integer::doubleValue));
    assertEquals("2", Either.bifold(a, String::valueOf));
    assertEquals("2.5", Either.bifold(b, String::valueOf));
    assertEquals(2, Either.bifold(a, Number::intValue));
    assertEquals(2, Either.bifold(b, Number::intValue));
    assertEquals("2", a.bifold(String::valueOf, String::valueOf));
    assertEquals("2.5", b.bifold(String::valueOf, String::valueOf));
  }

  @Test
  public void bifoldSupplierTest() {
    Either<Double, Integer> a = Right.of(2);
    Either<Double, Integer> b = Left.of(2.5);
    Supplier<Integer> zero = () -> Integer.valueOf(0);
    Supplier<Double> infinity = () -> Double.POSITIVE_INFINITY;
    assertEquals(2, a.bifold(zero, Integer::valueOf));
    assertEquals(0, b.bifold(zero, Integer::valueOf));
    assertEquals(Double.POSITIVE_INFINITY, a.bifold(Double::valueOf, infinity));
    assertEquals(2.5, b.bifold(Double::valueOf, infinity));
  }

  @Test
  public void upcastTest() {
    Either<Double, Integer> a = Right.of(2);
    Either<Double, Integer> b = Left.of(2.5);
    Either<Number, Number> a2 = Either.upcast(a);
    Either<Number, Number> b2 = Either.upcast(b);
    assertEquals("2", Either.bifold(a2, String::valueOf));
    assertEquals("2.5", Either.bifold(b2, String::valueOf));
    assertEquals(2, Either.bifold(a2, Number::intValue));
    assertEquals(2, Either.bifold(b2, Number::intValue));
    assertEquals(2, Either.<Number>upcastFold(a).intValue());
    assertEquals(2, Either.<Number>upcastFold(b).intValue());
    assertEquals(2.0, Either.<Number>upcastFold(a).doubleValue());
    assertEquals(2.5, Either.<Number>upcastFold(b).doubleValue());
    assertEquals("2", String.valueOf(Either.<Object>upcastFold(a)));
    assertEquals("2.5", String.valueOf(Either.<Object>upcastFold(b)));
  }

  @Test
  public void optionalTest() {
    Either<Double, Integer> a = Right.of(2);
    Either<Double, Integer> b = Left.of(2.5);
    assertTrue(a.optional().isPresent());
    assertTrue(a.swap().optionalLeft().isPresent());
    assertFalse(a.optionalLeft().isPresent());
    assertFalse(a.swap().optional().isPresent());
    assertFalse(b.optional().isPresent());
    assertFalse(b.swap().optionalLeft().isPresent());
    assertTrue(b.optionalLeft().isPresent());
    assertTrue(b.swap().optional().isPresent());
  }

  @Test
  public void nullityExceptionTests() {
    assertThrows(NullPointerException.class, () -> Right.of(null));
    assertThrows(NullPointerException.class, () -> Left.of(null));
    assertThrows(NullPointerException.class, () -> Either.ofLeft(null));
    assertThrows(NullPointerException.class, () -> Either.ofRight(null));
    assertThrows(NullPointerException.class, () -> Either.of(Optional.empty(), null));
    assertThrows(NullPointerException.class, () -> Either.of(Optional.empty(), () -> null));
    assertThrows(NullPointerException.class, () -> Either.of(null, () -> ""));

    assertThrows(NullPointerException.class, () -> Either.ofNullable(null, null));
    assertThrows(NullPointerException.class, () -> Either.ofNullable(null, () -> null));
    assertDoesNotThrow(() -> Either.ofNullable(null, () -> ""));
  }

  @Test
  public void StreamTest() {
    var ints = IntStream.range(0, 20).boxed().map(EithersUtil::parity).toList();
    var evens = ints.stream().flatMap(Either::stream).map(Even::value).toList();
    var odds = ints.stream().flatMap(Either::streamLeft).map(Odd::value).toList();
    var expectedEvens = IntStream.range(0, 10).map(x -> x * 2).boxed().toList();
    var expectedOdds = IntStream.range(0, 10).map(x -> x * 2 + 1).boxed().toList();
    assertEquals(expectedEvens, evens);
    assertEquals(expectedOdds, odds);
  }
}
