package io.github.chch0702.equivalence;

/**
 * 참조 구현과 새 구현의 결과가 다른 조합 하나.
 *
 * @param combination 조합 (축 이름=값)
 * @param input       조합으로 만든 입력
 * @param reference   참조 구현 결과
 * @param candidate   새 구현 결과
 */
public record Mismatch<I, O>(Combination combination, I input, Outcome<O> reference, Outcome<O> candidate) {

    @Override
    public String toString() {
        return "#" + combination.ordinal() + " " + combination
                + System.lineSeparator() + "      reference = " + reference
                + System.lineSeparator() + "      candidate = " + candidate;
    }
}
