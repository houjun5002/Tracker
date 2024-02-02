package com.richard.tracker.model

import java.util.*

class CommitLog {
    var pageName: String? = null

    var viewName: String? = null

    /**
     * total exposure times inside the page
     */
    var exposureTimes = 0

    /**
     * total exposure duration inside the page
     */
    var totalDuration: Long = 0

    /**
     * the attached info
     */
    var argsInfo = mutableMapOf<String, Any?>()

    fun CommitLog(pageName: String?, viewName: String?) {
        this.pageName = pageName
        this.viewName = viewName
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val commitLog = o as CommitLog
        return if (pageName != commitLog.pageName) false else viewName == commitLog.viewName
    }

    override fun hashCode(): Int {
        var result = pageName.hashCode()
        result = 31 * result + viewName.hashCode()
        return result
    }
}