# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an educational Java programming tutorial that teaches object-oriented programming through game development. The project is structured as a progressive learning path with 10 chapters, building from basic concepts to complete games.

## Key Architecture Points

### Class Hierarchy Structure
The codebase follows a progressive inheritance model:
```
Ball → PaintableBall → MovableBall → BoundedBall
```

Three core interfaces define behavior contracts:
- `Paintable` - for renderable objects
- `Movable` - for objects that can move
- `Boundable` - for objects with collision boundaries

### World Management Pattern
World classes extend JavaFX's `Pane` and manage game objects:
```
Pane → World → MovableWorld → BoundedWorld
```

### JavaFX Rendering Approaches
The project supports two rendering methods:
1. **Scene Graph**: Using JavaFX nodes (Circle, Rectangle) - better for interactive objects
2. **Canvas**: Using GraphicsContext - better for performance with many objects

## Development Setup

### JavaFX Configuration
The project requires JavaFX SDK configuration:
```bash
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
```

### Running Examples
Since this is an educational project, code examples are meant to be implemented by students following the lab exercises in each chapter. There are no pre-built executables.

## Documentation Structure

- `/doc/` - Contains all tutorial chapters in AsciiDoc format
- Each chapter has a `contents/` subdirectory with lab exercises
- Lab exercises follow the naming pattern `lab{chapter}-{number}.adoc`
- API documentation exists for core classes: `API_World.md`, `API_MovableBall.md`

## Important Notes

- The project was recently migrated from AWT/Swing to JavaFX
- Chapter 8 (upside_down) was removed, causing a gap in numbering
- All documentation is in Korean, focusing on Korean students
- This is purely educational - no production code or build scripts exist
- JUnit을 이용한 단위 테스트 검증
- Mokito를 이용한 목업 테스트 검증

## Common Tasks

주의
- 초급 학습 과정으로 디자인 패턴 제외

When working on documentation:
- Maintain consistency between Korean and English technical terms
- Keep lab exercises progressive and buildable
- Ensure JavaFX examples follow modern practices (AnimationTimer, Properties)
- 한글로 작성

When suggesting implementations:
- Use double precision for coordinates (JavaFX standard)
- Prefer Canvas for many objects, Scene Graph for few interactive ones
- Follow the established inheritance hierarchy
- Implement proper exception handling as specified in labs
- 단계별 구현 후 저장
  - git 브랜치를 이용해 학습자가 구현하기 위해 기본 메서드 프레임만 구현
  - 다음 단계에서 동작 코드 구현