package com.xentraone.dataentrylift

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.time.LocalDate

/** Draws the weekly report as a PNG that looks like the original Excel sheet:
 *  yellow highlighted columns, red bold values, one merged date cell per day
 *  and a TOTAL HOURS row at the bottom. */
object ReportRenderer {

    private const val YELLOW = 0xFFFFFF00.toInt()
    private const val RED = 0xFFCC0000.toInt()
    private const val GRID = 0xFF555555.toInt()
    private const val NAVY = 0xFF0F2A43.toInt()
    private const val GOLD = 0xFFFFB300.toInt()

    private val colTitles = listOf("DATE", "JOB", "UNIT", "DX LOADER", "NOR", "OT1", "OT2", "OT3")
    private val colWidths = listOf(190f, 130f, 95f, 340f, 145f, 80f, 80f, 80f)

    /** Like the Excel sheet: every date block shows 5 rows, blanks included. */
    private const val ROWS_PER_DAY = 5

    private class Line(val date: LocalDate, val entry: Entry?)

    fun render(entries: List<Entry>, from: LocalDate, to: LocalDate): Bitmap {
        val margin = 24f
        val rowH = 64f
        val headerH = 72f
        val bandH = 140f
        val bandGap = 24f
        val totalH = 80f

        // Every date block gets ROWS_PER_DAY rows (padded with blanks), just
        // like the Excel sheet; days with more entries grow as needed.
        val byDate = entries.groupBy { LocalDate.parse(it.date) }
        val lines = mutableListOf<Line>()
        var d = from
        while (!d.isAfter(to)) {
            val dayEntries = byDate[d].orEmpty()
            for (e in dayEntries) lines.add(Line(d, e))
            repeat(maxOf(0, ROWS_PER_DAY - dayEntries.size)) { lines.add(Line(d, null)) }
            d = d.plusDays(1)
        }

        val tableW = colWidths.sum()
        val width = (tableW + margin * 2).toInt()
        val height = (bandH + bandGap + headerH + rowH * lines.size + totalH + margin).toInt()

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = GRID
            strokeWidth = 2f
        }
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 28f
            color = Color.BLACK
        }
        val boldText = Paint(text).apply { typeface = Typeface.DEFAULT_BOLD }
        val redText = Paint(boldText).apply { color = RED }
        // Column left edges.
        val colX = FloatArray(colWidths.size + 1)
        colX[0] = margin
        for (i in colWidths.indices) colX[i + 1] = colX[i] + colWidths[i]

        // Navy title band across the full width.
        fill.color = NAVY
        canvas.drawRect(0f, 0f, width.toFloat(), bandH, fill)
        val titleText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 44f
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("LIFT WORK REPORT", margin + 8f, 62f, titleText)
        val subtitleText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 30f
            color = GOLD
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(Periods.label(from, to), margin + 8f, 110f, subtitleText)

        val tableTop = bandH + bandGap

        // Header row.
        for (i in colTitles.indices) {
            fill.color = YELLOW
            canvas.drawRect(colX[i], tableTop, colX[i + 1], tableTop + headerH, fill)
            canvas.drawRect(colX[i], tableTop, colX[i + 1], tableTop + headerH, stroke)
            drawCentered(canvas, colTitles[i], colX[i], colX[i + 1], tableTop + headerH / 2f, boldText)
        }

        val rowsTop = tableTop + headerH

        // Data cells (all columns except the merged DATE column).
        for ((idx, line) in lines.withIndex()) {
            val top = rowsTop + idx * rowH
            val bottom = top + rowH
            val e = line.entry
            val values = listOf(
                e?.job ?: "",
                e?.unit ?: "",
                e?.dx ?: "",
                e?.nor ?: "",
                otText(e?.ot1),
                otText(e?.ot2),
                otText(e?.ot3)
            )
            for (i in values.indices) {
                val col = i + 1 // skip DATE column
                fill.color = if (col == 3 || col == 4) YELLOW else Color.WHITE
                canvas.drawRect(colX[col], top, colX[col + 1], bottom, fill)
                canvas.drawRect(colX[col], top, colX[col + 1], bottom, stroke)
                val paint = when (col) {
                    1, 2 -> text          // JOB, UNIT in black
                    else -> redText       // DX, NOR, OT in red bold like the sheet
                }
                drawCentered(canvas, values[i], colX[col], colX[col + 1], (top + bottom) / 2f, paint)
            }
        }

        // Merged DATE cells, one per day.
        var start = 0
        while (start < lines.size) {
            var end = start
            while (end + 1 < lines.size && lines[end + 1].date == lines[start].date) end++
            val top = rowsTop + start * rowH
            val bottom = rowsTop + (end + 1) * rowH
            fill.color = YELLOW
            canvas.drawRect(colX[0], top, colX[1], bottom, fill)
            canvas.drawRect(colX[0], top, colX[1], bottom, stroke)
            drawCentered(
                canvas, Periods.display(lines[start].date),
                colX[0], colX[1], (top + bottom) / 2f, boldText
            )
            start = end + 1
        }

        // TOTAL row.
        val totalTop = rowsTop + lines.size * rowH
        val totalBottom = totalTop + totalH
        fill.color = YELLOW
        canvas.drawRect(colX[0], totalTop, colX[colWidths.size], totalBottom, fill)
        canvas.drawRect(colX[0], totalTop, colX[4], totalBottom, stroke)
        drawCentered(canvas, "TOTAL HOURS", colX[0], colX[4], (totalTop + totalBottom) / 2f, boldText)

        val norTotal = entries.sumOf { it.nor.trim().toDoubleOrNull() ?: 0.0 }
        val totals = listOf(
            norTotal,
            entries.sumOf { it.ot1 },
            entries.sumOf { it.ot2 },
            entries.sumOf { it.ot3 }
        )
        for (i in totals.indices) {
            val col = 4 + i
            canvas.drawRect(colX[col], totalTop, colX[col + 1], totalBottom, stroke)
            drawCentered(
                canvas, Fmt.num(totals[i]),
                colX[col], colX[col + 1], (totalTop + totalBottom) / 2f, redText
            )
        }

        return bmp
    }

    private fun otText(v: Double?): String =
        if (v == null || v == 0.0) "" else Fmt.num(v)

    private fun drawCentered(
        canvas: Canvas, value: String, left: Float, right: Float, centerY: Float, paint: Paint
    ) {
        if (value.isEmpty()) return
        val p = Paint(paint).apply { textAlign = Paint.Align.CENTER }
        // Shrink long values so they never overflow the cell.
        val maxWidth = right - left - 16f
        while (p.textSize > 14f && p.measureText(value) > maxWidth) {
            p.textSize = p.textSize - 2f
        }
        val fm = p.fontMetrics
        val baseline = centerY - (fm.ascent + fm.descent) / 2f
        canvas.drawText(value, (left + right) / 2f, baseline, p)
    }
}
