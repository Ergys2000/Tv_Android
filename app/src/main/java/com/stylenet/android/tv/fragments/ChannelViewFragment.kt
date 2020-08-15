package com.stylenet.android.tv.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Channel
import com.stylenet.android.tv.viewmodels.ChannelViewModel
import java.lang.Exception
import java.lang.Math.abs

private const val TAG = "Channel View Fragment"
class ChannelViewFragment: Fragment(), SettingsDialogFragment.Callbacks{
    private lateinit var channels: ArrayList<Channel>
    private var index: Int = 0 // to keep track of the current channel were playing
    private var url: String? = null
    private lateinit var playerView: PlayerView // the playerView displayed
    private lateinit var player: SimpleExoPlayer // the player used to play the video
    private val channelViewModel: ChannelViewModel by lazy {
        ViewModelProvider(this).get(ChannelViewModel::class.java)
    }

    companion object{
        fun newinstance(channels: ArrayList<Channel>, index: Int): Fragment{
            return ChannelViewFragment().apply {
                this.channels = channels
                this.index = index
                this.url = channels[index].link
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_channel_view,
            container, false)
        playerView = view.findViewById(R.id.videoView)
        if(url == null){ // this happens on configuration change
            url = channelViewModel.url
            channels = channelViewModel.channels
            index = channelViewModel.index
        }else{ // this happens when we first create this fragment
            channelViewModel.channels = channels
            channelViewModel.index = index
            channelViewModel.url = url as String
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }
    override fun onStop(){
        super.onStop()
        releasePlayer()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onDetach() {
        super.onDetach()
    }

    // creates the media source for the current url
    private fun createMediaSource(): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(context,
            Util.getUserAgent(context, "stylenet"))
        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(url))
        return mediaSource
    }

    private fun releasePlayer(){
        playerView.player = null
        player.release()
    }

    private fun initializePlayer(){
        try{
            val loadControl = makeCustomLoadControl()
            player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(context),
                DefaultTrackSelector(),
                loadControl)
            playerView.player = player
            setButtonActions()
            player.prepare(createMediaSource())
            player.playWhenReady = true
        }catch(e: Exception){
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun makeCustomLoadControl(): DefaultLoadControl{
        return DefaultLoadControl.Builder().setBufferDurationsMs(
            // when we have less than 8 mins buffered we start buffering again
            8*60*1000,
            // we stop buffering when we have 10 mins ready
            10*60*1000,
            // we start playing after 1 sec is buffered
            1*1000,
            // after rebuffering we make sure we have 3 sec before we start
            3*1000
        ).createDefaultLoadControl()
    }

    private fun setButtonActions(){
        playerView.findViewById<ImageButton>(R.id.back).apply{
            setOnClickListener {
                requireActivity().onBackPressed()
            }
            setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.transparent))
                }
            }
        }

        playerView.findViewById<ImageButton>(R.id.settings).apply {
            setOnClickListener {
                val isChecked = player.volume == 0F
                val speed = player.playbackParameters.speed

                val fragmentManager = childFragmentManager
                val fragment = SettingsDialogFragment.newInstance(isChecked, speed)
                fragment.show(fragmentManager, "dialog")
            }
            setOnFocusChangeListener { v, hasFocus ->
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.buttonFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.transparent))
                }
            }
        }

        playerView.findViewById<ImageButton>(R.id.next).apply{
            setOnClickListener {
                index = (index + 1)%channels.size
                notifyChannelChanged()
                Log.i(TAG, "New Channel Index: $index")
            }
        }

        playerView.findViewById<ImageButton>(R.id.prev).apply{
            setOnClickListener {
                index = (index - 1) % channels.size
                if(index < 0){
                    index = channels.size -1
                }
                notifyChannelChanged()
                Log.i(TAG, "New Channel Index: $index")
            }
        }
    }

    private fun notifyChannelChanged(){
        url = channels[index].link
        releasePlayer()
        initializePlayer()
    }

    override fun onVolumeSwitched() {
        val currVolume = player.volume
        if(currVolume > 0F){
            player.volume = 0F
        }else{
            player.volume = 1F
        }
    }

    override fun onSpeedChanged(value: Float) {
        val playbackParameters = PlaybackParameters(value)
        player.playbackParameters = playbackParameters
    }
}


