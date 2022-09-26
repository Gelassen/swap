package ru.home.swap.stub

import java.util.*

object Stubs {

    /*private*/ fun getDate(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = 0L
        cal[year, month] = day
        return cal.timeInMillis
    }
}