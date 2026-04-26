#syntax=docker/dockerfile:1

# Very basic build, copy requirements, upgrade pip and install deps, run init script, exit

FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .

RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["python3", "kafka_init.py"]