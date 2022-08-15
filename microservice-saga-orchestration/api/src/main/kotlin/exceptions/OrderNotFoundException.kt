package exceptions

class OrderNotFoundException: Throwable {
    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)
}
