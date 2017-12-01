package io.armcha.ribble.domain.fetcher

import io.armcha.ribble.domain.fetcher.result_listener.RequestType
import io.armcha.ribble.domain.fetcher.result_listener.ResultListener
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rxjava 的具体操作类,其中用 Map 记录了某种类型的上一次请求结果,所有的被观察者都在 IO 线程运行,结果则反馈给主线程执行.
 * Created by Chatikyan on 04.08.2017.
 */
@Singleton
class Fetcher @Inject constructor(private val disposable: CompositeDisposable) {

    private val requestMap = ConcurrentHashMap<RequestType, Status>()

    private fun <T> getIOToMainTransformer(): SingleTransformer<T, T> {
        return SingleTransformer {
            it.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> fetch(flowable: Flowable<T>, requestType: RequestType,
                  resultListener: ResultListener, success: (T) -> Unit) {
        disposable.add(flowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { resultListener startAndAdd requestType }
                .subscribe(onSuccess<T>(requestType, success),
                        resultListener.onError(requestType)))
    }

    fun <T> fetch(observable: Observable<T>, requestType: RequestType,
                  resultListener: ResultListener, success: (T) -> Unit) {
        disposable.add(observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { resultListener startAndAdd requestType }
                .subscribe(onSuccess<T>(requestType, success),
                        resultListener.onError(requestType)))
    }

    fun <T> fetch(single: Single<T>, requestType: RequestType,
                  resultListener: ResultListener, success: (T) -> Unit) {
        disposable.add(single
                .compose(getIOToMainTransformer()) //TODO 没搞懂,为什么不直接在这里指定线程
                .doOnSubscribe { resultListener startAndAdd requestType }
                .subscribe(onSuccess<T>(requestType, success),
                        resultListener.onError(requestType)))
    }

    fun complete(completable: Completable, requestType: RequestType,
                 resultListener: ResultListener, success: () -> Unit) {
        disposable.add(completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { resultListener startAndAdd requestType }
                .subscribe({
                    requestMap.replace(requestType, Status.SUCCESS)
                    success()
                }, resultListener.onError(requestType)))
    }

    /**
     * 在开始请求时,记录当前的请求类型,并标记类型为 Loading
     * 采用扩展函数,并使用 infix 标记为中缀表示法.
     */
    private infix fun ResultListener.startAndAdd(requestType: RequestType) {
        //Callback 请求开始
        onRequestStart(requestType)
        if (requestType != RequestType.TYPE_NONE)
            requestMap.put(requestType, Status.LOADING)
    }

    private fun ResultListener.onError(requestType: RequestType): (Throwable) -> Unit {
        return {
            requestMap.replace(requestType, Status.ERROR)
            onRequestError(requestType, it.message)
        }
    }

    /**
     * 回调数据结果,并且更新请求状态.
     */
    private fun <T> onSuccess(requestType: RequestType, success: (T) -> Unit): (T) -> Unit {
        return {
            val status = if (it is List<*> && it.isEmpty()) {
                Status.EMPTY_SUCCESS
            } else {
                Status.SUCCESS
            }
            requestMap.replace(requestType, status)
            success(it)
        }
    }

    fun hasActiveRequest(): Boolean = requestMap.isNotEmpty()

    fun getRequestStatus(requestType: RequestType) = requestMap.getOrElse(requestType, { Status.IDLE })

    fun removeRequest(requestType: RequestType) {
        requestMap.remove(requestType)
    }

    fun clear() {
        disposable.clear()
    }
}