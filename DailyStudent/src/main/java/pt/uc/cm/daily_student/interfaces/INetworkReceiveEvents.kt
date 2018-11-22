package pt.uc.cm.daily_student.interfaces

import pt.uc.cm.daily_student.models.MessagePacket

interface INetworkReceiveEvents {
    fun onPreDataReceived()

    fun onPosDataReceived(messagePacket: MessagePacket)
}
