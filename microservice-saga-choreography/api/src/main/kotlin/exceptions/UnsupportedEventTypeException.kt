package exceptions

class UnsupportedEventTypeException : Throwable {
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
}