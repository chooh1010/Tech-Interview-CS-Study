# Week 03 — OS I — 커널과 프로세스

> **시스템 콜, 인터럽트, 프로세스와 스케줄링**

---

# Part 1. 운영체제

## 1. 시스템 콜이 무엇인지 설명해 주세요.

### 내 답변

시스템 콜은 **사용자 프로그램이 운영체제 커널의 기능을 요청하기 위한 인터페이스**입니다.

일반적인 프로그램은 사용자 모드에서 실행되기 때문에 파일이나 디바이스 같은 시스템 자원에 직접 접근할 수 없습니다. 따라서 파일 읽기, 프로세스 생성, 네트워크 통신처럼 커널 권한이 필요한 작업은 시스템 콜을 통해 요청해야 합니다.

시스템 콜이 발생하면 CPU는 사용자 모드에서 커널 모드로 전환되고, 커널이 요청된 작업을 처리한 뒤 다시 사용자 모드로 복귀합니다.

대표적으로 `read`, `write`, `open`, `fork`, `exec` 등이 있습니다.

### 핵심 키워드

`User Mode` / `Kernel Mode` / `OS 기능 요청` / `권한 전환` / `read` / `write` / `fork`

### 꼬리 질문

**Q. 시스템 콜의 예시는?**

* 파일 관리: `open()`, `read()`, `write()`, `close()`
* 프로세스 관리: `fork()`, `exec()`, `exit()`
* 메모리 관리: `mmap()`
* 통신: `socket()`

---

**Q. 시스템 콜은 어떤 과정으로 실행되나요?**

1. 사용자 프로그램이 시스템 콜 호출
2. 시스템 콜 번호와 인자를 레지스터 등에 저장
3. CPU의 시스템 콜 명령 실행
4. User Mode → Kernel Mode 전환
5. 커널이 시스템 콜 번호를 확인
6. 대응하는 커널 함수 실행
7. 결과 반환
8. Kernel Mode → User Mode 복귀

---

**Q. 시스템 콜의 유형은?**

대표적으로 다음과 같이 나눌 수 있습니다.

* 프로세스 제어
* 파일 관리
* 디바이스 관리
* 정보 관리
* 통신
* 보호 및 권한 관리

---

**Q. Dual Mode란?**

CPU 실행 권한을 크게

* User Mode
* Kernel Mode

두 가지로 분리하는 구조입니다.

User Mode에서는 제한된 명령만 사용할 수 있고, Kernel Mode에서는 메모리·디바이스 등 시스템 전체에 접근할 수 있습니다.

---

**Q. 왜 User Mode와 Kernel Mode를 구분하나요?**

사용자 프로그램이 하드웨어나 다른 프로세스의 메모리에 마음대로 접근하면 시스템 전체가 손상될 수 있기 때문입니다.

따라서 중요한 작업은 커널만 수행하도록 하여 **보안과 시스템 안정성**을 확보합니다.

---

**Q. 서로 다른 시스템 콜을 어떻게 구분하나요?**

각 시스템 콜에는 **시스템 콜 번호(System Call Number)**가 존재합니다.

프로그램이 시스템 콜을 호출하면 번호를 CPU 레지스터 등에 전달하고, 커널은 해당 번호를 이용해 적절한 시스템 콜 처리 함수를 선택합니다.

---

## 2. 인터럽트가 무엇인지 설명해 주세요.

### 내 답변

인터럽트는 **CPU가 현재 실행 중인 작업을 잠시 중단하고 특정 이벤트를 처리하도록 알리는 신호**입니다.

예를 들어 키보드 입력이 발생하거나 디스크 I/O가 완료되면 하드웨어가 CPU에 인터럽트를 발생시킬 수 있습니다.

CPU는 현재 실행 상태를 저장하고 인터럽트 벡터를 이용해 해당 인터럽트 처리 루틴인 ISR을 실행합니다. 처리가 끝나면 저장했던 상태를 복원하여 기존 작업을 계속합니다.

인터럽트를 사용하면 CPU가 장치 상태를 계속 확인하지 않아도 되기 때문에 Polling 방식보다 효율적으로 동작할 수 있습니다.

### 핵심 키워드

`이벤트 알림` / `ISR` / `Interrupt Vector` / `상태 저장` / `Polling`

### 꼬리 질문

**Q. 인터럽트는 어떻게 처리하나요?**

1. 인터럽트 발생
2. CPU가 현재 명령 실행을 마침
3. 현재 CPU 상태 저장
4. 인터럽트 벡터 확인
5. ISR 실행
6. 인터럽트 처리
7. 기존 CPU 상태 복원
8. 원래 프로그램 실행 재개

---

**Q. Polling이란?**

CPU가 장치의 상태를 계속 반복해서 확인하는 방식입니다.

```text
장치 준비됐어?
→ 아니요
장치 준비됐어?
→ 아니요
장치 준비됐어?
→ 네
```

단순하지만 CPU 시간이 낭비될 수 있습니다.

인터럽트 방식에서는 장치가 필요한 순간 CPU에게 알려주기 때문에 일반적으로 더 효율적입니다.

---

**Q. HW / SW 인터럽트 차이는?**

하드웨어 인터럽트는 외부 하드웨어 장치에서 발생합니다.

예:

* 키보드 입력
* 네트워크 패킷 수신
* 타이머
* 디스크 I/O 완료

소프트웨어적으로 발생하는 트랩이나 예외에는 다음과 같은 것이 있습니다.

* 시스템 콜
* 0으로 나누기
* 잘못된 메모리 접근

---

**Q. 여러 인터럽트가 동시에 발생하면?**

보통 **인터럽트 우선순위**에 따라 처리합니다.

중요한 인터럽트를 먼저 처리하며, 처리 중 특정 인터럽트를 마스킹할 수도 있습니다.

시스템에 따라 높은 우선순위 인터럽트가 현재 처리 중인 인터럽트를 중단시키는 **Nested Interrupt**도 가능합니다.

---

# 3. 프로세스가 무엇인가요?

### 내 답변

프로세스는 **실행 중인 프로그램**입니다.

프로그램은 디스크에 저장되어 있는 정적인 코드라면, 프로세스는 프로그램이 메모리에 올라가 CPU를 할당받아 실행되고 있는 동적인 상태입니다.

프로세스마다 독립적인 주소 공간과 시스템 자원을 가지며 운영체제는 프로세스의 상태, CPU 레지스터, 프로그램 카운터 등의 정보를 PCB에 저장하여 관리합니다.

### 핵심 키워드

`실행 중인 프로그램` / `독립적인 주소 공간` / `PCB` / `CPU 자원`

### 꼬리 질문

**Q. 프로그램 / 프로세스 / 스레드 차이는?**

```text
프로그램
↓ 실행
프로세스
↓ 실행 흐름
스레드
```

프로그램은 디스크에 저장된 실행 파일입니다.

프로세스는 실행 중인 프로그램입니다.

스레드는 프로세스 내부의 **실행 단위**입니다.

같은 프로세스의 스레드들은 Code, Data, Heap 등을 공유하지만 각각의 Stack과 CPU 실행 상태는 별도로 가집니다.

---

**Q. PCB란?**

Process Control Block으로 운영체제가 프로세스를 관리하기 위한 정보를 저장하는 자료구조입니다.

대표적으로

* PID
* 프로세스 상태
* Program Counter
* CPU Register
* 스케줄링 정보
* 메모리 관리 정보
* 열린 파일 정보

등이 저장됩니다.

---

**Q. 스레드도 PCB를 가지고 있나요?**

개념적으로 스레드는 프로세스의 자원은 공유하지만 자신만의 실행 상태를 관리하기 위한 **TCB(Thread Control Block)**를 가집니다.

여기에는

* Program Counter
* Register
* Stack Pointer
* Thread ID

등이 저장됩니다.

실제 구현은 운영체제마다 다릅니다.

---

**Q. Linux에서 프로세스와 스레드는 어떻게 만들어지나요?**

리눅스에서는 둘을 완전히 별개의 존재로 취급하기보다는 모두 **task**라는 관점으로 관리합니다.

`fork()`나 `clone()` 계열 메커니즘을 사용하며, 메모리 공간이나 파일 테이블 등을 얼마나 공유하느냐에 따라 프로세스 또는 스레드처럼 동작합니다.

---

**Q. Zombie Process란?**

자식 프로세스가 종료되었지만 부모 프로세스가 아직 `wait()` 등을 호출하여 종료 정보를 회수하지 않은 상태입니다.

프로세스 자체는 종료됐지만 종료 코드 등의 정보가 프로세스 테이블에 남아 있습니다.

---

**Q. 부모가 자식보다 먼저 종료되면?**

자식 프로세스는 고아 프로세스가 됩니다.

리눅스에서는 이러한 프로세스를 PID 1 프로세스나 적절한 subreaper가 넘겨받아 관리하게 됩니다.

---

**Q. Daemon Process란?**

사용자와 직접 상호작용하지 않고 백그라운드에서 지속적으로 동작하는 프로세스입니다.

예:

* 웹 서버
* 로그 서비스
* 스케줄러

---

**Q. Linux 프로세스 트리의 루트는?**

일반적인 사용자 공간 프로세스 트리에서는 **PID 1 프로세스**가 중심적인 역할을 합니다.

현대 리눅스 배포판에서는 주로 `systemd`가 PID 1로 실행됩니다.

---

# 4. 프로세스 주소공간에 대해 설명해 주세요.

### 내 답변

프로세스는 자신만의 **가상 주소 공간**을 가지고 있습니다.

대표적으로 다음 영역으로 나누어집니다.

```text
높은 주소
┌───────────────┐
│     Stack     │
│       ↓       │
│               │
│       ↑       │
│     Heap      │
├───────────────┤
│ Data / BSS    │
├───────────────┤
│     Code      │
└───────────────┘
낮은 주소
```

**Code(Text)** 영역에는 실행할 명령어가 저장됩니다.

**Data** 영역에는 초기화된 전역 변수와 static 변수가 저장됩니다.

**BSS**에는 초기화하지 않았거나 0으로 초기화되는 전역/static 변수가 저장됩니다.

**Heap**은 `new`, `malloc` 같은 동적 메모리 할당에 사용됩니다.

**Stack**에는 함수 호출 정보, 지역 변수, 매개변수 등이 저장됩니다.

### 핵심 키워드

`Virtual Address Space` / `Code` / `Data` / `BSS` / `Heap` / `Stack`

### 꼬리 질문

**Q. 초기화하지 않은 전역/static 변수는?**

BSS 영역에 저장됩니다.

---

**Q. Stack과 Heap은 처음부터 엄청 크게 잡히나요?**

아닙니다.

가상 주소 공간에는 사용할 수 있는 범위가 존재하지만 실제 물리 메모리가 처음부터 전부 할당되는 것은 아닙니다.

필요에 따라 페이지가 할당되거나 영역이 확장됩니다.

---

**Q. Stack과 Heap 중 접근 속도가 더 빠른 곳은?**

메모리 자체의 접근 속도가 본질적으로 완전히 다른 것은 아닙니다.

다만 Stack은 Stack Pointer를 이동하는 식으로 메모리 할당과 해제가 매우 단순하기 때문에 **메모리 관리 비용이 Heap보다 작습니다.**

Heap은 동적 할당자를 통해 빈 공간 탐색과 관리 등이 필요하기 때문에 할당/해제 비용이 상대적으로 큽니다.

---

**Q. 왜 주소공간을 나누나요?**

각 데이터의 성격이 다르기 때문입니다.

예를 들어 Code 영역은 일반적으로 실행 중 변경할 필요가 없고, Stack은 함수 호출에 따라 빠르게 생성·제거됩니다.

영역을 분리하면

* 메모리 관리
* 접근 권한 관리
* 자원 공유
* 보안

등을 효과적으로 처리할 수 있습니다.

---

**Q. 스레드 주소공간은?**

같은 프로세스의 스레드들은

* Code
* Data
* BSS
* Heap

을 공유합니다.

하지만 각 스레드는 자신만의

* Stack
* CPU Register
* Program Counter

를 가집니다.

---

**Q. Stack/Heap 영역은 자료구조 Stack/Heap과 관련 있나요?**

Stack 영역은 자료구조 Stack처럼 함수 호출이 LIFO 방식으로 관리된다는 점에서 직접적인 관련이 있습니다.

```text
main()
  ↓
funcA()
  ↓
funcB()
```

호출 순서와 반대로

```text
funcB 종료
funcA 종료
main
```

가 됩니다.

반면 **Heap 메모리 영역은 자료구조의 Heap과 직접적인 관련이 없습니다.**

동적 메모리 영역을 관습적으로 Heap이라고 부르는 것입니다.

---

**Q. Shared Memory는 주소공간 어디에 있나요?**

운영체제가 공유 메모리 객체를 각 프로세스의 **가상 주소 공간에 매핑**합니다.

보통 `mmap` 계열의 매핑 영역 등에 연결됩니다.

서로 다른 프로세스의 가상 주소가 같은 물리 메모리를 가리키도록 만들어 빠르게 데이터를 공유할 수 있습니다.

---

**Q. Stack과 Heap 크기는 수정할 수 있나요?**

Stack은 운영체제의 프로세스 제한 등에 의해 최대 크기가 설정될 수 있습니다.

Linux에서는 예를 들어 `ulimit` 등을 이용하여 스택 크기 제한을 조절할 수 있습니다.

Heap은 프로그램이 필요에 따라 확장되며 시스템의 가상 메모리, 물리 메모리와 각종 자원 제한에 영향을 받습니다.

---

# 5. 단기, 중기, 장기 스케줄러에 대해 설명해 주세요.

### 내 답변

운영체제 스케줄러는 프로세스를 언제 시스템에 받아들이고, 메모리에 둘지, CPU에 실행시킬지를 결정합니다.

**장기 스케줄러**는 새로운 작업 중 어떤 작업을 시스템에 받아들일지를 결정합니다.

**중기 스케줄러**는 메모리가 부족할 때 프로세스를 메모리에서 잠시 제거하거나 다시 가져오는 역할을 담당합니다.

**단기 스케줄러**는 Ready Queue에 있는 프로세스 중 다음에 CPU를 사용할 프로세스를 선택합니다.

단기 스케줄러는 CPU 스케줄러라고도 하며 매우 자주 실행되기 때문에 빠르게 동작해야 합니다.

### 핵심 키워드

`Long-term` / `Medium-term` / `Short-term` / `Ready Queue` / `CPU Scheduler`

### 정리

| 스케줄러 | 역할                     |
| ---- | ---------------------- |
| 장기   | 어떤 프로세스를 시스템에 받아들일지 결정 |
| 중기   | 메모리에서 프로세스를 제거/복귀      |
| 단기   | 다음 CPU 실행 프로세스 선택      |

### 꼬리 질문

**Q. 현대 OS가 세 가지를 모두 사용하나요?**

고전적인 설명처럼 명확한 세 모듈이 모두 존재한다고 보기는 어렵습니다.

일반적인 현대 OS에서는 **단기 CPU 스케줄링은 반드시 존재**합니다.

중기 스케줄링과 유사한 역할은 가상 메모리, 스와핑, 메모리 회수 등이 담당하며, 장기 스케줄러의 역할은 일반 PC OS에서는 과거 배치 시스템만큼 명확하지 않습니다.

---

**Q. 프로세스 상태를 설명해주세요.**

대표적인 5가지 상태는

```text
New
 ↓
Ready
 ↓
Running
 ↓
Waiting
 ↓
Ready

Running → Terminated
```

입니다.

* New: 생성 중
* Ready: CPU를 기다림
* Running: CPU 실행 중
* Waiting/Blocked: I/O 등의 이벤트 기다림
* Terminated: 종료

---

**Q. Preemptive와 Non-preemptive 차이는?**

Preemptive는 운영체제가 실행 중인 프로세스의 CPU를 강제로 빼앗을 수 있습니다.

예:

```text
Running → Ready
```

가 타임 슬라이스 만료 등으로 강제로 발생할 수 있습니다.

Non-preemptive에서는 프로세스가 종료되거나 I/O 요청 등으로 스스로 CPU를 반납할 때까지 계속 실행합니다.

---

**Q. Memory가 부족하면 프로세스는 어떤 상태가 되나요?**

고전적인 모델에서는 중기 스케줄러가 프로세스를 메모리에서 제거하여 **Suspended 상태**로 만들 수 있습니다.

현대 OS에서는 페이지 단위 스와핑과 메모리 회수를 적극적으로 사용하기 때문에 항상 프로세스 전체가 Suspended 상태가 되는 것은 아닙니다.

극심한 메모리 부족 상황에서는 OS가 프로세스를 강제 종료하는 경우도 있습니다.

---

# 6. 컨텍스트 스위칭 시에는 어떤 일들이 일어나나요?

### 내 답변

컨텍스트 스위칭은 **CPU가 실행하는 프로세스 또는 스레드를 다른 것으로 변경하는 과정**입니다.

현재 실행 중인 프로세스의

* Program Counter
* Stack Pointer
* CPU Register

등의 실행 상태를 PCB나 커널 내부 자료구조에 저장합니다.

이후 스케줄러가 다음 프로세스를 선택하고 해당 프로세스의 저장된 CPU 상태를 복원하여 실행을 이어갑니다.

프로세스가 변경되는 경우 주소 공간까지 바뀔 수 있기 때문에 페이지 테이블 관련 작업이나 TLB 영향 등이 발생할 수 있으며, 이런 작업 자체는 실제 프로그램의 일을 처리하지 않기 때문에 오버헤드입니다.

### 핵심 키워드

`Register 저장` / `PCB` / `Scheduler` / `Context Restore` / `Overhead`

### 꼬리 질문

**Q. 프로세스와 스레드 Context Switching 차이는?**

다른 프로세스로 전환할 경우 주소 공간 자체가 달라질 수 있기 때문에 메모리 관리 정보도 변경해야 합니다.

같은 프로세스의 두 스레드 사이에서는 Code, Data, Heap 등을 공유하므로 주소 공간을 바꿀 필요가 없습니다.

따라서 일반적으로 같은 프로세스 내부 스레드 간 전환이 프로세스 간 전환보다 비용이 작습니다.

---

**Q. 기존 프로세스 정보는 커널 스택에 어떻게 저장되나요?**

인터럽트나 시스템 콜 진입 시 CPU 레지스터 등의 일부 상태가 **trap/interrupt frame과 같은 형태**로 커널 스택에 저장될 수 있습니다.

운영체제는 추가적인 스케줄링 상태를 PCB 또는 이에 해당하는 커널 자료구조에도 보관합니다.

---

**Q. Context Switch는 언제 발생하나요?**

대표적으로

* Time Quantum 만료
* I/O 요청
* 인터럽트
* 시스템 콜 이후 스케줄링
* 높은 우선순위 작업 등장
* 프로세스 종료

등이 있습니다.

---

# 7. 프로세스 스케줄링 알고리즘에는 어떤 것들이 있나요?

### 내 답변

대표적인 CPU 스케줄링 알고리즘에는

* FCFS
* SJF
* SRTF
* Priority Scheduling
* Round Robin
* Multi-Level Queue
* Multi-Level Feedback Queue

등이 있습니다.

FCFS는 먼저 들어온 프로세스를 먼저 실행합니다.

SJF는 실행 시간이 가장 짧은 프로세스를 먼저 선택합니다.

SRTF는 SJF의 선점형 방식입니다.

Priority Scheduling은 우선순위가 높은 프로세스를 실행합니다.

Round Robin은 각 프로세스에 Time Quantum을 부여하여 돌아가면서 CPU를 사용하게 합니다.

MLFQ는 여러 우선순위 Queue를 두고 프로세스의 CPU 사용 패턴에 따라 우선순위를 동적으로 변경합니다.

### 핵심 키워드

`FCFS` / `SJF` / `SRTF` / `Priority` / `RR` / `MLFQ`

### 꼬리 질문

**Q. Round Robin에서 Time Slice의 trade-off는?**

Time Slice가 너무 작으면 응답성은 좋아지지만 Context Switching이 지나치게 자주 발생합니다.

```text
Time Slice ↓
→ 응답성 ↑
→ Context Switching ↑
→ 오버헤드 ↑
```

반대로 너무 크면 Context Switching은 감소하지만 사실상 FCFS에 가까워져 응답성이 떨어집니다.

---

**Q. 하나의 CPU에서 반드시 계속 실행 기회를 받아야 하는 프로세스가 있다면?**

해당 프로세스가 정말 높은 중요도를 갖는다면 **선점형 Priority Scheduling**을 사용할 수 있습니다.

다만 높은 우선순위 프로세스가 CPU를 독점하면 다른 프로세스가 Starvation에 빠질 수 있으므로 우선순위 조정이나 Aging 등의 정책이 필요합니다.

공정한 CPU 분배가 목적이라면 Round Robin이 더 적합할 수 있습니다.

---

**Q. 동시성과 병렬성의 차이는?**

동시성은 여러 작업이 **겹치는 시간 동안 진행되는 것처럼 보이는 것**입니다.

싱글코어에서도 Context Switching을 통해 가능합니다.

```text
CPU
A → B → A → B
```

병렬성은 여러 작업이 실제로 **동시에 실행**되는 것입니다.

```text
Core 1 → A
Core 2 → B
```

따라서 여러 CPU 코어가 필요합니다.

---

**Q. MLFQ는 어떤 문제를 해결하나요?**

SJF는 성능이 좋지만 실제 실행 시간을 미리 알기 어렵습니다.

Priority Scheduling은 낮은 우선순위 프로세스의 Starvation 문제가 있습니다.

MLFQ는 프로세스의 실제 CPU 사용 행동을 관찰하여 우선순위를 동적으로 조정합니다.

짧은 interactive 작업에는 빠른 응답을 주고 CPU를 오래 사용하는 작업은 낮은 Queue로 이동시킬 수 있습니다.

---

**Q. FIFO도 쓸모가 있나요?**

구현이 매우 간단하고 Context Switching이 적습니다.

실행 시간이 비슷하거나 작업 순서가 중요하고 예측 가능한 배치 작업에서는 충분히 사용할 수 있습니다.

단점은 긴 작업 하나 때문에 뒤의 짧은 작업들이 오래 기다리는 **Convoy Effect**가 발생할 수 있다는 것입니다.

---

**Q. 스레드는 별도로 스케줄링하나요?**

현대 운영체제에서는 실제 CPU 실행 단위가 **커널이 인식하는 스레드**인 경우가 많습니다.

따라서 흔히 프로세스 스케줄링이라고 부르지만 실제로는 runnable thread/task를 CPU에 배치합니다.

---

**Q. User Thread와 Kernel Thread는 스케줄링이 같나요?**

항상 그렇지는 않습니다.

Kernel Thread는 운영체제가 직접 인식하기 때문에 커널 스케줄러가 직접 스케줄링합니다.

순수한 User-Level Thread는 커널이 각각의 스레드를 알지 못할 수 있기 때문에 사용자 수준 Thread Library가 자체적으로 어떤 스레드를 실행할지 결정할 수 있습니다.

---

# 8. 프로그램이 컴파일되어 실행되는 과정을 설명해 주세요.

### 내 답변

C 같은 전통적인 컴파일 언어를 기준으로 보면

```text
소스 코드
  ↓
전처리
  ↓
컴파일
  ↓
어셈블
  ↓
Object File
  ↓
링킹
  ↓
Executable
  ↓
로딩
  ↓
실행
```

과정을 거칩니다.

컴파일러가 소스 코드를 저수준 코드로 변환하고 어셈블러가 Object File을 만듭니다.

링커는 여러 Object File과 라이브러리를 연결해 실행 파일을 만듭니다.

프로그램을 실행하면 운영체제가 실행 파일의 코드와 데이터 등을 프로세스의 가상 주소 공간에 매핑하고 필요한 초기화를 수행합니다.

이후 CPU가 프로그램의 시작 지점부터 명령을 실행합니다.

### 핵심 키워드

`Compile` / `Object File` / `Linker` / `Executable` / `Loader`

### 꼬리 질문

**Q. 링커와 로더 차이는?**

링커는 **실행 파일을 만드는 역할**입니다.

```text
Object Files
+
Libraries
↓
Linker
↓
Executable
```

로더는 **실행 파일을 메모리에 적재하여 실행 가능하게 만드는 역할**입니다.

---

**Q. 컴파일 언어와 인터프리터 언어 차이는?**

컴파일 방식은 프로그램 전체 또는 큰 단위를 실행 전에 기계가 실행할 수 있는 형태로 변환합니다.

인터프리터 방식은 실행 시 코드를 해석하면서 실행합니다.

다만 현대 언어들은 둘을 혼합하는 경우가 많기 때문에 완전히 두 종류로 나누기는 어렵습니다.

---

**Q. JIT란?**

Just-In-Time Compilation의 약자입니다.

프로그램 실행 중 자주 사용되는 코드를 실제 머신 코드로 컴파일하여 실행 속도를 높이는 기술입니다.

```text
Bytecode
   ↓
Interpreter
   ↓
자주 실행되는 코드 발견
   ↓
JIT Compile
   ↓
Machine Code
```

---

**Q. Java 프로그램은 어떻게 실행되나요?**

Java의 경우

```text
Hello.java
   ↓ javac
Hello.class
   ↓
Bytecode
   ↓
JVM
   ↓
Interpreter / JIT
   ↓
Machine Code 실행
```

과정을 거칩니다.

먼저 `javac`가 Java 코드를 JVM Bytecode로 컴파일합니다.

JVM은 `.class` 파일을 로드하고 검증한 뒤 Bytecode를 실행합니다.

실행 빈도가 높은 코드는 JIT Compiler를 통해 네이티브 머신 코드로 변환하여 성능을 높일 수 있습니다.

---

**Q. CPython / Jython / PyPy 차이는?**

**CPython**

Python의 대표 구현체입니다.

Python 코드를 Bytecode로 변환하고 CPython 가상 머신에서 실행합니다.

**Jython**

Python 언어를 JVM 환경에서 실행하기 위한 구현체입니다.

Java 라이브러리와 연동하기 쉽습니다.

**PyPy**

JIT Compiler를 적극적으로 사용하여 반복 실행되는 Python 코드의 성능을 높이는 데 초점을 둔 구현체입니다.

따라서 같은 Python 코드라도 구현체에 따라 실제 실행 과정이 다를 수 있습니다.

---

**Q. fork(), exec()와 Loader는 어떤 관계인가요?**

`fork()`는 현재 프로세스를 기반으로 새로운 프로세스를 생성합니다.

```text
Parent
  ↓ fork
Parent + Child
```

`exec()`는 현재 프로세스의 주소 공간을 새로운 프로그램 이미지로 교체합니다.

```text
Child
 ↓ exec
새 프로그램
```

이때 `exec()`를 처리하는 과정에서 운영체제는 실행 파일을 해석하고 필요한 코드와 데이터를 프로세스 주소 공간에 매핑합니다.

동적 라이브러리를 사용하는 실행 파일이라면 이후 dynamic linker/loader가 필요한 라이브러리 로딩과 심볼 연결 등을 수행하기도 합니다.

---

# Part 2. 개발상식

# 9. 32비트와 64비트의 차이는 무엇인가요?

### 내 답변

32비트와 64비트의 가장 핵심적인 차이는 CPU 아키텍처와 주소 표현 능력입니다.

32비트 시스템에서는 주소를 표현하는 데 기본적으로 32비트를 사용하기 때문에 이론적으로

```text
2^32 bytes
= 4,294,967,296 bytes
≈ 4 GiB
```

의 주소 공간을 표현할 수 있습니다.

64비트 시스템은 훨씬 큰 주소 공간을 표현할 수 있습니다.

또한 CPU 레지스터 크기, 포인터 크기, 명령어 집합과 ABI 등에서도 차이가 발생합니다.

단, 64비트 CPU라고 해서 실제로 2^64개의 모든 주소를 구현하고 사용하는 것은 아니며 실제 지원하는 주소 비트 수는 CPU와 OS에 따라 더 작습니다.

### 핵심 키워드

`Address Width` / `Pointer` / `2^32` / `4GiB` / `Register`

### 꼬리 질문

**Q. 왜 32비트에서 최대 4GB인가요?**

32개의 비트로 표현할 수 있는 경우의 수는

```text
2^32
```

개입니다.

일반적인 byte-addressable 시스템에서는 주소 하나가 1Byte를 가리키므로

```text
2^32 × 1Byte
= 4,294,967,296 Bytes
= 4GiB
```

가 됩니다.

다만 이 4GiB 전체를 하나의 사용자 프로그램이 사용할 수 있다는 의미는 아닙니다.

운영체제와 커널 영역 등에 의해 실제 프로세스가 사용할 수 있는 범위는 더 작을 수 있습니다.

---

# Part 3. 손코딩

# 워밍업 — 배열 기반 Stack

```java
class ArrayStack {
    private int[] data;
    private int top;

    public ArrayStack() {
        data = new int[4];
        top = 0;
    }

    public void push(int value) {
        if (top == data.length) {
            resize();
        }

        data[top++] = value;
    }

    public int pop() {
        if (top == 0) {
            throw new RuntimeException("Stack is empty");
        }

        return data[--top];
    }

    public int peek() {
        if (top == 0) {
            throw new RuntimeException("Stack is empty");
        }

        return data[top - 1];
    }

    public boolean isEmpty() {
        return top == 0;
    }

    private void resize() {
        int[] newData = new int[data.length * 2];

        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }

        data = newData;
    }
}
```

### 핵심

여기서 `top`을

```text
다음 값이 들어갈 위치
```

라고 생각하면 구현이 쉽습니다.

```text
data = [10][20][30][ ]
                    ↑
                   top
```

따라서

```java
data[top++] = value;
```

로 push하고

```java
return data[--top];
```

으로 pop할 수 있습니다.

### 시간복잡도

| 연산     |     복잡도 |
| ------ | ------: |
| push   | 평균 O(1) |
| pop    |    O(1) |
| peek   |    O(1) |
| resize |    O(N) |

동적 배열을 두 배씩 확장하면 `push()`의 **amortized 시간복잡도는 O(1)**입니다.

### 공간복잡도

```text
O(N)
```

---

# 연결 리스트 기반 Stack

```java
class LinkedStack {

    private static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node top;

    public void push(int value) {
        Node node = new Node(value);

        node.next = top;
        top = node;
    }

    public int pop() {
        if (top == null) {
            throw new RuntimeException("Stack is empty");
        }

        int value = top.value;
        top = top.next;

        return value;
    }

    public int peek() {
        if (top == null) {
            throw new RuntimeException("Stack is empty");
        }

        return top.value;
    }

    public boolean isEmpty() {
        return top == null;
    }
}
```

### 동작

```text
push(10)

top
 ↓
10 → null
```

```text
push(20)

top
 ↓
20 → 10 → null
```

```text
push(30)

top
 ↓
30 → 20 → 10 → null
```

`pop()`을 하면

```text
20 → 10 → null
↑
top
```

이 됩니다.

---

# 배열 Stack vs LinkedList Stack

| 항목       | 배열          | 연결 리스트      |
| -------- | ----------- | ----------- |
| push     | 평균 O(1)     | O(1)        |
| pop      | O(1)        | O(1)        |
| 메모리      | 연속 공간       | 노드마다 포인터 필요 |
| 크기       | 부족하면 resize | 필요할 때 노드 생성 |
| Cache 효율 | 좋음          | 상대적으로 낮음    |
| 구현       | 단순          | 노드 관리 필요    |

면접에서는 이렇게 정리하면 됩니다.

> 배열 기반 Stack은 메모리가 연속적이어서 캐시 효율이 좋지만 크기가 부족하면 배열을 확장해야 합니다. 연결 리스트 기반 Stack은 동적으로 노드를 추가할 수 있지만 각 노드가 포인터를 가지고 있어 추가적인 메모리가 필요하고 캐시 지역성이 떨어집니다.

---

# 응용 문항

## 배열 1,000,000개의 수에서 원하는 수의 모든 인덱스 찾기

원하는 값이 여러 번 존재할 수 있으므로 배열 전체를 한 번 탐색하면 됩니다.

```java
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static List<Integer> findIndexes(int[] arr, int target) {

        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {

            if (arr[i] == target) {
                result.add(i);
            }
        }

        return result;
    }

    public static void main(String[] args) {

        int[] arr = {
            1, 5, 3, 5, 7, 5
        };

        List<Integer> result = findIndexes(arr, 5);

        System.out.println(result);
    }
}
```

출력

```text
[1, 3, 5]
```

### 시간복잡도

배열 전체를 확인해야 하므로

```text
O(N)
```

N = 1,000,000이어도 한 번만 순회합니다.

### 공간복잡도

찾은 인덱스 개수를 K라고 하면

```text
O(K)
```

입니다.

모든 값이 target이라면 최악의 경우

```text
O(N)
```

입니다.

---

## 추가 면접 포인트

만약 문제에서

> 같은 배열에 대해 검색을 수천 번 해야 한다.

라고 조건이 변경되면 이야기가 달라집니다.

한 번 검색할 때마다 O(N)을 사용하는 것보다 미리

```java
Map<Integer, List<Integer>>
```

형태로

```text
숫자 → 해당 숫자가 존재하는 모든 index
```

를 저장할 수 있습니다.

예를 들어

```text
배열
[3, 5, 2, 5, 3]

Map
3 → [0, 4]
5 → [1, 3]
2 → [2]
```

처럼 전처리할 수 있습니다.

전처리는 O(N)이지만 이후 동일 값 검색을 여러 번 수행할 때 효율적입니다.

---

# 이번 주 핵심 흐름

이번 주는 각각의 개념을 따로 외우는 것보다 다음 흐름을 연결해서 설명할 수 있어야 합니다.

```text
프로그램
   ↓
실행
   ↓
프로세스 생성
   ↓
가상 주소 공간 생성
   ↓
Ready Queue
   ↓
CPU Scheduler
   ↓
Running
   ↓
시스템 콜 / 인터럽트 / Time Slice
   ↓
Kernel Mode
   ↓
Scheduler 판단
   ↓
Context Switch
   ↓
다른 프로세스 Running
```

특히 면접에서는 다음 연결을 말할 수 있으면 좋습니다.

```text
User Mode
   ↓ System Call / Interrupt
Kernel Mode
   ↓
OS가 요청 처리
   ↓
필요하면 Scheduler 실행
   ↓
Context Switch
   ↓
다른 Process 또는 Thread 실행
```

---

# 면접 직전 암기용 9문장

1. **시스템 콜** → 사용자 프로그램이 커널 기능을 요청하는 인터페이스.
2. **인터럽트** → CPU에게 특정 이벤트가 발생했음을 알리는 신호.
3. **프로세스** → 실행 중인 프로그램.
4. **주소 공간** → Code, Data/BSS, Heap, Stack 등으로 구성된 프로세스의 가상 메모리 공간.
5. **스케줄러** → 장기는 작업 수용, 중기는 메모리 조절, 단기는 CPU 할당.
6. **Context Switch** → 현재 실행 상태를 저장하고 다른 실행 상태를 복원하는 과정.
7. **CPU Scheduling** → FCFS, SJF, SRTF, Priority, RR, MLFQ 등이 존재.
8. **컴파일과 실행** → Compile → Assemble → Link → Load → Execute.
9. **32/64비트** → 주소 및 CPU 아키텍처의 비트 폭 차이이며 32비트 주소 공간은 이론적으로 2^32 Byte, 즉 4GiB.
