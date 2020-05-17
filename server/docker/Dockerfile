FROM adoptopenjdk/openjdk13:alpine-slim
COPY ./*.jar /usr/app/deciderator-server.jar
# COPY ./run.sh /usr/app/run.sh

WORKDIR /usr/app
EXPOSE 8080

# RUN apk add --no-cache bash # needed for below
CMD java \
  -XX:+PrintCommandLineFlags \
  -server \
  -Xms2G \
  -Xmx2G \
  -jar deciderator-server.jar
