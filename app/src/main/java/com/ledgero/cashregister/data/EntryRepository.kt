package com.ledgero.cashregister.data

import com.ledgero.cashregister.data.remotedatasource.MyCallback

interface EntryRepository {

    suspend fun getEntries(uId: String, myCallback: MyCallback)

    suspend fun insertEntry(uId: String, entry: Entry)

    suspend fun deleteEntry(uId: String, id: String)

    suspend fun updateEntry(uId: String, id: String, entry: Entry)

    suspend fun getSumByProperty(uId: String, property: String)
}

// 👌🏼