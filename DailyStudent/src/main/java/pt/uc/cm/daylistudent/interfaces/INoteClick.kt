package pt.uc.cm.daylistudent.interfaces

import pt.uc.cm.daylistudent.models.Note

interface INoteClick {
    fun onNoteClick(note: Note)
    fun onDeleteNoteClick(note: Note)
}