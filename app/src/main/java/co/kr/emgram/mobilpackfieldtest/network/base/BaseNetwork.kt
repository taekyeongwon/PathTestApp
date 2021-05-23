package co.kr.emgram.mobilpackfieldtest.network.base

import co.kr.emgram.mobilpackfieldtest.network.JCResponseHandler
import co.kr.emgram.mobilpackfieldtest.network.data.UserRequest
import co.kr.emgram.mobilpackfieldtest.network.data.UserResponse
import retrofit2.Call

/**
 * api 인터페이스
 */
interface BaseNetwork {

    fun userCreate(request : UserRequest, handler: JCResponseHandler<UserResponse>) : Call<UserResponse>
}