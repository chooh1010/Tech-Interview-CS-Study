# Week 05 · 손코딩

> 작성일: 2026-10-06
> 범위: [Week 05](../week-05.md) — BST 구현, 2,000,000자리 숫자 여러 개 더하기
> 언어: Java 11 이상. 구현의 불변식과 시간·공간 복잡도를 함께 설명한다.
> 면접 답변: [메모리·파일 시스템·GC](week-05-answers.md)

---

## 워밍업 — 이진 탐색 트리 (BST)

**각 노드의 왼쪽 서브트리에는 더 작은 값, 오른쪽에는 더 큰 값이 있는 이진 트리입니다.** 이 구현은 집합처럼 중복 삽입을 무시합니다.

| 연산 | 시간 | 추가 공간 |
|---|---|---|
| `insert`, `search`, `delete` | O(h), 균형 잡히면 O(log N), 편향되면 O(N) | 반복형 search O(1), 재귀형 insert/delete O(h) |
| 중위 순회 | O(N) | 재귀 스택 O(h) + 반환 리스트 O(N) |
| 전체 노드 저장 | — | O(N) |

`h`는 트리 높이입니다. 별도 균형 유지 기능이 없으므로 O(log N)을 보장하지 않습니다. 정렬된 값을 순서대로 삽입하면 연결 리스트처럼 한쪽으로 늘어나 높이가 N에 비례합니다.

### 구현

```java
import java.util.ArrayList;
import java.util.List;

final class IntBst {
    private static final class Node {
        int key;
        Node left;
        Node right;

        Node(int key) {
            this.key = key;
        }
    }

    private Node root;

    public void insert(int key) {
        root = insert(root, key);
    }

    private Node insert(Node node, int key) {
        if (node == null) return new Node(key);
        if (key < node.key) node.left = insert(node.left, key);
        else if (key > node.key) node.right = insert(node.right, key);
        return node; // 같으면 중복을 추가하지 않는다.
    }

    public boolean search(int key) {
        Node node = root;
        while (node != null) {
            if (key == node.key) return true;
            node = key < node.key ? node.left : node.right;
        }
        return false;
    }

    public void delete(int key) {
        root = delete(root, key);
    }

    private Node delete(Node node, int key) {
        if (node == null) return null;

        if (key < node.key) {
            node.left = delete(node.left, key);
        } else if (key > node.key) {
            node.right = delete(node.right, key);
        } else {
            // 자식 0개면 null, 자식 1개면 남은 자식을 부모에게 돌려준다.
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;

            // 자식 2개: 오른쪽 서브트리의 최솟값(중위 후속자)을 사용한다.
            Node successor = node.right;
            while (successor.left != null) successor = successor.left;
            node.key = successor.key;
            node.right = delete(node.right, successor.key);
        }
        return node;
    }

    public List<Integer> inorder() {
        List<Integer> result = new ArrayList<>();
        inorder(root, result);
        return result;
    }

    private void inorder(Node node, List<Integer> result) {
        if (node == null) return;
        inorder(node.left, result);
        result.add(node.key);
        inorder(node.right, result);
    }
}
```

### 삭제를 설명하는 순서

1. **자식 0개** — 해당 링크를 `null`로 바꿉니다.
2. **자식 1개** — 부모가 남은 자식을 직접 가리키게 합니다.
3. **자식 2개** — 오른쪽 서브트리의 최솟값을 현재 위치로 옮기고, 원래 위치의 후속자 노드를 삭제합니다. 후속자는 왼쪽 자식이 없으므로 마지막 삭제는 0개·1개 케이스로 끝납니다.

후속자보다 왼쪽 서브트리의 모든 값이 작고, 후속자를 제거한 오른쪽 서브트리의 모든 값은 더 크므로 BST 불변식이 유지됩니다. 키와 값이 함께 있는 맵이라면 키뿐 아니라 값도 함께 옮겨야 합니다.

**주의할 부분** — `root = delete(root, key)`를 빠뜨리면 루트 삭제를 처리하지 못합니다. 재귀가 돌려준 서브트리 루트를 부모 링크에 다시 연결해야 합니다. 편향된 큰 트리에서는 재귀가 `StackOverflowError`를 일으킬 수 있어 반복형 구현이나 AVL·Red-Black Tree가 대안입니다.

### 손으로 추적할 예

`5, 3, 7, 2, 4, 6, 8`을 삽입하면 중위 순회는 `[2, 3, 4, 5, 6, 7, 8]`입니다.

- `delete(2)`: 잎 노드 삭제.
- 이어서 `delete(3)`: 남은 자식 4가 3의 자리를 대신함.
- 이어서 `delete(5)`: 루트의 두 자식 케이스. 후속자 6을 올린 뒤 원래 6을 삭제.
- 최종 중위 순회: `[4, 6, 7, 8]`.
- 없는 값 삭제와 빈 트리 삭제는 아무 변화가 없어야 합니다.

---

## 응용 11. 2,000,000자리 숫자 여러 개를 모두 더하려면?

### 면접에서 먼저 말할 답변

**숫자 전체를 기본 정수형으로 변환하지 않고, 문자열이나 작은 정수 배열로 저장한 뒤 자리별 덧셈과 올림을 구현하겠습니다.**

입력은 우선 부호 없는 10진 정수라고 가정하겠습니다. 두 숫자의 끝자리부터 더해 결과 자릿수와 올림을 구하고, 이를 반복하면 한 자리 연산에는 최대 `9 + 9 + 1 = 19`만 필요합니다. 입력이 몇백만 자리여도 기본 정수형의 범위를 넘지 않습니다.

여러 숫자는 누적 결과에 하나씩 더합니다. 입력 전체를 한꺼번에 보관할 필요 없이 현재 숫자와 누적 결과만 유지할 수 있습니다. 음수가 포함되면 부호와 절댓값을 분리해 크기 비교와 뺄셈까지 구현해야 합니다.

### 구현 — 임의 정밀도 라이브러리 없이 문자열 덧셈

```java
import java.util.Iterator;
import java.util.Objects;

final class HugeDecimalSum {
    // 입력: 하나 이상의 숫자로 이루어진 문자열. 선행 0 허용, 부호는 불허.
    // 출력: 선행 0이 없는 정규화된 결과. 0은 "0".
    public static String add(String a, String b) {
        validate(a);
        validate(b);

        int i = a.length() - 1;
        int j = b.length() - 1;
        int carry = 0;
        StringBuilder reversed = new StringBuilder();

        while (i >= 0 || j >= 0 || carry != 0) {
            int sum = carry;
            if (i >= 0) sum += a.charAt(i--) - '0';
            if (j >= 0) sum += b.charAt(j--) - '0';
            reversed.append((char) ('0' + sum % 10));
            carry = sum / 10;
        }

        // 역순 결과의 뒤쪽이 원래 결과의 선행 0에 해당한다.
        while (reversed.length() > 1
                && reversed.charAt(reversed.length() - 1) == '0') {
            reversed.setLength(reversed.length() - 1);
        }
        return reversed.reverse().toString();
    }

    public static String sumAll(Iterator<String> numbers) {
        Objects.requireNonNull(numbers, "numbers");
        String total = "0";
        while (numbers.hasNext()) {
            total = add(total, numbers.next());
        }
        return total; // 입력이 없으면 합의 항등원 0.
    }

    private static void validate(String value) {
        Objects.requireNonNull(value, "number");
        if (value.isEmpty()) throw new IllegalArgumentException("empty number");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("non-decimal digit");
            }
        }
    }
}
```

### 불변식과 복잡도

오른쪽에서 k자리를 처리했을 때 `reversed`에는 합의 하위 k자리가 역순으로 저장되고, `carry`에는 다음 자리에 반영할 올림이 남습니다. 두 숫자와 올림을 모두 소진하면 전체 합이 완성됩니다. 결과를 앞에 계속 붙이면 문자열 이동 때문에 O(D²)가 될 수 있으므로, **뒤에 추가하고 마지막에 한 번 뒤집습니다.**

두 입력 길이의 최댓값을 D라고 하면 검증·덧셈·뒤집기 모두 O(D)이고 추가 공간도 O(D)입니다. 숫자가 M개이고 각 숫자가 최대 D자리라면 누적 결과는 최대 `D + ceil(log10 M)`자리입니다(M ≥ 1). 순차 덧셈의 시간 상한은 **O(M(D + log M))**, 스트리밍 입력을 전제로 한 작업 공간은 **O(D + log M)**입니다. 입력을 이미 리스트에 전부 저장했다면 그 저장 공간은 별도입니다.

### 더 빠르게 구현하라는 꼬리 질문

한 자리 대신 **9자리씩 묶어 밑을 10⁹로 두는 배열**을 사용할 수 있습니다. 낮은 자리 묶음부터 저장하고, 두 묶음을 더할 때 `sum % 1_000_000_000`과 `sum / 1_000_000_000`으로 값과 올림을 구합니다. 중간 계산을 `long`으로 하면 여유가 있습니다. 최상위 묶음만 그대로 출력하고 나머지는 9자리가 되도록 앞을 0으로 채웁니다.

여러 숫자의 같은 자리 묶음을 한 번에 전부 더하면 M에 따라 합이 커져 오버플로할 수 있습니다. **누적 배열에 숫자를 하나씩 더하면서 매번 올림을 정리**하면 두 묶음과 올림만 계산하게 됩니다. 누적 배열을 재사용하면 위 문자열 구현의 반복 할당도 줄일 수 있습니다.

2,000,000자리 문자열 한 개는 메모리에 다룰 수 있는 크기지만, 실제 여유 메모리는 확인해야 합니다. 한 숫자조차 담기 어려우면 디스크에 임시 저장하고 뒤에서부터 블록을 읽거나, 자릿수를 뒤집은 중간 파일을 사용하는 외부 메모리 처리가 필요합니다. 일반 텍스트 입력은 큰 자리부터 오므로 낮은 자리부터 읽을 수 있다고 가정하지 않습니다.

### 검증 사례

| 입력 | 기대 결과 | 확인하는 조건 |
|---|---|---|
| `0 + 0` | `0` | 영 처리 |
| `000 + 0012` | `12` | 선행 0 |
| `999 + 1` | `1000` | 연속 올림과 길이 증가 |
| `12345 + 678` | `13023` | 서로 다른 길이 |
| `123, 456, 789`의 합 | `1368` | 여러 숫자 누적 |
| 빈 입력 목록 | `0` | 합의 항등원 |
| 9가 2,000,000개인 수 + 1 | 1 뒤에 0이 2,000,000개 | 실제 요구 크기와 전체 올림 |
| 빈 문자열, 음수, 숫자 아닌 문자 | 예외 | 입력 계약 |

> `BigInteger`는 제출 구현에 사용하지 않는다. 작은 무작위 입력의 정답을 대조하는 검증 도구로만 사용할 수 있다.
