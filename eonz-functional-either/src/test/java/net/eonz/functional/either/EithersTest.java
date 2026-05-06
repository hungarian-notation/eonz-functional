package net.eonz.functional.either;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.eonz.functional.either.EithersUtil.Even;
import net.eonz.functional.either.EithersUtil.Odd;
import net.eonz.testing.Generate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class EithersTest {

  record ListTuple(List<String> lefts, List<Integer> rights) {}

  @Test
  public void testToList() {
    List<Integer> integers = IntStream.range(0, 20).mapToObj(Integer::valueOf).toList();

    var collected = integers.stream().map(EithersUtil::parity).collect(Eithers.toList());
    var odds = integers.stream().filter(x -> x % 2 != 0).map(Odd::new).toList();
    var evens = integers.stream().filter(x -> x % 2 == 0).map(Even::new).toList();

    assertEquals(odds, collected.left());
    assertEquals(evens, collected.right());
  }

  @Test
  public void testToSet() {
    List<Integer> integers = IntStream.range(0, 20).mapToObj(Integer::valueOf).toList();

    var collected = integers.stream().map(EithersUtil::parity).collect(Eithers.toSet());

    var odds =
        integers.stream() //
            .filter(x -> x % 2 != 0)
            .map(Odd::new)
            .collect(Collectors.toSet());

    var evens =
        integers.stream() //
            .filter(x -> x % 2 == 0)
            .map(Even::new)
            .collect(Collectors.toSet());

    assertEquals(odds, collected.left());
    assertEquals(evens, collected.right());
  }

  public static Stream<List<Integer>> intLists() {
    return Generate.getIntegerLists(8, 16);
  }

  @ParameterizedTest
  @MethodSource("intLists")
  public void testTeeing(List<Integer> integers) {

    var collector1 =
        Eithers.teeing(
            Collectors.summingLong(Odd::value),
            Collectors.summingLong(Even::value),
            (odds, evens) -> odds + (evens << 32));

    var collector2 =
        Eithers.teeing(Collectors.summingLong(Odd::value), Collectors.summingLong(Even::value));

    var collected1 = integers.stream().map(EithersUtil::parity).collect(collector1);

    var collected2 = integers.stream().map(EithersUtil::parity).collect(collector2);

    var odds =
        integers.stream() //
            .filter(x -> x % 2 != 0)
            .mapToLong(x -> x)
            .sum();

    var evensPart =
        integers.stream() //
                .filter(x -> x % 2 == 0)
                .mapToLong(x -> x)
                .sum()
            << 32;

    var combined = odds + evensPart;

    assertEquals(combined, collected1);
    assertEquals(combined, collected2.left() + (collected2.right() << 32));
  }
}
