package core.restaurant

data class MenuItem(
    val menuItemId: String,
    val restaurantId: String,
    var name: String,
    var description: String,
    var price: Int,
    var rating: Float = 0f,
)
