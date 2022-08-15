package kr.dove.service.restaurant

import core.event.Event
import core.event.EventType
import core.order.Order
import exceptions.TicketIdIsNotPresentException
import exceptions.UnsupportedEventTypeException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class MessageSubscriberConfiguration(
    private val restaurantService: RestaurantService,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun orderMessageSubscriber(): Consumer<Event<String, Order>> {
        return Consumer<Event<String, Order>> { (type, key, order, publishedAt) ->
            logger.info("Processing a message from topic 'order-restaurant' published at {} and event type is {}", publishedAt, type)
            val rand = (Math.random() * 1).toFloat()
            when (type) {
                EventType.ORDER_CREATED -> {
                    if (order.errorRate >= rand) {
                        restaurantService.rejectTicket(key, order)
                            .subscribe()
                    } else {
                        restaurantService.createTicket(key, order)
                            .subscribe()
                    }
                }
                EventType.ORDER_APPROVED -> {
                    order.ticketId ?. let { ticketId ->
                        restaurantService.approveTicket(key, ticketId)
                            .subscribe()
                    } ?: throw TicketIdIsNotPresentException("Invalid request.")
                }
                EventType.ORDER_REJECTED -> {
                    restaurantService.rejectTicket(key, order)
                        .subscribe()
                }
                else -> throw UnsupportedEventTypeException("Invalid event.")
            }
        }
    }
}