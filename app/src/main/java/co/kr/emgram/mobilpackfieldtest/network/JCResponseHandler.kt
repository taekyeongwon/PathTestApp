package co.kr.emgram.mobilpackfieldtest.network

import co.kr.emgram.mobilpackfieldtest.network.base.BaseResponse
import co.kr.emgram.mobilpackfieldtest.network.base.BaseResponseHandler
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 모델에서 네트워크 응답 핸들링하기 위한 추상화 클래스
 */
abstract class JCResponseHandler<T: BaseResponse> : BaseResponseHandler<T>() , Callback<T>{

    override fun setResponse(response: T?, statusCode: Int, message: String) {
        when(statusCode) {
            200 -> {
                response?.let {
                    if (0 == it.resultCode) {
                        sendSuccess(it)
                    } else {
                        sendServerError(it)
                    }
                } ?: sendServerError(-1, "")
            }

        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        sendNetworkError(-1, t)
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        setResponse(response = response.body(), statusCode = response.code(), message = response.message())
    }


}