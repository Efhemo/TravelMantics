package com.efhems.travelmantics

import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.efhems.travelmantics.TListAdapter.OnclickListeer
import com.efhems.travelmantics.model.Model
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth


/**
 * A simple [Fragment] subclass.
 *
 */
class UserFragment : Fragment() {

    private var mAuth: FirebaseAuth?  = FirebaseAuth.getInstance()
    private val TAG = UserFragment::class.java.name
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        val rc: RecyclerView = view.findViewById(R.id.rc)

        val adapter = TListAdapter(object : OnclickListeer {
            override fun onClick(model: Model) {

            }
        })

        rc.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(
            rc.context,
            DividerItemDecoration.VERTICAL
        )
        rc.addItemDecoration(dividerItemDecoration)

        val database = FirebaseDatabase.getInstance().getReference()

        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val models: MutableList<Model> = ArrayList<Model>()
                val dataSnapshot1 = p0.child(""+mAuth?.uid)
                for (dataSnapshot2 in dataSnapshot1.getChildren()) {
                    val idKey = dataSnapshot2.getKey() //get all project name

                    //get task here
                    Log.d(TAG, "onDataChange: " + idKey)
                    val id = dataSnapshot2.getKey()
                    Log.d(TAG, "onDataChange: " + id)

                    val amount = dataSnapshot2.child("amount").getValue(Int::class.java)
                    val city = dataSnapshot2.child("city").getValue(String::class.java)
                    val desc = dataSnapshot2.child("desc").getValue(String::class.java)
                    val idk = dataSnapshot2.child("id").getValue(String::class.java)
                    val image = dataSnapshot2.child("image").getValue(String::class.java)

                    //Log.i(TAG, " amount"+ amount +" "+ " "+city+ " "+desc+ " "+idk+ " "+ image)
                    val model = Model(idk!!, city!!, amount!!, desc!!,image!!)
                    models.add(model)
                }

                adapter.submitList(models)
            }

        })


        arguments?.let {
            it
        }


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        if (id == R.id.add_deals) {
            this.findNavController().navigate(R.id.action_userFragment_to_adminFragment3)
            return true
        }

        return false
    }
}
