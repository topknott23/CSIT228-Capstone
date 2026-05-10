# Project Rules

This document is the core ruleset for all future AI code generation in this project. It is based exclusively on the current implementation in the codebase and must be followed precisely.

## 1. Tech Stack & Architecture

- Java version: **21**
- JavaFX version: **21.0.6**
- Database: **MySQL** using **mysql-connector-j 8.3.0**
- Database URL: `jdbc:mysql://localhost:3306/dorm_app`
- Database user: `root`
- Database password: `` (empty string)
- Architecture pattern: **JavaFX MVC-like controllers + DAO**
- Core pattern: UI controllers handle view interactions only, while all data access must go through DAO classes.
- Packages reflect separation:
  - `doboard.auth` — authentication controllers and user DAO
  - `doboard.chores` — chore domain model and DAOs
  - `doboard.expenses` — expense domain model and DAOs
  - `doboard.dorm` — dorm domain model and DAOs
  - `doboard.dashboard` — dashboard controllers and view navigation
  - `doboard.common.connection` — DB connection management
  - `doboard.common.util` — UI utilities, component factories, navigation helpers
  - `doboard.common.session` — session persistence logic

## 2. File Structure & Separation of Concerns

### 2.1 Controller Responsibilities

- Controllers must contain only UI interaction logic, event handlers, validation, and navigation.
- Controllers may call DAO methods and session utilities, but must not contain raw SQL or direct database connection logic.
- Controllers may manipulate JavaFX nodes, update labels, load views, and bind properties.
- Never write business rules or persistence code in FXML controller classes.

### 2.2 DAO Responsibilities

- DAO classes are the only place for SQL queries and database CRUD operations.
- DAOs must use `SQLConnector.getConnection()` for database access.
- DAOs may map database rows to domain objects and vice versa.
- DAOs must not perform UI operations, load scenes, or manipulate JavaFX controls.
- DAO class names must end with `DAO`.

### 2.3 Utility Responsibilities

- `SQLConnector` handles database connection creation and fallback initialization logic only.
- `ComponentFactory` handles JavaFX component creation from FXML templates and localized component population.
- `NavigationManager`, `SceneLoader`, `StageUtil`, and `CustomTitleBar` manage JavaFX navigation and window behavior.
- `SessionHandler` handles session persistence to disk and restores the logged-in `User`.

### 2.4 Model/Domain Classes

- Domain classes represent data structures only.
- They should expose getters and setters consistent with existing naming conventions.
- Domain classes may be used by controllers and DAOs but should not contain UI-specific code.

## 3. Database & Naming Conventions

### 3.1 Authoritative Schema Names

The database schema must use the following table and column names exactly as shown below.
Never invent alternate names or synonyms.

#### Tables and important columns

- `users`
  - `user_id`
  - `username`
  - `email`
  - `password`
  - `created_at`
  - `full_name`

- `chores`
  - `chore_id`
  - `dorm_id`
  - `title`
  - `description`
  - `frequency`
  - `due_date`
  - `status`

- `bills`
  - `bill_id`
  - `dorm_id`
  - `title`
  - `total_amount`
  - `due_date`
  - `created_at`

- `bill_splits`
  - `split_id`
  - `bill_id`
  - `user_id`
  - `amount`
  - `is_paid`

- `dorms`
  - `dorm_id`
  - `dorm_name`
  - `join_code`
  - `created_at`

- `chore_assignments`
  - `chore_id`
  - `user_id`

### 3.2 SQL Naming Rules

- Table names are plural and use snake_case.
- Primary keys use the singular object name suffixed with `_id`.
- Foreign keys use the referenced object name plus `_id`.
- Boolean status columns use `is_` prefix when appropriate, such as `is_paid`.
- Timestamps use `created_at`.
- Do not invent columns such as `category_name`, `name`, `status_text`, or `user_email_address` if they are not present in the current DAO queries.
- If the current project uses `full_name`, do not use `name` in generated schema or SQL.

### 3.3 SQL Query Style

- Use `SELECT * FROM table WHERE ...` only when the DAO maps all returned columns.
- Use parameterized queries with `?` placeholders and `PreparedStatement`.
- Always bind parameters in sequential order.
- Use `executeUpdate()` for `INSERT`, `UPDATE`, and `DELETE`.
- Use `executeQuery()` for `SELECT`.
- When inserting rows, prefer explicit column lists and values: `INSERT INTO table(col1, col2) VALUES(?, ?)`.
- For generated keys, use `Statement.RETURN_GENERATED_KEYS` only when the DAO explicitly needs the generated ID.

### 3.4 Database Schema Hallucination Rule

- Never invent table or column names.
- Always derive schema names from DAO classes and current SQL statements.
- If the query string is not present in the codebase, do not fabricate it.
- If a domain object needs a new field, first confirm that the DAO query and database schema support it.

## 4. UI Conventions

### 4.1 JavaFX View Loading

- Use `FXMLLoader` with `getResource(...)` and the existing FXML path conventions.
- For reusable UI fragments, use `ComponentFactory` methods such as `createNotification`, `createChoreItem`, `createExpenseAlert`, `createLeaderboardRow`, `createDueBill`, `createProcessedBill`, `createTransactionItem`, and `createSpaceItem`.
- Controllers should call `NavigationManager.loadView(...)` for content switching inside the dashboard.
- Scene loading across windows should use `SceneLoader.loadScene(...)` with the controller class and FXML file path.
- Do not instantiate JavaFX controls dynamically if the project already has a reusable FXML fragment and factory method.

### 4.2 Controller Bindings

- Use `@FXML` private fields for controls defined in the FXML file.
- Bind layout properties in `initialize()` when necessary, such as image fit dimensions.
- Use private `@FXML` handler methods for user actions.
- Avoid using public fields or non-annotated fields for FXML injection.

### 4.3 Styling and Assets

- JavaFX styling should rely on existing CSS files under `src/main/resources/styles/`.
- Keep style class names consistent with existing CSS conventions.
- Add or update styles in `dashboard.css` and `styles.css` only when they match the current application theme.
- Resource paths use the `/doboard/...` form for classpath loading in FXML and Java.

### 4.4 UI Responsibilities

- Controllers handle view-specific logic and UI updates.
- Utility classes handle window controls and component creation.
- Do not perform database writes inside UI event handlers; instead, invoke DAOs or service wrappers.
- Do not store session state in controller fields; use `SessionHandler`.

## 5. Coding Standards

### 5.1 Exception Handling

- Use `try-with-resources` for database resources: `Connection`, `PreparedStatement`, `ResultSet`.
- In DAO code, catch `SQLException` and log the stack trace.
- Prefer `e.printStackTrace()` only in the existing style; do not suppress exceptions silently.
- For UI controllers, show user-facing errors with `Popup.show(...)` and log technical detail only if needed.
- Do not allow exceptions to flow out of event handlers without handling them.

### 5.2 Logging and Output

- Use `System.out.println(...)` in the current style for simple informational traces.
- Use `System.err.println(...)` for error reporting when the existing code uses it.
- Do not add a new logging framework unless the project explicitly introduces one.

### 5.3 Comments

- Comments should explain non-obvious behavior and TODOs, not restate obvious code.
- Keep comments short and factual.
- Avoid decorative comments unless used to separate logical sections, e.g. `// --- WINDOW CONTROLS ---`.
- Remove commented-out dead code before finalizing a feature.

### 5.4 Imports

- Keep imports minimal and only import required classes.
- Use explicit imports; do not use wildcard imports like `javafx.scene.*` unless already required by IDE formatting.
- Group imports by standard Java, third-party, and project packages.
- Follow the existing style: one import per line.

### 5.5 Naming Conventions

- Java class names use `PascalCase`, e.g. `UserDAO`, `LoginController`, `CustomTitleBar`.
- Method names use `camelCase`, except existing static methods like `Login` and `Register` in `UserDAO` that currently use uppercase first letter. Do not change existing method names without a project-wide refactor.
- Variable names use `camelCase`.
- Field names in domain objects may mirror database columns with underscores if required by existing getters/setters, e.g. `getBill_dorm_id()`.
- FXML `fx:id` attributes should match field names exactly.

## 6. Strict AI Generation Rules

- Use only existing schema columns and table names from DAO query strings.
- Do not add or assume new database tables or columns not already present in the current code.
- Do not change project architecture from JavaFX+DAO to another pattern unless explicitly instructed.
- Keep all new UI code within the existing JavaFX and FXML loading patterns.
- When generating new persistence code, place it in DAO classes and keep controllers clean.
- When generating new UI component creation code, prefer `ComponentFactory` and existing FXML fragments.
- Maintain the current `doboard` package structure; do not introduce unrelated root packages.

## 7. Additional Guidance

- When evolving the codebase, keep the following boundaries strict:
  - Controllers: UI behavior and event handling only.
  - DAOs: SQL and database access only.
  - Util classes: shared JavaFX helpers, navigation, and session management only.
  - Models: plain data carriers only.
- The database name must remain `dorm_app` unless the project configuration is explicitly changed in `SQLConnector`.
- Always preserve existing filename casing and FXML paths exactly, especially when referencing `doboard` resource locations.

---

This `rules.md` is the authoritative prompt for future AI-generated code for this project. Always follow it exactly and never hallucinate schema, component, or architectural details.