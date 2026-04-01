# Weekly Commit Module

A micro-frontend that replaces 15-Five as ST6's weekly planning tool. Connects individual weekly tasks to the organization's strategic goals through the RCDO hierarchy (Rally Cry > Defining Objective > Outcome > Weekly Commit).

## Tech Stack

| Layer    | Technology                              |
|----------|-----------------------------------------|
| Frontend | React 18, TypeScript (strict), Webpack 5 |
| Backend  | Spring Boot 3.2.5, Java 21             |
| Database | SQL (H2 in-memory, seeded on startup)    |
| Integration | Module Federation (PM remote pattern) |

## Prerequisites

- Java 21
- Node.js 18+
- Maven 3.9+

## Getting Started

### Backend

```bash
cd backend
mvn spring-boot:run
```

Starts on **http://localhost:8080**. The H2 database is created automatically with schema and seed data (4 demo users, RCDO goals, and sample tasks).

### Frontend

```bash
cd frontend
npm install
npm start
```

Starts on **http://localhost:4000**. Calls the backend API at localhost:8080 via CORS.

### Running Tests

```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test
```

## Demo Users

| Name  | Role     | Email             |
|-------|----------|-------------------|
| Alice | Manager  | alice@st6.com     |
| Bob   | Employee | bob@st6.com       |
| Carol | Employee | carol@st6.com     |
| Dan   | Employee | dan@st6.com       |

Password for all demo users: `password`

## Module Federation

The frontend is configured as a Webpack Module Federation remote. A PA host app can consume it via:

```js
// host webpack.config.js
remotes: {
  weeklyCommitModule: "weeklyCommitModule@http://localhost:4000/remoteEntry.js"
}

// host code
const App = React.lazy(() => import("weeklyCommitModule/App"));
```

The app also runs standalone at localhost:4000 for development and demo purposes.

## Features

- **My Week** — plan weekly tasks with priority (King/Queen/Knight/Pawn) and RCDO goal linking
- **Weekly Lifecycle** — DRAFT > LOCKED > RECONCILING > RECONCILED / CARRY_FORWARD
- **Reconciliation** — end-of-week Done/Not Done review; unfinished tasks auto-carry to next week
- **Team Dashboard** — manager view with alignment indicators (green/yellow/red) and drill-down
- **Goal Alignment** — combo box links tasks to org Outcomes; custom goals flagged as unaligned
