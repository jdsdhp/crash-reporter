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

package com.jesusd0897.crashreporter.sample

import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jesusd0897.crashreporter.exception.IntentResolveException
import com.jesusd0897.crashreporter.model.ReportListener
import com.jesusd0897.crashreporter.model.StyleReporter
import com.jesusd0897.crashreporter.sample.databinding.ActivityMainBinding
import com.jesusd0897.crashreporter.util.trySendCrashReport

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        setContentView(binding.root)
        binding.apply {
            oneActionBtn.setOnClickListener {
                throw TestSampleException("It's just a sample TestSampleException for test CrashReporter library.")
            }
            twoActionBtn.setOnClickListener {
                throw NumberFormatException("It's just a sample NumberFormatException for test CrashReporter library.")
            }
            threeActionBtn.setOnClickListener {
                throw ArrayIndexOutOfBoundsException("It's just a sample ArrayIndexOutOfBoundsException for test CrashReporter library.")
            }
        }

        try {
            trySendCrashReport(
                activity = this,
                addresses = arrayOf(
                    "sample@gmail.com",
                    "other@yahoo.com"
                ), //Optionl.  Only useful if useAutoHandler = true
                subject = "My crash report", //Optionl. Only useful if useAutoHandler = true
                useAutoHandler = false, //Optionl
                styleReporter = StyleReporter(  //Optionl
                    title = "Dialog Title",
                    message = "Dialog message",
                    icon = ContextCompat.getDrawable(this, R.drawable.ic_round_bug_report)
                ),
                listener = object : ReportListener {  //Optionl
                    override fun onNoNeeded() {
                        //No crash trace detected.
                        Toast.makeText(this@MainActivity, "No crash trace detected.", LENGTH_SHORT)
                            .show()
                        binding.crashInput.text = null
                    }

                    override fun onSendClick(errorTrace: String?) {
                        //Handle error trace on Send option clicked.
                        Toast.makeText(this@MainActivity, "SEND option clicked.", LENGTH_SHORT)
                            .show()
                        binding.crashInput.setText(errorTrace)
                    }

                    override fun onCancelClick(errorTrace: String?) {
                        //Crash detected but Cancel option was clicked.
                        Toast.makeText(this@MainActivity, "CANCEL option clicked.", LENGTH_SHORT)
                            .show()
                        binding.crashInput.setText(errorTrace)
                    }
                })
        } catch (ex: IntentResolveException) {
            //There are no handler available that satisfy this Intent
            Toast.makeText(this@MainActivity, ex.message, LENGTH_SHORT).show()
        } catch (ex: Exception) {
            Toast.makeText(this@MainActivity, ex.message, LENGTH_SHORT).show()
        }
    }

}