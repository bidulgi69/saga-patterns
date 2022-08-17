package core.seat

import core.values.SeatClass

data class Seat(
    var reservationId: String? = null,
    val airplaneId: String,
    val seatClass: SeatClass,
    val seatNumber: Int,
)