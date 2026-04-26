# Alert Engine

Lightweight TypeScript/Express service responsible for alert-related functionality.

## Runtime

- Node.js 20
- Express
- TypeScript
- Default local port: `3000`
- Docker Compose host port: `3002`

## Local Setup

```bash
cd apps/alert-engine
npm install
```

Environment files are initialized repo-wide by `scripts/env-init.sh`. Once that script has been run, this service should already have the `.env` file it needs for local development.

## Development Server

```bash
npm run dev
```

The dev server uses `ts-node-dev` and restarts when files under `src/` change.

## Build and Production Run

```bash
npm run build
npm start
```

The production command runs the compiled entrypoint at `dist/server.js`.

## Tests

```bash
npm test
```

## Docker

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up --build alert-engine
```

## Dev Dependencies

This service uses development-only packages for:

- TypeScript compilation: `typescript`
- Hot reload: `ts-node-dev`
- Testing: `jest`, `ts-jest`, `supertest`
- Type definitions: `@types/*`

Install all dependencies with `npm install` for local development. The Docker production image installs runtime dependencies only with `npm ci --omit=dev`.

## Environment Configuration

Local environment values should live in `.env`. Keep committed defaults and documentation in `.env.example`. Use `scripts/env-init.sh` to copy `.env.example` files into `.env` files across the repo.

### Development

- If `scripts/env-init.sh` has already been run, the local `.env` file should be in place.
- Use localhost service addresses when running dependencies on the host.
- Use Docker service names such as `kafka` when running inside Docker Compose.
