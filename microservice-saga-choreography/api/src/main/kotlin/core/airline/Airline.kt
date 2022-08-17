package core.airline

import core.airplane.Airplane

data class Airline(
    val airlineId: String,
    var name: String,
    var airplanes: List<Airplane>,
)
