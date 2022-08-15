package kr.dove.service.restaurant.persistence

import core.state.State
import core.ticket.Ticket
import core.values.OrderItem
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "tickets")
data class TicketEntity(
    @Id val id: String,
    var state: State,
    @Indexed(unique = true) val orderId: String,
    val restaurantId: String,
    var acceptTime: LocalDateTime,
    var readyTime: LocalDateTime? = null,
    var preparingTime: Long? = null,
    var pickedUpTime: LocalDateTime? = null,
    val orderItems: List<OrderItem>,
) {

    fun mapToApi(): Ticket =
        Ticket(
            id,
            state,
            orderId,
            restaurantId,
            acceptTime,
            readyTime,
            preparingTime,
            pickedUpTime,
            orderItems
        )
}
