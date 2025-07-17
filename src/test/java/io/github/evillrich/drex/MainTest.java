package io.github.evillrich.drex;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void testMain() {
        // Basic test to ensure the main method runs without exceptions
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}