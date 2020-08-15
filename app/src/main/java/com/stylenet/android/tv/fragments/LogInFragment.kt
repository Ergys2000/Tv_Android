package com.stylenet.android.tv.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stylenet.android.tv.R

class LogInFragment: Fragment() {
    private lateinit var userTextField: EditText
    private lateinit var passTextField: EditText
    private lateinit var logInButton: Button
    private lateinit var linkTextField: EditText
    private var callbacks: Callbacks? = null

    interface Callbacks{
        fun onUserAccepted(username: String, password: String, link:String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_in, container, false)

        userTextField = view.findViewById(R.id.username)
        passTextField = view.findViewById(R.id.password)
        linkTextField = view.findViewById(R.id.link)
        logInButton = view.findViewById(R.id.log_in)

        setButtonActions()
        return view
    }

    override fun onStart() {
        super.onStart()
        logInButton.setOnClickListener {
            val username = userTextField.text.toString()
            val password = passTextField.text.toString()
            val link: String = linkTextField.text.toString()
            if(username == "gysi" && password == "gysi" && link == "styleNet.com"){
                callbacks?.onUserAccepted(username, password, link)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun setButtonActions(){
        userTextField.setText("gysi")
        passTextField.setText("gysi")
        linkTextField.setText("styleNet.com")
        logInButton.setOnFocusChangeListener{ v, hasFocus ->
            if(hasFocus){
                v?.setBackgroundColor(resources.getColor(R.color.colorAccent))
            }else{
                v?.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
            }
        }
    }
}