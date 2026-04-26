# Intelligence Engine

Python/FastAPI service for forecasting, anomaly detection, and future ML-driven recommendations.

## Runtime

- Python 3.11
- FastAPI
- Uvicorn
- Default local port: `8000`
- Docker Compose host port: `8000`

## Local Setup

```bash
cd apps/intelligence-engine
python -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt
```

Environment files are initialized repo-wide by `scripts/env-init.sh`. Once that script has been run, services with `.env.example` files should already have the `.env` files they need for local development. Add `apps/intelligence-engine/.env.example` before relying on the script for this service.

If the service needs Kafka locally, start the shared development dependencies from the repository root:

```bash
docker compose -f infra/docker-compose.yml up -d kafka schema-registry
```

## Development Server

```bash
uvicorn src.main:app --reload --host 0.0.0.0 --port 8000
```

The API is available at `http://localhost:8000`.

## Production Run

```bash
uvicorn src.main:app --host 0.0.0.0 --port 8000
```
## Docker

From the repository root:

```bash
docker compose -f infra/docker-compose.yml up --build intelligence-engine
```

## Dev Dependencies

Python dependencies are pinned in `requirements.txt`.

Key development/runtime groups include:

- API layer: `fastapi`, `uvicorn`, `pydantic`
- ML and analytics: `tensorflow`, `numpy`, `pandas`, `scikit-learn`
- Kafka integration: `confluent-kafka`
- Persistence: `psycopg`, `sqlalchemy`
- Environment loading: `python-dotenv`

Keep virtual environments out of git. Use `.venv/` locally. The `.gitignore` enforces this.

## Environment Configuration

Local environment values should live in `.env`. Add a `.env.example` file when new required variables are introduced, then use `scripts/env-init.sh` to copy `.env.example` files into `.env` files across the repo.

### Development

- If `scripts/env-init.sh` has already been run and this service has a `.env.example`, the local `.env` file should be in place.
- Use `localhost:29092` for Kafka when the app runs on the host.
- Use `kafka:9092` for Kafka when the app runs inside Docker Compose.
- Use reload mode only for local development.
