# macOS JavaFX 실행 문제 해결 가이드

macOS에서 JavaFX 애플리케이션 실행 시 발생하는 `NSTrackingRectTag` 오류를 해결하는 방법입니다.

## 문제 원인
macOS의 네이티브 윈도우 시스템과 JavaFX의 호환성 문제로 인해 발생합니다.

## 해결 방법

### 방법 1: 실행 스크립트 사용 (권장)
각 게임 디렉토리에 있는 실행 스크립트를 사용합니다:

```bash
# Breakout 게임 실행
cd src/chapter08_breakout
./run.sh

# Cannon 게임 실행
cd src/chapter10_cannon
./run.sh
```

### 방법 2: 직접 빌드 및 실행
1. 먼저 전체 프로젝트를 빌드합니다:
```bash
cd src
mvn clean install
```

2. 각 게임을 실행합니다:
```bash
# Breakout 게임
cd chapter08_breakout
mvn javafx:run

# Cannon 게임
cd chapter10_cannon
mvn javafx:run
```

### 방법 3: Java 명령줄로 직접 실행
JavaFX가 시스템에 설치되어 있다면:

```bash
# 공통 모듈 빌드
cd src/common
mvn clean install

# Breakout 게임 실행
cd ../chapter08_breakout
mvn clean compile
java -XstartOnFirstThread \
     --module-path /path/to/javafx-21/lib \
     --add-modules javafx.controls,javafx.graphics \
     --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
     --add-exports javafx.graphics/com.sun.glass.ui.mac=ALL-UNNAMED \
     -Dprism.order=sw \
     -cp "target/classes:../common/target/game-common-1.0-SNAPSHOT.jar" \
     com.nhnacademy.breakout.BreakoutGame
```

## 적용된 수정 사항

1. **JavaFX 버전 업데이트**: 17.0.2 → 21
2. **JVM 옵션 추가**:
   - `-XstartOnFirstThread`: macOS에서 GUI 스레드 관리
   - `--add-exports`: 내부 API 접근 허용
   - `-Dprism.order=sw`: 소프트웨어 렌더링 사용

3. **.mvn/jvm.config 파일 생성**: Maven 실행 시 자동으로 JVM 옵션 적용

## 추가 문제 해결

여전히 문제가 발생한다면:

1. Java 버전 확인:
```bash
java --version
```
Java 11 이상이 필요합니다.

2. Maven 버전 확인:
```bash
mvn --version
```
Maven 3.6 이상이 권장됩니다.

3. 소프트웨어 렌더링 강제:
JVM 옵션에 `-Dprism.order=sw` 추가

4. 디버그 모드 활성화:
`-Dprism.verbose=true` 옵션으로 JavaFX 로그 확인