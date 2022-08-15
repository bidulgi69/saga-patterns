package core.state

enum class State(
    private val order: Int
) {
    PENDING(0),
    ACCEPTED(1),
    REJECTED(2),
//  after the order has been approved,
//  below state values will be used in the process from food preparation to delivery
//    PREPARING(3),
//    READY(4),
//    DELIVERING(5);
}