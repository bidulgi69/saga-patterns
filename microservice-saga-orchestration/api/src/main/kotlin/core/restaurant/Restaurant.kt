package core.restaurant

import core.values.Location

data class Restaurant(
    val restaurantId: String,
    var name: String,
    var address: Location,
)
