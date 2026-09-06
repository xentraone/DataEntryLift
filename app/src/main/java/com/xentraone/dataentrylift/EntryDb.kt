package com.xentraone.dataentrylift

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EntryDb(context: Context) : SQLiteOpenHelper(context, "entries.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE entries(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT NOT NULL," +
                "job TEXT NOT NULL," +
                "unit TEXT NOT NULL DEFAULT ''," +
                "dx TEXT NOT NULL DEFAULT ''," +
                "nor TEXT NOT NULL DEFAULT ''," +
                "ot1 REAL NOT NULL DEFAULT 0," +
                "ot2 REAL NOT NULL DEFAULT 0," +
                "ot3 REAL NOT NULL DEFAULT 0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    private fun toValues(e: Entry): ContentValues = ContentValues().apply {
        put("date", e.date)
        put("job", e.job)
        put("unit", e.unit)
        put("dx", e.dx)
        put("nor", e.nor)
        put("ot1", e.ot1)
        put("ot2", e.ot2)
        put("ot3", e.ot3)
    }

    fun insert(e: Entry): Long = writableDatabase.insert("entries", null, toValues(e))

    fun update(e: Entry) {
        writableDatabase.update("entries", toValues(e), "id=?", arrayOf(e.id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete("entries", "id=?", arrayOf(id.toString()))
    }

    fun deleteAll() {
        writableDatabase.delete("entries", null, null)
    }

    fun get(id: Long): Entry? {
        readableDatabase.query(
            "entries", null, "id=?", arrayOf(id.toString()), null, null, null
        ).use { c ->
            if (c.moveToFirst()) return readEntry(c)
        }
        return null
    }

    fun between(from: String, to: String): List<Entry> {
        val out = mutableListOf<Entry>()
        readableDatabase.query(
            "entries", null, "date>=? AND date<=?", arrayOf(from, to),
            null, null, "date ASC, id ASC"
        ).use { c ->
            while (c.moveToNext()) out.add(readEntry(c))
        }
        return out
    }

    fun all(): List<Entry> {
        val out = mutableListOf<Entry>()
        readableDatabase.query(
            "entries", null, null, null, null, null, "date ASC, id ASC"
        ).use { c ->
            while (c.moveToNext()) out.add(readEntry(c))
        }
        return out
    }

    fun count(): Long {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM entries", null).use { c ->
            if (c.moveToFirst()) return c.getLong(0)
        }
        return 0
    }

    fun distinctValues(column: String): List<String> {
        val out = mutableListOf<String>()
        readableDatabase.query(
            true, "entries", arrayOf(column), null, null, null, null, column, null
        ).use { c ->
            while (c.moveToNext()) {
                val v = c.getString(0)
                if (!v.isNullOrBlank()) out.add(v)
            }
        }
        return out
    }

    private fun readEntry(c: android.database.Cursor): Entry = Entry(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        date = c.getString(c.getColumnIndexOrThrow("date")),
        job = c.getString(c.getColumnIndexOrThrow("job")),
        unit = c.getString(c.getColumnIndexOrThrow("unit")),
        dx = c.getString(c.getColumnIndexOrThrow("dx")),
        nor = c.getString(c.getColumnIndexOrThrow("nor")),
        ot1 = c.getDouble(c.getColumnIndexOrThrow("ot1")),
        ot2 = c.getDouble(c.getColumnIndexOrThrow("ot2")),
        ot3 = c.getDouble(c.getColumnIndexOrThrow("ot3"))
    )
}
