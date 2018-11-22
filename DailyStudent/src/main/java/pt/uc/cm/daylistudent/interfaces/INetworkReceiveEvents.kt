package pt.uc.cm.daylistudent.interfaces

import pt.uc.cm.daylistudent.models.MessagePacket

interface INetworkReceiveEvents {
    fun onPreDataReceived()

    fun onPosDataReceived(messagePacket: MessagePacket)
}
