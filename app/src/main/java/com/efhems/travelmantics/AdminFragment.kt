package com.efhems.travelmantics


import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.efhems.travelmantics.databinding.FragmentAdminBinding
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import androidx.databinding.adapters.NumberPickerBindingAdapter.setValue
import androidx.navigation.fragment.findNavController
import com.efhems.travelmantics.model.Model
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*


class AdminFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private val STORAGE_REQUEST_CODE = 2
    private var featureImage: String? = null

    private var binding: FragmentAdminBinding? = null
    private var myRef: DatabaseReference? = null

    private var mAuth: FirebaseAuth? = null

    private var mStorageRef: StorageReference? = null


    private val TAG = AdminFragment::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true);

        //Requesting storage permission
        if (checkSelfPermission(
                activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) === PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            requestStoragePermission()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_admin, container, false
        )

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        mStorageRef = FirebaseStorage.getInstance().getReference();
        binding?.selectBtn?.setOnClickListener {
            pickPicture(PICK_IMAGE_REQUEST)
        }

        return binding?.root
    }

    //this methods makes the storage permission request
    private fun requestStoragePermission() {
        //request the permission.
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {

            val filePathString = getFilePath(data)

            if (filePathString != null) {

                val file = File(filePathString)
                val bitmap = BitmapFactory.decodeFile(file.getAbsolutePath())
                binding?.showImg?.setImageBitmap(bitmap)
                featureImage = filePathString
            }
        }
    }


    private fun getFilePath(data: Intent): String? {
        val selectedImg = data.data

        val file: File? = null
        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImg);

        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity?.getContentResolver()?.query(selectedImg, filePathColumn, null, null, null)

        var filePathString: String? = null
        if (cursor != null) {
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            filePathString = cursor.getString(columnIndex)


        }
        return filePathString
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Permission GRANTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun pickPicture(request_code: Int) {
        val pickPictureIntent = Intent()
        pickPictureIntent.type = "image/*"

        //pickPictureIntent.setAction(Intent.ACTION_GET_CONTENT);
        pickPictureIntent.action = Intent.ACTION_PICK
        startActivityForResult(pickPictureIntent, request_code)
    }

    fun saveDeals(
        wrapCity: TextInputLayout?,
        wrapAmount: TextInputLayout?,
        wrapDesc: TextInputLayout?,
        image: String?
    ): Unit {

        val city = wrapCity?.editText?.text.toString()
        val amount = wrapAmount?.editText?.text.toString()
        val desc = wrapDesc?.editText?.text.toString()

        var amountInt: Int? = null

        when {
            city.isEmpty() -> {
                wrapCity?.error = "Can not be empty"
                return
            }
            amount.equals("") -> {
                wrapAmount?.error = "Can not be empty"
                return
            }
            desc.isEmpty() -> {
                wrapDesc?.error = "Can not be empty"
                return
            }
            image == null -> {
                Toast.makeText(context, "Please select Image", Toast.LENGTH_SHORT).show()
                return
            }
        }

        amountInt = amount.toInt()


        upload(city, amountInt, desc, image!!)


        //Log.d(TAG, myRef?.push()?.setValue(model).toString() )

    }

    fun upload(city: String, amountInt: Int, desc: String, path: String){

        val file = Uri.fromFile(File(path))
        val riversRef = mStorageRef?.child(path)

        riversRef?.putFile(file)?.addOnSuccessListener {
            it.storage.downloadUrl.addOnCompleteListener {
                val imageurl = it.result.toString()

                val model = Model(UUID.randomUUID().toString(), city, amountInt!!, desc, imageurl)

                var firebaseUser: FirebaseUser?  = mAuth?.currentUser

                // Write a message to the database
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                myRef = database.getReference().child(""+firebaseUser?.uid)

                myRef?.push()?.setValue(model)?.addOnSuccessListener{

                    this.findNavController().navigate(R.id.action_adminFragment_to_userFragment)
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId

        if (id == R.id.save_deals) {
            saveDeals(binding?.tv2, binding?.tv1, binding?.tv3, featureImage)
            return true
        }

        return false
    }



}
