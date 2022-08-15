package core.event

import java.time.LocalDateTime

data class Event<K, T>(
    val type: EventType,
    val key: K,
    val data: T,
    val publishedAt: LocalDateTime
)

enum class EventType {
    ORDER_CREATED,
    ORDER_APPROVED,
    ORDER_REJECTED,
    CUSTOMER_APPROVED,
    CUSTOMER_REJECTED,
    PAYMENT_APPROVED,
    PAYMENT_REJECTED,
    TICKET_CREATED,
    TICKET_REJECTED,
}