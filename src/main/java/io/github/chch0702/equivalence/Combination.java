package io.github.chch0702.equivalence;

import java.util.List;
import java.util.StringJoiner;

/**
 * 전 조합 중 하나. 축마다 값 하나씩을 가진다.
 * {@link #get(Axis)} 로 값을 꺼낸다.
 */
public final class Combination {

    private final List<Axis<?>> axes;
    private final int[] indexes;
    private final long ordinal;

    Combination(List<Axis<?>> axes, int[] indexes, long ordinal) {
        this.axes = axes;
        this.indexes = indexes.clone();
        this.ordinal = ordinal;
    }

    /** 이 조합에서 해당 축의 값. 조합에 없는 축이면 예외. */
    public <T> T get(Axis<T> axis) {
        for (int i = 0; i < axes.size(); i++) {
            if (axes.get(i) == axis) {
                return axis.valueAt(indexes[i]);
            }
        }
        throw new IllegalArgumentException("이 조합에 없는 축입니다: " + axis.name());
    }

    /** 전체 조합 중 몇 번째인지 (0부터). */
    public long ordinal() {
        return ordinal;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < axes.size(); i++) {
            Axis<?> axis = axes.get(i);
            joiner.add(axis.name() + "=" + axis.valueAt(indexes[i]));
        }
        return joiner.toString();
    }
}
