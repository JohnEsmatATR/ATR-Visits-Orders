package com.akhnaton.foodvisits.ui.home.visits2

import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {

    var checkInTimeMillis: Long = 0L
    var serverTimeOffsetMillis: Long = 0L

    var timerStarted: Boolean = false
}