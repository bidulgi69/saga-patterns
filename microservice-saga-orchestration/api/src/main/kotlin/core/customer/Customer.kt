package core.customer

import core.values.CreditCard
import core.values.Location;

data class Customer(
    val customerId: String,
    var firstname: String,
    var lastname: String,
    var fullname: String,
    var address: Location,
    var card: CreditCard,
) {
}