# CSIT228 - Capstone DoBoard
## Group Members
- Daniel Aguilar
- Gabriel Elorde
- Heron Jay Conde
- Joel Theo Gallarde
- Renz Gabriel Etcuban

## Project Description
A desktop application solving communication friction in boarding houses by automating chore schedules and bill splitting to reduce social anxiety.

## Proposed Features
- Automated Chore Rotation
- Chore Tracking
- Shared Expense and Utility Splitter
- Nudge Notifications (Automated alerts)
  
## Planned Technologies
- Java
- JavaFX
- JDBC
- Database (SQLite)

## Evaluation Criteria Mapping (Initial)
- **OOP:** Implementation of inheritance (Roommate extends User) and encapsulation for sensitive financial data.
- **Multithreading:** Background tasks will handle scheduled "Nudges" and database saves to keep the GUI responsive.
- **GUI:** Interactive JavaFX interface using FXML and event-driven button handlers.
- **Database:** SQLite integration for local persistence of household records.
- **Java Generics:** Use of List<Chore> and Map<User, Double> for flexible data handling. *(Maybe)*
  ### Design Pattern/s:
- **Singleton Pattern** for the Database Connection
- **Observer Pattern** for real-time dashboard updates.
