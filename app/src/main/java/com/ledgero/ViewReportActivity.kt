package com.ledgero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.ledgero.DataClasses.Entries
import com.ledgero.DataClasses.User
import com.ledgero.adapters.ViewReportAdapter
import com.ledgero.databinding.ActivityViewReportBinding
import com.ledgero.groupLedger.data.GroupInfo
import com.ledgero.groupLedger.recyclerViews.GroupAdapter
import com.ledgero.groupLedger.singleGroup.GroupEntry

class ViewReportActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityViewReportBinding
    private val dbReference = FirebaseDatabase.getInstance().reference
    private val allEntries = ArrayList<Pair<Boolean,Any>>()
    private lateinit var rv: RecyclerView
    private lateinit var adapter: ViewReportAdapter
    private var isGroup:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_report)
        rv = findViewById(R.id.report_RV)
        rv.layoutManager = LinearLayoutManager(this)

        var t = intent.getBooleanExtra("isGroup",false)
        Log.d("VRA", "onCreate:  $t")
        isGroup=t
        adapter= ViewReportAdapter(this,allEntries,isGroup)

        if (!isGroup){
        if (User.getUserSingleLedgers()?.size!! >0){

            User.getUserSingleLedgers()?.forEach {ledger ->

                dbReference.child("ledgerEntries").child(ledger.ledgerUID!!).get()
                    .addOnSuccessListener {
                        if (it.exists()){
                            allEntries.add( Pair<Boolean,Any>(false, ledger.friend_userName!!))
                            it.children.forEach { d ->
                                val m = d.getValue(Entries::class.java)!!
                                allEntries.add(Pair(true,m))
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
            }
            rv.adapter = adapter

        }
        }else{

            dbReference.child("users").child(User.userID!!).child("user_group_Ledgers").get()
                .addOnSuccessListener {
                    val userGroups= ArrayList<GroupInfo>()
                    if (it.exists()){
                        it.children.forEach { d ->
                            val t= d.getValue(GroupInfo::class.java)!!
                            userGroups.add(t)
                        }

                        userGroups.forEach {group ->

                            dbReference.child("groupEntries").child(group.groupUID).get()
                                .addOnSuccessListener {
                                    if (it.exists()){
                                        allEntries.add( Pair<Boolean,Any>(false, group.groupName))
                                        it.children.forEach { d ->
                                            val m = d.getValue(GroupEntry::class.java)!!
                                            allEntries.add(Pair(true,m))
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                        }

                    }
                }



                rv.adapter = adapter



        }




        }
}