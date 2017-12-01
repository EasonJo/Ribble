package io.armcha.ribble.presentation.base_mvp.api

import android.support.annotation.CallSuper
import io.armcha.ribble.domain.fetcher.Fetcher
import io.armcha.ribble.domain.fetcher.Status
import io.armcha.ribble.domain.fetcher.result_listener.RequestType
import io.armcha.ribble.domain.fetcher.result_listener.ResultListener
import io.armcha.ribble.presentation.base_mvp.base.BaseContract
import io.armcha.ribble.presentation.base_mvp.base.BasePresenter
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * 封装了各个 Presenter 的请求状态.以及请求类型.默认情况下使用的请求类型是 NONE
 *
 * 这里巧妙的把各个业务的请求(Observerable) 再次封装一层,加入请求类型和请求状态.
 * @author Chatikyan on 04.08.2017.
 */
abstract class ApiPresenter<VIEW : BaseContract.View> : BasePresenter<VIEW>(), ResultListener {

    @Inject
    protected lateinit var fetcher: Fetcher

    //请求类型
    private val TYPE_NONE = RequestType.TYPE_NONE
    protected val AUTH: RequestType = RequestType.AUTH
    protected val POPULAR_SHOTS = RequestType.POPULAR_SHOTS
    protected val RECENT_SHOTS = RequestType.RECENT_SHOTS
    protected val FOLLOWINGS_SHOTS = RequestType.FOLLOWINGS_SHOTS
    protected val LIKED_SHOTS = RequestType.LIKED_SHOTS
    protected val COMMENTS = RequestType.COMMENTS
    protected val LIKE = RequestType.LIKE
    //请求状态
    protected val SUCCESS = Status.SUCCESS
    protected val LOADING = Status.LOADING
    protected val ERROR = Status.ERROR
    protected val EMPTY_SUCCESS = Status.EMPTY_SUCCESS

    protected infix fun RequestType.statusIs(status: Status) = fetcher.getRequestStatus(this) == status

    //扩展属性,获取当前请求的类型的状态
    protected val RequestType.status
        get() = fetcher.getRequestStatus(this)

    /**
     * Flowable<T>类型请求封装
     */
    fun <TYPE> fetch(flowable: Flowable<TYPE>,
                     requestType: RequestType = TYPE_NONE, success: (TYPE) -> Unit) {
        fetcher.fetch(flowable, requestType, this, success)
    }

    /**
     * Observable<T>类型封装.
     */
    fun <TYPE> fetch(observable: Observable<TYPE>,
                     requestType: RequestType = TYPE_NONE, success: (TYPE) -> Unit) {
        fetcher.fetch(observable, requestType, this, success)
    }

    /**
     * Single<T>类型封装, Single 类型只发射一个数据
     * @param success(TYPE) 回调函数封装
     */
    fun <TYPE> fetch(single: Single<TYPE>,
                     requestType: RequestType = TYPE_NONE, success: (TYPE) -> Unit) {
        fetcher.fetch(single, requestType, this, success)
    }

    /**
     * Completable<T>类型封装
     */
    fun complete(completable: Completable,
                 requestType: RequestType = TYPE_NONE, success: () -> Unit = {}) {
        fetcher.complete(completable, requestType, this, success)
    }

    @CallSuper
    override fun onPresenterDestroy() {
        super.onPresenterDestroy()
        fetcher.clear()
    }

    @CallSuper
    override fun onRequestStart(requestType: RequestType) {
        onRequestStart()
    }

    @CallSuper
    override fun onRequestError(requestType: RequestType, errorMessage: String?) {
        onRequestError(errorMessage)
    }
}