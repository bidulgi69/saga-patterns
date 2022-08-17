package kr.dove.service.airline.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface AirplaneRepository : ReactiveMongoRepository<AirplaneEntity, String>