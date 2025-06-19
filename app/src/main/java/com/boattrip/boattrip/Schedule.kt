package com.boattrip.boattrip

data class Schedule(
    var time: String,
    var activity: String,
    var location: String,
    var coordinates: Coordinate
) {
    constructor() : this(
        time = "",
        activity = "",
        location = "",
        coordinates = Coordinate(0.0, 0.0)
    )
}
