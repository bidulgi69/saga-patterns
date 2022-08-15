package core.ticket

import core.state.State
import core.values.OrderItem
import java.time.LocalDateTime

data class Ticket(
    val ticketId: String,
    var state: State = State.PENDING,
    val orderId: String,
    val restaurantId: String,
    var acceptTime: LocalDateTime = LocalDateTime.now(),
    var readyTime: LocalDateTime? = null,
    var preparingTime: Long? = null,
    var pickedUpTime: LocalDateTime? = null,
    val orderItems: List<OrderItem>,
)
