# Game Projects

이 프로젝트는 게임 개발 학습을 위한 Maven 다중 모듈 프로젝트입니다.

## 프로젝트 구조

```
src/
├── pom.xml                 # 부모 POM
├── common/                 # 공통 라이브러리
│   ├── pom.xml
│   └── src/main/java/
│       └── com/nhnacademy/game/
│           ├── core/       # GameObject 등 핵심 클래스
│           ├── physics/    # Vector2D, Effect 등 물리 클래스
│           └── graphics/   # Renderable 등 그래픽 인터페이스
├── chapter08_breakout/     # Breakout 게임
│   ├── pom.xml
│   └── src/main/java/
│       └── com/nhnacademy/breakout/
└── chapter10_cannon/       # Cannon 게임
    ├── pom.xml
    └── src/main/java/
        └── com/nhnacademy/cannon/
```

## 빌드 방법

### 전체 프로젝트 빌드

```bash
cd src
mvn clean install
```

이 명령은 다음 순서로 모듈을 빌드합니다:
1. common - 공통 라이브러리
2. chapter08_breakout - Breakout 게임
3. chapter10_cannon - Cannon 게임

### 개별 게임 실행

#### Breakout 게임 실행
```bash
cd chapter08_breakout
mvn javafx:run
```

#### Cannon 게임 실행
```bash
cd chapter10_cannon
mvn javafx:run
```

## 공통 클래스

### core 패키지
- `GameObject`: 모든 게임 객체의 기본 추상 클래스
  - 위치, 크기, 속도 관리
  - 기본 충돌 검사
  - update() 및 draw() 메서드

### physics 패키지
- `Vector2D`: 2D 벡터 연산 클래스
- `Effect`: 물리 효과 인터페이스
- `GravityEffect`: 중력 효과 구현
- `WindEffect`: 바람 효과 구현

### graphics 패키지
- `Renderable`: 렌더링 가능한 객체 인터페이스

## 게임별 특징

### Chapter 8 - Breakout
- 클래식 벽돌깨기 게임
- 패들, 공, 벽돌, 파워업 시스템
- 5개의 파워업 타입
- 레벨 시스템

### Chapter 10 - Cannon Game
- 물리 기반 대포 게임
- 4가지 게임 모드 (클래식, 타임어택, 서바이벌, 퍼즐)
- 4가지 발사체 타입 (표준, 폭발, 관통, 분열)
- 5가지 타겟 타입
- 중력과 바람 효과

## 개발 환경

- Java 11 이상
- JavaFX 17.0.2
- Maven 3.6 이상

## 확장 가능성

공통 모듈을 사용하여 새로운 게임을 쉽게 추가할 수 있습니다:

1. 새 모듈 디렉토리 생성
2. pom.xml에 game-common 의존성 추가
3. GameObject를 상속받아 게임 객체 구현
4. 물리 효과는 Effect 인터페이스 구현
5. 부모 pom.xml의 modules에 추가