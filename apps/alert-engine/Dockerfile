#syntax=docker/dockerfile:1

# NOTE: This is built for production, not optimized for dev. Use the dev script in package.json while developing
# the alert-engine servcie

# ==== Stage 1: Build ====
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# ==== Stage 2: Runtime ====
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --omit=dev
COPY --from=builder /app/dist ./dist

# run as non-root
RUN addgroup -S app && adduser -S app -G app
USER app
EXPOSE 3000

CMD ["node", "dist/server.js"]
