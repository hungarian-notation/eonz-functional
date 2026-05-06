package net.eonz.functional.either;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class LeftTest {

  /** assert that {@code either} is a well-behaving instance of {@link Left} */
  static void assertIsLeft(Either<?, ?> either) {
    assertInstanceOf(Left.class, either);
    assertTrue(either.isLeft());
    assertFalse(either.isRight());
    assertTrue(either.optionalLeft().isPresent());
    assertFalse(either.optional().isPresent());
    assertDoesNotThrow(() -> either.orElseThrowLeft());
    assertDoesNotThrow(() -> either.orElseThrowLeft(UnsupportedOperationException::new));
    assertThrows(
        NoSuchElementException.class,
        () -> {
          either.orElseThrow();
        });
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          either.orElseThrow(UnsupportedOperationException::new);
        });
  }

  @Test
  public void ofLeftTest() {
    LeftTest.assertIsLeft(Either.ofLeft(1));
    LeftTest.assertIsLeft(Left.of(1));
  }

  @Test
  public void patternMatchTest() {
    String value = "STRING";
    Either<?, ?> either = Left.of(value);

    if (either instanceof Left left) {
      assertEquals(value, left.get());
    } else {
      fail();
    }
  }

  @Test
  public void hashCodeTest() {
    testHashCode(0);
    testHashCode("STRING");
    testHashCode(List.of(1, 2, 3));
  }

  private void testHashCode(Object content) {
    assertEquals(~content.hashCode(), Left.of(content).hashCode());
  }

  @Test
  public void toStringTest() {
    assertEquals("Left[1]", Left.of(1).toString());
    assertEquals("Left[Hello!]", Left.of("Hello!").toString());
  }
}
