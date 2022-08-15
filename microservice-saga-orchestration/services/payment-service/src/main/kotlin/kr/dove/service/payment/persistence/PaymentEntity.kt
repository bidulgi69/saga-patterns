package kr.dove.service.payment.persistence

import core.state.State
import core.values.CreditCard
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "payments")
data class PaymentEntity(
    var state: State,
    val orderId: String,
    val customerId: String,
    val ticketId: String,
    var payment: CreditCard? = null,
    @Version val version: Int = 0,
    //  etc
) {
    @Id lateinit var id: String
}
