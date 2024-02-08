FROM node:18-alpine3.18

WORKDIR client

COPY . .

RUN npm install

RUN npm run build && npm install -g serve;

EXPOSE 3000

CMD [ "serve", "-s", "build" ]
