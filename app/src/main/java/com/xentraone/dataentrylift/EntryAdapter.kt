package com.xentraone.dataentrylift

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EntryAdapter(
    private val onClick: (Entry) -> Unit,
    private val onLongClick: (Entry) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed class Row {
        class Header(val label: String) : Row()
        class Item(val entry: Entry) : Row()
    }

    private val rows = mutableListOf<Row>()

    fun setEntries(entries: List<Entry>) {
        rows.clear()
        var lastDate = ""
        for (e in entries) {
            if (e.date != lastDate) {
                rows.add(Row.Header(Periods.display(e.date)))
                lastDate = e.date
            }
            rows.add(Row.Item(e))
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        if (rows[position] is Row.Header) 0 else 1

    override fun getItemCount(): Int = rows.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            HeaderVH(inflater.inflate(R.layout.item_header, parent, false))
        } else {
            ItemVH(inflater.inflate(R.layout.item_entry, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = rows[position]
        if (holder is HeaderVH && row is Row.Header) holder.bind(row.label)
        if (holder is ItemVH && row is Row.Item) holder.bind(row.entry, onClick, onLongClick)
    }

    private class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(label: String) {
            itemView.findViewById<TextView>(R.id.headerText).text = label
        }
    }

    private class ItemVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(e: Entry, onClick: (Entry) -> Unit, onLongClick: (Entry) -> Unit) {
            itemView.findViewById<TextView>(R.id.line1).text =
                listOf(e.job, e.unit, e.dx).filter { it.isNotBlank() }.joinToString("  ·  ")
            val hours = buildString {
                if (e.nor.isNotBlank()) append("NOR ").append(e.nor)
                if (e.ot1 != 0.0) append("   OT1 ").append(Fmt.num(e.ot1))
                if (e.ot2 != 0.0) append("   OT2 ").append(Fmt.num(e.ot2))
                if (e.ot3 != 0.0) append("   OT3 ").append(Fmt.num(e.ot3))
            }
            itemView.findViewById<TextView>(R.id.line2).text =
                if (hours.isBlank()) "—" else hours
            itemView.setOnClickListener { onClick(e) }
            itemView.setOnLongClickListener { onLongClick(e); true }
        }
    }
}
