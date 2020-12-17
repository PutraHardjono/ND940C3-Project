package com.udacity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadInfo (val fileName: String, val status: String) : Parcelable