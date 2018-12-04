package pt.uc.cm.daylistudent.interfaces;

import java.util.List;

import pt.uc.cm.daylistudent.models.Group;
import pt.uc.cm.daylistudent.models.Note;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface INotesApiService {

    @GET("groups")
    Call<List<Group>> getGroups();

    @GET("group/{groupID}")
    Call<List<Group>> getGroup(@Path("groupID") int groupId);

    @POST("group")
    Call<List<Group>> postGroup(@Query("group_name") String groupName);

    @DELETE("group/{groupID}")
    Call<Integer> deleteGroup(@Path("groupID") int groupId);

    @GET("notes/{groupID}")
    Call<List<Note>> getNotes(@Path("groupID") int groupId);

    @POST("note")
    Call<Integer> postNote(@Body String noteJSONData);

    @POST("note/{noteID}")
    Call<Integer> updateNote(@Path("noteID") int noteId, @Body String noteJSONData);

    @DELETE("note/{noteID}")
    Call<Integer> deleteNote(@Path("noteID") int noteId);
}
