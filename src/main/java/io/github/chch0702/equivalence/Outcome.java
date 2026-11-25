package io.github.chch0702.equivalence;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * 구현 하나를 실행한 결과: 반환값 또는 던진 예외.
 * 예외는 종류(클래스)가 같으면 같은 결과로 본다.
 *
 * @param <O> 반환값 타입
 */
public final class Outcome<O> {

    private final O value;
    private final Throwable error;

    private Outcome(O value, Throwable error) {
        this.value = value;
        this.error = error;
    }

    static <I, O> Outcome<O> capture(Function<? super I, ? extends O> impl, I input) {
        try {
            return new Outcome<>(impl.apply(input), null);
        } catch (RuntimeException | Error e) {
            if (e instanceof VirtualMachineError vmError) {
                throw vmError;
            }
            return new Outcome<>(null, e);
        }
    }

    public boolean isError() {
        return error != null;
    }

    /** 반환값. 예외였다면 null. */
    public O value() {
        return value;
    }

    /** 던진 예외. 정상 반환이었다면 null. */
    public Throwable error() {
        return error;
    }

    boolean sameAs(Outcome<O> other, BiPredicate<? super O, ? super O> equivalence) {
        if (isError() || other.isError()) {
            return isError() && other.isError() && error.getClass() == other.error.getClass();
        }
        if (value == null || other.value == null) {
            return value == other.value;
        }
        return equivalence.test(value, other.value);
    }

    @Override
    public String toString() {
        if (isError()) {
            String message = error.getMessage();
            return "예외 " + error.getClass().getName() + (message == null ? "" : ": " + message);
        }
        return Objects.toString(value);
    }
}
