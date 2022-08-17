package exceptions

class SeatAlreadyTakenException : Throwable {
    constructor(message: String): super(message)
}