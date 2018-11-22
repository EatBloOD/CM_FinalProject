package pt.uc.cm.daylistudent.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.android.synthetic.main.activity_global_notes.*
import pt.uc.cm.daylistudent.R
import pt.uc.cm.daylistudent.adapters.GlobalNotesDbAdapter
import pt.uc.cm.daylistudent.fragments.NoteEdit
import pt.uc.cm.daylistudent.fragments.ScanQrCodeFragment
import pt.uc.cm.daylistudent.interfaces.INetworkReceiveEvents
import pt.uc.cm.daylistudent.interfaces.INetworkSendEvents
import pt.uc.cm.daylistudent.interfaces.IScanQrCodeEvents
import pt.uc.cm.daylistudent.models.MessagePacket
import pt.uc.cm.daylistudent.utils.EncryptionUtils
import pt.uc.cm.daylistudent.utils.NetworkUtils
import pt.uc.cm.daylistudent.utils.QRCodeUtils
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils

class GlobalNotes : AppCompatActivity() {

    private lateinit var mGlobalNotesDbAdapter: GlobalNotesDbAdapter
    private lateinit var mNotesCursor: Cursor

    private var id: Long = 0
    private var author: String? = null
    private var ip: String? = null

    private val progressDialog: ProgressDialog? = null

    private lateinit var sharedPreferences: SharedPreferences
    private var id_noti = 0

    companion object {
        private val TAG = GlobalNotes::class.java.simpleName

        private const val ACTIVITY_CREATE = 0
        private const val ACTIVITY_EDIT = 1

        private const val DELETE_ID = Menu.FIRST + 1
        private const val SHARE_ID = Menu.FIRST + 2
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtils.readPreferencesUser(applicationContext)
        setContentView(R.layout.activity_global_notes)
        title = getString(R.string.DayliStudentActivitySharedNote)

        // Open the text notes database
        mGlobalNotesDbAdapter = GlobalNotesDbAdapter(this)
        mGlobalNotesDbAdapter.open()

        fillData()

        id = 0

        registerForContextMenu(lvGlobalNotes)
        lvGlobalNotes!!.setOnItemClickListener { _, _, position, _ ->
            mNotesCursor.moveToPosition(position)
            val i = Intent(applicationContext, NoteEdit::class.java)
            i.putExtra(GlobalNotesDbAdapter.KEY_ROWID, position + 1) // Bug from the DB.
            i.putExtra(GlobalNotesDbAdapter.KEY_AUTHOR, mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_AUTHOR)))
            i.putExtra(GlobalNotesDbAdapter.KEY_TITLE, mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_TITLE)))
            i.putExtra(GlobalNotesDbAdapter.KEY_BODY, mNotesCursor.getString(mNotesCursor.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_BODY)))
            startActivityForResult(i, ACTIVITY_EDIT)
        }
    }

    override fun onResume() {
        super.onResume()

        // TODO: check Internet and Camera permissions?
    }

    private fun fillData() {
        // Get all of the rows from the database and create the item list
        mNotesCursor = mGlobalNotesDbAdapter.fetchAllGlobalNotes()
        startManagingCursor(mNotesCursor)

        // Create an array to specify the fields we want to display in the list (only TITLE)
        val from = arrayOf(GlobalNotesDbAdapter.KEY_AUTHOR, GlobalNotesDbAdapter.KEY_TITLE, GlobalNotesDbAdapter.KEY_BODY, GlobalNotesDbAdapter.KEY_DATE)

        // and an array of the fields we want to bind those fields to (in this case just text1)
        val to = intArrayOf(R.id.tvAuthor, R.id.tvGlobalTitulo, R.id.tvGlobalBody, R.id.tvGlobalDate)

        // Now create a simple cursor adapter and set it to display
        val notes = SimpleCursorAdapter(this, R.layout.notes_global_row, mNotesCursor, from, to)
        lvGlobalNotes!!.adapter = notes
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_sup_globa, menu)
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
                mGlobalNotesDbAdapter.deleteGlobalNote(info.id)
                fillData()
                return true
            }
            SHARE_ID -> {
                senderDialog()
                id = info.id
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    fun createBudgetAction(item: MenuItem) {
        startActivityForResult(Intent(this, NoteEdit::class.java), ACTIVITY_CREATE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK) {
            val extras = intent!!.extras

            when (requestCode) {
                ACTIVITY_CREATE -> {
                    val title = extras!!.getString(GlobalNotesDbAdapter.KEY_TITLE)
                    val body = extras.getString(GlobalNotesDbAdapter.KEY_BODY)
                    mGlobalNotesDbAdapter.createGlobalNote(author, title, body)
                    fillData()
                }
                ACTIVITY_EDIT -> {
                    val mRowId = extras!!.getInt(GlobalNotesDbAdapter.KEY_ROWID)
                    val editTitle = extras.getString(GlobalNotesDbAdapter.KEY_TITLE)
                    val editBody = extras.getString(GlobalNotesDbAdapter.KEY_BODY)
                    mGlobalNotesDbAdapter.updateGlobalNote(mRowId.toLong(), author, editTitle, editBody)
                    fillData()
                }
            }
        }
    }

    private fun senderDialog() {
        val scanQrCodeFragment = ScanQrCodeFragment()
        scanQrCodeFragment.title = getString(R.string.DayliStudentActivitySendingSharedNote)
        scanQrCodeFragment.scanQrCodeEvents = object : IScanQrCodeEvents {
            override fun onDataScanned(scannedText: String) {
                val decryptedIp = EncryptionUtils.instance.getDecryptionString(scannedText)

                Toast.makeText(applicationContext, decryptedIp, Toast.LENGTH_LONG).show()

                val networkUtils = NetworkUtils.instance
                networkUtils.sendData(decryptedIp,
                        mGlobalNotesDbAdapter.getGlobalNoteToSend(id), object : INetworkSendEvents {
                    override fun onPosDataSend(messagePacket: MessagePacket, success: Boolean?) {
                        posDataSend(messagePacket, success!!)
                    }
                })
            }
        }
        scanQrCodeFragment.show(supportFragmentManager, null)
    }

    fun onReceiveNoteClick(item: MenuItem) {
        val networkUtils = NetworkUtils.instance
        networkUtils.receiveData(object : INetworkReceiveEvents {
            override fun onPreDataReceived() {
                preDataReceived()
            }

            override fun onPosDataReceived(messagePacket: MessagePacket) {
                posDataReceived(messagePacket)
            }
        })
    }

    private fun buildNotification(msg: MessagePacket) {
        val mBuilder = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(msg.author)
                .setContentText(msg.title)
        val resultIntent = Intent(applicationContext, GlobalNotes::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)

        id_noti++
        val mNotifyMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(id_noti, mBuilder.build())
    }

    private fun preDataReceived() {
        val localIp = NetworkUtils.localIpAddress
        val encryptedLocalIp = EncryptionUtils.instance.encryptionString(localIp!!)?.encryptMsg()
        val qrCodeBitmap = QRCodeUtils.newInstance(this)
                .setContent(encryptedLocalIp)
                .setErrorCorrectionLevel(ErrorCorrectionLevel.Q).setMargin(2)
                .qrcOde

        val alertDialog = layoutInflater.inflate(R.layout.receiver_alert_dialog, null)
        val imageView = alertDialog.findViewById<ImageView>(R.id.qrCode)
        imageView.setImageBitmap(qrCodeBitmap)

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.receive))
        dialog.setView(alertDialog)
        dialog.setNegativeButton(getString(R.string.cancel)) { _, _ -> NetworkUtils.instance.stopAll() }
        dialog.show()
    }

    private fun posDataReceived(messagePacket: MessagePacket?) {
        if (messagePacket != null) {
            progressDialog!!.dismiss()

            mGlobalNotesDbAdapter.createGlobalNote(messagePacket.author,
                    messagePacket.title,
                    messagePacket.obs)

            fillData()

            Toast.makeText(applicationContext,
                    getString(R.string.DayliStudentActivityReceivedSharedNote) +
                            messagePacket.title
                            + getString(R.string.DayliStudentActivityReceived2SharedNote)
                            + messagePacket.author + ".", Toast.LENGTH_LONG).show()

            buildNotification(messagePacket)
        } else {
            Toast.makeText(applicationContext, getString(R.string.error), Toast.LENGTH_LONG).show()
            // Refresh main activity upon close of dialog box
            val refresh = Intent(applicationContext, GlobalNotes::class.java)
            startActivity(refresh)
            finish()
        }
    }

    private fun posDataSend(messagePacket: MessagePacket, success: Boolean) {
        val context = applicationContext
        val toast = if (success)
            Toast.makeText(context, context.getString(R.string.SendingSharedNoteSuccess)
                    + ": " + messagePacket.title, Toast.LENGTH_LONG)
        else
            Toast.makeText(context,
                    context.getString(R.string.SendingSharedNoteFail)
                            + ": " + messagePacket.title
                            + context.getString(R.string.PleaseTryAgainLater),
                    Toast.LENGTH_LONG)
        toast.show()
    }
}
