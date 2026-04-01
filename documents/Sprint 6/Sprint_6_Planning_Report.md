# Sprint Planning Report

## Sprint Number & Dates
**Sprint 6**  
**Duration:** 2 weeks (01/04/2026 - 15/04/2026)

## Sprint Goal
Extend localization support into the database layer while improving overall code quality, maintainability, and readiness for acceptance testing.  
This sprint focuses on implementing multilingual database structures, performing systematic code review, refactoring the codebase, and designing acceptance test plans.

## Selected Product Backlog Items

1. Database Localization

2.  Statistical Code Review

3. Code Clean-Up and Refactoring

4. Acceptance Test Planning

5. Architecture Design Documentation(prepare for reviewing)

## Planned Tasks / Breakdown

### 1. Database Localization: Ngoc
Create new entities to include translation relationships, Backend Model & Logic Updates

- Add Arabic language (RTL)

- USER_TRANSLATION

- Add constraints, indexes, and language codes

- Write SQL migration scripts  

- Create DAO methods to fetch localized content

- Create methods to add language parameter handling in service layer

- Create LocalizationService class(load localization and fallback if missing)


### 2.  Statistical Code Review: Hoang
- Run Static Analysis Tools (SonarQuib and SonarScaner): Detect errors, inefficiencies, and readability concerns.
- Analyze Code Metrics. Record statistics for:  
   ▪ Cyclomatic complexity  
   ▪ Lines of code per method  
   ▪ Duplicate or unreachable code  
- Document Findings  
   o Summarize and interpret analysis results.  
   o Include tables, charts, or screenshots of metrics.  
   o Highlight high-priority issues and recommendations.  

### 3. Code Clean-Up and Refactoring: Nhut
- Refactor and Simplify  
    o Split complex functions into smaller units.  
    o Remove redundant or duplicate code.  
    o Simplify naming conventions and ensure consistent formatting.  
- Enforce Code Standards  
   o Apply Java coding conventions and linting tools.  
   o Add inline documentation and method-level comments.  
- Verify Functionality  
   o Re-run unit tests to confirm stability after refactoring.

### 4. Acceptance Test Planning
- Define Acceptance Criteria  
    o Review original project and sprint requirements.  
    o Establish clear, measurable success criteria.  
- Design Acceptance Tests  
   o Create test cases for:  
   ▪ Functional testing  
   ▪ Usability testing  
   ▪ Performance and reliability testing  
   o Map each test to corresponding user stories or requirements.  

### 5. Architecture Design Documentation(prepare for reviewing)
- Ngoc: Update system design artifacts such as ER Diagrams and UML models. 
- Ngoc: Database localization plan and implementation report.
- Ngoc: Documentation on chosen localization method  
- Hoang: Statistical Code Review Report containing:  
  o Summary metrics  
  o Key findings and suggested improvements  
  o Visual evidence (screenshots, charts)    
- Thanh: Formal Acceptance Test Plan Document including:
  o Defined criteria and coverage matrix.
  o Example test cases with expected outcomes.

## Team Capacity & Assumptions
- Team size: 4 students
    - Ngoc Nguyen
    - Thanh Nguyen: Scrum Master
    - Nhut Vo
    - Hoang Vu
- Focus on implementation, testing, and documentation
- Lecturer acts as Product Owner
- All members actively participate in Sprint Review and in-class tasks
- Agile Scrum practices and tools (GitHub, CI/CD) are applied throughout the sprint :contentReference[oaicite:1]{index=1}

## Definition of Done
Sprint 6 is complete when:
1. Database localization implemented: Translation tables and static localization table created, functional, and migration scripts committed.

2. Backend updated for multilingual support: Entities, services, and fallback logic implemented and verified to return localized content correctly.

3. Acceptance Test Plan Completed

4. Quality ensured: All localization features tested with no regressions; code reviewed and merged successfully.

5. Sprint Review prepared: All materials and demonstrations ready for presentation.