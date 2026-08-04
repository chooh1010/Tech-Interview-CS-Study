## Java, Spring

<details>
  <summary><h3>1. JVM이 정확히 무엇이고, 어떤 기능을 하는지 설명해 주세요.</h3></summary>
<ul>
<li> 그럼, 자바 말고 다른 언어는 JVM 위에 올릴 수 없나요?</li>
<li> 반대로 JVM 계열 언어를 일반적으로 컴파일해서 사용할 순 없나요?</li>
<li> VM을 사용함으로써 얻을 수 있는 장점과 단점에 대해 설명해 주세요.</li>
<li> JVM과 내부에서 실행되고 있는 프로그램은 부모 프로세스 - 자식 프로세스 관계를 갖고 있다고 봐도 무방한가요?</li>
</ul>
</details>

<details>
  <summary><h3>2. Java 8과 Java 17의 주요 차이에 대해 설명해 주세요.</h3></summary>
<ul>
<li> Java 8에서 추가된 대표적인 기능(람다, Stream, Optional, 인터페이스 default 메서드)은 각각 어떤 문제를 해결하기 위해 등장했나요?</li>
<li> LTS 버전이 무엇이고, 실무에서 왜 LTS 버전을 선호할까요?</li>
<li> var, record, sealed class, switch 표현식은 각각 어떤 상황에서 유용한가요?</li>
<li> record는 기존 DTO 클래스와 비교해 어떤 점이 다른가요? 어떤 경우에는 쓰면 안 될까요?</li>
<li> Java 8과 17은 기본 GC가 다릅니다. 무엇에서 무엇으로 바뀌었고, 왜 그런 선택을 했을까요?</li>
<li> 운영 중인 서비스의 Java 버전을 8에서 17로 올린다면, 어떤 점을 확인해야 할까요?</li>
</ul>
</details>

<details>
  <summary><h3>3. final 키워드를 사용하면, 어떤 이점이 있나요?</h3></summary>
<ul>
<li> 그렇다면 컴파일 과정에서, final 키워드는 다르게 취급되나요?</li>
</ul>
</details>

<details>
  <summary><h3>4. 인터페이스와 추상 클래스의 차이에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 왜 클래스는 단일 상속만 가능한데, 인터페이스는 2개 이상 구현이 가능할까요?</li>
</ul>
</details>

<details>
  <summary><h3>5. 리플렉션에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 의미만 들어보면 리플렉션은 보안적인 문제가 있을 가능성이 있어보이는데, 실제로 그렇게 생각하시나요? 만약 그렇다면, 어떻게 방지할 수 있을까요?</li>
<li> 리플렉션을 언제 활용할 수 있을까요?</li>
</ul>
</details>

<details>
  <summary><h3>6. static class와 static method를 비교해 주세요.</h3></summary>
<ul>
<li> static 을 사용하면 어떤 이점을 얻을 수 있나요? 어떤 제약이 걸릴까요?</li>
<li> 컴파일 과정에서 static 이 어떻게 처리되는지 설명해 주세요.</li>
</ul>
</details>

<details>
  <summary><h3>7. Java의 Exception에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 예외처리를 하는 세 방법에 대해 설명해 주세요.</li>
<li> CheckedException, UncheckedException 의 차이에 대해 설명해 주세요.</li>
<li> 예외처리가 성능에 큰 영향을 미치나요? 만약 그렇다면, 어떻게 하면 부하를 줄일 수 있을까요?</li>
</ul>
</details>

<details>
  <summary><h3>8. Synchronized 키워드에 대해 설명해 주세요.</h3></summary>
<ul>
<li> Synchronized 키워드가 어디에 붙는지에 따라 의미가 약간씩 변화하는데, 각각 어떤 의미를 갖게 되는지 설명해 주세요.</li>
<li> 효율적인 코드 작성 측면에서, Synchronized는 좋은 키워드일까요?</li>
<li> Synchronized 를 대체할 수 있는 자바의 다른 동기화 기법에 대해 설명해 주세요.</li>
<li> Thread Local에 대해 설명해 주세요.</li>
</ul>
</details>

<details>
  <summary><h3>9. Java Collection Framework의 구조와, List / Set / Map의 차이에 대해 설명해 주세요.</h3></summary>
<ul>
<li> Collection 인터페이스를 상속받지 않는 Map은, 왜 Collection Framework의 일부로 묶여 있을까요?</li>
<li> Array와 ArrayList의 차이를 설명해 주세요. ArrayList는 어떻게 크기가 늘어나나요?</li>
<li> ArrayList와 LinkedList는 각 연산에서 어떤 시간복잡도 차이를 갖나요? 실무에서는 왜 대부분 ArrayList를 쓸까요?</li>
<li> 정렬되지 않은 List에서 특정 "값"을 찾는 시간복잡도는 어떻게 되나요? 정렬되어 있다면 어떻게 달라지나요?</li>
<li> HashMap의 내부 동작을 설명해 주세요. 해시 충돌이 났을 때는 어떻게 처리되나요?</li>
<li> HashMap, HashTable, ConcurrentHashMap의 차이를 설명해 주세요.</li>
<li> Set은 중복을 어떻게 판단하나요? equals()와 hashCode()를 재정의하지 않으면 어떤 일이 벌어질까요?</li>
<li> 순서를 보장해야 한다면 어떤 구현체를 선택해야 하나요? (ex. LinkedHashMap, TreeMap)</li>
</ul>
</details>

<details>
  <summary><h3>10. Java Stream에 대해 설명해 주세요.</h3></summary>
<ul>
<li> Stream과 for ~ loop의 성능 차이를 비교해 주세요,</li>
<li> Stream은 병렬처리 할 수 있나요?</li>
<li> Stream에서 사용할 수 있는 함수형 인터페이스에 대해 설명해 주세요.</li>
<li> 가끔 외부 변수를 사용할 때, final 키워드를 붙여서 사용하는데 왜 그럴까요? 꼭 그래야 할까요?</li>
</ul>
</details>

<details>
  <summary><h3>11. Java의 GC에 대해 설명해 주세요.</h3></summary>
<ul>
<li> finalize() 를 수동으로 호출하는 것은 왜 문제가 될 수 있을까요?</li>
<li> 어떤 변수의 값이 null이 되었다면, 이 값은 GC가 될 가능성이 있을까요?</li>
</ul>
</details>

<details>
  <summary><h3>12. equals()와 hashcode()에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 본인이 hashcode() 를 정의해야 한다면, 어떤 점을 염두에 두고 구현할 것 같으세요?</li>
<li> 그렇다면 equals() 를 재정의 해야 할 때, 어떤 점을 염두에 두어야 하는지 설명해 주세요.</li>
</ul>
</details>

<details>
  <summary><h3>13. Spring Framework와 Spring Boot의 차이에 대해 설명해 주세요.</h3></summary>
<ul>
<li> Spring Boot가 등장하기 전에는, 어떤 점이 불편했을까요?</li>
<li> 자동 설정(Auto Configuration)은 어떤 원리로 동작하나요? 조건에 따라 설정이 적용되는 방식을 설명해 주세요.</li>
<li> @SpringBootApplication 안에는 어떤 애노테이션들이 묶여 있나요?</li>
<li> starter 의존성은 무엇을 해결해 주나요? 버전 관리는 어떻게 이루어지나요?</li>
<li> 내장 톰캣(Embedded WAS)을 사용하면, 외부 WAS에 배포하는 방식과 비교해 어떤 장단점이 있나요?</li>
<li> 자동 설정된 Bean을 직접 만든 Bean으로 덮어쓰려면 어떻게 해야 할까요?</li>
<li> Spring Boot는 Spring Framework를 대체하는 것인가요? 그렇지 않다면, 둘의 관계를 어떻게 설명하시겠어요?</li>
</ul>
</details>

<details>
  <summary><h3>14. IoC와 DI에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 후보 없이 특정 기능을 하는 클래스가 딱 한 개하면, 구체 클래스를 그냥 사용해도 되지 않나요? 그럼에도 불구하고 왜 Spring에선 Bean을 사용 할까요?</li>
<li> Spring의 Bean 생성 주기에 대해 설명해 주세요.</li>
<li> 프로토타입 빈은 무엇인가요?</li>
</ul>
</details>

<details>
  <summary><h3>15. AOP에 대해 설명해 주세요.</h3></summary>
<ul>
<li> @Aspect는 어떻게 동작하나요?</li>
</ul>
</details>

<details>
  <summary><h3>16. Spring 에서 Interceptor와 Servlet Filter에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 설명만 들어보면 인터셉터만 쓰는게 나아보이는데, 아닌가요? 필터는 어떤 상황에 사용 해야 하나요?</li>
</ul>
</details>

<details>
  <summary><h3>17. DispatcherServlet 의 역할에 대해 설명해 주세요.</h3></summary>
<ul>
<li>여러 요청이 들어온다고 가정할 때, DispatcherServlet은 한번에 여러 요청을 모두 받을 수 있나요?</li>
<li>수많은 @Controller 를 DispatcherServlet은 어떻게 구분 할까요?</li>
</ul>
</details>

<details>
  <summary><h3>18. JPA와 같은 ORM을 사용하는 이유가 무엇인가요?</h3></summary>
<ul>
<li> 영속성은 어떤 기능을 하나요? 이게 진짜 성능 향상에 큰 도움이 되나요?</li>
<li> N + 1 문제에 대해 설명해 주세요.</li>
</ul>
</details>

<details>
  <summary><h3>19. @Transactional 은 어떤 기능을 하나요?</h3></summary>
<ul>
<li> @Transactional(readonly=true) 는 어떤 기능인가요? 이게 도움이 되나요?</li>
<li> 그런데, 읽기에 트랜잭션을 걸 필요가 있나요? @Transactional을 안 붙이면 되는거 아닐까요?</li>
</ul>
</details>


<details>
  <summary><h3>20. Java 에서 Annotation 은 어떤 기능을 하나요?</h3></summary>
<ul>
<li> 별 기능이 없는 것 같은데, 어떻게 Spring 에서는 Annotation 이 그렇게 많은 기능을 하는 걸까요?</li>
<li> Lombok의 @Data를 잘 사용하지 않는 이유는 무엇일까요?</li>
</ul>
</details>

<details>
  <summary><h3>21. Tomcat이 정확히 어떤 역할을 하는 도구인가요?</h3></summary>
<ul>
<li> 혹시 Netty에 대해 들어보셨나요? 왜 이런 것을 사용할까요?</li>
</ul>
</details>
