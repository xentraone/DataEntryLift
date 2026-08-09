package com.xentraone.dataentrylift

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.time.LocalDate

class ReportActivity : AppCompatActivity() {

    private lateinit var db: EntryDb
    private var periods: List<Pair<LocalDate, LocalDate>> = emptyList()
    private var current: Pair<LocalDate, LocalDate>? = null
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val root = findViewById<View>(R.id.rootReport)
        val toolbarPadTop = toolbar.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            toolbar.updatePadding(top = toolbarPadTop + bars.top)
            root.updatePadding(bottom = bars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = true

        db = EntryDb(this)

        periods = Periods.recent(8)
        val labels = periods.map { Periods.label(it.first, it.second) }

        val spinner = findViewById<Spinner>(R.id.periodSpinner)
        spinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, labels
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                generate(periods[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<MaterialButton>(R.id.btnShare).setOnClickListener { saveAndShare() }
    }

    private fun generate(period: Pair<LocalDate, LocalDate>) {
        current = period
        val entries = db.between(period.first.toString(), period.second.toString())
        val bmp = ReportRenderer.render(entries, period.first, period.second)
        bitmap = bmp
        findViewById<ImageView>(R.id.preview).setImageBitmap(bmp)
    }

    private fun saveAndShare() {
        val bmp = bitmap ?: return
        val period = current ?: return
        val name = "LiftReport_" + Periods.display(period.first).replace('-', '.') +
            "_to_" + Periods.display(period.second).replace('-', '.') + ".png"
        try {
            val uri = ReportSaver.save(this, bmp, name)
            Toast.makeText(this, R.string.report_saved, Toast.LENGTH_LONG).show()
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(share, getString(R.string.share_report)))
        } catch (t: Throwable) {
            Toast.makeText(this, getString(R.string.save_failed, t.message), Toast.LENGTH_LONG).show()
        }
    }
}
