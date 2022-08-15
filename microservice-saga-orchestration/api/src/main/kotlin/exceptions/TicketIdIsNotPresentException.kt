package exceptions

class TicketIdIsNotPresentException : Throwable {
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
}