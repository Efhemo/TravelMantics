package com.efhems.travelmantics


import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.efhems.travelmantics.databinding.FragmentSignUpBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


/**
 * A simple [Fragment] subclass.
 *
 */
class SignUpFragment : Fragment() {

    private var mAuth: FirebaseAuth?  = FirebaseAuth.getInstance()
    private val TAG = SignUpFragment::class.java.name
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSignUpBinding.inflate(inflater)

        Log.i(TAG, "reach signup fragment")

        binding.saveBtn.setOnClickListener {

            validateUser(binding.tv2, binding.tv3)
        }
        return binding.root
    }

    private fun validateUser(emailWrap: TextInputLayout, passwordWrap: TextInputLayout) {

        val email = emailWrap.editText?.text.toString()
        val password = passwordWrap.editText?.text.toString()
        if (email.isEmpty()) {
            emailWrap.error = "Not a valid email"
            return
        } else {
            emailWrap.error = ""
        }

        if (password.length < 6) {
            passwordWrap.error = "Should be more than 5 characters"
            return
        } else {
            passwordWrap.error = ""
        }

        signUpUser(email, password)
    }

    //extension function
    private fun String.isValidEmail(): Boolean = this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun signUpUser(email: String, password: String): Unit {

        mAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth?.getCurrentUser()
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this.context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }
    /*override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.getCurrentUser()
        updateUI(currentUser)
    }*/

    private fun updateUI(currentUser: FirebaseUser?) {

        currentUser?.email?.let {
            val bundle = Bundle()
            bundle.putString("email", currentUser?.email)
            this.findNavController().navigate(R.id.userFragment, bundle)
        }
    }


}
