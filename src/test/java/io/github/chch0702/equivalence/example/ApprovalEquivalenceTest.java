package io.github.chch0702.equivalence.example;

import io.github.chch0702.equivalence.Axis;
import io.github.chch0702.equivalence.Combinations;
import io.github.chch0702.equivalence.EquivalenceCheck;
import io.github.chch0702.equivalence.EquivalenceResult;
import io.github.chch0702.equivalence.example.ApprovalModel.ApprovalInput;
import io.github.chch0702.equivalence.example.ApprovalModel.Button;
import io.github.chch0702.equivalence.example.ApprovalModel.DocStatus;
import io.github.chch0702.equivalence.example.ApprovalModel.Role;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalEquivalenceTest {

    // 단계 범위는 "원본에서 실제로 쓰는 값의 한계"를 근거로 잡는다. 여기서는 0~3.
    static final Axis<DocStatus> STATUS = Axis.ofEnum("status", DocStatus.class);
    static final Axis<Integer> STEP = Axis.range("step", 0, 3);
    static final Axis<Role> ROLE = Axis.ofEnum("role", Role.class);
    static final Axis<Boolean> DELEGATED = Axis.bool("delegated");

    static EquivalenceResult<ApprovalInput, Set<Button>> compareWith(Function<ApprovalInput, Set<Button>> candidate) {
        return EquivalenceCheck
                .over(Combinations.of(STATUS, STEP, ROLE, DELEGATED))
                .input(c -> new ApprovalInput(c.get(STATUS), c.get(STEP), c.get(ROLE), c.get(DELEGATED)))
                .reference(LegacyStyleRules::buttons)
                .candidate(candidate)
                .maxReported(5)
                .run();
    }

    @Test
    void 모든_조합에서_참조구현과_새구현의_결과가_같다() {
        var result = compareWith(ApprovalRules::buttons);

        assertEquals(6L * 4 * 4 * 2, result.checked());
        result.assertEquivalent();
    }

    @Test
    void 위임_조건을_빠뜨린_새구현은_불일치로_잡힌다() {
        // 흔한 실수: 위임받은 검토자의 최종 단계 처리를 빠뜨림
        Function<ApprovalInput, Set<Button>> buggy = in -> {
            if (in.role() == Role.REVIEWER && in.delegated()) {
                return in.step() >= ApprovalModel.FINAL_STEP
                        ? EnumSet.noneOf(Button.class)
                        : ApprovalRules.buttons(in);
            }
            return ApprovalRules.buttons(in);
        };

        var result = compareWith(buggy);

        assertFalse(result.isEquivalent());
        // IN_REVIEW × 단계 2·3 × REVIEWER × 위임 = 2건
        assertEquals(2, result.mismatchCount());
        var first = result.firstMismatches().get(0);
        assertEquals(DocStatus.IN_REVIEW, first.combination().get(STATUS));
        assertEquals(Role.REVIEWER, first.combination().get(ROLE));
        assertTrue(first.combination().get(DELEGATED));

        var error = assertThrows(AssertionError.class, result::assertEquivalent);
        assertTrue(error.getMessage().contains("불일치 2건 / 전체 192조합"), error.getMessage());
    }

    @Test
    void 경계값_하나만_틀려도_잡힌다() {
        // 흔한 실수: ">= FINAL_STEP" 을 "> FINAL_STEP" 으로 옮김
        Function<ApprovalInput, Set<Button>> offByOne = in -> {
            if (in.step() == ApprovalModel.FINAL_STEP) {
                return ApprovalRules.buttons(new ApprovalInput(in.status(), in.step() - 1, in.role(), in.delegated()));
            }
            return ApprovalRules.buttons(in);
        };

        var result = compareWith(offByOne);

        assertFalse(result.isEquivalent());
        assertTrue(result.firstMismatches().stream()
                .allMatch(m -> m.combination().get(STEP) == ApprovalModel.FINAL_STEP));
    }
}
