# 1장: 소개 (Introduction)

## 학습 목표

이 장을 완료하면 다음을 할 수 있습니다:
- Java 개발 환경을 설정하고 JavaFX 프로젝트를 구성할 수 있습니다
- 객체 지향 프로그래밍의 기본 개념을 이해합니다
- JUnit을 사용한 단위 테스트를 작성할 수 있습니다
- JavaFX의 기본 구조와 Scene Graph를 이해합니다
- 패키지 구조와 프로젝트 조직화의 중요성을 설명할 수 있습니다

## 핵심 개념

### 1.1 객체 지향 프로그래밍 기초

**클래스(Class)란 무엇인가?**

클래스는 객체를 만들기 위한 설계도 또는 틀입니다. 붕어빵 틀과 붕어빵의 관계로 생각하면 쉽습니다.
- **붕어빵 틀** = 클래스 (Class)
- **실제 붕어빵** = 객체 (Object)

**클래스의 구성 요소**

1. **필드(Field)**: 객체의 속성을 저장하는 변수
   - 예: 공의 x좌표, y좌표, 반지름

2. **생성자(Constructor)**: 객체를 생성할 때 호출되는 특별한 메서드
   - 객체의 초기값을 설정
   - 클래스 이름과 동일한 이름을 가짐

3. **메서드(Method)**: 객체의 동작을 정의하는 함수
   - 객체가 할 수 있는 행동들
   - 예: 위치 가져오기, 이동하기

**Ball 클래스 구조 예시**
```java
// Ball 클래스의 기본 구조
public class Ball {
    // 필드 선언 방법
    private double x;        // x 좌표
    private double y;        // y 좌표
    private double radius;   // 반지름
    
    // 생성자 예시
    public Ball(double x, double y, double radius) {
        // this는 현재 객체를 가리킴
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    // Getter 메서드 패턴
    public double getX() { 
        return x; 
    }
    
    // TODO: 나머지 getter 메서드 구현
}
```

**클래스 사용 예시**
```java
// 객체 생성 방법
Ball myBall = new Ball(100, 200, 50);

// 메서드 호출 방법
double xPosition = myBall.getX();
System.out.println("X 좌표: " + xPosition);
```

**접근 제어자(Access Modifier)**
- `private`: 클래스 내부에서만 접근 가능
- `public`: 어디서든 접근 가능
- 필드는 보통 `private`로 선언하여 외부에서 직접 수정하지 못하게 보호

### 1.2 JavaFX 기본 구조

**JavaFX 애플리케이션의 기본 구조**
```java
import javafx.application.Application;
import javafx.stage.Stage;

public class GameApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // TODO: 여기에 JavaFX UI 구성 코드 작성
        // 1. Canvas 생성 (800 x 600)
        // 2. GraphicsContext 가져오기
        // 3. Scene 만들기
        // 4. Stage 설정하기
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

**JavaFX 핵심 객체:**
- **Stage**: 윈도우 (최상위 컨테이너)
- **Scene**: 장면 (콘텐츠 컨테이너)
- **Canvas**: 그림을 그릴 도화지
- **GraphicsContext**: 그리기 도구

### 1.3 프로젝트 구조

```
cannongame/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── nhnacademy/
│   │               └── cannongame/
│   │                   ├── Ball.java        # 공 클래스
│   │                   ├── World.java       # 공을 관리하는 세계
│   │                   └── GameApp.java     # 메인 실행 클래스
│   └── test/
│       └── java/
│           └── com/
│               └── nhnacademy/
│                   └── cannongame/
│                       ├── BallTest.java    # Ball 클래스 테스트
│                       └── WorldTest.java   # World 클래스 테스트
└── pom.xml                                  # Maven 설정 파일
```

**각 클래스의 역할**

1. **Ball.java**: 게임에서 사용할 공 객체
   - 위치(x, y)와 크기(radius) 정보 저장
   - 공의 속성을 가져오는 메서드 제공

2. **World.java**: 여러 개의 공을 관리하는 컨테이너
   - 공을 추가, 삭제하는 기능
   - 화면에 모든 공을 그리는 기능
   - JavaFX의 Pane을 상속받아 구현

3. **GameApp.java**: 프로그램의 시작점
   - JavaFX Application을 상속
   - 윈도우 생성 및 게임 시작
   - main 메서드 포함

**프로젝트 구조 참고 자료:**
- [Maven 표준 디렉토리 구조](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)
- [Java 패키지 명명 규칙](https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html)

### 1.4 JUnit 테스트 기초

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class BallTest {
    private Ball ball;
    
    @BeforeEach
    public void setUp() {
        ball = new Ball(100, 100, 20);
    }
    
    @Test
    public void testConstructor() {
        assertEquals(100, ball.getX());
        assertEquals(100, ball.getY());
        assertEquals(20, ball.getRadius());
    }
    
    @Test
    public void testInvalidRadius() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Ball(0, 0, -5));
    }
}
```

**JUnit 학습 자료:**
- [JUnit 5 공식 문서](https://junit.org/junit5/docs/current/user-guide/)
- [JUnit 어노테이션 정리](https://www.baeldung.com/junit-5-annotations)
- [@BeforeEach와 @AfterEach 사용법](https://howtodoinjava.com/junit5/before-each-after-each/)
- [Assertions 메서드 가이드](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html)

## 실습 과제

### Lab 1-1: 개발 환경 설정
1. JDK 11 이상 설치
2. IDE 설정 (IntelliJ IDEA 권장)
3. JavaFX SDK 다운로드 및 설정
4. Maven 프로젝트 생성

**체크리스트:**
- [ ] Java 버전 확인: `java -version`
- [ ] JavaFX 라이브러리 추가
- [ ] 첫 JavaFX 프로그램 실행

**참고 자료:**
- [JDK 11 설치 가이드 (Oracle)](https://docs.oracle.com/en/java/javase/11/install/overview-jdk-installation.html)
- [OpenJDK 11 설치 (대안)](https://adoptopenjdk.net/)
- [IntelliJ IDEA 설치 및 설정](https://www.jetbrains.com/idea/download/)
- [JavaFX 시작하기 가이드](https://openjfx.io/openjfx-docs/)
- [Maven 프로젝트 생성 튜토리얼](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)

### Lab 1-2: 첫 번째 클래스 작성
`Point` 클래스를 작성하세요:
- x, y 좌표를 저장
- 생성자와 getter 메서드 구현
- 두 점 사이의 거리를 계산하는 메서드 추가

```java
public class Point {
    // TODO: 구현하기
    
    public double distanceTo(Point other) {
        // TODO: 피타고라스 정리를 사용하여 거리 계산
    }
}
```

**참고 자료:**
- [Java 클래스와 객체 기초](https://docs.oracle.com/javase/tutorial/java/javaOO/classes.html)
- [생성자와 메서드 작성법](https://docs.oracle.com/javase/tutorial/java/javaOO/constructors.html)
- [피타고라스 정리와 거리 계산](https://www.khanacademy.org/math/geometry/hs-geo-analytic-geometry/hs-geo-distance-and-midpoints/a/distance-formula)

### Lab 1-3: JUnit 테스트 작성
Lab 1-2에서 작성한 `Point` 클래스에 대한 테스트를 작성하세요:
- 생성자 테스트
- 거리 계산 테스트
- 예외 상황 테스트 (null 입력 등)

**참고 자료:**
- [JUnit 5 시작하기 (한글)](https://steady-coding.tistory.com/349)
- [JUnit 5 기본 Assertions](https://junit.org/junit5/docs/current/user-guide/#writing-tests-assertions)
- [JUnit 5 @Test 어노테이션 가이드](https://www.baeldung.com/junit-5-test-annotation)
- [예외 테스트 작성법](https://www.baeldung.com/junit-assert-exception)
- [Maven에 JUnit 5 추가하기](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html)

### Lab 1-4: 간단한 JavaFX 프로그램
화면에 원을 그리는 JavaFX 프로그램을 작성하세요:
- 800x600 크기의 윈도우 생성
- 화면 중앙에 빨간색 원 그리기
- 제목 표시: "My First JavaFX Game"

**참고 자료:**
- [JavaFX Canvas 튜토리얼](https://docs.oracle.com/javafx/2/canvas/jfxpub-canvas.htm)
- [JavaFX Application 구조](https://openjfx.io/javadoc/17/javafx.graphics/javafx/application/Application.html)
- [GraphicsContext로 도형 그리기](https://www.tutorialspoint.com/javafx/javafx_2d_shapes.htm)
- [JavaFX 프로젝트 설정 문제 해결](https://stackoverflow.com/questions/tagged/javafx)

## 자가 평가 문제

1. **객체와 클래스의 차이점은 무엇인가요?**
   - 클래스는 설계도, 객체는 실제 인스턴스

2. **JavaFX의 Scene Graph란 무엇인가요?**
   - UI 요소들의 계층적 구조

3. **JUnit 테스트를 작성하는 이유는 무엇인가요?**
   - 코드의 정확성 검증
   - 리팩토링 시 안정성 보장
   - 문서화 효과

4. **생성자(Constructor)의 역할은 무엇인가요?**
   - 객체 초기화
   - 유효성 검사
   - 필수 값 설정

## 자주 하는 실수와 해결 방법

### 1. JavaFX 모듈 경로 오류
**문제:** "JavaFX runtime components are missing"
```bash
# 해결: VM 옵션 추가
--module-path /path/to/javafx/lib --add-modules javafx.controls
```

### 2. NullPointerException
**문제:** 초기화하지 않은 객체 사용
```java
// 잘못된 코드
private Ball ball;
public void draw() {
    ball.getX(); // NullPointerException!
}

// 올바른 코드
private Ball ball = new Ball(0, 0, 10);
```

### 3. 접근 제어자 실수
**문제:** private 필드에 직접 접근
```java
// 잘못된 코드
Ball ball = new Ball(0, 0, 10);
ball.x = 100; // 컴파일 오류!

// 올바른 코드
ball.setX(100); // setter 메서드 사용
```

## 다음 장 미리보기

2장에서는 `Ball` 클래스를 구현하고 화면에 그리는 방법을 배웁니다:
- `Ball` 클래스 설계와 구현
- JavaFX Canvas에 도형 그리기
- 색상과 스타일 적용
- 여러 개의 공 관리

## 추가 학습 자료

### 기본 개념 학습
- [Java 프로그래밍 기초 (Oracle 공식 튜토리얼)](https://docs.oracle.com/javase/tutorial/)
- [객체 지향 프로그래밍 개념](https://www.geeksforgeeks.org/object-oriented-programming-oops-concept-in-java/)
- [Java 코딩 규칙 가이드](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html)

### JavaFX 관련
- [JavaFX 공식 문서](https://openjfx.io/javadoc/17/)
- [JavaFX 튜토리얼 시리즈](https://jenkov.com/tutorials/javafx/index.html)
- [JavaFX 예제 모음](https://github.com/openjfx/samples)

### 테스팅 관련
- [JUnit 5 사용자 가이드](https://junit.org/junit5/docs/current/user-guide/)
- [단위 테스트 작성 베스트 프랙티스](https://phauer.com/2019/modern-best-practices-testing-java/)
- [Mockito 프레임워크 가이드](https://site.mockito.org/)

### 도구 관련
- [Maven 공식 가이드](https://maven.apache.org/guides/index.html)
- [IntelliJ IDEA 단축키 모음](https://resources.jetbrains.com/storage/products/intellij-idea/docs/IntelliJIDEA_ReferenceCard.pdf)
- [Git 기초 학습](https://git-scm.com/book/ko/v2)

### 추천 도서
- [Effective Java 3rd Edition](https://www.oreilly.com/library/view/effective-java/9780134686097/) - Joshua Bloch
- [Clean Code](https://www.oreilly.com/library/view/clean-code-a/9780136083238/) - Robert C. Martin
- [Head First Design Patterns](https://www.oreilly.com/library/view/head-first-design/9781492077992/) - Eric Freeman

## 학습 체크포인트

- [ ] Java와 JavaFX 개발 환경을 설정했습니다
- [ ] 클래스와 객체의 개념을 이해했습니다
- [ ] 첫 JavaFX 프로그램을 실행했습니다
- [ ] JUnit 테스트를 작성할 수 있습니다
- [ ] 프로젝트 구조를 이해했습니다