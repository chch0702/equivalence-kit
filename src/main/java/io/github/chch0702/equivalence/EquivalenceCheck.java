package io.github.chch0702.equivalence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * 모든 조합에서 참조 구현과 새 구현의 결과를 비교한다.
 *
 * <pre>{@code
 * EquivalenceCheck
 *     .over(Combinations.of(status, step, role))
 *     .input(c -> new Input(c.get(status), c.get(step), c.get(role)))
 *     .reference(Legacy::decide)
 *     .candidate(NewPolicy::decide)
 *     .maxReported(20)
 *     .run()
 *     .assertEquivalent();
 * }</pre>
 *
 * <p>불일치가 나와도 멈추지 않고 끝까지 돌아 전체 건수를 센다. 상세 내용은 앞 {@code maxReported} 건만 모은다.
 */
public final class EquivalenceCheck<I, O> {

    /** 기본으로 모아 보여주는 불일치 건수. */
    public static final int DEFAULT_MAX_REPORTED = 20;

    private final Combinations combinations;
    private final Function<Combination, ? extends I> inputMapper;
    private final Function<? super I, ? extends O> reference;
    private final Function<? super I, ? extends O> candidate;
    private BiPredicate<? super O, ? super O> equivalence = Objects::equals;
    private int maxReported = DEFAULT_MAX_REPORTED;

    private EquivalenceCheck(Combinations combinations, Function<Combination, ? extends I> inputMapper,
                             Function<? super I, ? extends O> reference, Function<? super I, ? extends O> candidate) {
        this.combinations = combinations;
        this.inputMapper = inputMapper;
        this.reference = reference;
        this.candidate = candidate;
    }

    /** 비교할 조합을 정하고 시작한다. */
    public static InputStep over(Combinations combinations) {
        return new InputStep(Objects.requireNonNull(combinations, "combinations"));
    }

    /** 결과 비교 방법을 바꾼다 (기본: {@link Objects#equals}). 예: 순서를 무시하고 비교. */
    public EquivalenceCheck<I, O> comparingWith(BiPredicate<? super O, ? super O> equivalence) {
        this.equivalence = Objects.requireNonNull(equivalence, "equivalence");
        return this;
    }

    /** 상세 내용을 모을 불일치 건수 (0 이면 건수만 센다). */
    public EquivalenceCheck<I, O> maxReported(int maxReported) {
        if (maxReported < 0) {
            throw new IllegalArgumentException("maxReported 는 0 이상이어야 합니다: " + maxReported);
        }
        this.maxReported = maxReported;
        return this;
    }

    /** 모든 조합을 비교한다. */
    public EquivalenceResult<I, O> run() {
        long started = System.nanoTime();
        long checked = 0;
        long mismatchCount = 0;
        List<Mismatch<I, O>> mismatches = new ArrayList<>();

        for (Combination combination : combinations) {
            I input = inputMapper.apply(combination);
            Outcome<O> expected = Outcome.capture(reference, input);
            Outcome<O> actual = Outcome.capture(candidate, input);
            checked++;
            if (!expected.sameAs(actual, equivalence)) {
                mismatchCount++;
                if (mismatches.size() < maxReported) {
                    mismatches.add(new Mismatch<>(combination, input, expected, actual));
                }
            }
        }
        return new EquivalenceResult<>(checked, mismatchCount, mismatches,
                Duration.ofNanos(System.nanoTime() - started));
    }

    /** 1단계: 조합 → 입력 변환. */
    public static final class InputStep {
        private final Combinations combinations;

        private InputStep(Combinations combinations) {
            this.combinations = combinations;
        }

        public <I> ReferenceStep<I> input(Function<Combination, ? extends I> inputMapper) {
            return new ReferenceStep<>(combinations, Objects.requireNonNull(inputMapper, "inputMapper"));
        }
    }

    /** 2단계: 참조 구현 (정답지). */
    public static final class ReferenceStep<I> {
        private final Combinations combinations;
        private final Function<Combination, ? extends I> inputMapper;

        private ReferenceStep(Combinations combinations, Function<Combination, ? extends I> inputMapper) {
            this.combinations = combinations;
            this.inputMapper = inputMapper;
        }

        public <O> CandidateStep<I, O> reference(Function<? super I, ? extends O> reference) {
            return new CandidateStep<>(combinations, inputMapper, Objects.requireNonNull(reference, "reference"));
        }
    }

    /** 3단계: 새 구현. */
    public static final class CandidateStep<I, O> {
        private final Combinations combinations;
        private final Function<Combination, ? extends I> inputMapper;
        private final Function<? super I, ? extends O> reference;

        private CandidateStep(Combinations combinations, Function<Combination, ? extends I> inputMapper,
                              Function<? super I, ? extends O> reference) {
            this.combinations = combinations;
            this.inputMapper = inputMapper;
            this.reference = reference;
        }

        public EquivalenceCheck<I, O> candidate(Function<? super I, ? extends O> candidate) {
            return new EquivalenceCheck<>(combinations, inputMapper, reference,
                    Objects.requireNonNull(candidate, "candidate"));
        }
    }
}
