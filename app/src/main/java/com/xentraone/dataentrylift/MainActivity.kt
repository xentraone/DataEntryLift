package com.xentraone.dataentrylift

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var db: EntryDb
    private lateinit var adapter: EntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = EntryDb(this)

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
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val (from, to) = Periods.periodOf(LocalDate.now())
        findViewById<TextView>(R.id.periodLabel).text =
            getString(R.string.period_label, Periods.label(from, to))
        val entries = db.between(from.toString(), to.toString())
        adapter.setEntries(entries)
        findViewById<TextView>(R.id.emptyLabel).visibility =
            if (entries.isEmpty()) View.VISIBLE else View.GONE
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}
