package kr.dove.service.customer

import core.event.Event
import core.event.EventType
import core.order.Order
import core.values.CreditCard
import core.values.Location
import kr.dove.service.customer.persistence.CustomerEntity
import kr.dove.service.customer.persistence.CustomerRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val streamBridge: StreamBridge,
) : InitializingBean {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun verifyCustomer(orderId: String, order: Order): Mono<Void> {
        return customerRepository.existsById(order.customerId)
            .flatMap { exists ->
                if (!exists) {
                    sendMessage(
                        EventType.CUSTOMER_REJECTED,
                        orderId,
                        null
                    )
                } else {
                    sendMessage(
                        EventType.CUSTOMER_APPROVED,
                        orderId,
                        order
                    )
                }
            }
    }

    private fun <K, T> sendMessage(type: EventType, key: K, data: T?): Mono<Void> {
        return Mono.fromRunnable {
            streamBridge.send(
                "customers-out-0",
                Event(
                    type,
                    key,
                    data,
                    LocalDateTime.now()
                ),
                MediaType.APPLICATION_JSON
            )
        }
    }

    override fun afterPropertiesSet() {
        val customers = Flux.just(
            CustomerEntity(
                id = "1",
                firstname = "Nikola",
                lastname = "Jokic",
                fullname = "Nikola Jokic",
                address = Location.of(39.742043, -104.991531),
                card = CreditCard(
                    cvc = "000",
                    number = "0123456789",
                    yy = "24",
                    mm = "12"
                ),
            ),
            CustomerEntity(
                id = "2",
                firstname = "LeBron",
                lastname = "James",
                fullname = "LeBron Raymone James",
                address = Location.of(34.052235, -118.243683),
                card = CreditCard(
                    cvc = "111",
                    number = "9876543210",
                    yy = "25",
                    mm = "02"
                ),
            ),
            CustomerEntity(
                id = "3",
                firstname = "Benjamin",
                lastname = "Simons",
                fullname = "Benjamin David Simons",
                address = Location.of(40.682732, -73.975876),
                card = CreditCard(
                    cvc = "222",
                    number = "5432109876",
                    yy = "25",
                    mm = "01"
                ),
            )
        )
        customerRepository.saveAll(
            customers
        ).subscribe {
            logger.debug("Insert some customer data...")
        }
    }
}