package co.kr.emgram.mobilpackfieldtest.network.base

/**
 * 네트워크 응답에 대한 상위 추상화 클래스
 */
abstract class BaseResponseHandler<T:BaseResponse> {

    private var response : T? = null
    private var httpCode = -1
    private var e : Throwable? = null

    fun sendSuccess(response : T) {
        this.response = response
        response(Result.Success)
    }

    fun sendServerError(code : Int, message: String?) {
        response(Result.ServerError)
    }

    fun sendServerError(response : T) {
        this.response = response
        response(Result.ServerError)
    }

    fun sendNetworkError(httpCode : Int, e : Throwable? = null) {
        this.httpCode = httpCode
        this.e = e

        response(Result.NetworkError)
    }

    fun getResponse() : T? {
        return response
    }

    abstract fun setResponse(response: T?, statusCode: Int = -1, message: String = "")
    abstract fun response(type : Result)

    enum class Result {
        Success, ServerError, NetworkError
    }
}