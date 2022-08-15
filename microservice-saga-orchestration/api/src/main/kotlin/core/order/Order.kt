package core.order

import core.state.State
import core.values.CreditCard
import core.values.OrderItem

data class Order(
    var id: String? = null,
    var state: State = State.PENDING,
    val customerId: String,
    val restaurantId: String,
    val payment: CreditCard,
    val orderItems: List<OrderItem>,
    var ticketId: String? = null,
    var errorRate: Float = 0f,
)
