package exceptions

class AirplaneNotFoundException : Throwable {
    constructor(message: String): super(message)
}