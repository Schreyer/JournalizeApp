package at.dev.msd.journalizeapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val auth = FirebaseAuth.getInstance()

        val user = auth.currentUser?.displayName.toString()

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

        loadDataFromServer(auth)
    }

    private fun loadDataFromServer(auth: FirebaseAuth) {
        val dataList = ArrayList<Model>()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser!!.uid
        db.collection(userId)
            .get()
            .addOnSuccessListener { result ->
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
                    val rvAdapter = RvAdapter(dataList, this)
                    recyclerView.adapter = rvAdapter

                    val loadingCircle = findViewById<ProgressBar>(R.id.loadingCircle)
                    loadingCircle.visibility = View.GONE
                }

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
            .setNegativeButton(getString(R.string.error_accured), dialogClickListener)
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
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logOut -> {
                showLogOutDialogBox()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
