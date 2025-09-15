FROM node:24-alpine AS builder

WORKDIR /app

COPY front-end/package.json front-end/package-lock.json ./

RUN npm ci

COPY front-end/ .

RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html

