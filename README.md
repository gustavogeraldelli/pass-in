# pass.in

[![CI](https://github.com/gustavogeraldelli/pass-in/actions/workflows/ci.yml/badge.svg)](https://github.com/gustavogeraldelli/pass-in/actions/workflows/ci.yml)

Fullstack event check-in platform with React, TypeScript, Spring Boot, PostgreSQL, QR code badges, Docker, OpenAPI, and tests.

The project was originally based on Rocketseat's NLW event and was expanded with real persistence, backend validation, a navigable frontend flow, QR code check-ins, Docker setup, CI, and business-rule tests.

## Features

- Event creation, listing, and details
- Attendee registration per event
- Capacity validation for full events
- Duplicate registration prevention by event and email
- Attendee search and backend pagination
- Attendee badge page with QR code
- Check-in flow with duplicate check-in prevention
- Event dashboard with seats, attendees, check-ins, and remaining capacity
- Standardized API errors and DTO validation
- OpenAPI/Swagger documentation
- Docker Compose setup for PostgreSQL, backend, and frontend
- Backend unit/controller tests and GitHub Actions CI

## Stack

- Java 21
- Spring Boot 4.1
- Spring Web, Spring Data JPA, Bean Validation
- Flyway
- PostgreSQL
- React 19
- TypeScript
- Vite 8
- React Router
- Tailwind CSS 4
- Docker and Docker Compose

## Architecture

```text
frontend/  React + TypeScript + Vite application served by Nginx in Docker
backend/   Spring Boot REST API with JPA, Flyway migrations, OpenAPI, and tests
postgres   PostgreSQL database managed by Docker Compose
```

The frontend consumes the Spring Boot API through `VITE_API_BASE_URL`. The backend reads database and CORS configuration from environment variables.

## Interface

![pass.in interface](assets/ui.png)

## Running With Docker

Create a local `.env` from the example if you want to customize values:

```bash
cp .env.example .env
```

Start the full stack:

```bash
docker compose up --build
```

Services:

```text
Frontend: http://localhost:9090
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
Postgres: localhost:5432
```

Stop the stack:

```bash
docker compose down
```

## Local Development

Start only PostgreSQL:

```bash
docker compose up -d postgres
```

Run the backend:

```bash
cd backend
./mvnw spring-boot:run
```

Run the frontend:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on:

```text
http://localhost:9090
```

## Environment

Root `.env.example`:

```text
POSTGRES_DB=pass_in
POSTGRES_USER=pass_in
POSTGRES_PASSWORD=pass_in
POSTGRES_PORT=5432

DATABASE_URL=jdbc:postgresql://localhost:5432/pass_in
DATABASE_USERNAME=pass_in
DATABASE_PASSWORD=pass_in
FRONTEND_ORIGIN=http://localhost:9090
VITE_API_BASE_URL=http://localhost:8080
CHECK_IN_TOKEN_SECRET=change-me
CHECK_IN_TOKEN_TTL=PT24H
```

Frontend-only `.env.example`:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## API

Main endpoints:

```text
GET  /events
POST /events
GET  /events/{eventId}
GET  /events/{eventId}/attendees?page=0&size=10&query=
POST /events/{eventId}/attendees
GET  /attendees/{attendeeId}/badge
POST /check-ins/{token}
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Validation

Backend:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd frontend
npm run lint
npm run build
```

Docker:

```bash
docker compose build
```

## Technical Decisions

- PostgreSQL replaced the original demo/file database to make persistence closer to a real application.
- Flyway owns the database schema and indexes.
- Backend pagination/search avoids loading all attendees into the browser.
- The frontend uses React Router instead of hardcoded event state.
- QR code badges use signed check-in tokens.
- Backend tests cover event capacity, duplicate registration, badge generation, signed check-in tokens, HTTP status codes, and standardized error responses.

## Limitations

- There is no authentication or organizer role yet.
- Email delivery is not implemented.
- The dashboard is intentionally simple and focused on the event workflow.
