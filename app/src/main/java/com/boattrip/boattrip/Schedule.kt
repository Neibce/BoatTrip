package com.boattrip.boattrip

data class Schedule(
    var time: String,
    var activity: String,
    var location: String,
    var coordinates: Coordinate
)