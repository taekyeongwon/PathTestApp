package co.kr.emgram.mobilpackfieldtest.network

import co.kr.emgram.mobilpackfieldtest.MainApplication
import co.kr.emgram.mobilpackfieldtest.R
import co.kr.emgram.mobilpackfieldtest.network.api.JCAPI
import co.kr.emgram.mobilpackfieldtest.network.base.BaseNetwork
import co.kr.emgram.mobilpackfieldtest.network.base.BaseResponse
import co.kr.emgram.mobilpackfieldtest.network.data.UserRequest
import co.kr.emgram.mobilpackfieldtest.network.data.UserResponse
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 네트워크 통신용 레트로핏 사용 클래스
 */
class RetrofitNetwork : BaseNetwork {
    private val retrofit : Retrofit
    private val jcapi : JCAPI
    private val dispatcher: Dispatcher

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        dispatcher = Dispatcher()

        val client = OkHttpClient.Builder()
            .writeTimeout(10, TimeUnit.SECONDS)
            .dispatcher(dispatcher)
            .addInterceptor(JCAPIInterceptor())
            .addInterceptor(interceptor).build()

        retrofit = Retrofit.Builder()
            .baseUrl(MainApplication.application?.getString(R.string.base_url) ?: "http://")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        jcapi = retrofit.create(JCAPI::class.java)
    }

    fun allCancel() {
        dispatcher.cancelAll()
    }

    override fun userCreate(
        request: UserRequest,
        handler: JCResponseHandler<UserResponse>
    ): Call<UserResponse> {
        return jcapi.userCreate(request).also { it.enqueue(handler) }
    }
}