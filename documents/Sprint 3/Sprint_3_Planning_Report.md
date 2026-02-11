# Sprint Planning Report

## Sprint Number & Dates
**Sprint 3**  
**Duration:** *[10/02/2026 - 02/03/2026]* (16/02/2026 - 20/02/2026: off)

## Sprint Goal
The goal of Sprint 3 is to extend the functional prototype of the **Online Flashcard System for Study** by implementing remaining core features, strengthening backend logic, integrating frontend and backend, establishing a Jenkins‑based CI/CD pipeline, expanding automated testing with code coverage, and preparing the system for functional review.  
Additionally, the team will create and test the first local Docker image of the application.

## Selected Product Backlog Items
1. Extend functional prototype
2. Implement authentication and authorization
3. Integrate backend and frontend
4. Configure Jenkins CI/CD pipeline
5. Implement automated unit testing and JaCoCo coverage
6. Prepare for functional review
7. Create and test initial Docker image
8. Update documentation and sprint review materials

## Planned Tasks / Breakdown

### Task Breakdown by Team

| **Category**      | **Task**                                                                                                                                                                                                                       | **Respond** |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| **Frontend**      |                                                                                                                                                                                                                                |
| **1**             | Implement or extend the graphical user interface (JavaFX)- Remaining                                                                                                                                                           | Ngoc        |
| **2**             | Connect UI components to backend logic and database:                                                                                                                                                                           |             |
| **2.1**           | ClassCardController, ClassDetailController, ClassesController, TermTitleController                                                                                                                                             | Thanh       |
| **2.2**           | FlashCardFlipCardController, FlashCardSetCardController, FlashCardDetailController, FlashCardSetController, QuizController                                                                                                     | Hoang       |
| **2.3**           | HomeController, RegisterController, LoginController, HeaderController, NavController                                                                                                                                           | Nhut        |
| **3**             | Ensure UI supports all core workflows end‑to‑end                                                                                                                                                                               |             |
| **4**             | Integrate frontend with backend services (authentication, CRUD operations, flashcard logic)                                                                                                                                    |             |
| **5**             | Fix UI‑side bugs discovered during integration                                                                                                                                                                                 |             |
| **Backend**       |                                                                                                                                                                                                                                | Thanh, Nhut
| **1**             | Implement authentication and authorization module                                                                                                                                                                              |             |
| **2**             | Implement remaining business logic from the product backlog                                                                                                                                                                    |             |
| **3**             | Conduct performance profiling and fix bottlenecks                                                                                                                                                                              |             |
| **4**             | Write unit tests for new and existing backend features                                                                                                                                                                         |             |
| **5**             | Expand test coverage and integrate JaCoCo                                                                                                                                                                                      |             |
| **6**             | Configure Jenkins pipeline (checkout → build → test → coverage)                                                                                                                                                                |             |
| **7**             | Ensure pipeline triggers on commits to main branch                                                                                                                                                                             |             |
| **8**             | Build and test Docker image locally                                                                                                                                                                                            |             |
| **Sprint Review** | Prepare all for sprint review:  Demonstration, Jenkins CI/CD Demonstration, Code Coverage Report Presentation,Demonstrate that the docker image is working locally, Team Contribution Evidence, GitHub Updates, Trello Updates | Thanh       |
                                                                                                                                                                                                                                |                   |



## Team Capacity & Assumptions
- Team size: 4 students
    - Thanh Nguyen: Scrum Master
    - Ngoc Nguyen
    - Nhut Vo
    - Hoang Vu
- Focus on backend logic, CI/CD automation, and integration tasks
- Lecturer acts as Product Owner
- All team members participate in review and contribute to development
- Sprint assumes stable baseline from Sprint 2

## Definition of Done
Sprint 3 is complete when:
- Backend features are implemented, tested, and integrated
- Frontend and backend communicate correctly
- Jenkins CI/CD pipeline builds, tests, and generates coverage reports
- Unit tests cover new and existing functionality
- The application is ready for functional review with end‑to‑end workflows
- A working Docker image is created and tested locally
- Documentation, GitHub repository, and task boards are updated for Sprint Review  
