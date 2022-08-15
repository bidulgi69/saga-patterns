package kr.dove.service.restaurant

import core.event.Event
import core.event.EventType
import core.order.Order
import core.state.State
import core.values.Location
import kr.dove.service.restaurant.persistence.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepository,
    private val menuItemRepository: MenuItemRepository,
    private val ticketRepository: TicketRepository,
    private val streamBridge: StreamBridge,
) : InitializingBean {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun createTicket(orderId: String, order: Order): Mono<Void> {
        return ticketRepository.save(
            TicketEntity(
                id = UUID.randomUUID().toString(),
                state = State.PENDING,
                orderId = orderId,
                restaurantId = order.restaurantId,
                acceptTime = LocalDateTime.now(),
                orderItems = order.orderItems,
            )
        ).flatMap { en ->
            sendMessage(
                "tickets-out-0",
                EventType.TICKET_CREATED,
                orderId,
                order.apply {
                    this.ticketId = en.id
                }
            )
        }
    }

    fun approveTicket(orderId: String, ticketId: String): Mono<Void> {
        return ticketRepository.findById(ticketId)
            .flatMap { en ->
                ticketRepository.save(
                    en.apply {
                        this.state = State.ACCEPTED
                    }
                )
                //  then send an event to kitchen service to make foods...
//                sendMessage(
//                    "kitchens",
//                    EventType.APPROVED,
//                    orderId,
//                    ticket,
//                )
            }.then()
    }

    //  compensation transaction
    fun rejectTicket(orderId: String, order: Order): Mono<Void> {
        val ticketEntity: Mono<TicketEntity> = order.ticketId ?. let { ticketId ->
            ticketRepository.findById(ticketId)
        } ?: run {
            ticketRepository.findByOrderId(orderId)
        }

        return ticketEntity
            .flatMap { en ->
                ticketRepository.save(
                    en.apply {
                        this.state = State.REJECTED
                    }
                )
            }.then(
                sendMessage(
                    "tickets-out-0",
                    EventType.TICKET_REJECTED,
                    orderId,
                    order,
                )
            )
    }

    private fun <K, T> sendMessage(bindingName: String, type: EventType, key: K, ticket: T): Mono<Void> {
        return Mono.fromRunnable {
            streamBridge.send(
                bindingName,
                Event(
                    type,
                    key,
                    ticket,
                    LocalDateTime.now()
                )
            )
        }
    }

    override fun afterPropertiesSet() {
        val restaurants = Flux.just(
            RestaurantEntity(
                "1",
                "KimBap heaven",
                Location.of(51.54787, 54.49801),
            ),
            RestaurantEntity(
                "2",
                "Sushi-ro",
                Location.of(30.13828, -94.55143),
            ),
            RestaurantEntity(
                "3",
                "Yeon-Tonkatsu",
                Location.of(33.5097, 126.5219),
            )
        )

        val menuItems = Flux.just(
            MenuItemEntity(
                restaurantId = "1",
                menuItemId = "1",
                name = "KimBap",
                description = "Basic seaweed and rice combo",
                price = 3500,
                rating = 3.6f,
            ),
            MenuItemEntity(
                restaurantId = "1",
                menuItemId = "2",
                name = "Tonkatsu",
                description = "Pork cutlet with tart sauce",
                price = 6500,
                rating = 3.8f,
            ),
            MenuItemEntity(
                restaurantId = "3",
                menuItemId = "3",
                name = "Jeyuk-Bokkeum",
                description = "Meat, seafood, or vegetables seasoned and quickly stir-fried over a high flame",
                price = 6500,
                rating = 4.0f,
            ),
            MenuItemEntity(
                restaurantId = "2",
                menuItemId = "4",
                name = "Salmon sushi",
                description = "Sushi topped with salmon and wasabi",
                price = 2000,
                rating = 3.3f,
            ),
            MenuItemEntity(
                restaurantId = "2",
                menuItemId = "5",
                name = "Egg sushi",
                description = "Sushi topped with sweet egg",
                price = 2500,
                rating = 3.3f,
            ),
            MenuItemEntity(
                restaurantId = "2",
                menuItemId = "6",
                name = "Flatfish sushi",
                description = "Sushi topped with flatfish and wasabi",
                price = 3000,
                rating = 3.3f
            ),
            MenuItemEntity(
                restaurantId = "3",
                menuItemId = "7",
                name = "Sirloin pork cutlet",
                description = "Best seller of this restaurant",
                price = 11000,
                rating = 4.8f
            ),
            MenuItemEntity(
                restaurantId = "3",
                menuItemId = "8",
                name = "Tenderloin pork cutlet",
                description = "Another best seller of this restaurant",
                price = 12000,
                rating = 4.9f
            ),
            MenuItemEntity(
                restaurantId = "3",
                menuItemId = "9",
                name = "Cheese pork cutlet",
                description = "Sirloin pork cutlet with melted cheese topped",
                price = 13000,
                rating = 4.6f
            )
        )
        Flux.zip(
            restaurantRepository.saveAll(restaurants),
            menuItemRepository.saveAll(menuItems)
        ).subscribe {
            logger.debug("Insert some restaurant data...")
        }
    }
}