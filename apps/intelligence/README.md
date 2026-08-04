# Running the Intelligence Service

## Locally

1. Initialize & activate python venv

```sh
python3 -m venv .venv
source .venv/bin/activate
```

2. Install dependencies
```sh
pip install -r requirements.txt
```

3. Run fastapi dev server
```sh
cd src
fastapi dev --port <of-your-choosing>
```

## Docker

> Note that making GPU resources accessible to container not yet configured, so model will use cpu for inference 

1. Build the image
```sh
docker build -t intelligence-service -f Dockerfile .
```

2. Run 
```sh
docker run -p 5000:5000 --name intelligence-service-container intelligence-service
```

# Design
## Informal flowchart
```mermaid
flowchart TB
A[Dashboard] <--> |REST|B["Application (service)"]
C[(sherpa-db)] e2@==>B
e2@{ animate: true }

B <--> |"REST (Authorization Header API key)"|E

subgraph Intelligence ["Intelligence Service (Deployed seperately from AWS deployment, self hosted for GPU)"]

    E[Model Serving]
    F[Model Interface]
    E -->|Inference call| F
    F --> |Inference return|E
    F --> |Dispatch|G[Chronos-2]
end
```