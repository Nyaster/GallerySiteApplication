FROM maven as build

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
COPY model ./model

RUN mvn clean package -DskipTests

FROM eclipse-temurin

WORKDIR /app

COPY --from=build /app/target/GallerySite.jar ./app.jar

COPY --from=build /app/model ./model

RUN mkdir -p /app/image

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]