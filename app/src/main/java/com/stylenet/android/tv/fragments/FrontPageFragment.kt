package com.stylenet.android.tv.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.stylenet.android.tv.R

class FrontPageFragment: Fragment() {
    private lateinit var tvButton: Button
    private lateinit var youtubeButton: Button
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var link: String
    private var callbacks: Callbacks? = null

    interface Callbacks{
        fun onTvClicked()
    }

    companion object{
        fun getInstance(username: String, password: String, link: String): Fragment{
            val fragment = FrontPageFragment()
            fragment.apply{
                this.username = username
                this.password = password
                this.link = link
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_front_page, container, false)
        tvButton = view.findViewById(R.id.tv)
        youtubeButton = view.findViewById(R.id.youtube)
        setButtonActions()
        return view
    }

    override fun onStart() {
        super.onStart()
        tvButton.setOnClickListener {
            callbacks?.onTvClicked()
        }
        youtubeButton.setOnClickListener {
            val intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/"))
            startActivity(intent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks // when the fragment is attached we initialize callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null // when the fragment is detached we set the callbacks to null
    }
    private fun setButtonActions(){
        tvButton.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                v?.setBackgroundColor(resources.getColor(R.color.colorAccent))
            }else{
                v?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
            }
        }
        youtubeButton.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                v?.setBackgroundColor(resources.getColor(R.color.colorAccent))
            }else{
                v?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
            }
        }
    }
}