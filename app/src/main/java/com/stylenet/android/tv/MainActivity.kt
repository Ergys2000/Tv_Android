package com.stylenet.android.tv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ui.PlayerView
import com.stylenet.android.tv.fragments.*
import com.stylenet.android.tv.models.Channel

private var channels: ArrayList<Channel> = arrayListOf(
    Channel("MPEG-Dash", "http://d3rlna7iyyu8wu.cloudfront.net/DolbyVision_Atmos/profile8.1_DASH/p8.1.mpd"),
    Channel("Apple-HLS", "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8"),
    Channel("Alb Uk-.stream", "rtmp://albuk.dyndns.tv:1935/albuk/albuk.stream"),
    Channel("Euro Al-HLS", "http://5.135.92.131:1935/live/euroAl/chunklist.m3u8"),
    Channel("PeaceTV-HLS", "http://82.114.67.178:8081/hls/PeaceTV.m3u8")
)

class MainActivity : AppCompatActivity(),
    LogInFragment.Callbacks,
    FrontPageFragment.Callbacks,
    ChannelListFragment.Callbacks{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currFragment: Fragment?  =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currFragment == null){
            val fragment = LogInFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onUserAccepted(username: String, password: String, link: String) {
        val fragment = FrontPageFragment.getInstance(username, password, link)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onTvClicked() {
        //val fragment = ChannelListFragment.newInstance()
        val fragment = MenuChannelViewFragment.newInstance(channels)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onChannelClicked(channels: ArrayList<Channel>, index: Int) {
        val fragment = ChannelViewFragment.newinstance(channels, index)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val currFragment: Fragment?  =
            supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (keyCode == KeyEvent.KEYCODE_MENU){
            if(currFragment is MenuChannelViewFragment){
                currFragment.onMenuPressed()
                return true
            }
        }
        if(currFragment is MenuChannelViewFragment){
            currFragment.onButtonPressed()
        }
        return super.onKeyDown(keyCode, event)
    }
}
