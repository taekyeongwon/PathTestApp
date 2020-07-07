package co.kr.emgram.mobilpackfieldtest

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.system.Os.close
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.facebook.*
import kotlinx.android.synthetic.main.activity_screentshot.*
import java.io.File
import java.io.FileOutputStream
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.model.*
import com.facebook.share.widget.ShareDialog
import java.util.*

class ScreenshotActivity: AppCompatActivity() {
    lateinit var callback: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_screentshot)

        callback = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callback, object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                Log.d("Success", "token=${result?.accessToken}")

                val bitmap = screenCapture()


                val photo = SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build()


//            val list = ArrayList<SharePhoto>()
//            list.add(photo)

//            val content = ShareLinkContent.Builder()
//                .setContentUrl(Uri.parse("https://developers.facebook.com"))
//                .build()

                if(ShareDialog.canShow(SharePhotoContent::class.java)) {
                    val content = SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build()
                    ShareDialog.show(this@ScreenshotActivity, content)
                    //val dialog = ShareDialog(this)
                    //dialog.show(content, ShareDialog.Mode.FEED)
                } else {
                    Log.d("Share", "else")
                }
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }
        })

        btn_screeshot.setOnClickListener {
            val token = AccessToken.getCurrentAccessToken()
            val isLoggedIn = token != null && !token.isExpired

//            if(isLoggedIn) {
//                LoginManager.getInstance().retrieveLoginStatus(this,  object: LoginStatusCallback {
//                    override fun onFailure() {
//                        Log.d("retrieve", "fail")
//                    }
//
//                    override fun onError(exception: java.lang.Exception?) {
//                        Log.d("retrieve", "error")
//                    }
//
//                    override fun onCompleted(accessToken: AccessToken?) {
//                        Log.d("retrieve", "completed")
//                    }
//                })
//            } else {
                LoginManager.getInstance()
                    .logInWithReadPermissions(this, Arrays.asList("public_profile"))

//            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callback.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun screenCapture(): Bitmap {
        val bitmap = getBitmapFromView(parent_sv, parent_sv.getChildAt(0).height, parent_sv.getChildAt(0).width)
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                with(values) {
                    put(MediaStore.Images.Media.TITLE, "test_capture")
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/my_folder")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }

                val uri = getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                val fos = contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos?.run {
                    flush()
                    close()
                }
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
                        File.separator +
                        "my_folder"
                val file = File(dir)
                if (!file.exists()) {
                    file.mkdirs()
                }

                val imgFile = File(file, "test_capture.jpg")
                val os = FileOutputStream(imgFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()

                val values = ContentValues()
                with(values) {
                    put(MediaStore.Images.Media.TITLE, "test_capture")
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.BUCKET_ID, "test_capture")
                    put(MediaStore.Images.Media.DATA, imgFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }

                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    private fun getBitmapFromView(view: View, height: Int, width: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        canvas.drawColor(Color.WHITE)
        bgDrawable?.let {
            bgDrawable.draw(canvas)
        } ?: {
            canvas.drawColor(Color.WHITE)
        } ()
        view.draw(canvas)
        return bitmap
    }
}