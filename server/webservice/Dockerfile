FROM gradle:8.6-jdk21
LABEL authors="alberto.spadoni3"

ENV SERVICE=webservice
WORKDIR ${SERVICE}

COPY ../ .

RUN chmod +x waitRabbitThenStart.sh
RUN apt-get update && apt-get install -y netcat

RUN gradle clean && gradle build --exclude-task test

EXPOSE 8080
ENTRYPOINT ["./waitRabbitThenStart.sh"]
