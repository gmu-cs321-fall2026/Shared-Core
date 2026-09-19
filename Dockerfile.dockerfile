#Uses openJDK as base image
FROM eclipse-temurin:25
#Goes to app working directory
WORKDIR /app
#Copies code into container
COPY . .
#Installs dependencies
RUN javac HelloWorld.java
#Runs the code
CMD ["java", "HelloWorld"]