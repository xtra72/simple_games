# Lab 실습 참고 자료 모음

## Lab 1-1: 개발 환경 설정 상세 가이드

### JDK 설치
1. **Windows**
   - [Oracle JDK 11 다운로드](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
   - [OpenJDK 11 (무료)](https://adoptopenjdk.net/)
   - 환경 변수 설정: `JAVA_HOME`, `PATH`

2. **macOS**
   - Homebrew 사용: `brew install openjdk@11`
   - 또는 [Adoptium](https://adoptium.net/) 에서 다운로드

3. **Linux**
   - Ubuntu/Debian: `sudo apt install openjdk-11-jdk`
   - Fedora: `sudo dnf install java-11-openjdk-devel`

### JavaFX 설정
```xml
<!-- pom.xml에 추가 -->
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-graphics</artifactId>
        <version>17.0.2</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>com.nhnacademy.cannongame.Main</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### IntelliJ IDEA JavaFX 설정
1. File → Project Structure → Libraries
2. '+' 버튼 → Java → JavaFX lib 폴더 선택
3. Run Configuration → VM options:
   ```
   --module-path "경로/javafx-sdk-17/lib" --add-modules javafx.controls,javafx.fxml
   ```

## Lab 1-2: Point 클래스 구현 힌트

### 거리 계산 공식
```java
// 두 점 (x1, y1)과 (x2, y2) 사이의 거리
double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
```

### 완성된 예제 (참고용)
```java
public class Point {
    private final double x;
    private final double y;
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double distanceTo(Point other) {
        if (other == null) {
            throw new NullPointerException("Other point cannot be null");
        }
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
```

## Lab 1-3: JUnit 테스트 예제

### Maven 의존성 추가
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
```

### 테스트 클래스 템플릿
```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class PointTest {
    private Point p1;
    private Point p2;
    
    @BeforeEach
    void setUp() {
        p1 = new Point(0, 0);
        p2 = new Point(3, 4);
    }
    
    @Test
    @DisplayName("생성자가 올바르게 작동하는지 테스트")
    void testConstructor() {
        assertEquals(0, p1.getX());
        assertEquals(0, p1.getY());
    }
    
    @Test
    @DisplayName("거리 계산이 정확한지 테스트")
    void testDistanceTo() {
        double distance = p1.distanceTo(p2);
        assertEquals(5.0, distance, 0.001);
    }
    
    @Test
    @DisplayName("null 입력시 예외 발생 테스트")
    void testDistanceToNull() {
        assertThrows(NullPointerException.class, () -> {
            p1.distanceTo(null);
        });
    }
}
```

## Lab 1-4: JavaFX 첫 프로그램

### 기본 템플릿
```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FirstJavaFXApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // 캔버스 생성
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 원 그리기
        gc.setFill(Color.RED);
        gc.fillOval(400 - 50, 300 - 50, 100, 100); // 중앙에 반지름 50인 원
        
        // Scene 생성
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);
        
        // Stage 설정
        primaryStage.setTitle("My First JavaFX Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

### 실행 방법
1. **명령줄에서 실행**
   ```bash
   javac --module-path /path/to/javafx/lib --add-modules javafx.controls FirstJavaFXApp.java
   java --module-path /path/to/javafx/lib --add-modules javafx.controls FirstJavaFXApp
   ```

2. **Maven으로 실행**
   ```bash
   mvn clean javafx:run
   ```

## 문제 해결 가이드

### 자주 발생하는 오류

1. **"Error: JavaFX runtime components are missing"**
   - 해결: VM arguments에 JavaFX 모듈 경로 추가
   
2. **"Package javafx.* does not exist"**
   - 해결: JavaFX 라이브러리가 프로젝트에 추가되었는지 확인

3. **"Cannot find symbol: method launch(String[])"**
   - 해결: Application 클래스를 상속받았는지 확인

### 디버깅 팁

1. **콘솔 출력으로 확인**
   ```java
   System.out.println("Ball position: (" + ball.getX() + ", " + ball.getY() + ")");
   ```

2. **단계별 실행**
   - IntelliJ IDEA: 중단점 설정 후 Debug 모드 실행
   - 변수 값 확인 및 단계별 진행

## 추가 연습 문제

1. **Point 클래스 확장**
   - 중점 계산 메서드 추가
   - toString() 메서드 오버라이드
   - equals()와 hashCode() 구현

2. **다양한 도형 그리기**
   - 사각형, 삼각형 그리기
   - 여러 색상 사용
   - 그라데이션 효과 적용

3. **테스트 확장**
   - 파라미터화된 테스트 작성
   - 테스트 커버리지 측정
   - 성능 테스트 추가