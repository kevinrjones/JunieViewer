package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.domain.Sender
import com.knowledgespike.junieviewer.domain.Turn

/**
 * Computes the LazyColumn item index for the message at [messageIndex] in the flat
 * filteredMessages list, accounting for Junie Turn headers inserted before each Junie turn.
 *
 * This is a UI/presentation concern because it depends on how the LazyColumn
 * lays out Turn headers and message items.
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
