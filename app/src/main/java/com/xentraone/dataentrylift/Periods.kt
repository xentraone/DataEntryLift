package com.xentraone.dataentrylift

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Report periods inside a month: 1-8, 9-16, 17-24, 25-end.
 *  A report is finalized and sent at the end of each period (8th, 16th, ...). */
object Periods {

    private val DISPLAY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    fun periodOf(date: LocalDate): Pair<LocalDate, LocalDate> {
        val d = date.dayOfMonth
        return when {
            d <= 8 -> date.withDayOfMonth(1) to date.withDayOfMonth(8)
            d <= 16 -> date.withDayOfMonth(9) to date.withDayOfMonth(16)
            d <= 24 -> date.withDayOfMonth(17) to date.withDayOfMonth(24)
            else -> date.withDayOfMonth(25) to date.withDayOfMonth(date.lengthOfMonth())
        }
    }

    /** Current period first, then the previous ones. */
    fun recent(n: Int): List<Pair<LocalDate, LocalDate>> {
        val out = mutableListOf<Pair<LocalDate, LocalDate>>()
        var p = periodOf(LocalDate.now())
        repeat(n) {
            out.add(p)
            p = periodOf(p.first.minusDays(1))
        }
        return out
    }

    fun label(from: LocalDate, to: LocalDate): String =
        display(from) + " to " + display(to)

    fun display(date: LocalDate): String = date.format(DISPLAY)

    fun display(isoDate: String): String = display(LocalDate.parse(isoDate))

    /** e.g. "Monday · 03-08-2026" for the list section headers. */
    fun headerLabel(isoDate: String): String {
        val d = LocalDate.parse(isoDate)
        val day = d.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH
        )
        return day + " · " + display(d)
    }
}
