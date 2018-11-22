package pt.uc.cm.daily_student.interfaces;

import pt.uc.cm.daily_student.models.MessagePacket;

public interface INetworkReceiveEvents {
    void onPreDataReceived();

    void onPosDataReceived(MessagePacket messagePacket);
}
