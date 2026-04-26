# Dashboard Backend

NestJS backend API for the CloudSherpa dashboard. This service acts as the backend-for-frontend for dashboard screens.

## Runtime

- Node.js 20
- NestJS
- TypeScript
- Default local port: `3001`
- Docker Compose host port: `3001`

## Local Setup

```bash
cd apps/dashboard-backend
npm install
```

Environment files are initialized repo-wide by `scripts/env-init.sh`. Once that script has been run, this service should already have the `.env` file it needs for local development.

## Development Server

```bash
npm run start:dev
```

The development server runs Nest in watch mode.

## Build and Production Run

```bash
npm run build
npm run start:prod
```

The production command runs the compiled entrypoint from `dist/`.

## Tests

```bash
npm test
npm run test:e2e
```

Additional test commands:

```bash
npm run test:watch
npm run test:cov
```

## Lint and Format

```bash
npm run lint
npm run format
```

## Docker

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up --build dashboard-backend
```

## Dev Dependencies

This service uses development-only packages for:

- Nest CLI and schematics: `@nestjs/cli`, `@nestjs/schematics`
- TypeScript build tooling: `typescript`, `ts-node`, `ts-loader`, `tsconfig-paths`
- Testing: `jest`, `ts-jest`, `supertest`, `@nestjs/testing`
- Linting and formatting: `eslint`, `prettier`, `typescript-eslint`
- Type definitions: `@types/*`

Install all dependencies with `npm install` for local development. The Docker production image installs runtime dependencies only with `npm ci --omit=dev`.

## Environment Configuration

Local environment values should live in `.env`. Keep committed defaults and documentation in `.env.example`. Use `scripts/env-init.sh` to copy `.env.example` files into `.env` files across the repo.

### Development

- If `scripts/env-init.sh` has already been run, the local `.env` file should be in place.
- Use localhost URLs for services running on the host.
- Use Docker service names when running through Docker Compose.