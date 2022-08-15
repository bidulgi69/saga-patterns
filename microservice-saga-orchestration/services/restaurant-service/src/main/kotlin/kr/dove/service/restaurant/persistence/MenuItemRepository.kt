package kr.dove.service.restaurant.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface MenuItemRepository : ReactiveMongoRepository<MenuItemEntity, String>