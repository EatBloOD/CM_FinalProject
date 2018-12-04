package pt.uc.cm.daylistudent.models

import java.io.Serializable

class Note(var id: String = "-1",
           var groupId: String = "-1",
           var username: String? = "",
           var title: String,
           var body: String) : Serializable {
    companion object {
        private const val serialVersionUID = 7526471155622776147L
    }
}


