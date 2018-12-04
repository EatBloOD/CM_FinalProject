package pt.uc.cm.daylistudent.models

import java.io.Serializable
import java.util.*

class MessagePacket(val author: String, val title: String, val obs: String) : Serializable {
    private val parameters: List<String>

    init {
        parameters = ArrayList()
    }

    companion object {
        private const val serialVersionUID = 1010L
    }
}
