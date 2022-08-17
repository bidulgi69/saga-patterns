package kr.dove.service.airline

import core.event.Event
import core.event.EventType
import core.ticket.Ticket
import core.state.State
import exceptions.*
import kr.dove.service.airline.persistence.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class AirlineService(
    private val airlineRepository: AirlineRepository,
    private val airplaneRepository: AirplaneRepository,
    private val reservationRepository: ReservationRepository,
    private val streamBridge: StreamBridge,
) : InitializingBean {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun reserveSeat(ticketId: String, ticket: Ticket): Mono<Void> {
        return airlineRepository.existsById(ticket.airlineId)
            .switchIfEmpty(Mono.error(AirlineNotFoundException("Invalid airline id.")))
            .flatMap {
                airplaneRepository.existsById(ticket.airplaneId)
            }.switchIfEmpty(
                Mono.error(AirplaneNotFoundException("Invalid airplane id."))
            ).flatMap {
                reservationRepository.existsByAirplaneIdAndDepartureAndArrivalAndDepartureTimeAndSeatClassAndSeatNumberAndStateNot(
                    airplaneId = ticket.airlineId,
                    departure = ticket.departure,
                    arrival = ticket.arrival,
                    departureTime = ticket.departureTime,
                    seatClass = ticket.seat.seatClass,
                    seatNumber = ticket.seat.seatNumber,
                    state = State.REJECTED
                )
            }.flatMap { reserved ->
                with(ticket.seat) {
                    if (!reserved) {
                            reservationRepository.save(
                                ReservationEntity(
                                    UUID.randomUUID().toString(),
                                    State.PENDING,
                                    ticket.airplaneId,
                                    this.seatClass,
                                    this.seatNumber,
                                    ticket.departure,
                                    ticket.arrival,
                                    ticket.departureTime
                                )
                            )
                    } else Mono.error(SeatAlreadyTakenException("Cannot reserve the seat ${this.seatClass}/ ${this.seatNumber}."))
                }
            }.flatMap { en ->
                sendMessage(
                    "airlines-out-0",
                    EventType.SEAT_RESERVED,
                    ticketId,
                    ticket.apply {
                        this.seat.reservationId = en.id
                    }
                )
            }.onErrorResume {
                sendMessage(
                    "airlines-out-1",
                    EventType.SEAT_REJECTED,
                    ticketId,
                    ticket
                )
            }
    }

    fun approveSeat(ticket: Ticket): Mono<Void> {
        return ticket.seat.reservationId ?. let { reservationId ->
            reservationRepository.findById(reservationId)
                .switchIfEmpty(Mono.error(SeatReservationNotFoundException("Invalid seat id.")))
                .flatMap { en ->
                    reservationRepository.save(
                        en.apply {
                            this.state = State.APPROVED
                        }
                    )
                }.then()
        } ?: run {
            Mono.error(SeatReservationIdIsNotPresentException("Invalid aggregate."))
        }
    }

    fun cancelSeat(ticket: Ticket): Mono<Void> {
        return ticket.seat.reservationId ?. let { reservationId ->
            reservationRepository.findById(reservationId)
                .flatMap { en ->
                    reservationRepository.save(
                        en.apply {
                            this.state = State.REJECTED
                        }
                    )
                }.then()
        } ?: run {
            Mono.error(SeatReservationIdIsNotPresentException("Invalid aggregate."))
        }
    }

    private fun <K, T> sendMessage(bindingName: String, eventType: EventType, key: K, ticket: T): Mono<Void> {
        return Mono.fromRunnable {
            streamBridge.send(
                bindingName,
                Event(
                    eventType,
                    key,
                    ticket,
                    LocalDateTime.now()
                ),
                MediaType.APPLICATION_JSON
            )
        }
    }

    override fun afterPropertiesSet() {
        val airlines = Flux.just(
            AirlineEntity(
                "1",
                "Qatar Airways"
            ),
            AirlineEntity(
                "2",
                "Etihad Airways"
            ),
            AirlineEntity(
                "3",
                "Korean Airways"
            ),
            AirlineEntity(
                "4",
                "Emirates"
            ),
            AirlineEntity(
                "5",
                "Japan Air Lines"
            )
        )

        val airplanes = Flux.just(
            AirplaneEntity(
                "1",
                "1",
                "Airbus A320 Family",
                "Q-01 Airbus A320-200",
                "US",
                LocalDate.of(1987, 2, 22)
            ),
            AirplaneEntity(
                "2",
                "2",
                "Boeing 787 Dreamliner",
                "E-01 Boeing 787-9",
                "US",
                LocalDate.of(2014, 12, 31)
            ),
            AirplaneEntity(
                "3",
                "3",
                "Boeing 737 Next Generation",
                "K-1 Boeing 737-800",
                "US",
                LocalDate.of(2019, 1, 31)
            ),
            AirplaneEntity(
                "4",
                "4",
                "Boeing 777",
                "E-1 Boeing 777-300ER",
                "US",
                LocalDate.of(2003, 7, 18)
            ),
            AirplaneEntity(
                "5",
                "5",
                "Mitsubishi SpaceJet",
                "J-1 Mitsubishi MRJ-90",
                "JP",
                LocalDate.of(2019, 6, 13)
            )
        )

        Flux.zip(
            airlines.flatMap(airlineRepository::save),
            airplanes.flatMap(airplaneRepository::save)
        ).subscribe {
            logger.debug("Insert some airline data...")
        }
    }
}