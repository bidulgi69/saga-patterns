package core.values

data class Location(
    val lat: Double,
    val long: Double,
) {
    override fun toString(): String = "$lat, $long"

    companion object {
        fun of(lat: Double, long: Double): Location = Location(lat, long)
    }
}
