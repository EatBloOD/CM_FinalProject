package pt.uc.cm.daily_student.interfaces

interface IScanQrCodeEvents {
    fun onDataScanned(scannedText: String)
}