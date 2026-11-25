package io.github.chch0702.equivalence;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 여러 축의 모든 조합(데카르트 곱). 조합은 필요할 때 하나씩 만들어지므로
 * 조합 수가 커도 메모리에 한꺼번에 올리지 않는다.
 *
 * <p>순서는 마지막 축이 가장 빨리 바뀌는 순서(자동차 주행거리계 방식)다.
 */
public final class Combinations implements Iterable<Combination> {

    private final List<Axis<?>> axes;
    private final long size;

    private Combinations(List<Axis<?>> axes) {
        if (axes.isEmpty()) {
            throw new IllegalArgumentException("축이 하나 이상 필요합니다");
        }
        long total = 1;
        for (Axis<?> axis : axes) {
            if (axes.stream().filter(a -> a.name().equals(axis.name())).count() > 1) {
                throw new IllegalArgumentException("축 이름이 겹칩니다: " + axis.name());
            }
            total = Math.multiplyExact(total, axis.size());
        }
        this.axes = axes;
        this.size = total;
    }

    public static Combinations of(Axis<?>... axes) {
        return new Combinations(List.of(axes));
    }

    public static Combinations of(List<Axis<?>> axes) {
        return new Combinations(List.copyOf(axes));
    }

    /** 전체 조합 수. 축 중 하나라도 비어 있으면 0. */
    public long size() {
        return size;
    }

    public List<Axis<?>> axes() {
        return axes;
    }

    @Override
    public Iterator<Combination> iterator() {
        return new Odometer();
    }

    public Stream<Combination> stream() {
        Spliterator<Combination> spliterator = Spliterators.spliterator(
                iterator(), size,
                Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.SIZED);
        return StreamSupport.stream(spliterator, false);
    }

    private final class Odometer implements Iterator<Combination> {
        private final int[] indexes = new int[axes.size()];
        private long next = 0;

        @Override
        public boolean hasNext() {
            return next < size;
        }

        @Override
        public Combination next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Combination current = new Combination(axes, indexes, next);
            next++;
            advance();
            return current;
        }

        private void advance() {
            for (int i = indexes.length - 1; i >= 0; i--) {
                indexes[i]++;
                if (indexes[i] < axes.get(i).size()) {
                    return;
                }
                indexes[i] = 0;
            }
        }
    }

    @Override
    public String toString() {
        return "Combinations" + axes + " size=" + size;
    }
}
