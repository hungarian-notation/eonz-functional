package net.eonz.functional.either;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class RightTest {

  /** assert that {@code either} is a well-behaving instance of {@link Right} */
  static void assertIsRight(Either<?, ?> either) {
    assertInstanceOf(Right.class, either);
    assertTrue(either.isRight());
    assertFalse(either.isLeft());
    assertTrue(either.optional().isPresent());
    assertFalse(either.optionalLeft().isPresent());
    assertDoesNotThrow(() -> either.orElseThrow());
    assertDoesNotThrow(() -> either.orElseThrow(UnsupportedOperationException::new));
    assertThrows(
        NoSuchElementException.class,
        () -> {
          either.orElseThrowLeft();
        });
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          either.orElseThrowLeft(UnsupportedOperationException::new);
        });
  }

  @Test
  public void ofRightTest() {
    RightTest.assertIsRight(Either.ofRight(1));
    RightTest.assertIsRight(Right.of(1));
  }

  @Test
  public void patternMatchTest() {
    String value = "STRING";
    Either<?, ?> either = Right.of(value);

    if (either instanceof Right left) {
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
    assertEquals(content.hashCode(), Right.of(content).hashCode());
  }

  @Test
  public void toStringTest() {
    assertEquals("Right[1]", Right.of(1).toString());
    assertEquals("Right[Hello!]", Right.of("Hello!").toString());
  }
}
