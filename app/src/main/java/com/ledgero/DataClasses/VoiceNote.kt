package com.ledgero.DataClasses

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VoiceNote(var localPath: String?=null,
                     var duration: Int?=null,
                     var firebaseDownloadURI:String?=null
                     ): Parcelable
