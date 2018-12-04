package pt.uc.cm.daylistudent.interfaces

interface IScanQrCodeEvents {
    fun onDataScanned(scannedText: String)
    fun onError()
}