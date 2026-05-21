# Dashboard

Next.js frontend for the CloudSherpa dashboard.

## Runtime

- Node.js 20
- Next.js 16
- React 19
- Default local port: `3000`
- Docker Compose host port: `3000`

## Local Setup

```bash
cd apps/dashboard
npm install
```

Environment files are initialized repo-wide by `scripts/env-init.sh`. Once that script has been run, this service should already have the `.env` file it needs for local development.

## Development Server

```bash
npm run dev
```

The Next.js dev server starts on `http://localhost:3000` unless a different port is provided.

## Build and Production Run

```bash
npm run build
npm start
```

The production start script serves the built Next.js app on port `3000`.

## Lint

```bash
npm run lint
```

## Testing

From `apps/dashboard`:

```bash
npm test
```

Vitest runs unit tests from `__tests__/unit`.

### Playwright

Install browser binaries once:

```bash
npx playwright install
```

Build before e2e tests:

```bash
npm run build
npm run test:e2e
```

Playwright runs specs from `__tests__/e2e`, starts `npm run start`, and uses `http://localhost:3000`.

Useful local commands:

```bash
npx playwright test --ui
npx playwright test --debug
npx playwright show-report
```

## Docker

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up --build dashboard
```

## Development Container

From the repository root:

```bash
docker compose -f infra/docker-compose.dev.yml up --build dashboard
```

The development Compose service bind-mounts `apps/dashboard` into `/app` and runs the Next.js development server. Next.js watches the mounted source and live reloads browser changes for pages, components, styles, and other frontend files.

## Dev Dependencies

This app uses development-only packages for:

- TypeScript types: `@types/node`, `@types/react`, `@types/react-dom`
- Linting: `eslint`, `eslint-config-next`
- Styling pipeline: `tailwindcss`, `@tailwindcss/postcss`
- TypeScript: `typescript`

Install all dependencies with `npm install` for local development. The Docker production image installs runtime dependencies only with `npm ci --omit=dev`.

## Environment Configuration

Local environment values should live in `.env`. Keep committed defaults and documentation in `.env.example`. Use `scripts/env-init.sh` to copy `.env.example` files into `.env` files across the repo.

### Development

- If `scripts/env-init.sh` has already been run, the local `.env` file should be in place.
- Client-exposed values must use the `NEXT_PUBLIC_` prefix.
- Point API URLs at the local backend service, usually `http://localhost:8083`.

### Production

- Build with production API URLs.
- Run with `NODE_ENV=production`.
