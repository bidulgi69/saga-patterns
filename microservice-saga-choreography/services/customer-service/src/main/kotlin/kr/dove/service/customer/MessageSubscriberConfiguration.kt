package kr.dove.service.customer

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
    private val customerService: CustomerService,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun ticketMessageSubscriber(): Consumer<Event<String, Ticket>> {
        return Consumer<Event<String, Ticket>> { (type, key, ticket, publishedAt) ->
            logger.info("Processing message from topic 'tickets' published at {} and event type is {}", publishedAt, type)
            when (type) {
                EventType.TICKET_CREATED -> {
                    customerService.verifyCustomer(
                        key,
                        ticket,
                    ).subscribe()
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