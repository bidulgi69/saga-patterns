package kr.dove.service.customer

import core.event.Event
import core.event.EventType
import core.ticket.Ticket
import core.values.CreditCard
import core.values.Location
import core.values.Sexuality
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

    fun verifyCustomer(ticketId: String, ticket: Ticket): Mono<Void> {
        return customerRepository.existsById(ticket.customerId)
            .flatMap { exists ->
                sendMessage(
                    "customers-out-${if (!exists) 1 else 0}",
                    if (!exists) EventType.CUSTOMER_REJECTED else EventType.CUSTOMER_APPROVED,
                    ticketId,
                    ticket
                )
            }
    }

    fun <K, T> sendMessage(bindingName: String, eventType: EventType, key: K, ticket: T): Mono<Void> {
        return Mono.fromRunnable {
            streamBridge.send(
                bindingName,
                Event(
                    eventType,
                    key,
                    ticket,
                    LocalDateTime.now()
                ),
                MediaType.APPLICATION_JSON
            )
        }
    }

    override fun afterPropertiesSet() {
        val customers = Flux.just(
            CustomerEntity(
                "1",
                "BeomSoo",
                "Jeon",
                "BeomSoo Jeon",
                Sexuality.Male,
                "KR",
                Location.of(37.456257, 126.705208),
                CreditCard(
                    "123",
                    "0123456789",
                    "25",
                    "02"
                )
            ),
            CustomerEntity(
                "2",
                "Stephen",
                "Bruner",
                "Stephen Lee Bruner",
                Sexuality.Male,
                "US",
                Location.of(36.778259, -119.417931),
                CreditCard(
                    "456",
                    "9876543210",
                    "24",
                    "12"
                )
            )
        )

        customers.flatMap(customerRepository::save)
            .subscribe {
                logger.debug("Insert some customer data...")
            }
    }
}