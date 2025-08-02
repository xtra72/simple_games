#!/bin/bash

# Breakout 게임 실행 스크립트 (Maven 의존성 사용)

echo "Building and running Breakout game..."

# 프로젝트 루트로 이동
cd "$(dirname "$0")/.."

# 전체 프로젝트 빌드
mvn clean install

# Breakout 게임 디렉토리로 이동
cd chapter08_breakout

# Maven을 통해 JavaFX 실행
mvn clean javafx:run