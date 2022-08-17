package kr.dove.service.ticket

import core.event.Event
import core.event.EventType
import core.ticket.Ticket
import exceptions.UnsupportedEventTypeException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class MessageSubscriberConfiguration(
    private val ticketService: TicketService,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun pivotMessageSubscriber(): Consumer<Event<String, Ticket>> {
        return Consumer<Event<String, Ticket>> { (type, key, _, publishedAt) ->
            logger.info("Processing message from topic 'pivot' published at {} and event type is {}", publishedAt, type)
            when (type) {
                EventType.PAYMENT_APPROVED -> {
                    ticketService.approveTicket(key)
                        .subscribe()
                }
                EventType.CUSTOMER_REJECTED,
                EventType.SEAT_REJECTED,
                EventType.PAYMENT_REJECTED -> {
                    ticketService.rejectTicket(key)
                        .subscribe()
                }
                else -> {
                    val errorMessage = "Event type $type is not supported."
                    logger.error(errorMessage)
                    throw UnsupportedEventTypeException(errorMessage)
                }
            }
        }
    }
}