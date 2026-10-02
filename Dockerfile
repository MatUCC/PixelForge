# ---------- 1) Compilar y correr las pruebas ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY src ./src
COPY test ./test
RUN javac -d out $(find src test -name "*.java") \
 && java -Djava.awt.headless=true -cp out com.pixelforge.DecoratorTests

# ---------- 2) Imagen final, solo con el JRE ----------
FROM eclipse-temurin:21-jre
# Fuentes para que WatermarkDecorator pueda dibujar texto en un servidor sin pantalla
RUN apt-get update \
 && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/out ./out
COPY frontend ./frontend
RUN useradd --system pixelforge
USER pixelforge
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-Djava.awt.headless=true", "-XX:MaxRAMPercentage=75", "-cp", "out", "com.pixelforge.api.ApiServer"]
