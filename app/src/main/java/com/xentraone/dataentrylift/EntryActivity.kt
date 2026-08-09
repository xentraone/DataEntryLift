package com.xentraone.dataentrylift

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.time.LocalDate

class EntryActivity : AppCompatActivity() {

    private lateinit var db: EntryDb
    private var editId: Long = 0
    private var date: LocalDate = LocalDate.now()

    private lateinit var btnDate: MaterialButton
    private lateinit var inputJob: AutoCompleteTextView
    private lateinit var inputUnit: AutoCompleteTextView
    private lateinit var inputDx: AutoCompleteTextView
    private lateinit var inputNor: AutoCompleteTextView
    private lateinit var inputOt1: EditText
    private lateinit var inputOt2: EditText
    private lateinit var inputOt3: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { finish() }
        db = EntryDb(this)
        editId = intent.getLongExtra("id", 0)

        btnDate = findViewById(R.id.btnDate)
        inputJob = findViewById(R.id.inputJob)
        inputUnit = findViewById(R.id.inputUnit)
        inputDx = findViewById(R.id.inputDx)
        inputNor = findViewById(R.id.inputNor)
        inputOt1 = findViewById(R.id.inputOt1)
        inputOt2 = findViewById(R.id.inputOt2)
        inputOt3 = findViewById(R.id.inputOt3)

        setupSuggestions(inputJob, (Presets.jobs + db.distinctValues("job")).distinct())
        setupSuggestions(inputUnit, (Presets.units + db.distinctValues("unit")).distinct())
        setupSuggestions(inputDx, (Presets.dxLoaders + db.distinctValues("dx")).distinct())
        setupSuggestions(inputNor, (Presets.nor + db.distinctValues("nor")).distinct())

        val btnDelete = findViewById<MaterialButton>(R.id.btnDelete)
        if (editId > 0) {
            val e = db.get(editId)
            if (e != null) {
                date = LocalDate.parse(e.date)
                inputJob.setText(e.job, false)
                inputUnit.setText(e.unit, false)
                inputDx.setText(e.dx, false)
                inputNor.setText(e.nor, false)
                if (e.ot1 != 0.0) inputOt1.setText(Fmt.num(e.ot1))
                if (e.ot2 != 0.0) inputOt2.setText(Fmt.num(e.ot2))
                if (e.ot3 != 0.0) inputOt3.setText(Fmt.num(e.ot3))
            }
            btnDelete.setOnClickListener { confirmDelete() }
        } else {
            btnDelete.visibility = View.GONE
        }
        updateDateLabel()

        btnDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    date = LocalDate.of(y, m + 1, d)
                    updateDateLabel()
                },
                date.year, date.monthValue - 1, date.dayOfMonth
            ).show()
        }

        findViewById<MaterialButton>(R.id.btnHoliday).setOnClickListener {
            inputJob.setText("Sunday", false)
            inputUnit.setText("", false)
            inputDx.setText("HOLIDAY", false)
            inputNor.setText("", false)
            inputOt1.setText("")
            inputOt2.setText("")
            inputOt3.setText("")
        }

        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener { save() }
    }

    private fun setupSuggestions(view: AutoCompleteTextView, values: List<String>) {
        view.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, values))
        // Show the full list as soon as the field is tapped, so values can be
        // picked without typing.
        view.setOnClickListener { view.showDropDown() }
        view.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && view.text.isEmpty()) view.showDropDown()
        }
    }

    private fun updateDateLabel() {
        btnDate.text = getString(R.string.date_label, Periods.display(date))
    }

    private fun save() {
        val job = inputJob.text.toString().trim()
        if (job.isEmpty()) {
            Toast.makeText(this, R.string.job_required, Toast.LENGTH_LONG).show()
            return
        }
        val entry = Entry(
            id = editId,
            date = date.toString(),
            job = job,
            unit = inputUnit.text.toString().trim(),
            dx = inputDx.text.toString().trim(),
            nor = inputNor.text.toString().trim(),
            ot1 = inputOt1.text.toString().trim().toDoubleOrNull() ?: 0.0,
            ot2 = inputOt2.text.toString().trim().toDoubleOrNull() ?: 0.0,
            ot3 = inputOt3.text.toString().trim().toDoubleOrNull() ?: 0.0
        )
        if (editId > 0) db.update(entry) else db.insert(entry)
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_entry)
            .setMessage(R.string.delete_entry_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                db.delete(editId)
                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
