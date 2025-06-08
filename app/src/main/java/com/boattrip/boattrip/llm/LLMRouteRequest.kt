package com.boattrip.boattrip.llm;

data class LLMRouteRequest(
    var model: String,
    var messages: List<Message>,
    var stream: Boolean,
)
