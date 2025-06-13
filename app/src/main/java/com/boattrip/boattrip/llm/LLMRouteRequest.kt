package com.boattrip.boattrip.llm;

data class LLMRouteRequest(
    var model: String,
    var tools: List<Tool>,
    var input: List<Message>,
    var stream: Boolean,
)
