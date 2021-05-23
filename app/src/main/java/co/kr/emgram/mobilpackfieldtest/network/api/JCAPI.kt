package co.kr.emgram.mobilpackfieldtest.network.api

import co.kr.emgram.mobilpackfieldtest.network.base.BaseResponse
import co.kr.emgram.mobilpackfieldtest.network.data.UserRequest
import co.kr.emgram.mobilpackfieldtest.network.data.UserResponse
import retrofit2.Call
import retrofit2.http.*

interface JCAPI {

    /**
     * 로그인
     */
    @POST("/api/v1/users.create")
    fun userCreate(@Body request: UserRequest) : Call<UserResponse>


}