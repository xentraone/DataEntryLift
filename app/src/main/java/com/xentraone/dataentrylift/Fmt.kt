package com.xentraone.dataentrylift

object Fmt {
    /** 2.0 -> "2", 2.5 -> "2.5" */
    fun num(v: Double): String =
        if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
}
