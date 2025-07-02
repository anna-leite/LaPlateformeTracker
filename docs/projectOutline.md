### Day 1: Project Kickoff & Foundation

- Kickoff: 
    - scope, repo layout (per “repo sketch”), branch strategy, communication.
- Environment & CI:
    - Install JDK, Maven, JavaFX, IDE extensions, Postegres.
    - Create GitHub repo, add .github/workflows/maven-ci.yml stub.
- DB schema & connection:
    - Dev A writes sql/init-schema.sql (users + students tables).
    - Dev A implements DbConnectionManager.java (reads application.properties).
    - Dev B sets up application.properties template and test override.
    - Dev C writes README’s “Setup” section: Postgres install, run init-schema.

Deliverables:

- init-schema.sql, DbConnectionManager, application.properties stub, README updated.

### Day 2: DAO Interfaces & Core CRUD

Dev A:
- Define StudentRepository.java interface (add/update/delete/findById/findAll).
- Start UserRepository.java (authenticate/add/list).

Dev B:
- Implement PostgresStudentRepository.java (CRUD with PreparedStatements).
- Write basic JUnit tests for “add” and “findById” against a test DB.

Dev C:
- Implement PostgresUserRepository.java#authenticate().
- Tests for valid/invalid login.
All by EOD:
- Passing tests for core DAO methods.

### Day 3: JavaFX Skeleton + Login Flow

Dev A:
- Create MainApp.java (launches login.fxml).
- FXML + LoginController.java with user/password fields + “Login” button stub.

Dev B:
- Implement AuthService.java (calls UserRepository.authenticate(); stores current user in AppContext).
- Hook LoginController to AuthService. On success, load dashboard.fxml.

Dev C:
- Write DashboardController.java with empty TabPane (“Students” & “Users” tabs).
- Add event-based placeholder to show “not authorized” if user lacks role.
Deliverables:
- Login & dashboard shell working end-to-end (no error crashes).


### Day 4: Student UI & Integration

Dev A:
- FXML + StudentListController.java: TableView, “Add/Edit/Delete” buttons.
- Bind TableView to ObservableList<Student> from StudentService.findAll().

Dev B:
- Implement StudentService.java (wraps repository calls, does validation, fires change events).
- FXML + StudentFormController.java (form to add/edit with fields + “Save” button).

Dev C:
- Wire “Add” button to open student_form.fxml in modal, call StudentService.add().
- Write integration tests: form → DAO → DB → List refresh.
Deliverables:
- Full Student CRUD through JavaFX UI.

### Day 5: User Management & Authentication Enforcement

Dev A:
- FXML + UserListController.java with TableView, “Add User” button.
- Bind to UserService.listUsers().

Dev B:
- Implement UserService.java (addUser with salt+hash, listUsers).
- UserFormController.java (username/password/role form) + hook to UserService.

Dev C:
- Secure the “Users” tab: only show if currentUser.role == "ADMIN".
- Tests: non-admin login cannot see user management; admin can.
Deliverables:
- User CRUD & role-based UI access.

### Day 6: Sorting, Filtering & Advanced Search

Dev A:
- Service/DAO: add findByCriteria(ageMin, ageMax, gradeMin…).
- Expose this in StudentService.

Dev B:
- In StudentListController: add sort ComboBoxes (FirstName, LastName, Age, Grade).
- Add filter fields (min/max) + “Search” button → calls findByCriteria.

Dev C:
- Implement Strategy pattern for sorting (Comparator factories).
- Use JavaFX FilteredList + SortedList for efficient table updates.
Deliverables:
- Sorting & advanced search UI functioning.

### Day 7: Statistics, Pagination & Backup Trigger

Dev A:
- DAO & StudentService: double averageGrade(), Map<Integer,Long> countByAge().
- Pagination: findPage(int page, int pageSize).

Dev B:
- Add “Statistics” view or section: show avg grade & age-group counts in labels or mini-chart.
- In StudentList view add pagination controls (Prev/Next + page number).

Dev C:
- Implement BackupService subscribing to student/user change events via a simple EventBus.
- BackupService runs a shell pg_dump (using ProcessBuilder) into backups/ with timestamp.
Deliverables:
- Statistics panel, pagination working, and backups triggered on every add/update/delete.

### Day 8: Import/Export & Error Handling

Dev A:
- Template Method base DataExporter<T>, CsvStudentExporter + hook to UI (“Export CSV”).
- Similar for JsonStudentExporter.

Dev B:
- DataImporter<T> abstract + CsvStudentImporter.
- UI dialogs to choose a file and import (with progress/confirmation).

Dev C:
- Centralize error handling: catch exceptions in services/controllers, show Alert dialogs with friendly messages & retry options.
- Write integration tests for import/export round-trip and error cases.
Deliverables:
- CSV/JSON import-export flows, robust error dialogs.

### Day 9: Polish, CI, Docs & Demo Prep

Dev A:
- Final code cleanup: remove unused imports, ensure consistent style, JavaDoc on public APIs.
- Update pom.xml with JavaFX plugin, shade, or assembly for runnable jar.

Dev B:
- Write or finalize README.md: setup, run, backup/restore, import/export, troubleshooting.
- Fill out application.properties.example.

Dev C:
- Complete CI workflow: run mvn test, mvn javafx:run smoke check, lint.
- Prepare demo: short slides or live-run script covering login, CRUD, search, stats, import/export, backup.
All (late afternoon):
- Team demo rehearsal, quick Q&A.
- Merge all feature branches into main, tag v1.0-release.

### Summary of schedule:

- **Week 1** (Days 1–5) from zero up to full Student + User CRUD and auth
- **Week 2** (Days 6–9) add sorting/search, stats/pagination, import/export, backup, polish and deliver.

