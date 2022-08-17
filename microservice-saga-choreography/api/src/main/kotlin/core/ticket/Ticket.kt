package core.ticket

import core.values.Airport
import core.values.CreditCard
import core.seat.Seat
import core.state.State
import core.values.TicketType
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class  Ticket(
    var ticketId: String? = null,
    var state: State = State.PENDING,
    val type: TicketType,
    val customerId: String,
    val payment: CreditCard,
    val airlineId: String,
    val airplaneId: String,
    val seat: Seat,
    val departure: Airport,
    val arrival: Airport,
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val departureTime: LocalDateTime,
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val arrivalTime: LocalDateTime,
)