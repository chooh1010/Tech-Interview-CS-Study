# Week 03 — OS I — 커널과 프로세스

> **시스템 콜, 인터럽트, 프로세스와 스케줄링**
> 진행일: `2026-09-08` (화) · 질문 9개 · 손코딩 워밍업 1 + 응용 1문항

**이번 주 목표** — 유저 모드와 커널 모드의 경계를 설명하고, 프로세스가 생성되어 CPU를 할당받기까지의 흐름을 끊김 없이 말할 수 있다.

## 범위 한눈에 보기

|  #  | 질문                                   | 난이도 |  구분 |
| :-: | ------------------------------------ | :-: | :-: |
|  1  | 시스템 콜이 무엇인지 설명해 주세요.                 | ★★☆ |  필수 |
|  2  | 인터럽트가 무엇인지 설명해 주세요.                  | ★★☆ |  필수 |
|  3  | 프로세스가 무엇인가요?                         | ★☆☆ |  필수 |
|  4  | 프로세스 주소공간에 대해 설명해 주세요.               | ★★☆ |  필수 |
|  5  | 단기, 중기, 장기 스케쥴러에 대해 설명해 주세요.         | ★★☆ |  필수 |
|  6  | 컨텍스트 스위칭 시에는 어떤 일들이 일어나나요?           | ★★☆ |  필수 |
|  7  | 프로세스 스케줄링 알고리즘에는 어떤 것들이 있나요?         | ★★☆ |  필수 |
|  8  | 프로그램이 컴파일 되어, 실행되는 과정을 간략하게 설명해 주세요. | ★★☆ |  필수 |
|  9  | 32비트와 64비트의 차이는 무엇인가요?               | ★★☆ |  심화 |

> 시간이 부족하면 `심화` 표시 질문을 다음 주로 미루고, `필수`부터 소화하세요.

## 모의 면접 진행 체크리스트

### 스터디 전 (각자)

* [ ] 이번 주 범위 질문을 전부 읽고, **내 답변** 칸을 채웠다
* [ ] 남에게 설명하듯 소리 내어 1회 말해봤다 (읽는 것과 말하는 것은 다르다)
* [ ] 답이 막힌 질문을 **막힌 부분** 칸에 적어뒀다
* [ ] 워밍업 구현과 응용 문항을 직접 작성해봤다 (IDE 자동완성 없이)

### 당일 진행 (2시간 기준)

| 시간          | 내용                       |
| ----------- | ------------------------ |
| 0:00 ~ 0:10 | 지난주 미해결 질문 공유, 오늘 순서 정하기 |
| 0:10 ~ 1:20 | **모의 면접** — 출제자 로테이션     |
| 1:20 ~ 1:30 | 워밍업 구현 비교 (자료구조·정렬)      |
| 1:30 ~ 1:40 | 손코딩 응용 풀이 비교             |
| 1:40 ~ 2:00 | 회고, 다음 주 범위 확인           |

### 모의 면접 규칙

* 출제자는 대질문을 던지고, 답변이 끝나면 **꼬리 질문을 최소 2개** 던진다
* 답변자는 한 질문당 **3분 이내**로 답한다. 모르면 "모릅니다"라고 명확히 말하고 넘어간다
* 답변 도중 다른 사람은 끼어들지 않는다. 보충·정정은 답변이 끝난 뒤에
* 한 문제가 끝나면 출제자와 답변자를 시계 방향으로 넘긴다
* 꼬리 질문은 목록에 없는 것도 자유롭게 던진다. **"왜 그런가요?"를 두 번 이상** 파고들 것
* 아무도 답하지 못한 질문은 아래 **미해결 질문**에 적고 다음 주에 다시 다룬다

---

## Part 1. 운영체제

#### 1. 시스템 콜이 무엇인지 설명해 주세요.

`★★☆` · `필수` · 원본 [운영체제 #1](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 6개 (펼치기)</b></summary>

* 우리가 사용하는 시스템 콜의 예시를 들어주세요.
* 시스템 콜이, 운영체제에서 어떤 과정으로 실행되는지 설명해 주세요.
* 시스템 콜의 유형에 대해 설명해 주세요.
* 운영체제의 Dual Mode 에 대해 설명해 주세요.
* 왜 유저모드와 커널모드를 구분해야 하나요?
* 서로 다른 시스템 콜을 어떻게 구분할 수 있을까요?

</details>

**내 답변**

```text
시스템 콜은 유저 모드에서 실행되는 프로그램이 운영체제 커널의 기능을 요청하기 위한 인터페이스입니다.

일반 애플리케이션이 파일이나 네트워크 장치 같은 자원에 마음대로 직접 접근할 수 있도록 하면 다른 프로세스나 운영체제 자체를 손상시킬 수 있습니다. 그래서 CPU는 일반 프로그램이 실행되는 유저 모드와 높은 권한을 가진 커널 모드를 구분하고, 권한이 필요한 작업은 시스템 콜을 통해서만 커널에 요청하도록 합니다.

예를 들어 Linux에서 파일을 읽는 read, 파일을 여는 open 계열, 프로세스를 생성하는 fork, 새로운 프로그램을 실행하는 execve, 네트워크 통신에 사용하는 socket 등이 있습니다.

시스템 콜이 실행되면 시스템 콜 번호와 인자를 전달하고 CPU가 커널 모드로 전환됩니다. 커널은 번호에 맞는 작업을 수행한 뒤 결과를 반환하고 다시 유저 모드로 돌아갑니다.

이때 유저 모드에서 커널 모드로 바뀌는 것은 mode switch이고, 반드시 다른 프로세스로 CPU가 넘어가는 context switch가 발생하는 것은 아닙니다.
```

**상세 설명**

시스템 콜(System Call)은 쉽게 말하면 **애플리케이션과 운영체제 커널 사이의 공식적인 출입구**입니다.

일반 애플리케이션은 CPU의 모든 명령을 실행할 수 없습니다.

개념적으로 CPU는 다음과 같이 권한을 나눕니다.

```text
User Mode
    ↓ 시스템 콜
Kernel Mode
    ↓
CPU / Memory / Disk / Network Device 등
```

예를 들어 애플리케이션에서

```java
Files.readString(path);
```

을 호출했다고 해서 Java 코드가 SSD 컨트롤러를 직접 조작하는 것은 아닙니다.

대략적으로는

```text
Java API
→ JVM / Native Library
→ OS API
→ System Call
→ Kernel
→ File System / Device Driver
→ Storage
```

와 같은 계층을 거칩니다.

### 왜 User Mode와 Kernel Mode를 나누는가?

가장 중요한 이유는 **보호와 격리**입니다.

만약 모든 프로그램이 커널과 같은 권한을 가진다면 버그가 있는 프로그램 하나가

* 다른 프로그램의 메모리를 수정하거나
* 디스크의 아무 영역이나 덮어쓰거나
* CPU 제어 기능을 임의로 변경하거나
* 시스템 전체를 중단

시킬 수 있습니다.

따라서 위험한 명령을 **privileged instruction(특권 명령)** 으로 분류하고 커널만 실행할 수 있도록 제한합니다.

### 시스템 콜 실행 흐름

아키텍처와 OS마다 세부 구현은 다르지만 개념적으로는 다음과 같습니다.

```text
사용자 프로그램
    ↓
라이브러리 함수 / Runtime
    ↓
System Call 번호 + 인자 준비
    ↓
syscall 등의 특별한 CPU 명령 실행
    ↓
User Mode → Kernel Mode
    ↓
커널의 System Call Handler
    ↓
번호에 해당하는 커널 함수 실행
    ↓
결과 / 오류 코드 설정
    ↓
Kernel Mode → User Mode
    ↓
사용자 프로그램 계속 실행
```

Linux에서는 서로 다른 시스템 콜을 **system call number**로 식별합니다.

즉 개념적으로

```text
번호 A → read
번호 B → write
번호 C → open...
```

처럼 커널이 어떤 작업을 요청받았는지 판단합니다.

실제 번호는 CPU 아키텍처에 따라서도 달라질 수 있습니다.

### 시스템 콜의 대표 유형

운영체제 교재에서는 보통 다음과 같이 분류합니다.

| 종류                      | 역할        | 예                        |
| ----------------------- | --------- | ------------------------ |
| Process Control         | 프로세스 관리   | fork, execve, exit, wait |
| File Management         | 파일 관리     | open, read, write, close |
| Device Management       | 장치 접근     | ioctl 등                  |
| Information Maintenance | 시스템 정보    | getpid, time 계열          |
| Communication           | 프로세스 간 통신 | pipe, socket             |
| Protection              | 권한 관리     | chmod, setuid 계열         |

### 주의: 시스템 콜 = 소프트웨어 인터럽트?

항상 그렇다고 말하면 부정확합니다.

과거 x86 Linux에서는 `int 0x80` 같은 소프트웨어 인터럽트 명령을 시스템 콜 진입에 사용하기도 했습니다.

현대 x86-64에서는 일반적으로 `syscall` 같은 전용 명령을 사용합니다.

따라서 면접에서는

> "시스템 콜을 실행하면 CPU가 정해진 메커니즘을 통해 커널 모드로 진입합니다."

정도로 표현하는 것이 안전합니다.

### Mode Switch와 Context Switch

둘은 반드시 구분해야 합니다.

```text
Mode Switch
User Mode ↔ Kernel Mode
```

실행 중인 프로세스는 그대로일 수 있습니다.

반면

```text
Context Switch
Process A / Thread A
        ↓
Process B / Thread B
```

는 CPU가 실행하는 작업 자체가 변경되는 것입니다.

시스템 콜을 호출했더라도 바로 처리하고 같은 프로세스로 돌아간다면 **mode switch만 발생하고 task context switch는 발생하지 않을 수 있습니다.**

**꼬리 질문 대비**

* **Q. 시스템 콜의 예시는?**
  Linux 기준 `read`, `write`, `openat`, `fork`, `execve`, `socket`, `mmap` 등을 예로 들 수 있습니다.

* **Q. 서로 다른 시스템 콜은 어떻게 구분하나요?**
  시스템 콜 번호를 이용합니다. 호출자는 번호와 인자를 레지스터 등에 넣고 커널에 진입하며, 커널은 해당 번호를 이용해 요청된 기능을 구분합니다.

* **Q. Dual Mode란?**
  일반 프로그램이 실행되는 User Mode와 운영체제가 높은 권한으로 실행되는 Kernel Mode를 분리하는 구조입니다.

* **Q. 왜 분리하나요?**
  프로세스 격리, 메모리 보호, 장치 보호, 시스템 안정성 및 보안을 확보하기 위해서입니다.

**현업 / 실제 사례**

Spring Boot 서버에서 클라이언트 요청을 처리할 때도 결국 네트워크 패킷의 송수신에는 커널이 개입합니다.

```text
Spring
→ Java Socket API
→ JVM Native Layer
→ OS Network API
→ System Call
→ Kernel TCP/IP Stack
→ NIC
```

따라서 애플리케이션 개발자도 시스템 콜 비용이나 I/O 방식에 대한 이해가 있으면 blocking I/O, non-blocking I/O, epoll 같은 서버 기술을 이해하기 쉬워집니다.

**핵심 키워드** → User Mode · Kernel Mode · Privilege · System Call Interface · syscall number · read/write · fork/execve · Mode Switch · Protection

**막힌 부분 / 다시 볼 것** → 시스템 콜과 일반 함수 호출의 차이 · Mode Switch와 Context Switch 구분 · 시스템 콜과 소프트웨어 인터럽트를 동일시하지 않기

---

#### 2. 인터럽트가 무엇인지 설명해 주세요.

`★★☆` · `필수` · 원본 [운영체제 #2](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 4개 (펼치기)</b></summary>

* 인터럽트는 어떻게 처리하나요?
* Polling 방식에 대해 설명해 주세요.
* HW / SW 인터럽트에 대해 설명해 주세요.
* 동시에 두 개 이상의 인터럽트가 발생하면, 어떻게 처리해야 하나요?

</details>

**내 답변**

```text
인터럽트는 CPU가 현재 실행 중인 작업을 잠시 중단하고 우선 처리해야 하는 사건을 처리하도록 알려주는 메커니즘입니다.

예를 들어 네트워크 패킷이 도착하거나 디스크 I/O가 끝나거나 타이머가 만료되면 하드웨어가 CPU에 인터럽트를 발생시킬 수 있습니다.

인터럽트가 발생하면 CPU는 현재 실행 상태의 필요한 부분을 저장하고, 인터럽트 벡터를 이용해 해당 인터럽트를 처리할 핸들러를 찾아 실행합니다. 처리가 끝나면 저장했던 상태를 복원하고 기존 실행으로 돌아갑니다.

Polling처럼 CPU가 장치 상태를 계속 확인하지 않아도 장치가 필요한 순간에 CPU에게 알려줄 수 있다는 것이 장점입니다.

다만 엄밀하게는 하드웨어 인터럽트와 CPU 내부에서 동기적으로 발생하는 exception, trap 등의 용어를 구분할 필요가 있습니다.
```

**상세 설명**

인터럽트의 핵심은

> **CPU에게 "지금 처리해야 할 사건이 발생했다"고 알리는 것**

입니다.

CPU가 프로그램을 실행하는 동안 외부 장치에서는 여러 사건이 발생합니다.

예를 들어

```text
키보드 입력
네트워크 패킷 도착
디스크 작업 완료
타이머 만료
```

등입니다.

CPU가 매번 모든 장치에

> "일 끝났어?"
> "패킷 왔어?"
> "키 입력됐어?"

라고 물어본다면 매우 비효율적입니다.

그래서 장치가 필요한 순간 CPU에 알려주는 방식이 인터럽트입니다.

### 일반적인 처리 과정

```text
CPU가 프로그램 실행
        ↓
Interrupt 발생
        ↓
현재 실행 상태 일부 저장
        ↓
Interrupt Vector 등을 통해 Handler 확인
        ↓
Interrupt Service Routine(ISR) 실행
        ↓
필요한 처리 수행
        ↓
저장한 상태 복원
        ↓
기존 코드 실행 재개
```

여기서 **ISR(Interrupt Service Routine)** 은 인터럽트 처리 루틴입니다.

### Interrupt Vector

인터럽트마다 처리해야 하는 코드가 다릅니다.

예를 들어

```text
Timer Interrupt → Timer Handler
Network Interrupt → Network Handler
Disk Interrupt → Disk Handler
```

가 필요합니다.

인터럽트 종류와 해당 처리 루틴을 연결하는 데 **Interrupt Vector / Interrupt Vector Table**과 같은 구조가 사용됩니다.

세부 구조와 명칭은 CPU 아키텍처에 따라 달라집니다.

### Polling

Polling은 CPU가 장치의 상태를 반복적으로 확인하는 방식입니다.

```text
while (true) {
    장치 상태 확인
    if (작업 완료)
        처리
}
```

장점은 구조가 단순하고, 매우 짧은 간격으로 항상 상태를 확인해야 하는 특정 상황에서는 유용할 수 있다는 것입니다.

하지만 아무 사건이 없어도 계속 CPU 시간을 사용하게 됩니다.

비교하면 다음과 같습니다.

| 방식        | 특징                  |
| --------- | ------------------- |
| Polling   | CPU가 계속 상태를 확인      |
| Interrupt | 장치가 사건 발생 시 CPU에 알림 |

다만 실제 고성능 시스템에서는 **인터럽트와 polling을 혼합**하기도 합니다.

예를 들어 매우 많은 네트워크 패킷이 발생할 때 매 패킷마다 인터럽트를 발생시키면 인터럽트 처리 비용이 커질 수 있기 때문입니다.

### Hardware Interrupt와 Software 쪽 이벤트

엄밀하게는 용어를 구분하는 것이 좋습니다.

**Hardware Interrupt**

CPU 외부 장치에서 비동기적으로 발생합니다.

```text
Network
Disk
Keyboard
Timer
```

등이 대표적입니다.

반면 CPU가 명령을 수행하다 발생하는 사건은 보통 **Exception**이라는 표현을 사용합니다.

예:

```text
0으로 나누기
Page Fault
잘못된 명령어
```

`trap`, `fault`, `exception`, `software interrupt` 등의 용어는 CPU 아키텍처와 문헌에 따라 분류 방식이 조금 다릅니다.

따라서 면접에서는

> "넓은 의미에서는 CPU의 정상 실행 흐름을 바꾸는 사건들을 함께 설명하기도 하지만, 엄밀히는 외부 하드웨어 인터럽트와 동기적인 exception을 구분합니다."

라고 답하면 좋습니다.

### 동시에 여러 인터럽트가 발생한다면?

일반적으로 다음 메커니즘을 조합합니다.

* Interrupt Priority
* Interrupt Masking
* Pending 상태
* Nested Interrupt

우선순위가 높은 인터럽트를 먼저 처리할 수 있으며, 특정 인터럽트를 처리하는 동안 낮은 우선순위 인터럽트를 일시적으로 막을 수도 있습니다.

허용되는 시스템이라면 ISR 실행 중 더 높은 우선순위 인터럽트가 들어와 **nested interrupt**가 발생할 수도 있습니다.

### Timer Interrupt와 스케줄링

운영체제에서 특히 중요한 것이 timer interrupt입니다.

```text
Process A 실행
        ↓
Timer Interrupt
        ↓
Kernel 진입
        ↓
Scheduler 판단
        ↓
Process A 계속 실행 또는 Process B 실행
```

선점형 운영체제가 하나의 프로세스에게 CPU를 무한정 빼앗기지 않는 중요한 기반 중 하나입니다.

**꼬리 질문 대비**

* **Q. Polling이 무조건 안 좋은가요?**
  아닙니다. 이벤트 빈도가 매우 높거나 polling 비용이 충분히 작은 상황에서는 오히려 효과적일 수 있습니다. 실제 시스템에서는 interrupt와 polling을 혼합하기도 합니다.

* **Q. 인터럽트가 발생하면 무조건 context switch가 일어나나요?**
  아닙니다. 커널의 인터럽트 핸들러를 실행한 뒤 기존 task로 돌아갈 수도 있습니다.

* **Q. 두 인터럽트가 동시에 발생하면?**
  인터럽트 컨트롤러와 OS가 우선순위, masking, pending 상태 등을 이용해 처리 순서를 결정합니다.

**현업 / 실제 사례**

웹 서버로 패킷이 들어오는 과정에도 NIC(Network Interface Card)와 커널의 네트워크 처리 과정이 관여합니다.

트래픽이 극단적으로 많아지면 인터럽트 횟수 자체가 부담이 될 수 있기 때문에 Linux 네트워크 스택에서는 단순히 "패킷 하나 = 인터럽트 하나" 형태로만 처리하지 않고 interrupt와 polling을 적절히 조합하는 메커니즘을 사용합니다.

**핵심 키워드** → Interrupt · ISR · Interrupt Vector · Hardware Interrupt · Exception · Polling · Priority · Masking · Timer Interrupt · I/O

**막힌 부분 / 다시 볼 것** → Interrupt와 Exception 용어 구분 · Polling과 Interrupt trade-off · Interrupt 발생과 Context Switch를 동일시하지 않기

---

#### 3. 프로세스가 무엇인가요?

`★☆☆` · `필수` · 원본 [운영체제 #3](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 7개 (펼치기)</b></summary>

* 프로그램과 프로세스, 스레드의 차이에 대해 설명해 주세요.
* PCB가 무엇인가요?
* 그렇다면, 스레드는 PCB를 갖고 있을까요?
* 리눅스에서, 프로세스와 스레드는 각각 어떻게 생성될까요?
* 자식 프로세스가 상태를 알리지 않고 죽거나, 부모 프로세스가 먼저 죽게 되면 어떻게 처리하나요?
* 리눅스에서, 데몬프로세스에 대해 설명해 주세요.
* 리눅스는 프로세스가 일종의 트리를 형성하고 있습니다. 이 트리의 루트 노드에 위치하는 프로세스에 대해 설명해 주세요.

</details>

**내 답변**

```text
프로세스는 실행 중인 프로그램이라고 설명할 수 있지만, 더 정확히는 프로그램 코드뿐 아니라 현재 실행 상태와 메모리 주소 공간, 열린 파일 같은 자원을 포함하는 실행 단위입니다.

디스크에 존재하는 실행 파일 자체는 프로그램이고, 그것이 메모리에 적재되어 CPU 스케줄링의 대상이 되면 프로세스가 됩니다.

운영체제는 각 프로세스를 관리하기 위해 PID, 실행 상태, 레지스터 정보, 스케줄링 정보, 메모리 정보 등을 관리합니다. 교과서에서는 이러한 정보를 PCB, 즉 Process Control Block이라고 부릅니다.

프로세스 안에는 하나 이상의 스레드가 존재할 수 있고, 같은 프로세스의 스레드들은 코드와 힙 같은 주소 공간과 파일 등의 자원을 공유하지만 각자 PC, 레지스터, 스택과 같은 실행 상태를 가집니다.

Linux에서는 프로세스와 스레드를 완전히 별개의 개념으로 구현하기보다는 모두 schedulable task로 다루며, task_struct 같은 커널 자료구조를 통해 관리합니다.
```

**상세 설명**

### Program vs Process vs Thread

세 개를 구분하는 것이 핵심입니다.

```text
Program
= 실행 가능한 코드가 저장된 정적인 파일

Process
= 프로그램을 실행한 동적인 실행 인스턴스

Thread
= Process 내부의 실제 실행 흐름
```

예를 들어 `java -jar server.jar`을 실행하기 전 `server.jar`는 프로그램입니다.

실행하면 JVM을 포함한 하나의 프로세스가 만들어지고, 그 안에는

```text
main thread
GC thread
worker thread
...
```

등 여러 스레드가 존재할 수 있습니다.

### Process가 가지는 것

프로세스는 일반적으로 다음 정보를 가집니다.

* PID
* Virtual Address Space
* Code
* Data
* Heap
* Stack
* 열린 파일
* 권한 정보
* 프로세스 상태
* Scheduling 정보
* CPU register 상태

### PCB

PCB(Process Control Block)는 운영체제가 프로세스를 관리하기 위해 유지하는 정보의 집합을 설명하는 **교과서적 개념**입니다.

일반적으로 다음 정보가 포함됩니다.

```text
Process ID
Process State
Program Counter
CPU Registers
Scheduling Information
Memory Management Information
Open File Information
Accounting / Security Information
```

### 그러면 Thread도 PCB가 있는가?

면접에서 단순히

> "없습니다."

라고 하는 것은 지나치게 단순화한 답입니다.

스레드도 독립적으로 스케줄링되려면 최소한

* Program Counter
* Stack Pointer
* Registers
* Scheduling State

같은 실행 context가 필요합니다.

교재에서는 이를 TCB(Thread Control Block)라고 분리해서 설명하기도 합니다.

하지만 실제 OS 구현은 이러한 교과서적 명칭과 반드시 1:1로 대응하지 않습니다.

Linux에서는 프로세스와 스레드 모두 **task**로 추상화하여 각각 `task_struct`와 같은 커널 구조로 관리합니다.

### Linux의 fork()

`fork()`는 현재 프로세스를 기반으로 **새로운 자식 프로세스를 생성**합니다.

개념적으로

```text
Parent Process
      ↓ fork()
Parent + Child
```

가 됩니다.

과거 설명처럼 전체 메모리를 즉시 통째로 복사한다고 이해하면 부정확합니다.

현대 Linux에서는 일반적으로 **Copy-on-Write(COW)** 를 활용합니다.

처음에는 부모와 자식이 물리 페이지를 공유하고, 한쪽이 페이지를 수정하려 할 때 실제 복사를 수행합니다.

### execve()

`execve()`는 새 프로세스를 추가로 만드는 함수가 아닙니다.

**현재 프로세스의 프로그램 이미지를 새로운 실행 프로그램으로 교체**합니다.

```text
현재 Process
    ↓ execve()
PID를 유지하면서
새 프로그램 이미지 실행
```

따라서 shell에서 명령을 실행할 때 전통적인 Unix 모델은 개념적으로

```text
Shell
 ↓ fork
Child
 ↓ exec
새 프로그램
```

형태로 이해할 수 있습니다.

Linux `execve()`는 현재 프로세스 이미지를 새로운 프로그램으로 대체합니다.

### clone()

Linux에서는 `clone()` 계열 메커니즘을 통해 어떤 자원을 공유할지 세밀하게 결정할 수 있습니다.

예를 들어

```text
주소 공간 공유
파일 descriptor table 공유
signal handler 공유
```

등의 여부에 따라 프로세스와 유사하거나 스레드와 유사한 실행 단위를 만들 수 있습니다.

이 때문에 Linux에서는 프로세스와 스레드를 본질적으로 완전히 다른 객체라고 보기보다는 **자원 공유 정도가 다른 task**라는 관점이 중요합니다.

### Zombie Process

자식 프로세스가 종료되면 부모에게 종료 상태를 전달해야 합니다.

자식이 종료됐지만 부모가 아직 `wait()` 계열 호출로 종료 상태를 회수하지 않았다면 일부 프로세스 정보가 남아 있는 **Zombie** 상태가 됩니다.

```text
Child 종료
↓
종료 코드 보관
↓
Parent가 아직 wait 안 함
↓
Zombie
```

부모가 `wait()`를 호출하면 남은 정보가 정리됩니다.

### Orphan Process

반대로 부모 프로세스가 자식보다 먼저 종료될 수도 있습니다.

이러한 자식은 orphan이라고 부릅니다.

Unix/Linux 계열에서는 orphan이 적절한 reaper 프로세스에 re-parenting되어 이후 종료 상태가 회수될 수 있도록 처리됩니다.

전통적으로 PID 1의 init 프로세스를 설명하지만, Linux에는 subreaper 메커니즘도 있으므로 항상 "무조건 systemd에게 간다"라고 외우는 것은 좋지 않습니다.

### PID 1

일반적인 현대 Linux 시스템에서는 PID 1 역할을 `systemd`가 맡는 경우가 많습니다.

전통적으로는 `init`이라고 설명합니다.

주요 역할은 시스템 초기화와 서비스 관리 및 프로세스 관리입니다.

단, Docker 같은 PID namespace 내부에서는 컨테이너의 첫 프로세스가 해당 namespace의 PID 1이 될 수 있으므로

> "Linux의 PID 1은 항상 systemd다."

라고 단정하면 안 됩니다.

### Daemon Process

Daemon은 사용자와 직접 상호작용하지 않고 백그라운드에서 지속적으로 서비스를 제공하는 프로세스입니다.

예:

```text
sshd
web server
database server
logging daemon
```

등입니다.

**현업 / 실제 사례**

Spring Boot 애플리케이션 하나를 실행하면 OS 관점에서는 JVM 프로세스가 만들어집니다.

JVM 프로세스 안에는 여러 Java Thread가 있으며 이 스레드들은 JVM Heap을 공유하면서 각자 Java Stack을 사용합니다.

또 Docker를 사용해도 프로세스가 없어지는 것이 아닙니다.

Container는 VM처럼 별도의 OS 커널을 하나 더 실행하는 개념이 아니라 Linux의 namespace와 cgroup 등을 이용하여 **프로세스를 격리하는 구조**에 가깝습니다.

**꼬리 질문 대비**

* **fork와 exec 차이**
  `fork`는 새로운 프로세스를 만들고, `exec`는 현재 프로세스의 프로그램 이미지를 교체합니다.

* **프로세스와 스레드의 가장 중요한 차이**
  일반적으로 프로세스들은 독립된 주소 공간을 가지지만 같은 프로세스의 스레드들은 주소 공간과 여러 자원을 공유합니다.

* **Zombie와 Orphan 차이**
  Zombie는 종료됐지만 부모가 종료 상태를 아직 회수하지 않은 프로세스이고, Orphan은 부모가 먼저 종료된 살아 있는 자식 프로세스입니다.

**핵심 키워드** → Program · Process · Thread · PCB · task_struct · fork · execve · clone · Zombie · PID 1

**막힌 부분 / 다시 볼 것** → PCB와 Linux task_struct 관계 · fork와 exec 차이 · Zombie와 Orphan 구분 · Linux 프로세스와 스레드 관계

---

#### 4. 프로세스 주소공간에 대해 설명해 주세요.

`★★☆` · `필수` · 원본 [운영체제 #4](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 8개 (펼치기)</b></summary>

* 초기화 하지 않은 변수들은 어디에 저장될까요?
* 일반적인 주소공간 그림처럼, Stack과 Heap의 크기는 매우 크다고 할 수 있을까요? 그렇지 않다면, 그 크기는 언제 결정될까요?
* Stack과 Heap 공간에 대해, 접근 속도가 더 빠른 공간은 어디일까요?
* 다음과 같이 공간을 분할하는 이유가 있을까요?
* 스레드의 주소공간은 어떻게 구성되어 있을까요?
* "스택"영역과 "힙"영역은 정말 자료구조의 스택/힙과 연관이 있는 걸까요? 만약 그렇다면, 각 주소공간의 동작과정과 연계해서 설명해 주세요.
* IPC의 Shared Memory 기법은 프로세스 주소공간의 어디에 들어가나요? 그런 이유가 있을까요?
* 스택과 힙영역의 크기는 언제 결정되나요? 프로그램 개발자가 아닌, 사용자가 이 공간의 크기를 수정할 수 있나요?

</details>

**내 답변**

```text
프로세스 주소 공간은 프로세스가 사용하는 가상 메모리 공간입니다.

일반적으로 Code 또는 Text 영역, Data 영역, BSS 영역, Heap, Stack, 그리고 shared library나 mmap 영역 등으로 구성됩니다.

Code 영역에는 실행 명령어가, Data에는 초기화된 전역·정적 변수가, BSS에는 초기화되지 않았거나 0으로 초기화되는 전역·정적 변수가 저장됩니다. Heap은 동적 메모리 할당에 사용되고 Stack은 함수 호출 정보와 지역 변수, 반환 주소 등의 실행 정보를 관리하는 데 사용됩니다.

프로세스마다 독립적인 가상 주소 공간을 제공함으로써 다른 프로세스의 메모리에 임의로 접근하지 못하게 하고 메모리를 효과적으로 관리할 수 있습니다.

같은 프로세스의 스레드는 코드와 힙 같은 주소 공간을 공유하지만 각 스레드는 별도의 Stack과 실행 context를 가집니다.
```

**상세 설명**

우리가 흔히 보는 그림은 다음과 같습니다.

```text
높은 주소
┌────────────────────┐
│       Stack        │
│         ↓          │
├────────────────────┤
│ mmap / shared lib  │
├────────────────────┤
│         ↑          │
│        Heap        │
├────────────────────┤
│        BSS         │
├────────────────────┤
│        Data        │
├────────────────────┤
│     Text / Code    │
└────────────────────┘
낮은 주소
```

이 그림은 **개념적인 전형적 모델**입니다.

실제 주소 배치는 OS, 실행 파일 형식, ABI, ASLR 등의 영향을 받기 때문에 항상 정확히 이 순서로 고정된다고 생각하면 안 됩니다.

### Code / Text

실행할 기계어 명령 등이 위치합니다.

일반적으로 실행 가능하고 임의의 쓰기는 제한되는 형태로 매핑합니다.

### Data

초기값이 명시된 전역 변수 및 static 변수가 대표적입니다.

```c
int count = 10;
static int flag = 1;
```

### BSS

초기화되지 않았거나 0으로 초기화되는 전역/static 변수가 대표적입니다.

```c
int count;
static int flag;
```

실행 파일 안에 거대한 0 배열을 그대로 저장할 필요가 없기 때문에 BSS 형태로 크기 정보 등을 표현하고 로딩 과정에서 0으로 초기화된 메모리를 제공합니다.

### Heap

동적 메모리 할당을 위한 영역입니다.

C에서

```c
malloc()
```

등을 이용한 동적 메모리가 대표적입니다.

운영체제 수준에서는 `brk` 계열이나 `mmap` 같은 메커니즘이 메모리 allocator의 기반으로 활용될 수 있습니다.

하지만

> "Java 객체가 저장되는 JVM Heap = 운영체제 그림의 Heap 영역"

이라고 1:1로 동일시하면 안 됩니다.

JVM은 프로세스의 **가상 주소 공간 내에서 메모리를 확보한 뒤 JVM 자체의 Heap 관리 정책과 GC를 적용**합니다.

### Stack

함수 호출과 밀접하게 관련되어 있습니다.

일반적으로 stack frame에

* 지역 변수
* 반환 주소
* 저장된 레지스터
* 함수 호출 관련 정보

등이 저장될 수 있습니다.

함수가 호출되면 frame이 쌓이고 반환되면 제거되는 구조이기 때문에 자료구조 **Stack의 LIFO 특성과 실제 관련이 있습니다.**

반면 Heap 메모리 영역의 이름은 우리가 알고리즘에서 배우는 **Heap 자료구조(Binary Heap 등)와 직접적인 관련이 없습니다.**

### Stack이 무조건 Heap보다 빠른가?

이렇게 단정하면 부정확합니다.

일반적으로 Stack allocation은 stack pointer를 이동하는 정도의 단순한 연산으로 처리할 수 있기 때문에 **할당과 해제 자체가 매우 저렴**합니다.

반면 일반적인 Heap allocator는

* 적절한 free block 검색
* metadata 관리
* fragmentation 관리

등이 필요할 수 있습니다.

하지만 실제 메모리 **접근 속도 자체**는 cache hit 여부나 메모리 접근 패턴 등 많은 조건에 영향을 받습니다.

따라서

> "Stack 메모리라서 CPU가 물리적으로 항상 더 빠르게 읽는다."

라고 이해하면 안 됩니다.

### Thread와 주소 공간

같은 프로세스의 스레드는 일반적으로

**공유**

```text
Code
Data
BSS
Heap
Open Files
Memory Mapping
```

하고,

**개별적으로 보유**

```text
Stack
Program Counter
CPU Registers
Thread-local execution state
```

합니다.

그래서 스레드 간 통신은 빠르지만 공유 데이터에 동시에 접근할 경우 race condition이 발생할 수 있습니다.

### Shared Memory는 어디에 들어가는가?

Shared Memory는 각 프로세스의 **가상 주소 공간에 같은 물리 메모리를 매핑**하는 방식으로 이해하면 됩니다.

```text
Process A Virtual Address
        ↓
    Physical Page
        ↑
Process B Virtual Address
```

따라서 특정 프로세스의 Heap 안에 반드시 들어간다고 보는 것보다는 **memory mapping 영역으로 매핑된 공유 페이지**로 이해하는 것이 정확합니다.

### Stack과 Heap 크기는 언제 결정되는가?

둘 다 그림처럼 처음부터 전체 공간에 대한 물리 메모리가 할당되어 있는 것이 아닙니다.

가상 메모리 시스템에서는 필요에 따라 페이지가 실제 물리 메모리에 연결됩니다.

Stack에는 시스템 및 runtime이 정한 크기 제한이 있을 수 있습니다.

Linux에서는 사용자가 shell resource limit 등을 통해 stack 크기 제한을 조정할 수 있습니다.

Java에서도

```text
-Xss
```

등을 통해 Java thread stack 크기를 설정할 수 있습니다.

JVM Heap 역시

```text
-Xms
-Xmx
```

등으로 JVM 수준에서 크기를 조절할 수 있습니다.

다만 이는 OS 프로세스의 전통적인 C Heap 개념과 JVM Heap 설정을 구분해야 합니다.

### ASLR

ASLR(Address Space Layout Randomization)은 메모리 영역의 위치를 매 실행마다 달라지게 만들어 공격자가 특정 메모리 주소를 예측하기 어렵게 하는 보안 기술입니다.

그래서 교과서 그림에 나온 주소 배치를 **고정 주소**라고 생각하면 안 됩니다.

**현업 / 실제 사례**

Java 서버의 메모리를 볼 때

```text
JVM Heap
Thread Stack
Metaspace
Code Cache
Direct Buffer
Native Memory
Shared Library
```

등 다양한 영역을 구분해야 합니다.

"Java 프로세스가 메모리를 4GB 쓴다"라는 말이 반드시 "Java Heap이 4GB다"라는 의미가 아닌 이유도 여기에 있습니다.

**핵심 키워드** → Virtual Address Space · Text · Data · BSS · Heap · Stack · mmap · Shared Memory · Thread Stack · ASLR

**막힌 부분 / 다시 볼 것** → BSS와 Data 차이 · JVM Heap과 OS Heap 구분 · Stack이 빠른 이유를 단순 메모리 속도로 설명하지 않기 · Shared Memory의 mapping 개념

---

#### 5. 단기, 중기, 장기 스케쥴러에 대해 설명해 주세요.

`★★☆` · `필수` · 원본 [운영체제 #5](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 4개 (펼치기)</b></summary>

* 현대 OS에는 단기, 중기, 장기 스케쥴러를 모두 사용하고 있나요?
* 프로세스의 스케쥴링 상태에 대해 설명해 주세요.
* preemptive/non-preemptive 에서 존재할 수 없는 상태가 있을까요?
* Memory가 부족할 경우, Process는 어떠한 상태로 변화할까요?

</details>

**내 답변**

```text
교과서적으로 프로세스 스케줄러는 장기, 중기, 단기 스케줄러로 나눌 수 있습니다.

장기 스케줄러는 어떤 작업을 시스템에 받아들여 메모리와 실행 대상에 포함할지 결정해서 시스템의 다중 프로그래밍 정도를 조절합니다.

중기 스케줄러는 메모리가 부족하거나 시스템 부하를 조절할 필요가 있을 때 일부 프로세스를 메모리에서 일시적으로 제외하거나 다시 복귀시키는 역할을 하며, 전통적으로 swapping이나 suspended 상태와 연결해서 설명합니다.

단기 스케줄러는 Ready Queue의 실행 가능한 프로세스 또는 스레드 중 실제로 CPU를 할당할 대상을 선택합니다. 가장 자주 실행되기 때문에 CPU Scheduler라고도 합니다.

다만 이 세 가지는 운영체제를 이해하기 위한 고전적인 분류이고, 현대 Linux 같은 범용 운영체제에서 세 종류가 각각 독립된 모듈로 그대로 존재한다고 보면 부정확합니다.
```

**상세 설명**

### 장기 스케줄러 Long-Term Scheduler

```text
Job Pool
   ↓
Long-term Scheduler
   ↓
Ready Queue
```

어떤 job을 시스템의 실행 대상으로 받아들일지 결정합니다.

따라서 메모리에 존재하면서 실행을 기다리는 프로세스 수, 즉 **degree of multiprogramming**을 조절하는 역할로 설명됩니다.

전통적인 batch processing 시스템에서 특히 중요한 개념입니다.

### 단기 스케줄러 Short-Term Scheduler

실제 CPU를 누가 사용할지 결정합니다.

```text
Ready Queue
   ↓
Short-term Scheduler
   ↓
CPU
```

매우 자주 호출되기 때문에 빠르게 동작해야 합니다.

우리가 흔히 배우는

```text
FCFS
SJF
Round Robin
Priority
```

등은 주로 이 CPU scheduling 문제와 연결됩니다.

### 중기 스케줄러 Medium-Term Scheduler

메모리 부족이나 multiprogramming 정도를 조절하기 위해 일부 프로세스를 일시적으로 메모리에서 제외했다가 복귀시키는 역할로 설명됩니다.

전통적으로

```text
Ready
↓
Ready Suspended
```

또는

```text
Blocked
↓
Blocked Suspended
```

같은 상태와 연결됩니다.

### 프로세스 상태

기본적으로는 다음 다섯 상태가 많이 사용됩니다.

```text
New
 ↓
Ready
 ↓
Running
 ↓
Terminated
```

Running 상태에서 I/O를 기다리면

```text
Running
   ↓
Waiting / Blocked
   ↓ I/O 완료
Ready
```

가 됩니다.

선점형 시스템에서는

```text
Running
   ↓ Time Slice 만료 등
Ready
```

전이도 중요합니다.

### Preemptive와 Non-preemptive에서 없어지는 상태가 있는가?

일반적인

```text
New
Ready
Running
Waiting
Terminated
```

중 어떤 상태 자체가 사라지는 것은 아닙니다.

차이는 **상태 전이가 가능한 조건**에 있습니다.

선점형에서는 운영체제가 실행 중인 프로세스로부터 CPU를 강제로 회수하여

```text
Running → Ready
```

상태로 만들 수 있습니다.

비선점형에서는 일반적으로 프로세스가

* 종료하거나
* I/O로 Blocked 되거나
* 명시적으로 CPU를 양보

하는 시점까지 CPU를 유지합니다.

따라서 "non-preemptive에는 Ready 상태가 없다"와 같은 답은 틀립니다.

### 메모리가 부족하면 무조건 Suspended?

교과서 문제에서는 중기 스케줄링과 연결해

```text
Ready → Ready Suspended
Blocked → Blocked Suspended
```

같이 설명할 수 있습니다.

하지만 **현대 Linux가 메모리가 부족하다고 프로세스 전체를 반드시 고전적인 Suspended 상태로 바꾼다고 이해하면 안 됩니다.**

현대 시스템에서는

* Page Reclaim
* Page Cache 회수
* Swap
* Memory Compaction
* 필요 시 OOM 처리

등 여러 메커니즘이 작동할 수 있습니다.

즉 고전적인 scheduler 모델과 실제 현대 VM(Virtual Memory) 구현을 구분해야 합니다.

**현업 / 실제 사례**

서버에서 메모리가 부족하다고 Spring Boot 프로세스가 즉시 "중기 스케줄러에 의해 통째로 suspended"되는 것은 아닙니다.

운영체제는 페이지 단위 메모리 관리와 reclaim 등을 수행합니다.

따라서 Linux 서버 장애를 분석할 때 CPU scheduler뿐 아니라 virtual memory, swap, OOM Killer 등을 함께 봐야 합니다.

**핵심 키워드** → Long-term · Medium-term · Short-term · Ready Queue · CPU Scheduler · Multiprogramming · Suspended · Swapping · Process State · Preemption

**막힌 부분 / 다시 볼 것** → 고전적 3 Scheduler와 현대 OS 구현 구분 · 상태와 상태 전이 구분 · 메모리 부족이 반드시 Process 전체 swapping을 의미하지 않음

---

#### 6. 컨텍스트 스위칭 시에는 어떤 일들이 일어나나요?

`★★☆` · `필수` · 원본 [운영체제 #6](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 3개 (펼치기)</b></summary>

* 프로세스와 스레드는 컨텍스트 스위칭이 발생했을 때 어떤 차이가 있을까요?
* 컨텍스트 스위칭이 발생할 때, 기존의 프로세스 정보는 커널스택에 어떠한 형식으로 저장되나요?
* 컨텍스트 스위칭은 언제 일어날까요?

</details>

**내 답변**

```text
컨텍스트 스위칭은 CPU가 현재 실행 중인 프로세스나 스레드의 실행 상태를 저장하고 다른 실행 대상의 상태를 복원해서 CPU 실행 대상을 변경하는 과정입니다.

현재 task의 Program Counter, Stack Pointer, CPU Register 등의 상태를 저장하고 스케줄러가 다음 task를 선택하면 그 task의 저장된 상태를 복원해 실행을 이어갑니다.

프로세스 간 전환에서는 실행 상태뿐 아니라 서로 다른 주소 공간을 사용하기 때문에 page table과 관련된 메모리 관리 정보도 변경될 수 있습니다. 반면 같은 프로세스 안의 스레드 전환은 주소 공간을 공유하기 때문에 일반적으로 프로세스 전환보다 변경해야 할 메모리 context가 적습니다.

또 컨텍스트 스위칭 자체는 실제 비즈니스 로직을 처리하지 않는 overhead이고, cache나 TLB 효율에도 영향을 줄 수 있기 때문에 과도하게 발생하면 성능이 저하될 수 있습니다.
```

**상세 설명**

CPU는 프로세스 A를 실행하다 B를 실행하기로 결정했을 때 A가 나중에 **정확히 그 지점부터 이어서 실행**될 수 있도록 상태를 저장해야 합니다.

대표적인 context에는 다음이 포함됩니다.

```text
Program Counter
Stack Pointer
General Purpose Registers
CPU 상태 정보
Scheduling 관련 상태
```

### 기본 과정

```text
Process A 실행
       ↓
Interrupt / Blocking / Scheduler 호출
       ↓
Kernel 진입
       ↓
A의 실행 Context 저장
       ↓
Scheduler가 B 선택
       ↓
B의 Context 복원
       ↓
Process B 실행
```

### 언제 발생하는가?

대표적으로

* Time Slice 만료
* 실행 중인 프로세스가 I/O로 Block
* 더 높은 우선순위 task가 실행 가능 상태가 됨
* 명시적인 yield
* sleep / synchronization으로 대기

등이 있습니다.

### System Call = Context Switch?

아닙니다.

예를 들어

```text
Process A
↓
read() System Call
↓
Kernel Mode
↓
읽을 데이터가 이미 준비되어 있음
↓
Process A로 복귀
```

한다면 task는 계속 A입니다.

Mode Switch는 발생했지만 다른 task로의 Context Switch가 반드시 발생한 것은 아닙니다.

반대로 `read()`에서 데이터가 준비되지 않아 A가 Block된다면 scheduler가 B를 실행하면서 Context Switch가 발생할 수 있습니다.

### Process Context Switch

Process A와 Process B는 일반적으로 서로 다른 가상 주소 공간을 가집니다.

따라서 CPU 실행 상태뿐 아니라

```text
Memory Management Context
Page Table 관련 상태
```

도 변경될 수 있습니다.

이 때문에 TLB(Translation Lookaside Buffer)에 있는 주소 변환 cache에도 영향을 줄 수 있습니다.

현대 CPU에서는 PCID/ASID 같은 메커니즘으로 이러한 비용을 줄이기도 하므로

> "프로세스 switch가 발생하면 TLB가 무조건 전부 flush된다."

라고 단정해서는 안 됩니다.

### Thread Context Switch

같은 Process에 속한 Thread A와 Thread B라면

```text
Code / Heap / Address Space
```

을 공유합니다.

하지만 각각

```text
Program Counter
Registers
Stack
```

을 가지므로 이 실행 context는 변경되어야 합니다.

주소 공간 전체를 바꿀 필요가 없는 경우가 많기 때문에 일반적으로 같은 프로세스 내 스레드 전환이 프로세스 간 전환보다 가벼울 수 있습니다.

그렇다고 Thread Context Switch가 공짜인 것은 아닙니다.

### Kernel Stack에는 무엇이 저장되는가?

구체적인 형식은 CPU 아키텍처와 OS 구현에 따라 다릅니다.

Linux에서도 interrupt/system call 진입 시 register 상태 등이 architecture-specific한 **trap frame / `pt_regs`와 유사한 형태**로 kernel stack에 저장될 수 있습니다.

모든 프로세스 상태 전체가 단순히 "PCB 하나가 통째로 kernel stack에 복사된다"고 이해하면 안 됩니다.

지속적으로 관리할 scheduler/process 정보는 task 관리 구조체 등에 있고, 함수 호출 및 trap 진입에 필요한 상태 일부가 kernel stack에 놓입니다.

### 왜 Context Switching이 비용인가?

Switch 자체에는 사용자가 원하는 비즈니스 로직이 없습니다.

또한

* register save/restore
* scheduler 실행
* 주소 공간 관련 처리
* CPU cache locality 저하
* TLB 영향

등의 비용이 생길 수 있습니다.

**현업 / 실제 사례**

Java 서버에서 요청 하나마다 새로운 thread를 무제한으로 만들면 동시에 runnable한 thread가 너무 많아질 수 있습니다.

예:

```text
CPU Core = 8개
Runnable Thread = 5,000개
```

라고 해서 실제로 5,000개가 동시에 실행되는 것은 아닙니다.

CPU는 계속 thread를 교체해야 합니다.

이 때문에 서버에서는

* Thread Pool
* Async I/O
* Event Loop
* 적절한 worker 수

등을 설계합니다.

다만 thread가 많다고 언제나 나쁜 것은 아니고 I/O-bound인지 CPU-bound인지에 따라 적절한 concurrency 수준이 달라집니다.

**핵심 키워드** → Context Switch · Register · Program Counter · Stack Pointer · Scheduler · Address Space · TLB · Cache · Thread Switch · Blocking

**막힌 부분 / 다시 볼 것** → System Call과 Context Switch 차이 · Process와 Thread Switch 비용 차이 · kernel stack과 PCB/task_struct 역할 구분

---

#### 7. 프로세스 스케줄링 알고리즘에는 어떤 것들이 있나요?

`★★☆` · `필수` · 원본 [운영체제 #7](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 7개 (펼치기)</b></summary>

* RR을 사용할 때, Time Slice에 따른 trade-off를 설명해 주세요.
* 싱글 스레드 CPU 에서 상시로 돌아가야 하는 프로세스가 있다면, 어떤 스케쥴링 알고리즘을 사용하는 것이 좋을까요? 또 왜 그럴까요?
* 동시성과 병렬성의 차이에 대해 설명해 주세요.
* 타 스케쥴러와 비교하여, Multi-level Feedback Queue는 어떤 문제점들을 해결한다고 볼 수 있을까요?
* FIFO 스케쥴러는 정말 쓸모가 없는 친구일까요? 어떤 시나리오에 사용하면 좋을까요?
* 우리는 스케줄링 알고리즘을 "프로세스" 스케줄링 알고리즘이라고 부릅니다. 스레드는 다른 방식으로 스케줄링을 하나요?
* 유저 스레드와 커널 스레드의 스케쥴링 알고리즘은 똑같을까요?

</details>

**내 답변**

```text
대표적인 CPU 스케줄링 알고리즘으로 FCFS, SJF, SRTF, Priority Scheduling, Round Robin, Multilevel Queue, Multilevel Feedback Queue 등이 있습니다.

FCFS는 먼저 들어온 프로세스를 먼저 실행하는 가장 단순한 방식이고, SJF는 CPU burst가 짧은 작업을 먼저 실행해서 평균 대기 시간을 줄이는 방식입니다. SRTF는 SJF의 선점형 버전입니다.

Priority Scheduling은 우선순위가 높은 작업을 먼저 실행하고, Round Robin은 각 프로세스에 일정한 Time Quantum을 주면서 순환하기 때문에 interactive 환경에서 응답성을 확보하기 좋습니다.

MLFQ는 여러 우선순위 큐를 사용하면서 프로세스의 실제 실행 특성에 따라 우선순위를 동적으로 변경하여 짧은 interactive 작업의 응답성을 높이면서 긴 작업도 실행할 수 있도록 합니다.

다만 이러한 알고리즘은 운영체제 스케줄링 원리를 배우기 위한 모델이고, 현대 Linux가 이 중 하나만 그대로 사용하는 것은 아닙니다.
```

**상세 설명**

### 주요 알고리즘 비교

| 알고리즘             | 선점 여부  | 핵심              | 주요 문제                   |
| ---------------- | ------ | --------------- | ----------------------- |
| FCFS             | 비선점    | 먼저 온 작업부터       | Convoy Effect           |
| SJF              | 비선점    | 짧은 CPU Burst 우선 | Burst 예측 필요, Starvation |
| SRTF             | 선점     | 남은 시간이 가장 짧은 작업 | Starvation, 예측 문제       |
| Priority         | 둘 다 가능 | 높은 우선순위 우선      | Starvation              |
| Round Robin      | 선점     | 일정 Quantum만 실행  | Quantum 설정              |
| Multilevel Queue | 정책에 따라 | 작업 종류별 Queue    | Queue 간 고정 우선순위 문제      |
| MLFQ             | 선점 중심  | 행동에 따라 Queue 이동 | 정책 설계 복잡                |

### FCFS / FIFO

```text
A → B → C
```

도착 순서 그대로 실행합니다.

장점:

* 구현이 단순
* scheduling overhead가 작음
* starvation이 적음

단점:

긴 CPU-bound 작업이 앞에 있으면 짧은 작업들이 기다려야 합니다.

이를 **Convoy Effect**라고 합니다.

### FIFO는 정말 쓸모없는가?

아닙니다.

작업 시간이 비슷하고 순서 보장이 중요한 batch 처리에서는 단순한 FIFO queue가 매우 실용적입니다.

또한 Linux/POSIX real-time scheduling에는 `SCHED_FIFO`와 같은 정책도 존재합니다.

따라서

> "FCFS는 성능이 나쁘니까 현실에서는 쓸모없다."

라고 보는 것은 잘못입니다.

### SJF

Shortest Job First.

CPU Burst가 가장 짧을 것으로 예상되는 작업을 먼저 실행합니다.

이론적으로 특정 조건에서 평균 waiting time을 최소화하는 장점이 있습니다.

하지만 미래의 CPU burst 시간을 정확히 알 수 없다는 현실적인 문제가 있습니다.

긴 작업은 계속 뒤로 밀려 **Starvation**이 발생할 수 있습니다.

### SRTF

Shortest Remaining Time First.

SJF의 선점형 버전입니다.

새로운 짧은 작업이 들어오면 현재 작업을 중단시키고 더 짧은 작업을 실행할 수 있습니다.

### Priority Scheduling

우선순위가 높은 task를 먼저 실행합니다.

문제는 낮은 우선순위 task가 계속 실행되지 못하는 starvation입니다.

이를 완화하는 대표적인 개념이 **Aging**입니다.

오래 기다린 프로세스의 우선순위를 점진적으로 높여줍니다.

### Round Robin

Ready Queue를 원형으로 돌며 각 task에 **Time Quantum**을 제공합니다.

```text
A 10ms
B 10ms
C 10ms
A 10ms
...
```

#### Quantum이 너무 큰 경우

```text
Quantum → ∞
```

이면 사실상 FCFS에 가까워집니다.

응답성이 떨어질 수 있습니다.

#### Quantum이 너무 작은 경우

예를 들어 실제 작업보다 매우 작은 Quantum을 사용하면

```text
A
switch
B
switch
C
switch
A
...
```

가 반복되어 context switching overhead가 커집니다.

따라서 trade-off는

```text
큰 Quantum
→ Context Switch 감소
→ 응답성 저하 가능

작은 Quantum
→ 응답성 증가
→ Context Switch 증가
```

입니다.

### Multilevel Queue

프로세스를 종류에 따라 서로 다른 queue에 배치합니다.

예:

```text
Interactive Queue
Batch Queue
Background Queue
```

하지만 한번 정해진 queue에서 이동할 수 없는 정책이라면 실제 동작 특성의 변화에 적응하기 어렵습니다.

### MLFQ

Multi-Level Feedback Queue는 이 문제를 개선합니다.

핵심은 **Feedback**입니다.

```text
높은 Priority Queue
        ↓ CPU 오래 사용
중간 Priority Queue
        ↓ CPU 오래 사용
낮은 Priority Queue
```

I/O를 자주 하고 CPU를 짧게 사용하는 interactive task는 높은 우선순위에서 빠르게 처리할 수 있습니다.

반면 CPU를 지속적으로 사용하는 CPU-bound task는 낮은 queue로 이동합니다.

Periodic priority boost 등을 사용해 starvation을 줄일 수도 있습니다.

MLFQ는 미래 CPU Burst를 정확히 알지 못해도 **프로세스의 과거 행동을 이용해 SJF와 유사한 효과를 추구**한다는 점이 중요합니다.

### Linux에서는 무엇을 사용하나?

교과서에서 흔히 CFS(Completely Fair Scheduler)를 현대 Linux의 대표적인 fair scheduler로 배웠다면 최신 구현과는 구분할 필요가 있습니다.

Linux 커널은 6.6부터 기존 CFS 방식에서 **EEVDF(Earliest Eligible Virtual Deadline First)** 기반 fair scheduling으로 전환을 시작했습니다. EEVDF는 runnable task의 virtual runtime, lag, virtual deadline 등을 이용해 공정성과 응답성을 조절합니다.

따라서 2026년 기준 면접에서는

> "교과서에서는 RR, Priority, MLFQ 등을 배우지만 실제 Linux의 일반 task scheduling은 이러한 알고리즘 하나를 그대로 적용한 것이 아니며, 최신 Linux fair scheduling은 EEVDF 기반으로 발전하고 있습니다."

정도로 말하는 것이 정확합니다.

실시간 task에는 일반 fair scheduling과 다른 `SCHED_FIFO`, `SCHED_RR` 등의 정책도 존재합니다.

### 싱글 CPU에서 반드시 계속 돌아가야 하는 프로세스

"계속 실행되어야 한다"가 단순히 background service인지, **deadline을 반드시 만족해야 하는 real-time task인지** 구분해야 합니다.

real-time 요구사항이라면 우선순위를 보장하는 real-time scheduling이 필요할 수 있습니다.

하지만 높은 우선순위의 `SCHED_FIFO` task가 CPU를 양보하지 않으면 일반 task를 starvation시킬 수 있으므로 매우 신중하게 사용해야 합니다.

### 동시성과 병렬성

**Concurrency**

```text
CPU 1개

A → B → A → C → B ...
```

여러 작업이 진행되는 시간 구간이 겹치는 것처럼 보이지만 실제 특정 순간에는 하나만 실행될 수도 있습니다.

**Parallelism**

```text
Core 1 → A
Core 2 → B
Core 3 → C
```

실제로 같은 시점에 여러 작업을 실행합니다.

따라서

> 동시성은 여러 작업을 함께 진행시키는 구조에 관한 개념이고, 병렬성은 실제 동시에 여러 연산을 수행하는 개념입니다.

### 스레드는 어떻게 스케줄링되는가?

현대 OS에서는 실제 CPU를 받는 실행 단위가 thread인 경우가 일반적입니다.

Linux에서는 각각의 schedulable task를 scheduler가 다룹니다.

같은 process의 thread라고 해서 CPU scheduler가 반드시 process 단위로 한꺼번에 처리하는 것은 아닙니다.

### User Thread vs Kernel Thread

Kernel이 인식하는 thread는 OS scheduler가 직접 scheduling할 수 있습니다.

반면 순수 user-level thread는 runtime/library가 사용자 공간에서 자체적으로 scheduling할 수 있습니다.

예를 들어

```text
100개의 User Task
      ↓ runtime scheduler
4개의 Kernel Thread
      ↓ OS scheduler
4개의 CPU Core
```

같은 M:N 구조도 가능합니다.

따라서

> User thread scheduling과 kernel thread scheduling이 항상 동일하다.

라고 할 수 없습니다.

**핵심 키워드** → FCFS · SJF · SRTF · Priority · RR · Time Quantum · MLFQ · Starvation · Convoy Effect · EEVDF

**막힌 부분 / 다시 볼 것** → RR Quantum trade-off · SJF와 SRTF 차이 · MLFQ 목적 · 교과서 Scheduler와 최신 Linux Scheduler 구분 · Thread scheduling

---

#### 8. 프로그램이 컴파일 되어, 실행되는 과정을 간략하게 설명해 주세요.

`★★☆` · `필수` · 원본 [운영체제 #10](../02-OPERATING_SYSTEM.md)

<details>
<summary><b>꼬리 질문 6개 (펼치기)</b></summary>

* 링커와, 로더의 차이에 대해 설명해 주세요.
* 컴파일 언어와 인터프리터 언어의 차이에 대해 설명해 주세요.
* JIT에 대해 설명해 주세요.
* 본인이 사용하는 언어는, 어떤식으로 컴파일 및 실행되는지 설명해 주세요.
* Python 같은 언어는 CPython, Jython, PyPy등의 다양한 구현체가 있습니다. 각각은 어떤 차이가 있을까요? 또한, 실행되는 과정 또한 다를까요?
* 우리는 흔히 fork(), exec() 시스템 콜을 사용하여 프로세스를 적재할 수 있다고 배웠습니다. 로더의 역할은 이 시스템 콜과 상관있는 걸까요? 아니면 다른 방식으로 프로세스를 적재할 수 있는 건가요?

</details>

**내 답변**

```text
C나 C++ 같은 전통적인 컴파일 언어를 기준으로 보면 소스 코드는 전처리, 컴파일, 어셈블 과정을 거쳐 object file이 되고, 링커가 필요한 object file과 library를 연결해서 실행 파일을 만듭니다.

그 후 프로그램을 실행하면 운영체제의 로딩 과정에서 실행 파일의 코드와 데이터 등이 프로세스의 가상 주소 공간에 매핑되고 필요한 동적 라이브러리 등을 준비한 뒤 entry point부터 실행됩니다.

링커는 여러 코드와 심볼을 연결해서 실행 가능한 결과물을 만드는 역할이고, 로더는 만들어진 실행 파일을 실제 실행 가능한 프로세스 주소 공간에 배치하는 역할이라는 차이가 있습니다.

Java의 경우에는 javac가 소스 코드를 JVM bytecode인 class 파일로 컴파일하고, JVM의 Class Loader가 필요한 클래스를 로딩하고 linking과 initialization을 진행합니다. 이후 bytecode를 인터프리터가 실행하거나 HotSpot JVM에서는 자주 실행되는 코드를 JIT 컴파일해서 native machine code로 실행할 수 있습니다.
```

**상세 설명**

### C/C++의 전통적인 과정

```text
Source Code
    ↓
Preprocessing
    ↓
Compilation
    ↓
Assembly
    ↓
Object File
    ↓
Linking
    ↓
Executable
    ↓
Loading
    ↓
Process 실행
```

### 1. Preprocessing

C의

```c
#include
#define
#if
```

같은 preprocessor directive를 처리합니다.

### 2. Compilation

전처리된 고수준 코드를 분석하고 최적화하며 assembly 등의 저수준 표현으로 변환합니다.

실제 현대 compiler pipeline에서는 단계들이 내부적으로 통합되어 있을 수 있으므로 반드시 assembly text 파일이 물리적으로 생성돼야 하는 것은 아닙니다.

### 3. Assembly

Assembly를 object code로 변환합니다.

Object file에는 기계어뿐 아니라

* symbol 정보
* relocation 정보

등이 들어갈 수 있습니다.

### 4. Linking

여러 object file과 library를 연결합니다.

예를 들어

```text
main.o
util.o
library
```

가 있을 때 함수 참조와 주소 등을 해결해 실행 가능한 binary를 만듭니다.

### Static Linking

필요한 library 코드를 실행 파일에 포함시킵니다.

장점:

* 실행 환경 의존성 감소

단점:

* 실행 파일 크기 증가
* 여러 프로세스가 동일 library code를 공유하기 어려울 수 있음

### Dynamic Linking

library를 별도 shared library로 유지하고 실행 시 필요한 library를 연결합니다.

장점:

* binary 크기 감소
* library code 공유 가능

단점:

* runtime library 의존성
* 버전 관리 문제 가능

### Loader

프로그램 실행 시

```text
Executable
     ↓
Loader
     ↓
Virtual Address Space
```

형태로 실행 파일의 segment를 가상 주소 공간에 mapping하고 실행 환경을 구성합니다.

동적 실행 파일이라면 dynamic linker/loader가 shared library를 준비하는 과정도 관여합니다.

### Linker vs Loader

면접에서는 이렇게 정리하면 좋습니다.

```text
Linker
실행하기 전에 여러 object/library를 연결해 실행 파일을 만든다.

Loader
만들어진 실행 파일을 실행 시 메모리 주소 공간에 적재/매핑하고 실행 가능한 상태로 만든다.
```

---

### Java 실행 과정

Java는 단순히

> "컴파일 언어"

또는

> "인터프리터 언어"

하나로만 분류하기보다 **bytecode compilation + JVM execution + JIT** 구조로 이해하는 것이 좋습니다.

```text
Main.java
   ↓ javac
Main.class
   ↓
JVM 시작
   ↓
Class Loader
   ↓
Loading
   ↓
Linking
   ├─ Verification
   ├─ Preparation
   └─ Resolution
   ↓
Initialization
   ↓
Bytecode 실행
   ├─ Interpreter
   └─ JIT Compiler
         ↓
     Native Machine Code
         ↓
        CPU
```

JVM Specification에서도 class와 interface를 동적으로 **Loading → Linking → Initialization**하는 과정을 정의하고 있습니다.

#### Class Loading

필요한 `.class` binary를 찾아 JVM 내부에 Class 객체를 구성합니다.

#### Linking

대표적으로

```text
Verification
Preparation
Resolution
```

등의 과정이 있습니다.

#### Initialization

static field 초기화와 `<clinit>` 실행 등이 이루어집니다.

#### Interpreter

Bytecode instruction을 해석하며 실행합니다.

#### JIT

JIT(Just-In-Time Compiler)는 실행 중 반복적으로 많이 사용되는 코드를 native machine code로 컴파일해 이후 실행 속도를 향상시킵니다.

JIT 방식은 JVM 구현체에 따라 다를 수 있으며 JVM Specification이 특정 JIT 구현을 강제하는 것은 아닙니다.

HotSpot JVM에서는 JIT compilation이 매우 중요한 성능 최적화 요소입니다.

### OS Loader와 JVM Class Loader는 다르다

이름에 Loader가 모두 들어가지만 계층이 다릅니다.

**OS Loader**

```text
java 실행 파일
→ OS Process로 실행
```

**JVM Class Loader**

```text
.class
→ 실행 중인 JVM 내부로 Java Class 로딩
```

입니다.

---

### Python / CPython

Python 역시

> "소스 코드를 한 줄씩 바로 CPU가 실행한다."

라고 이해하면 부정확합니다.

CPython에서는 개념적으로

```text
.py Source
   ↓
Parsing / Compilation
   ↓
Python Bytecode
   ↓
CPython Virtual Machine
   ↓
실행
```

과정을 거칩니다.

bytecode가 `.pyc` 형태로 cache될 수도 있습니다.

### CPython

가장 널리 사용되는 Python 구현체이며 주로 C로 작성되어 있습니다.

Python bytecode를 CPython interpreter가 실행합니다.

### PyPy

Python 구현체 중 하나이며 JIT compilation을 활용하여 특정 workload에서 CPython보다 높은 실행 성능을 제공할 수 있습니다.

### Jython

Python 코드를 JVM 생태계에서 실행할 수 있도록 만든 구현체입니다.

따라서 Python이라는 같은 언어라도 **어떤 구현체를 사용하느냐에 따라 실행 과정과 runtime 환경이 달라질 수 있습니다.**

---

### Compiler vs Interpreter

전통적으로는

```text
Compiler
전체 프로그램을 실행 전에 machine code 등으로 변환

Interpreter
실행 과정에서 코드를 해석하며 실행
```

이라고 구분합니다.

하지만 현대 언어 runtime은 둘을 혼합하는 경우가 많습니다.

Java가 대표적입니다.

```text
Source
↓ compile
Bytecode
↓ interpret + JIT
Machine Code
```

그래서 실제 시스템에서는 "컴파일 언어냐 인터프리터 언어냐"라는 이분법만으로는 충분하지 않을 때가 많습니다.

### fork와 exec 그리고 Loader

`fork()`와 `execve()`를 구분해야 합니다.

```text
fork()
현재 프로세스를 기반으로 새로운 프로세스 생성
```

```text
execve()
현재 프로세스의 프로그램 이미지를 새로운 실행 파일로 교체
```

새 executable을 실행하는 핵심 과정은 `execve()` 쪽과 연결됩니다.

커널은 executable 형식을 확인하고 새로운 virtual address space를 구성하며 program image를 준비합니다.

따라서 loader는 `fork()`와 별개인 완전히 독립된 "사용자 프로그램 하나"라고만 이해하기보다 **exec를 통해 새로운 프로그램을 실행하는 과정에 필요한 OS loading 메커니즘**이라고 보는 것이 좋습니다.

`fork()` 자체는 새로운 프로그램을 load하지 않습니다.

**현업 / 실제 사례**

Java 애플리케이션을 처음 실행했을 때와 충분히 warm-up 된 뒤 성능이 다른 이유 중 하나가 JIT입니다.

그래서 Java 서버 benchmark를 할 때 단 한 번 실행한 latency만 측정하면 실제 steady-state 성능과 큰 차이가 날 수 있습니다.

**핵심 키워드** → Preprocessing · Compilation · Assembly · Object File · Linker · Loader · Bytecode · Class Loader · JIT · execve

**막힌 부분 / 다시 볼 것** → Linker와 Loader 차이 · OS Loader와 JVM Class Loader 구분 · Java를 단순 interpreted/compiled 중 하나로만 분류하지 않기 · fork와 exec 차이

---

## Part 2. 개발상식 코너

> 주제와 별개로 매주 곁들이는 공통 질문입니다. 면접에서 워밍업으로 자주 나옵니다.

#### 9. 32비트와 64비트의 차이는 무엇인가요?

`★★☆` · `심화` · 원본 [개발상식·기타 #12](../05-ETC.md)

<details>
<summary><b>꼬리 질문 1개 (펼치기)</b></summary>

* 32비트에서 가용한 메모리의 크기는 최대 4GB라고 하는데, 왜 그런걸까요?

</details>

**내 답변**

```text
32비트와 64비트의 차이는 CPU의 명령어 집합 구조와 레지스터, 주소 표현 방식 등 여러 부분에 있지만, 개발자가 체감하기 쉬운 차이 중 하나는 포인터와 가상 주소 공간의 크기입니다.

32비트 주소를 사용한다고 가정하면 서로 다른 주소를 2의 32승 개 표현할 수 있습니다. 바이트 단위 주소 체계라면 약 4GiB의 주소 공간을 표현할 수 있습니다.

반면 64비트 시스템에서는 이론적으로 2의 64승 개의 주소를 표현할 수 있어 훨씬 큰 주소 공간을 사용할 수 있습니다. 다만 실제 64비트 CPU와 운영체제가 64비트 전체 주소 범위를 모두 구현하거나 사용할 수 있는 것은 아닙니다.

또 64비트 환경에서는 포인터가 일반적으로 더 커지기 때문에 주소 공간은 넓어지지만 데이터 구조의 메모리 사용량이 증가할 수도 있습니다.
```

**상세 설명**

### 32bit에서 왜 4GB인가?

32개의 bit로 표현할 수 있는 경우의 수는

```text
2^32
= 4,294,967,296
```

입니다.

각 주소가 1Byte를 가리킨다고 가정하면

```text
2^32 Byte
= 4 GiB
```

의 주소 범위를 표현할 수 있습니다.

따라서 흔히

> "32bit process의 주소 공간은 4GB다."

라고 설명합니다.

정확히는 SI 단위 GB보다 **4GiB**라고 표현하는 것이 정확합니다.

### Virtual Address와 Physical Memory를 구분해야 한다

여기서 중요한 것은

```text
Virtual Address Space
≠
Physical RAM
```

이라는 것입니다.

프로세스가 사용하는 주소는 일반적으로 virtual address이고 MMU와 page table 등을 통해 physical address로 변환됩니다.

따라서

> 32bit CPU면 컴퓨터에 물리 RAM을 절대 4GiB 이상 장착할 수 없다.

라고 일반화하면 부정확합니다.

예를 들어 x86 계열에는 PAE(Physical Address Extension)를 이용해 32bit 환경에서도 4GiB보다 많은 **전체 물리 메모리**를 다룰 수 있었던 시스템이 있습니다.

다만 개별 32bit 프로세스의 virtual address space에는 여전히 상당한 제약이 있습니다.

### User / Kernel Address Split

32bit의 4GiB virtual address space 전체가 반드시 애플리케이션에 제공되는 것도 아닙니다.

운영체제 구성에 따라 일부 영역을 kernel address space에 예약하기도 합니다.

그래서 32bit 애플리케이션이 실제 사용자 공간에서 활용할 수 있는 주소 범위는 4GiB보다 작을 수 있습니다.

### 64bit라고 실제로 2^64 Byte를 쓰는가?

아닙니다.

이론적 표현 범위는

```text
2^64 Byte
```

이지만 현재 CPU가 모든 64개 address bit를 실제 address translation에 사용하는 것은 아닙니다.

CPU와 운영체제가 지원하는 virtual/physical address bit 수가 별도로 존재합니다.

따라서

> "64bit니까 실제 RAM을 16EB까지 바로 사용할 수 있다."

라고 설명하면 안 됩니다.

### Pointer 크기

일반적인 64bit native 환경에서는 pointer가 8Byte인 경우가 많고 32bit에서는 4Byte인 경우가 많습니다.

그래서

```c
struct Node {
    Node* next;
    ...
}
```

같이 pointer가 많이 포함된 자료구조는 64bit 환경에서 메모리 사용량이 증가할 수 있습니다.

alignment와 padding까지 영향을 줄 수 있습니다.

### 성능도 무조건 64bit가 두 배인가?

아닙니다.

64bit라고 CPU가 무조건 모든 연산을 32bit 시스템보다 두 배 빠르게 처리하는 것은 아닙니다.

성능은

* CPU architecture
* instruction set
* compiler
* cache
* memory bandwidth
* workload

등 다양한 요소에 영향을 받습니다.

### 호환성

64bit OS가 32bit 애플리케이션을 실행할 수 있는지는 OS와 compatibility layer 제공 여부에 따라 다릅니다.

반대로 일반적으로 32bit 실행 환경은 64bit binary를 그대로 실행할 수 없습니다.

**현업 / 실제 사례**

서버 애플리케이션에서 64bit 환경을 사용하는 중요한 이유 중 하나는 큰 가상 주소 공간입니다.

특히 큰 JVM Heap, database buffer, large memory mapping 등을 사용하는 시스템에서는 32bit 주소 공간이 심각한 제약이 될 수 있습니다.

반면 pointer가 커짐에 따라 객체 및 native data structure의 memory footprint가 증가할 수 있습니다.

**핵심 키워드** → 32bit · 64bit · Register · Pointer · Virtual Address · Physical Address · 2^32 · 4GiB · PAE · Address Space

**막힌 부분 / 다시 볼 것** → Virtual Address와 Physical RAM 구분 · 4GB가 나오는 계산 · 64bit가 2^64 전체 주소를 실제 사용한다고 생각하지 않기

---

## Part 3. 손코딩

### 워밍업 — 자료구조 · 정렬 구현

> 원본 [손코딩 #1 (각종 정렬) · #2 (각종 자료구조)](../06-ALGORITHM.md)를 12주로 나눈 조각입니다. 매주 1개씩, **10분 안에** IDE 없이 손으로 작성합니다.

#### [자료구조 2/6] Stack

**구현 범위** — 배열 기반 Stack(`push`/`pop`/`peek` + 동적 확장)과 연결 리스트 기반 Stack. 두 방식의 트레이드오프 정리

```java
// 1. 배열 기반 Stack
class ArrayStack {

    private int[] arr;
    private int size;

    public ArrayStack() {
        arr = new int[4];
        size = 0;
    }

    public void push(int value) {
        if (size == arr.length) {
            resize();
        }

        arr[size++] = value;
    }

    public int pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }

        return arr[--size];
    }

    public int peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }

        return arr[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void resize() {
        int[] newArr = new int[arr.length * 2];

        for (int i = 0; i < arr.length; i++) {
            newArr[i] = arr[i];
        }

        arr = newArr;
    }
}


// 2. 연결 리스트 기반 Stack
class LinkedStack {

    private static class Node {
        int value;
        Node next;

        Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node head;

    public void push(int value) {
        head = new Node(value, head);
    }

    public int pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }

        int value = head.value;
        head = head.next;

        return value;
    }

    public int peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }

        return head.value;
    }

    public boolean isEmpty() {
        return head == null;
    }
}
```

**시간복잡도** →

배열 기반:

```text
push → 평균(amortized) O(1)
resize가 발생한 push → O(n)
pop → O(1)
peek → O(1)
isEmpty → O(1)
```

연결 리스트 기반:

```text
push → O(1)
pop → O(1)
peek → O(1)
isEmpty → O(1)
```

**공간복잡도** →

```text
배열 Stack → O(n)
연결 리스트 Stack → O(n)
```

다만 두 구현은 같은 `O(n)`이어도 실제 메모리 구조가 다릅니다.

### 배열 기반 Stack

push는 보통

```java
arr[size++] = value;
```

한 번이면 되기 때문에 O(1)입니다.

하지만 배열이 가득 차면

```text
기존 n개 원소
↓
크기가 2n인 배열 생성
↓
n개 복사
```

해야 하므로 해당 push 한 번은 O(n)입니다.

그렇다면 왜 push를 O(1)이라고 하는가?

Capacity를

```text
4 → 8 → 16 → 32 → 64
```

처럼 2배씩 늘리면 resize는 매번 일어나지 않습니다.

여러 push의 비용을 전체적으로 나누어 보면 하나의 push당 평균 비용은 O(1)이 됩니다.

이를 **amortized O(1)** 이라고 합니다.

### 연결 리스트 기반 Stack

Stack의 top을 head로 잡으면

```text
push

newNode
   ↓
기존 head

head = newNode
```

만 수행하면 됩니다.

pop 역시

```text
head = head.next
```

로 처리할 수 있어 O(1)입니다.

### 배열 vs 연결 리스트 Trade-off

| 항목              | 배열 Stack       | 연결 리스트 Stack  |
| --------------- | -------------- | ------------- |
| push            | amortized O(1) | O(1)          |
| resize          | 필요             | 불필요           |
| 여유 capacity     | 발생 가능          | 거의 없음         |
| Node pointer    | 없음             | 필요            |
| memory locality | 좋음             | 상대적으로 나쁠 수 있음 |
| cache 친화성       | 일반적으로 좋음       | 상대적으로 낮음      |
| 구현              | 단순             | Node 필요       |

연결 리스트는 resize가 없지만 각 Node마다

```text
value + next reference + 객체 관리 overhead
```

가 발생할 수 있습니다.

또 Node들이 메모리상 연속해서 배치된다는 보장이 없기 때문에 CPU cache locality 측면에서는 배열이 유리한 경우가 많습니다.

그래서 실제 Java에서 일반적인 Stack/Queue 용도로 연결 리스트보다 `ArrayDeque`가 선호되는 이유 중 하나도 이런 메모리 특성과 관련이 있습니다.

### Edge Case

반드시 생각할 것:

```text
빈 Stack에서 pop
빈 Stack에서 peek
배열 capacity 초과
원소 1개에서 pop
여러 번 resize
```

**막힌 부분 / 다시 볼 것** → amortized O(1)의 의미 · 배열 resize 과정 · Linked Stack에서 head를 top으로 사용하는 이유 · cache locality

---

### 응용 문항

> 원본: [손코딩 연습 문항](../06-ALGORITHM.md) · IDE 없이 손으로 먼저 써본 뒤, 스터디에서 서로 비교합니다.

#### 3. 배열에 1,000,000 개의 수가 있다고 가정할 때, 여기에서 원하는 수가 몇 번째 인덱스에 위치해 있는지 확인하는 프로그램을 작성해 보세요. (단, 원하는 수가 하나가 아닐 수 있습니다.)

```java
import java.util.*;

class Solution {

    static List<Integer> findIndexes(int[] arr, int target) {

        List<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                indexes.add(i);
            }
        }

        return indexes;
    }

    public static void main(String[] args) {

        int[] arr = {1, 5, 3, 5, 7, 5};

        List<Integer> result = findIndexes(arr, 5);

        System.out.println(result); // [1, 3, 5]
    }
}
```

**시간복잡도** → `O(n)`

배열이 정렬되어 있다는 조건이 없고 원하는 값이 여러 번 등장할 수 있기 때문에 전체 배열을 확인해야 합니다.

```text
n = 1,000,000

최악의 경우
1,000,000개의 원소를 모두 확인
```

따라서

```text
O(n)
```

입니다.

target을 처음 찾았다고 종료하면 안 됩니다.

문제에서

> 원하는 수가 하나가 아닐 수 있다.

라고 했기 때문에 마지막 원소까지 확인해야 합니다.

**공간복잡도** → `O(k)`

여기서

```text
k = target이 등장한 횟수
```

입니다.

결과 index를 `List<Integer>`에 저장하기 때문에 반환 결과를 포함한 추가 공간은 O(k)입니다.

target이 모든 원소와 동일하다면

```text
k = n
```

이므로 최악의 경우 O(n)입니다.

### 왜 선형 탐색인가?

정렬 여부가 주어지지 않았습니다.

예를 들어

```text
[8, 2, 5, 1, 5, 3, 5]
```

에서는 target이 어디에 있는지 알 수 없으므로 전체를 확인하는 것이 가장 직접적입니다.

### 검색을 한 번만 한다면

```text
전처리 없음
검색 O(n)
추가 결과 공간 O(k)
```

이면 충분합니다.

### 같은 배열에서 검색을 수천 번 해야 한다면?

매번 배열 전체를 검색하면

```text
검색 횟수 Q
배열 크기 N

총 O(Q × N)
```

이 될 수 있습니다.

이때는 미리 index를 만들 수 있습니다.

```java
Map<Integer, List<Integer>> indexMap = new HashMap<>();

for (int i = 0; i < arr.length; i++) {
    indexMap
        .computeIfAbsent(arr[i], key -> new ArrayList<>())
        .add(i);
}
```

이렇게 하면

```text
5 → [1, 3, 5]
8 → [0]
2 → [1]
...
```

와 같이 값마다 원본 index 목록을 저장할 수 있습니다.

전처리:

```text
O(n)
```

이후 특정 값 검색:

```text
HashMap 탐색 평균 O(1)
+
결과 k개 확인 O(k)
```

즉 반복 검색이 많다면

```text
시간 ↓
메모리 ↑
```

라는 trade-off를 통해 성능을 개선할 수 있습니다.

### 정렬해서 Binary Search를 하면 안 되는가?

가능하지만 문제가 하나 있습니다.

원본 배열을 단순 정렬하면 **원래 index가 사라집니다.**

예:

```text
원본
[8, 3, 5, 1]

정렬
[1, 3, 5, 8]
```

정렬된 배열의 index와 원본 index는 다릅니다.

따라서 원본 index가 필요하다면

```text
(value, originalIndex)
```

쌍을 저장해서 정렬해야 합니다.

또 단 한 번 검색하는 문제라면

```text
정렬 O(n log n)
```

을 한 뒤 binary search를 하는 것보다 그냥

```text
linear scan O(n)
```

을 하는 것이 더 적절합니다.

### Edge Case

```text
target이 없는 경우 → 빈 List
target이 한 번 등장
target이 여러 번 등장
모든 값이 target
배열이 비어 있음
원소가 1,000,000개
```

모두 정상 처리할 수 있어야 합니다.

**다른 사람 풀이에서 배운 점** → 스터디 후 작성. 단일 검색이라면 선형 탐색이 가장 단순하지만 반복 검색이라면 HashMap 기반 인덱스를 미리 구축하는 방식도 비교해보기

---

## 회고

**미해결 질문** (다음 주에 다시 다룰 것)

* [ ] 시스템 콜로 Kernel Mode에 진입하는 것과 실제 task Context Switch가 발생하는 것을 구분해서 다시 설명해보기
* [ ] 교과서의 FCFS·RR·MLFQ와 현대 Linux의 EEVDF 기반 scheduling이 어떤 계층에서 다른 개념인지 추가로 정리하기

**이번 주에 가장 약했던 주제** → 스터디 후 직접 작성

**참고한 자료** (링크·책·문서)

* Abraham Silberschatz et al., *Operating System Concepts*
* Linux Kernel Documentation — EEVDF Scheduler
* Linux man-pages — `execve(2)`
* Java Virtual Machine Specification — Loading, Linking, and Initializing
* Java Virtual Machine Specification SE 26

---

[← Week 02](week-02.md) · [로드맵](../README.md) · [Week 04 →](week-04.md)
