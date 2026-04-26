#syntax=docker/dockerfile:1

# ==== Stage 1: Build ====
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
# Installs dependencies required for build (dependencies, devDependencies)
RUN npm ci
COPY . .
RUN npm run build

# ==== Stage 2: Runtime ====
FROM node:20-alpine
# TODO: Add dashboard backend user with restricted permissions to restrict container attack surface 
WORKDIR /app
COPY package*.json ./
# Installs dependencies required for runtime (dependencies). Smaller
# image compared to keeping all node_modules that include devDependencies, longer build time since
# node_modules not copied from previous stage
RUN npm ci --omit=dev
COPY --from=builder /app/dist ./dist
EXPOSE 3001

CMD ["node", "dist/main.js"]
