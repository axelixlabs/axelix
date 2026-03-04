FROM node:22-alpine

WORKDIR /app

ENV NODE_ENV=production

COPY package.json package-lock.json ./
RUN npm install --omit=dev

COPY . .

RUN npm run build

EXPOSE 3000

CMD ["npm", "run", "serve"]