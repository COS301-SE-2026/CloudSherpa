# syntax=docker/dockerfile:1

# ==== Base ====
FROM node:24-alpine AS build

WORKDIR /app

COPY package*.json .

COPY . . 

RUN npm ci && npm run build

FROM node:24-alpine

COPY package*.json ./
RUN npm ci --omit=dev

COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public
COPY --from=build /app/next.config.* ./

EXPOSE 3000

USER node

CMD ["npm", "start"]