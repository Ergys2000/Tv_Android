package com.stylenet.android.tv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stylenet.android.tv.fragments.*
import com.stylenet.android.tv.models.Channel

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
        val fragment = ChannelListFragment.getInstance()
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
}