package kr.dove.service.restaurant.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface TicketRepository : ReactiveMongoRepository<TicketEntity, String> {
    fun findByOrderId(orderId: String): Mono<TicketEntity>
}