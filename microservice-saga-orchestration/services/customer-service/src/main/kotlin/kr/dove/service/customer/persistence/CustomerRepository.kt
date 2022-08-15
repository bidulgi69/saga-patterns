package kr.dove.service.customer.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface CustomerRepository : ReactiveMongoRepository<CustomerEntity, String>