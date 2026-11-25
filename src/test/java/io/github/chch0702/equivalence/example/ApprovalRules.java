package io.github.chch0702.equivalence.example;

import io.github.chch0702.equivalence.example.ApprovalModel.ApprovalInput;
import io.github.chch0702.equivalence.example.ApprovalModel.Button;
import io.github.chch0702.equivalence.example.ApprovalModel.DocStatus;

import java.util.EnumSet;
import java.util.Set;

import static io.github.chch0702.equivalence.example.ApprovalModel.FINAL_STEP;

/**
 * 새 구현 예시. 같은 규칙을 역할별로 읽기 쉽게 다시 썼다.
 * {@link LegacyStyleRules} 와 모든 조합에서 같은 결과를 내야 한다.
 */
public final class ApprovalRules {

    private ApprovalRules() {
    }

    public static Set<Button> buttons(ApprovalInput in) {
        Set<Button> buttons = EnumSet.noneOf(Button.class);
        if (isClosed(in.status())) {
            return buttons;
        }
        boolean inProgress = in.status() == DocStatus.SUBMITTED || in.status() == DocStatus.IN_REVIEW;
        boolean finalStage = in.step() >= FINAL_STEP;

        switch (in.role()) {
            case AUTHOR -> {
                if (in.status() == DocStatus.DRAFT || in.status() == DocStatus.RETURNED) {
                    buttons.add(Button.EDIT);
                    buttons.add(Button.SUBMIT);
                } else if (in.status() == DocStatus.SUBMITTED) {
                    buttons.add(Button.WITHDRAW);
                }
            }
            case REVIEWER -> {
                boolean ownStage = inProgress && !finalStage;
                boolean actingForApprover = in.delegated() && in.status() == DocStatus.IN_REVIEW && finalStage;
                if (ownStage || actingForApprover) {
                    buttons.add(Button.APPROVE);
                    buttons.add(Button.RETURN);
                }
            }
            case APPROVER -> {
                if (inProgress && finalStage) {
                    buttons.add(Button.APPROVE);
                    buttons.add(Button.RETURN);
                    buttons.add(Button.REJECT);
                }
            }
            case ADMIN -> buttons.add(Button.REJECT);
        }
        return buttons;
    }

    private static boolean isClosed(DocStatus status) {
        return status == DocStatus.APPROVED || status == DocStatus.REJECTED;
    }
}
