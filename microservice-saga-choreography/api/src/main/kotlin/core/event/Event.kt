package core.event

import java.time.LocalDateTime

data class Event<K, T>(
    val type: EventType,
    val key: K,
    val data: T,
    val publishedAt: LocalDateTime
)

enum class EventType {
    TICKET_CREATED,
    CUSTOMER_APPROVED,
    CUSTOMER_REJECTED,
    SEAT_RESERVED,
    SEAT_REJECTED,
    PAYMENT_APPROVED,
    PAYMENT_REJECTED,
}