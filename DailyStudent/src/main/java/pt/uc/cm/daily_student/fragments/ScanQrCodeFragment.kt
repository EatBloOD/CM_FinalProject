package pt.uc.cm.daily_student.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import kotlinx.android.synthetic.main.scan_qr_code_fragment.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import pt.uc.cm.daily_student.R
import pt.uc.cm.daily_student.interfaces.IScanQrCodeEvents

class ScanQrCodeFragment : DialogFragment(), ZXingScannerView.ResultHandler {

    companion object {
        private val TAG = ScanQrCodeFragment::class.java.simpleName

        private const val HUAWEI = "huawei"
        private const val MY_CAMERA_REQUEST_CODE = 6515
        fun getScanQrCodeActivity(callingClassContext: Context) = Intent(callingClassContext, ScanQrCodeFragment::class.java)
    }

    lateinit var scanQrCodeEvents: IScanQrCodeEvents

    var title: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.scan_qr_code_fragment, container)
    }

    override fun onStart() {
        super.onStart()
        dialog.setTitle(title)
        setScannerProperties()
        barcodeBackImageView.setOnClickListener { dismiss() }
        flashOnOffImageView.setOnClickListener {
            if (qrCodeScanner.flash) {
                qrCodeScanner.flash = false
                flashOnOffImageView.background = ContextCompat.getDrawable(activity!!, R.drawable.flash_off_vector_icon)
            } else {
                qrCodeScanner.flash = true
                flashOnOffImageView.background = ContextCompat.getDrawable(activity!!, R.drawable.flash_on_vector_icon)
            }
        }
    }

    /**
     * Set bar code scanner basic properties.
     */
    private fun setScannerProperties() {
        qrCodeScanner.setFormats(listOf(BarcodeFormat.QR_CODE))
        qrCodeScanner.setAutoFocus(true)
        qrCodeScanner.setLaserColor(R.color.colorAccent)
        qrCodeScanner.setMaskColor(R.color.colorAccent)
        if (Build.MANUFACTURER.equals(HUAWEI, ignoreCase = true))
            qrCodeScanner.setAspectTolerance(0.5f)
    }

    /**
     * resume the qr code camera when activity is in onResume state.
     */

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity!!.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA),
                        MY_CAMERA_REQUEST_CODE)
                return
            }
        }
        qrCodeScanner.startCamera()
        qrCodeScanner.setResultHandler(this)
    }

    /**
     * To check if user grant camera permission then called openCamera function.If not then show not granted
     * permission snack bar.
     *
     * @param requestCode  specify which request result came from operating system.
     * @param permissions  to specify which permission result is came.
     * @param grantResults to check if user granted the specific permission or not.
     */

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openCamera()
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                showCameraSnackBar()
        }
    }

    private fun showCameraSnackBar() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.CAMERA)) {
            val snackbar = Snackbar.make(scanQrCodeRootView,
                    "App need camera to do perform this feature!", Snackbar.LENGTH_LONG)
            val view1 = snackbar.view
            view1.setBackgroundColor(ContextCompat.getColor(activity!!, R.color.white))
            val textView = view1.findViewById<TextView>(android.support.design.R.id.snackbar_text)
            textView.setTextColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
            snackbar.show()
        }
    }

    private fun openCamera() {
        qrCodeScanner.startCamera()
        qrCodeScanner.setResultHandler(this)
    }

    /**
     * stop the qr code camera scanner when activity is in onPause state.
     */
    override fun onPause() {
        super.onPause()
        qrCodeScanner.stopCamera()
    }

    override fun handleResult(p0: Result?) {
        scanQrCodeEvents.onDataScanned(p0!!.text)
        dismiss()
    }

    /**
     * Resume the camera after 2 seconds when qr code successfully scanned through bar code reader.
     */

    private fun resumeCamera() {
        Toast.LENGTH_LONG
        val handler = Handler()
        handler.postDelayed({ qrCodeScanner.resumeCameraPreview(this@ScanQrCodeFragment) }, 2000)
    }

}
