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

package com.jesusd0897.crashreporter.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.jesusd0897.crashreporter.R
import com.jesusd0897.crashreporter.exception.IntentResolveException
import com.jesusd0897.crashreporter.model.ReportListener
import com.jesusd0897.crashreporter.model.StyleReporter
import com.jesusd0897.kutil.dialogBuilder
import com.jesusd0897.kutil.provideSimpleDeviceInfo
import java.io.BufferedReader
import java.io.InputStreamReader

const val REQUEST_EMAIL = 169

@Throws(IntentResolveException::class)
private fun composeEmailAndWait(
    activity: Activity,
    subject: String? = null,
    message: String? = null,
    vararg addresses: String?,
) {
    val intent = Intent(Intent.ACTION_SENDTO)
        .setData(Uri.parse("mailto:"))
        .putExtra(Intent.EXTRA_EMAIL, addresses)
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .putExtra(Intent.EXTRA_TEXT, message)
    if (intent.resolveActivity(activity.packageManager) != null)
        activity.startActivityForResult(intent, REQUEST_EMAIL)
    else throw IntentResolveException("There are no handler available that satisfy this Intent")
}

@Throws(IntentResolveException::class)
fun trySendCrashReport(
    activity: Activity,
    listener: ReportListener? = null,
    subject: String? = null,
    useAutoHandler: Boolean = true,
    styleReporter: StyleReporter? = null,
    addresses: Array<String> = arrayOf(),
) {
    val trace = StringBuilder()
    try {
        val reader = BufferedReader(InputStreamReader(activity.openFileInput("stack.trace")))
        while (reader.readLine().also { trace.append(it + "\n") } != null);
    } catch (ex: Exception) {
        activity.deleteFile("stack.trace")
        ex.printStackTrace()
    }
    if (trace.isNotBlank()) {
        val message = provideSimpleDeviceInfo() + "\n\n" + trace
        val builder = dialogBuilder(
            activity, R.string.crash_report, R.string.crash_report_msg,
            R.string.send, R.string.cancel, R.drawable.ic_round_bug_report, false,
            { _, _ ->
                if (useAutoHandler) composeEmailAndWait(
                    activity = activity,
                    subject = subject,
                    message = provideSimpleDeviceInfo() + "\n\n" + trace,
                    addresses = addresses,
                )
                listener?.onSendClick(message)
            }, { _, _ ->
                listener?.onCancelClick(message)
                activity.deleteFile("stack.trace")
            })
        styleReporter?.let {
            builder.setTitle(it.title)
            builder.setMessage(it.message)
            builder.setIcon(it.icon)
        }
        builder.show()
    } else {
        listener?.onNoNeeded()
        activity.deleteFile("stack.trace")
    }
    activity.deleteFile("stack.trace")
}