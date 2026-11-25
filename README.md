# equivalence-kit

> 기존 구현과 새 구현이 **모든 입력 조합에서 같은 결과**를 내는지 확인하는 작은 Java 테스트 도구.
> A tiny Java library for exhaustive equivalence testing: enumerate every input combination and compare a reference implementation with a new one.

레거시 로직을 새로 옮길 때 가장 어려운 질문은 "옮긴 결과가 정말 같은가"입니다.
입력이 열거형·작은 정수 범위·예/아니오처럼 **셀 수 있는 값**이라면, 샘플 몇 개를 고르는 것보다 **전부 비교하는 편이 싸고 확실합니다.**

이 도구는 그 과정을 세 단계로 줄입니다.

1. **입력 축 정의** — `Axis` 로 입력마다 가능한 값을 적는다
2. **전 조합 생성** — `Combinations` 가 모든 축의 곱을 차례로 만든다 (메모리에 한꺼번에 올리지 않음)
3. **두 구현 비교** — `EquivalenceCheck` 가 조합마다 참조 구현과 새 구현을 실행하고, 다르면 **앞 N건을 모아서** 보여준다

## 사용 예

가상의 "문서 결재" 예제입니다. 문서 상태·결재 단계·역할·위임 여부에 따라 누를 수 있는 버튼이 정해집니다.

```java
var status    = Axis.ofEnum("status", DocStatus.class);
var step      = Axis.range("step", 0, 3);
var role      = Axis.ofEnum("role", Role.class);
var delegated = Axis.bool("delegated");

EquivalenceCheck
    .over(Combinations.of(status, step, role, delegated))
    .input(c -> new ApprovalInput(c.get(status), c.get(step), c.get(role), c.get(delegated)))
    .reference(LegacyStyleRules::buttons)   // 원본을 그대로 옮긴 참조 구현
    .candidate(ApprovalRules::buttons)      // 새로 정리한 구현
    .maxReported(20)
    .run()
    .assertEquivalent();
```

다르면 이런 메시지로 실패합니다.

```
불일치 12건 / 전체 192조합 (앞 3건만 표시)
  #17 status=SUBMITTED, step=2, role=APPROVER, delegated=false
      reference = [APPROVE, RETURN, REJECT]
      candidate = [APPROVE, RETURN]
  ...
```

- 예외도 결과로 비교합니다. 한쪽만 예외를 던지거나 예외 종류가 다르면 불일치입니다.
- 불일치가 나와도 멈추지 않고 끝까지 돌며 **전체 건수**를 셉니다.

예제 전체는 [`src/test/java/.../example`](src/test/java/io/github/chch0702/equivalence/example) 에 있습니다.

## 언제 쓰나

- 오래된 절차형 로직(저장 프로시저, 긴 if 문 등)을 새 구조로 옮길 때
- 리팩터링 전후의 동작이 같은지 확인할 때
- 입력이 유한한 열거값으로 이루어진 권한·상태 판단 로직

입력이 무한하거나(자유 문자열, 큰 숫자 범위) 조합 수가 지나치게 크다면, 경계값을 축으로 뽑거나 서로 독립인 축을 나눠 따로 비교하세요.

## 빌드

- Java 21, JUnit 5, Gradle
- `gradle test`

## 로드맵

- [x] 1주: 축·조합·비교 핵심 API, 문서 결재 예제, CI
- [ ] 2주: 불일치 리포트 (축별 패턴 요약)
- [ ] 3주: 큰 조합 — 축 분리 실행, 병렬 실행(옵션), 진행률·소요 시간
- [ ] 4주: 문서 정리, 0.1.0

## 라이선스

MIT
