package exceptions

class SeatReservationIdIsNotPresentException : Throwable {
    constructor(message: String): super(message)
}