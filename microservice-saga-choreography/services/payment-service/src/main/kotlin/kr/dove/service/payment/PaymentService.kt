package kr.dove.service.payment

import core.event.Event
import core.event.EventType
import core.state.State
import core.ticket.Ticket
import kr.dove.service.payment.persistence.PaymentEntity
import kr.dove.service.payment.persistence.PaymentRepository
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val streamBridge: StreamBridge,
) {

    fun approvePayment(ticketId: String, ticket: Ticket): Mono<Void> {
        val forcedError = (Math.random() * 1).toFloat() >= 0.5f
        return paymentRepository.save(
            PaymentEntity(
                if (forcedError) State.REJECTED else State.APPROVED,
                ticketId,
                ticket.customerId,
                ticket.payment,
            )
        ).then(
            sendMessage(
                "payments-out-0",
                if (forcedError) EventType.PAYMENT_REJECTED else EventType.PAYMENT_APPROVED,
                ticketId,
                ticket
            )
        )
    }

    fun <K, T> sendMessage(bindingName: String, eventType: EventType, key: K, ticket: T): Mono<Void> {
        return Mono.fromRunnable {
            streamBridge.send(
                bindingName,
                Event(
                    eventType,
                    key,
                    ticket,
                    LocalDateTime.now(),
                ),
                MediaType.APPLICATION_JSON
            )
        }
    }
}