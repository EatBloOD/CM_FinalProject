package pt.uc.cm.daylistudent.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
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

class GlobalNotesActivity : AppCompatActivity() {

    private lateinit var retrofitUtils: RetrofitUtils
    private lateinit var notesAdapter: NotesAdapter

    private var notesList: List<Note> = mutableListOf()
    private var selectedGroupId = -1

    companion object {
        private val TAG = GlobalNotesActivity::class.java.simpleName

        private const val ACTIVITY_CREATE = 0
        private const val ACTIVITY_EDIT = 1
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtils.readPreferencesUser(applicationContext)
        setContentView(R.layout.activity_global_notes)
        title = getString(R.string.DayliStudentActivitySharedNote)

        retrofitUtils = RetrofitUtils()
    }

    override fun onResume() {
        super.onResume()
        selectedGroupId = SharedPreferencesUtils.readSelectedGroupId()
        getGroupNotes(selectedGroupId)

        rvGlobalNotes.layoutManager = LinearLayoutManager(this)
        notesAdapter = NotesAdapter(this, notesList, object : INoteClick {
            override fun onDeleteNoteClick(note: Note) {
                deleteGroupNote(note.id)
                refreshListData()
            }

            override fun onNoteClick(note: Note) {
                val editNoteIntent = Intent(applicationContext, ManageGlobalNoteActivity::class.java)
                editNoteIntent.putExtra(ManageGlobalNoteActivity.INTENT_NOTE_KEY, note)
                startActivityForResult(editNoteIntent, ACTIVITY_EDIT)
            }
        })
        rvGlobalNotes.adapter = notesAdapter
    }

    private fun refreshListData() {
        notesAdapter.setNotes(this.notesList)
        if (this.notesList.isEmpty()) {
            rvGlobalNotes.visibility = View.GONE;
            empty!!.visibility = View.VISIBLE;
        } else {
            rvGlobalNotes.visibility = View.VISIBLE;
            empty!!.visibility = View.GONE;
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_global_notes, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun onExitGroupAction(item: MenuItem) {
        val groupId = SharedPreferencesUtils.readSelectedGroupId()
        retrofitUtils.deleteGroup(groupId, object : Callback<Int> {
            override fun onFailure(call: Call<Int>, t: Throwable) {
                Toast.makeText(applicationContext, "Error exiting group!", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                Log.d(TAG, "deleted groups: ${response.body()}")
                Toast.makeText(applicationContext, "Success exiting group!", Toast.LENGTH_LONG).show()
            }
        })
        Toast.makeText(applicationContext, getString(R.string.ExitGroup), Toast.LENGTH_LONG).show()
        SharedPreferencesUtils.writeSelectedGroupId(applicationContext, NONE_GROUP_ID)
        finish()
    }

    fun onRefreshGlobalAction(item: MenuItem) {
        getGroupNotes(selectedGroupId)
        refreshListData()
    }

    fun createBudgetAction(item: MenuItem) {
        startActivityForResult(Intent(this, ManageGlobalNoteActivity::class.java), ACTIVITY_CREATE)
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
            }

            override fun onResponse(call: Call<List<Note>>, response: Response<List<Note>>) {
                notesList = response.body()
                refreshListData()
            }
        })
    }

    private fun deleteGroupNote(noteId: String) {
        retrofitUtils.deleteGroupNote(noteId, object : Callback<Int> {
            override fun onFailure(call: Call<Int>, t: Throwable) {
                Toast.makeText(applicationContext, getString(R.string.ErrorDeletingNote), Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                getGroupNotes(selectedGroupId)
            }
        })
    }
}
