package io.github.chch0702.equivalence.example;

/**
 * 가상의 "문서 결재" 예제에 쓰는 타입들. 실제 업무 규칙과 관계없다.
 */
public final class ApprovalModel {

    private ApprovalModel() {
    }

    /** 문서 상태. */
    public enum DocStatus {
        DRAFT, SUBMITTED, IN_REVIEW, RETURNED, APPROVED, REJECTED
    }

    /** 화면을 보는 사람의 역할. */
    public enum Role {
        AUTHOR, REVIEWER, APPROVER, ADMIN
    }

    /** 화면에 보이는 버튼. */
    public enum Button {
        EDIT, SUBMIT, WITHDRAW, APPROVE, RETURN, REJECT
    }

    /** 결재 단계는 0부터. 이 단계부터 최종 결재자가 처리한다. */
    public static final int FINAL_STEP = 2;

    /** 버튼 판단에 필요한 입력. */
    public record ApprovalInput(DocStatus status, int step, Role role, boolean delegated) {
    }
}
