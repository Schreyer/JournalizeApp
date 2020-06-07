package at.dev.msd.journalizeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val RC_SIGN_IN = 15
    }

    private lateinit var storage: FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            intentHome()
        }

        loadLogo()
        btnSignIn.setOnClickListener {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build()
            )

            // Create and launch sign-in intent
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN
            )
        }

    }

    private fun loadLogo() {
        storage = FirebaseStorage.getInstance()
        val logoView = findViewById<ImageView>(R.id.logoView)

        val storageRef = storage.reference
        val logoRef = storageRef.child("journalize_icon_transparent.png")

        Glide.with(this)
            .load(logoRef)
            .into(logoView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                //Successfully signed in
//                val user = FirebaseAuth.getInstance().currentUser
                intentHome()
            } else {
                val duration = Toast.LENGTH_SHORT
                val toast =
                    Toast.makeText(applicationContext, getString(R.string.error_accured), duration)
                toast.show()
            }
        }
    }

    private fun intentHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}
