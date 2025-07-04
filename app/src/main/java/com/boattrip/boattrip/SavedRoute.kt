package com.boattrip.boattrip

data class SavedRoute(
    var id: String = "",
    var destination: String = "",
    var theme: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var route: Route? = null,
    var savedAt: Long = System.currentTimeMillis()
) 