
compile:
	./gradlew :api:build && \
	./gradlew :services:customer-service:build && \
	./gradlew :services:order-service:build && \
	./gradlew :services:restaurant-service:build && \
	./gradlew :services:payment-service:build && \
	docker-compose build

run:
	docker-compose up -d

clean:
	docker-compose down --remove-orphans && \
	docker rmi $$(docker images | grep -e none -e dove-saga-orchestration)
	docker volume rm $$(docker volume ls -qf dangling=true)