package com.boattrip.boattrip

data class Coordinate(
    var lat: Double,
    var lng: Double
) {
    constructor() : this(
        lat = 0.0,
        lng = 0.0
    )
}