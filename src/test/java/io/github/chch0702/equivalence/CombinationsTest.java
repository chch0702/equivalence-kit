package io.github.chch0702.equivalence;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CombinationsTest {

    enum Color { RED, GREEN }

    @Test
    void 축의_곱만큼_조합이_나오고_마지막_축이_가장_빨리_바뀐다() {
        var color = Axis.ofEnum("color", Color.class);
        var n = Axis.range("n", 1, 3);
        var combos = Combinations.of(color, n);

        List<String> seen = new ArrayList<>();
        for (Combination c : combos) {
            seen.add(c.get(color) + "-" + c.get(n));
        }

        assertEquals(6, combos.size());
        assertEquals(List.of("RED-1", "RED-2", "RED-3", "GREEN-1", "GREEN-2", "GREEN-3"), seen);
    }

    @Test
    void 조합_번호와_문자열_표현() {
        var flag = Axis.bool("flag");
        var n = Axis.range("n", 0, 1);
        var list = Combinations.of(flag, n).stream().toList();

        assertEquals(3, list.get(3).ordinal());
        assertEquals("flag=false, n=1", list.get(3).toString());
    }

    @Test
    void 빈_축이_있으면_조합은_0개() {
        Axis<String> empty = Axis.of("empty", List.of());
        var combos = Combinations.of(Axis.bool("b"), empty);

        assertEquals(0, combos.size());
        assertFalse(combos.iterator().hasNext());
        assertThrows(NoSuchElementException.class, () -> combos.iterator().next());
    }

    @Test
    void 조합에_없는_축을_꺼내면_예외() {
        var a = Axis.bool("a");
        var other = Axis.bool("other");
        var first = Combinations.of(a).iterator().next();

        assertThrows(IllegalArgumentException.class, () -> first.get(other));
    }

    @Test
    void 축_이름이_겹치면_예외() {
        assertThrows(IllegalArgumentException.class,
                () -> Combinations.of(Axis.bool("x"), Axis.range("x", 0, 1)));
    }

    @Test
    void 조합_수가_long_범위를_넘으면_예외() {
        var big = Axis.range("big", 0, 99_999);
        assertThrows(ArithmeticException.class,
                () -> Combinations.of(big, Axis.range("b2", 0, 99_999), Axis.range("b3", 0, 99_999),
                        Axis.range("b4", 0, 99_999)));
    }
}
