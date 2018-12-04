package pt.uc.cm.daylistudent.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_global_notes.*
import pt.uc.cm.daylistudent.R
import pt.uc.cm.daylistudent.adapters.NotesAdapter
import pt.uc.cm.daylistudent.interfaces.INoteClick
import pt.uc.cm.daylistudent.models.Note
import pt.uc.cm.daylistudent.utils.RetrofitUtils
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils.NONE_GROUP_ID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GlobalNotes : AppCompatActivity() {

    private lateinit var retrofitUtils: RetrofitUtils
    private lateinit var notesAdapter: NotesAdapter

    private var notesList: List<Note> = mutableListOf()
    private var selectedGroupId = -1

    companion object {
        private val TAG = GlobalNotes::class.java.simpleName

        private const val ACTIVITY_CREATE = 0
        private const val ACTIVITY_EDIT = 1

        private const val DELETE_ID = Menu.FIRST + 1
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtils.readPreferencesUser(applicationContext)
        setContentView(R.layout.activity_global_notes)
        title = getString(R.string.DayliStudentActivitySharedNote)

        retrofitUtils = RetrofitUtils()

        selectedGroupId = SharedPreferencesUtils.readSelectedGroupId()
        getGroupNotes(selectedGroupId)

        rvGlobalNotes.layoutManager = LinearLayoutManager(this)
        notesAdapter = NotesAdapter(this, notesList, object : INoteClick {
            override fun onDeleteNoteClick(note: Note) {
                deleteGroupNote(note.id)
                refreshListData()
            }

            override fun onNoteClick(note: Note) {
                val editNoteIntent = Intent(applicationContext, GlobalNoteActivity::class.java)
                editNoteIntent.putExtra(GlobalNoteActivity.INTENT_NOTE_KEY, note)
                startActivityForResult(editNoteIntent, ACTIVITY_EDIT)
            }
        })
        rvGlobalNotes.adapter = notesAdapter
    }

    private fun refreshListData() {
        notesAdapter.setNotes(this.notesList)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_sup_globa, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun onExitGroupAction(item: MenuItem) {
        Toast.makeText(applicationContext, getString(R.string.ExitGroup), Toast.LENGTH_LONG).show()
        SharedPreferencesUtils.writeSelectedGroupId(applicationContext, NONE_GROUP_ID)
        finish()
    }

    fun onRefreshGlobalAction(item: MenuItem) {
        getGroupNotes(selectedGroupId)
        refreshListData()
    }

    fun createBudgetAction(item: MenuItem) {
        startActivityForResult(Intent(this, GlobalNoteActivity::class.java), ACTIVITY_CREATE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ACTIVITY_CREATE, ACTIVITY_EDIT -> {
                    getGroupNotes(selectedGroupId)
                    refreshListData()
                }
            }
        }
    }

    private fun getGroupNotes(selectedGroupId: Int) {
        retrofitUtils.getGroupNotes(selectedGroupId, object : Callback<List<Note>> {
            override fun onFailure(call: Call<List<Note>>, t: Throwable) {
                Toast.makeText(applicationContext, getString(R.string.GetNotesError), Toast.LENGTH_LONG).show()
                tvEmpty!!.visibility = View.VISIBLE
            }

            override fun onResponse(call: Call<List<Note>>, response: Response<List<Note>>) {
                tvEmpty!!.visibility = View.GONE
                notesList = response.body()
                refreshListData()
            }
        })
    }

    private fun deleteGroupNote(noteId: String) {
        retrofitUtils.deleteGroupNote(noteId, object : Callback<Int> {
            override fun onFailure(call: Call<Int>, t: Throwable) {
                Toast.makeText(applicationContext, "Cannot delete note! Please try again later.", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.body() == 0)
                    Toast.makeText(applicationContext, "Cannot delete note! Please try again later.", Toast.LENGTH_LONG).show()
                else {
                    getGroupNotes(selectedGroupId)
                }
            }
        })
    }
}
