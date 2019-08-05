package com.efhems.travelmantics


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.efhems.travelmantics.databinding.FragmentAuthBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import androidx.annotation.NonNull

/**
 * A simple [Fragment] subclass.
 *
 */
class AuthFragment : Fragment() {

    private val TAG = AuthFragment::class.java.name
    private var mAuth: FirebaseAuth?  = FirebaseAuth.getInstance()

    val RC_SIGN_IN = 53

    var mGoogleApiClient: GoogleApiClient? = null

    var mAuthListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
         mGoogleApiClient = setupGoogleClient(gso)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val bindig: FragmentAuthBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth, container, false)


        //If the user is already sign in to firebase, go to other activity
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->

            firebaseAuth.currentUser?.let {
                updateUI(it)
            }
        }

        //setupGoogleClient(gso)

        //signup with email
        bindig.materialButton.setOnClickListener {
            this.findNavController().navigate(R.id.action_authFragment_to_signUpFragment)
        }

        //Google signin
        bindig.materialButton2.setOnClickListener {
            signIn()
        }

        return bindig.root
    }

    private fun setupGoogleClient(gso: GoogleSignInOptions) = GoogleApiClient.Builder(context!!)
            .enableAutoManage(this.activity!!
            ) {
                Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
            }
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()


    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    //START auth_with_google
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {

        Log.d(TAG, "firebaseAuthWithGoogle:" + acct?.id!!)
        //showProgressDialog()

        //Get Google api Client credentials
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this.activity!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth?.getCurrentUser()
                    user?.let {
                        updateUI(it)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)

                    updateUI(null)
                }

                //hideProgressDialog()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            //IdpResponse.fromResultIntent(data)

            if (result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account)
                Log.w(TAG, "Google sign in passes")

            } else {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed"+ result.isSuccess)
                updateUI(null)

            }
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        mAuthListener?.let {
            mAuth?.addAuthStateListener(it)
        }
        /*// Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.getCurrentUser()

        currentUser?.let {
            updateUI(currentUser)
        }*/

    }

    private fun updateUI(currentUser: FirebaseUser?) {
        val bundle = Bundle()
        bundle.putString("email", currentUser?.email)
        Log.i(TAG, "my email is " +currentUser?.email)

        currentUser?.email?.let {

            this.findNavController().navigate(R.id.userFragment, bundle)
        }

    }

    override fun onPause() {
        super.onPause()
        mGoogleApiClient?.stopAutoManage(activity!!)
        mGoogleApiClient?.disconnect()
    }

}

