package com.example.jorge.tpamov.Activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.example.jorge.tpamov.Classes.MessagePacket;
import com.example.jorge.tpamov.DataBase.GlobalNotesDbAdapter;
import com.example.jorge.tpamov.R;
import com.example.jorge.tpamov.Resources.NoteEdit;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class GlobalNotes extends AppCompatActivity {
    //NOTAS TEXTUAIS
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int SHARE_ID = Menu.FIRST + 2;

    static ListView lv_global_notes;
    long id;
    String autor = null, ip = null;

    private GlobalNotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;

    SharedPreferences sharedPreferences;

    //COMUNICACAO
    ProgressDialog pd = null;
    ServerSocket receiverSocket=null;
    Socket senderSocket = null;
    private static final int LISTENING_PORT = 9001;

    ObjectInputStream inObj;
    ObjectOutputStream outObj;

    Handler procMsg = null;

    int id_noti = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.activity_global_notes);
        setTitle(getString(R.string.DayliStudentActivitySharedNote));
        procMsg = new Handler();

        //abrir a base de dados das notas textuais
        mDbHelper = new GlobalNotesDbAdapter(this);
        mDbHelper.open();

        fillData();

        id = 0;

        lv_global_notes = (ListView) findViewById(R.id.lv_global_notes);
        registerForContextMenu(lv_global_notes);

        lv_global_notes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor c = mNotesCursor;
                c.moveToPosition(position);
                Intent i = new Intent(getApplicationContext(),NoteEdit.class);
                i.putExtra(GlobalNotesDbAdapter.KEY_ROWID,id);
                i.putExtra(GlobalNotesDbAdapter.KEY_AUTHOR, c.getString(c.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_AUTHOR)));
                i.putExtra(GlobalNotesDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_TITLE)));
                i.putExtra(GlobalNotesDbAdapter.KEY_BODY, c.getString(c.getColumnIndexOrThrow(GlobalNotesDbAdapter.KEY_BODY)));
                startActivityForResult(i,ACTIVITY_EDIT);
            }
        });
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
        mNotesCursor = mDbHelper.fetchAllGlobalNotes();
        startManagingCursor(mNotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{GlobalNotesDbAdapter.KEY_AUTHOR,GlobalNotesDbAdapter.KEY_TITLE, GlobalNotesDbAdapter.KEY_BODY, GlobalNotesDbAdapter.KEY_DATE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.tvAuthor,R.id.tvGlobalTitulo, R.id.tvGlobalBody, R.id.tvGlobalDate};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_global_row, mNotesCursor, from, to);
        lv_global_notes = (ListView) findViewById(R.id.lv_global_notes);
        lv_global_notes.setAdapter(notes);
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

        menu.add(0,DELETE_ID,0, R.string.DayliStudentActivityDeleteNote);
        menu.add(0,SHARE_ID,0, R.string.DayliStudentActivityShareNote);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case DELETE_ID:
                mDbHelper.deleteGlobalNote(info.id);
                fillData();
                return true;
            case SHARE_ID:
                senderDialog();
                id = info.id;
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void createBudgetAction(MenuItem item){
        Intent i= new Intent(this,NoteEdit.class);
        startActivityForResult(i,ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK) {

            Bundle extras = intent.getExtras();

            switch (requestCode) {
                case ACTIVITY_CREATE:
                    String title = extras.getString(GlobalNotesDbAdapter.KEY_TITLE);
                    String body = extras.getString(GlobalNotesDbAdapter.KEY_BODY);
                    mDbHelper.createGlobalNote(autor,title, body);
                    fillData();
                    break;
                case ACTIVITY_EDIT:
                    Integer mRowId = extras.getInt(GlobalNotesDbAdapter.KEY_ROWID);
                    if (mRowId != null) {
                        String editTitle = extras.getString(GlobalNotesDbAdapter.KEY_TITLE);
                        String editBody = extras.getString(GlobalNotesDbAdapter.KEY_BODY);
                        mDbHelper.updateGlobalNote(mRowId,autor, editTitle, editBody);
                    }
                    fillData();
                    break;
                default:
            }
        }else{
        }
    }


    private void senderDialog() {
        final EditText edtIP =  new EditText(this);
        //FALTA IMPLEMENTAR A LER DAS SHARED PREFERENCES
        edtIP.setText(ip);
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(R.string.DayliStudentActivitySendingSharedNote)
                .setMessage(R.string.DayliStudentActivityIPSendingSharedNote)
                .setView(edtIP)
                .setPositiveButton(R.string.DayliStudentActivityButtonSendingSharedNote, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //CHAMAAQUI O CONSTRUTOR
                        sender(edtIP.getText().toString(), LISTENING_PORT);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                    }
                })
                .create();
        ad.show();
    }

    void sender(final String ip, final int port){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    senderSocket = new Socket(ip, port);
                }catch (SocketTimeoutException e){
                    senderSocket = null;
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(senderSocket==null){
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"SENDERSOCKET = NULL",Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                Thread comunica = new Thread(new sendMSG());
                comunica.start();
            }
        });
        t.start();
    }

    public void receiveNote(MenuItem item) {
        String ip = getLocalIpAddress();
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.waitingConection) + "\n(IP: " + ip
                + ")");
        pd.setTitle(getString(R.string.receive));
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (receiverSocket!=null) {
                    try {
                        receiverSocket.close();
                    } catch (IOException e) {
                    }
                    receiverSocket=null;
                }
            }
        });
        pd.show();

        Thread t = new Thread(new waitConection());
        t.start();
    }

    public class waitConection implements Runnable{
        @Override
        public void run() {
            try {
                receiverSocket = new ServerSocket(9001);
                senderSocket = receiverSocket.accept();
                receiverSocket.close();
                receiverSocket=null;
                Thread t = new Thread(new receiveMSG());
                t.start();
            } catch (Exception e) {
                e.printStackTrace();
                senderSocket = null;
            }
            procMsg.post(new Runnable() {
                @Override
                public void run() {
                    pd.dismiss();
                    if (receiverSocket != null)
                        try {
                            receiverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            });
        }
    }

    public class receiveMSG implements Runnable {

        public void run() {
            try{
                inObj = new ObjectInputStream(senderSocket.getInputStream());

                final MessagePacket read = (MessagePacket) (inObj.readObject());

                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        mDbHelper.createGlobalNote(read.getAuthor(),read.getTitle(), read.getObs());

                        fillData();

                        Toast.makeText(getApplicationContext(),getString(R.string.DayliStudentActivityReceivedSharedNote) +
                                read.getTitle() + getString(R.string.DayliStudentActivityReceived2SharedNote) + read.getAuthor()+".", Toast.LENGTH_LONG).show();

                        buildNotification(read);
                    }
                });

            }catch (Exception ex){
                ex.printStackTrace();
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"ERROR", Toast.LENGTH_LONG).show();
                        // Refresh main activity upon close of dialog box
                        Intent refresh = new Intent(getApplicationContext(), GlobalNotes.class);
                        startActivity(refresh);
                        finish();
                    }
                });
            }
        }
    }

    public class sendMSG implements Runnable {
        MessagePacket msg;
        public void run() {
            try{
                outObj = new ObjectOutputStream(senderSocket.getOutputStream());

                msg = mDbHelper.getGlobalNoteToSend(id);

                id=0;

                outObj.writeObject(msg);
                outObj.flush();

                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),getString(R.string.DayliStudentActivitySuccessfullSendingSharedNote) + msg.getTitle() + " ]",Toast.LENGTH_LONG).show();
                    }
                });


            }catch (Exception ex){
                ex.printStackTrace();
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
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

    private boolean readPreferencesUser(){
        int textSize = -1;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GlobalNotes.this);

        autor = sharedPreferences.getString("nameKey", "DEFAULT");
        ip = sharedPreferences.getString("serverKey", "0.0.0.0");

        switch(sharedPreferences.getString("themeKey", "YellowTheme")) {
            case "RedTheme":
                setTheme(R.style.RedTheme);
                break;
            case "YellowTheme":
                setTheme(R.style.YellowTheme);
                break;
            case "GreenTheme":
                setTheme(R.style.GreenTheme);
                break;
        }

        System.out.println("TAMANHO ESCOLHIDO : " + sharedPreferences.getString("fontSizeKey", "darkab"));
        switch(sharedPreferences.getString("fontSizeKey", "normal")) {
            case "smallest":
                textSize = 12;
                break;
            case "small":
                textSize = 14;
                break;
            case "normal":
                textSize = 16;
                break;
            case "large":
                textSize = 18;
                break;
            case "largest":
                textSize = 20;
                break;
        }

        return true;
    }

    public void buildNotification(MessagePacket msg){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.icon)
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
}



