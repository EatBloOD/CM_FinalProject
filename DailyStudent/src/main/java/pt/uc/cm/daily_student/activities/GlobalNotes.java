package pt.uc.cm.daily_student.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import pt.uc.cm.daily_student.R;
import pt.uc.cm.daily_student.adapters.GlobalNotesDbAdapter;
import pt.uc.cm.daily_student.fragments.NoteEdit;
import pt.uc.cm.daily_student.interfaces.INetworkReceiveEvents;
import pt.uc.cm.daily_student.models.MessagePacket;
import pt.uc.cm.daily_student.utils.NetworkUtils;

public class GlobalNotes extends StructureActivity {
    private final String TAG = GlobalNotes.class.getSimpleName();

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;

    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int SHARE_ID = Menu.FIRST + 2;

    private GlobalNotesDbAdapter mGlobalNotesDbAdapter;
    private Cursor mNotesCursor;

    private ListView lvGlobalNotes;
    private long id;
    private String author = null, ip = null;

    private ProgressDialog progressDialog = null;

    int id_noti = 0;

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.activity_global_notes);
        setTitle(getString(R.string.DayliStudentActivitySharedNote));

        //abrir a base de dados das notas textuais
        mGlobalNotesDbAdapter = new GlobalNotesDbAdapter(this);
        mGlobalNotesDbAdapter.open();

        fillData();

        id = 0;

        lvGlobalNotes = findViewById(R.id.lv_global_notes);
        registerForContextMenu(lvGlobalNotes);

        lvGlobalNotes.setOnItemClickListener((adapterView, view, position, id) -> {
            Cursor c = mNotesCursor;
            c.moveToPosition(position);
            Intent i = new Intent(getApplicationContext(), NoteEdit.class);
            i.putExtra(GlobalNotesDbAdapter.KEY_ROWID, id);
            i.putExtra(GlobalNotesDbAdapter.KEY_AUTHOR, c.getString(c.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_AUTHOR)));
            i.putExtra(GlobalNotesDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_TITLE)));
            i.putExtra(GlobalNotesDbAdapter.KEY_BODY, c.getString(c.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_BODY)));
            startActivityForResult(i, ACTIVITY_EDIT);
        });
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
        mNotesCursor = mGlobalNotesDbAdapter.fetchAllGlobalNotes();
        startManagingCursor(mNotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{GlobalNotesDbAdapter.KEY_AUTHOR, GlobalNotesDbAdapter.KEY_TITLE, GlobalNotesDbAdapter.KEY_BODY, GlobalNotesDbAdapter.KEY_DATE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.tvAuthor, R.id.tvGlobalTitulo, R.id.tvGlobalBody, R.id.tvGlobalDate};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_global_row, mNotesCursor, from, to);
        lvGlobalNotes = findViewById(R.id.lv_global_notes);
        lvGlobalNotes.setAdapter(notes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sup_globa, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, DELETE_ID, 0, R.string.DayliStudentActivityDeleteNote);
        menu.add(0, SHARE_ID, 0, R.string.DayliStudentActivityShareNote);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case DELETE_ID:
                mGlobalNotesDbAdapter.deleteGlobalNote(info.id);
                fillData();
                return true;
            case SHARE_ID:
                senderDialog();
                id = info.id;
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void createBudgetAction(MenuItem item) {
        startActivityForResult(new Intent(this, NoteEdit.class), ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();

            switch (requestCode) {
                case ACTIVITY_CREATE:
                    String title = extras.getString(GlobalNotesDbAdapter.KEY_TITLE);
                    String body = extras.getString(GlobalNotesDbAdapter.KEY_BODY);
                    mGlobalNotesDbAdapter.createGlobalNote(author, title, body);
                    fillData();
                    break;
                case ACTIVITY_EDIT:
                    Integer mRowId = extras.getInt(GlobalNotesDbAdapter.KEY_ROWID);
                    if (mRowId != null) {
                        String editTitle = extras.getString(GlobalNotesDbAdapter.KEY_TITLE);
                        String editBody = extras.getString(GlobalNotesDbAdapter.KEY_BODY);
                        mGlobalNotesDbAdapter.updateGlobalNote(mRowId, author, editTitle, editBody);
                    }
                    fillData();
                    break;
                default:
            }
        }
    }

    private void senderDialog() {
        final EditText edtIP = new EditText(this);

        // TODO: implement get IP from QRCode
        edtIP.setText(ip);
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(R.string.DayliStudentActivitySendingSharedNote)
                .setMessage(R.string.DayliStudentActivityIPSendingSharedNote)
                .setView(edtIP)
                .setPositiveButton(R.string.DayliStudentActivityButtonSendingSharedNote, (dialogInterface, i) -> {
                    // TODO: check for permissions Internet
                    NetworkUtils networkUtils = NetworkUtils.getInstance();
                    networkUtils.sendData(mGlobalNotesDbAdapter.getGlobalNoteToSend(this.id),
                            this::posDataSend);
                })
                .setOnCancelListener(dialogInterface -> {
                })
                .create();
        ad.show();
    }

    public void onReceiveNoteClick(MenuItem item) {
        NetworkUtils networkUtils = NetworkUtils.getInstance();
        networkUtils.receiveData(new INetworkReceiveEvents() {
            @Override
            public void onPreDataReceived() {
                preDataReceived();
            }

            @Override
            public void onPosDataReceived(MessagePacket messagePacket) {
                posDataReceived(messagePacket);
            }
        });
    }

    public void receiveNote(MenuItem item) {
        String ip = getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.waitingConection) + "\n(IP: " + ip
                + ")");
        pd.setTitle(getString(R.string.receive));
        pd.setOnCancelListener(dialog -> {
            if (receiverSocket != null) {
                try {
                    receiverSocket.close();
                } catch (IOException ignored) {
                }
                receiverSocket = null;
            }
        });
        pd.show();

        Thread t = new Thread(new waitConnection());
        t.start();
    }

    //TODO: CONVERT TO ASYNC
    public class waitConnection implements Runnable {
        @Override
        public void run() {
            try {
                receiverSocket = new ServerSocket(9001);
                senderSocket = receiverSocket.accept();
                receiverSocket.close();
                receiverSocket = null;
                Thread t = new Thread(new receiveMSG());
                t.start();
            } catch (Exception e) {
                e.printStackTrace();
                senderSocket = null;
            }
            procMsg.post(() -> {
                pd.dismiss();
                if (receiverSocket != null)
                    try {
                        receiverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            });
        }
    }

    //TODO: CONVERT TO ASYNC(2)
    public class receiveMSG implements Runnable {

        public void run() {
            try {
                inObj = new ObjectInputStream(senderSocket.getInputStream());

                final MessagePacket read = (MessagePacket) (inObj.readObject());

                procMsg.post(() -> {
                    mDbHelper.createGlobalNote(read.getAuthor(), read.getTitle(), read.getObs());

                    fillData();

                    Toast.makeText(getApplicationContext(), getString(R.string.DayliStudentActivityReceivedSharedNote) +
                            read.getTitle() + getString(R.string.DayliStudentActivityReceived2SharedNote) + read.getAuthor() + ".", Toast.LENGTH_LONG).show();

                    buildNotification(read);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                procMsg.post(() -> {
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                    // Refresh main activity upon close of dialog box
                    Intent refresh = new Intent(getApplicationContext(), GlobalNotes.class);
                    startActivity(refresh);
                    finish();
                });
            }
        }
    }

    public class sendMSG implements Runnable {
        MessagePacket msg;

        public void run() {
            try {
                outObj = new ObjectOutputStream(senderSocket.getOutputStream());

                msg = mDbHelper.getGlobalNoteToSend(id);

                id = 0;

                outObj.writeObject(msg);
                outObj.flush();

                procMsg.post(() -> Toast.makeText(getApplicationContext(),
                        getString(R.string.DayliStudentActivitySuccessfullSendingSharedNote) + msg.getTitle() + " ]", Toast.LENGTH_LONG).show());
            } catch (Exception ex) {
                ex.printStackTrace();
                procMsg.post(() -> {
                });
            }
        }
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void buildNotification(MessagePacket msg) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msg.getAuthor())
                        .setContentText(msg.getTitle());
        Intent resultIntent = new Intent(getApplicationContext(), GlobalNotes.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        id_noti++;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(id_noti, mBuilder.build());
    }

    private void preDataReceived() {
        // TODO: implement show IP from QRCode
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.waitingConection) + "\n(IP: " + ip + ")");
        progressDialog.setTitle(getString(R.string.receive));
        /* TODO: remove this if cancel works!
        progressDialog.setOnCancelListener(dialog -> {
            if (receiverSocket != null) {
                try {
                    receiverSocket.close();
                } catch (IOException ignored) {
                }
                receiverSocket = null;
            }
        });*/
        progressDialog.show();
    }

    private void posDataReceived(MessagePacket messagePacket) {
        if (messagePacket != null) {
            progressDialog.dismiss();

            mGlobalNotesDbAdapter.createGlobalNote(messagePacket.getAuthor(),
                    messagePacket.getTitle(),
                    messagePacket.getObs());

            fillData();

            Toast.makeText(getApplicationContext(),
                    getString(R.string.DayliStudentActivityReceivedSharedNote) +
                            messagePacket.getTitle()
                            + getString(R.string.DayliStudentActivityReceived2SharedNote)
                            + messagePacket.getAuthor() + ".", Toast.LENGTH_LONG).show();

            buildNotification(messagePacket);
        } else {
            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
            // Refresh main activity upon close of dialog box
            Intent refresh = new Intent(getApplicationContext(), GlobalNotes.class);
            startActivity(refresh);
            finish();
        }
    }

    private void posDataSend(MessagePacket messagePacket, boolean success) {
        Context context = getApplicationContext();
        Toast toast = success ?
                Toast.makeText(context, context.getString(R.string.SendingSharedNoteSuccess)
                        + ": " + messagePacket.getTitle(), Toast.LENGTH_LONG)
                :
                Toast.makeText(context,
                        context.getString(R.string.SendingSharedNoteFail)
                                + ": " + messagePacket.getTitle()
                                + context.getString(R.string.PleaseTryAgainLater),
                        Toast.LENGTH_LONG);
        toast.show();
    }
}
