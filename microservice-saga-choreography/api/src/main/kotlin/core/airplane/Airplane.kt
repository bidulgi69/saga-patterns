package core.airplane

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class Airplane(
    val airplaneId: String,
    var airlineId: String? = null,
    var name: String,
    var code: String,
    val madeIn: String,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    val madeAt: LocalDate,
)
