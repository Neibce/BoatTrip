package com.boattrip.boattrip

data class Route(
    var itinerary: List<Itinerary>
) {
    constructor() : this(
        itinerary = emptyList()
    )
}