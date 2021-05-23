package co.kr.emgram.mobilpackfieldtest.network

import android.text.TextUtils
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Post로 요청 시 헤더에 토큰 값 넣어주기 위한 Interceptor
 */
class JCAPIInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val token = "CqmY4ppq61GmixFD6QFP-_ahwnhvBx5DVEWSARj6xbK"
        val id = "ePztgtQ39TN2YPeRx"
        request = chain.request().newBuilder().addHeader("X-Auth-Token", token).addHeader("X-User-Id", id).build()

        return chain.proceed(request)

    }

}