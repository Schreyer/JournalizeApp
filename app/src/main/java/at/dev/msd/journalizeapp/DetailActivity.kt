package at.dev.msd.journalizeapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
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

        if (documentId == "") {
            // creating new day
            setDate(day, month, year)
            txtTitle.requestFocus()
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(txtTitle, InputMethodManager.SHOW_IMPLICIT)
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
                },
                year.toInt(),
                month.toInt() - 1,
                day.toInt()
            )
            dpd.show()
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

            val newDay: HashMap<String, Serializable> = hashMapOf(
                "id" to documentId,
                "title" to title.toString(),
                "paragraph" to paragraph.toString(),
                "day" to day,
                "month" to month,
                "year" to year
            )

            db.collection(userId).document(documentId)
                .set(newDay)
                .addOnSuccessListener {
                    showToast(getString(R.string.save_successful))
                    finish()
                }.addOnFailureListener {
                    showToast(getString(R.string.error_accured))
                }
        }
    }

    private fun setDate(day: String, month: String, year: String) {
        (this as AppCompatActivity).supportActionBar?.title = ("$day.$month.$year")
    }

    private fun deleteDay() {
        db.collection(userId).document(documentId)
            .delete()
            .addOnSuccessListener {
                showToast(getString(R.string.delete_successful))
                finish()
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

    private fun showToast(message: String) {
        val duration = Toast.LENGTH_SHORT
        val toast =
            Toast.makeText(
                applicationContext,
                message,
                duration
            )
        toast.show()
    }
}