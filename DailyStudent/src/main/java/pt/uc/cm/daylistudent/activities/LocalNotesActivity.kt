package pt.uc.cm.daylistudent.activities

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.android.synthetic.main.notes_list.*
import pt.uc.cm.daylistudent.R
import pt.uc.cm.daylistudent.adapters.BudgetDbAdapter
import pt.uc.cm.daylistudent.adapters.NotesDbAdapter
import pt.uc.cm.daylistudent.fragments.ScanQrCodeFragment
import pt.uc.cm.daylistudent.interfaces.IScanQrCodeEvents
import pt.uc.cm.daylistudent.models.Note
import pt.uc.cm.daylistudent.utils.EncryptionUtils
import pt.uc.cm.daylistudent.utils.QRCodeUtils
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils

class LocalNotesActivity : AppCompatActivity() {

    companion object {
        private val TAG = LocalNotesActivity::class.java.simpleName

        private val ACTIVITY_CREATE = 0
        private val ACTIVITY_EDIT = 1
        private val REQUEST_CAMERA_PERMISSION = 1

        private val DELETE_ID = Menu.FIRST + 1
        private val SHARE_ID = Menu.FIRST + 2

        private var notificationCounter: Int = 0

        private val NOTIFICATION_CHANNEL_ID = "10001"

        private var mNotificationManager: NotificationManager? = null
        private var mBuilder: NotificationCompat.Builder? = null
    }

    private var mDbHelper: NotesDbAdapter? = null
    private var mDbBudgetHelper: BudgetDbAdapter? = null
    private var mNotesCursor: Cursor? = null

    //permission is automatically granted on sdk<23 upon installation
    private val isReadStoragePermissionGranted: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission is granted1")
                    return true
                } else {

                    Log.v(TAG, "Permission is revoked1")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 3)
                    return false
                }
            } else {
                Log.v(TAG, "Permission is granted1")
                return true
            }
        }

    //permission is automatically granted on sdk<23 upon installation
    private val isWriteStoragePermissionGranted: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission is granted2")
                    return true
                } else {
                    Log.v(TAG, "Permission is revoked2")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
                    return false
                }
            } else {
                Log.v(TAG, "Permission is granted2")
                return true
            }
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtils.readPreferencesUser(applicationContext)
        setContentView(R.layout.notes_list)

        title = getString(R.string.dayliStudentActivityTitle)

        mDbHelper = NotesDbAdapter(this)
        mDbHelper!!.open()

        mDbBudgetHelper = BudgetDbAdapter(this)
        mDbBudgetHelper!!.open()

        fillData()

        registerForContextMenu(lvNotes)

        lvNotes.setOnItemClickListener { _, _, position, _ ->
            val c = mNotesCursor
            c!!.moveToPosition(position)
            val i = Intent(applicationContext, ManageNoteActivity::class.java)
            i.putExtra(NotesDbAdapter.KEY_ROWID, position)
            i.putExtra(NotesDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)))
            i.putExtra(NotesDbAdapter.KEY_BODY, c.getString(c.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)))
            startActivityForResult(i, ACTIVITY_EDIT)
        }

        isReadStoragePermissionGranted
        isWriteStoragePermissionGranted
    }

    private fun fillData() {
        mNotesCursor = mDbHelper!!.fetchAllNotes()
        startManagingCursor(mNotesCursor)

        val from = arrayOf(NotesDbAdapter.KEY_TITLE, NotesDbAdapter.KEY_BODY, NotesDbAdapter.KEY_DATE)

        val to = intArrayOf(R.id.tvTitulo, R.id.tvBody, R.id.tvDate)

        val notes = SimpleCursorAdapter(this, R.layout.notes_row, mNotesCursor, from, to)
        lvNotes.adapter = notes
        lvNotes.emptyView = empty
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_sup_notes, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)

        menu.add(0, DELETE_ID, 0, R.string.DayliStudentActivityDeleteNote)
        menu.add(0, SHARE_ID, 0, R.string.DayliStudentActivityShareNote)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        when (item.itemId) {
            DELETE_ID -> {
                mDbHelper!!.deleteNote(info.id)
                fillData()
                return true
            }
            SHARE_ID -> {
                val c = mNotesCursor
                c!!.moveToPosition(info.position)
                val title = c.getString(c.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE))
                val body = c.getString(c.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY))
                showSendNoteDialog(Note(title = title, body = body))
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    fun createBudgetAction(item: MenuItem) {
        val i = Intent(this, ManageNoteActivity::class.java)
        startActivityForResult(i, ACTIVITY_CREATE)
    }

    fun onReceiveNoteAction(item: MenuItem) {
        showReceiverNoteDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK) {
            val extras = intent!!.extras

            when (requestCode) {
                ACTIVITY_CREATE -> {
                    val title = extras!!.getString(NotesDbAdapter.KEY_TITLE)
                    val body = extras.getString(NotesDbAdapter.KEY_BODY)
                    mDbHelper!!.createNote(title, body)
                    fillData()
                }
                ACTIVITY_EDIT -> {
                    val mRowId = extras!!.getInt(NotesDbAdapter.KEY_ROWID)
                    if (mRowId != null) {
                        val editTitle = extras.getString(NotesDbAdapter.KEY_TITLE)
                        val editBody = extras.getString(NotesDbAdapter.KEY_BODY)
                        mDbHelper!!.updateNote((mRowId + 1).toLong(), editTitle, editBody)
                    }
                    fillData()
                }
            }
        }
    }

    private fun showReceiverNoteDialog() {
        val scanQrCodeFragment = ScanQrCodeFragment()
        scanQrCodeFragment.title = getString(R.string.DayliStudentActivitySendingSharedNote)
        scanQrCodeFragment.scanQrCodeEvents = object : IScanQrCodeEvents {
            override fun onError() {
                val snackbar  = Snackbar.make(window.decorView, getString(R.string.error), Snackbar.LENGTH_LONG)
                val snackbarTextView = snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
                snackbar.view.setBackgroundColor(Color.RED)
                snackbarTextView.setTextColor(Color.WHITE)
                snackbarTextView.setTypeface(snackbarTextView.typeface, Typeface.BOLD)
                snackbar.show()
            }

            override fun onDataScanned(scannedText: String) {
                val decryptedObjectData = EncryptionUtils.instance.getDecryptionString(scannedText)
                val note = Gson().fromJson(decryptedObjectData, Note::class.java)
                mDbHelper!!.createNote(note.title, note.body)
                fillData()

                val snackbar  = Snackbar.make(window.decorView, "${getString(R.string.DayliStudentActivityReceivedSharedNote)} ${note.title}"
                        + "${getString(R.string.DayliStudentActivityReceived2SharedNote)} ${note.username}.", Snackbar.LENGTH_LONG)
                val snackbarTextView = snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
                snackbar.view.setBackgroundColor(Color.GREEN)
                snackbarTextView.setTextColor(Color.BLACK)
                snackbarTextView.setTypeface(snackbarTextView.typeface, Typeface.BOLD)
                snackbar.show()

//                Toast.makeText(applicationContext,"${getString(R.string.DayliStudentActivityReceivedSharedNote)} ${note.title}"+ "${getString(R.string.DayliStudentActivityReceived2SharedNote)} ${note.username}.", Toast.LENGTH_LONG).show()

                buildNotification(note)
            }
        }
        scanQrCodeFragment.show(supportFragmentManager, null)
    }

    private fun showSendNoteDialog(note: Note) {
        val objectData = Gson().toJson(note)
        val encryptedObjectData = EncryptionUtils.Companion.instance.encryptionString(objectData)?.encryptMsg()
        val qrCodeBitmap = QRCodeUtils.newInstance(this)
                .setContent(encryptedObjectData)
                .setErrorCorrectionLevel(ErrorCorrectionLevel.Q).setMargin(2)
                .qrcOde

        val alertDialog = layoutInflater.inflate(R.layout.receiver_alert_dialog, null)
        val imageView = alertDialog.findViewById<ImageView>(R.id.qrCode)
        imageView.setImageBitmap(qrCodeBitmap)

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.ScanThisWithDayliStudentApp))
        dialog.setView(alertDialog)
        dialog.show()
    }

    private fun buildNotification(note: Note) {
        mBuilder = NotificationCompat.Builder(applicationContext)
        mBuilder!!.setSmallIcon(R.drawable.ic_account_balance_wallet_black_24dp)
        mBuilder!!.setContentTitle(note.title)
                .setContentText(note.body)
                .setAutoCancel(true)

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "NOTIFICATION_CHANNEL_NAME", importance)
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            mBuilder!!.setChannelId(NOTIFICATION_CHANNEL_ID)
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        }
        mNotificationManager!!.notify(0 /* Request Code */, mBuilder!!.build())
    }
}
