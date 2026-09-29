# Week 04 — OS II — 동시성과 동기화

> **임계 구역, 데드락, 스레드 모델, IPC**
> 진행일: `____-__-__` (화) · 질문 9개 · 손코딩 워밍업 1 + 응용 1문항

**이번 주 목표** — 경쟁 상태가 생기는 지점을 코드 수준에서 짚고, 동기화 기법의 비용과 데드락 조건을 설명할 수 있다.

---

# Part 1. 운영체제

## 1. 뮤텍스와 세마포어의 차이점은 무엇인가요?

`★★☆` · `필수`

### 내 답변 — 면접용

뮤텍스와 세마포어는 모두 여러 스레드나 프로세스가 공유 자원에 접근하는 것을 제어하기 위한 동기화 기법입니다.

가장 큰 차이는 **소유권과 허용 가능한 동시 접근 수**입니다.

뮤텍스는 Mutual Exclusion이라는 이름처럼 한 시점에 하나의 스레드만 임계 구역에 들어갈 수 있도록 하는 Lock입니다. 일반적으로 Lock을 획득한 스레드가 직접 해제해야 한다는 **소유권 개념**이 있습니다.

반면 세마포어는 내부적으로 permit의 개수를 관리합니다. Counting Semaphore라면 permit 개수만큼 여러 스레드가 동시에 접근할 수 있고, Binary Semaphore는 permit이 1개이므로 뮤텍스와 비슷하게 사용할 수 있습니다. 하지만 세마포어에는 일반적으로 뮤텍스와 같은 소유권 개념이 없기 때문에 acquire한 스레드가 아닌 다른 스레드가 release할 수도 있습니다.

그래서 보통 **공유 데이터 자체의 배타적 접근에는 Mutex**, **DB Connection Pool처럼 동시에 사용할 수 있는 자원의 개수를 제한할 때는 Semaphore**를 사용합니다.

Oracle의 Java `Semaphore` 역시 permit 개수를 관리하며, binary semaphore도 `Lock`과 달리 소유권 개념이 없다고 설명합니다. 반대로 `ReentrantLock`은 획득한 스레드가 lock의 owner가 됩니다.

### 비교

| 구분        | Mutex             | Semaphore              |
| --------- | ----------------- | ---------------------- |
| 핵심 목적     | 상호 배제             | 동시 접근 개수 제어            |
| 상태        | Locked / Unlocked | Permit 개수              |
| 동시에 접근 가능 | 일반적으로 1개          | N개 가능                  |
| 소유권       | 있음                | 일반적으로 없음               |
| 해제        | 보통 획득한 스레드        | 다른 스레드도 가능             |
| 대표 사례     | 공유 변수 수정          | Connection Pool, 작업 슬롯 |

### 꼬리 질문 — 이진 세마포어와 뮤텍스는 무엇이 다른가요?

둘 다 동시에 한 스레드만 통과시킬 수 있다는 점에서는 비슷합니다.

하지만 **뮤텍스는 owner가 존재**하는 반면, Binary Semaphore는 단순히 permit이 `0 또는 1`인 세마포어일 뿐입니다.

따라서 Mutex는

```text
Thread A: lock()
Thread B: unlock() → 일반적으로 잘못된 사용
```

이지만 Semaphore는 구조상

```text
Thread A: acquire()
Thread B: release()
```

같은 패턴도 가능합니다.

이 때문에 세마포어는 단순 Lock뿐 아니라 **스레드 간 신호 전달 signaling** 용도로도 사용할 수 있습니다.

---

### 꼬리 질문 — Spin Lock이란?

Spin Lock은 Lock을 획득하지 못한 스레드가 sleep하지 않고 계속 반복하면서 Lock이 풀렸는지 확인하는 방식입니다.

```java
while (!lock.tryLock()) {
    // 계속 검사
}
```

장점은 Lock이 매우 짧은 시간 안에 풀린다면 **스레드를 재우고 다시 스케줄링하는 Context Switch 비용을 피할 수 있다는 것**입니다.

반대로 Lock이 오래 유지되면 CPU를 계속 소비하므로 성능이 크게 떨어집니다.

따라서 일반적으로

```text
Critical Section이 매우 짧음
+
멀티코어
+
경쟁이 심하지 않음
```

일 때 효과적입니다.

실제 구현에서는 무조건 spin하거나 무조건 block하기보다,

```text
잠깐 Spin
        ↓
Lock 획득 실패
        ↓
Sleep / Kernel Wait
```

같은 **Adaptive Spinning**을 사용할 수 있습니다.

---

### 꼬리 질문 — Mutex와 Semaphore는 Lock마다 시스템 콜을 할까요?

반드시 그렇지는 않습니다.

Linux에서는 대표적으로 **futex(Fast Userspace Mutex)**를 사용합니다.

경쟁이 없다면 사용자 공간에서 atomic instruction을 이용해 Lock을 처리하고, 다른 스레드가 이미 Lock을 잡고 있어서 실제로 대기해야 할 때만 `futex()`를 통해 커널에 진입합니다.

```text
lock()
 │
 ├─ 아무도 사용 안 함
 │      ↓
 │  User Space Atomic CAS
 │      ↓
 │     성공
 │
 └─ 이미 사용 중
        ↓
   futex syscall
        ↓
     thread sleep
```

즉,

> **Fast Path는 User Space, Contended Slow Path에서 Kernel을 사용한다.**

Linux 문서도 futex는 경쟁이 없는 경우 사용자 공간에서 동작하고, 커널은 contention을 조정해야 하는 경우 개입한다고 설명합니다.

### 현업 연결

예를 들어 서버에서 DB Connection을 최대 20개까지만 사용할 수 있다면

```java
Semaphore dbConnections = new Semaphore(20);
```

처럼 제한할 수 있습니다.

반면 여러 요청이 동일한 결제 상태 객체를 수정해야 한다면 공유 상태에 대한 일관성을 위해 Mutex/Lock이 더 적합합니다.

**핵심 키워드** → Mutual Exclusion, Ownership, Permit, Counting Semaphore, Binary Semaphore, Spin Lock, Futex, Context Switch

**막힌 부분 / 다시 볼 것** → Binary Semaphore와 Mutex의 ownership 차이, Futex의 User Space Fast Path

---

# 2. Deadlock에 대해 설명해 주세요.

`★★☆` · `필수`

### 내 답변 — 면접용

Deadlock은 둘 이상의 프로세스나 스레드가 서로 상대방이 가진 자원을 기다리면서 영원히 진행하지 못하는 상태입니다.

대표적으로 Thread A가 Lock 1을 가지고 Lock 2를 기다리고, Thread B가 Lock 2를 가지고 Lock 1을 기다리면 두 스레드 모두 진행할 수 없습니다.

Deadlock이 발생하려면 네 가지 필요 조건인

1. Mutual Exclusion
2. Hold and Wait
3. No Preemption
4. Circular Wait

이 모두 성립해야 합니다.

따라서 Deadlock을 예방하려면 네 조건 중 하나를 깨면 됩니다. 실무에서는 여러 Lock을 획득해야 할 때 **Lock 획득 순서를 항상 동일하게 유지하는 방식**이 대표적인 방법이고, `tryLock()`과 timeout을 이용해 무한 대기를 피하기도 합니다.

### 예시

```text
Thread A                  Thread B

lock(A)                   lock(B)
   ↓                         ↓
lock(B) 기다림            lock(A) 기다림
   ↓                         ↓
   └──── 서로 기다림 ────────┘

             DEADLOCK
```

---

## Deadlock의 4가지 조건

### 1. Mutual Exclusion — 상호 배제

자원 하나를 동시에 한 프로세스만 사용할 수 있습니다.

### 2. Hold and Wait — 점유 대기

이미 자원을 하나 가지고 있으면서 다른 자원을 기다립니다.

### 3. No Preemption — 비선점

다른 프로세스가 가지고 있는 자원을 강제로 뺏을 수 없습니다.

### 4. Circular Wait — 순환 대기

```text
P1 → P2 → P3 → P1
```

과 같은 자원 대기 순환 구조가 만들어집니다.

---

### 꼬리 질문 — 세 가지만 만족하면 왜 Deadlock이 발생하지 않나요?

이 네 조건은 Deadlock의 **필요 조건**입니다.

따라서 하나라도 존재하지 않으면 Deadlock 구조를 만들 수 없습니다.

예를 들어 Circular Wait가 없다면

```text
T1 → T2 → T3
```

처럼 마지막 대기가 다시 T1으로 돌아오지 않으므로 누군가는 작업을 완료하고 자원을 반환할 수 있습니다.

다만 면접에서 정확하게 말하면

> 네 조건이 모두 만족된다는 사실만으로 현재 반드시 Deadlock이 발생했다고 단정하는 것보다는, Deadlock이 발생하기 위해 네 조건이 모두 필요하다고 표현하는 것이 정확합니다.

---

## Deadlock 처리 방법

### 1. Prevention — 예방

4조건 중 하나를 제거합니다.

예:

```text
Circular Wait 제거

항상
Lock A → Lock B → Lock C

순서로만 획득
```

### 2. Avoidance — 회피

자원을 할당하기 전에 시스템이 안전 상태인지 판단합니다.

대표적인 알고리즘이 **Banker's Algorithm**입니다.

### 3. Detection & Recovery — 탐지 후 복구

Deadlock을 허용한 뒤 Wait-for Graph 등을 이용하여 탐지합니다.

이후

* 프로세스 종료
* 트랜잭션 롤백
* 자원 회수

등으로 복구합니다.

---

### 현업 사례 — DB Deadlock

Deadlock은 DB에서 실제로 자주 접하게 됩니다.

예를 들어

```text
Transaction 1
UPDATE account A
UPDATE account B

Transaction 2
UPDATE account B
UPDATE account A
```

가 동시에 실행되면

```text
T1: A lock 보유 → B 기다림
T2: B lock 보유 → A 기다림
```

이 발생할 수 있습니다.

PostgreSQL은 Deadlock을 자동 탐지해 관련 트랜잭션 중 하나를 abort하며, 공식 문서에서도 여러 자원을 사용할 때 **항상 같은 순서로 Lock을 획득하는 것이 가장 좋은 방어책**이라고 설명합니다. MySQL InnoDB 역시 Deadlock을 감지한 뒤 한 트랜잭션을 rollback합니다.

따라서 서버 코드는

```text
Transaction 시작
    ↓
Deadlock 발생
    ↓
DB가 한 Transaction Rollback
    ↓
Application Retry
```

까지 고려해야 합니다.

---

### 꼬리 질문 — 왜 현대 OS는 Deadlock을 처리하지 않나요?

이 질문은 조금 조심해서 답해야 합니다.

**현대 시스템이 Deadlock을 전혀 처리하지 않는 것은 아닙니다.**

예를 들어 DBMS는 적극적으로 Deadlock을 탐지하기도 합니다.

하지만 운영체제가 시스템의 모든 자원에 대해 범용적인 Deadlock Prevention/Detection을 수행하는 것은 일반적이지 않습니다.

그 이유는

* 모든 자원의 미래 요청량을 OS가 알 수 없음
* 모든 Lock 관계를 추적하는 비용
* 탐지 알고리즘 자체의 오버헤드
* 애플리케이션 내부 Lock까지 OS가 의미를 알 수 없음

등이 있기 때문입니다.

Java 역시 언어 명세 차원에서 Deadlock을 예방하거나 반드시 탐지하도록 요구하지 않습니다.

---

## Wait-Free와 Lock-Free

### Lock-Free

시스템 전체 관점에서 **적어도 하나의 스레드는 계속 진행할 수 있음**을 보장합니다.

한 스레드는 계속 실패하거나 starvation될 수 있습니다.

```text
T1 → 성공
T2 → CAS 실패
T3 → CAS 실패

그래도 시스템 전체는 진행
```

### Wait-Free

모든 스레드가 **유한 번의 연산 안에 작업을 완료할 수 있음**을 보장합니다.

따라서 진행 보장은

```text
Wait-Free > Lock-Free
```

입니다.

Lock-Free 구조에서는 주로 CAS(Compare-And-Set)를 이용합니다.

---

**핵심 키워드** → Mutual Exclusion, Hold and Wait, No Preemption, Circular Wait, Lock Ordering, Detection, Recovery, Lock-Free, Wait-Free

**막힌 부분 / 다시 볼 것** → 4조건은 필요조건이라는 점, Lock-Free와 Wait-Free의 progress guarantee 차이

---

# 3. IPC가 무엇이고, 어떤 종류가 있는지 설명해 주세요.

`★★☆` · `심화`

### 내 답변 — 면접용

IPC는 Inter-Process Communication의 약자로, 독립된 주소 공간을 가진 프로세스들이 데이터를 주고받거나 서로 동기화하기 위한 방법입니다.

대표적인 방법에는

* Pipe / Named Pipe
* Message Queue
* Shared Memory
* Socket
* Signal

등이 있습니다.

Pipe와 Message Queue는 커널이 데이터를 전달해 주기 때문에 구현이 비교적 단순하지만 데이터 복사 비용이 있을 수 있습니다.

Shared Memory는 여러 프로세스가 같은 메모리 영역을 직접 접근하기 때문에 매우 빠르지만, 동시에 접근하면 Race Condition이 발생할 수 있으므로 Semaphore나 Mutex 같은 별도의 동기화가 필요합니다.

Socket은 같은 시스템뿐 아니라 네트워크의 다른 시스템과도 통신할 수 있기 때문에 범용성이 높습니다.

Linux의 POSIX Shared Memory도 여러 프로세스가 동일한 메모리 영역을 공유하는 방식이며, 공식 문서에서도 일반적으로 semaphore 등을 이용해 접근을 동기화해야 한다고 설명합니다.

---

## IPC 종류

### Pipe

```text
Process A ──── data ────▶ Process B
```

일반 Pipe는 기본적으로 단방향입니다.

Linux pipe는 read end와 write end를 가지는 unidirectional communication channel입니다.

### Named Pipe / FIFO

일반 Pipe와 달리 파일 시스템에 이름이 존재하므로 서로 부모-자식 관계가 없는 프로세스도 통신할 수 있습니다.

### Message Queue

```text
Producer
   ↓
[Message Queue]
   ↓
Consumer
```

메시지 단위로 데이터를 주고받습니다.

POSIX Message Queue는 `mq_send()`와 `mq_receive()`를 통해 프로세스가 메시지를 교환합니다.

### Shared Memory

```text
Process A ──┐
            │
       Shared Memory
            │
Process B ──┘
```

데이터를 별도의 IPC 버퍼로 계속 복사하지 않고 같은 메모리를 공유할 수 있기 때문에 매우 빠릅니다.

하지만

```java
sharedCounter++;
```

처럼 동시에 수정하면 Race Condition이 발생할 수 있습니다.

따라서

```text
Shared Memory
+
Semaphore / Mutex
```

조합이 자주 사용됩니다.

### Socket

```text
Process A
   ↓
Socket
   ↓
Process B
```

동일 시스템에서는 Unix Domain Socket을 사용할 수 있고, 네트워크 통신에서는 TCP/UDP Socket을 사용할 수 있습니다.

---

### 꼬리 질문 — Shared Memory 사용 시 주의할 점은?

가장 중요한 문제는 **동기화**입니다.

Shared Memory 자체는

> 같은 데이터를 볼 수 있게 해 줄 뿐 누가 언제 수정할 수 있는지를 제어하지 않습니다.

따라서

* Mutex
* Semaphore
* Atomic operation

등을 함께 사용해야 합니다.

추가로 공유 데이터 구조의 lifetime, memory layout, crash 발생 시 consistency도 고려해야 합니다.

---

### 꼬리 질문 — Message Queue는 단방향인가요?

Message Queue 자체를 반드시 단방향이라고 볼 필요는 없습니다.

POSIX Message Queue는 권한이 있는 프로세스들이 같은 Queue에 `send`와 `receive`를 수행할 수 있습니다.

다만 요청-응답 구조를 명확하게 만들기 위해 실무에서는

```text
Request Queue
Client ─────────▶ Server

Response Queue
Client ◀───────── Server
```

처럼 두 개를 분리할 수도 있습니다.

따라서

> **Pipe는 구조적으로 read/write 방향이 분리된 단방향 채널이고, Message Queue의 단방향 여부는 통신 프로토콜 설계에 가깝다.**

라고 답하면 좋습니다.

---

### 현업 연결

프로세스 구조에 따라 IPC 선택이 달라집니다.

```text
고속 대용량 데이터 공유
→ Shared Memory

단순 스트림
→ Pipe

비동기 작업 전달
→ Message Queue

서버 간 통신
→ Socket
```

**핵심 키워드** → Process Address Space, Pipe, FIFO, Message Queue, Shared Memory, Socket, Synchronization

**막힌 부분 / 다시 볼 것** → Shared Memory와 동기화의 관계, Pipe와 Message Queue의 방향성 차이

---

# 4. Thread Safe 하다는 것은 어떤 의미인가요?

`★★☆` · `필수`

### 내 답변 — 면접용

Thread Safe하다는 것은 여러 스레드가 동시에 같은 코드나 객체를 사용하더라도 데이터가 깨지거나 잘못된 상태가 되지 않고, 프로그램이 의도한 결과를 보장하는 것을 의미합니다.

예를 들어

```java
count++;
```

는 하나의 연산처럼 보이지만 실제로는

```text
read
increment
write
```

로 구성되기 때문에 두 스레드가 동시에 수행하면 증가가 유실될 수 있습니다.

Thread Safety를 보장하는 대표적인 방법은 `synchronized`나 Lock을 이용한 상호 배제, Atomic 연산, Immutable Object, Concurrent Collection, Thread Local과 같은 thread confinement 등이 있습니다.

Java 공식 문서에서도 공유 데이터 접근은 Thread Interference와 Memory Consistency Error를 만들 수 있으며 이를 막기 위한 방법 중 하나가 synchronization이라고 설명합니다.

---

## Race Condition

Race Condition은 여러 스레드가 동시에 공유 데이터에 접근할 때 **실행 순서에 따라 결과가 달라지는 상황**입니다.

예:

초기값

```text
count = 0
```

두 스레드가 동시에

```java
count++;
```

실행:

```text
T1 read count = 0
T2 read count = 0

T1 write 1
T2 write 1

결과 = 1
```

원래 기대 결과는 `2`입니다.

---

## Thread Safety 보장 방법

### Lock

```java
synchronized (lock) {
    count++;
}
```

가장 직관적인 방법입니다.

### Atomic

```java
AtomicInteger count = new AtomicInteger();

count.incrementAndGet();
```

내부적으로 CAS 같은 Atomic 연산을 활용합니다.

Java의 Atomic 클래스들은 `compareAndSet()` 등을 제공하며 synchronization 없이 단일 변수에 대한 원자적 연산을 지원합니다.

### Immutable Object

객체 생성 이후 상태를 변경할 수 없다면 동시에 읽는 것은 안전합니다.

예:

```java
String
```

### Thread Confinement

공유 자체를 하지 않습니다.

대표적인 방식:

```java
ThreadLocal<T>
```

### Concurrent Collection

```java
ConcurrentHashMap
ConcurrentLinkedQueue
BlockingQueue
```

등을 사용합니다.

---

## Peterson's Algorithm

두 프로세스가 공유 변수만을 이용해서 Mutual Exclusion을 구현하는 고전적인 알고리즘입니다.

핵심 변수는

```text
flag[2]
turn
```

입니다.

각 스레드는

```text
나도 Critical Section에 들어가고 싶다
+
상대방에게 우선권을 준다
```

를 표시합니다.

### 한계

실무에서는 거의 직접 사용하지 않습니다.

주요 이유는

* 두 개의 스레드만 지원
* Busy Waiting
* 현대 CPU/Compiler의 memory reordering 고려 필요
* Memory Barrier가 필요할 수 있음
* 하드웨어 Atomic instruction 기반 Lock보다 비효율적

때문입니다.

---

### 꼬리 질문 — Thread Safe하려면 반드시 Lock이 필요한가요?

아닙니다.

```text
Thread Safety
      │
      ├─ Lock
      ├─ Atomic / CAS
      ├─ Immutable
      ├─ Thread Confinement
      ├─ Message Passing
      └─ Lock-Free Data Structure
```

등 여러 방법이 있습니다.

오히려 공유 상태 자체를 없애는 것이 가장 단순한 방법일 때도 많습니다.

---

### 현업 연결

예를 들어 사용자 요청 수를 집계한다고 할 때

```java
private int count;

public void request() {
    count++;
}
```

는 멀티스레드 웹 서버에서 안전하지 않습니다.

대신

```java
AtomicLong count = new AtomicLong();

count.incrementAndGet();
```

처럼 처리할 수 있습니다.

단,

```java
if (balance >= amount) {
    balance -= amount;
}
```

처럼 여러 연산이 하나의 논리적 작업을 구성한다면 단순 Atomic 변수 하나로 해결되지 않을 수 있기 때문에 Lock이나 Transaction 같은 더 큰 범위의 동기화가 필요합니다.

**핵심 키워드** → Race Condition, Atomicity, Visibility, synchronized, CAS, Immutable, Thread Confinement

**막힌 부분 / 다시 볼 것** → 단일 Atomic 연산과 복합 연산의 차이, Peterson Algorithm의 한계

---

# 5. Thread Pool, Monitor, Fork-Join에 대해 설명해 주세요.

`★★★` · `심화`

## Thread Pool

### 면접 답변

Thread Pool은 작업이 들어올 때마다 새로운 Thread를 생성하지 않고, 미리 만들어 둔 Thread들을 재사용하여 작업을 처리하는 방식입니다.

Thread 생성과 제거에는 비용이 들고 무제한으로 Thread를 만들면 메모리와 Context Switching 비용이 증가할 수 있습니다.

따라서

```text
Task
Task ───▶ Queue ───▶ Worker Thread
Task                 Worker Thread
                     Worker Thread
```

형태로 Thread 수를 제한하여 시스템 자원을 관리합니다.

Java의 `ThreadPoolExecutor` 역시 많은 비동기 작업을 실행할 때 per-task thread 생성 비용을 줄이고, 애플리케이션이 사용하는 Thread 자원을 제한할 수 있다는 것을 주요 장점으로 설명합니다.

---

## Thread 개수는 어떻게 결정할까요?

작업 종류에 따라 다릅니다.

### CPU Bound

CPU 계산이 대부분인 작업:

```text
Thread 수 ≈ CPU Core 수
```

정도가 출발점입니다.

Thread를 지나치게 늘려도 CPU를 동시에 실행할 수 있는 Core 수는 제한되어 있기 때문입니다.

### I/O Bound

DB, Network, File I/O처럼 대기 시간이 많은 작업은 CPU가 놀고 있는 시간이 많기 때문에 Core 수보다 많은 Thread를 사용할 수 있습니다.

대표적인 추정식은

```text
Threads
≈
CPU Core 수
×
(1 + Wait Time / Compute Time)
```

입니다.

다만 이것은 초기 추정값일 뿐이고 실제 서비스에서는

* CPU Utilization
* Queue Length
* Latency
* DB Connection 수
* Memory

등을 측정하며 조정해야 합니다.

---

# Monitor

Monitor는 공유 데이터와 그 데이터를 다루는 동기화 연산을 하나의 추상화 안에 묶은 고수준 동기화 기법입니다.

보통

```text
Monitor
 ├─ Shared Data
 ├─ Mutual Exclusion
 └─ Condition Variable
```

로 생각하면 됩니다.

Java에서는 모든 객체에 intrinsic lock, 즉 monitor lock이 연결되어 있으며 `synchronized`를 사용하면 해당 monitor를 획득합니다.

```java
synchronized (obj) {
    // Critical Section
}
```

한 스레드가 monitor를 가지고 있으면 다른 스레드는 해당 monitor를 획득할 때까지 block됩니다. Java의 객체에는 monitor 외에도 `wait()` / `notify()`가 사용하는 wait set이 존재합니다.

---

# Fork-Join

큰 작업을 작은 작업들로 재귀적으로 분할한 뒤 병렬로 처리하고 결과를 합치는 방식입니다.

```text
             Task
           /      \
        Task      Task
       /   \      /   \
      T     T    T     T
       \   /      \   /
        Result    Result
           \      /
            Result
```

### Fork

작업을 작은 작업으로 나눕니다.

### Join

나누어진 작업들의 결과를 합칩니다.

Java의 `ForkJoinPool`은 **Work-Stealing**을 사용합니다.

```text
Worker A Queue: [T1 T2 T3 T4]
Worker B Queue: []

Worker B → Worker A 작업 일부 steal
```

즉 작업이 없는 Worker가 다른 Worker의 Queue에서 작업을 가져갑니다. Oracle 역시 ForkJoinPool의 핵심 차이를 work-stealing이라고 설명합니다.

---

## 정렬에는 어떤 전략을 사용하면 좋은가요?

데이터 크기와 작업 특성을 먼저 봅니다.

작은 배열이라면 일반적인

```java
Arrays.sort()
```

가 병렬화 비용이 없기 때문에 적절합니다.

반대로 매우 큰 배열이고 비교 연산이 CPU Bound라면

```text
Array
 ↓
Split
 ↓
Parallel Sort
 ↓
Merge
```

처럼 Fork-Join을 이용할 수 있습니다.

단순히 스레드를 많이 사용하는 것이 항상 빠른 것은 아닙니다.

작업 크기가 너무 작으면

```text
Task 생성
Scheduling
Synchronization
Merge
```

비용이 실제 정렬보다 커질 수 있습니다.

따라서 일반적으로 일정 크기 이하가 되면 Sequential Sort로 전환하는 **Threshold** 전략을 사용합니다.

### 현업 연결

```text
웹 요청 처리
→ Thread Pool

공유 객체 보호 + 조건 대기
→ Monitor

대규모 CPU 병렬 계산
→ Fork-Join
```

**핵심 키워드** → Thread Pool, Task Queue, Worker Thread, Monitor, Condition Variable, Fork, Join, Work-Stealing

**막힌 부분 / 다시 볼 것** → CPU Bound와 I/O Bound Thread Pool 크기 차이, ForkJoin의 Work-Stealing

---

# 6. 동기화를 구현하기 위한 하드웨어적인 해결 방법에 대해 설명해 주세요.

`★★★` · `심화`

### 내 답변 — 면접용

소프트웨어 Lock의 가장 아래에는 CPU가 제공하는 Atomic Instruction이 있습니다.

대표적으로

* Test-And-Set
* Compare-And-Swap / Compare-And-Set
* Swap
* Fetch-And-Add
* LL/SC

같은 연산이 있습니다.

이 연산들은 여러 CPU가 동시에 접근하더라도 Read-Modify-Write를 원자적으로 수행할 수 있도록 보장합니다.

하지만 Atomicity만으로 모든 동기화 문제가 해결되는 것은 아닙니다. 현대 CPU와 Compiler는 성능을 위해 명령과 메모리 접근 순서를 변경할 수 있기 때문에 Memory Barrier 또는 Acquire/Release와 같은 Memory Ordering도 필요합니다.

실제 Mutex 구현에서는 Atomic CAS로 먼저 Lock 획득을 시도하고, 경쟁이 발생하여 오래 기다려야 하는 경우 futex 같은 커널 기능을 이용해 Thread를 재우는 방식이 사용될 수 있습니다.

---

## Test-And-Set

대략 다음 연산을 하드웨어가 Atomic하게 처리한다고 볼 수 있습니다.

```text
old = lock
lock = true
return old
```

이를 이용해

```java
while (testAndSet(lock)) {
    // spin
}
```

형태의 Spin Lock을 구현할 수 있습니다.

---

## Compare-And-Swap

```text
현재 값 == 기대 값
        ↓
      true
        ↓
새로운 값으로 변경
```

개념적으로

```java
CAS(address, expected, newValue)
```

입니다.

예:

```text
value = 10

CAS(value, 10, 11)
→ 성공

CAS(value, 10, 12)
→ 실패
```

Java의 `AtomicInteger.compareAndSet()` 등이 대표적인 예입니다.

---

## Memory Barrier

멀티코어에서는 CPU와 Compiler가 성능 향상을 위해 메모리 연산을 재배치할 수 있습니다.

예:

```java
data = 100;
ready = true;
```

작성 순서는 이렇더라도 다른 CPU가 반드시 동일한 순서로 관찰한다는 보장이 필요한 경우가 있습니다.

Memory Barrier는 특정 지점을 기준으로 메모리 연산의 순서를 제한합니다.

Linux Kernel 문서 역시 Memory Barrier를 CPU/Compiler의 memory operation reordering을 제한하여 여러 CPU 또는 장치 간 상호작용을 올바르게 만드는 장치로 설명합니다.

---

## volatile은 무엇인가요?

Java 기준으로 `volatile`은 크게

```text
Visibility
+
Ordering
```

을 제공합니다.

한 Thread가 volatile 변수에 write하면 이후 다른 Thread가 같은 volatile 변수를 read할 때 해당 변경을 볼 수 있도록 happens-before 관계가 형성됩니다.

하지만

```java
volatile int count;
count++;
```

는 Thread Safe하지 않습니다.

왜냐하면 `count++`은

```text
read
+
increment
+
write
```

라는 복합 연산이기 때문입니다.

즉,

```text
volatile
≠
mutex
```

입니다.

volatile은 Mutual Exclusion을 제공하지 않습니다.

---

## 멀티코어에서는 어떻게 동기화가 이뤄지나요?

멀티코어에서는 각 Core가 Cache를 가지고 있기 때문에 단순히 Memory 값만 생각하면 안 됩니다.

핵심 요소는

```text
Atomic Instruction
+
Cache Coherence
+
Memory Ordering / Barrier
```

입니다.

CPU의 Atomic RMW 명령이 공유 상태 변경의 원자성을 제공하고, Cache Coherence Protocol이 Core 간 Cache 상태를 조정하며, Memory Barrier 또는 Acquire/Release semantics가 메모리 접근 순서를 제어합니다.

---

### 실제 Lock 구조

단순화하면 다음처럼 생각할 수 있습니다.

```text
lock()
  ↓
CAS 시도
  ↓
성공 ───────────▶ Critical Section

실패
  ↓
잠시 Spin
  ↓
계속 실패
  ↓
Kernel / Futex Wait
```

즉 하드웨어 Atomic Instruction과 OS 스케줄링을 결합합니다.

**핵심 키워드** → CAS, Test-And-Set, Atomic RMW, Memory Barrier, Cache Coherence, volatile, Acquire/Release

**막힌 부분 / 다시 볼 것** → volatile은 visibility이지 mutual exclusion이 아니라는 점, Atomicity와 Memory Ordering의 차이

---

# 7. 동기와 비동기, 블로킹과 논블로킹의 차이에 대해 설명해 주세요.

`★★★` · `필수`

### 내 답변 — 면접용

동기/비동기와 블로킹/논블로킹은 서로 다른 관점입니다.

동기와 비동기는 **작업 완료를 누가 확인하고 흐름을 이어 가는지**, 블로킹과 논블로킹은 **호출한 스레드가 작업을 기다리는 동안 멈추는지**를 기준으로 구분할 수 있습니다.

Blocking은 호출한 Thread가 결과를 받을 때까지 대기하는 방식이고, Non-Blocking은 작업이 바로 완료되지 않더라도 제어권을 즉시 돌려받습니다.

Synchronous 방식에서는 호출 측이 작업의 완료 흐름과 밀접하게 연결되어 있고, Asynchronous 방식에서는 작업 완료 후 callback, Future, event 같은 방법으로 결과를 전달받는 구조를 사용합니다.

따라서 동기/비동기와 블로킹/논블로킹은 반드시 같은 개념이 아닙니다.

---

## Blocking

```java
String result = read();
```

`read()`가 끝날 때까지 현재 Thread가 기다립니다.

```text
Caller
  │
  ├── read()
  │
  │   Waiting...
  │   Waiting...
  │
  └── result
```

---

## Non-Blocking

```text
read()
 ↓
아직 데이터 없음
 ↓
즉시 return
```

호출자가 다른 작업을 할 수 있습니다.

---

## Synchronous

호출 흐름이 작업의 완료 여부와 직접 연결되어 있습니다.

```text
A 호출
 ↓
B 수행
 ↓
A가 완료를 확인
```

## Asynchronous

작업을 요청한 뒤 완료 처리를 별도의 callback, event, future 등의 메커니즘으로 받습니다.

```text
A ── 요청 ──▶ B

A 다른 작업 수행

B 완료
   ↓
callback / event
   ↓
A 결과 처리
```

---

## 네 가지 조합

| 조합                   | 예                                        |
| -------------------- | ---------------------------------------- |
| Sync + Blocking      | 일반적인 blocking 함수 호출                      |
| Sync + Non-Blocking  | polling                                  |
| Async + Non-Blocking | event loop / callback                    |
| Async + Blocking     | async 작업을 시작한 뒤 `Future.get()`으로 기다리는 경우 |

### 동기 + Non-Blocking

```java
while (!ready()) {
    doOtherWork();
}
```

호출자는 완료를 직접 계속 확인하지만 특정 호출 때문에 계속 잠들지는 않습니다.

### 비동기 + Blocking

비동기 작업을 만들 수는 있지만 결과를 이렇게 받는 순간

```java
Future<Result> future = asyncTask();

Result result = future.get();
```

현재 Thread는 block됩니다.

즉 API 자체의 작업 모델과 결과를 기다리는 코드의 Blocking 여부를 별도로 볼 수 있습니다.

---

# I/O Multiplexing

하나의 Thread가 여러 Socket/File Descriptor를 감시하여 준비된 I/O만 처리하는 방식입니다.

Linux에서는

```text
select
poll
epoll
```

등이 있습니다.

```text
Socket A ─┐
Socket B ─┤
Socket C ─┼─▶ epoll ─▶ Ready Socket 처리
Socket D ─┤
Socket E ─┘
```

따라서

```text
Connection 10,000개
=
Thread 10,000개
```

로 만들 필요가 없습니다.

Linux `epoll`은 여러 File Descriptor를 감시하며 많은 FD에 대해 확장성이 좋은 I/O event notification mechanism으로 설명됩니다.

---

## Non-Blocking I/O의 결과는 어떻게 받나요?

대표적으로

### Polling

```text
read()
→ EAGAIN

다른 작업

read()
→ EAGAIN

read()
→ DATA
```

### I/O Multiplexing

```text
epoll_wait()
       ↓
어떤 socket이 ready인지 확인
       ↓
read()
```

### Callback / Event

Runtime이나 Framework가 작업 완료 시 callback을 실행합니다.

### Future / Promise

```java
future.thenApply(...)
```

형태로 처리할 수 있습니다.

---

### 현업 연결

고성능 네트워크 서버는 Connection마다 Thread 하나를 만드는 모델보다

```text
Non-Blocking Socket
+
I/O Multiplexing
+
Event Loop
```

구조를 사용할 수 있습니다.

이 방식은 많은 동시 연결에서 Thread와 Context Switching 비용을 줄이는 데 유리합니다.

**핵심 키워드** → Blocking, Non-Blocking, Sync, Async, Polling, Event Loop, select, poll, epoll, Future

**막힌 부분 / 다시 볼 것** → 동기/비동기와 블로킹/논블로킹을 같은 축으로 생각하지 않기, epoll의 역할

---

# Part 2. 개발상식 코너

# 8. 가상화가 무엇이고, 이것이 가상머신과 어떠한 차이가 있는지 설명해 주세요.

`★★☆` · `필수`

### 내 답변 — 면접용

가상화는 CPU, 메모리, 네트워크, 운영체제 같은 실제 컴퓨팅 자원을 추상화하여 논리적인 자원처럼 사용할 수 있도록 만드는 기술 자체를 의미합니다.

반면 Virtual Machine은 가상화 기술을 이용해 만들어진 하나의 실행 환경입니다.

즉

```text
Virtualization = 기술 / 개념
Virtual Machine = 그 기술을 이용한 실행 단위
```

라고 볼 수 있습니다.

VM은 Hypervisor를 통해 하드웨어를 가상화하고 각각 독립적인 Guest OS와 Kernel을 실행합니다.

반면 Docker Container는 일반적으로 Host OS의 Kernel을 공유하면서 프로세스, Network, File System 등을 격리하는 OS-level isolation을 사용하기 때문에 VM보다 가볍고 시작이 빠릅니다.

Microsoft 문서 역시 VM은 자체 OS와 Kernel을 가지지만 Container는 Host Kernel 위에서 동작하는 lightweight isolated environment라고 설명합니다.

---

## VM 구조

```text
App        App
 ↓          ↓
Guest OS   Guest OS
 ↓          ↓
──────── Hypervisor ────────
           ↓
        Hardware
```

각 VM에 Guest Kernel이 존재합니다.

### 장점

* 강한 격리
* 서로 다른 OS 사용 가능

### 단점

* 상대적으로 높은 메모리 사용
* 이미지 크기 큼
* 시작 시간 길어짐

---

# Docker Container

```text
App        App        App
 ↓          ↓          ↓
Container Container Container
      \      |      /
        Host Kernel
             ↓
          Hardware
```

Host Kernel을 공유합니다.

### 장점

* 가벼움
* 빠른 Startup
* 높은 배포 밀도
* Image 기반 환경 일관성

---

## Docker는 가상화인가요?

면접에서는 다음처럼 답하면 가장 안전합니다.

> Docker는 VM처럼 하드웨어 전체를 가상화하는 Hypervisor 기반 가상머신은 아니고, Host Kernel을 공유하며 Namespace와 cgroup 등을 이용하는 Container 또는 OS-level virtualization 기술입니다.

Docker는 Container를 시작할 때 namespace와 control group을 생성하여 프로세스와 자원을 격리합니다.

---

## 왜 Docker를 많이 사용하나요?

가장 큰 이유 중 하나는 **환경 일관성**입니다.

```text
Developer PC
    ↓
Docker Image
    ↓
CI Server
    ↓
Staging
    ↓
Production
```

모두 같은 이미지를 실행할 수 있습니다.

그래서

```text
제 컴퓨터에서는 되는데 서버에서는 안 됩니다.
```

라는 환경 차이를 줄이는 데 도움이 됩니다.

또 VM보다 상대적으로 가볍기 때문에 동일한 서버에서 더 많은 애플리케이션을 실행하기도 쉽습니다.

---

## Host Kernel을 공유하면 다른 Container가 간섭할 수 있지 않나요?

가능성 자체는 존재합니다.

Container의 격리는 VM과 동일한 보안 경계가 아닙니다. Microsoft 역시 일반적인 Container 격리는 VM만큼 강한 security boundary를 제공하지 않는다고 설명합니다.

이를 줄이기 위해 Linux/Docker에서는

* Namespace
* cgroup
* Linux Capabilities
* seccomp
* SELinux
* AppArmor
* User Namespace
* Rootless Container

등을 사용합니다.

예를 들어 Namespace는 다른 Container의 Process나 Network를 보지 못하도록 격리하고, cgroup은 CPU와 Memory 같은 자원 사용량을 제한합니다.

---

## Docker 위에 Docker를 올릴 수 있나요?

가능합니다.

보통 **Docker-in-Docker, DinD**라고 부릅니다.

```text
Host
 ↓
Docker Container
 ↓
Docker Daemon
 ↓
Container
```

CI 환경 등에서 사용될 수 있습니다.

다만

* privileged 권한
* Storage
* Networking
* Security

문제가 복잡해질 수 있습니다.

또 다른 방법으로 Container 안에서 Docker CLI만 실행하고 Host의 Docker Socket에 연결하는 방식도 있는데, 이 경우 Container가 Host Docker Daemon을 제어할 수 있기 때문에 매우 높은 권한을 얻게 된다는 보안 문제가 있습니다.

**핵심 키워드** → Virtualization, Hypervisor, VM, Guest OS, Container, Host Kernel, Namespace, cgroup, Isolation

**막힌 부분 / 다시 볼 것** → Container는 VM이 아니라 Host Kernel을 공유한다는 점, VM보다 격리 수준이 약할 수 있다는 점

---

# 9. CI/CD를 사용해 본 경험이 있나요?

`★★☆` · `필수`

## 내 답변 — 실제 경험 기반 면접용

네, 프로젝트에서 GitHub Actions와 Vercel을 이용한 CI/CD 흐름을 경험했습니다.

Alkong 프로젝트에서는 GitHub Actions를 이용해 PR이나 변경 사항에 대해 QA와 Regression Test를 실행하고, Vercel Preview Deployment에서 실제 배포 결과를 확인한 뒤 Production으로 반영하는 흐름을 사용했습니다.

또 강의 정보 RAG 챗봇 프로젝트에서는 `main` 브랜치에 코드가 Push되면 GitHub Actions가 Self-hosted Runner에서 Repository를 Checkout하고 Docker Image를 Build한 뒤, 기존 Container를 내리고 새 Container를 실행하도록 자동 배포를 구성했습니다.

당시 파이프라인은

```text
Push
→ Checkout
→ Docker Build
→ 기존 Container 제거
→ 새로운 Container 실행
```

구조였습니다.

다만 이 프로젝트에는 자동 Unit Test나 Integration Test 단계가 충분히 포함되어 있지 않았기 때문에 엄밀히 보면 완전한 CI라기보다는 **Build와 Deployment 자동화에 가까운 CD 중심 파이프라인**이었다고 생각합니다.

이 경험을 통해 단순히 자동 배포만 만드는 것이 아니라, 배포 전에 Test와 Validation을 Gate로 두는 것이 중요하다는 것을 배웠습니다.

GitHub Actions 역시 Pull Request마다 build/test를 수행하고 merged code를 production에 배포하는 CI/CD workflow를 지원합니다.

---

# 소스코드 작성부터 사용자 배포까지

일반적인 흐름은 다음과 같습니다.

```text
Developer
   ↓
Commit / Push
   ↓
Pull Request
   ↓
CI
 ├─ Lint
 ├─ Unit Test
 ├─ Static Analysis
 └─ Build
   ↓
Artifact / Docker Image 생성
   ↓
Registry
   ↓
Staging Deployment
   ↓
Integration / E2E / Smoke Test
   ↓
Production Deployment
   ↓
Monitoring
   ↓
Rollback / Continue
```

---

## Build와 Deploy의 차이

### Build

소스코드를 실행 가능한 결과물로 만드는 과정입니다.

예:

```text
.java
 ↓
compile
 ↓
.jar
```

또는

```text
Source Code
 ↓
docker build
 ↓
Docker Image
```

### Deploy

Build 결과물을 실제 실행 환경에 올려 사용자에게 제공하는 과정입니다.

```text
Docker Image
 ↓
Production Server
 ↓
Container Run
```

---

# CI

Continuous Integration.

개발자의 코드 변경을 자주 통합하면서

```text
Build
Test
Lint
Static Analysis
```

등을 자동 실행해 문제가 있는 코드를 빠르게 발견합니다.

---

# CD

CD는 두 의미가 있습니다.

### Continuous Delivery

Production에 배포할 수 있는 상태까지 자동화하지만

```text
Production Deploy
```

에는 사람의 승인 절차가 있을 수 있습니다.

### Continuous Deployment

검증을 통과한 변경 사항이 Production까지 자동 배포됩니다.

GitHub 문서에서도 Continuous Deployment를 software update를 자동으로 publish/deploy하는 방식으로 설명하며 일반적으로 배포 전에 build와 test를 수행합니다.

---

# 테스트는 어디에 넣는 것이 좋나요?

가능하면 실패를 **빨리** 발견하도록 값싼 테스트부터 실행합니다.

```text
Lint
 ↓
Unit Test
 ↓
Build
 ↓
Integration Test
 ↓
E2E Test
 ↓
Deploy
 ↓
Smoke Test
```

예를 들어 Unit Test가 실패했는데 Docker Image를 만들고 E2E Test까지 실행하면 CI 자원이 낭비됩니다.

그래서

> 빠르고 값싼 검증을 앞에, 느리고 실제 환경 의존적인 검증을 뒤에 둔다.

가 기본 원칙입니다.

---

# 무중단 배포

## Rolling Deployment

Instance를 조금씩 새로운 버전으로 교체합니다.

```text
v1 v1 v1 v1

v2 v1 v1 v1

v2 v2 v1 v1

v2 v2 v2 v2
```

장점:

* 추가 Resource가 상대적으로 적음

단점:

* 배포 중 v1과 v2가 동시에 존재

---

## Blue-Green

```text
Blue = Current Production
Green = New Version
```

Green 환경을 완전히 준비한 뒤 Traffic을 한 번에 전환합니다.

```text
User → Blue

검증 완료

User → Green
```

장점:

* 빠른 전환
* 빠른 Rollback

단점:

* 두 환경이 동시에 필요해서 Resource 비용 증가

---

## Canary

일부 사용자에게 먼저 새 버전을 제공합니다.

```text
95% → v1
 5% → v2
```

문제가 없다면

```text
50% → v2
100% → v2
```

로 증가시킵니다.

Risk를 점진적으로 확인할 수 있습니다.

Kubernetes Deployment는 RollingUpdate와 revision rollback을 지원하며, 별도의 Deployment를 이용한 canary pattern도 문서화하고 있습니다.

---

# 배포 장애 시 Rollback은 어떻게 준비하나요?

가장 중요한 것은 **이전 정상 버전이 무엇인지 명확해야 한다는 것**입니다.

따라서

```text
v1.0.1
v1.0.2
v1.0.3
```

처럼 Artifact나 Docker Image를 immutable하게 버전 관리합니다.

새 버전인

```text
v1.0.3
```

에서 장애가 발생하면

```text
v1.0.2
```

로 다시 배포합니다.

또

* 이전 Deployment Revision 보관
* Health Check
* Monitoring
* Feature Flag
* DB Migration의 backward compatibility

등을 같이 고려해야 합니다.

특히 DB Schema 변경은 Application만 이전 버전으로 Rollback했을 때 호환되지 않을 수 있기 때문에

```text
Expand
→ Application Migration
→ Contract
```

같은 점진적인 Schema 변경 방식이 필요할 수 있습니다.

---

# 왜 서버에서 직접 Build하지 않고 Image를 만들어 배포하나요?

가장 큰 장점은 **Reproducibility와 Immutability**입니다.

서버에서 직접

```text
git pull
npm install
build
```

을 하면 Server마다 환경 차이가 발생할 수 있습니다.

반면

```text
CI
 ↓
Docker Image:v123
 ↓
Registry
 ↓
Server A
Server B
Server C
```

처럼 동일한 Image를 실행하면 같은 결과물을 배포할 수 있습니다.

또 장애가 발생하면

```text
Image:v123
 ↓
Image:v122
```

처럼 빠르게 Rollback할 수도 있습니다.

---

**핵심 키워드** → CI, Continuous Delivery, Continuous Deployment, GitHub Actions, Build, Artifact, Docker Image, Rolling, Blue-Green, Canary, Rollback

**막힌 부분 / 다시 볼 것** → Delivery와 Deployment의 차이, DB Migration이 포함된 Rollback 전략

---

# Part 3. 손코딩

# 워밍업 — 병합 정렬

## 구현 범위

재귀 분할 + `merge` 병합.

**임시 배열을 매번 생성하지 않고 처음 한 번만 만들어 재사용한다.**

```java
public class MergeSort {

    static int[] temp;

    public static void mergeSort(int[] arr) {

        // 임시 배열은 처음 한 번만 생성
        temp = new int[arr.length];

        mergeSort(arr, 0, arr.length - 1);
    }

    static void mergeSort(int[] arr, int left, int right) {

        // 원소가 1개이면 종료
        if (left >= right) {
            return;
        }

        int mid = (left + right) / 2;

        // 왼쪽 절반 정렬
        mergeSort(arr, left, mid);

        // 오른쪽 절반 정렬
        mergeSort(arr, mid + 1, right);

        // 두 정렬 배열 병합
        merge(arr, left, mid, right);
    }

    static void merge(int[] arr, int left, int mid, int right) {

        int i = left;
        int j = mid + 1;
        int k = left;

        // 두 배열 비교
        while (i <= mid && j <= right) {

            // 같은 값이면 왼쪽 원소를 먼저 넣음
            // → Stable Sort
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
            }
        }

        // 왼쪽 배열 남은 값
        while (i <= mid) {
            temp[k++] = arr[i++];
        }

        // 오른쪽 배열 남은 값
        while (j <= right) {
            temp[k++] = arr[j++];
        }

        // 정렬 결과 원본 배열에 복사
        for (int idx = left; idx <= right; idx++) {
            arr[idx] = temp[idx];
        }
    }
}
```

---

## 병합 정렬 과정

```text
[5 2 4 1]

      split

[5 2]        [4 1]

split        split

[5] [2]      [4] [1]

merge

[2 5]        [1 4]

        merge

[1 2 4 5]
```

---

## 왜 Stable Sort인가요?

두 값이 동일한 경우

```java
if (arr[i] <= arr[j])
```

로 **왼쪽 값을 먼저 선택**하기 때문입니다.

예를 들어

```text
A(90점)
B(90점)
```

이 원래

```text
A → B
```

순서였다면 정렬 후에도 같은 점수 사이의 상대 순서가 유지됩니다.

---

## 매번 임시 배열을 만들지 않는 이유

다음처럼 `merge()`마다

```java
new int[right - left + 1]
```

을 하면 Merge 호출 때마다 새로운 배열이 생성됩니다.

대신

```java
temp = new int[arr.length];
```

을 처음 한 번 만들고 모든 Merge에서 공유하면 불필요한 객체 생성과 GC 부담을 줄일 수 있습니다.

---

**시간복잡도** →

```text
O(N log N)
```

Best / Average / Worst 모두 동일합니다.

**공간복잡도** →

```text
O(N)
```

임시 배열 `temp`가 필요합니다.

재귀 호출 스택은 추가적으로 `O(log N)`입니다.

**막힌 부분 / 다시 볼 것** → merge index 관리, 동일 값에서 왼쪽을 먼저 선택해야 Stable Sort가 된다는 점

---

# 응용 문항

## 12. 정렬된 두 LinkedList를 합쳐 하나의 정렬된 LinkedList로 만드는 코드를 작성해 보세요.

```java
class Node {

    int value;
    Node next;

    Node(int value) {
        this.value = value;
    }
}

public class MergeLinkedList {

    static Node merge(Node a, Node b) {

        // 시작 노드 처리를 편하게 하기 위한 Dummy Node
        Node dummy = new Node(0);

        Node current = dummy;

        while (a != null && b != null) {

            if (a.value <= b.value) {

                current.next = a;
                a = a.next;

            } else {

                current.next = b;
                b = b.next;
            }

            current = current.next;
        }

        // 둘 중 하나가 남아 있으면 그대로 연결
        if (a != null) {
            current.next = a;
        }

        if (b != null) {
            current.next = b;
        }

        return dummy.next;
    }
}
```

---

## 예

```text
A

1 → 3 → 7

B

2 → 4 → 5 → 8
```

두 Pointer를 비교합니다.

```text
1 vs 2
↓
1

3 vs 2
↓
1 → 2

3 vs 4
↓
1 → 2 → 3
```

최종:

```text
1 → 2 → 3 → 4 → 5 → 7 → 8
```

---

## 왜 Dummy Node를 사용하나요?

Dummy Node가 없다면 첫 노드를 연결할 때

```java
if (head == null) {
    head = ...
}
```

같은 예외 처리가 필요합니다.

Dummy Node를 사용하면 모든 Node에 대해 동일하게

```java
current.next = node;
```

를 수행할 수 있습니다.

마지막에

```java
return dummy.next;
```

만 하면 됩니다.

---

**시간복잡도** →

두 LinkedList의 길이를 각각 N, M이라고 하면

```text
O(N + M)
```

모든 Node를 최대 한 번씩 방문합니다.

**공간복잡도** →

```text
O(1)
```

기존 Node의 `next`를 다시 연결하므로 새로운 LinkedList 전체를 만들지 않습니다.

Dummy Node 하나만 추가됩니다.

**다른 사람 풀이에서 배운 점** →

Dummy Node를 이용하면 Head 처리에 대한 예외 분기를 없앨 수 있고, 남은 LinkedList는 이미 정렬되어 있으므로 하나씩 다시 순회하지 않고

```java
current.next = a;
```

또는

```java
current.next = b;
```

로 한 번에 연결할 수 있습니다.

---

# 회고

## 미해결 질문

* [ ] CAS 기반 Lock-Free 자료구조에서 발생할 수 있는 **ABA Problem**은 무엇이며 어떻게 해결하는가?
* [ ] `epoll`의 **Level Triggered와 Edge Triggered**는 어떤 차이가 있으며, Edge Triggered에서는 왜 `EAGAIN`이 발생할 때까지 읽어야 하는가?

## 이번 주에 가장 약했던 주제

추천 점검 대상 →

```text
Hardware Synchronization
+
Memory Ordering
+
Blocking / Non-Blocking I/O
```

특히 아래 관계를 말로 설명할 수 있는지 확인한다.

```text
Atomicity ≠ Visibility ≠ Ordering
```

그리고

```text
Sync / Async
        ≠
Blocking / Non-Blocking
```

이라는 점을 정확하게 구분한다.

---

# 면접 직전 1분 압축

```text
Mutex
→ 1개 Thread, Ownership

Semaphore
→ N개 Permit, Resource Count

Deadlock
→ Mutual Exclusion
→ Hold & Wait
→ No Preemption
→ Circular Wait

IPC
→ Pipe / Queue / Shared Memory / Socket

Thread Safe
→ 여러 Thread가 동시에 사용해도 상태 일관성 유지

Thread Pool
→ Thread 재사용 + 개수 제한

Monitor
→ Lock + Shared State + Condition

ForkJoin
→ Divide & Conquer + Work Stealing

Hardware Sync
→ CAS / Test-And-Set / Memory Barrier

volatile
→ Visibility + Ordering
→ Mutual Exclusion X

Blocking
→ Thread 대기

Non-Blocking
→ 즉시 제어권 반환

Virtualization
→ 가상화 기술

VM
→ Guest Kernel 포함

Container
→ Host Kernel 공유

CI
→ Build/Test 자동화

CD
→ Release/Deploy 자동화
```

---

# 참고한 자료

* Oracle Java Concurrency — Intrinsic Locks, Synchronization, Atomic Variables, ThreadPoolExecutor, ForkJoinPool. Java 객체의 monitor와 `synchronized`, Thread Pool 및 work-stealing 동작 확인.
* Linux man-pages — `futex`, `pipe`, POSIX Shared Memory, POSIX Message Queue, `epoll`. Linux의 실제 synchronization 및 IPC/I/O 동작 확인.
* Linux Kernel Documentation — Memory Barriers 및 Atomic Operation의 ordering semantics 확인.
* PostgreSQL / MySQL Documentation — 실제 DB Deadlock 탐지, rollback, transaction retry 및 consistent lock ordering 사례 확인.
* Docker Documentation — Namespace, cgroup과 Container isolation 구조 확인.
* Microsoft Learn — Container와 Virtual Machine의 Kernel 및 isolation 구조 비교.
* GitHub Actions Documentation — CI의 Build/Test 자동화와 CD Workflow 구조 확인.
* Kubernetes Documentation — Rolling Update, Revision Rollback, Canary Deployment 구조 확인.
