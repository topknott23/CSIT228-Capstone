<div align="center">

<img width="600" height="200" alt="Maintenant Application Banner" src="https://github.com/user-attachments/assets/761b8a90-51f6-405a-b113-07c47e40d2bd" />

# Maintenant: Boarding House Management System

> A desktop application designed to solve communication friction in boarding houses by automating chore schedules and bill splitting to formalize responsibilities. It includes a dedicated manager screen for overseeing payments, balance tracking, and maintaining tenant accountability.

</div>

---

##  The Purpose: Why We Built Maintenant
Living in a boarding house or dorm brings shared responsibilities, but managing them often leads to friction. Reminding a roommate to clean the shared spaces or awkwardly asking them to pay their portion of the utilities can create unnecessary tension. 

We created this system to serve as a neutral third party that handles the awkwardness for you. By formalizing these responsibilities into an automated system, roommates can focus on co-existing peacefully while the application tracks who did what and who owes what.

---

##  Group Members
* **Daniel Aguilar**
* **Gabriel Elorde**
* **Heron Jay Conde**
* **Joel Theo Gallarde**
* **Renz Gabriel Etcuban**

---

##  Key Features
* **Automated Chore Rotation & Tracking:** Eliminates confusion over whose turn it is by automatically assigning and tracking household chores.
* **Shared Expense and Utility Splitter:** Easily log shared bills, divide costs equally among roommates, and track individual payments.
* **Nudge Notifications (Signals):** Automated alerts to politely remind roommates about pending chores or overdue bills without requiring face-to-face confrontation.
* **Boarding House Manager Dashboard:** A centralized screen for tracking accountability, managing dorm roles (ADMIN/RESIDENT), and overseeing overall house balances.
* **Secure Dorm Entry:** Unique Join Codes with strict validation to ensure only authorized residents can access their specific boarding house dashboard.
* **ADMIN View:** Has admin view (landlord POV) which can create the dorm and can manage stuff
* **WORKING CHAT SYSTEM:** Has working chat system for the dormmates to chat(gibuhat ni gab the goat)
  
---

##  Built With
`Java` | `JavaFX` | `JDBC` | `SQLite`

---

##  System Architecture & Implementation

* **Object-Oriented Programming (OOP):** Utilizes inheritance (e.g., specific user roles extending base classes) and encapsulation to secure sensitive financial and session data.
* **Multithreading:** Background concurrent tasks handle scheduled "Nudges" and database saves to ensure the JavaFX GUI remains fully responsive.
* **GUI Structure:** Interactive, event-driven interface built using JavaFX and FXML.
* **Database Infrastructure:** Local persistence via SQLite featuring auto-initialization, strict foreign key constraints, and cascade deletes for seamless household record management.
* **Data Handling:** Extensive use of Java Generics (e.g., `List<Chore>` and `Map<User, Double>`) for flexible and type-safe data manipulation.

### Design Patterns
* **Singleton Pattern:** Used for secure and efficient Database Connection management.
* **Observer Pattern:** Implemented for real-time dashboard state updates to keep all views synchronized.
