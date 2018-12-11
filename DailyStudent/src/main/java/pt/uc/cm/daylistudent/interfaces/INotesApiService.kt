package pt.uc.cm.daylistudent.interfaces

import pt.uc.cm.daylistudent.models.Group
import pt.uc.cm.daylistudent.models.Note
import retrofit2.Call
import retrofit2.http.*

interface INotesApiService {

    @get:GET("groups")
    val groups: Call<List<Group>>

    @GET("group/{groupID}")
    fun getGroup(@Path("groupID") groupId: Int): Call<List<Group>>

    @POST("group")
    fun postGroup(@Query("group_name") groupName: String): Call<Int>

    @DELETE("group/{groupID}")
    fun deleteGroup(@Path("groupID") groupId: Int): Call<Int>

    @GET("notes/{groupID}")
    fun getNotes(@Path("groupID") groupId: Int): Call<List<Note>>

    @POST("note")
    fun postNote(@Body noteJSONData: String): Call<Int>

    @POST("note/{noteID}")
    fun updateNote(@Path("noteID") noteId: Int, @Body noteJSONData: String): Call<Int>

    @DELETE("note/{noteID}")
    fun deleteNote(@Path("noteID") noteId: Int): Call<Int>
}
