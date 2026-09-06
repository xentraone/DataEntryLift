package com.xentraone.dataentrylift

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

/** Reads a report PNG made by this app (also a copy received via WhatsApp)
 *  and converts it back into entries. Text is recognized on-device with
 *  ML Kit, then every word is mapped to its table cell using the known
 *  report geometry. */
object ReportImageImporter {

    private val DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    fun import(
        context: Context,
        uri: Uri,
        onResult: (List<Entry>) -> Unit,
        onError: (String) -> Unit
    ) {
        val image = try {
            InputImage.fromFilePath(context, uri)
        } catch (t: Throwable) {
            onError(t.message ?: "Cannot open image")
            return
        }
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(image)
            .addOnSuccessListener { text ->
                try {
                    val entries = parse(text, image.width.toFloat(), image.height.toFloat())
                    if (entries.isEmpty()) {
                        onError("No entries found — use a report PNG made by this app")
                    } else {
                        onResult(entries)
                    }
                } catch (t: Throwable) {
                    onError(t.message ?: "Could not read the report")
                }
            }
            .addOnFailureListener { onError(it.message ?: "Text recognition failed") }
    }

    private fun parse(text: Text, imageW: Float, imageH: Float): List<Entry> {
        val scale = imageW / ReportLayout.WIDTH
        if (scale <= 0f) return emptyList()
        val rowsTop = ReportLayout.ROWS_TOP * scale
        val rowH = ReportLayout.ROW_H * scale
        val rowCount = (((imageH / scale) -
            (ReportLayout.ROWS_TOP + ReportLayout.TOTAL_H + ReportLayout.MARGIN)) /
            ReportLayout.ROW_H).roundToInt()
        if (rowCount <= 0) return emptyList()

        // Column right edges in image pixels.
        val colRight = FloatArray(ReportLayout.COL_WIDTHS.size)
        var edge = ReportLayout.MARGIN
        for (i in ReportLayout.COL_WIDTHS.indices) {
            edge += ReportLayout.COL_WIDTHS[i]
            colRight[i] = edge * scale
        }

        class Word(val x: Float, val s: String)

        val cells = Array(rowCount) {
            Array(ReportLayout.COL_WIDTHS.size) { mutableListOf<Word>() }
        }
        // Row position of each recognized date in the merged DATE column.
        val dateCenters = mutableListOf<Pair<Float, LocalDate>>()

        for (block in text.textBlocks) {
            for (line in block.lines) {
                for (el in line.elements) {
                    val box = el.boundingBox ?: continue
                    val cx = box.exactCenterX()
                    val cy = box.exactCenterY()
                    val rowPos = (cy - rowsTop) / rowH
                    val row = rowPos.toInt()
                    if (rowPos < 0f || row >= rowCount) continue
                    val col = colRight.indexOfFirst { cx < it }
                    if (col == -1) continue
                    if (col == 0) {
                        val d = try {
                            LocalDate.parse(el.text.trim(), DATE_FMT)
                        } catch (t: Throwable) {
                            null
                        }
                        if (d != null) dateCenters.add(rowPos to d)
                    } else {
                        cells[row][col].add(Word(cx, el.text.trim()))
                    }
                }
            }
        }
        if (dateCenters.isEmpty()) return emptyList()

        fun cellText(row: Int, col: Int): String =
            cells[row][col].sortedBy { it.x }.joinToString(" ") { it.s }.trim()

        val out = mutableListOf<Entry>()
        for (r in 0 until rowCount) {
            val job = cellText(r, 1)
            val unit = cellText(r, 2)
            val dx = cellText(r, 3)
            val nor = cellText(r, 4)
            val ot1 = cellText(r, 5).toDoubleOrNull() ?: 0.0
            val ot2 = cellText(r, 6).toDoubleOrNull() ?: 0.0
            val ot3 = cellText(r, 7).toDoubleOrNull() ?: 0.0
            val empty = job.isBlank() && unit.isBlank() && dx.isBlank() &&
                nor.isBlank() && ot1 == 0.0 && ot2 == 0.0 && ot3 == 0.0
            if (empty) continue
            // The date cell is merged per day, so each row takes the nearest
            // recognized date (dates sit vertically centered in their block).
            val rowCenter = r + 0.5f
            val date = dateCenters.minByOrNull { abs(it.first - rowCenter) }!!.second
            out.add(
                Entry(
                    date = date.toString(), job = job, unit = unit, dx = dx,
                    nor = nor, ot1 = ot1, ot2 = ot2, ot3 = ot3
                )
            )
        }
        return out
    }
}
