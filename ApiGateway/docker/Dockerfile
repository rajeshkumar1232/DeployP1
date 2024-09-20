# Use a lightweight base image
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

COPY target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar

EXPOSE 8062

## Set environment variables if needed
# ENV JAVA_OPTS="-Xmx256m -Xms128m"

# Run the application
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
