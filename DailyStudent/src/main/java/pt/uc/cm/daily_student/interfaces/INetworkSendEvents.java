package pt.uc.cm.daily_student.interfaces;

import pt.uc.cm.daily_student.models.MessagePacket;

public interface INetworkSendEvents {
    void onPosDataSend(MessagePacket messagePacket, Boolean success);
}
