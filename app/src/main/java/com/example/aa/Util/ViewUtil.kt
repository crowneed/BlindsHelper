package com.example.aa.Util

import android.widget.TextView
import java.util.*

fun TextView.setTextBlinds(currentBlinds : Deque<String>) {
    this.text =
        if (currentBlinds.isEmpty())
            MESSAGE_BLINDS_OUT_OF_LIMIT
        else
            currentBlinds.peekFirst()
}