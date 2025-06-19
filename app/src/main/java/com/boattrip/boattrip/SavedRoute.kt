package com.boattrip.boattrip

data class SavedRoute(
    var id: String = "",
    var destination: String = "",
    var period: String = "",
    var route: Route? = null,
    var savedAt: Long = System.currentTimeMillis()
) 