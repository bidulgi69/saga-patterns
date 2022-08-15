package kr.dove.service.customer

import core.event.Event
import core.event.EventType
import core.order.Order
import exceptions.UnsupportedEventTypeException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
class OrderMessageSubscriberConfiguration(
    private val customerService: CustomerService,
) {

    @Bean
    fun orderMessageSubscriber(): Consumer<Event<String, Order>> {
        return Consumer<Event<String, Order>> { (type, key, order) ->
            when (type) {
                EventType.ORDER_CREATED -> {
                    customerService.verifyCustomer(key, order)
                        .subscribe()
                }
                else -> throw UnsupportedEventTypeException("Invalid event.")
            }
        }
    }
}