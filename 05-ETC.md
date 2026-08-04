## 개발상식, 기타

<details>
  <summary><h3>1. 가상화가 무엇이고, 이것이 가상머신과 어떠한 차이가 있는지 설명해 주세요.</h3></summary>
<ul>
<li> 그렇다면 Docker는 둘 중 어디에 속하나요? 왜 사람들이 Docker를 많이 채택할까요?</li>
<li> 하나의 Host OS에서 돌아간다면 충분히 한 컨테이너가 다른 컨테이너에 간섭할 수 있는 위험이 있지 않을까요? 이를 어떻게 방어할 수 있을까요?</li>
<li> Docker 위에 Docker를 올릴 순 없을까요?</li>
</ul>
</details>

<details>
  <summary><h3>2. CI/CD 를 사용해 본 경험이 있나요? 있다면 간단하게 설명해 주세요.</h3></summary>
<ul>
<li> 소스코드를 작성하고 나서, 실제 사용자에게 배포되기까지의 과정을 순서대로 설명해 주세요.</li>
<li> 빌드와 배포는 어떻게 다른가요?</li>
<li> CI와 CD는 각각 무엇을 자동화하는 것인가요? CD의 Delivery와 Deployment는 어떤 차이가 있나요?</li>
<li> 파이프라인의 어느 단계에 테스트를 넣는 것이 좋을까요? 그 이유는 무엇인가요?</li>
<li> 무중단 배포는 어떻게 구현할 수 있을까요? (Rolling, Blue-Green, Canary)</li>
<li> 배포한 버전에 장애가 발생했을 때, 롤백은 어떤 방식으로 준비해 둘 수 있을까요?</li>
<li> 빌드 결과물을 이미지로 만들어 배포하는 방식은, 서버에서 직접 빌드하는 방식과 비교해 어떤 장점이 있을까요?</li>
</ul>
</details>

<details>
  <summary><h3>3. 테스트 코드를 왜 작성하나요? 단위 테스트, 통합 테스트, E2E 테스트의 차이에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 개발 과정에서 테스트는 언제 작성하는 것이 좋을까요? 구현 전과 구현 후는 어떤 차이가 있나요?</li>
<li> TDD가 무엇인지 설명하고, 장점과 현실적인 한계에 대해 설명해 주세요.</li>
<li> 본인이 사용하는 언어와 프레임워크에서는 어떤 테스트 도구를 사용하나요? (ex. JUnit, Mockito, Testcontainers)</li>
<li> 어떤 대상을 Mocking 하고, 어떤 대상은 실제 객체를 쓰는 것이 좋을까요? 그 판단 기준은 무엇인가요?</li>
<li> 테스트 커버리지 100%는 좋은 목표일까요? 커버리지가 높은데도 버그가 나는 경우는 어떤 경우인가요?</li>
<li> 테스트하기 어려운 코드는 어떤 특징을 갖고 있나요? 이를 어떻게 개선할 수 있을까요?</li>
<li> 외부 API나 시간(현재 시각)에 의존하는 로직은 어떻게 테스트할 수 있을까요?</li>
<li> 테스트가 가끔씩만 실패하는 경우(Flaky Test), 어떤 원인을 의심해 볼 수 있을까요?</li>
</ul>
</details>

<details>
  <summary><h3>4. static 키워드는 어떤 의미를 갖나요? (본인이 사용하는 언어에서 없다면 패스...)</h3></summary>
<ul>
<li> 컴파일 할 때, static 키워드가 붙은 변수, 함수는 어떻게 처리되나요?</li>
<li> Java에서 static과 static final은 어떤 차이를 갖나요? final과 static final은요? </li>
</ul>
</details>

<details>
  <summary><h3>5. 객체지향 프로그래밍이 무엇인가요?</h3></summary>
<ul>
<li> SOLID 원칙에 대해 설명해 주세요.</li>
<li> 다형성이 무엇인지 설명하고, 동적 다형성과 정적 다형성이 무엇인지 설명해 주세요.</li>
<li> 오버로딩과 오버라이딩의 차이에 대해 설명해 주세요.</li>
<li> 상속이 무엇인지 설명하고, 상속을 사용했을 때 얻는 것과 잃는 것을 설명해 주세요.</li>
<li> "상속보다 합성(Composition)을 사용하라"는 말이 있습니다. 왜 그럴까요?</li>
<li> 캡슐화와 정보은닉은 어떤 차이가 있나요? 접근제어자만 붙이면 캡슐화가 되는 걸까요?</li>
<li> 다형성이 실제 코드에서 어떻게 쓰이는지, 본인의 언어를 기준으로 예시를 들어 설명해 주세요.</li>
<li> 클래스가 있는 언어는 반드시 객체지향 언어라고 할 수 있을까요? 그 반대는 성립하나요?</li>
</ul>
</details>

<details>
  <summary><h3>6. 프레임워크와 라이브러리의 차이에 대해 설명해 주세요.</h3></summary>
<ul>
</ul>
</details>

<details>
  <summary><h3>7. Call By Value와 Call By Reference의 차이를 본인의 언어를 기반으로 설명해 주세요.</h3></summary>
<ul>
<li> 사실 이 질문에는 약간의 낚시가 있습니다. 과연 모든 언어에 저 개념이 존재할까요?</li>
</ul>
</details>

<details>
  <summary><h3>8. 순수함수가 무엇인지를 함수형 프로그래밍 매커니즘과 연관지어 설명해 주세요.</h3></summary>
<ul>
<li> Side Effect가 무엇인가요? 이를 모두 없애는 프로그래밍이 이상적이라고 할 수 있을까요?</li>
<li> 왜 함수형 프로그래밍 매커니즘을 사용한다고 생각하시나요?</li>
<li> 순수함수는 Thread Safe 한가요? 왜 그럴까요?</li>
<li> 고차함수에 대해 설명해 주세요.</li>
</ul>
</details>

<details>
  <summary><h3>9. MVC 패턴이 무엇인가요?</h3></summary>
<ul>
<li> 다른 아키텍쳐 패턴은 없나요? MVC랑 비교해서 어떤 차이가 있나요?</li>
</ul>
</details>

<details>
  <summary><h3>10. 디자인 패턴이 무엇인지 설명해주고, 대표적인 디자인 패턴에 대해 설명해 주세요.</h3></summary>
<ul>
<li> Singleton의 장단점에 대해 설명해 주세요.</li>
<li> Singleton이 하나의 객체를 생성한다는 것을 어떻게 보장할 수 있을까요?</li>
</ul>
</details>

<details>
  <summary><h3>11. GC에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 본인이 사용하는 언어에서는 GC를 어떻게 구현했나요?</li>
<li> GC의 장단점에 대해 설명해 주세요.</li>
<li> GC는 어떤 영역에 있는 데이터를 관리하나요?</li>
<li> Reference Counting 방식에 대해 설명하고, 이 알고리즘에서 발생할 수 있는 순환 참조 및 Retain Cycle에 대해 설명해 주세요.</li>
</ul>
</details>

<details>
  <summary><h3>12. 32비트와 64비트의 차이는 무엇인가요?</h3></summary>
<ul>
<li> 32비트에서 가용한 메모리의 크기는 최대 4GB라고 하는데, 왜 그런걸까요?</li>
</ul>
</details>

<details>
  <summary><h3>13. 인증과 인가의 차이에 대해 설명해 주세요.</h3></summary>
<ul>
<li> OAuth가 무엇인지 설명하고, 이것은 인증인지 인가인지에 대해 설명해 주세요.</li>
</ul>
</details>

<details>
  <summary><h3>14. JWT 인증 방식이 무엇인가요?</h3></summary>
<ul>
<li> Signature는 어떻게 만들어지나요?</li>
<li> 만약 Access Token이 탈취되면, 어떻게 대응할 수 있을까요?</li>
<li> 반대로 Refresh Token이 탈취되면, 어떻게 대응해야 할까요?</li>
</ul>
</details>

<details>
  <summary><h3>15. 암호화 알고리즘에 대해 설명해 주세요.</h3></summary>
<ul>
</details>

<details>
  <summary><h3>16. 문자열 인코딩에 대해 설명해 주세요.</h3></summary>
<ul>
<li> Base64 인코딩은 일반적인 문자열 인코딩과는 달리, 사용자가 읽기 어려운 알파벳과 숫자 조합으로 변경합니다. 이를 사용하는 이유는 무엇일까요?</li>
</details>

<details>
  <summary><h3>17. Git에 대해 설명해 주세요.</h3></summary>
<ul>
<li> 여러 브랜치를 합쳐야 할 때, 어떤 방법을 사용할 수 있는지 "모두" 설명해 주세요.</li>
<li> Merge와 Rebase는 각각 커밋 히스토리에 어떤 차이를 남기나요?</li>
</ul>
</details>