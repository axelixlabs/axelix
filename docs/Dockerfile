FROM node:22-alpine

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000

COPY build ./build

RUN adduser --system --uid 1001 docs
USER docs

RUN npm install serve -g

EXPOSE 3000

CMD ["serve", "-s", "build", "-l", "3000"]