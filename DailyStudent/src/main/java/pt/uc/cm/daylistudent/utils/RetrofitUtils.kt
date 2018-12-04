package pt.uc.cm.daylistudent.utils

import com.google.gson.Gson
import okhttp3.OkHttpClient
import pt.uc.cm.daylistudent.interfaces.INotesApiService
import pt.uc.cm.daylistudent.models.Group
import pt.uc.cm.daylistudent.models.Note
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitUtils {
    companion object {
        val STATUS_CODE_OK = 200
    }

    private val API_BASE_URL: String = "http://35.243.199.87:8080"

    private var client: INotesApiService

    private val okHttpClient = OkHttpClient()

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        client = retrofit.create(INotesApiService::class.java)
    }

    fun getGroups(callback: Callback<List<Group>>) {
        val groupsCallBack = client.groups
        groupsCallBack.enqueue(callback)
    }

    fun getGroupNotes(selectedGroupId: Int, callback: Callback<List<Note>>) {
        val notesCallBack = client.getNotes(selectedGroupId)
        notesCallBack.enqueue(callback)
    }

    fun postNote(note: Note, callback: Callback<Int>) {
        val postNoteCallback = client.postNote(Gson().toJson(note))
        postNoteCallback.enqueue(callback)
    }

    fun updateNote(note: Note, callback: Callback<Int>) {
        val updateNoteCallback = client.updateNote(note.id.toInt(), Gson().toJson(note))
        updateNoteCallback.enqueue(callback)
    }

    fun deleteGroupNote(noteId: String, callback: Callback<Int>) {
        val notesCallback = client.deleteNote(noteId.toInt())
        notesCallback.enqueue(callback)
    }
}
