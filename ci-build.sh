#!/bin/bash
./mvnw clean package
java -jar target/vertx-springboot-realworld-example-app.jar > service.log &
SERVICE_PROCESS=$!
tail -f -n0 service.log | grep -q 'vertx-springboot-realworld-example-app.jar started'
echo "Application started"
./collections/run-api-tests.sh
kill $SERVICE_PROCESS
rm service.log

