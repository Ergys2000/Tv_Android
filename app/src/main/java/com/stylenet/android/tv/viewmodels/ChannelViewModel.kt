package com.stylenet.android.tv.viewmodels

import androidx.lifecycle.ViewModel
import com.stylenet.android.tv.models.Channel

class ChannelViewModel: ViewModel() {
    lateinit var url: String
    lateinit var channels: ArrayList<Channel>
    var index: Int = 0
}