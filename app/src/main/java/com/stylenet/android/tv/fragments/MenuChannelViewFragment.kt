package com.stylenet.android.tv.fragments

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Channel
import com.stylenet.android.tv.viewmodels.ChannelViewModel
import java.lang.Exception

private const val TAG = "TestFragment"
class MenuChannelViewFragment: Fragment(), SettingsDialogFragment.Callbacks{
    private lateinit var recyclerView: RecyclerView
    private lateinit var playerView: PlayerView
    private lateinit var player: SimpleExoPlayer
    private lateinit var menuView: View
    private lateinit var searchView: SearchView
    private lateinit var hideButton: Button

    private val channelViewModel: ChannelViewModel by lazy {
        ViewModelProvider(this).get(ChannelViewModel::class.java)
    }
    var channels: ArrayList<Channel> = ArrayList()
    var index: Int = 0

    companion object{
        fun newInstance(channels: ArrayList<Channel>): Fragment{
            return MenuChannelViewFragment().apply {
                this.channels = channels
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu_view, container, false)
        playerView = view.findViewById(R.id.videoView)
        menuView = view.findViewById(R.id.menu)
        recyclerView = view.findViewById(R.id.channel_recycler_view)
        searchView = view.findViewById(R.id.search_view)
        hideButton = view.findViewById(R.id.hide)

        if(channels.isEmpty()){ // this happens on configuration change
            channels = channelViewModel.channels
            index = channelViewModel.index
        }
        else{ // this happens when we first create this fragment
            channelViewModel.channels = channels
            channelViewModel.index = index
        }
        return view
    }

    // methods to override
    override fun onStart() {
        super.onStart()
        setButtonActions()
        updateUI()
        initializePlayer()
    }
    override fun onStop() {
        super.onStop()
        releasePlayer()
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
        player.setPlaybackParameters(playbackParameters)
    }

    // animations
    private fun hideAnimation(){
        val finalValue = menuView.width.toFloat()
        ObjectAnimator
            .ofFloat(menuView, "x", -finalValue)
            .setDuration(700)
            .start()
    }
    private fun showAnimation(){
        ObjectAnimator
            .ofFloat(menuView, "x", 0F)
            .setDuration(700)
            .start()
    }

    // wiring up the buttons
    private fun setButtonActions(){
        setHideFunction()
        setPlayerViewFunctions()
        setSearchViewFunction()
        setSearchViewFunction()
    }

    // functions to wire up the buttons
    private fun setHideFunction(){
        hideButton.apply {
            setOnClickListener {
                hideAnimation()
                menuView.isEnabled = false
            }
        }

    }
    private fun setPlayerViewFunctions(){
        playerView.findViewById<ImageButton>(R.id.channel_list).apply {
            setOnClickListener {
                showAnimation()
                menuView.isEnabled = true
                searchView.requestFocus()
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
                //val fragment = ChannelListDialogFragment()
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
        playerView.findViewById<ImageButton>(R.id.next).apply {
            setOnClickListener {
                index = (index + 1)%channels.size
                notifyChannelChanged()
                Log.i(TAG, "New Channel Index: $index")
            }
        }
        playerView.findViewById<ImageButton>(R.id.prev).apply {
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
    private fun setSearchViewFunction(){
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val adapter = recyclerView.adapter as ChannelAdapter
                adapter.filter.filter(newText)
                return false
            }

        })
    }
    private fun setPlayerTitle(){
        playerView.findViewById<TextView>(R.id.channel_name).text = channels[index].name
    }

    // setting up the player
    private fun makeCustomLoadControl(): DefaultLoadControl {
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
    private fun createMediaSource(): MediaSource {
        val url: String = channels[index].link
        val dataSourceFactory = DefaultDataSourceFactory(context,
            Util.getUserAgent(requireContext(), "stylenet"))
        if(url.endsWith(".m3u8", true)){
            return HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url))
        }
        else if(url.endsWith(".mpd", true)){
            return DashMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))
        }
        else{
            return ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url))
        }
    }
    private fun releasePlayer(){
        playerView.player = null
        player.release()
    }
    private fun initializePlayer(){
        try{
            val loadControl = makeCustomLoadControl()
            player = SimpleExoPlayer.Builder(requireContext())
                .setLoadControl(loadControl)
                .build()
            //player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            playerView.player = player
            setPlayerTitle()
            player.prepare(createMediaSource())
            player.playWhenReady = true
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }catch(e: Exception){
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    // for updating the list of channels
    private fun updateUI(){
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ChannelAdapter(channels)
    }
    private fun notifyChannelChanged(){
        player.prepare(createMediaSource())
        channelViewModel.index = index
        setPlayerTitle()
    }

    // classes used for the recyclerView that holds the channels
    private inner class ChannelHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var channel: Channel
        private var index: Int = 0
        private var nameTextView: TextView = itemView.findViewById(R.id.channel_name)

        fun bind(channel: Channel, i: Int){
            this.channel = channel
            this.index = i
            nameTextView.text = channel.name
        }

        init{
            itemView.setOnClickListener(this)
            itemView.onFocusChangeListener = CustomFocusListener()
        }

        // we also implemented the interface onClickListener so we can hook up our functions to
        // our view holders right here
        override fun onClick(v: View?) {
            this@MenuChannelViewFragment.index = index
            notifyChannelChanged()
        }

        private inner class CustomFocusListener(): View.OnFocusChangeListener{
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if(hasFocus){
                    v?.setBackgroundColor(resources.getColor(R.color.channelViewFocused))
                }else{
                    v?.setBackgroundColor(resources.getColor(R.color.transparent))
                }
            }

        }
    }
    private inner class ChannelAdapter(var channels: ArrayList<Channel>) :
        RecyclerView.Adapter<ChannelHolder>(),
        Filterable
    {
        private var channelsAll: ArrayList<Channel> = ArrayList(channels)
        private var filter: Filter = ChannelFilter()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_channel_list_item, parent, false)
            return ChannelHolder(view)
        }

        override fun getItemCount(): Int {
            return channels.size
        }

        // defines how we bind the holder at that position, apparently we assign it the proper
        // channel
        override fun onBindViewHolder(holder: ChannelHolder, position: Int) {
            val channel = channels[position]
            holder.bind(channel, position)
        }

        override fun getFilter(): Filter {
            return filter
        }

        private inner class ChannelFilter: Filter(){
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val filteredList =  ArrayList<Channel>()

                if(charSequence.isNullOrEmpty()){
                    filteredList.addAll(channelsAll)
                }else{
                    for(movie in channelsAll){
                        val contains = movie.name.contains(charSequence, true)
                        if(contains){
                            filteredList.add(movie)
                        }
                    }
                }
                return FilterResults().apply {
                    values = filteredList
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                channels.clear()
                channels.addAll(results?.values as ArrayList<Channel>)
                notifyDataSetChanged()
            }
        }
    }

    // remote commands overrides
    fun onMenuPressed(){
        if(menuView.isEnabled){
            hideAnimation()
            menuView.isEnabled = false
            view?.findViewById<ImageButton>(R.id.exo_pause)?.requestFocus()
        }else{
            showAnimation()
            menuView.isEnabled = true
            hideButton.requestFocus()
        }
    }
    fun onButtonPressed(){
        playerView.showController()
    }

}