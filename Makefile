
JAR_FILE = build/libs/challenge-0.0.1-SNAPSHOT.jar

default: build

build: 
	./gradlew clean build

run: build
	java -jar $(JAR_FILE)

docker-build: docker build -t tenpo .

docker-create-net: docker network create tenpo-net

docker-compose-up: docker compose -f docker/docker-compose.yml up -d

docker-compose-down: docker compose -f docker/docker-compose.yml down