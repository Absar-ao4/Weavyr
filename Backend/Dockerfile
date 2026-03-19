FROM node:20-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

RUN npx prisma generate

EXPOSE 8787

CMD ["npx", "wrangler", "dev", "--ip", "0.0.0.0"]