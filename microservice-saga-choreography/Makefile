
compile:
	./gradlew :api:build && \
	./gradlew :services:ticket-service:build && \
	./gradlew :services:customer-service:build && \
	./gradlew :services:airline-service:build && \
	./gradlew :services:payment-service:build && \
	docker-compose build

bg:
	docker-compose up -d mongodb kafka zookeeper

run:
	docker-compose up -d


clean:
	docker-compose down --remove-orphans && \
	docker volume rm $$(docker volume ls -qf dangling=true) && \
	docker rmi $$(docker images | grep dove-saga-choreography)
