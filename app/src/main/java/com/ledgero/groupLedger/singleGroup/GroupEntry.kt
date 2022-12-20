package com.ledgero.groupLedger.singleGroup

import android.os.Parcelable
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.VoiceNote
import com.ledgero.other.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
 class GroupEntry(
    var cashInOutFlag: Boolean?=null
  ) : Entries(), Parcelable
//group entry is a entry
