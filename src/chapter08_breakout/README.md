# Chapter 8 - Breakout Game

Breakout 게임의 실행 가능한 Maven 프로젝트입니다.

## 프로젝트 구조

```
chapter08_breakout/
├── pom.xml                 # Maven 설정 파일
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nhnacademy/breakout/
│   │   │       └── BreakoutGame.java    # 메인 게임 클래스
│   │   └── resources/      # 리소스 파일 (이미지, 사운드 등)
│   └── test/
│       └── java/           # 테스트 코드
└── README.md

```

## 실행 방법

### 1. Maven이 설치되어 있는지 확인

```bash
mvn --version
```

### 2. 프로젝트 빌드

```bash
cd chapter08_breakout
mvn clean compile
```

### 3. 게임 실행

```bash
mvn javafx:run
```

## 게임 조작법

- **좌/우 화살표**: 패들 이동
- **스페이스바**: 게임 시작/일시정지
- **R키**: 게임 오버 시 재시작

## 게임 특징

- 5개 레벨
- 파워업 시스템 (패들 확장, 추가 생명, 볼 가속)
- 점수 시스템
- 생명 시스템 (기본 3개)
- 레벨별 난이도 증가

## 주요 클래스

- `BreakoutGame`: 메인 게임 클래스, JavaFX Application
- `GameObject`: 모든 게임 객체의 기본 클래스
- `Paddle`: 플레이어가 조작하는 패들
- `Ball`: 게임 볼
- `Brick`: 파괴 가능한 벽돌
- `PowerUp`: 파워업 아이템

## 개발 환경

- Java 11 이상
- JavaFX 17.0.2
- Maven 3.6 이상

## 테스트 실행

```bash
mvn test
```

## 패키징

실행 가능한 JAR 파일 생성:

```bash
mvn clean package
```