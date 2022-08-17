package exceptions

class AirlineNotFoundException : Throwable {
    constructor(message: String): super(message)
}