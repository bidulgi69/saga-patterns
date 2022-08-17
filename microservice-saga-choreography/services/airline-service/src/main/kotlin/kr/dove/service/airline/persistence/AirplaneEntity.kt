package kr.dove.service.airline.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "airplanes")
data class AirplaneEntity(
    @Id val id: String,
    var airlineId: String,
    var name: String,
    var code: String,
    val madeIn: String,
    val madeAt: LocalDate,
    @Version val version: Int = 0,
)
