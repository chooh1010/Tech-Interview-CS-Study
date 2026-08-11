# Week 01 · 손코딩 — 단일 연결 리스트 · 스택 2개로 큐 · 큐 2개로 스택

> 진행일: 2026-08-11
> 범위: `week-01.md` Part 3 — 워밍업 [자료구조 1/6] LinkedList + 응용 문항 15번
> 조건: IDE 없이, 자동완성 없이, 10분
> 리뷰: Codex(gpt-5.6-sol)

---

## 목차

- [워밍업 — 단일 연결 리스트](#워밍업--단일-연결-리스트)
- [응용 ① 스택 2개로 큐](#응용--스택-2개로-큐)
- [응용 ② 큐 2개로 스택](#응용--큐-2개로-스택)
- [Codex 리뷰 총평](#codex-리뷰-총평)
- [복잡도 종합표](#복잡도-종합표)

---

## 워밍업 — 단일 연결 리스트

### 내 시도 (미완)

```java
public class MyLinkedList<E> {
    private Node<E> head;
    private int size;

    private static class Node<E> {
        E item;
        Node<E> next;
    }

    public void add(int index, E e) {
        Node<E> node = new Node<>();
    }
}
```

노드 생성 한 줄에서 막힘. **"LinkedList는 모르겠다"** → 강의로 전환.

### 왜 배열과 다른가

배열은 메모리가 **한 덩어리로 붙어** 있어서 `arr[3]`을 "시작 주소 + 3칸"으로 **계산**해서 바로 간다. 이것이 O(1) 무작위 접근.

연결 리스트는 노드가 **메모리 여기저기 흩어져** 있고 **각 노드가 다음 노드의 주소만** 들고 있다.

```
head
 │
 ▼
┌───┬───┐   ┌───┬───┐   ┌───┬───┐
│ A │ ●─┼──▶│ B │ ●─┼──▶│ C │ / │
└───┴───┘   └───┴───┘   └───┴───┘
 item next
```

**계산으로 갈 수 없으니 반드시 `head`부터 걸어가야 한다.** 여기서 연결 리스트의 모든 성질이 나온다.

- `get(2)` → 2번 걸어감 → **O(n)**
- 중간 삽입 → **걸어가는 것이 O(n)**, 도착 후 연결 변경은 O(1)

### 1단계 — `get(index)`

```java
public E get(int index) {
    if (index < 0 || index >= size) {
        throw new IndexOutOfBoundsException("index: " + index);
    }
    Node<E> cur = head;
    for (int i = 0; i < index; i++) {
        cur = cur.next;
    }
    return cur.item;
}
```

`for`가 `index`번 도는 이유를 손으로 따라갈 것.

```
index = 0 → 루프 0회 → cur = head             (0번 노드) ✓
index = 2 → 루프 2회 → cur = head.next.next   (2번 노드) ✓
```

### 2단계 — 헬퍼로 뽑기

이 걷기는 `add`, `remove`에서도 똑같이 필요하다.

```java
private Node<E> nodeAt(int index) {
    Node<E> cur = head;
    for (int i = 0; i < index; i++) {
        cur = cur.next;
    }
    return cur;
}
```

### 3단계 — `add(index, e)`

**노드 생성자를 먼저 만든다.** 이것이 아래 "순서 함정"을 구조적으로 막아준다.

```java
private static class Node<E> {
    E item;
    Node<E> next;

    Node(E item, Node<E> next) {
        this.item = item;
        this.next = next;
    }
}
```

**경우 A — `index == 0` (맨 앞)**

직전 노드가 없으므로 `head` 자체를 바꿔야 한다.

```
넣기 전:  head ──▶ [A] ──▶ [B]

① 새 노드의 next를 기존 head로:  [X] ──▶ [A]
② head를 새 노드로:              head ──▶ [X] ──▶ [A] ──▶ [B]
```

```java
head = new Node<>(e, head);
```

한 줄로 끝난다. 우변의 `head`가 먼저 평가되므로 기존 `head`를 `next`로 문 새 노드가 만들어진다. 빈 리스트(`head == null`)일 때도 `next`가 `null`이 되어 자동으로 맞다.

**경우 B — 그 외 (중간/끝)**

**`index-1`번 노드(직전 노드)**를 잡는다.

```
index = 2 위치에 X 삽입

          prev            (index-1 = 1번)
           │
head ──▶ [A] ──▶ [B] ──▶ [C]
                  │
                  └── 여기가 원래 2번

① X.next = prev.next   →  [X] ──▶ [B]
② prev.next = X        →  [A] ──▶ [X] ──▶ [B] ──▶ [C]
```

```java
Node<E> prev = nodeAt(index - 1);
prev.next = new Node<>(e, prev.next);
```

> **순서가 절대적이다.** `prev.next`를 먼저 `X`로 바꿔버리면 `[B]` 이후로 가는 유일한 통로가 사라져 **리스트 절반이 증발한다.** 생성자를 쓰면 `prev.next`가 인자로 먼저 평가되므로 이 실수가 구조적으로 막힌다 — **생성자를 만드는 진짜 이유.**

### 4단계 — `remove(index)`

삽입의 거울. 역시 **직전 노드**를 잡는다.

```
index = 1 제거
          prev
           │
head ──▶ [A] ──▶ [B] ──▶ [C]
                  ↑
                target

prev.next = target.next   →   head ──▶ [A] ────────▶ [C]
```

### 완성 코드

```java
public class MyLinkedList<E> {
    private Node<E> head;
    private int size;

    private static class Node<E> {
        E item;
        Node<E> next;
        Node(E item, Node<E> next) {
            this.item = item;
            this.next = next;
        }
    }

    public void add(int index, E e) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
        if (index == 0) {
            head = new Node<>(e, head);
        } else {
            Node<E> prev = nodeAt(index - 1);
            prev.next = new Node<>(e, prev.next);
        }
        size++;
    }

    public E remove(int index) {
        checkElementIndex(index);
        Node<E> target;
        if (index == 0) {
            target = head;
            head = head.next;          // 원소가 하나뿐이면 head = null
        } else {
            Node<E> prev = nodeAt(index - 1);
            target = prev.next;
            prev.next = target.next;   // 건너뛰기
        }
        target.next = null;            // GC 도움 (링크 끊기)
        size--;
        return target.item;
    }

    public E get(int index) {
        checkElementIndex(index);
        return nodeAt(index).item;
    }

    public int size() { return size; }

    private Node<E> nodeAt(int index) {
        Node<E> cur = head;
        for (int i = 0; i < index; i++) cur = cur.next;
        return cur;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
    }
}
```

### 반드시 챙길 경계 조건

| 항목 | 내용 |
|---|---|
| **`index == 0`** | 직전 노드가 없어 `head` 자체를 교체. **이 분기를 안 만들면 반드시 NPE** |
| **유효 범위 차이** | `add`는 **`0 ~ size`**, `get`/`remove`는 **`0 ~ size-1`** |
| **원소 1개 삭제** | `head = head.next`가 자동으로 `head = null` 처리 |
| **`size` 갱신** | 가장 흔한 누락 |
| **범위 밖** | `IndexOutOfBoundsException` |
| **포인터 순서** | `X.next = prev.next` **먼저**, `prev.next = X` **나중** |

**`add`만 `index == size`가 유효한 이유** — `add(size, e)`는 "맨 끝에 **덧붙이기**"로 의미가 있지만, `get(size)`/`remove(size)`는 **존재하지 않는 원소**를 읽거나 지우는 것이라 무효다.

### 복잡도

`add` / `remove` / `get` 모두 `Θ(i+1)`, 최악 **Θ(n)**. 공간은 전체 Θ(n), 연산당 보조 공간 Θ(1).

**`add(0, e)`와 `remove(0)`만 Θ(1)** — 이것이 `LinkedList`를 **스택이나 큐로 쓸 때만** 쓸모 있는 이유다.

### 이중 연결 리스트로 확장하면

각 노드에 `prev` 포인터가 추가되고 보통 `tail` 필드도 함께 둔다.

| | 단일 | 이중 |
|---|:--:|:--:|
| 맨 앞 삽입/삭제 | Θ(1) | Θ(1) |
| **맨 뒤 삽입/삭제** | **Θ(n)** | **Θ(1)** ← `tail` |
| **노드 참조를 아는 상태에서 삭제** | **Θ(n)** ← 직전 노드를 못 찾음 | **Θ(1)** |
| 역방향 순회 | 불가 | 가능 |
| `get(i)` | Θ(n) | Θ(n) *(양끝에서 시작해 상수 2배 개선)* |
| 노드당 메모리 | 참조 1개 | **참조 2개** |

**핵심은 2행과 3행이다.** 단일 연결 리스트는 **뒤로 갈 수 없어** 직전 노드를 항상 앞에서부터 다시 찾아야 한다. `prev` 포인터가 그 문제를 없앤다.

그래서 `java.util.LinkedList`는 이중 연결이고 `Deque`를 구현할 수 있다 — 양끝 연산이 전부 O(1)이라야 덱이므로.

대가는 **메모리(노드당 참조 하나 더)와 연결 관리 복잡도**다. 삽입/삭제마다 고칠 포인터가 2배가 되고 버그날 곳도 늘어난다.

---

## 응용 ① 스택 2개로 큐

### 내 코드

```java
import java.util.*;

public class QueueWithTwoStacks<E> {
    Stack<E> in = new Stack<>();
    Stack<E> out = new Stack<>();

    public void offer(E e) {
        in.push(e);
    }

    public E poll() {
        if (out.size() == 0) {
            while (in.size() > 0) {
                out.push(in.pop());
            }
        }
        return out.pop();
    }
}
```

**로직 정확.** 이 문제의 핵심을 짚었다.

### `if (out.isEmpty())` 가드 — 성능이 아니라 **정확성** 문제

처음에 이 가드를 "amortized O(1)이 성립하는 지점"이라고 설명했는데, 그건 **부차적인 이유**였다. 진짜 이유는 더 근본적이다.

**`out`이 비어있지 않은데 옮기면 FIFO 순서 자체가 깨진다.**

```
A, B, C 삽입 → in = [A,B,C] (C가 top)
poll() → out으로 전부 이동 → out = [C,B,A] (A가 top) → A 반환   ✓

이제 D 삽입 → in = [D], out = [C,B] (B가 top)

가드 없이 옮기면 → out = [C,B,D] (D가 top)
다음 poll() → D 반환   ✗   B가 나와야 함
```

늦게 들어온 D가 아직 안 나간 B 위에 얹혀 **먼저 나가버린다.** 큐가 아니게 된다.

즉 이 가드는 **없으면 느려지는** 게 아니라 **없으면 틀린다.** 성능은 그 위에 따라오는 보너스다. 면접에서 이 순서로 설명하면 훨씬 강하다.

### amortized O(1)인 이유

**원소가 `out` → `in`으로 절대 되돌아가지 않는다**는 불변식 덕분이다. 원소 하나의 일생 동안 스택 연산이 **최대 4회로 고정**된다.

```
in.push → in.pop → out.push → out.pop
```

그래서 비싼 연산(out으로 통째 이동)이 **미래를 위한 저축**이 된다. n번 연산에 총 O(n).

### 지적 사항

| 구분 | 내용 |
|---|---|
| **버그** | 두 스택이 모두 비면 `out.pop()`이 `EmptyStackException`을 던진다 |
| **계약** | 이름이 `poll()`이면 빈 큐에서 **`null`을 반환**해야 한다 |
| **계약** | `Queue.offer()`는 **`boolean`을 반환**해야 하는데 `void`다 |
| **관용구** | `java.util.Stack`은 **레거시** — `Vector` 상속이라 모든 메서드가 `synchronized`. **`ArrayDeque` 권장** (단 `null` 저장 금지) |
| **관용구** | `size() == 0` → `isEmpty()`, 필드는 `private final` |

**Java 컬렉션 명명 규약** (외울 것)

| 이름 | 비었을 때 |
|---|---|
| `poll()`, `peek()` | **`null` 반환** |
| `remove()`, `element()` | **예외 던짐** |

---

## 응용 ② 큐 2개로 스택

### 내 코드

```java
import java.util.*;

public class StackWithTwoQueues<E> {
    Queue<E> q1 = new LinkedList<>();
    Queue<E> q2 = new LinkedList<>();

    public void push(E e) {
        q1.offer(e);
    }

    public E pop() {
        while (q1.size() > 1) {
            q2.offer(q1.poll());
        }
        E e = q1.poll();
        Queue<E> tmp = q1;
        q1 = q2;
        q2 = tmp;
        return e;
    }
}
```

**로직 정확. 스왑도 맞다.**

`q1`에 하나만 남을 때까지 넘기고, 그 하나가 LIFO의 top이다. **마지막 스왑을 빼먹으면 다음 `pop()`이 빈 `q1`을 보게 되므로 필수** — 여기서 많이 틀린다.

### 빠진 것 — 변형 명시

이것은 **costly-pop 변형**이다 (`push` Θ(1) / `pop` Θ(n)). `push`를 비싸게 하는 반대 변형도 있다.

면접에서는 **"저는 pop을 비싸게 하는 쪽을 택했고, push를 비싸게 하면 반대가 됩니다"**까지 말해야 한다.

### 왜 amortized로도 O(1)이 안 되나

**매 pop마다 기존 k개를 회전시키는데, 그 작업이 다음 연산에 아무것도 남기지 않는다.** 저축이 없으니 상환할 것도 없다.

크기 n에서 pop을 연속으로 다 하면 이동 총량이 `(n-1)+(n-2)+…+1 = Θ(n²)` → pop당 amortized도 **Θ(n)**.

**정밀한 표현** (면접에서 차이가 크다):

- ✗ "n번 연산하면 항상 Θ(n²)"
- ○ "**총비용이 Θ(n²)이 되는 연산 순서가 존재한다.** 따라서 amortized Θ(n)"

또한 "큐 2개로는 O(1)이 **불가능함이 증명됐다**"고 단정하면 안 된다. 표준 두 변형(costly-push / costly-pop)에서 알려져 있지 않다는 것이지, 모든 구현에 대한 불가능성 증명이 아니다.

### 지적 사항

| 구분 | 내용 |
|---|---|
| **버그** | 빈 스택에서 `pop()`이 **조용히 `null`을 반환**한다 (`LinkedList.poll()`의 동작) |
| **버그** | 그 결과 `push(null)`한 경우와 **빈 상태를 구별할 수 없다** |
| **계약** | 이름이 `pop()`이면 `Deque.pop()` 규약대로 **`NoSuchElementException`을 던져야** 한다 |
| **누락** | `peek()`, `isEmpty()`, `size()`가 없어 사용자가 **빈 상태를 미리 확인할 방법 자체가 없다** |
| **관용구** | `LinkedList`도 유효한 `Queue`지만 일반 FIFO에는 **`ArrayDeque`가 더 빠르다** (노드 할당 없음, 캐시 지역성) |

> **①과 ②가 정확히 반대로 어긋나 있다** — ①은 `poll`인데 예외를 던지고, ②는 `pop`인데 `null`을 반환한다. 우연이지만 두 구현의 빈 상태 처리 정책이 서로 뒤집혀 있다.

---

## Codex 리뷰 총평

Codex(gpt-5.6-sol)가 확인해준 것과 추가로 잡은 것.

**확인된 것**
- ①의 `out` 비었을 때만 리필하는 조건은 정확 — 회계 논법으로 amortized Θ(1) 증명 가능
- ②의 스왑은 정확하고 필요함. costly-pop 변형이 맞음
- ②의 pop은 **amortized 개선이 없음** (`Θ(n)`)

**추가로 잡은 것**
- ①의 `offer()`는 `boolean`을 반환해야 함
- ①의 `offer` 최악은 O(1)이 아니라 **Θ(n)** (`Stack` 내부 배열 확장 순간). amortized O(1)이 정확한 표현
- ②는 `push(null)`과 빈 상태를 구별할 수 없음
- ②에 `peek`/`isEmpty`/`size`가 없음
- 워밍업의 `Node(item, next)` 생성자 부재로 연결 로직이 장황해짐
- `ArrayDeque`의 캐비어트: **`null` 비허용, 스레드 세이프 아님**

---

## 복잡도 종합표

### 단일 연결 리스트 (tail 없음)

| 연산 | 위치별 | 최악 | 공간(보조) |
|---|:--:|:--:|:--:|
| `add(i, e)` | Θ(i+1) | Θ(n) | Θ(1) |
| `remove(i)` | Θ(i+1) | Θ(n) | Θ(1) |
| `get(i)` | Θ(i+1) | Θ(n) | Θ(1) |

전체 공간 Θ(n).

### 스택 2개 → 큐

| 연산 | 최악 | amortized |
|---|:--:|:--:|
| `offer` | Θ(n) *(배열 확장)* | **Θ(1)** |
| `poll` | Θ(n) | **Θ(1)** |

전체 공간 Θ(n).

### 큐 2개 → 스택 (costly-pop 변형)

| 연산 | 최악 | amortized |
|---|:--:|:--:|
| `push` | Θ(1) | Θ(1) |
| `pop` | Θ(n) | **Θ(n)** ← 개선 없음 |

전체 공간 Θ(n).

**이 두 표의 대비가 세션 1-A 모의면접 ④번의 결론을 코드로 확인해준다.**

---

## 남은 확인 질문 (미답변)

코드를 보지 말고 답할 것.

1. `add`에서 `prev.next = X`를 먼저 하고 `X.next = prev.next`를 나중에 하면 리스트가 어떤 상태가 되는가? 구체적으로 그려볼 것
2. `add`의 유효 범위는 `0 ~ size`인데 `remove`는 `0 ~ size-1`이다. `add`에서 `index == size`가 유효한 이유를 한 문장으로
3. `tail` 필드를 추가하면 **단일** 연결 리스트에서도 맨 뒤 **삽입**이 O(1)이 된다. 그런데 맨 뒤 **삭제**는 여전히 O(n)이다. 왜인가?

---

## 참조

- 원본 문항: [`../week-01.md`](../week-01.md) Part 3
- 개념 세션: [`week-01-session-A.md`](week-01-session-A.md) (스택 2개 큐 / 큐 2개 스택의 amortized 분석)
- [`week-01-session-B.md`](week-01-session-B.md) · [`week-01-session-C.md`](week-01-session-C.md)
