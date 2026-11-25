package io.github.chch0702.equivalence;

import java.time.Duration;
import java.util.List;

/**
 * 전수 비교 결과.
 *
 * @param checked         비교한 조합 수
 * @param mismatchCount   불일치 전체 건수
 * @param firstMismatches 앞에서부터 모은 불일치 (최대 {@code maxReported} 건)
 * @param elapsed         소요 시간
 */
public record EquivalenceResult<I, O>(
        long checked, long mismatchCount, List<Mismatch<I, O>> firstMismatches, Duration elapsed) {

    public EquivalenceResult {
        firstMismatches = List.copyOf(firstMismatches);
    }

    public boolean isEquivalent() {
        return mismatchCount == 0;
    }

    /** 불일치가 있으면 앞 N건을 담은 {@link AssertionError} 를 던진다. JUnit 등 어떤 테스트 도구에서도 실패로 잡힌다. */
    public void assertEquivalent() {
        if (!isEquivalent()) {
            throw new AssertionError(report());
        }
    }

    /** 사람이 읽는 요약. */
    public String report() {
        String nl = System.lineSeparator();
        if (isEquivalent()) {
            return "불일치 0건 / 전체 " + checked + "조합 (" + elapsed.toMillis() + "ms)";
        }
        StringBuilder sb = new StringBuilder()
                .append("불일치 ").append(mismatchCount).append("건 / 전체 ").append(checked).append("조합");
        if (firstMismatches.size() < mismatchCount) {
            sb.append(" (앞 ").append(firstMismatches.size()).append("건만 표시)");
        }
        for (Mismatch<I, O> m : firstMismatches) {
            sb.append(nl).append("  ").append(m);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return report();
    }
}
