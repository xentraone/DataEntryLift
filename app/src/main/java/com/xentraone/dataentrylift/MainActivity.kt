package com.xentraone.dataentrylift

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var db: EntryDb
    private lateinit var adapter: EntryAdapter

    /** The period currently shown in the list; ‹ › arrows move it so past
     *  weeks can be opened and their entries edited. */
    private var period: Pair<LocalDate, LocalDate> = Periods.periodOf(LocalDate.now())

    private val backupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) writeBackup(uri) }

    private val restoreLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) readBackup(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = EntryDb(this)

        // Keep the header and bottom buttons out of the status/navigation bar
        // areas on edge-to-edge devices (Android 15+).
        val header = findViewById<View>(R.id.headerRoot)
        val bottomBar = findViewById<View>(R.id.bottomBar)
        val headerPadTop = header.paddingTop
        val bottomBarPadBottom = bottomBar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootMain)) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            header.updatePadding(top = headerPadTop + bars.top)
            bottomBar.updatePadding(bottom = bottomBarPadBottom + bars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = true

        val list = findViewById<RecyclerView>(R.id.entryList)
        list.layoutManager = LinearLayoutManager(this)
        adapter = EntryAdapter(
            onClick = { e ->
                startActivity(Intent(this, EntryActivity::class.java).putExtra("id", e.id))
            },
            onLongClick = { e -> confirmDelete(e) }
        )
        list.adapter = adapter

        findViewById<MaterialButton>(R.id.btnNew).setOnClickListener {
            startActivity(Intent(this, EntryActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnReport).setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { v -> showMenu(v) }

        findViewById<ImageButton>(R.id.btnPrevPeriod).setOnClickListener {
            period = Periods.periodOf(period.first.minusDays(1))
            refresh()
        }
        findViewById<ImageButton>(R.id.btnNextPeriod).setOnClickListener {
            if (period.first.isBefore(Periods.periodOf(LocalDate.now()).first)) {
                period = Periods.periodOf(period.second.plusDays(1))
                refresh()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        findViewById<TextView>(R.id.periodLabel).text =
            Periods.label(period.first, period.second)
        val entries = db.between(period.first.toString(), period.second.toString())
        adapter.setEntries(entries)
        findViewById<TextView>(R.id.emptyLabel).visibility =
            if (entries.isEmpty()) View.VISIBLE else View.GONE

        val nor = entries.sumOf { it.nor.trim().toDoubleOrNull() ?: 0.0 }
        val ot = entries.sumOf { it.ot1 + it.ot2 + it.ot3 }
        findViewById<TextView>(R.id.statNor).text = Fmt.num(nor)
        findViewById<TextView>(R.id.statOt).text = Fmt.num(ot)
        findViewById<TextView>(R.id.statEntries).text = entries.size.toString()

        val atCurrent = !period.first.isBefore(Periods.periodOf(LocalDate.now()).first)
        val next = findViewById<ImageButton>(R.id.btnNextPeriod)
        next.isEnabled = !atCurrent
        next.alpha = if (atCurrent) 0.3f else 1f
    }

    private fun showMenu(anchor: View) {
        val menu = PopupMenu(this, anchor)
        menu.menuInflater.inflate(R.menu.main_menu, menu.menu)
        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_backup -> {
                    backupLauncher.launch("LiftDiary_backup_" + LocalDate.now() + ".json")
                    true
                }
                R.id.action_restore -> {
                    restoreLauncher.launch(arrayOf("*/*"))
                    true
                }
                R.id.action_sample -> {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.load_sample)
                        .setMessage(R.string.load_sample_message)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            SampleData.load(db)
                            refresh()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                R.id.action_clear -> {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.clear_all)
                        .setMessage(R.string.clear_all_message)
                        .setPositiveButton(R.string.delete) { _, _ ->
                            db.deleteAll()
                            refresh()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                    true
                }
                else -> false
            }
        }
        menu.show()
    }

    private fun writeBackup(uri: Uri) {
        try {
            val arr = JSONArray()
            for (e in db.all()) {
                val o = JSONObject()
                o.put("date", e.date)
                o.put("job", e.job)
                o.put("unit", e.unit)
                o.put("dx", e.dx)
                o.put("nor", e.nor)
                o.put("ot1", e.ot1)
                o.put("ot2", e.ot2)
                o.put("ot3", e.ot3)
                arr.put(o)
            }
            contentResolver.openOutputStream(uri)!!.use { out ->
                out.write(arr.toString(2).toByteArray(Charsets.UTF_8))
            }
            Toast.makeText(this, getString(R.string.backup_done, arr.length()), Toast.LENGTH_LONG).show()
        } catch (t: Throwable) {
            Toast.makeText(this, getString(R.string.backup_failed, t.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun readBackup(uri: Uri) {
        try {
            val text = contentResolver.openInputStream(uri)!!
                .bufferedReader(Charsets.UTF_8).readText()
            val arr = JSONArray(text)
            val existing = db.all()
                .map { listOf(it.date, it.job, it.unit, it.dx, it.nor, it.ot1, it.ot2, it.ot3) }
                .toHashSet()
            var added = 0
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val e = Entry(
                    date = o.getString("date"),
                    job = o.getString("job"),
                    unit = o.optString("unit"),
                    dx = o.optString("dx"),
                    nor = o.optString("nor"),
                    ot1 = o.optDouble("ot1", 0.0),
                    ot2 = o.optDouble("ot2", 0.0),
                    ot3 = o.optDouble("ot3", 0.0)
                )
                val key = listOf(e.date, e.job, e.unit, e.dx, e.nor, e.ot1, e.ot2, e.ot3)
                if (key !in existing) {
                    db.insert(e)
                    existing.add(key)
                    added++
                }
            }
            refresh()
            Toast.makeText(this, getString(R.string.restore_done, added), Toast.LENGTH_LONG).show()
        } catch (t: Throwable) {
            Toast.makeText(this, getString(R.string.restore_failed, t.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun confirmDelete(e: Entry) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_entry)
            .setMessage(getString(R.string.delete_entry_message, e.job, Periods.display(e.date)))
            .setPositiveButton(R.string.delete) { _, _ ->
                db.delete(e.id)
                refresh()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
