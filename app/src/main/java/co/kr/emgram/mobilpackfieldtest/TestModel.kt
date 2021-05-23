package co.kr.emgram.mobilpackfieldtest

import co.kr.emgram.mobilpackfieldtest.network.JCResponseHandler
import co.kr.emgram.mobilpackfieldtest.network.RetrofitNetwork
import co.kr.emgram.mobilpackfieldtest.network.data.UserData
import co.kr.emgram.mobilpackfieldtest.network.data.UserRequest
import co.kr.emgram.mobilpackfieldtest.network.data.UserResponse
import com.google.android.gms.common.internal.ConnectionErrorMessages.getErrorMessage
import retrofit2.Call

class TestModel {
    private var network = RetrofitNetwork()
    private var call: Call<*>? = null

    interface OnMainModelListener<T> {
        fun onSuccess(data: T?)
        fun onFailure(message: String)
    }

    fun userCreate(name: String, email: String, username: String, listener: OnMainModelListener<UserData>) {
        cancel()
        val request = UserRequest(name,email,"1234",username)
        call = network.userCreate(request, object : JCResponseHandler<UserResponse>() {
            override fun response(type: Result) {
                when (type) {
                    Result.Success -> {
                        val body = getResponse()!!.body
                        listener.onSuccess(body)
                    }
                }
            }
        })
    }

    fun cancel() {
        call?.cancel()
    }
}