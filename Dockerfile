FROM openjdk:16-oraclelinux7

ADD target/uberjar/app-0.0.1-standalone.jar /app.jar

ADD gcp.json /gcp/gcp.json

CMD java -XX:-OmitStackTraceInFastThrow -jar /app.jar -m mybox.core
