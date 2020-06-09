package at.dev.msd.journalizeapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_edit_day.*
import java.io.Serializable
import java.util.*

class DetailActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()
    private var userId = auth.currentUser!!.uid
    private lateinit var documentId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_day)

        var paragraph: CharSequence
        var title: CharSequence

        //initial current date
        val c = Calendar.getInstance()
        var day = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        var month = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        var year = c.get(Calendar.YEAR).toString()

        var oldDay = ""
        var oldMonth = ""
        var oldYear = ""
        var oldGuid = ""

        documentId = intent.getStringExtra(RvAdapter.ViewHolder.DOCUMENT_ID_KEY)!!
//        auth = FirebaseAuth.getInstance()

        //get documentId
//        var documentId = intent.getStringExtra(RvAdapter.ViewHolder.DOCUMENT_ID_KEY)

        if (documentId == "") {
            // creating new day
//            println("--------------month: $month")
            setDate(day, month, year)

        } else {
            // editing existing day
            userId = auth.currentUser!!.uid
            db.collection(userId)
                .document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    val data = document.data!!
                    day = data["day"].toString()
                    oldDay = day

                    month = data["month"].toString()
                    oldMonth = month

                    year = data["year"].toString()
                    oldYear = year

                    title = data["title"].toString()
                    paragraph = data["paragraph"].toString()

                    oldGuid = data["id"].toString()

                    txtTitle.setText(title)
                    txtParagraph.setText(paragraph)

                    setDate(day, month, year)
                }
        }

        btnDatePicker.setOnClickListener {
            val dpd: DatePickerDialog
            dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, yearOfYear, monthOfYear, dayOfMonth ->
                    day = dayOfMonth.toString().padStart(2, '0')
                    month = (monthOfYear + 1).toString().padStart(2, '0')
                    year = yearOfYear.toString()
                    setDate(day, month, year)
//                    btnSave.visibility = View.VISIBLE
                },
                year.toInt(),
                month.toInt() - 1,
                day.toInt()
            )
            dpd.show()
//            btnSave.visibility = View.GONE
        }
        btnSave.setOnClickListener {
            title = findViewById<TextView>(R.id.txtTitle).text
            paragraph = findViewById<TextView>(R.id.txtParagraph).text

            //check if date has changed
            documentId = if (day == oldDay && month == oldMonth && year == oldYear) {
                oldGuid
            } else {
                // combination of date and UUID for multiple entries on one day and for future use (count entries on same day with help of the date)
                year + month + day + "_" + UUID.randomUUID()
            }

            val userId = auth.currentUser!!.uid

//            val db = FirebaseFirestore.getInstance()
            val duration = Toast.LENGTH_SHORT
            val newDay: HashMap<String, Serializable> = hashMapOf(
                "id" to documentId,
                "title" to title.toString(),
                "paragraph" to paragraph.toString(),
                "day" to day,
                "month" to month,
                "year" to year
            )
//            val date = hashMapOf(
//                "day" to day,
//                "month" to month,
//                "year" to year
//            )
//
//            user["date"] = date
            db.collection(userId).document(documentId)
                .set(newDay)
                .addOnSuccessListener {
                    val toast =
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.save_successful),
                            duration
                        )
                    toast.show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                }.addOnFailureListener {
                    val toast =
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.error_accured),
                            duration
                        )
                    toast.show()
                }
        }
    }

    private fun setDate(day: String, month: String, year: String) {
//        txtDate.text = ("$day.$month.$year")
        (this as AppCompatActivity).supportActionBar?.title = ("$day.$month.$year")
    }

    private fun deleteDay() {
        db.collection(userId).document(documentId)
            .delete()
            .addOnSuccessListener {
                val duration = Toast.LENGTH_SHORT
                val toast =
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.delete_successful),
                        duration
                    )
                toast.show()

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
    }

    private fun showDeleteDialogBox() {
        val builder = AlertDialog.Builder(this)
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        deleteDay()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }
        builder.setMessage(getString(R.string.delete_day_prompt))
            .setPositiveButton(getString(R.string.delete), dialogClickListener)
            .setNegativeButton(getString(R.string.cancel), dialogClickListener)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.detail_day_manu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                showDeleteDialogBox()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}