package com.xentraone.dataentrylift

/** Geometry of the report PNG, shared by the renderer (drawing) and the
 *  image importer (mapping recognized text back to table cells). */
object ReportLayout {
    const val MARGIN = 24f
    const val ROW_H = 64f
    const val HEADER_H = 72f
    const val BAND_H = 140f
    const val BAND_GAP = 24f
    const val TOTAL_H = 80f

    val COL_WIDTHS = listOf(190f, 130f, 95f, 340f, 145f, 80f, 80f, 80f)

    /** Total bitmap width in layout units. */
    val WIDTH = COL_WIDTHS.sum() + MARGIN * 2

    /** Y where the first data row starts. */
    const val ROWS_TOP = BAND_H + BAND_GAP + HEADER_H
}
