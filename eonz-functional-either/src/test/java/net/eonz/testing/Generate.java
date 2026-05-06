package net.eonz.testing;

import java.lang.Character.UnicodeBlock;
import java.lang.Character.UnicodeScript;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.SequencedCollection;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

/** Quick and dirty utility for generating reproducible arbitrary test parameters. */
public class Generate {

  static interface Generator extends Function<Random, String> {
    default double weight() {
      return 1.0;
    }
  }

  static record WeightedGenerator(Function<Random, String> generator, double weight)
      implements Generator {

    @Override
    public String apply(Random rnd) {
      return generator().apply(rnd);
    }

    public static WeightedGenerator of(Function<Random, String> generator, double weight) {
      return new WeightedGenerator(generator, weight);
    }
  }

  public static int SEED = 0xCAFECAFE;

  public static Stream<List<Integer>> getIntegerLists(int count, int listSize) {
    return getIntegerLists(new Random(SEED), count, listSize);
  }

  public static IntStream getIntegers(int count) {
    return getIntegers(new Random(SEED), count);
  }

  public static Stream<List<Integer>> ints_8x8() {
    return getIntegerLists(8, 8);
  }

  public static Stream<List<Integer>> ints_64x64() {
    return getIntegerLists(64, 64);
  }

  public static IntStream ints_64() {
    return getIntegers(64);
  }

  public static IntStream ints_1024() {
    return getIntegers(1024);
  }

  public static DoubleStream doubles_64() {
    return getDoubles(new Random(SEED), 64);
  }

  public static DoubleStream doubles_1024() {
    return getDoubles(new Random(SEED), 1024);
  }

  public static Stream<Object> objects_64() {
    return getObjects(new Random(SEED), 64);
  }

  public static Stream<Object> objects_1024() {
    return getObjects(new Random(SEED), 1024);
  }

  public static Stream<Object> strings_64() {
    return getStrings(new Random(SEED), 64);
  }

  public static Stream<Object> strings_1024() {
    return getStrings(new Random(SEED), 1024);
  }

  public static ArrayList<Integer> range(int from, int to) {
    return IntStream.rangeClosed(from, to)
        .mapToObj(Integer::valueOf)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  static Stream<Object> getStrings(Random rnd, int amount) {
    var builder = Stream.builder();
    var generators = defaultGenerators();
    for (int i = 0; i < amount; ++i) {
      int type = rnd.nextInt(generators.size());
      builder.add(generators.get(type).apply(rnd));
    }
    return builder.build();
  }

  static Object getObjects(Random rnd) {
    int type = rnd.nextInt(2);
    if (type == 0) {
      return generate(rnd);
    } else if (type == 1) {
      int type2 = rnd.nextInt(2);
      if (type2 == 0) return Integer.valueOf(rnd.nextInt());
      return Double.valueOf(rnd.nextDouble());
    } else {
      throw new Error();
    }
  }

  static Stream<Object> getObjects(Random rnd, int amount) {
    var builder = Stream.builder();

    if (amount > 3) {
      builder.add("");
      builder.add(0);
      builder.add(Double.NaN);
      amount -= 3;
    }
    if (amount > 1) {
      builder.add(new Object[] {});
      amount -= 1;
    }

    for (int i = 0; i < amount; ++i) {
      builder.add(provideArbitraryObject(rnd));
    }

    return builder.build().map(each -> new Object[] {each});
  }

  static IntStream getIntegers(Random rnd, int amount, boolean edgeCases) {
    var builder = IntStream.builder();

    if (edgeCases && amount > 5) {
      builder.add(0);
      builder.add(1);
      builder.add(Integer.MAX_VALUE);
      builder.add(-1);
      builder.add(Integer.MIN_VALUE);
      amount -= 5;
    }

    for (int i = 0; i < amount; ++i) {
      builder.add(rnd.nextInt());
    }

    return builder.build();
  }

  static IntStream getIntegers(Random rnd, int amount) {
    return getIntegers(rnd, amount, amount > 10);
  }

  static Stream<List<Integer>> getIntegerLists(Random rnd, int amount, int size) {
    Builder<List<Integer>> builder = Stream.builder();

    for (int i = 0; i < size; ++i) {
      builder.add(getIntegers(rnd, amount, i == 0).boxed().toList());
    }

    return builder.build();
  }

  static DoubleStream getDoubles(Random rnd, int amount) {
    var builder = DoubleStream.builder();

    if (amount > 10) {
      builder.add(Double.NEGATIVE_INFINITY);
      builder.add(Double.POSITIVE_INFINITY);
      builder.add(Double.NaN);
      amount -= 3;
    }

    for (int i = 0; i < amount; ++i) {
      builder.add(rnd.nextDouble());
    }

    return builder.build();
  }

  static boolean isPrintable(int codePoint) {
    int type = Character.getType(codePoint);
    switch (type) {
      case Character.UNASSIGNED:
      case Character.CONTROL:
      case Character.FORMAT:
      case Character.PRIVATE_USE:
      case Character.SURROGATE:
      case Character.SPACE_SEPARATOR:
      case Character.LINE_SEPARATOR:
      case Character.PARAGRAPH_SEPARATOR:
        return false;
      default:
        return true;
    }
  }

  static String spaces(Random rnd) {
    return " ";
  }

  static WeightedGenerator spaces(double weight) {
    return WeightedGenerator.of(Generate::spaces, weight);
  }

  static String randomUnicode(IntSupplier supplier, IntPredicate predicate) {
    while (true) {
      int codePoint = supplier.getAsInt();
      if (predicate.test(codePoint)) {
        return Character.toString(codePoint);
      }
    }
  }

  static String randomUnicode(Random rnd, IntPredicate predicate) {
    return randomUnicode(() -> rnd.nextInt(0, Character.MAX_CODE_POINT), predicate);
  }

  static String randomPrintable(Random rnd) {
    return randomUnicode(rnd, ((IntPredicate) Generate::isPrintable));
  }

  static String randomEmoji(Random rnd) {
    return randomUnicode(rnd, Character::isEmoji);
  }

  static String randomAscii(Random rnd) {
    return randomUnicode(() -> rnd.nextInt(0x7f), Generate::isPrintable);
  }

  static String randomLatin(Random rnd) {
    return randomUnicode(rnd, inBlocks(UnicodeBlock.BASIC_LATIN).and(Character::isAlphabetic));
  }

  static IntPredicate inBlocks(UnicodeBlock... blocks) {
    return inBlocks(Arrays.asList(blocks));
  }

  static IntPredicate inScripts(UnicodeScript... blocks) {
    return inScripts(Arrays.asList(blocks));
  }

  static String generate(Random rnd, SequencedCollection<Generator> generators) {
    int length = rnd.nextInt(0, 128);
    var builder = new StringBuilder();
    double weightTotal = totalWeight(generators);

    outer:
    for (int i = 0; i < length; ++i) {
      while (true) {
        var roll = rnd.nextDouble(weightTotal);
        for (var gen : generators) {
          if (roll <= gen.weight()) {
            builder.append(gen.apply(rnd));
            continue outer;
          } else {
            roll -= gen.weight();
          }
        }
      }
    }

    return builder.toString().trim();
  }

  static String generate(Random rnd, Generator... generators) {
    return generate(
        rnd,
        generators.length > 0 ? Arrays.asList(generators) : List.of(Generate::randomPrintable));
  }

  private static IntPredicate inBlocks(List<UnicodeBlock> blocks) {
    return (codePoint) -> blocks.contains(UnicodeBlock.of(codePoint));
  }

  private static IntPredicate inScripts(List<UnicodeScript> blocks) {
    return (codePoint) -> blocks.contains(UnicodeScript.of(codePoint));
  }

  private static double totalWeight(SequencedCollection<Generator> generators) {
    double weightTotal = 0;
    for (var generator : generators) weightTotal += generator.weight();
    return weightTotal;
  }

  private static List<Function<Random, String>> defaultGenerators() {
    List<Function<Random, String>> generators = new ArrayList<>();

    generators.add((rnd) -> generate(rnd, Generate::randomLatin, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomLatin, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomLatin, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomLatin, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomAscii, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomAscii, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomAscii, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomPrintable, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomPrintable, Generate.spaces(0.1)));
    generators.add((rnd) -> generate(rnd, Generate::randomEmoji, Generate.spaces(0.1)));

    return generators;
  }

  private static Object provideArbitraryObject(Random random) {
    return getObjects(random);
  }
}
