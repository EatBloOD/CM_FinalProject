package pt.uc.cm.daily_student.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import pt.uc.cm.daily_student.interfaces.INetworkReceiveEvents;
import pt.uc.cm.daily_student.interfaces.INetworkSendEvents;
import pt.uc.cm.daily_student.models.MessagePacket;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final int LISTENING_PORT = 9001;

    private static NetworkUtils networkUtils = null;

    private static String ip;

    private NetworkUtils() {
        ip = getLocalIpAddress();
    }

    public static NetworkUtils getInstance() {
        if (networkUtils == null)
            networkUtils = new NetworkUtils();
        return networkUtils;
    }

    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddress = networkInterface
                        .getInetAddresses(); enumIpAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return null;
    }

    public void receiveData(INetworkReceiveEvents iNetworkReceiveEvents) {
        Receiver receiver = new Receiver(iNetworkReceiveEvents);
        receiver.execute();
    }

    public void sendData(MessagePacket messagePacket, INetworkSendEvents iNetworkSendEvents) {
        Sender sender = new Sender(messagePacket, iNetworkSendEvents);
        sender.execute();
    }

    private static class Receiver extends AsyncTask<Void, Void, MessagePacket> {
        private ServerSocket receiverServerSocket = null;
        private Socket receiverSocket = null;
        private INetworkReceiveEvents iNetworkReceiveEvents;

        Receiver(INetworkReceiveEvents iNetworkReceiveEvents) {
            this.iNetworkReceiveEvents = iNetworkReceiveEvents;
        }

        @Override
        protected void onPreExecute() {
            this.iNetworkReceiveEvents.onPreDataReceived();
        }

        @Override
        protected MessagePacket doInBackground(Void... voids) {
            final MessagePacket[] messagePacket = new MessagePacket[1];
            try {
                receiverServerSocket = new ServerSocket(LISTENING_PORT);
                receiverSocket = receiverServerSocket.accept();
                receiverServerSocket.close();
                receiverServerSocket = null;
                Thread receiveThread = new Thread(() -> {
                    try {
                        ObjectInputStream objectInputStream = new ObjectInputStream(receiverSocket.getInputStream());

                        messagePacket[0] = (MessagePacket) (objectInputStream.readObject());
                    } catch (IOException | ClassNotFoundException ex) {
                        Log.e(TAG, ex.getMessage());
                        messagePacket[0] = null;
                    }
                });
                receiveThread.start();
                receiveThread.join();
            } catch (InterruptedException | IOException ex) {
                Log.e(TAG, ex.getMessage());
                messagePacket[0] = null;
            }
            receiverSocket = null;
            receiverServerSocket = null;
            return messagePacket[0];
        }

        @Override
        protected void onPostExecute(MessagePacket messagePacket) {
            iNetworkReceiveEvents.onPosDataReceived(messagePacket);
        }
    }

    private static class Sender extends AsyncTask<Void, Void, Boolean> {
        private Socket senderSocket = null;
        private MessagePacket messagePacket;
        private INetworkSendEvents iNetworkSendEvents;

        Sender(MessagePacket messagePacket, INetworkSendEvents iNetworkSendEvents) {
            this.messagePacket = messagePacket;
            this.iNetworkSendEvents = iNetworkSendEvents;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                this.senderSocket = new Socket(ip, LISTENING_PORT);
            } catch (IOException e) {
                this.senderSocket = null;
                Log.e(TAG, e.getMessage());
                return false;
            }

            if (this.messagePacket != null) {
                Log.e(TAG, "messagePacket not passed");
                return false;
            }

            ObjectOutputStream objectOutputStream;
            try {
                objectOutputStream = new ObjectOutputStream(this.senderSocket.getOutputStream());
                objectOutputStream.writeObject(this.messagePacket);
                objectOutputStream.flush();
            } catch (IOException ex) {
                this.senderSocket = null;
                Log.e(TAG, ex.getMessage());
                return false;
            }
            this.senderSocket = null;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            iNetworkSendEvents.onPosDataSend(messagePacket, success);
        }
    }
}
