# syntax=docker/dockerfile:1

# For production images, requires --build-arg API_URL="https://whatever/api", explicitly
# left out default value to ensure that the arg is explicitly set 
ARG API_URL

# ==== Base ====
FROM node:24-alpine AS base

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .


# ==== Dev ====
FROM base AS dev

CMD ["npm", "run", "dev"]


# ==== Build ====
FROM base AS build

ARG API_URL
ENV NEXT_PUBLIC_API_URL=$API_URL
ENV DISABLE_AUTH=false

RUN npm run build


# ==== Prod ====
FROM node:24-alpine AS prod

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000

COPY package*.json ./
RUN npm ci --omit=dev

COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public
COPY --from=build /app/next.config.* ./

USER node

EXPOSE 3000

CMD ["npm", "start"]