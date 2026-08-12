FROM python:3.11-slim

WORKDIR /app

RUN groupadd -r appgroup && useradd -r -m -d /home/appuser -g appgroup appuser && chown -R appuser:appgroup /app 

# Prevent Python from writing .pyc files and buffer logs
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# System dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Install dependencies first for Docker layer caching
COPY requirements.txt .

RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy app source
COPY src .

EXPOSE 5000

USER appuser

CMD ["fastapi", "run", "--port", "5000"]