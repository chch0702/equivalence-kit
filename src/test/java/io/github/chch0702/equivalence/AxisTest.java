package io.github.chch0702.equivalence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AxisTest {

    enum Size { S, M, L }

    @Test
    void 열거형_범위_불리언_축() {
        assertEquals(List.of(Size.S, Size.M, Size.L), Axis.ofEnum("size", Size.class).values());
        assertEquals(List.of(-1, 0, 1), Axis.range("n", -1, 1).values());
        assertEquals(List.of(true, false), Axis.bool("b").values());
        assertEquals(List.of("a", "b"), Axis.of("s", "a", "b").values());
    }

    @Test
    void 이름이_없거나_범위가_거꾸로면_예외() {
        assertThrows(IllegalArgumentException.class, () -> Axis.bool(" "));
        assertThrows(IllegalArgumentException.class, () -> Axis.range("n", 2, 1));
    }

    @Test
    void null_값은_받지_않는다() {
        assertThrows(NullPointerException.class, () -> Axis.of("s", "a", null));
    }

    @Test
    void 값_목록은_바꿀_수_없다() {
        var axis = Axis.of("s", "a");
        assertThrows(UnsupportedOperationException.class, () -> axis.values().add("b"));
    }
}
