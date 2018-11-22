package pt.uc.cm.daily_student.interfaces

import pt.uc.cm.daily_student.models.MessagePacket

interface INetworkSendEvents {
    fun onPosDataSend(messagePacket: MessagePacket, success: Boolean?)
}
