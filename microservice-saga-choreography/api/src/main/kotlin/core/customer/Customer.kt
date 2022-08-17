package core.customer

import core.values.CreditCard
import core.values.Location
import core.values.Sexuality

data class Customer(
    val customerId: String,
    var firstname: String,
    var lastname: String,
    var fullname: String,
    var sexuality: Sexuality,
    var nationality: String,
    var address: Location,
    var card: CreditCard,
)