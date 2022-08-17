package kr.dove.service.airline

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
    private val airlineService: AirlineService,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun customerMessageSubscriber(): Consumer<Event<String, Ticket>> {
        return Consumer<Event<String, Ticket>> { (type, key, ticket, publishedAt) ->
            logger.info("Processing message from topic 'customers' published at {} and event type is {}", publishedAt, type)
            when (type) {
                EventType.CUSTOMER_APPROVED -> {
                    airlineService.reserveSeat(
                        key, ticket
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

    @Bean
    fun pivotMessageSubscriber(): Consumer<Event<String, Ticket>> {
        return Consumer<Event<String, Ticket>> { (type, _, ticket, publishedAt) ->
            logger.info("Processing message from topic 'pivot' published at {} and event type is {}", publishedAt, type)
            when (type) {
                EventType.PAYMENT_APPROVED -> {
                    airlineService.approveSeat(
                        ticket
                    ).subscribe()
                }
                EventType.PAYMENT_REJECTED -> {
                    airlineService.cancelSeat(
                        ticket
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