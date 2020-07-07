package co.kr.emgram.mobilpackfieldtest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class SplashActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        const val REQUEST_PERMISSIONS = 0x00
    }

    val PERMS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);
        checkAndRequestPermissions()
    }

    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    fun checkAndRequestPermissions() {
        if (EasyPermissions.hasPermissions(this, *PERMS)) {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            EasyPermissions.requestPermissions(
                this as Activity,
                "This app needs to access your phone state and storage.",
                REQUEST_PERMISSIONS,
                *PERMS
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}