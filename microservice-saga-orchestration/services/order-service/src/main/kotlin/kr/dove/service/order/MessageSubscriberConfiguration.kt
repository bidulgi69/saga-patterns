package kr.dove.service.order

import core.event.Event
import core.event.EventType
import core.order.Order
import exceptions.UnsupportedEventTypeException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class MessageSubscriberConfiguration(
    private val orderService: OrderService,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun orderReplyMessageSubscriber(): Consumer<Event<String, Order>> {
        return Consumer<Event<String, Order>> { (type, key, order, publishedAt) ->
            logger.info("Processing a reply message published at {} and event type is {}", publishedAt, type)
            when (type) {
                EventType.CUSTOMER_APPROVED -> {
                    //  send restaurant service to create a ticket
                    orderService.sendMessage(
                        "order-restaurant-out-0",
                        EventType.ORDER_CREATED,
                        key,
                        order
                    ).subscribe()
                }
                EventType.TICKET_CREATED -> {
                    //  send payment service to approve payment
                    orderService.sendMessage(
                        "order-payment-out-0",
                        EventType.ORDER_CREATED,
                        key,
                        order
                    ).subscribe()
                }
                EventType.TICKET_REJECTED -> {
                    orderService.rejectOrder(
                        key,
                        order
                    ).subscribe()
                }
                EventType.PAYMENT_APPROVED -> {
                    orderService.approveOrder(key, order)
                        .subscribe()
                }
                EventType.PAYMENT_REJECTED -> {
                    orderService.sendMessage(
                        "order-restaurant-out-0",
                        EventType.ORDER_REJECTED,
                        key,
                        order
                    ).subscribe()
                }
                else -> throw UnsupportedEventTypeException("Invalid event.")
            }
        }
    }
}