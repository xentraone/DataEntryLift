package com.xentraone.dataentrylift

/** Preset dropdown values taken from the existing Excel sheet, so entries can
 *  be picked from a list instead of typed. New values typed by the user are
 *  learned automatically (they come back as suggestions from the database). */
object Presets {

    val jobs = listOf(
        "KONE", "Sunday",
        "D4046", "D4662", "D4368", "D4882", "D0541", "D4890", "D5084", "D3381"
    )

    val units = listOf("P01", "P02", "P03", "P04", "P05", "P06")

    val dxLoaders = listOf(
        "LIFT KONE", "HOLIDAY", "other brand", "survey work",
        "DX1S01DX28",
        "DX1S32317953", "DX1S32317957", "DX1S32317962", "DX1S32317967",
        "DX1S32322734", "DX1S32322737", "DX1S32324245", "DX1S32324248",
        "DX1S32324251", "DX1S32324254", "DX1S32324257", "DX1S32324259",
        "DX1S32324262", "DX1S32324265", "DX1S20137094", "DX1S20137095",
        "DX1S20137097", "DX1S32331718", "DX1S32331720", "DX1S32331722",
        "DX1S50216167", "DX1S32322119", "DX1S32322201", "DX1S32322203"
    )

    val nor = listOf("2", "3", "8", "call back")
}
