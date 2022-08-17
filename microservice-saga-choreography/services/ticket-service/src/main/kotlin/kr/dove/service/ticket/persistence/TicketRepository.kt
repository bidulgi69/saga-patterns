package kr.dove.service.ticket.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface TicketRepository : ReactiveMongoRepository<TicketEntity, String>