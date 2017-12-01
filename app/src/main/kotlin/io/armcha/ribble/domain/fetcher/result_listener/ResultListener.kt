package io.armcha.ribble.domain.fetcher.result_listener

/**
 * Created by Chatikyan on 04.08.2017.
 */
interface ResultListener {

    fun onRequestStart(){}

    /**
     * 请求开始
     * @param requestType [RequestType]
     */
    fun onRequestStart(requestType: RequestType){}

    fun onRequestError(errorMessage: String?){}

    fun onRequestError(requestType: RequestType, errorMessage: String?){}
}