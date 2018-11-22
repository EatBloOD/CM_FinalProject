package pt.uc.cm.daily_student.utils

import android.os.AsyncTask
import android.util.Log
import pt.uc.cm.daily_student.interfaces.INetworkReceiveEvents
import pt.uc.cm.daily_student.interfaces.INetworkSendEvents
import pt.uc.cm.daily_student.models.MessagePacket
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.*

class NetworkUtils private constructor() {

    private object Holder {
        val INSTANCE = NetworkUtils()
    }

    private var receiver: Receiver? = null
    private var sender: Sender? = null

    companion object {

        private const val LISTENING_PORT = 9001

        private val TAG = NetworkUtils::class.java.simpleName

        val instance: NetworkUtils by lazy { Holder.INSTANCE }

        val localIpAddress: String?
            get() {
                try {
                    val en = NetworkInterface.getNetworkInterfaces()
                    while (en.hasMoreElements()) {
                        val networkInterface = en.nextElement()
                        val enumIpAddress = networkInterface
                                .inetAddresses
                        while (enumIpAddress.hasMoreElements()) {
                            val iNetAddress = enumIpAddress.nextElement()
                            if (!iNetAddress.isLoopbackAddress && iNetAddress is Inet4Address) {
                                return iNetAddress.getHostAddress()
                            }
                        }
                    }
                } catch (ex: SocketException) {
                    Log.e(TAG, ex.message)
                }

                return null
            }
    }

    fun receiveData(iNetworkReceiveEvents: INetworkReceiveEvents) {
        receiver = Receiver(iNetworkReceiveEvents)
        receiver!!.execute()
    }

    fun sendData(ip: String, messagePacket: MessagePacket, iNetworkSendEvents: INetworkSendEvents) {
        sender = Sender(ip, messagePacket, iNetworkSendEvents)
        sender!!.execute()
    }

    fun stopAll() {
        if (receiver != null) {
            receiver!!.cancel(true)
            receiver = null
        }
        if (sender != null) {
            sender!!.cancel(true)
            sender = null
        }
    }

    private class Receiver constructor(private val iNetworkReceiveEvents: INetworkReceiveEvents)
        : AsyncTask<Void, Void, MessagePacket>() {

        private var receiverServerSocket: ServerSocket? = null
        private var receiverSocket: Socket? = null

        override fun onPreExecute() {
            this.iNetworkReceiveEvents.onPreDataReceived()
        }

        override fun doInBackground(vararg voids: Void): MessagePacket? {
            val messagePacket = arrayOfNulls<MessagePacket>(1)
            Log.e(TAG, localIpAddress)
            try {
                receiverServerSocket = ServerSocket(LISTENING_PORT)
                receiverSocket = receiverServerSocket!!.accept()
                receiverServerSocket!!.close()
                receiverServerSocket = null
                val receiveThread = Thread {
                    try {
                        val objectInputStream = ObjectInputStream(receiverSocket!!.getInputStream())

                        messagePacket[0] = objectInputStream.readObject() as MessagePacket
                    } catch (ex: IOException) {
                        Log.e(TAG, ex.message)
                        messagePacket[0] = null
                    } catch (ex: ClassNotFoundException) {
                        Log.e(TAG, ex.message)
                        messagePacket[0] = null
                    }
                }
                receiveThread.start()
                receiveThread.join()
            } catch (ex: InterruptedException) {
                Log.e(TAG, ex.message)
                messagePacket[0] = null
            } catch (ex: IOException) {
                Log.e(TAG, ex.message)
                messagePacket[0] = null
            }

            receiverSocket = null
            receiverServerSocket = null
            return messagePacket[0]
        }

        override fun onPostExecute(messagePacket: MessagePacket) {
            iNetworkReceiveEvents.onPosDataReceived(messagePacket)
        }
    }

    private class Sender constructor(private val ip: String,
                                     private val messagePacket: MessagePacket?,
                                     private val iNetworkSendEvents: INetworkSendEvents)
        : AsyncTask<Void, Void, Boolean>() {

        private var senderSocket: Socket? = null

        override fun onPreExecute() {}

        override fun doInBackground(vararg voids: Void): Boolean? {
            try {
                this.senderSocket = Socket(ip, LISTENING_PORT)
            } catch (e: IOException) {
                this.senderSocket = null
                Log.e(TAG, e.message)
                return false
            }

            if (this.messagePacket != null) {
                Log.e(TAG, "messagePacket not passed")
                return false
            }

            val objectOutputStream: ObjectOutputStream
            try {
                objectOutputStream = ObjectOutputStream(this.senderSocket!!.getOutputStream())
                objectOutputStream.writeObject(this.messagePacket)
                objectOutputStream.flush()
            } catch (ex: IOException) {
                this.senderSocket = null
                Log.e(TAG, ex.message)
                return false
            }

            this.senderSocket = null
            return true
        }

        override fun onPostExecute(success: Boolean?) {
            iNetworkSendEvents.onPosDataSend(messagePacket!!, success)
        }
    }
}
