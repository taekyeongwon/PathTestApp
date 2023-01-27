package co.kr.emgram.mobilpackfieldtest.webview

import android.os.Bundle
import android.os.Message
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import co.kr.emgram.mobilpackfieldtest.R
import co.kr.emgram.mobilpackfieldtest.databinding.ActivityWebviewBinding
import org.json.JSONObject

class WebViewActivity: AppCompatActivity() {
    private var mBinding: ActivityWebviewBinding? = null
    private val binding get() = mBinding!!
    private var childWebView: WebView? = null
    private val webAppUrl = "http://192.168.0.4:8080/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        mBinding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initWebView()

    }

    override fun onDestroy() {
        binding.webview.removeJavascriptInterface("WebViewBridge")
        super.onDestroy()
    }

    private fun initWebView() {
        settingWebView()
        setWebViewClient()
        addInterfaceAndLoad()
    }

    private fun settingWebView() {
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.loadWithOverviewMode = true
        binding.webview.settings.useWideViewPort = true
        binding.webview.settings.domStorageEnabled = true   //웹앱 로컬 스토리지 사용
        binding.webview.settings.setSupportMultipleWindows(true)
        binding.webview.settings.javaScriptCanOpenWindowsAutomatically = true

        //임시 줌 처리
        binding.webview.settings.setSupportZoom(true)
        binding.webview.settings.setBuiltInZoomControls(true)
        //디버깅 테스트용
        WebView.setWebContentsDebuggingEnabled(true)
    }

    private fun setWebViewClient() {
        binding.webview.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
//                if(url?.contains(NEXT_URL) == true) {
//                    childBackPressed()  //window.close 처리
//                }
            }
        }

        binding.webview.webChromeClient = object: WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                return super.onJsAlert(view, url, message, result)
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val child = CustomView(this@WebViewActivity)
                childWebView = child.findViewById<WebView>(R.id.webview).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onCloseWindow(window: WebView?) {
//                            childWebViewList.remove(window)
                            view?.removeView(child)
                            window?.destroy()
                            childWebView = null
                        }
                    }
                }
                child.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                view?.addView(child)
//                childWebViewList.add(newWebView)
                (resultMsg?.obj as WebView.WebViewTransport).webView = childWebView
                resultMsg.sendToTarget()

                return true
            }
        }
    }

    override fun onBackPressed() {
        if(childWebView != null) {
            if(childWebView!!.canGoBack()) {
                childWebView!!.goBack()
            } else {
                childWebView!!.loadUrl("javascript:window.close();")
            }
        } else {
            if (binding.webview.url.equals(webAppUrl)) {  //메인화면인 경우 MainActivity 이전화면으로 이동
                super.onBackPressed()
            } else if (binding.webview.canGoBack()) {    //뒤로 갈 수 있는 경우 웹뷰 뒤로가기
                binding.webview.goBack()
            } else {    //딥링크로 2depth 이상 들어온 경우 메인화면으로
                binding.webview.loadUrl(webAppUrl)
            }
        }
    }

    private fun addInterfaceAndLoad() {
        binding.webview.addJavascriptInterface(this, "WebViewBridge")
        binding.webview.loadUrl(webAppUrl)
    }

    @JavascriptInterface
    fun reqPushToken() {

    }

    @JavascriptInterface
    fun reqUserInfo() {
        val userInfoJson = JSONObject()
        userInfoJson.put("userId", "tkw3351")
        userInfoJson.put("userName", "taekw")
        runOnUiThread {
            binding.webview.evaluateJavascript("window.NativeReceiver.resUserInfo(${userInfoJson})", null)
        }
    }

    @JavascriptInterface
    fun closeWebApp() {

    }

    @JavascriptInterface
    fun callToPartner(phoneNumber: String) {

    }

    @JavascriptInterface
    fun addPreventBackPressUrl(urls: Array<String>) {

    }
}