package com.ledgero.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


class TimeFormatter {

    companion object{

        fun getFormattedTime(time: String, date: Date): String {

            val format = "dd MMM yyyy hh:mm a"


            val sdf = SimpleDateFormat(format)
            // default system timezone if passed null or empty
            // default system timezone if passed null or empty
            val timeZone = Calendar.getInstance().timeZone.id

            // set timezone to SimpleDateFormat
            // set timezone to SimpleDateFormat
            sdf.timeZone = TimeZone.getTimeZone(timeZone)
            // return Date in required format with timezone as String
            // return Date in required format with timezone as String
            val d = sdf.format(date).toString().split(' ')
            val localTime = d[3] + " " + d[4]



            Log.d("TimeFormatter", "getFormattedTime: $time ")

            val tokens = time.split(' ')
            //  LocalTime.parse(tokens[3])

            return tokens[0] + ", " + tokens[2] + " " + tokens[1] + " | " + localTime
        }

    }
}