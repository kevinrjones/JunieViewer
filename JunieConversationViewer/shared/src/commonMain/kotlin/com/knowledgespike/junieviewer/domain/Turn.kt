package com.knowledgespike.junieviewer.domain

/**
 * Represents a contiguous span of Messages from the same Sender.
 * Used to group Junie messages into visual "turns" in the conversation UI.
 */
data class Turn(
    val sender: Sender,
    val messages: List<Message>
)

/**
 * Groups a flat list of Messages into Turns — contiguous runs of the same Sender.
 * Preserves chronological order.
 */
fun groupMessagesIntoTurns(messages: List<Message>): List<Turn> {
    if (messages.isEmpty()) return emptyList()

    val turns = mutableListOf<Turn>()
    var currentSender = messages.first().sender
    var currentMessages = mutableListOf(messages.first())

    for (i in 1 until messages.size) {
        val message = messages[i]
        if (message.sender == currentSender) {
            currentMessages.add(message)
        } else {
            turns.add(Turn(sender = currentSender, messages = currentMessages))
            currentSender = message.sender
            currentMessages = mutableListOf(message)
        }
    }
    turns.add(Turn(sender = currentSender, messages = currentMessages))
    return turns
}

/**
 * Computes the LazyColumn item index for the message at [messageIndex] in the flat
 * filteredMessages list, accounting for Junie Turn headers inserted before each Junie turn.
 */
fun lazyColumnIndexForMessage(turns: List<Turn>, messageIndex: Int): Int {
    var flatIdx = 0
    var lazyIdx = 0
    for (turn in turns) {
        if (turn.sender == Sender.Junie) {
            if (flatIdx + turn.messages.size > messageIndex) {
                return lazyIdx + 1 + (messageIndex - flatIdx)
            }
            lazyIdx += 1 + turn.messages.size
        } else {
            if (flatIdx + turn.messages.size > messageIndex) {
                return lazyIdx + (messageIndex - flatIdx)
            }
            lazyIdx += turn.messages.size
        }
        flatIdx += turn.messages.size
    }
    return lazyIdx
}
