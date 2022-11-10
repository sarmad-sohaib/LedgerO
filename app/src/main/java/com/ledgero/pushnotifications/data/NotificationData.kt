package com.ledgero.pushnotifications.data

data class NotificationData(
    val title: String,
    val message:String,
    val ledgerUID:String,
    val ledgerEntry:String
)