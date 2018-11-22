package pt.uc.cm.daylistudent.interfaces

import pt.uc.cm.daylistudent.models.MessagePacket

interface INetworkSendEvents {
    fun onPosDataSend(messagePacket: MessagePacket, success: Boolean?)
}
