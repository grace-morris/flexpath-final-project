D&D Battle Organizer

A full-stack web app for Dungeon Masters to build a bestiary and roster of player characters, then run them through combat encounters with initiative tracking, health management, and tracking for reactions/legendary actions.

Designed to lighten the load involved with tracking health, AC, and abilities during combat.

Built for the LaunchCode FlexPath capstone project.

Author

Grace Morris — https://github.com/grace-morris

Video Walkthrough

Soon to come!

Features

 - User can register a new account and log in
 - Role-based access: regular users vs. admin
 - User can create, view, edit, and delete monsters
 - User can create, view, edit, and delete player characters
 - User can create, view, edit, and delete encounters
 - Monsters, characters, and encounters can be marked public or private
 - Search monsters/characters/encounters by name (partial match) and visibility (public / mine / all)
 - Sort search results by at least two different fields, ascending or descending
 - Add monsters and player characters to an encounter, including multiple copies of the same monster
 - Track initiative order during an encounter
 - Track health, reactions, and legendary actions for each combatant during a round
 - Advance to the next round of combat
 - Remove a combatant from an encounter
 - "Encounter Ended" state once every combatant has been removed


Tech Stack

Backend: Java 17, Spring Boot, Spring Security (JWT authentication), JdbcTemplate

Frontend: React, React Router, Vite

Database: MySQL

Getting Started
Prerequisites
Java 17
Node.js and npm
MySQL Server
Database Setup
Start your local MySQL server.
Run the script in database/create-database.sql against your MySQL instance. This drops and recreates the flexpath_final database and seeds it with an admin and a regular user account.
   mysql -u root -p < database/create-database.sql
Note: this script fully resets the database (drop + recreate) every time it's run, so any data you've added — including registered accounts — will be wiped if you re-run it. Re-run it whenever the schema changes, and expect to re-register test accounts afterward.
Backend Setup
In backend/src/main/resources/application.properties, set your own local MySQL credentials:
properties
   spring.datasource.url=jdbc:mysql://localhost:3306/flexpath_final
   spring.datasource.username=[your MySQL username]
   spring.datasource.password=[your MySQL password]

Do not commit real credentials to source control — keep this file untracked or use environment variables/a secrets file if you want to share the project publicly.

From the backend directory, run:
   ./mvnw spring-boot:run

The API will be available at http://localhost:8080.

Frontend Setup
From the frontend directory, install dependencies and start the dev server:
   npm install
   npm run dev
The app will be available at the URL Vite prints (typically http://localhost:5173).


API Overview

Method	Endpoint	Description
POST	/api/login	Authenticate and receive a JWT
POST	/api/register	Create a new user account
GET	/api/monsters	Search monsters (name, type, visibility, sort, pagination)
POST	/api/monsters	Create a monster
PUT	/api/monsters/{id}	Update a monster
DELETE	/api/monsters/{id}	Delete a monster
GET	/api/characters	Search player characters
POST	/api/characters	Create a player character
PUT	/api/characters/{id}	Update a player character
DELETE	/api/characters/{id}	Delete a player character
GET	/api/encounters	Search encounters
POST	/api/encounters	Create an encounter
PUT	/api/encounters/{id}	Update an encounter
DELETE	/api/encounters/{id}	Delete an encounter
POST	/api/encounters/{id}/monsters	Add a monster to an encounter
PATCH	/api/encounters/{id}/monsters/{monsterId}/initiative	Update a monster's initiative
PATCH	/api/encounters/{id}/monsters/{monsterId}/reaction	Toggle a monster's reaction used
PATCH	/api/encounters/{id}/monsters/{monsterId}/health	Update a monster's health
DELETE	/api/encounters/{id}/monsters/{monsterId}	Remove a monster from an encounter
POST	/api/encounters/{id}/next-round	Advance the encounter to the next round
Testing


Backend:

cd backend
./mvnw test

Frontend:

cd frontend
npm test


Known Issues / Future Work
- Will update UI to be prettier
- Will update so that legendary actions/reactions offer a list of possible options for those actions
- Will update to incldue spells and spell slot usage

- Currently, users can only toggle reactions, initiative, legendary actions, and change basic data. Will add more data to change in the future.


License

This project was created for educational purposes as part of the LaunchCode FlexPath program.


Do not feed my work to AI. I hate it and it should not exist. Buffoonery and bamboozlement and creativity should be made by our own hands. If you feed my work to AI I will find you.