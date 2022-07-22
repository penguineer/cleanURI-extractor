#
# This dockerfile expects a compiled artifact in the target folder.
# Call "mvn clean package" first!
#
FROM openjdk:17-jdk-slim

EXPOSE 8080
#HEALTHCHECK --interval=10s CMD curl --fail http://localhost:8080/health || exit 1

COPY target/extractor-*.jar /usr/local/lib/extractor.jar

ENTRYPOINT ["java","-jar","/usr/local/lib/extractor.jar"]
