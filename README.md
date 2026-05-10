<div align="center">

<img width="600" height="200" alt="Image" src="https://github.com/user-attachments/assets/761b8a90-51f6-405a-b113-07c47e40d2bd" />

# Our Capstone Project

> A desktop application solving communication friction in boarding houses by automating chore schedules and bill splitting to formalize responsibilities. And a dedicated manager screen for overseeing payments, balance tracking, and maintaining tenant accountability.

</div>

---

## Group Members
* **Daniel Aguilar**
* **Gabriel Elorde**
* **Heron Jay Conde**
* **Joel Theo Gallarde**
* **Renz Gabriel Etcuban**

---

## Proposed Features
* Automated Chore Rotation
* Chore Tracking
* Shared Expense and Utility Splitter
* Nudge Notifications *(Automated alerts)*
* Boarding House Manager Dashboard

---

## Planned Technologies
`Java` | `JavaFX` | `JDBC` | `SQLite`

---

## Evaluation Criteria Mapping (Initial)

* **OOP:** Implementation of inheritance (`Roommate` extends `User`) and encapsulation for sensitive financial data.
* **Multithreading:** Background tasks will handle scheduled "Nudges" and database saves to keep the GUI responsive.
* **GUI:** Interactive JavaFX interface using FXML and event-driven button handlers.
* **Database:** SQLite integration for local persistence of household records.
* **Java Generics:** Use of `List<Chore>` and `Map<User, Double>` for flexible data handling. *(Maybe)*

### Design Pattern/s:
* **Singleton Pattern:** For the Database Connection.
* **Observer Pattern:** For real-time dashboard updates.

---

## Project Status & Remaining Work

### Completed Features (100%)
- **User Authentication** - Login/Register with session persistence and duplicate prevention
- **Dashboard Navigation** - Main dashboard with 4 sections (Dashboard, Chores, Expenses, Signals)
- **Expense & Bill Splitting** - Full CRUD operations, equal splitting, payment tracking
- **Dorm Management** - Create dorms, join via code, member roles (ADMIN/RESIDENT)
- **Database Infrastructure** - Auto-initialization, proper foreign keys, cascade deletes

### Partially Implemented (30-40%)
| Feature | Status | Issues |
|---------|--------|--------|
| **Chore Tracking** | 40% | Leaderboard not loading, Add/Edit/Delete dialogs missing, no DB integration |
| **Signals/Nudges** | 30% | UI only, no database persistence, DND state not saved |
| **Dashboard Content** | Shell only | No data loading, no refresh logic, mark-as-done not implemented |

### Critical Issues (Will Crash App)
1. **Database Schema Mismatch** - `BillSplitDAO` reads `amount` but DB creates `amount_owed`
2. **Missing Leaderboard FXML** - `/doboard/common/leaderboard-item.fxml` doesn't exist

### Missing Features (0%)
| Feature | Impact | Effort |
|---------|--------|--------|
| **Manager Dashboard** | Core feature for accountability | 8-10 hrs |
| **Automated Chore Rotation** | Proposed key feature | 6-8 hrs |
| **Notification System** | Nudge persistence & alerts | 5-6 hrs |
| **Complete Signal/Nudge Persistence** | User experience critical | 4-5 hrs |

### 📋 High Priority TODO Items (12 Total)

#### Immediate (Blocker Fixes - 2-3 hours)
- [ ] Fix `BillSplitDAO` column name: `amount_owed` (from `amount`)
- [ ] Create missing `/doboard/common/leaderboard-item.fxml` file
- [ ] Implement password hashing (bcrypt recommended)
- [ ] Fix database schema column name mismatch in `InitDB.java`

#### Core Functionality (8-10 hours)
- [ ] **Chore System**: Load leaderboard data from database
- [ ] **Chore System**: Implement Add/Edit/Delete chore dialogs  
- [ ] **Dashboard**: Fetch real dorm_id and load chores/bills/notifications
- [ ] **Dashboard**: Setup hourly refresh timeline
- [ ] **Dashboard**: Implement mark-as-done database updates
- [ ] **Signals**: Create `SignalDAO` for persistence
- [ ] **Signals**: Add `notifications` and `do_not_disturb` database tables
- [ ] **Signals**: Implement actual signal sending (currently UI-only)

#### Additional Improvements (5+ hours)
- [ ] Add input validation (email format, password strength, amount ranges)
- [ ] Implement missing DAO methods (`getAllUsers`, `getById`, `update`, `delete`, etc.)
- [ ] Complete settings implementation (profile, notifications, automation, privacy)
- [ ] Add overdue chore/bill detection and indicators
- [ ] Implement chore frequency handling (once, daily, weekly, monthly)

###  Feature Completion Matrix
```
User Auth              ████████████████████ 100%
Dashboard Navigation   ████████████████████ 100%
Bill Splitting         ████████████████████ 100%
Chore Tracking         ████████░░░░░░░░░░░░  40%
Signals/Nudges         ██████░░░░░░░░░░░░░░  30%
Manager Dashboard      ░░░░░░░░░░░░░░░░░░░░   0%
Chore Rotation         ░░░░░░░░░░░░░░░░░░░░   0%
Notifications          ░░░░░░░░░░░░░░░░░░░░   0%
```

### Recommended Completion Order
1. **Fix Critical Bugs** (Session 1) - Database mismatch, missing files
2. **Complete Chore System** (Session 2) - Leaderboards, dialogs, database integration
3. **Build Signals/Notifications** (Session 3) - Persistence layer, DND state, actual sending
4. **Manager Dashboard** (Session 4) - Role-based UI switching, accountability views
5. **Polish & Testing** (Final) - Validation, error handling, edge cases

### Estimated Total Effort: **40-60 hours** of development
