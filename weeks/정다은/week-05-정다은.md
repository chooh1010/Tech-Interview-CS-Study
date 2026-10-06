# Week 05 — OS III — 메모리와 파일 시스템

> **메모리 계층, 가상 메모리, 페이징, 파일 I/O**  
> 진행일: `2026-10-06` (화)

---

# Part 1. 운영체제

## 1. 캐시 메모리 및 메모리 계층성에 대해 설명해 주세요.

### 면접용 답변

캐시 메모리는 **CPU와 주기억장치 사이의 속도 차이를 줄이기 위해 자주 사용하는 데이터를 임시 저장하는 고속 메모리**입니다.

컴퓨터의 메모리는 일반적으로

`Register → L1 Cache → L2 Cache → L3 Cache → RAM → SSD/HDD`

와 같은 계층 구조를 가집니다.

CPU에 가까울수록 **속도는 빠르지만 용량이 작고 가격이 비싸며**, 멀어질수록 **속도는 느리지만 용량이 크고 저렴**합니다.

캐시는 프로그램이 보이는 **시간 지역성과 공간 지역성**을 이용합니다.

- 시간 지역성: 최근 사용한 데이터는 다시 사용할 가능성이 높음
- 공간 지역성: 어떤 주소를 사용했다면 인접한 주소도 사용할 가능성이 높음

CPU가 메모리에 접근할 때 캐시에 데이터가 있으면 **Cache Hit**, 없으면 **Cache Miss**라고 합니다. Miss가 발생하면 하위 메모리 계층에서 데이터를 가져오며, 일반적으로 한 바이트가 아니라 **Cache Line 단위**로 가져옵니다.

따라서 배열처럼 연속된 메모리를 순차적으로 접근하면 공간 지역성을 활용할 수 있어 성능이 좋아질 수 있습니다.

### 상세 설명

메모리 계층을 만드는 가장 중요한 이유는 **CPU와 메모리의 성능 격차**입니다.

```text
CPU
 │
 ├─ Register
 │
 ├─ L1 Cache
 │
 ├─ L2 Cache
 │
 ├─ L3 Cache
 │
 └─ RAM
      │
      └─ SSD / HDD
```

상위 계층일수록 다음 특성이 있습니다.

| 계층 | 속도 | 용량 | 가격/bit |
|---|---:|---:|---:|
| Register | 매우 빠름 | 매우 작음 | 매우 높음 |
| L1 | 매우 빠름 | 작음 | 높음 |
| L2 | 빠름 | 중간 | 높음 |
| L3 | 비교적 빠름 | 큼 | 중간 |
| RAM | 느림 | 매우 큼 | 낮음 |
| SSD/HDD | 매우 느림 | 매우 큼 | 매우 낮음 |

현대 CPU에서는 정확한 구성은 아키텍처마다 다르지만, 일반적으로 L1/L2는 코어에 가까이 위치하고 L3는 여러 코어가 공유하는 형태가 많이 사용됩니다.

---

### Q. 캐시 메모리는 어디에 위치하나요?

CPU 내부 또는 CPU 코어와 매우 가까운 위치에 존재합니다.

특히 현대 CPU에서는 L1, L2, L3 캐시가 CPU 패키지 내부에 구현되는 것이 일반적입니다.

---

### Q. L1, L2 캐시에 대해 설명해 주세요.

**L1 Cache**

- CPU 코어와 가장 가까움
- 가장 빠름
- 가장 작음
- 명령어 캐시와 데이터 캐시가 분리되는 경우가 많음

```text
L1I → Instruction
L1D → Data
```

**L2 Cache**

- L1보다 크지만 느림
- L1 miss 발생 시 다음으로 탐색되는 계층
- 코어별 private cache인 경우가 많음

그 아래 L3 캐시는 여러 코어가 공유하는 설계가 흔하지만 CPU 아키텍처마다 다릅니다.

---

### Q. 캐시에 올라오는 데이터는 어떻게 관리되나요?

보통 **Cache Line**이라는 블록 단위로 관리합니다.

예를 들어 CPU가 주소 `0x1000`의 데이터를 요청했을 때 해당 값 하나만 가져오는 것이 아니라 그 주변 데이터까지 하나의 cache line으로 가져옵니다.

```text
RAM

[ A ][ B ][ C ][ D ][ E ][ F ][ G ][ H ]
       ↑ CPU가 C 요청

Cache

[ A B C D ... ]
```

이것이 **공간 지역성**을 활용하는 방식입니다.

캐시 공간이 부족해지면 기존 cache line 중 하나를 교체해야 합니다.

실제 CPU의 교체 정책은 복잡하지만 개념적으로는 LRU와 유사한 정책 등을 생각할 수 있습니다.

---

### Q. 캐시간의 동기화는 어떻게 이루어지나요?

멀티코어 환경에서는 각각의 CPU 코어가 같은 메모리 값을 캐싱할 수 있습니다.

```text
Core 1 Cache ─┐
              ├─ 같은 변수 X
Core 2 Cache ─┘
```

Core 1이 X를 수정했다면 Core 2의 캐시에 있는 X도 더 이상 그대로 사용하면 안 됩니다.

이를 해결하기 위해 **Cache Coherence Protocol**을 사용합니다.

대표적으로 MESI 계열 프로토콜이 있습니다.

```text
M = Modified
E = Exclusive
S = Shared
I = Invalid
```

CPU들이 캐시 라인의 상태를 추적하여 다른 코어가 데이터를 수정했을 때 관련 cache line을 무효화하거나 필요한 데이터를 전달합니다.

> 단, **cache coherence**와 **멀티스레드 동기화**는 같은 개념이 아닙니다.  
> 하드웨어가 캐시 일관성을 유지한다고 해서 프로그램의 race condition까지 해결되는 것은 아닙니다.

---

### Q. 캐시 Mapping 방식은?

대표적으로 세 가지가 있습니다.

#### Direct Mapping

메모리 블록이 들어갈 캐시 위치가 하나로 정해집니다.

```text
Cache Index = Memory Block % Cache Line Count
```

장점:

- 구현 단순
- 빠름

단점:

- 서로 다른 메모리 블록이 계속 같은 cache line을 요구하면 conflict miss 발생

---

#### Fully Associative

메모리 블록이 캐시의 어느 위치든 들어갈 수 있습니다.

장점:

- conflict miss 감소

단점:

- 어떤 line에 데이터가 있는지 전체를 비교해야 해서 하드웨어 비용 증가

---

#### Set Associative

Direct Mapping과 Fully Associative의 절충입니다.

캐시를 여러 set으로 나누고 해당 set 안에서는 여러 위치 중 하나를 사용할 수 있습니다.

현대 CPU에서 널리 사용됩니다.

---

### Q. 이차원 배열을 가로/세로 탐색하면 왜 차이가 나나요?

C의 row-major 배열을 예로 들면

```text
arr[0][0]
arr[0][1]
arr[0][2]
arr[0][3]
arr[1][0]
...
```

순서로 메모리에 저장됩니다.

따라서

```java
for (int i = 0; i < N; i++)
    for (int j = 0; j < M; j++)
        arr[i][j];
```

처럼 행 방향으로 순차 탐색하면 연속된 데이터를 사용하게 되어 cache hit 확률이 높습니다.

반면

```java
for (int j = 0; j < M; j++)
    for (int i = 0; i < N; i++)
        arr[i][j];
```

처럼 열 방향으로 멀리 떨어진 주소를 반복 접근하면 cache miss가 많아질 수 있습니다.

Java의 2차원 배열은 C의 진짜 연속 2차원 배열과 달리 **배열의 배열**이라는 차이가 있지만, 각 행 내부를 순차 접근하는 것이 일반적으로 locality 측면에서 유리합니다.

### 현업 사례

대규모 이미지 처리, 행렬 연산, 머신러닝, 게임 엔진 등에서는 단순히 알고리즘의 Big-O만 중요한 것이 아닙니다.

예를 들어 둘 다 `O(N²)`인 코드라도 메모리를 순차 접근하는 코드가 cache locality 덕분에 훨씬 빠를 수 있습니다.

이 때문에 고성능 코드에서는 **Data-Oriented Design**, 배열 연속 배치, blocking/tiling 같은 기법을 사용합니다.

### 핵심 키워드

`Memory Hierarchy` · `Cache Hit/Miss` · `Temporal Locality` · `Spatial Locality` · `Cache Line` · `L1/L2/L3` · `Cache Coherence` · `MESI` · `Set Associative`

---

# 2. 메모리의 연속할당 방식 세 가지를 설명해주세요.

## 면접용 답변

연속 메모리 할당에서 프로세스가 들어갈 수 있는 여러 빈 공간이 있을 때 어떤 공간을 선택할 것인지 결정하는 대표적인 방법으로 **First-Fit, Best-Fit, Worst-Fit**이 있습니다.

- **First-Fit**: 처음 발견한 충분한 공간에 할당
- **Best-Fit**: 필요한 크기와 가장 가까운 작은 공간에 할당
- **Worst-Fit**: 가장 큰 빈 공간에 할당

First-Fit은 탐색 시간이 상대적으로 짧다는 장점이 있고, Best-Fit은 큰 공간을 보존하려 하지만 작은 조각을 많이 만들 수 있습니다. Worst-Fit은 가장 큰 공간을 사용하여 할당 후에도 비교적 큰 공간을 남기려는 방식입니다.

세 방식 모두 연속할당이기 때문에 **외부 단편화** 문제가 발생할 수 있습니다.

---

## 예시

현재 빈 공간이

```text
100MB | 500MB | 200MB | 300MB | 600MB
```

이고 212MB가 필요하다면,

### First-Fit

처음 만나는 충분한 공간:

```text
500MB
```

### Best-Fit

212MB 이상 중 가장 작은 공간:

```text
300MB
```

### Worst-Fit

가장 큰 공간:

```text
600MB
```

---

## Q. Worst-Fit은 언제 사용할 수 있을까요?

큰 free block에서 필요한 만큼만 잘라 사용하면 **남은 공간 역시 어느 정도 큰 공간으로 유지될 가능성**이 있습니다.

따라서 작은 unusable hole이 만들어지는 것을 피하려는 아이디어입니다.

다만 실제 성능은 workload에 의존하며 Worst-Fit이 일반적으로 가장 좋다고 할 수는 없습니다.

---

## Q. 성능이 가장 좋은 알고리즘은 무엇인가요?

절대적으로 하나가 가장 좋다고 할 수 없습니다.

하지만 단순 구현에서는 **First-Fit이 탐색 비용과 메모리 이용률의 균형이 비교적 좋아 흔히 설명되는 방식**입니다.

Best-Fit은 모든 빈 공간을 탐색해야 할 수 있고 매우 작은 hole들을 만들어 오히려 단편화를 악화시킬 수도 있습니다.

실제 OS 메모리 관리에서는 이런 단순한 연속 할당 방식만 사용하는 것이 아니라 **Paging, Buddy Allocator, Slab 계열 allocator** 등 다양한 기법을 사용합니다.

### 핵심 키워드

`First-Fit` · `Best-Fit` · `Worst-Fit` · `External Fragmentation`

---

# 3. Thrashing이란 무엇인가요?

## 면접용 답변

Thrashing은 프로세스가 실제 연산보다 **페이지를 메모리와 디스크 사이에서 교체하는 데 대부분의 시간을 소비하는 상태**입니다.

프로세스들이 필요로 하는 working set에 비해 물리 메모리가 부족하면 Page Fault가 계속 발생합니다.

OS가 페이지를 가져오기 위해 다른 페이지를 내보냈는데 곧바로 그 페이지가 다시 필요해지면서,

```text
Page Fault
→ Page 교체
→ 다시 Page Fault
→ 다시 Page 교체
```

가 반복됩니다.

그 결과 디스크 I/O는 급증하고 CPU 이용률과 전체 시스템 성능은 크게 감소합니다.

---

## 상세 흐름

```text
Physical Memory 부족
        ↓
Working Set 전체를 RAM에 유지하지 못함
        ↓
Page Fault 증가
        ↓
Page In / Page Out 반복
        ↓
Disk / SSD I/O 증가
        ↓
CPU는 I/O를 기다림
        ↓
성능 급락
```

### Working Set

최근 일정 시간 동안 프로세스가 실제로 자주 사용하고 있는 페이지 집합입니다.

```text
Process A → 2GB 필요
Process B → 3GB 필요
Process C → 4GB 필요

실제 사용 가능 RAM → 5GB
```

모든 프로세스의 working set을 유지하지 못한다면 page replacement가 지나치게 발생할 수 있습니다.

---

## Q. Thrashing을 완화하려면?

### 1. Multiprogramming 정도 감소

동시에 실행하는 프로세스 수를 줄입니다.

### 2. 물리 RAM 증가

가장 직접적인 해결 방법입니다.

### 3. Working Set 기반 관리

프로세스별로 필요한 working set을 RAM에 유지합니다.

### 4. Page Fault Frequency 이용

Page Fault 빈도가 지나치게 높다면 더 많은 frame을 할당하거나 프로세스를 일시 중단합니다.

### 5. 적절한 Page Reclaim 정책 사용

최근 사용된 hot page는 유지하고 사용되지 않는 cold page를 제거합니다.

현대 Linux에는 전통적인 단순 LRU보다 접근 최신성을 더 잘 구분하기 위한 **Multi-Gen LRU**가 존재하며, Linux 문서에서도 memory pressure에서 page reclaim 효율과 thrashing 완화를 주요 목표로 설명합니다.

---

## 현업 사례

서버의 RAM이 부족해져 swap 사용량이 급격히 증가하면

```text
API latency ↑
CPU 사용률은 의외로 낮음
Disk I/O ↑↑
Swap ↑↑
```

와 같은 현상이 나타날 수 있습니다.

예를 들어 여러 JVM 컨테이너를 하나의 서버에 지나치게 많이 배치하면 working set이 RAM보다 커지고 swap/reclaim이 반복되면서 서비스 응답시간이 급격히 악화될 수 있습니다.

### 핵심 키워드

`Working Set` · `Page Fault` · `Page In/Out` · `Swap` · `Page Replacement` · `Memory Pressure`

---

# 4. 가상 메모리란 무엇인가요?

## 면접용 답변

가상 메모리는 각 프로세스에게 **독립적이고 연속된 것처럼 보이는 가상 주소 공간을 제공하고 이를 실제 물리 메모리에 매핑하는 메모리 관리 기법**입니다.

프로세스는 직접 물리 주소에 접근하는 것이 아니라 Virtual Address를 사용합니다.

```text
Virtual Address
       ↓
      MMU
       ↓
Page Table
       ↓
Physical Address
```

이를 통해 실제 RAM보다 큰 주소 공간을 사용할 수 있고, 프로세스 간 메모리 격리와 보호가 가능해집니다.

필요한 페이지만 메모리에 올리는 Demand Paging을 사용할 수 있어 메모리 이용률도 높일 수 있습니다.

Linux에서도 가상 메모리와 demand paging은 메모리 관리의 핵심 기능으로 설명됩니다.

---

## 왜 필요한가?

### 1. 프로세스 격리

```text
Process A : 0x1000
Process B : 0x1000
```

동일한 가상 주소를 사용하더라도 서로 다른 physical page에 매핑할 수 있습니다.

따라서 A가 B의 메모리를 임의로 접근하는 것을 막을 수 있습니다.

### 2. 물리 메모리보다 큰 주소 공간

현재 필요한 부분만 RAM에 올리고 나머지는 파일이나 swap 등에 둘 수 있습니다.

### 3. 프로그래밍 단순화

프로그램에서는 물리 RAM의 단편화를 신경 쓰지 않고 연속된 address space처럼 사용할 수 있습니다.

### 4. 공유 가능

shared library나 shared memory를 여러 프로세스의 서로 다른 가상 주소에서 같은 physical page로 매핑할 수 있습니다.

### 5. Copy-On-Write

`fork()` 이후 부모와 자식이 실제로 수정하기 전까지 같은 physical page를 공유할 수 있습니다.

---

# Q. 가상 메모리가 가능한 이유는?

프로그램은 전체 메모리를 항상 동시에 사용하지 않기 때문입니다.

즉 **지역성(Locality)**이 존재합니다.

실제로 당장 필요한 page만 RAM에 두고 필요하지 않은 page는 RAM 밖에 둘 수 있습니다.

---

# Q. Page Fault가 발생하면 어떻게 처리하나요?

```text
CPU
 │ Virtual Address 접근
 ▼
MMU
 │
 ├─ Mapping 있음 → 정상 접근
 │
 └─ Mapping 없음 / 조건 불충족
             ↓
         Page Fault
             ↓
          Kernel
```

OS는 page fault가 발생하면 먼저 그 접근이 **유효한 접근인지 확인**합니다.

### 유효한 접근

예:

- 아직 RAM에 올라오지 않은 파일-backed page
- demand-zero page
- swap에 내려간 page
- Copy-On-Write write fault

필요하면 physical frame을 확보하고 데이터를 읽은 후 Page Table Entry를 수정하고 명령어를 다시 실행합니다.

### 유효하지 않은 접근

예:

```c
int *p = NULL;
*p = 10;
```

또는 read-only page에 잘못된 write를 하는 경우입니다.

이 경우 OS는 프로세스에 오류를 전달할 수 있으며 Unix 계열에서는 대표적으로 `SIGSEGV`가 발생할 수 있습니다.

Linux 공식 문서 역시 page table이 CPU가 보는 virtual address를 physical address로 매핑하고 MMU/TLB가 이 변환 과정에 관여한다고 설명합니다.

---

# Q. 페이지 크기의 Trade-Off는?

## 페이지가 크면

장점:

- Page Table Entry 수 감소
- Page Table 크기 감소
- 하나의 TLB entry가 더 넓은 주소 범위를 커버
- sequential workload에서 유리할 수 있음

단점:

- 내부 단편화 증가
- 실제로 필요하지 않은 데이터까지 메모리에 올라올 수 있음
- Page Fault 한 번의 I/O 비용 증가 가능
- Copy-On-Write 등의 단위가 커질 수 있음

## 페이지가 작으면

장점:

- 필요한 데이터만 세밀하게 로딩 가능
- 내부 단편화 감소

단점:

- Page Table이 커짐
- 더 많은 TLB entry 필요
- 주소 변환 관리 비용 증가

---

# Q. 페이지 크기가 커지면 Page Fault가 더 많이 발생하나요?

**반드시 그렇지는 않습니다.**

오히려 큰 page 하나를 가져오면 더 넓은 범위가 한 번에 올라오기 때문에 sequential access에서는 page fault 횟수가 감소할 수도 있습니다.

하지만 sparse access처럼 실제 필요한 데이터가 매우 적다면 필요 없는 데이터까지 불러오게 되어 메모리 낭비와 I/O 비용이 커질 수 있습니다.

따라서

> Page Size ↑ = Page Fault ↑

라고 단순하게 말하면 틀립니다.

---

# Q. 세그멘테이션을 사용하면 가상 메모리를 사용할 수 없나요?

아닙니다.

**세그멘테이션과 가상 메모리는 배타적인 개념이 아닙니다.**

과거 x86에서는 segmentation과 paging을 함께 사용할 수도 있었습니다.

현대 범용 OS에서는 일반적으로 paging이 가상 메모리 구현의 핵심 역할을 담당합니다.

### 핵심 키워드

`Virtual Address` · `Physical Address` · `Page Table` · `Demand Paging` · `Page Fault` · `Memory Protection` · `Isolation` · `COW`

---

# 5. 세그멘테이션과 페이징의 차이점은 무엇인가요?

## 면접용 답변

Paging과 Segmentation의 가장 큰 차이는 **메모리를 나누는 기준**입니다.

Paging은 메모리를 **고정 크기 단위인 Page**로 나누고 물리 메모리를 같은 크기의 Frame으로 나눕니다.

반면 Segmentation은 Code, Data, Stack과 같이 프로그램의 **논리적 단위에 따라 가변 크기로 나눕니다.**

Paging은 고정 크기이기 때문에 외부 단편화를 줄일 수 있지만 내부 단편화가 발생할 수 있고, Segmentation은 가변 크기이기 때문에 내부 단편화는 적지만 외부 단편화가 발생할 수 있습니다.

| | Paging | Segmentation |
|---|---|---|
| 단위 | 고정 크기 | 가변 크기 |
| 기준 | 물리적 관리 | 논리적 의미 |
| 주소 | Page + Offset | Segment + Offset |
| 주요 단편화 | 내부 | 외부 |

---

# Q. Page와 Frame의 차이는?

**Page**

가상 주소 공간을 일정한 크기로 나눈 단위입니다.

**Frame**

물리 메모리를 동일한 크기로 나눈 단위입니다.

```text
Virtual Memory            Physical Memory

Page 0 ────────────────→ Frame 5
Page 1 ────────────────→ Frame 2
Page 2 ────────────────→ Frame 9
```

크기는 같습니다.

```text
Page Size == Frame Size
```

---

# Q. 내부 단편화와 외부 단편화란?

## 내부 단편화

할당받은 공간 내부에 사용하지 않는 공간이 생기는 것.

예:

```text
Page size = 4KB

실제 필요 = 10KB

3 Page = 12KB

2KB 낭비
```

## 외부 단편화

전체 free memory는 충분하지만 작은 공간으로 흩어져 있어 큰 연속 공간을 할당하지 못하는 문제입니다.

```text
[사용][2MB free][사용][3MB free][사용]

총 free = 5MB

4MB 연속 할당 → 불가능
```

---

# Q. 페이지에서 실제 주소를 어떻게 가져오나요?

가상 주소를

```text
Virtual Address
= Page Number + Offset
```

으로 나눕니다.

Page Number를 이용해 page table을 찾습니다.

```text
Page Table

VPN 17 → PFN 8
```

그 뒤 physical address는 개념적으로

```text
Physical Address
= PFN × PageSize + Offset
```

이 됩니다.

Linux 문서에서도 page frame number를 physical page 주소를 PAGE_SIZE로 나눈 값으로 정의하고 있습니다.

---

# Q. 주소 공간이 수정 가능한지 어떻게 확인하나요?

Page Table Entry에는 physical frame 번호 외에도 여러 protection bit가 존재합니다.

예를 들어 개념적으로

```text
Present
Read/Write
User/Supervisor
Execute 관련 권한
Accessed
Dirty
```

등의 정보가 있습니다.

따라서 write permission이 없는 page를 수정하면 CPU가 fault를 발생시키고 OS가 처리합니다.

OS 차원에서는 VMA 등의 메타데이터도 함께 사용하여 해당 주소 범위가 어떤 권한을 가지는지 관리합니다.

---

# Q. 32비트에서 Page Size가 1KB라면 페이지 테이블 최대 엔트리는?

32bit virtual address 공간:

```text
2^32 bytes
```

Page Size:

```text
1KB = 2^10 bytes
```

따라서 page 수:

```text
2^32 / 2^10
= 2^22
= 4,194,304 pages
```

즉 단일 레벨 page table이라고 가정하면 최대

**4,194,304개의 Page Table Entry**

가 필요합니다.

만약 PTE 하나가 4 bytes라면

```text
2^22 × 4
= 2^24 bytes
= 16MB
```

의 page table이 필요합니다.

이것이 multi-level page table이 필요한 이유 중 하나입니다.

---

# Q. 32비트 운영체제는 RAM을 최대 4GB까지 사용할 수 있다는 이유는?

면접에서 이 질문에는 표현을 정확하게 해야 합니다.

32bit address는

```text
2^32
= 4,294,967,296
```

개의 서로 다른 byte 주소를 표현할 수 있습니다.

따라서 하나의 32bit 가상 주소 공간은 이론적으로 약 **4GiB** 범위를 표현합니다.

```text
0x00000000
~
0xFFFFFFFF
```

다만

> "32bit OS는 물리 RAM을 무조건 최대 4GB만 사용할 수 있다"

라고 말하면 엄밀히는 틀립니다.

PAE 같은 기술을 사용하면 32bit CPU/OS에서도 4GB를 넘는 physical memory를 다룰 수 있는 경우가 있습니다.

다만 하나의 일반적인 32bit 프로세스가 갖는 virtual address space 자체는 32bit 주소 크기의 제약을 받습니다.

---

# Q. Segmentation Fault는 세그멘테이션 방식과 관계가 있나요?

이름 때문에 자주 혼동하지만 현대 OS에서 발생하는 Segmentation Fault를 단순히

> "Segmentation 메모리 관리 방식을 사용해서 발생한다"

라고 이해하면 잘못입니다.

Segmentation Fault는 프로세스가 **허용되지 않은 virtual memory 영역에 접근했을 때 발생하는 메모리 접근 오류**입니다.

예:

```c
int *p = NULL;
*p = 10;
```

또는

```text
읽기 전용 메모리에 write
할당되지 않은 주소 접근
이미 해제된 잘못된 주소 접근
```

등입니다.

CPU가 page table permission 등의 이유로 fault를 발생시키고 OS가 해당 접근이 유효하지 않다고 판단하면 Unix 계열에서 `SIGSEGV`를 전달할 수 있습니다.

### 핵심 키워드

`Page` · `Frame` · `Internal Fragmentation` · `External Fragmentation` · `PTE` · `Protection Bit` · `Multi-level Page Table`

---

# 6. TLB는 무엇인가요?

## 면접용 답변

TLB, 즉 Translation Lookaside Buffer는 **최근 사용한 Virtual Page Number와 Physical Frame Number의 주소 변환 결과를 저장하는 CPU 내부의 작은 고속 캐시**입니다.

가상 주소를 physical address로 변환하려면 Page Table을 조회해야 하는데, page table 자체도 메모리에 있기 때문에 매번 조회하면 비용이 큽니다.

따라서 먼저 TLB를 조회합니다.

```text
Virtual Address
      ↓
     TLB
   ↙     ↘
 Hit      Miss
 ↓          ↓
바로 변환   Page Table Walk
```

TLB Hit이면 page table을 다시 탐색하지 않고 physical address를 빠르게 얻을 수 있습니다.

Linux 문서에서도 MMU가 주소 변환을 수행하며 TLB와 Page Walk Cache를 이용해 변환을 가속한다고 설명합니다.

---

# 매우 중요한 구분

```text
TLB Miss ≠ Page Fault
```

### TLB Miss

TLB에 주소 변환 정보가 없다는 뜻입니다.

Page Table에는 정상적인 mapping이 있을 수 있습니다.

```text
TLB Miss
→ Page Table Walk
→ PTE 발견
→ TLB 업데이트
→ 정상 실행
```

### Page Fault

Page Table을 확인했는데 필요한 mapping이 존재하지 않거나 접근 조건이 충족되지 않은 상황입니다.

```text
TLB Miss
→ Page Table Walk
→ PTE로 즉시 처리 불가능
→ Page Fault
→ Kernel 개입
```

면접에서 자주 나오는 구분입니다.

---

# Q. MMU란?

**Memory Management Unit**

Virtual Address를 Physical Address로 변환하고 memory protection 관련 검사를 지원하는 하드웨어입니다.

```text
CPU
 │
 │ Virtual Address
 ▼
MMU
 │
 ├─ TLB
 └─ Page Table Walk
 │
 ▼
Physical Memory
```

---

# Q. TLB와 MMU는 어디에 있나요?

일반적으로 CPU 내부 또는 CPU 코어에 매우 가까운 하드웨어입니다.

현대 프로세서는 instruction/data TLB를 별도로 두거나 여러 단계 TLB를 가지기도 합니다.

정확한 구조는 CPU architecture에 따라 다릅니다.

---

# Q. 코어가 여러 개라면 TLB는 어떻게 동기화하나요?

코어별 TLB가 있을 수 있습니다.

문제는 OS가 Page Table을 수정했을 때입니다.

```text
Core 1 TLB
VPN 10 → PFN 5

Page Table 변경
VPN 10 → PFN 9

Core 1 TLB에는 옛날 값 존재
```

이런 오래된 TLB entry를 제거해야 합니다.

이를 위해 OS는 다른 CPU core에게 해당 TLB entry를 무효화하도록 요청하는 **TLB Shootdown**을 수행할 수 있습니다.

일반적으로 inter-processor interrupt 등을 통해 다른 core에 invalidation을 요청합니다.

---

# Q. Context Switch가 발생하면 TLB는 어떻게 되나요?

단순한 구현에서는 process가 바뀌면 이전 process의 TLB mapping을 그대로 사용할 수 없기 때문에 TLB를 flush해야 합니다.

```text
Process A

VPN 1 → PFN 100

Process B

VPN 1 → PFN 500
```

같은 virtual address라도 mapping이 다르기 때문입니다.

하지만 TLB flush는 성능 비용이 큽니다.

따라서 현대 CPU에서는 **ASID(Address Space Identifier)** 또는 x86의 **PCID(Process Context Identifier)**처럼 TLB entry에 address space를 식별하는 tag를 붙여 서로 다른 process의 entry를 구분할 수 있습니다.

따라서 context switch 때 반드시 모든 TLB entry를 flush해야 하는 것은 아닙니다.

### 핵심 키워드

`TLB` · `MMU` · `TLB Hit` · `TLB Miss` · `Page Table Walk` · `TLB Shootdown` · `ASID` · `PCID`

---

# 7. 페이지 교체 알고리즘에 대해 설명해 주세요.

## 면접용 답변

Page Fault가 발생했는데 사용 가능한 physical frame이 없다면 OS는 기존 page 중 하나를 선택해서 제거해야 합니다.

이때 어떤 page를 제거할지 결정하는 것이 Page Replacement Algorithm입니다.

대표적으로

- FIFO
- Optimal
- LRU
- Clock / Second Chance

등이 있습니다.

Optimal은 앞으로 가장 오랫동안 사용되지 않을 page를 제거하기 때문에 이론적으로 최적이지만 미래의 접근을 알아야 하므로 실제 구현에는 사용할 수 없습니다.

LRU는 가장 오래 사용되지 않은 page를 제거하며 프로그램의 시간 지역성을 활용합니다.

하지만 완벽한 LRU를 구현하면 access마다 사용 시점을 기록해야 하므로 비용이 커서 실제 OS에서는 이를 근사하는 방법을 사용합니다.

---

# FIFO

가장 먼저 들어온 page를 제거합니다.

```text
먼저 들어옴                최근
   ↓                        ↓

[A][B][C][D]

A 제거
```

장점:

- 구현 간단

단점:

- 사용 빈도를 고려하지 않음
- Belady's Anomaly 발생 가능

### Belady's Anomaly

frame을 더 많이 줬는데 오히려 Page Fault가 증가하는 현상입니다.

FIFO에서 발생할 수 있습니다.

---

# Optimal

앞으로 가장 늦게 사용될 page를 제거합니다.

```text
현재

A B C D

미래:
A → 2초 후
B → 5초 후
C → 100초 후
D → 3초 후

C 제거
```

Page Fault를 최소화할 수 있는 이론적 알고리즘이지만 미래를 알 수 없기 때문에 실제 OS 구현에는 사용할 수 없습니다.

다른 알고리즘의 성능을 평가하는 기준으로 사용할 수 있습니다.

---

# LRU

**Least Recently Used**

가장 오랫동안 사용되지 않은 page를 제거합니다.

과거에 오래 사용되지 않았다면 가까운 미래에도 사용되지 않을 가능성이 높다는 **시간 지역성**을 이용합니다.

```text
최근 사용

A → 1초 전
B → 30초 전
C → 3초 전
D → 10초 전

B 제거
```

---

# Q. LRU는 어떤 특성을 이용하나요?

**Temporal Locality, 시간 지역성**입니다.

최근 사용한 데이터는 가까운 미래에도 다시 사용할 가능성이 높다는 특성을 이용합니다.

---

# Q. LRU를 직접 구현한다면?

애플리케이션 수준 LRU Cache라면 흔히

```text
HashMap + Doubly Linked List
```

를 사용합니다.

```text
HashMap
key → Node

Linked List
MRU <------------> LRU
```

조회:

```text
HashMap → O(1)
```

최근 사용 위치 이동:

```text
Linked List → O(1)
```

삭제:

```text
Tail 제거 → O(1)
```

따라서 get/put을 평균 `O(1)`로 구현할 수 있습니다.

---

# Q. LRU의 단점은?

정확한 LRU를 OS 수준에서 구현하려면 memory access가 일어날 때마다 최근 사용 정보를 갱신해야 하므로 비용이 큽니다.

따라서 실제 OS에서는 **Reference Bit를 활용한 Clock/Second Chance** 같은 근사 알고리즘이나 더 복잡한 page reclaim 정책을 사용할 수 있습니다.

---

# Clock / Second Chance

page마다 reference bit를 둡니다.

```text
        pointer
           ↓
[A:1][B:0][C:1][D:1]
```

pointer가 가리킨 page의 bit가

```text
0 → 제거
1 → 0으로 변경하고 다음 page 확인
```

하는 방법입니다.

정확한 LRU보다 구현 비용이 작습니다.

---

# 실제 Linux 사례

현대 Linux에서는 단순한 교과서 LRU 하나로 모든 page를 교체한다고 이해하면 정확하지 않습니다.

Linux에는 **Multi-Gen LRU**가 있으며 page를 접근 시점에 따라 여러 generation으로 분류하여 hot/cold page를 구별합니다. Linux kernel 문서는 이를 page reclaim과 memory pressure에서의 RAM 효율 개선을 위한 LRU 구현으로 설명합니다.

### 핵심 키워드

`FIFO` · `Belady's Anomaly` · `Optimal` · `LRU` · `Temporal Locality` · `Clock` · `Second Chance` · `Page Reclaim`

---

# 8. File Descriptor와 File System에 대해 설명해 주세요.

## 면접용 답변

File System은 저장장치의 데이터를 파일과 디렉터리 형태로 저장하고 관리하기 위한 시스템입니다.

파일의 이름, 위치, 크기, 권한과 같은 metadata와 실제 data block을 관리합니다.

File Descriptor는 Unix/Linux에서 프로세스가 열린 파일이나 socket 같은 I/O 자원에 접근할 때 사용하는 **작은 정수형 식별자**입니다.

예를 들어

```text
0 = stdin
1 = stdout
2 = stderr
```

가 관례적으로 사용됩니다.

`open()`으로 파일을 열면 kernel이 open file 정보를 만들고 프로세스에게 File Descriptor를 반환합니다.

Linux `open(2)` 문서에서도 FD를 프로세스의 open-file descriptor table entry를 가리키는 작은 음이 아닌 정수라고 정의합니다.

---

# File Descriptor 구조

개념적으로 Linux에서는

```text
Process
   │
   ▼
FD Table
   │
   │ fd = 3
   ▼
Open File Description
   │
   ├─ current file offset
   ├─ status flags
   │
   ▼
File / inode
   │
   ▼
Filesystem
   │
   ▼
Disk
```

처럼 이해할 수 있습니다.

File Descriptor 자체는 단지 작은 정수입니다.

```c
int fd = open("data.txt", O_RDONLY);
```

예를 들어

```text
fd = 3
```

이 반환될 수 있습니다.

이 3이라는 숫자가 파일 자체는 아닙니다.

프로세스의 descriptor table에서 kernel object를 찾아가기 위한 handle입니다.

`open()`은 새로운 **open file description**을 만들고 FD가 이를 참조하며, file offset과 status flag 등이 open file description에 저장됩니다. `dup()`이나 `fork()`로 만들어진 descriptor는 동일한 open file description을 공유할 수 있습니다.

---

# File Descriptor는 파일에만 사용되나요?

아닙니다.

Unix/Linux에서는 많은 I/O 객체를 FD로 다룹니다.

```text
Regular File
Socket
Pipe
Terminal
Device
...
```

따라서 Linux 서버 프로그래밍에서는 네트워크 socket도 File Descriptor로 처리합니다.

---

# Q. inode란?

inode는 Unix 계열 file system에서 **파일 자체에 대한 metadata를 저장하는 구조**라고 볼 수 있습니다.

대표적으로

```text
File type
Permission
Owner UID/GID
File size
Timestamp
Data block 정보
Link count
...
```

등을 가집니다.

Linux 문서에서도 각 파일이 inode를 가지며 파일의 metadata가 inode에 저장된다고 설명합니다.

### 중요한 점

일반적인 Unix filesystem 개념에서 **파일 이름 자체는 inode의 핵심 정보가 아닙니다.**

directory entry가

```text
file name → inode
```

관계를 관리합니다.

따라서 hard link를 만들면 서로 다른 이름이 같은 inode를 가리킬 수 있습니다.

```text
a.txt ─┐
       ├─ inode 100
b.txt ─┘
```

---

# File System 전체 구조

단순화하면

```text
Path
  ↓
Directory Entry
  ↓
inode
  ↓
Data Blocks
```

입니다.

Linux에는 다양한 filesystem이 존재합니다.

```text
ext4
XFS
Btrfs
tmpfs
NFS
...
```

애플리케이션이 각각을 전부 다른 API로 처리하지 않도록 Linux kernel은 **VFS(Virtual File System)** 계층을 제공합니다. Linux kernel 문서에서도 VFS 아래에 inode, file object, dentry 등의 핵심 객체를 정의하고 있습니다.

---

# Q. Python open(), Java BufferedReader는 실제 파일을 어떻게 읽나요?

애플리케이션 관점에서는

```java
BufferedReader br = new BufferedReader(...);
```

같은 고수준 API를 사용하지만 결국 OS의 file I/O 기능을 이용합니다.

개념적으로

```text
Application
    ↓
BufferedReader / Python Buffered I/O
    ↓
Runtime / Library
    ↓
read() 등 OS I/O
    ↓
Kernel
    ↓
Page Cache
    ↓
Filesystem
    ↓
Storage
```

와 같은 계층을 거칩니다.

### 왜 BufferedReader를 사용하는가?

파일에서 한 글자 읽을 때마다 매번 system call을 수행하면 user mode ↔ kernel mode 전환 비용이 반복됩니다.

따라서 일정량을 한 번에 읽어서 user-space buffer에 저장해 놓고 애플리케이션에 전달합니다.

```text
나쁜 예

read 1 byte
read 1 byte
read 1 byte
read 1 byte
...

좋은 예

OS에서 8KB 등 일정 블록 read
             ↓
        User Buffer
             ↓
       조금씩 소비
```

Linux의 일반적인 buffered file I/O는 **Page Cache**를 사용합니다. Linux kernel 문서는 일반적인 read/write/mmap이 page cache를 통하며 `O_DIRECT` 등을 통해 이를 우회할 수 있다고 설명합니다.

즉 흔히 두 종류의 caching/buffering을 구분할 수 있습니다.

```text
Java BufferedReader
      ↓
User-space Buffer

Linux Kernel
      ↓
Page Cache
```

둘은 동일한 것이 아닙니다.

---

# 현업 사례 — Too many open files

웹 서버에서 socket 또는 파일을 열어 놓고 `close()`하지 않으면 FD leak이 발생할 수 있습니다.

결국 process의 FD 한도를 초과하면

```text
Too many open files
EMFILE
```

오류가 발생할 수 있습니다.

예를 들어 DB connection이나 HTTP socket 역시 FD를 소비할 수 있기 때문에 서버 장애 분석에서

```bash
lsof
/proc/<pid>/fd
ulimit -n
```

등을 확인하기도 합니다.

### 핵심 키워드

`File Descriptor` · `FD Table` · `Open File Description` · `inode` · `dentry` · `VFS` · `Page Cache` · `Buffered I/O`

---

# Part 2. 개발상식

# 9. GC에 대해 설명해 주세요.

## 면접용 답변

GC, 즉 Garbage Collection은 **프로그램에서 더 이상 사용하지 않는 객체를 자동으로 찾아 메모리를 회수하는 기능**입니다.

Java에서는 객체가 주로 Heap 영역에 생성되고 GC는 GC Root로부터 객체의 reference를 추적하여 도달할 수 없는 객체를 garbage로 판단합니다.

대표적인 GC 기본 알고리즘에는

- Mark-Sweep
- Mark-Compact
- Copying
- Generational Collection

등이 있습니다.

Java HotSpot에서는 여러 종류의 collector가 제공되고, JDK 9 이후 G1 GC가 기본 collector로 사용되어 왔습니다. G1은 Heap을 여러 region으로 나누고, 일부 marking 작업을 애플리케이션과 동시에 수행하며 필요한 region의 객체를 이동시켜 메모리를 회수합니다.

---

# GC의 핵심 개념 — Reachability

다음 객체가 있다고 하겠습니다.

```text
GC Root
  │
  ▼
  A
  │
  ▼
  B

      C → D
```

GC Root에서 A와 B에는 도달할 수 있습니다.

하지만 C와 D에는 도달할 수 없다면

```text
C
D
```

는 garbage가 됩니다.

따라서 Java의 GC는 단순히

> "reference count가 0인가?"

만 검사하는 방식이 아닙니다.

---

# GC Root 예

JVM 구현 세부는 있지만 면접 수준에서는 다음과 같이 설명할 수 있습니다.

```text
Thread Stack의 참조
Static Field의 참조
JVM 내부에서 유지하는 참조
JNI Reference
...
```

이런 root에서 시작해 객체 그래프를 탐색합니다.

---

# Mark-Sweep

## Mark

GC Root로부터 reachable object를 표시합니다.

```text
Root → A → B

C → D
```

A, B → Mark

## Sweep

Mark되지 않은 객체를 제거합니다.

```text
C, D 제거
```

### 단점

객체가 제거된 자리에 작은 공간이 흩어지면서 fragmentation이 발생할 수 있습니다.

---

# Mark-Compact

Mark 이후 살아 있는 객체를 한쪽으로 이동합니다.

```text
Before

[A][ ][B][ ][ ][C]

After

[A][B][C][       ]
```

fragmentation을 줄일 수 있지만 객체 이동 비용이 발생합니다.

---

# Copying

메모리 영역을 나누고 살아 있는 객체만 다른 영역으로 복사합니다.

```text
From Space
[A][X][B][X]

        ↓

To Space
[A][B]
```

살아 있는 객체가 적을 때 효율적입니다.

---

# Generational GC

많은 애플리케이션에서

> 대부분의 객체는 생성된 뒤 오래 살아남지 않는다.

는 특성이 있습니다.

이를 **Weak Generational Hypothesis**라고 합니다.

따라서 Heap을 개념적으로

```text
Young Generation
        ↓
Old Generation
```

처럼 나누어 관리할 수 있습니다.

짧게 살아가는 객체는 Young 영역에서 빠르게 회수하고, 여러 번 살아남은 객체는 Old 영역으로 이동시킵니다.

---

# Q. Java에서는 GC를 어떻게 구현하나요?

Java HotSpot은 하나의 고정 GC 알고리즘만 사용하는 것이 아니라 여러 collector를 제공합니다.

예:

```text
Serial GC
Parallel GC
G1 GC
ZGC
...
```

현재 Oracle 문서 기준 Java SE에서는 G1이 기본 선택이며 G1은 대부분 concurrent하게 동작하면서 pause-time 목표와 throughput 사이의 균형을 제공하도록 설계되어 있습니다.

---

# G1 GC

**Garbage First**

Heap을 동일 크기의 여러 region으로 나눕니다.

```text
Heap

[Eden][Old][Old][Free][Survivor][Old][Free]...
```

전통적인 방식처럼 Young/Old가 반드시 하나의 연속된 메모리 범위일 필요가 없습니다.

Oracle의 최신 G1 문서에서도 Heap이 동일 크기의 region들로 나뉘며 region이 Eden, Survivor, Old 등의 역할을 가질 수 있다고 설명합니다.

G1은 garbage 비율이 높아 회수 효율이 좋은 region을 우선 대상으로 선택하는 방식으로 동작합니다.

Concurrent Marking 과정이 존재하지만 모든 작업이 concurrent인 것은 아닙니다.

Young Collection이나 Remark 등 **Stop-The-World pause가 존재합니다.**

따라서

> "G1은 애플리케이션을 절대 멈추지 않는다"

라고 말하면 틀립니다.

---

# Q. GC의 장점과 단점은?

## 장점

### 자동 메모리 관리

개발자가 모든 객체에 대해 직접 `free()`할 필요가 없습니다.

### Dangling Pointer 위험 감소

C/C++의 manual memory management에서 발생할 수 있는 use-after-free 문제를 상당 부분 방지합니다.

### 생산성 향상

비즈니스 로직에 집중할 수 있습니다.

---

## 단점

### GC Pause

일부 단계에서 애플리케이션 thread가 중지될 수 있습니다.

```text
Application Running
       ↓
     GC Pause
       ↓
Application Running
```

latency-sensitive server에서는 문제가 될 수 있습니다.

### CPU 사용

GC 자체가 CPU 자원을 사용합니다.

### 메모리 여유 공간 필요

GC가 효율적으로 동작하려면 일정 수준의 spare heap이 필요합니다.

### 실행 시점의 비결정성

개발자가 모든 메모리 회수 시점을 정확히 제어하기 어렵습니다.

---

# Q. GC는 어떤 영역 데이터를 관리하나요?

Java에서는 핵심적으로 **Heap의 객체**를 대상으로 합니다.

```text
Stack
 ├─ local primitive
 └─ object reference ──────┐
                           ▼
Heap                    Object
```

Stack frame 자체는 함수 호출이 끝나면 자연스럽게 제거됩니다.

Stack에 저장된 reference는 GC가 객체의 생존 여부를 판단할 때 GC Root 역할을 할 수 있습니다.

따라서

> "GC가 Stack 메모리를 청소한다"

라고 답하면 부정확합니다.

JVM의 class unloading 등은 별도 JVM 메모리 영역과 관련될 수 있지만 기본적인 면접 답변에서는 **Heap 객체의 생명주기를 관리한다**고 답하면 됩니다.

---

# Q. Reference Counting이란?

객체를 참조하고 있는 reference의 개수를 저장하는 방식입니다.

```text
A → Object X
B → Object X

refCount = 2
```

B가 reference를 제거하면

```text
refCount = 1
```

A도 제거하면

```text
refCount = 0
```

즉시 제거할 수 있습니다.

장점은 객체를 언제 해제할 수 있는지 비교적 즉각적으로 판단할 수 있다는 것입니다.

---

# 순환 참조 문제

```text
A → B
↑   ↓
└───┘
```

외부에서는 A와 B에 접근할 방법이 없어도

```text
A refCount = 1
B refCount = 1
```

이 유지될 수 있습니다.

따라서 단순 Reference Counting만 사용하면 두 객체를 제거하지 못합니다.

이것이 **Cycle / Retain Cycle** 문제입니다.

---

# Java와 Python 비교

### Java

기본적으로 tracing GC 기반으로 reachability를 판단합니다.

따라서 단순 reference-count cycle 문제와 동일한 방식으로 객체가 영원히 남는 구조는 아닙니다.

### CPython

Reference Counting을 주요 메모리 관리 방법으로 사용하면서 별도의 cyclic GC를 함께 사용하여 순환 참조를 처리합니다.

이 비교까지 이야기하면 면접에서 GC 이해도를 보여주기 좋습니다.

---

# 현업 사례

Spring Boot 서버에서 객체 allocation rate가 지나치게 높으면 Young GC가 자주 발생할 수 있습니다.

증상:

```text
API P99 latency 상승
GC 횟수 증가
CPU 사용률 증가
Allocation rate 증가
```

이 경우 단순히 Heap을 늘리는 것만이 아니라

```text
GC Log
JFR
Heap Dump
Allocation Profiling
```

등으로 원인을 찾습니다.

대량의 임시 객체 생성, 불필요한 String 생성, 큰 collection 유지, cache eviction 실패 등을 조사할 수 있습니다.

### 핵심 키워드

`Heap` · `Reachability` · `GC Root` · `Mark-Sweep` · `Mark-Compact` · `Copying` · `Generational GC` · `G1` · `STW` · `Reference Counting` · `Circular Reference`

---

# Part 3. 손코딩

# 10. BST 구현

## 구현

```java
class BST {

    static class Node {
        int value;
        Node left;
        Node right;

        Node(int value) {
            this.value = value;
        }
    }

    Node root;

    // 삽입
    public void insert(int value) {
        root = insert(root, value);
    }

    private Node insert(Node node, int value) {
        if (node == null) {
            return new Node(value);
        }

        if (value < node.value) {
            node.left = insert(node.left, value);
        } else if (value > node.value) {
            node.right = insert(node.right, value);
        }

        return node;
    }

    // 탐색
    public boolean search(int value) {

        Node current = root;

        while (current != null) {

            if (value == current.value) {
                return true;
            }

            if (value < current.value) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        return false;
    }

    // 삭제
    public void delete(int value) {
        root = delete(root, value);
    }

    private Node delete(Node node, int value) {

        if (node == null) {
            return null;
        }

        if (value < node.value) {

            node.left = delete(node.left, value);

        } else if (value > node.value) {

            node.right = delete(node.right, value);

        } else {

            // Case 1. 자식 없음
            if (node.left == null && node.right == null) {
                return null;
            }

            // Case 2. 왼쪽 자식만 있음
            if (node.right == null) {
                return node.left;
            }

            // Case 2. 오른쪽 자식만 있음
            if (node.left == null) {
                return node.right;
            }

            // Case 3. 자식 2개
            Node successor = findMin(node.right);

            node.value = successor.value;

            node.right = delete(node.right, successor.value);
        }

        return node;
    }

    private Node findMin(Node node) {

        while (node.left != null) {
            node = node.left;
        }

        return node;
    }

    // 중위 순회
    public void inorder() {
        inorder(root);
        System.out.println();
    }

    private void inorder(Node node) {

        if (node == null) {
            return;
        }

        inorder(node.left);

        System.out.print(node.value + " ");

        inorder(node.right);
    }
}
```

---

# BST 삭제 3가지 경우

## 자식 0개

```text
   5

삭제 5
   ↓

 null
```

그냥 제거합니다.

---

## 자식 1개

```text
    5
     \
      8
```

5를 삭제하면

```text
8
```

자식이 부모의 자리를 대신합니다.

---

## 자식 2개

```text
       5
      / \
     3   8
        / \
       6   9
```

5를 삭제한다면 오른쪽 subtree에서 가장 작은 값인

```text
6
```

을 successor로 사용할 수 있습니다.

```text
       6
      / \
     3   8
          \
           9
```

그리고 원래 6이 있던 node를 삭제합니다.

---

# 왜 편향 트리에서는 O(N)인가?

정상적으로 균형이 잡혀 있으면

```text
       4
     /   \
    2     6
   / \   / \
  1   3 5   7
```

높이는 약

```text
log N
```

입니다.

따라서 Search / Insert / Delete가 평균적으로

```text
O(log N)
```

입니다.

하지만 정렬된 숫자를 순서대로 삽입하면

```text
1
 \
  2
   \
    3
     \
      4
       \
        5
```

Linked List와 비슷한 구조가 됩니다.

높이:

```text
H = N
```

따라서 탐색 역시

```text
O(N)
```

이 됩니다.

---

# 시간복잡도

트리 높이를 `H`라고 하면

```text
Search = O(H)
Insert = O(H)
Delete = O(H)
```

균형 BST:

```text
H ≈ log N

→ O(log N)
```

편향 BST:

```text
H = N

→ O(N)
```

중위 순회:

```text
O(N)
```

---

# 공간복잡도

노드 저장:

```text
O(N)
```

재귀 함수 stack:

```text
O(H)
```

Balanced:

```text
O(log N)
```

Worst Case:

```text
O(N)
```

---

# 면접 추가 답변

이런 문제 때문에 실제로는 AVL Tree나 Red-Black Tree와 같은 **Self-Balancing BST**를 사용할 수 있습니다.

예를 들어 Java의 `TreeMap`, `TreeSet`은 Red-Black Tree 계열 자료구조를 사용하여 연산의 `O(log N)` 성능을 보장하도록 설계되어 있습니다.

**시간복잡도** → 평균 `O(log N)`, 최악 `O(N)`  
**공간복잡도** → Tree `O(N)`, recursion `O(H)`

---

# 11. 2,000,000자리 숫자 여러 개를 모두 더해야 한다면?

## 핵심 아이디어

일반 정수형에 넣을 수 없습니다.

```text
long 최대 약 19자리

문제 숫자 = 2,000,000자리
```

따라서 숫자를 **문자열로 받아 각 자릿수를 직접 더해야 합니다.**

학교/면접 문제에서는 BigInteger를 직접 사용하는 것보다 arbitrary precision addition을 구현하는 것이 핵심입니다.

---

# 면접용 설명

각 숫자를 String으로 입력받고 맨 뒤 자리부터 한 자리씩 더합니다.

각 자리의 합과 carry만 저장하면 되기 때문에 숫자 전체를 primitive integer에 변환할 필요가 없습니다.

예를 들어

```text
  999
+ 123
-----
```

오른쪽부터 계산합니다.

```text
9 + 3 = 12
result = 2
carry = 1

9 + 2 + 1 = 12
result = 2
carry = 1

9 + 1 + 1 = 11
result = 1
carry = 1
```

마지막 carry를 붙여

```text
1122
```

가 됩니다.

---

# Java 코드

```java
import java.io.*;
import java.util.*;

public class Main {

    static String add(String a, String b) {

        int i = a.length() - 1;
        int j = b.length() - 1;

        int carry = 0;

        StringBuilder sb = new StringBuilder(
            Math.max(a.length(), b.length()) + 1
        );

        while (i >= 0 || j >= 0 || carry > 0) {

            int sum = carry;

            if (i >= 0) {
                sum += a.charAt(i--) - '0';
            }

            if (j >= 0) {
                sum += b.charAt(j--) - '0';
            }

            sb.append(sum % 10);

            carry = sum / 10;
        }

        return sb.reverse().toString();
    }

    public static void main(String[] args) throws Exception {

        BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));

        int N = Integer.parseInt(br.readLine());

        String answer = "0";

        for (int i = 0; i < N; i++) {

            String number = br.readLine();

            answer = add(answer, number);
        }

        System.out.println(answer);
    }
}
```

---

# 왜 overflow가 발생하지 않는가?

각 단계에서 더하는 값은 최대

```text
9 + 9 + 1 = 19
```

입니다.

따라서 2,000,000자리 숫자라도 한 번에 전체 값을 primitive integer에 저장하는 것이 아니라 한 자리씩 처리하므로 overflow가 발생하지 않습니다.

---

# 시간복잡도

숫자가

```text
K개
```

이고 각 숫자의 길이가 대략

```text
D
```

라면 각 덧셈이 `O(D)` 수준이고 이를 K개 처리하므로 대략

```text
O(KD)
```

입니다.

정확하게는 누적 결과의 자릿수가 `log K`만큼 증가할 수 있으므로

```text
O(K × (D + log K))
```

정도로 볼 수 있습니다.

입력 데이터 자체가 `K × D`자리이므로 적어도 모든 입력을 한 번은 읽어야 합니다.

---

# 공간복잡도

현재 누적 결과와 입력 숫자 하나를 저장하면 되므로 대략

```text
O(D + log K)
```

입니다.

모든 숫자를 배열에 저장할 필요가 없습니다.

```text
입력 1개
 ↓
answer에 합산
 ↓
버림

다음 입력
 ↓
answer에 합산
```

처럼 streaming 처리할 수 있습니다.

---

# 더 최적화한다면?

실제 arbitrary precision library에서는 한 자리씩 저장하기보다 여러 decimal digit을 하나의 limb에 저장하는 방식 등을 사용할 수 있습니다.

예:

```text
Base = 1,000,000,000
```

이라고 하면

```text
123456789012345678

→

[123456789][012345678]
```

처럼 9자리씩 처리할 수 있습니다.

그러면 반복 횟수를 크게 줄일 수 있습니다.

실무에서는 직접 구현하기보다는

```java
BigInteger
```

나 언어에서 검증된 arbitrary precision library를 사용하는 것이 안전합니다.

하지만 이 문제의 제한 조건에서는 **String + Carry를 이용한 큰 정수 덧셈 구현**이 핵심입니다.

**시간복잡도** → `O(KD)`  
**공간복잡도** → `O(D + log K)`

---

# Week 05 면접 직전 최종 암기표

| 개념 | 반드시 말해야 할 한 문장 |
|---|---|
| Cache | CPU-RAM 속도 차이를 줄이며 지역성을 이용한다 |
| Locality | 시간 지역성 + 공간 지역성 |
| Thrashing | Working Set 부족으로 Page Fault/Page 교체가 반복되는 상태 |
| Virtual Memory | VA를 PA에 매핑하여 독립적인 주소 공간을 제공한다 |
| Page Fault | 현재 mapping으로 메모리 접근을 완료하지 못해 OS 처리가 필요한 fault |
| Paging | 고정 크기 Page ↔ Frame |
| Segmentation | 논리적 의미에 따른 가변 크기 Segment |
| TLB | 최근 VA→PA 변환 결과를 저장하는 캐시 |
| TLB Miss | Page Fault와 같지 않음 |
| LRU | 시간 지역성을 이용 |
| FD | 프로세스의 열린 I/O 객체를 가리키는 작은 정수 handle |
| inode | 파일의 metadata를 나타내는 filesystem 객체 |
| Page Cache | Kernel이 파일 데이터를 RAM에 caching |
| GC | reachable하지 않은 Heap 객체를 찾아 회수 |
| G1 | Region 기반, generational, mostly-concurrent GC |
| BST | 연산 O(H), 균형이면 O(log N), 편향이면 O(N) |
| 큰 수 덧셈 | String으로 입력받아 뒤에서부터 carry 계산 |

---

# 반드시 구분해야 하는 함정 7개

### 1.

```text
TLB Miss ≠ Page Fault
```

TLB miss가 나도 page table에 정상 mapping이 있으면 page fault 없이 끝납니다.

### 2.

```text
Virtual Memory ≠ Swap
```

Swap은 가상 메모리를 구현하는 데 활용될 수 있는 메커니즘 중 하나일 뿐입니다.

### 3.

```text
Page ≠ Frame
```

Page는 virtual memory 단위, Frame은 physical memory 단위입니다.

### 4.

```text
Segmentation Fault
≠
Segmentation 방식에서만 발생하는 오류
```

현대 OS에서는 잘못된 virtual memory 접근이라는 의미로 이해하는 것이 적절합니다.

### 5.

```text
32bit = 물리 RAM 무조건 4GB 제한
```

이라고 단정하면 안 됩니다.

정확한 핵심은 32bit 주소가 `2^32`개의 byte 주소를 표현한다는 것입니다.

### 6.

```text
GC = reference count 0인 객체 삭제
```

가 아닙니다.

Java GC는 기본적으로 **reachability 기반 tracing GC**입니다.

### 7.

```text
File Descriptor = File
```

가 아닙니다.

FD는 file, socket, pipe 등 열린 kernel I/O object를 가리키기 위한 **프로세스-local 정수 handle**입니다.

---

# 이 주제 전체를 하나로 연결하면

이번 주 내용은 사실 다음 하나의 흐름으로 연결됩니다.

```text
프로그램
   │
   │ Virtual Address
   ▼
CPU
   │
   ├─ Cache
   │
   └─ TLB
       │
       ▼
      MMU
       │
       ▼
  Page Table
       │
       ▼
Physical Memory
       │
       ├─ Anonymous Memory
       │
       └─ Page Cache
               │
               ▼
           File System
               │
               ▼
             SSD
```

CPU가 데이터를 요청하면 먼저 cache locality가 성능에 영향을 줍니다.

주소는 MMU와 TLB를 통해 virtual address에서 physical address로 변환됩니다.

필요한 page가 RAM에 없다면 Page Fault가 발생하고 OS가 이를 처리합니다.

RAM이 부족하면 page replacement가 수행되며, working set을 감당하지 못하면 Thrashing이 발생할 수 있습니다.

파일을 읽는 경우 애플리케이션은 File Descriptor를 이용해 kernel의 파일 객체에 접근하고, 일반적인 Linux Buffered I/O에서는 파일 데이터가 Page Cache를 거칩니다.

Java와 같은 managed runtime 위에서는 이 메모리 계층 위에 JVM Heap이 존재하며, Heap 안의 객체 생명주기는 GC가 관리합니다.

즉 Week 05의 핵심은 결국

> **"CPU가 만든 가상 주소가 실제 데이터에 도달하기까지 어떤 계층을 거치며, 각 계층은 어떻게 성능과 메모리 안정성을 확보하는가?"**

로 정리할 수 있습니다.
