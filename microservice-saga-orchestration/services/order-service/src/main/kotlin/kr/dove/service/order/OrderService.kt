package kr.dove.service.order

import core.event.Event
import core.event.EventType
import core.order.Order
import core.state.State
import exceptions.OrderIdIsNotPresentException
import kr.dove.service.order.persistence.OrderEntity
import kr.dove.service.order.persistence.OrderRepository
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@RestController
class OrderService(
    private val orderRepository: OrderRepository,
    private val streamBridge: StreamBridge,
) {

    //  The errorRate value is the probability that payment service forces an error to occur.
    @PostMapping(
        value = ["/order"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun create(@RequestBody order: Order, @RequestParam(name = "errorRate", defaultValue = "0") errorRate: Float): Mono<Order> {
        return orderRepository.save(
            OrderEntity(
                id = UUID.randomUUID().toString(),
                state = State.PENDING,
                customerId = order.customerId,
                restaurantId = order.restaurantId,
                payment = order.payment,
                orderItems = order.orderItems,
            )
        ).flatMap { en ->
            //  inject id field
            //  then, send an event to topic "orders"
            sendMessage(
                "order-customer-out-0",
                EventType.ORDER_CREATED,
                en.id,
                order.apply {
                    this.id = en.id
                    this.errorRate = errorRate
                }
            )
        }
    }

    @PutMapping(
        value = ["/order"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun revise(@RequestBody order: Order): Mono<Order> {
        order.id ?. let { orderId ->
            orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(OrderIdIsNotPresentException("Invalid Request.")))
                .flatMap { en ->
                    orderRepository.save(
                        en.apply {
                            this.payment = order.payment
                            this.orderItems = order.orderItems
                        }
                    )
                }
        } ?: run {
            Mono.error(OrderIdIsNotPresentException("Invalid Request."))
        }

        return Mono.empty()
    }

    @GetMapping(
        value = ["/order/{orderId}"],
    )
    fun get(@PathVariable(name = "orderId") orderId: String): Mono<Order> {
        //  idempotent
        return orderRepository.findById(orderId)
            .flatMap { en ->
                Mono.just(
                    en.mapToApi()
                )
            }
    }

    fun approveOrder(orderId: String, order: Order): Mono<Void> {
        return orderRepository.findById(orderId)
            .flatMap { en ->
                orderRepository.save(
                    en.apply {
                        this.state = State.ACCEPTED
                    }
                )
            }.flatMap {
                sendMessage(
                    "order-restaurant-out-0",
                    EventType.ORDER_APPROVED,
                    orderId,
                    order
                )
            }.then()
    }

    //  compensation transaction
    fun rejectOrder(orderId: String, order: Order): Mono<Void> {
        return orderRepository.findById(orderId)
            .flatMap { en ->
                orderRepository.save(
                    en.apply {
                        this.state = State.REJECTED
                    }
                )
            }.flatMap {
                sendMessage(
                    "order-restaurant-out-0",
                    EventType.ORDER_REJECTED,
                    orderId,
                    order
                )
            }.then()
    }

    fun <K, T> sendMessage(bindingName: String, type: EventType, key: K, order: T): Mono<T> {
        return Mono.fromCallable {
            streamBridge.send(
                bindingName,
                Event(
                    type,
                    key,
                    order,
                    LocalDateTime.now()
                ),
                MediaType.APPLICATION_JSON
            )
            order
        }
    }
}