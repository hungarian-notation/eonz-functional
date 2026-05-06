package net.eonz.functional.either;

public class EithersUtil {

  public static interface Parity {
    int value();
  }

  public static record Even(int value) implements Parity {}

  public static record Odd(int value) implements Parity {}

  public static Either<Odd, Even> parity(Integer value) {
    if (value % 2 == 0) {
      return Either.ofRight(new Even(value));
    } else {
      return Either.ofLeft(new Odd(value));
    }
  }
}
