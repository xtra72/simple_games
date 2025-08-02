# Chapter 10 - Cannon Game

완성된 Cannon Game의 실행 가능한 Maven 프로젝트입니다.

## 프로젝트 구조

```
chapter10_cannon/
├── pom.xml                 # Maven 설정 파일
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nhnacademy/cannon/
│   │   │       └── CannonGame.java    # 메인 게임 클래스
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
cd chapter10_cannon
mvn clean compile
```

### 3. 게임 실행

```bash
mvn javafx:run
```

## 게임 모드

### 1. Classic Mode
- 제한된 탄약으로 모든 타겟 파괴
- 다양한 타겟 타입 (정적, 이동, 비행)
- 점수 시스템

### 2. Time Attack Mode
- 무제한 탄약
- 지속적으로 생성되는 타겟
- 최고 점수 도전

### 3. Survival Mode
- 웨이브 기반 진행
- 웨이브마다 증가하는 난이도
- 제한된 탄약 (웨이브 클리어 시 보충)

### 4. Puzzle Mode
- 특정 배치된 타겟
- 제한된 탄약으로 퍼즐 해결
- 전략적 사고 필요

## 게임 조작법

- **마우스 이동**: 대포 조준
- **마우스 클릭**: 발사
- **1키**: 표준 발사체
- **2키**: 폭발 발사체
- **3키**: 관통 발사체
- **4키**: 분열 발사체
- **스페이스바**: 일시정지
- **ESC**: 메뉴로 돌아가기

## 발사체 타입

1. **Standard (표준)**: 기본 발사체
2. **Explosive (폭발)**: 충돌 시 주변 타겟에도 피해
3. **Piercing (관통)**: 타겟을 관통하여 계속 진행
4. **Split (분열)**: 1초 후 3개로 분열

## 타겟 타입

- **S (Static)**: 정적 타겟
- **M (Moving)**: 좌우로 이동하는 타겟
- **F (Flying)**: 상하로 이동하는 타겟
- **A (Armored)**: 높은 체력의 장갑 타겟
- **S (Special)**: 특별 보너스 타겟

## 게임 특징

- 물리 시뮬레이션 (중력, 바람)
- 발사체 궤적 표시
- 타겟 체력바 표시
- 다양한 게임 모드
- 점수 및 웨이브 시스템

## 주요 클래스

- `CannonGame`: 메인 게임 클래스, JavaFX Application
- `GameObject`: 모든 게임 객체의 기본 클래스
- `Cannon`: 플레이어가 조작하는 대포
- `Projectile`: 다양한 타입의 발사체
- `Target`: 다양한 타입의 타겟
- `Effect`: 중력, 바람 등의 효과 인터페이스
- `Vector2D`: 2D 벡터 연산 클래스

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

## 확장 가능성

이 프로젝트는 다음과 같이 확장할 수 있습니다:
- 사운드 효과 추가
- 파티클 효과 시스템
- 멀티플레이어 모드
- 레벨 에디터
- 추가 발사체 및 타겟 타입
- 업그레이드 시스템