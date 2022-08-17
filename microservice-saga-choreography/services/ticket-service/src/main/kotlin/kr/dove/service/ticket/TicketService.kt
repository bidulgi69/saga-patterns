package kr.dove.service.ticket

import core.event.Event
import core.event.EventType
import core.state.State
import core.ticket.Ticket
import exceptions.TicketNotFoundException
import kr.dove.service.ticket.persistence.TicketEntity
import kr.dove.service.ticket.persistence.TicketRepository
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@RestController
class TicketService(
    private val ticketRepository: TicketRepository,
    private val streamBridge: StreamBridge,
) {

    @PostMapping(
        value = ["/ticket"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createTicket(@RequestBody ticket: Ticket): Mono<Ticket> {
        return ticketRepository.save(
            TicketEntity(
                UUID.randomUUID().toString(),
                State.PENDING,
                ticket.type,
                ticket.customerId,
                ticket.payment,
                ticket.airlineId,
                ticket.airplaneId,
                ticket.seat,
                ticket.departure,
                ticket.arrival,
                ticket.departureTime,
                ticket.arrivalTime,
            )
        ).flatMap { en ->
            sendMessage(
                en.id,
                ticket.apply {
                    this.ticketId = en.id
                }
            )
        }
    }

    @GetMapping(
        value = ["/ticket/{ticketId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getTicket(@PathVariable(name = "ticketId") ticketId: String): Mono<Ticket> {
        return ticketRepository.findById(ticketId)
            .flatMap { en ->
                Mono.just(
                    en.mapToApi()
                )
            }
            .switchIfEmpty(
                Mono.error(
                    TicketNotFoundException("Invalid ticket id.")
                )
            )
    }

    fun approveTicket(ticketId: String): Mono<Void> {
        return ticketRepository.findById(ticketId)
            .flatMap { en ->
                ticketRepository.save(
                    en.apply {
                        this.state = State.APPROVED
                    }
                )
            }.then()
    }

    fun rejectTicket(ticketId: String): Mono<Void> {
        return ticketRepository.findById(ticketId)
            .flatMap { en ->
                ticketRepository.save(
                    en.apply {
                        this.state = State.REJECTED
                    }
                )
            }.then()
    }

    fun <K, T> sendMessage(key: K, ticket: T): Mono<T> {
        return Mono.fromCallable {
            streamBridge.send(
                "tickets-out-0",
                Event(
                    EventType.TICKET_CREATED,
                    key,
                    ticket,
                    LocalDateTime.now()
                ),
                MediaType.APPLICATION_JSON
            )
            ticket
        }
    }
}