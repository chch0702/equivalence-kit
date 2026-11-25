package io.github.chch0702.equivalence;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquivalenceCheckTest {

    static final Axis<Integer> A = Axis.range("a", 0, 9);
    static final Axis<Integer> B = Axis.range("b", 0, 9);

    static EquivalenceCheck<int[], Integer> check(Function<int[], Integer> reference, Function<int[], Integer> candidate) {
        return EquivalenceCheck
                .over(Combinations.of(A, B))
                .input(c -> new int[]{c.get(A), c.get(B)})
                .reference(reference)
                .candidate(candidate);
    }

    @Test
    void 같은_구현이면_불일치_0건() {
        var result = check(in -> in[0] + in[1], in -> in[1] + in[0]).run();

        assertEquals(100, result.checked());
        assertTrue(result.isEquivalent());
        assertDoesNotThrow(result::assertEquivalent);
        assertTrue(result.report().startsWith("불일치 0건 / 전체 100조합"));
    }

    @Test
    void 불일치는_앞_N건만_모으고_전체_건수는_끝까지_센다() {
        // a 가 짝수인 50개 조합에서만 다르다
        var result = check(in -> in[0] * in[1], in -> in[0] % 2 == 0 ? -1 : in[0] * in[1])
                .maxReported(3)
                .run();

        assertEquals(100, result.checked());
        assertEquals(50, result.mismatchCount());
        assertEquals(3, result.firstMismatches().size());
        assertEquals(0, result.firstMismatches().get(0).combination().ordinal());
        assertTrue(result.report().contains("(앞 3건만 표시)"), result.report());

        var error = assertThrows(AssertionError.class, result::assertEquivalent);
        assertTrue(error.getMessage().contains("a=0, b=0"), error.getMessage());
    }

    @Test
    void maxReported_0이면_건수만_센다() {
        var result = check(in -> 1, in -> 2).maxReported(0).run();

        assertEquals(100, result.mismatchCount());
        assertTrue(result.firstMismatches().isEmpty());
    }

    @Test
    void 같은_종류의_예외는_같은_결과로_본다() {
        Function<int[], Integer> divide = in -> in[0] / in[1];
        var result = check(divide, in -> Math.floorDiv(in[0], in[1])).run();

        // 0 으로 나누면 둘 다 ArithmeticException → 같음, 나머지는 양수 나눗셈이라 같음
        assertTrue(result.isEquivalent(), result.report());
    }

    @Test
    void 한쪽만_예외를_던지면_불일치() {
        var result = check(in -> in[0], in -> {
            if (in[0] == 5 && in[1] == 5) {
                throw new IllegalStateException("고장");
            }
            return in[0];
        }).run();

        assertEquals(1, result.mismatchCount());
        var m = result.firstMismatches().get(0);
        assertFalse(m.reference().isError());
        assertTrue(m.candidate().isError());
        assertTrue(m.toString().contains("IllegalStateException: 고장"), m.toString());
    }

    @Test
    void 비교_방법을_바꿀_수_있다() {
        var axis = Axis.of("x", "a", "b");
        var result = EquivalenceCheck
                .over(Combinations.of(axis))
                .input(c -> c.get(axis))
                .reference(s -> List.of(s, "z"))
                .candidate(s -> List.of("z", s))
                .comparingWith((l, r) -> l.size() == r.size() && l.containsAll(r))
                .run();

        assertTrue(result.isEquivalent());
    }

    @Test
    void null_결과끼리는_같고_한쪽만_null이면_다르다() {
        assertTrue(check(in -> null, in -> null).run().isEquivalent());
        assertEquals(100, check(in -> null, in -> 0).run().mismatchCount());
    }
}
