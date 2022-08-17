package exceptions

class TicketNotFoundException : Throwable {
    constructor(message: String): super(message)
}