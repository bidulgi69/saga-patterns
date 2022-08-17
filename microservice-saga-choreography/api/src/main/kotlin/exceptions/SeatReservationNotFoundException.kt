package exceptions

class SeatReservationNotFoundException : Throwable {
    constructor(message: String): super(message)
}