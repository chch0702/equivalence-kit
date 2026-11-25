package io.github.chch0702.equivalence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 입력 하나가 가질 수 있는 값의 목록. 이름은 불일치 메시지에 쓰인다.
 *
 * <pre>{@code
 * var status = Axis.ofEnum("status", DocStatus.class);
 * var step   = Axis.range("step", 0, 3);
 * var urgent = Axis.bool("urgent");
 * }</pre>
 *
 * @param <T> 값 타입
 */
public final class Axis<T> {

    private final String name;
    private final List<T> values;

    private Axis(String name, List<T> values) {
        this.name = requireName(name);
        this.values = List.copyOf(values);
    }

    /** 값을 직접 나열한다. 값은 null 일 수 없다 (null 을 시험하려면 Optional 등으로 감싼다). */
    @SafeVarargs
    @SuppressWarnings("varargs") // 배열은 생성자에서 바로 복사하므로 밖으로 새지 않는다
    public static <T> Axis<T> of(String name, T... values) {
        Objects.requireNonNull(values, "values");
        return new Axis<>(name, Arrays.asList(values));
    }

    /** 목록에서 값을 가져온다. */
    public static <T> Axis<T> of(String name, List<T> values) {
        Objects.requireNonNull(values, "values");
        return new Axis<>(name, values);
    }

    /** 열거형의 모든 상수. */
    public static <E extends Enum<E>> Axis<E> ofEnum(String name, Class<E> type) {
        Objects.requireNonNull(type, "type");
        return new Axis<>(name, Arrays.asList(type.getEnumConstants()));
    }

    /** {@code from} 부터 {@code toInclusive} 까지의 정수. */
    public static Axis<Integer> range(String name, int from, int toInclusive) {
        if (toInclusive < from) {
            throw new IllegalArgumentException("범위가 비었습니다: " + from + ".." + toInclusive);
        }
        List<Integer> list = new ArrayList<>(toInclusive - from + 1);
        for (int i = from; i <= toInclusive; i++) {
            list.add(i);
        }
        return new Axis<>(name, list);
    }

    /** {@code true}, {@code false}. */
    public static Axis<Boolean> bool(String name) {
        return new Axis<>(name, List.of(true, false));
    }

    public String name() {
        return name;
    }

    /** 변경할 수 없는 값 목록. */
    public List<T> values() {
        return values;
    }

    public int size() {
        return values.size();
    }

    T valueAt(int index) {
        return values.get(index);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("축 이름이 필요합니다");
        }
        return name;
    }

    @Override
    public String toString() {
        return name + values;
    }
}
