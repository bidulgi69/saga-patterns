package kr.dove.service.restaurant.persistence

import core.values.Location
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "restaurants")
data class RestaurantEntity(
    @Id val id: String,
    var name: String,
    var location: Location,
    @Version val version: Int = 0,
)
