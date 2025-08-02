#!/bin/bash

# Cannon 게임 실행 스크립트

# 프로젝트 디렉토리로 이동
cd "$(dirname "$0")"

# 공통 모듈 빌드
echo "Building common module..."
cd common
mvn clean install
if [ $? -ne 0 ]; then
    echo "Failed to build common module"
    exit 1
fi

# Cannon 게임 빌드 및 실행
echo "Building and running Cannon game..."
cd ../chapter10_cannon
mvn clean compile
if [ $? -ne 0 ]; then
    echo "Failed to build Cannon game"
    exit 1
fi

# JavaFX 실행 (Java 11 기준)
java --module-path /usr/local/javafx-21/lib --add-modules javafx.controls,javafx.graphics \
     --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
     --add-exports javafx.graphics/com.sun.glass.ui.mac=ALL-UNNAMED \
     -cp "target/classes:../common/target/game-common-1.0-SNAPSHOT.jar" \
     com.nhnacademy.cannon.CannonGame