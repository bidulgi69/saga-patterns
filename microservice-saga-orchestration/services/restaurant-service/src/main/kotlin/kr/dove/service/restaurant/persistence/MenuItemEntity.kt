package kr.dove.service.restaurant.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "menuItems")
@CompoundIndex(
    name = "menu-item-id",
    unique = true,
    def = "{'restaurantId': 1, 'menuItemId': 1}"
)
data class MenuItemEntity(
    val restaurantId: String,
    val menuItemId: String,
    var name: String,
    var description: String,
    var price: Int,
    var rating: Float = 0f,
    @Version val version: Int = 0,
) {
    @Id lateinit var id: String
        private set
}