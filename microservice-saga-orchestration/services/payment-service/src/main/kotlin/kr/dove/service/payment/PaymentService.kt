package kr.dove.service.payment

import core.event.Event
import core.event.EventType
import core.order.Order
import core.state.State
import exceptions.TicketIdIsNotPresentException
import kr.dove.service.payment.persistence.PaymentEntity
import kr.dove.service.payment.persistence.PaymentRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val streamBridge: StreamBridge,
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun approvePayment(orderId: String, order: Order): Mono<Void> {
        return order.ticketId ?. let { ticketId ->
            paymentRepository.save(
                PaymentEntity(
                    state = State.ACCEPTED,
                    orderId = orderId,
                    customerId = order.customerId,
                    ticketId = ticketId,
                    payment = order.payment,
                )
            ).then(
                sendMessage(
                    EventType.PAYMENT_APPROVED,
                    orderId,
                    order
                )
            )
        } ?: run {
            throw TicketIdIsNotPresentException("Invalid request.")
        }
    }

    fun rejectPayment(orderId: String, order: Order): Mono<Void> {
        return paymentRepository.save(
            PaymentEntity(
                state = State.REJECTED,
                orderId = orderId,
                customerId = order.customerId,
                ticketId = order.ticketId!!,
                payment = order.payment,
            )
        ).then(
            sendMessage(
                EventType.PAYMENT_REJECTED,
                orderId,
                order,
            )
        )
    }

    private fun <K> sendMessage(type: EventType, key: K, order: Order): Mono<Void> {
        return Mono.fromRunnable {
            streamBridge.send(
                "payment-out-0",
                Event(
                    type,
                    key,
                    order,
                    LocalDateTime.now()
                )
            )
        }
    }
}