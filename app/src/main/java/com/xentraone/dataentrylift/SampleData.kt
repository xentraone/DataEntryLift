package com.xentraone.dataentrylift

/** The exact week from the original Excel sheet (01-08-2026 to 08-08-2026),
 *  loadable as test data to try the app and the PNG report. */
object SampleData {

    fun load(db: EntryDb) {
        val rows = listOf(
            Entry(date = "2026-08-01", job = "KONE", unit = "", dx = "LIFT KONE", nor = ""),
            Entry(date = "2026-08-01", job = "KONE", unit = "P01", dx = "DX1S01DX28", nor = ""),
            Entry(date = "2026-08-01", job = "KONE", unit = "P02", dx = "other brand", nor = "8", ot1 = 2.0),
            Entry(date = "2026-08-01", job = "KONE", unit = "P03", dx = "survey work", nor = ""),

            Entry(date = "2026-08-02", job = "Sunday", unit = "", dx = "HOLIDAY", nor = ""),

            Entry(date = "2026-08-03", job = "D4046", unit = "P01", dx = "DX1S32317953", nor = "2"),
            Entry(date = "2026-08-03", job = "D4046", unit = "P02", dx = "DX1S32317957", nor = "2"),
            Entry(date = "2026-08-03", job = "D4046", unit = "P03", dx = "DX1S32317962", nor = "2"),
            Entry(date = "2026-08-03", job = "D4046", unit = "P04", dx = "DX1S32317967", nor = "2"),

            Entry(date = "2026-08-04", job = "D4662", unit = "P01", dx = "DX1S32322734", nor = "2"),
            Entry(date = "2026-08-04", job = "D4662", unit = "P02", dx = "DX1S32322737", nor = "2"),
            Entry(date = "2026-08-04", job = "D4368", unit = "P01", dx = "DX1S32324245", nor = "2"),
            Entry(date = "2026-08-04", job = "D4368", unit = "P02", dx = "DX1S32324248", nor = "2"),

            Entry(date = "2026-08-05", job = "D4662", unit = "P01", dx = "DX1S32324251", nor = "2"),
            Entry(date = "2026-08-05", job = "D4662", unit = "P02", dx = "DX1S32324254", nor = "2"),
            Entry(date = "2026-08-05", job = "D4662", unit = "P03", dx = "DX1S32324257", nor = "2"),
            Entry(date = "2026-08-05", job = "D4882", unit = "P01", dx = "DX1S32324259", nor = "2"),

            Entry(date = "2026-08-06", job = "D4882", unit = "P02", dx = "DX1S32324262", nor = "2"),
            Entry(date = "2026-08-06", job = "D4882", unit = "P03", dx = "DX1S32324265", nor = "2"),
            Entry(date = "2026-08-06", job = "D0541", unit = "P01", dx = "DX1S20137094", nor = "2"),
            Entry(date = "2026-08-06", job = "D0541", unit = "P02", dx = "DX1S20137095", nor = "2"),

            Entry(date = "2026-08-07", job = "D0541", unit = "P03", dx = "DX1S20137097", nor = "2"),
            Entry(date = "2026-08-07", job = "D4890", unit = "P01", dx = "DX1S32331718", nor = "2"),
            Entry(date = "2026-08-07", job = "D4890", unit = "P02", dx = "DX1S32331720", nor = "2"),
            Entry(date = "2026-08-07", job = "D4890", unit = "P03", dx = "DX1S32331722", nor = "2"),
            Entry(date = "2026-08-07", job = "D5084", unit = "P02", dx = "DX1S50216167", nor = "call back", ot1 = 2.0),

            Entry(date = "2026-08-08", job = "D3381", unit = "P01", dx = "DX1S32322119", nor = "3"),
            Entry(date = "2026-08-08", job = "D3381", unit = "P02", dx = "DX1S32322201", nor = "3"),
            Entry(date = "2026-08-08", job = "D3381", unit = "P03", dx = "DX1S32322203", nor = "2")
        )
        for (e in rows) db.insert(e)
    }
}
