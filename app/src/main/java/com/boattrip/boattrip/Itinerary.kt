package com.boattrip.boattrip

data class Itinerary(
    var day: Int,
    var date: String,
    var schedule: List<Schedule>
)