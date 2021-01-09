/*
 * Copyright (c) 2021 jesusd0897.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jesusd0897.crashreporter.model

import android.app.Application
import android.content.Context
import java.io.IOException

interface ReportListener {
    fun onNoNeeded()
    fun onSendClick(errorTrace: String?)
    fun onCancelClick(errorTrace: String? = null)
}

class CrashReporterHandler(private val app: Application) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        var arr = e.stackTrace
        val report = StringBuilder("$e\n\n").append("********** Stack trace **********\n")
        for (stackTraceElement in arr) report.append("    $stackTraceElement\n")
        report.append("******************************\n\n")
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report.append("********** Cause **********\n\n")
        val cause = e.cause
        if (cause != null) {
            report.append(cause.toString()).append("\n\n")
            arr = cause.stackTrace
            for (stackTraceElement in arr) report.append("    $stackTraceElement\n")
        }
        report.append("******************************\n\n")
        try {
            val trace = app.openFileOutput("stack.trace", Context.MODE_PRIVATE)
            trace.write(report.toString().toByteArray())
            trace.close()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        defaultHandler?.uncaughtException(t, e)
    }

}