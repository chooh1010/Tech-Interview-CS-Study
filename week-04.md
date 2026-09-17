# Week 04 — OS II — 동시성과 동기화

> **임계 구역, 데드락, 스레드 모델, IPC**
> 진행일: `____-__-__` (화)  ·  질문 9개  ·  손코딩 워밍업 1 + 응용 1문항

**이번 주 목표** — 경쟁 상태가 생기는 지점을 코드 수준에서 짚고, 동기화 기법의 비용과 데드락 조건을 설명할 수 있다.

> 아래 답변은 원본 질문 구조를 유지하면서 면접 대비용으로 보완한 답변입니다.

## 범위 한눈에 보기

| # | 질문 | 난이도 | 구분 |
|:--:|---|:--:|:--:|
| 1 | 뮤텍스와 세마포어의 차이점은 무엇인가요? | ★★☆ | 필수 |
| 2 | Deadlock 에 대해 설명해 주세요. | ★★☆ | 필수 |
| 3 | IPC가 무엇이고, 어떤 종류가 있는지 설명해 주세요. | ★★☆ | 심화 |
| 4 | Thread Safe 하다는 것은 어떤 의미인가요? | ★★☆ | 필수 |
| 5 | Thread Pool, Monitor, Fork-Join에 대해 설명해 주세요. | ★★★ | 심화 |
| 6 | 동기화를 구현하기 위한 하드웨어적인 해결 방법에 대해 설명해 주세요. | ★★★ | 심화 |
| 7 | 동기와 비동기, 블로킹과 논블로킹의 차이에 대해 설명해 주세요. | ★★★ | 필수 |
| 8 | 가상화가 무엇이고, 이것이 가상머신과 어떠한 차이가 있는지 설명해 주세요. | ★★☆ | 필수 |
| 9 | CI/CD 를 사용해 본 경험이 있나요? 있다면 간단하게 설명해 주세요. | ★★☆ | 필수 |

> 시간이 부족하면 `심화` 표시 질문을 다음 주로 미루고, `필수`부터 소화하세요.

## 모의 면접 진행 체크리스트

### 스터디 전 (각자)
- [ ] 이번 주 범위 질문을 전부 읽고, **내 답변** 칸을 채웠다
- [ ] 남에게 설명하듯 소리 내어 1회 말해봤다
- [ ] 답이 막힌 질문을 **막힌 부분** 칸에 적어뒀다
- [ ] 워밍업 구현과 응용 문항을 직접 작성해봤다

---

## Part 1. 운영체제

#### 1. 뮤텍스와 세마포어의 차이점은 무엇인가요?

`★★☆` · `필수` · 원본 [운영체제 #8](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 3개 (펼치기)</b></summary>

- 이진 세마포어와 뮤텍스의 차이에 대해 설명해 주세요.

  **답변:** 둘 다 한 번에 하나만 진입시키는 용도로 사용할 수 있지만, 뮤텍스는 **소유권**이 있다는 점이 핵심입니다. 일반적으로 Lock을 획득한 스레드가 직접 해제해야 합니다. 반면 이진 세마포어는 소유권 개념이 없어 한 스레드가 `wait()`하고 다른 스레드가 `signal()`하는 식의 이벤트 알림에도 사용할 수 있습니다.

- Lock을 얻기 위해 대기하는 프로세스들은 Spin Lock 기법을 사용할 수 있습니다. 이 방법의 장단점은 무엇인가요? 단점을 해결할 방법은 없을까요?

  **답변:** Spin Lock은 Lock이 풀릴 때까지 CPU를 놓지 않고 반복해서 확인하는 방식입니다. 문맥 교환이 없기 때문에 Lock 대기 시간이 매우 짧다면 빠를 수 있습니다. 반대로 Lock이 오래 유지되면 CPU 시간을 낭비합니다. 이를 줄이기 위해 일정 시간만 Spin한 뒤 sleep하는 Adaptive Spin, Hybrid Lock 같은 방식을 사용할 수 있습니다.

- 뮤텍스와 세마포어 모두 커널이 관리하기 때문에, Lock을 얻고 방출하는 과정에서 시스템 콜을 호출해야 합니다. 이 방법의 장단점이 있을까요? 단점을 해결할 수 있는 방법은 없을까요?

  **답변:** 커널이 대기 스레드를 재우고 깨울 수 있기 때문에 CPU 낭비를 줄일 수 있지만, 커널 모드 전환과 문맥 교환 비용이 발생할 수 있습니다. 현대 구현은 경쟁이 없을 때는 사용자 영역의 원자적 연산으로 처리하고, 실제 경합이 발생했을 때만 커널에 진입하는 방식으로 비용을 줄이기도 합니다.

</details>

**내 답변**

```text
뮤텍스와 세마포어는 모두 공유 자원에 대한 동시 접근을 제어하는 동기화 도구입니다.

뮤텍스는 한 번에 하나의 스레드만 임계 구역에 들어가도록 하는 Lock이고,
Lock을 획득한 스레드가 직접 해제해야 하는 소유권 개념이 있습니다.

세마포어는 내부의 정수 값을 이용해 동시에 접근할 수 있는 스레드 수를 제어합니다.
값이 1이면 이진 세마포어, 2 이상이면 여러 스레드의 동시 접근을 허용할 수 있습니다.

따라서 뮤텍스는 주로 상호 배제에,
세마포어는 자원의 개수 제한이나 스레드 간 신호 전달에 사용합니다.
```

**핵심 키워드** → `상호 배제`, `소유권`, `Mutex`, `Semaphore`, `wait(P)`, `signal(V)`

**막힌 부분 / 다시 볼 것** → `Spin Lock`, `user-space fast path`, `futex`

---

#### 2. Deadlock 에 대해 설명해 주세요.

`★★☆` · `필수` · 원본 [운영체제 #9](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 5개 (펼치기)</b></summary>

- Deadlock 이 동작하기 위한 4가지 조건에 대해 설명해 주세요.

  **답변:** 상호 배제, 점유 대기, 비선점, 순환 대기 네 가지입니다.

  1. **상호 배제:** 자원을 동시에 하나의 프로세스만 사용
  2. **점유 대기:** 이미 자원을 가진 상태에서 다른 자원을 기다림
  3. **비선점:** 다른 프로세스의 자원을 강제로 빼앗을 수 없음
  4. **순환 대기:** 프로세스들이 원형으로 서로의 자원을 기다림

- 그렇다면 3가지만 충족하면 왜 Deadlock 이 발생하지 않을까요?

  **답변:** 네 조건은 Deadlock의 필요조건이기 때문입니다. 하나라도 깨지면 영원한 순환 대기 구조를 만들 수 없습니다.

- 어떤 방식으로 예방할 수 있을까요?

  **답변:** 네 가지 필요조건 중 하나를 제거하면 됩니다. 실무에서는 모든 스레드가 Lock을 동일한 순서로 획득하도록 정해 순환 대기를 제거하는 방법이 자주 사용됩니다.

- 왜 현대 OS는 Deadlock을 처리하지 않을까요?

  **답변:** 모든 Deadlock을 일반적으로 탐지하고 복구하려면 지속적인 추적 비용이 크고, 어떤 프로세스를 종료하거나 어떤 자원을 회수할지 OS가 일괄적으로 결정하기 어렵습니다. 따라서 일반 목적 OS는 응용 프로그램이 Lock 순서, timeout 등의 방식으로 관리하도록 두는 경우가 많습니다.

- Wait Free와 Lock Free를 비교해 주세요.

  **답변:** Lock-Free는 시스템 전체 관점에서 항상 최소 하나의 스레드가 진행됨을 보장합니다. Wait-Free는 모든 스레드가 유한한 단계 안에 작업을 완료함을 보장합니다. 따라서 Wait-Free가 더 강한 진행 보장입니다.

</details>

**내 답변**

```text
Deadlock, 즉 교착 상태는 둘 이상의 프로세스나 스레드가
서로 상대방이 가진 자원을 기다리면서 영원히 진행하지 못하는 상태입니다.

Deadlock이 발생하려면
상호 배제, 점유 대기, 비선점, 순환 대기의 네 가지 조건이 모두 만족되어야 합니다.

따라서 네 조건 중 하나라도 제거하면 Deadlock을 예방할 수 있습니다.
```

**핵심 키워드** → `Mutual Exclusion`, `Hold and Wait`, `No Preemption`, `Circular Wait`

**막힌 부분 / 다시 볼 것** → `Deadlock prevention / avoidance / detection`, `Lock-Free`, `Wait-Free`

---

#### 3. IPC가 무엇이고, 어떤 종류가 있는지 설명해 주세요.

`★★☆` · `심화` · 원본 [운영체제 #11](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 2개 (펼치기)</b></summary>

- Shared Memory가 무엇이며, 사용할 때 유의해야 할 점에 대해 설명해 주세요.

  **답변:** 서로 다른 프로세스가 동일한 물리 메모리 영역을 자신의 주소 공간에 매핑해 데이터를 주고받는 IPC 방식입니다. 데이터 복사가 적어 빠르지만 여러 프로세스가 동시에 수정하면 Race Condition이 발생할 수 있어 Mutex, Semaphore 등의 동기화가 필요합니다.

- 메시지 큐는 단방향이라고 할 수 있나요?

  **답변:** 메시지 큐 자체를 무조건 단방향이라고 하기는 어렵습니다. 하나의 큐를 한 방향의 통신 채널로 사용할 수 있지만, 양방향 통신이 필요하면 두 개의 큐를 사용하거나 메시지에 송수신 정보를 포함시키는 구조를 만들 수 있습니다.

</details>

**내 답변**

```text
IPC는 Inter Process Communication의 약자로,
서로 독립된 주소 공간을 가진 프로세스들이 데이터를 주고받기 위한 통신 방법입니다.

대표적인 방식으로
Pipe, Named Pipe, Message Queue, Shared Memory, Socket, Signal 등이 있습니다.

Pipe나 Message Queue는 커널을 통해 데이터를 전달하고,
Shared Memory는 같은 메모리 영역을 공유하기 때문에 빠르지만
별도의 동기화가 필요합니다.
```

**핵심 키워드** → `Pipe`, `Named Pipe`, `Message Queue`, `Shared Memory`, `Socket`, `Signal`

**막힌 부분 / 다시 볼 것** → `Shared Memory 동기화`, `Pipe vs Named Pipe`

---

#### 4. Thread Safe 하다는 것은 어떤 의미인가요?

`★★☆` · `필수` · 원본 [운영체제 #12](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 4개 (펼치기)</b></summary>

- Thread Safe 를 보장하기 위해 어떤 방법을 사용할 수 있나요?

  **답변:** Mutex, `synchronized` 같은 Lock, Atomic 연산, 불변 객체, Thread Local, Concurrent Collection 등을 사용할 수 있습니다.

- Peterson's Algorithm 이 무엇이며, 한계점에 대해 설명해 주세요.

  **답변:** 두 프로세스가 `flag`와 `turn`이라는 공유 변수를 이용해 임계 구역 진입을 제어하는 소프트웨어 상호배제 알고리즘입니다. 두 프로세스에만 적용된다는 한계가 있고 Busy Waiting이 발생합니다. 또한 현대 CPU와 컴파일러의 메모리 재배치까지 고려해야 하므로 실제 동기화에서는 하드웨어 Atomic 명령을 기반으로 한 방법을 사용합니다.

- Race Condition 이 무엇인가요?

  **답변:** 여러 스레드가 공유 데이터에 동시에 접근하여 실행 순서에 따라 결과가 달라지는 상황입니다. 예를 들어 `count++`는 읽기, 증가, 쓰기의 여러 단계로 수행될 수 있어 동시에 실행하면 증가 값이 유실될 수 있습니다.

- Thread Safe를 구현하기 위해 반드시 락을 사용해야 할까요? 그렇지 않다면, 어떤 다른 방법이 있을까요?

  **답변:** 반드시 Lock을 사용할 필요는 없습니다. Atomic 연산, CAS 기반 Lock-Free 구조, 불변 객체, Thread Local, 공유 상태 자체를 없애는 방법 등으로도 Thread Safe를 만들 수 있습니다.

</details>

**내 답변**

```text
Thread Safe하다는 것은 여러 스레드가 동시에 같은 코드나 공유 데이터에 접근하더라도
데이터가 손상되거나 실행 순서에 따라 잘못된 결과가 발생하지 않고
항상 올바른 결과를 보장한다는 의미입니다.

이를 위해 Lock을 사용할 수도 있고,
Atomic 연산, 불변 객체, Thread Local 등 다른 방법을 사용할 수도 있습니다.
```

**핵심 키워드** → `Race Condition`, `Critical Section`, `Lock`, `Atomic`, `Immutable`, `ThreadLocal`

**막힌 부분 / 다시 볼 것** → `Peterson's Algorithm`, `CAS`

---

#### 5. Thread Pool, Monitor, Fork-Join에 대해 설명해 주세요.

`★★★` · `심화` · 원본 [운영체제 #13](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 2개 (펼치기)</b></summary>

- Thread Pool을 사용한다고 가정하면, 어떤 기준으로 스레드의 수를 결정할 것인가요?

  **답변:** CPU 연산 위주의 작업은 CPU 코어 수와 비슷한 수준에서 시작하는 것이 일반적이고, I/O 대기가 많은 작업은 대기 중인 스레드가 많기 때문에 코어 수보다 더 많은 스레드를 둘 수 있습니다. 실제 값은 작업 특성, 메모리 사용량, 응답시간을 측정해 조정해야 합니다.

- 어떤 데이터를 정렬 하려고 합니다. 어떤 방식의 전략을 사용하는 것이 가장 안전하면서도 좋은 성능을 낼 수 있을까요?

  **답변:** 데이터가 충분히 크고 작업을 독립적인 구간으로 나눌 수 있다면 Fork-Join 방식으로 분할 정복 정렬을 수행할 수 있습니다. 예를 들어 Merge Sort를 여러 구간으로 나누어 병렬 정렬한 뒤 결과를 합칠 수 있습니다. 단, 작은 데이터는 병렬화 오버헤드가 더 클 수 있으므로 일정 크기 이하에서는 순차 정렬로 처리하는 것이 좋습니다.

</details>

**내 답변**

```text
Thread Pool은 일정 수의 스레드를 미리 만들어 두고,
작업이 들어올 때마다 스레드를 새로 생성하지 않고 재사용하는 방식입니다.
스레드 생성/삭제 비용을 줄이고 동시에 실행되는 작업 수를 제한할 수 있습니다.

Monitor는 공유 데이터와 해당 데이터를 보호하기 위한 동기화 기능을 하나로 묶은 구조입니다.
한 번에 하나의 스레드만 Monitor 내부의 임계 영역을 실행하게 할 수 있으며,
Java의 synchronized, wait, notify가 대표적인 예입니다.

Fork-Join은 큰 작업을 작은 작업으로 계속 나누어 병렬로 수행하고,
각 결과를 다시 합치는 분할 정복 기반 병렬 처리 방식입니다.
Java에서는 ForkJoinPool이 대표적이며 Work Stealing을 사용합니다.
```

**핵심 키워드** → `Thread 재사용`, `Monitor`, `synchronized`, `Fork`, `Join`, `Work Stealing`

**막힌 부분 / 다시 볼 것** → `Thread Pool 크기`, `Work Stealing`

---

#### 6. 동기화를 구현하기 위한 하드웨어적인 해결 방법에 대해 설명해 주세요.

`★★★` · `심화` · 원본 [운영체제 #20](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 2개 (펼치기)</b></summary>

- volatile 키워드는 어떤 의미가 있나요?

  **답변:** Java의 `volatile`은 한 스레드의 변경 사항을 다른 스레드가 볼 수 있도록 가시성을 보장하고, 해당 변수 주변의 특정 명령어 재배치를 제한합니다. 하지만 `count++` 같은 복합 연산의 원자성을 보장하지는 않습니다.

- 싱글코어가 아니라 멀티코어라면, 어떻게 동기화가 이뤄질까요?

  **답변:** 멀티코어에서는 각 코어가 별도의 캐시를 사용하기 때문에 Atomic Instruction, Cache Coherence Protocol, Memory Barrier 등을 통해 메모리의 가시성과 연산 순서를 맞춥니다.

</details>

**내 답변**

```text
동기화를 안전하게 구현하려면 읽기와 쓰기가 중간에 끊기지 않는
원자적 연산이 필요합니다.

하드웨어에서는 Test-And-Set, Compare-And-Swap(CAS),
Swap, Fetch-And-Add 같은 Atomic Instruction을 제공합니다.

예를 들어 CAS는 현재 메모리 값이 예상한 값과 같을 때만
새로운 값으로 변경하는 비교와 변경 과정을 하나의 원자적 연산으로 수행합니다.

멀티코어 환경에서는 Atomic Instruction과 함께
Memory Barrier와 Cache Coherence를 통해 메모리 가시성과 순서를 보장합니다.
```

**핵심 키워드** → `Atomic Instruction`, `CAS`, `Test-And-Set`, `Memory Barrier`, `Cache Coherence`

**막힌 부분 / 다시 볼 것** → `volatile ≠ atomic`, `memory visibility`

---

#### 7. 동기와 비동기, 블로킹과 논블로킹의 차이에 대해 설명해 주세요.

`★★★` · `필수` · 원본 [운영체제 #23](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 3개 (펼치기)</b></summary>

- 그렇다면, 동기이면서 논블로킹이고, 비동기이면서 블로킹인 경우는 의미가 있다고 할 수 있나요?

  **답변:** 가능합니다. 동기/비동기는 완료를 확인하는 방식의 관점이고 Blocking/Non-Blocking은 호출한 함수가 제어권을 언제 반환하는지에 대한 관점이기 때문입니다. 동기+논블로킹은 호출이 즉시 반환된 뒤 호출자가 계속 완료 여부를 확인하는 Polling 형태가 될 수 있습니다. 비동기 작업을 시작했더라도 이후 Future의 `get()`처럼 결과를 기다리면 특정 지점에서는 Blocking이 발생할 수 있습니다.

- I/O 멀티플렉싱에 대해 설명해 주세요.

  **답변:** 하나의 스레드가 여러 I/O 채널을 감시하다가 실제로 읽기/쓰기가 가능한 채널만 처리하는 방식입니다. `select`, `poll`, `epoll` 등이 대표적입니다. 소켓마다 스레드를 하나씩 만들 필요가 없어 많은 연결을 효율적으로 처리할 수 있습니다.

- 논블로킹 I/O를 수행한다고 하면, 그 결과를 어떻게 수신할 수 있나요?

  **답변:** 반복해서 확인하는 Polling, `select/poll/epoll` 같은 이벤트 감시, Callback, Event Loop, Future/Promise 등의 방법을 사용할 수 있습니다.

</details>

**내 답변**

```text
동기와 비동기는 작업의 완료를 어떤 방식으로 확인하는지에 대한 개념이고,
Blocking과 Non-Blocking은 호출한 쪽이 제어권을 언제 돌려받는지에 대한 개념입니다.

Blocking은 호출한 작업이 끝날 때까지 호출 스레드가 기다리고,
Non-Blocking은 작업이 완료되지 않았더라도 즉시 제어권을 반환합니다.

동기 방식은 호출한 쪽이 작업의 완료를 직접 확인하는 구조이고,
비동기 방식은 작업을 요청한 뒤 다른 일을 수행하다가
완료 시 Callback이나 이벤트 등의 방식으로 결과를 전달받을 수 있습니다.

따라서 두 개념은 서로 다른 기준이며 조합될 수 있습니다.
```

**핵심 키워드** → `완료 통지`, `제어권`, `Blocking`, `Non-Blocking`, `Sync`, `Async`, `I/O Multiplexing`

**막힌 부분 / 다시 볼 것** → `Sync+Non-Blocking`, `Event Loop`, `epoll`

---

## Part 2. 개발상식 코너

> 주제와 별개로 매주 곁들이는 공통 질문입니다. 면접에서 워밍업으로 자주 나옵니다.

#### 8. 가상화가 무엇이고, 이것이 가상머신과 어떠한 차이가 있는지 설명해 주세요.

`★★☆` · `필수` · 원본 [개발상식·기타 #1](../05-ETC.md)

<details>
<summary><b>꼬리 질문 3개 (펼치기)</b></summary>

- 그렇다면 Docker는 둘 중 어디에 속하나요? 왜 사람들이 Docker를 많이 채택할까요?

  **답변:** Docker는 OS 수준 가상화인 컨테이너 기술을 사용합니다. VM처럼 Guest OS 전체를 실행하지 않고 Host OS의 커널을 공유하기 때문에 시작 속도가 빠르고 이미지 크기가 작으며 배포 환경을 일관되게 만들기 쉽습니다.

- 하나의 Host OS에서 돌아간다면 충분히 한 컨테이너가 다른 컨테이너에 간섭할 수 있는 위험이 있지 않을까요? 이를 어떻게 방어할 수 있을까요?

  **답변:** 가능성이 있기 때문에 Linux Namespace로 프로세스, 네트워크 등을 격리하고 cgroups로 CPU와 메모리 사용량을 제한합니다. 추가로 Capabilities, seccomp, SELinux/AppArmor 등을 사용해 권한을 제한할 수 있습니다.

- Docker 위에 Docker를 올릴 순 없을까요?

  **답변:** 가능합니다. Docker-in-Docker(DinD) 방식이 있습니다. 다만 권한과 보안, 캐시 문제 때문에 CI 환경에서는 Host Docker daemon을 공유하거나 별도의 이미지 빌드 도구를 사용하는 방식도 많이 사용합니다.

</details>

**내 답변**

```text
가상화는 CPU, 메모리, 저장장치 같은 물리적 자원을 추상화해서
여러 개의 독립적인 실행 환경을 만드는 기술 전체를 의미합니다.

가상머신은 가상화 기술을 이용한 구현 방식 중 하나로,
Hypervisor 위에 가상의 하드웨어 환경을 만들고
각 VM마다 별도의 Guest OS를 실행합니다.

즉 가상화가 더 큰 개념이고,
가상머신은 가상화를 구현하는 방법 중 하나입니다.
```

**핵심 키워드** → `Virtualization`, `VM`, `Hypervisor`, `Guest OS`, `Container`, `Namespace`, `cgroups`

**막힌 부분 / 다시 볼 것** → `VM vs Container`, `Docker isolation`

---

#### 9. CI/CD 를 사용해 본 경험이 있나요? 있다면 간단하게 설명해 주세요.

`★★☆` · `필수` · 원본 [개발상식·기타 #2](../05-ETC.md)

<details>
<summary><b>꼬리 질문 7개 (펼치기)</b></summary>

- 소스코드를 작성하고 나서, 실제 사용자에게 배포되기까지의 과정을 순서대로 설명해 주세요.

  **답변:** 일반적으로 `Code → Commit/Push → Build → Test → Artifact/Image 생성 → Deploy → Monitoring` 순으로 진행할 수 있습니다.

- 빌드와 배포는 어떻게 다른가요?

  **답변:** 빌드는 소스코드를 실행 가능한 결과물로 만드는 과정이고, 배포는 그 결과물을 실제 사용자가 접근할 수 있는 서버나 운영 환경에 반영하는 과정입니다.

- CI와 CD는 각각 무엇을 자동화하는 것인가요? CD의 Delivery와 Deployment는 어떤 차이가 있나요?

  **답변:** CI는 코드가 통합될 때 Build와 Test를 자동으로 수행해 변경 사항을 지속적으로 검증하는 과정입니다. Continuous Delivery는 운영 배포 직전까지 자동화하고 실제 배포에는 승인을 둘 수 있습니다. Continuous Deployment는 검증을 통과하면 운영 환경까지 자동으로 배포합니다.

- 파이프라인의 어느 단계에 테스트를 넣는 것이 좋을까요? 그 이유는 무엇인가요?

  **답변:** 가능한 한 앞 단계부터 넣는 것이 좋습니다. Unit Test처럼 빠른 테스트를 먼저 실행해 오류를 빠르게 발견하고, 그 뒤 Integration/E2E Test처럼 비용이 큰 테스트를 실행하면 실패를 조기에 확인할 수 있습니다.

- 무중단 배포는 어떻게 구현할 수 있을까요? (Rolling, Blue-Green, Canary)

  **답변:** Rolling은 서버를 하나씩 새 버전으로 교체하고, Blue-Green은 기존 버전과 새 버전 환경을 동시에 운영한 뒤 트래픽을 전환하며, Canary는 일부 사용자에게 새 버전을 먼저 배포한 뒤 점차 범위를 늘리는 방식입니다.

- 배포한 버전에 장애가 발생했을 때, 롤백은 어떤 방식으로 준비해 둘 수 있을까요?

  **답변:** 이전 버전의 Artifact나 Docker Image를 보관하고, 배포 버전에 태그를 부여해 즉시 이전 버전으로 전환할 수 있도록 준비합니다. DB Schema 변경도 이전 버전과의 호환성을 고려해야 합니다.

- 빌드 결과물을 이미지로 만들어 배포하는 방식은, 서버에서 직접 빌드하는 방식과 비교해 어떤 장점이 있을까요?

  **답변:** 빌드 환경과 운영 환경의 차이를 줄일 수 있고 동일한 이미지를 여러 서버에 그대로 배포할 수 있습니다. 또한 이미지 자체가 배포 가능한 불변 결과물이 되므로 재현성과 롤백이 쉬워집니다.

</details>

**내 답변**

```text
실제 경험이 있다면 본인이 사용한 도구와 프로젝트 기준으로 답변하는 것이 가장 좋습니다.

직접 CI/CD를 구축한 경험이 없다면 다음처럼 답할 수 있습니다.

"아직 CI/CD 파이프라인을 직접 구축해서 운영해 본 경험은 없습니다.
다만 CI는 코드가 Push될 때 Build와 Test 등을 자동으로 수행해
변경 사항을 지속적으로 통합하는 과정이고,
CD는 검증된 결과물을 배포 가능한 상태로 만들거나
운영 환경까지 자동 배포하는 과정으로 이해하고 있습니다.

앞으로는 GitHub Actions 같은 도구를 이용해
Push → Build → Test → Deploy가 이어지는 파이프라인을 직접 구성해 보고 싶습니다."
```

**핵심 키워드** → `CI`, `Continuous Delivery`, `Continuous Deployment`, `Build`, `Test`, `Artifact`, `Deploy`, `Rollback`

**막힌 부분 / 다시 볼 것** → `Rolling`, `Blue-Green`, `Canary`, `배포 이미지`

---

## Part 3. 손코딩

### 워밍업 — 자료구조 · 정렬 구현

> 원본 [손코딩 #1 (각종 정렬) · #2 (각종 자료구조)](../06-ALGORITHM.md)를 12주로 나눈 조각입니다. 매주 1개씩, **10분 안에** IDE 없이 손으로 작성합니다.

#### [정렬 2/6] 병합 정렬

**구현 범위** — 재귀 분할 + `merge` 병합. 임시 배열을 매번 할당하지 않는 방법과, 병합 정렬이 안정 정렬인 이유

```java
static int[] arr;
static int[] temp;

static void mergeSort(int left, int right) {
    if (left >= right) {
        return;
    }

    int mid = (left + right) / 2;

    mergeSort(left, mid);
    mergeSort(mid + 1, right);

    merge(left, mid, right);
}

static void merge(int left, int mid, int right) {
    int i = left;
    int j = mid + 1;
    int k = left;

    while (i <= mid && j <= right) {
        if (arr[i] <= arr[j]) {
            temp[k++] = arr[i++];
        } else {
            temp[k++] = arr[j++];
        }
    }

    while (i <= mid) {
        temp[k++] = arr[i++];
    }

    while (j <= right) {
        temp[k++] = arr[j++];
    }

    for (int x = left; x <= right; x++) {
        arr[x] = temp[x];
    }
}
```

호출 전 `temp = new int[arr.length];`로 임시 배열을 한 번만 만들면 `merge()`가 호출될 때마다 새 배열을 만들 필요가 없습니다.

값이 같을 때 `arr[i] <= arr[j]`로 왼쪽 원소를 먼저 넣기 때문에 동일한 값의 기존 순서가 유지되어 **안정 정렬**입니다.

**시간복잡도** → `O(N log N)`  
**공간복잡도** → `O(N)`

**막힌 부분 / 다시 볼 것** → `merge 과정`, `안정 정렬 조건`

---

### 응용 문항

> 원본: [손코딩 연습 문항](../06-ALGORITHM.md) · IDE 없이 손으로 먼저 써본 뒤, 스터디에서 서로 비교합니다.

#### 12. 정렬된 두 LinkedList를 합쳐 하나의 정렬된 LinkedList로 만드는 코드를 작성해 보세요.

```java
static class Node {
    int value;
    Node next;

    Node(int value) {
        this.value = value;
    }
}

static Node merge(Node a, Node b) {
    Node dummy = new Node(0);
    Node cur = dummy;

    while (a != null && b != null) {
        if (a.value <= b.value) {
            cur.next = a;
            a = a.next;
        } else {
            cur.next = b;
            b = b.next;
        }

        cur = cur.next;
    }

    if (a != null) {
        cur.next = a;
    } else {
        cur.next = b;
    }

    return dummy.next;
}
```

기존 Node의 `next` 연결만 바꾸므로 전체 노드를 새로 만들 필요가 없습니다.

**시간복잡도** → `O(N + M)`  
**공간복잡도** → `O(1)` 추가 공간

**다른 사람 풀이에서 배운 점** → `dummy node를 사용하면 첫 노드 예외처리를 줄일 수 있다.`

---

## 회고

**미해결 질문** (다음 주에 다시 다룰 것)

- [ ] 
- [ ] 

**이번 주에 가장 약했던 주제** →

**참고한 자료** (링크·책·문서)

- 

---

## 시험 직전 압축

```text
Mutex = 소유권이 있는 상호배제 Lock
Semaphore = Counter로 동시 접근 개수 제어

Deadlock 4조건
= 상호배제 + 점유대기 + 비선점 + 순환대기

IPC
= Pipe / Message Queue / Shared Memory / Socket / Signal

Thread Safe
= 여러 스레드가 동시에 실행해도 올바른 결과

CAS
= 예상 값과 같을 때만 원자적으로 값 변경

Blocking
= 작업이 끝날 때까지 제어권을 받지 못함

Non-Blocking
= 작업 완료 여부와 상관없이 즉시 제어권 반환

가상화
= 자원을 추상화하는 기술
VM
= 가상화의 구현 방식 중 하나

CI
= Build/Test를 통한 지속적 통합
CD
= 배포 가능 상태 또는 실제 배포까지 자동화
```

---

[← Week 03](week-03.md) · [로드맵](../README.md) · [Week 05 →](week-05.md)
