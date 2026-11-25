package io.github.chch0702.equivalence.example;

import io.github.chch0702.equivalence.example.ApprovalModel.ApprovalInput;
import io.github.chch0702.equivalence.example.ApprovalModel.Button;
import io.github.chch0702.equivalence.example.ApprovalModel.DocStatus;
import io.github.chch0702.equivalence.example.ApprovalModel.Role;

import java.util.EnumSet;
import java.util.Set;

/**
 * 참조 구현 (정답지) 예시. 오래된 절차형 코드를 "해석하지 않고 줄 단위로" 옮겼다고 가정한 모양이다.
 * 플래그 변수와 중첩 IF 를 일부러 그대로 둔다. 다듬지 않는 것이 참조 구현의 규칙이다.
 */
public final class LegacyStyleRules {

    private LegacyStyleRules() {
    }

    public static Set<Button> buttons(ApprovalInput in) {
        String edit = "N";
        String submit = "N";
        String withdraw = "N";
        String approve = "N";
        String ret = "N";
        String reject = "N";

        if (in.status() == DocStatus.APPROVED || in.status() == DocStatus.REJECTED) {
            // 끝난 문서는 아무것도 못 누른다
        } else {
            if (in.role() == Role.ADMIN) {
                reject = "Y";
            }
            if (in.role() == Role.AUTHOR) {
                if (in.status() == DocStatus.DRAFT || in.status() == DocStatus.RETURNED) {
                    edit = "Y";
                    submit = "Y";
                } else if (in.status() == DocStatus.SUBMITTED) {
                    withdraw = "Y";
                }
            }
            if (in.role() == Role.REVIEWER || in.role() == Role.APPROVER) {
                if (in.status() == DocStatus.SUBMITTED || in.status() == DocStatus.IN_REVIEW) {
                    if (in.step() < 2) {
                        if (in.role() == Role.REVIEWER) {
                            approve = "Y";
                            ret = "Y";
                        }
                    } else {
                        if (in.role() == Role.APPROVER) {
                            approve = "Y";
                            ret = "Y";
                            reject = "Y";
                        } else if (in.delegated() && in.status() == DocStatus.IN_REVIEW) {
                            // 위임받은 검토자는 최종 단계에서도 승인·반려 가능 (거부는 불가)
                            approve = "Y";
                            ret = "Y";
                        }
                    }
                }
            }
        }

        Set<Button> result = EnumSet.noneOf(Button.class);
        if ("Y".equals(edit)) result.add(Button.EDIT);
        if ("Y".equals(submit)) result.add(Button.SUBMIT);
        if ("Y".equals(withdraw)) result.add(Button.WITHDRAW);
        if ("Y".equals(approve)) result.add(Button.APPROVE);
        if ("Y".equals(ret)) result.add(Button.RETURN);
        if ("Y".equals(reject)) result.add(Button.REJECT);
        return result;
    }
}
