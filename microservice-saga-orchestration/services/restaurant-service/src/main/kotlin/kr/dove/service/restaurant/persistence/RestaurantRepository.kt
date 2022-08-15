package kr.dove.service.restaurant.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface RestaurantRepository : ReactiveMongoRepository<RestaurantEntity, String>