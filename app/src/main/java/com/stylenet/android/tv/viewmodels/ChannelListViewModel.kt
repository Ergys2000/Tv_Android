package com.stylenet.android.tv.viewmodels

import androidx.lifecycle.ViewModel
import com.stylenet.android.tv.models.Channel

class ChannelListViewModel: ViewModel() {
    var channels: ArrayList<Channel> = arrayListOf(
        Channel("RTSH Sport", "http://edge01eu.ekranet.com:80/rtshsport_240p/index.m3u8"),
        Channel("Euro Al", "http://5.135.92.131:1935/live/euroAl/chunklist.m3u8"),
        Channel("PeaceTV", "http://82.114.67.178:8081/hls/PeaceTV.m3u8"),
        Channel("News24", "http://tv.balkanweb.com:8081/news24/livestream/playlist.m3u8")
    )
}