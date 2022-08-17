package kr.dove.service.airline.persistence

import core.state.State
import core.values.Airport
import core.values.SeatClass
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "reservations")
data class ReservationEntity(
    @Id val id: String,
    var state: State,
    val airplaneId: String,
    var seatClass: SeatClass,
    var seatNumber: Int,
    val departure: Airport,
    val arrival: Airport,
    val departureTime: LocalDateTime,
)
