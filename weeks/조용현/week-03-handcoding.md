# Week 03 · 손코딩

> 작성일: 2026-09-08
> 용도: IDE 없이 10분 안에 구현하고, 불변식과 복잡도를 설명하기 위한 답안

---

## 워밍업 — 배열 기반 Stack과 연결 리스트 기반 Stack

### 한 장 요약

| 구현 | `push` | `pop` / `peek` | 공간 특성 | 장점 | 단점 |
|---|:---:|:---:|---|---|---|
| 동적 배열 | amortized O(1) | O(1) | capacity만큼 연속 공간 | 캐시 지역성이 좋고 객체 오버헤드가 작음 | 확장 순간 O(n), 남는 capacity 존재 |
| 연결 리스트 | O(1) | O(1) | 원소마다 노드·참조 필요 | 용량 확장과 복사가 없음 | 노드 할당 비용과 낮은 지역성 |

스택의 핵심 불변식은 **가장 나중에 넣은 원소를 가장 먼저 꺼내는 LIFO**입니다. 빈 스택에서 `pop`이나 `peek`을 호출할 때의 정책도 반드시 정해야 합니다. 아래 구현은 `NoSuchElementException`을 던집니다.

### 1. 동적 배열 기반 Stack

```java
import java.util.Arrays;
import java.util.NoSuchElementException;

final class ArrayStack<E> {
    private static final int DEFAULT_CAPACITY = 8;

    private Object[] elements = new Object[DEFAULT_CAPACITY];
    private int size;

    public void push(E value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    @SuppressWarnings("unchecked")
    public E pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }

        int top = --size;
        E value = (E) elements[top];
        elements[top] = null; // 더 이상 쓰지 않는 객체 참조 제거
        return value;
    }

    @SuppressWarnings("unchecked")
    public E peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        return (E) elements[size - 1];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void ensureCapacity(int required) {
        if (required <= elements.length) {
            return;
        }
        int next = elements.length * 2;
        elements = Arrays.copyOf(elements, next);
    }
}
```

**불변식** — 유효한 원소는 항상 `elements[0..size-1]`에 있고, top은 `size-1`입니다.

**왜 `push`가 O(1)인가** — 매번 O(1)은 아닙니다. 배열이 차면 기존 원소를 복사하므로 해당 연산은 O(n)입니다. 하지만 용량을 두 배로 늘리면 여러 push에 복사 비용이 분산되어 **amortized O(1)**입니다.

**손코딩 함정**

- `elements[size++] = value`에서 대입 후 크기를 증가시킵니다.
- `pop`은 `--size`로 top 인덱스를 만든 뒤 값을 꺼냅니다.
- 꺼낸 칸을 `null`로 지우지 않으면 스택이 필요 없는 객체를 계속 참조하는 memory loitering이 생깁니다.
- Java에서는 `new E[]`를 만들 수 없어 `Object[]`를 사용하고 꺼낼 때 캐스팅합니다.

### 2. 연결 리스트 기반 Stack

```java
import java.util.NoSuchElementException;

final class LinkedStack<E> {
    private Node<E> top;
    private int size;

    public void push(E value) {
        top = new Node<>(value, top);
        size++;
    }

    public E pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }

        E value = top.value;
        top = top.next;
        size--;
        return value;
    }

    public E peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        return top.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return top == null;
    }

    private static final class Node<E> {
        private final E value;
        private final Node<E> next;

        private Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
    }
}
```

**불변식** — `top`은 가장 최근에 삽입한 노드를 가리키며, `top == null`과 `size == 0`은 항상 함께 성립합니다.

**트레이드오프 답변** — 연결 리스트는 매 push가 최악에도 O(1)이고 배열 복사가 없지만, 원소마다 노드 객체와 참조가 필요하고 메모리가 흩어져 캐시 지역성이 낮습니다. 동적 배열은 확장 순간 O(n)이지만 보통 메모리 효율과 순차 접근 성능이 좋아 범용 구현의 기본 선택입니다.

---

## 응용 — 배열에서 원하는 수가 위치한 모든 인덱스 찾기

문제에는 배열이 정렬됐다는 조건이 없습니다. 따라서 일반적인 답은 **처음부터 끝까지 한 번 순회하면서 일치하는 인덱스를 모으는 것**입니다. 원하는 수가 여러 번 나올 수 있으므로 첫 발견에서 종료하면 안 됩니다.

### 풀이 1 — 한 번 조회할 때

```java
import java.util.ArrayList;
import java.util.List;

static List<Integer> findAllIndices(int[] values, int target) {
    List<Integer> indices = new ArrayList<>();

    for (int i = 0; i < values.length; i++) {
        if (values[i] == target) {
            indices.add(i);
        }
    }
    return indices;
}
```

**시간복잡도** — Θ(n). 마지막 원소까지 확인해야 원하는 수가 더 없는지 알 수 있습니다. 100만 개라는 숫자가 이 하한을 바꾸지는 않습니다.

**공간복잡도** — 반환 결과를 포함하면 O(k), 여기서 k는 일치하는 인덱스 수입니다. 결과 공간을 제외한 추가 공간은 O(1)입니다.

### 풀이 2 — 결과가 매우 많을 때 기본형 배열로 저장

`List<Integer>`는 각 인덱스를 `Integer`로 boxing하므로 결과가 많으면 객체·메모리 비용이 커질 수 있습니다. 외부 라이브러리를 쓰지 않고 기본형을 유지하려면 동적 `int[]`를 사용할 수 있습니다.

```java
import java.util.Arrays;

static int[] findAllIndicesPrimitive(int[] values, int target) {
    int[] result = new int[16];
    int size = 0;

    for (int i = 0; i < values.length; i++) {
        if (values[i] != target) {
            continue;
        }

        if (size == result.length) {
            result = Arrays.copyOf(result, result.length * 2);
        }
        result[size++] = i;
    }

    return Arrays.copyOf(result, size);
}
```

시간은 여전히 Θ(n), 결과 공간은 O(k)이며 배열 확장 비용은 amortized 분석으로 전체 O(k)입니다.

### 조건이 달라지면

- **정렬된 배열** — lower bound와 upper bound를 각각 이진 탐색해 일치 범위 `[first, last)`를 찾을 수 있습니다. 경계 탐색은 O(log n), 인덱스를 실제 목록으로 반환하는 데 O(k)가 들어 총 O(log n + k)입니다.
- **같은 배열에 조회가 반복됨** — 한 번 O(n)에 `값 → 인덱스 목록` 맵을 만들면 이후 조회는 결과 반환 비용을 제외하고 기대 O(1)입니다. 대신 O(n) 추가 공간이 필요합니다.
- **메모리에 결과를 모두 담기 어려움** — 발견 즉시 consumer에 전달하거나 스트림으로 출력합니다. 출력 자체가 k개이므로 Ω(k) 시간은 피할 수 없습니다.

### 면접에서 먼저 확인할 조건

1. 배열이 정렬되어 있는가?
2. 조회는 한 번인가, 여러 번인가?
3. 모든 인덱스를 반환해야 하는가, 출력해도 되는가?
4. 찾지 못했을 때 빈 결과를 반환할 것인가?

### 함정 정리

| 흔한 답 | 문제점 | 보완 |
|---|---|---|
| 찾는 즉시 인덱스 반환 | 중복된 위치를 놓침 | 끝까지 순회하며 모두 수집 |
| 무조건 이진 탐색 | 정렬 조건이 없음 | 미정렬이면 선형 탐색 |
| HashMap이면 무조건 빠름 | 단발 조회에는 전처리 비용·메모리만 증가 | 반복 조회일 때 선택 |
| 공간 O(1) | 반환 목록 자체가 최대 n개 | 결과 포함 O(k), 제외 O(1)로 구분 |
