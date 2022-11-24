FROM docker.io/clojure:temurin-19-tools-deps-alpine AS builder
WORKDIR /app
copy . .
RUN clojure -T:build ci
FROM docker.io/eclipse-temurin:19.0.1_10-jre-ubi9-minimal AS runner
COPY --from=builder /app/app.jar ./app.jar
ENV PORT="8080"
ENV HOST="0.0.0.0"
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

