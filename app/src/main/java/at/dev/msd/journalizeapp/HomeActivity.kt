package at.dev.msd.journalizeapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    var noEntries = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val auth = FirebaseAuth.getInstance()

        val user = auth.currentUser?.displayName.toString()

        if (noEntries) {
            txtAddFirstDay.visibility = View.VISIBLE
        }
        val animation = animationForButton()
        btnNewDay.startAnimation(animation)

        txtUserName.text = user

        val c = Calendar.getInstance()
        val time = c.get(Calendar.HOUR_OF_DAY)
        when {
            time < 11 -> {
                txtWelcome.text = getString(R.string.good_morning)
            }
            time < 18 -> {
                txtWelcome.text = getString(R.string.good_afternoon)
            }
            else -> {
                txtWelcome.text = getString(R.string.good_evening)
            }
        }

        btnNewDay.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(RvAdapter.ViewHolder.DOCUMENT_ID_KEY, "")
            startActivity(intent)
        }

        //recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private fun loadDataFromServer(auth: FirebaseAuth) {
        loadingCircle.visibility = View.VISIBLE
        val dataList = ArrayList<Model>()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser!!.uid
        db.collection(userId)
            .get()
            .addOnSuccessListener { result ->
                if (result === null) {
                    noEntries = true
                }
                for (document in result) {
                    val data = document.data
                    val day = data["day"].toString().padStart(2, '0')
                    val month = data["month"].toString().padStart(2, '0')
                    val year = data["year"].toString()
                    val title: String = data["title"].toString()
                    val id: String = data["id"].toString()

                    dataList.add(
                        Model(id, title, day, month, year)
                    )

                }

                val rvAdapter = RvAdapter(dataList, this)
                recyclerView.adapter = rvAdapter


                loadingCircle.visibility = View.GONE
                btnNewDay.clearAnimation()
                txtAddFirstDay.visibility = View.GONE

            }
            .addOnCompleteListener {

                loadingCircle.visibility = View.GONE
            }
    }

    private fun showLogOutDialogBox() {
        val builder = AlertDialog.Builder(this)
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        logOut()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }
        builder.setMessage(getString(R.string.log_out_prompt))
            .setPositiveButton(getString(R.string.log_out), dialogClickListener)
            .setNegativeButton(getString(R.string.cancel), dialogClickListener)
            .show()
    }

    private fun logOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                val duration = Toast.LENGTH_SHORT
                val toast =
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.logout_successful),
                        duration
                    )
                toast.show()
                finish()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        val auth = FirebaseAuth.getInstance()
        loadDataFromServer(auth)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logOut -> {
                showLogOutDialogBox()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        showLogOutDialogBox()
    }

    private fun animationForButton(): Animation {
        val animation: Animation =
            AlphaAnimation(1f, 0f) // Change alpha from fully visible to invisible

        animation.duration = 1000

        animation.interpolator = LinearInterpolator() // do not alter animation rate

        animation.repeatCount = Animation.INFINITE

        animation.repeatMode =
            Animation.REVERSE // Reverse animation at the end so the button will fade back in
        return animation
    }
}
