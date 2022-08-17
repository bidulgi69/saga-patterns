package kr.dove.service.ticket.persistence

import core.state.State
import core.ticket.Ticket
import core.values.Airport
import core.values.CreditCard
import core.seat.Seat
import core.values.TicketType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "tickets")
data class TicketEntity(
    @Id val id: String,
    var state: State,
    val type: TicketType,
    val customerId: String,
    val payment: CreditCard,
    val airlineId: String,
    val airplaneId: String,
    val seat: Seat,
    val departure: Airport,
    val arrival: Airport,
    val departureTime: LocalDateTime,
    val arrivalTime: LocalDateTime,
    @Version val version: Int = 0,
) {
    fun mapToApi(): Ticket =
        Ticket(
            id,
            state,
            type,
            customerId,
            payment,
            airlineId,
            airplaneId,
            seat,
            departure,
            arrival,
            departureTime,
            arrivalTime
        )
}