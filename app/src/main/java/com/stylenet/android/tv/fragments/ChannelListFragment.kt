package com.stylenet.android.tv.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stylenet.android.tv.R
import com.stylenet.android.tv.models.Channel
import com.stylenet.android.tv.viewmodels.ChannelListViewModel

class ChannelListFragment: Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChannelAdapter
    private lateinit var channels: ArrayList<Channel>
    private var callbacks: Callbacks? = null
    private lateinit var searchView: SearchView

    // we use the channelListViewModel to make sure the channels are not lost in
    // configuration change
    private val channelListViewModel by lazy{
        ViewModelProvider(this).get(ChannelListViewModel::class.java)
    }

    // every time we get an instance of this class we use this function
    // even though right now it does nothing later we can add stuff
    companion object{
        fun newInstance(): Fragment{
            return ChannelListFragment()
        }
    }
    // define an interface the main activity will implement
    interface Callbacks{
        fun onChannelClicked(channels: ArrayList<Channel>, index: Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_channel_list, container, false)
        recyclerView = view.findViewById(R.id.channel_recycler_view) as RecyclerView
        searchView = view.findViewById(R.id.search_view)
        channels = channelListViewModel.channels
        updateUI()
        setButtonActions()
        return view
    }

    // this updates the list that the user sees.
    private fun updateUI(){
        // sets the layoutManager of the recycler view to a linear layout manager
        recyclerView.layoutManager = LinearLayoutManager(context)
        // sets the adapter of the recycler view to our defined adapter
        adapter = ChannelAdapter(channels)
        recyclerView.adapter = adapter
    }

    private fun setButtonActions(){
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }

        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }
    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

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
            itemView.onFocusChangeListener = customFocusListener()
        }

        // we also implemented the interface onClickListener so we can hook up our functions to
        // our view holders right here
        override fun onClick(v: View?) {
            callbacks?.onChannelClicked(channels, index)
        }

        private inner class customFocusListener(): View.OnFocusChangeListener{
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
}