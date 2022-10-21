package com.ledgero.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ledgero.LoginActivity
import com.ledgero.R
import kotlinx.android.synthetic.main.fragment_more.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var view = inflater.inflate(R.layout.fragment_more, container, false)
        var log = view.findViewById<Button>(R.id.logout)
        Log.d("more", "onCreateView: skskdsdksignout ")

        log.setOnClickListener {

            Log.d("more", "onCreateView: signout ")
            var auth: FirebaseAuth = Firebase.auth
            auth.signOut()
            startActivity(Intent(view.context,LoginActivity::class.java))
            var ac= view.context as Activity
            ac.finish()

        }
        return view
    }



}