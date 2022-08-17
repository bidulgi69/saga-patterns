package kr.dove.service.airline.persistence

import core.state.State
import core.values.Airport
import core.values.SeatClass
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface ReservationRepository : ReactiveMongoRepository<ReservationEntity, String> {
    fun existsByAirplaneIdAndDepartureAndArrivalAndDepartureTimeAndSeatClassAndSeatNumberAndStateNot(
        airplaneId: String,
        departure: Airport,
        arrival: Airport,
        departureTime: LocalDateTime,
        seatClass: SeatClass,
        seatNumber: Int,
        state: State
    ): Mono<Boolean>
}