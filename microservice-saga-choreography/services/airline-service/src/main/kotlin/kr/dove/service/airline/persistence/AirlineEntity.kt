package kr.dove.service.airline.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "airlines")
data class AirlineEntity(
    @Id val id: String,
    val name: String,
    @Version val version: Int = 0,
)
