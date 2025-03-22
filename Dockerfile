# Base image with Java
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests

# Final image
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Install Tailscale
RUN apt-get update && apt-get install -y iproute2 curl unzip && \
    curl -fsSL https://tailscale.com/install.sh | sh

# Start Tailscale in the background before running Spring Boot
CMD (tailscaled --tun=userspace-networking &) && \
    sleep 2 && \
    tailscale up --authkey=$TS_AUTHKEY --hostname=koyeb-tailscale && \
    java -jar app.jar