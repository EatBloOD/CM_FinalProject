package pt.uc.cm.daylistudent.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.note_edit.*
import pt.uc.cm.daylistudent.R
import pt.uc.cm.daylistudent.models.Note
import pt.uc.cm.daylistudent.utils.RetrofitUtils
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GlobalNoteActivity : AppCompatActivity() {

    companion object {
        const val INTENT_NOTE_KEY = "note"
    }

    private lateinit var note: Note

    private var isEditing: Boolean = false

    override fun onBackPressed() {
        val mIntent = Intent()
        setResult(Activity.RESULT_CANCELED, mIntent)
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtils.readPreferencesUser(applicationContext)
        setContentView(R.layout.note_edit)
        title = getString(R.string.noteEditTitle)

        val extras = intent.extras
        if (extras != null) {
            isEditing = true
            note = extras.getSerializable(INTENT_NOTE_KEY) as Note
            etTitle.text = SpannableStringBuilder(note.title)
            etBody.text = SpannableStringBuilder(note.body)
        } else
            isEditing = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sup_editnote, menu)
        return true
    }

    fun createEmailAction(item: MenuItem) {
        val email = Intent(Intent.ACTION_SEND)
        email.type = "message/rfc822"
        email.putExtra(Intent.EXTRA_SUBJECT, etTitle.text.toString())
        email.putExtra(Intent.EXTRA_TEXT, etBody.text.toString())

        try {
            startActivity(Intent.createChooser(email, getString(R.string.noteEditSelectEmailApp)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(applicationContext, R.string.noteEditNoEmailApp, Toast.LENGTH_SHORT).show()
        }
    }

    fun saveAction(item: MenuItem) {
        val retrofitUtils = RetrofitUtils()
        if (isEditing) {
            retrofitUtils.updateNote(note, getCallback(getString(R.string.UpdatingNoteError)))
        } else {
            note = Note(groupId = SharedPreferencesUtils.readSelectedGroupId().toString(), title = etTitle.text.toString(), body = etBody.text.toString(), username = SharedPreferencesUtils.readUserName())
            retrofitUtils.postNote(note, getCallback(getString(R.string.InsertingNoteError)))
        }
    }

    private fun getCallback(message: String): Callback<Int> {
        return object : Callback<Int> {
            override fun onFailure(call: Call<Int>?, t: Throwable?) {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.body() > 0) {
                    setResult(Activity.RESULT_OK, Intent())
                    finish()
                } else
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
