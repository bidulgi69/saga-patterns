package kr.dove.service.payment.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface PaymentRepository : ReactiveMongoRepository<PaymentEntity, String>