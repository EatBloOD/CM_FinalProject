package pt.uc.cm.daylistudent.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.notes_global_row.view.*
import pt.uc.cm.daylistudent.R
import pt.uc.cm.daylistudent.interfaces.INoteClick
import pt.uc.cm.daylistudent.models.Note

class NotesAdapter(val context: Context,
                   var notesList: List<Note>,
                   val onItemClickListener: INoteClick)
    : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.notes_global_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notesList[position], onItemClickListener)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    fun setNotes(notesList: List<Note>) {
        this.notesList = notesList
        this.notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(note: Note, onClickListener: INoteClick) {
            itemView.tvGlobalTitle.text = note.title
            itemView.tvAuthor.text = note.username
            itemView.tvGlobalBody.text = note.body
            itemView.ivDeleteNote!!.setOnClickListener { onClickListener.onDeleteNoteClick(note) }
            itemView.setOnClickListener { onClickListener.onNoteClick(note) }
        }
    }
}

