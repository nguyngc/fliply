# Sprint Planning Report

## Sprint Number & Dates
**Sprint 4**  
**Duration:** *[03/03/2026 - 16/03/2026]*

## Sprint Goal
The goal of Sprint 4 is to **finalize and stabilize** the functional prototype of the **Online Flashcard System for Study** by completing all remaining core features, performing thorough testing and bug fixing, and preparing the application for the **final demonstration (Week 8)**.  
In addition, the team will **containerize the full project (frontend + backend) using Docker**, **build and push a public Docker image to Docker Hub**, and **test the deployed image in a suitable environment (e.g., Play with Docker / Docker Play)**.  
Finally, the team will **share the functional prototype with classmates for feedback**, and update documentation and sprint materials in GitHub/Trello/Jira as required.

## Selected Product Backlog Items
1. Finalize functionality of the prototype
2. Bug fixing and stabilization
3. End-to-end testing of core workflows
4. Containerize project (frontend + backend) with Docker
5. Build and test Docker image locally
6. Push Docker image to Docker Hub (public)
7. Test deployed image in a suitable environment (Docker Play / Play with Docker)
8. Share prototype with classmates for feedback
9. Prepare final demonstration materials (Week 8) and finalize documentation

## Planned Tasks / Breakdown

### Task Breakdown by Team

| **Category**      | **Task**                                                                                                                                                                                                 | **Respond** |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| **Finalization**  |                                                                                                                                                                                                          | All Team |
| **1**             | Finalize all planned features (Authentication, API integration, data visualization)                                                                                                                      | All Team |
| **2**             | Bug fixing: resolve data display errors, UI glitches, and improve error handling (debugging/log analysis)                                                                                                | All Team |
| **Testing / QA**  |                                                                                                                                                                                                          | All Team |
| **3**             | Verify business logic accuracy (CRUD workflows, flashcard logic, quiz logic)                                                                                                                             | All Team |
| **4**             | Ensure UI components render correctly and respond as expected (navigation, cards, lists, empty states)                                                                                                   | All Team |
| **5**             | Validate data input, storage, retrieval, and display across all key screens (Home → Classes → Sets → Flashcards → Quiz)                                                                                  | All Team |
| **Docker / DevOps** |                                                                                                                                                                                                        | All Team |
| **6**             | Create Dockerfile(s): define base image, build steps, dependencies, app code, and startup command (containerize frontend + backend)                                                                      | All Team |
| **7**             | Build Docker image with `docker build` and tag with version                                                                                                                                              | All Team |
| **8**             | Local testing with `docker run` to verify app runs correctly (basic smoke tests)                                                                                                                         | All Team |
| **Release / Deployment** |                                                                                                                                                                                                   | All Team |
| **9**             | Docker Hub: create account/repo (if needed), tag image (username + version), push image with `docker push`                                                                                               | All Team |
| **10**            | Verification: confirm image is public on Docker Hub and can be pulled successfully                                                                                                                       | All Team |
| **11**            | Test deployed image in Docker Play / Play with Docker environment (run container, verify logs, confirm app behavior)                                                                                     | All Team |
| **Sharing / Feedback** |                                                                                                                                                                                                     | All Team |
| **12**            | Share Docker Hub link + run instructions to classmates, collect feedback, and summarize key suggestions                                                                                                  | All Team |
| **Sprint Review** | Prepare for final demonstration (Week 8): product walkthrough, Docker Hub + deployment proof, user flow & major features, individual contributions & challenges solved, GitHub + Trello/Jira updates     | Nhut Vo  |

## Team Capacity & Assumptions
- Team size: 4 students
    - Nhut Vo: Scrum Master
    - Thanh Nguyen
    - Ngoc Nguyen
    - Hoang Vu
- Focus on final stabilization, Docker containerization, release to Docker Hub, deployment testing, and demo preparation
- Lecturer acts as Product Owner
- All team members participate in testing, review, and contribute to development
- Sprint assumes stable baseline from Sprint 3 (integrated prototype + CI pipeline available)

## Definition of Done
Sprint 4 is complete when:
- All planned features are fully implemented and operational (no critical blockers)
- Bugs discovered during final integration/testing are fixed and verified
- Core workflows work end-to-end (Register/Login → Classes → Sets → Flashcards → Quiz)
- A working Docker image is created for the full application (frontend + backend)
- Docker image is tested locally using `docker run`
- Docker image is pushed to Docker Hub and publicly accessible
- Deployed image is tested in a suitable environment (Docker Play / Play with Docker) with evidence (logs/screenshots)
- Prototype is shared with classmates and feedback is collected and documented
- Final demo materials are ready (PPT + walkthrough plan)
- Documentation, GitHub repository, and task boards (Trello/Jira) are updated for Sprint Review
