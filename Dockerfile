# Step 1: Build stage
# Use the official Maven image to build the application
FROM maven:3.9.7-eclipse-temurin-17-alpine as build

# Set the working directory inside the container
ENV HOME=/opt/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
# Package the application into a JAR file
RUN mvn clean package -DskipTests

# Step 2: Run stage
# Use the official JDK 17 image to run the application
FROM maven:3.9.7-eclipse-temurin-17-alpine
# Copy the JAR file from the build stage to the run stage
COPY --from=build /opt/app/target/*.jar ksei-investor-service.jar
# Expose the port the application will run on
EXPOSE 8080
# Run the JAR file
ENTRYPOINT ["java","-jar","/ksei-investor-service.jar"]