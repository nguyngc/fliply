# Sprint Planning Report

## Sprint Number & Dates
**Sprint 7**  
**Duration:** 2 weeks (15/04/2026 - 29/04/2026)

## Sprint Goal
Sprint 7 ensures that your product is functional, usable, and aligned with the initial requirements defined by the Product Owner. Therefore, the focus of this sprint is on testing the functional and non functional features of the product.
## Selected Product Backlog Items

1. Test Plan Creation
2. Final Functional Testing and Bug Fixing
3. Bug Tracking and Issue Resolution
4. Statistical Code Quality Review (Jenkins & SonarQube)
5. User Stories Review and Refinement (Trello)
6. User Acceptance Test Planning and Execution
7. Heuristic Evaluation and Usability Assessment
8. Architecture and Technical Documentation Update

## Planned Tasks / Breakdown

### 1. Test Plan Creation:
- Create a complete test plan including:

  - Test objectives
  - Required resources
  - Test environment configuration
  - Test scope and test tasks: Functional testing

### 2.   Final Unit Testing

- Conduct final unit testing for all implemented features
- Verify system functionality after code cleanup and refactoring
- Ensure that all critical functionalities work as expected  

### 3. Bug Tracking and Issue Management

- Create a bug tracking table to record issues identified during testing
Track information such as:

    - Issue description
    - Severity 
    - Status
    - Resolution


- Address and resolve identified bugs and issues
- Re-test fixed issues to confirm successful resolution

### 4. User Stories Review and Refinement

- Review current user stories in Trello related to the latest software functionality
- Refine and update user stories to accurately reflect the implemented system features
- Ensure consistency between implementation, testing results, and documented user stories
### 5. Static Code Analysis and CI/CD Execution

- Use Jenkins to perform a full project build
- Deploy the project using Docker successfully
- Generate static code analysis reports using SonarQube
- Verify that code quality metrics meet the required standards

### 6. Heuristic Evaluation

- Conduct a heuristic evaluation of the final product based on usability principles introduced in lectures
- Evaluate key usability aspects including:

    - System consistency and feedback
    - Navigation and learnability
    - Accessibility and localization usability

- Identify usability problems and classify them by severity
- Document evaluation results and suggested improvements

###  7. User Acceptance Testing (UAT)

- Perform User Acceptance Testing based on acceptance criteria defined in Sprint 6
- Design UAT test cases following the provided template
- Execute test scenarios representing real user workflows
- Record test results, including pass/fail status and observations

###  8. Documentation of Technical Changes

| Problem identified | How it was implemented | Technical change / recommendation | Impact | Test / verification |
|---|---|---|---|---|
| Teacher Create Class: empty class code | Trim input, block blank code, then call service duplicate check | Require class code and show a clear localized error message | Prevents invalid class creation and gives immediate feedback | Verify create-class with empty code and confirm the error is shown |
| Teacher Creates Flashcard Set: empty required fields | Validate subject/file first, then parse CSV, create set, import cards, reload class | Require subject and uploaded flashcard data | Avoids incomplete flashcard sets and improves input quality | Test submit with missing fields and confirm validation blocks it |
| Student Creates Flashcard: empty term or definition | Validate set, term, definition, and user before save; update existing card in EDIT mode | Reject empty Term/Definition values and show a warning before saving | Prevents incomplete flashcards from being stored | Try saving with empty term/definition and confirm it is rejected |
| Student Edit Flashcard: save changes stuck | Update `AppState.currentDetailList`, then return to the correct screen after save | Refresh the save flow so changes are applied and the user returns to flashcard details | Improves workflow stability and makes updates visible immediately | Edit a flashcard, save it, and verify the updated content appears |
| Student Delete Flashcard: missing confirmation | Ask for confirmation, delete quiz details first, then sync all flashcard lists | Add a confirmation dialog before deleting and show success/error feedback after the action | Reduces accidental deletion and improves safety | Trigger delete and confirm the confirmation dialog appears first |
| Student Create Quiz: invalid input | Validate empty, non-numeric, zero/negative, and too-large counts before service call | Validate empty, zero, negative, and too-large question counts against available flashcards | Prevents unexpected quiz generation and improves error handling | Test invalid inputs and verify the proper error message is shown |

**Conclusion:** There is **no major impact on system architecture**. The changes are limited to controller logic, validation, and localization resources, so the overall system architecture remains unchanged.


## Team Capacity & Assumptions
- Team size: 4 students
    - Ngoc Nguyen
    - Thanh Nguyen
    - Nhut Vo
    - Hoang Vu: Scrum Master
- Lecturer acts as Product Owner
- All members must attend Sprint Review and present individual contributions
- Agile Scrum practices are applied
- CI/CD tools (Jenkins, SonarQube, Docker) are used throughout the sprint

## Definition of Done
Sprint 7 is completed when:

1. Test Plan is completed and executed.
2. Final unit and functional testing are finished.
3. Bug tracking table is created, updated, and resolved issues are verified.
4. Jenkins build and SonarQube analysis reports are generated with acceptable grades.
5. Heuristic Evaluation and UAT are completed and documented.
6. Technical and architecture documentation is updated.
7. Sprint Review materials are prepared and presented.