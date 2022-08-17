package kr.dove.service.customer.persistence

import core.values.CreditCard
import core.values.Location
import core.values.Sexuality
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "customers")
data class CustomerEntity(
    @Id val id: String,
    var firstname: String,
    var lastname: String,
    var fullname: String,
    var sexuality: Sexuality,
    var nationality: String,
    var address: Location,
    var card: CreditCard,
    @Version val version: Int = 0,
)