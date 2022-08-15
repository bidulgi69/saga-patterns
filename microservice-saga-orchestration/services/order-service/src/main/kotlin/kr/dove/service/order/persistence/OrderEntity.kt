package kr.dove.service.order.persistence

import core.order.Order
import core.state.State
import core.values.CreditCard
import core.values.OrderItem
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "orders")
data class OrderEntity(
    @Id val id: String,
    var state: State,
    val customerId: String,
    val restaurantId: String,
    var payment: CreditCard,
    var orderItems: List<OrderItem>,
    @Version val version: Int = 0,
) {
    fun mapToApi(): Order =
        Order(
            id,
            state,
            customerId,
            restaurantId,
            payment,
            orderItems
        )
}