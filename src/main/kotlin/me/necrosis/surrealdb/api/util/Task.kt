package me.necrosis.surrealdb.api.util

import java.util.concurrent.Executors

/**
 * Abstract Task class,
 * to handle sync request and async requests.
 */
abstract class Task<T>(
    val function : ()->T
) {

    /**
     * Get [T] from [callback]
     *
     * @param callback
     */
    abstract fun get(callback: (T) -> Unit)

    /**
     * Force sync
     */
    abstract fun getSync(callback: (T) -> Unit)

    /**
     * Force async
     */
    abstract fun getAsync(callback: (T) -> Unit)

}

class SyncTask<T>(function: () -> T) : Task<T>(function) {

    override fun getSync(callback: (T) -> Unit) = get(callback)

    override fun getAsync(callback: (T) -> Unit) = GlobalExecutorService.service.execute{callback(function())}

    override fun get(callback: (T) -> Unit) = callback(function())

}

class AsyncTask<T>(function: () -> T) : Task<T>(function) {

    override fun getSync(callback: (T) -> Unit) = callback(function())

    override fun getAsync(callback: (T) -> Unit) = get(callback)

    override fun get(callback: (T) -> Unit) = GlobalExecutorService.service.execute{callback(function())}

}

object GlobalExecutorService{

    val service = Executors.newFixedThreadPool(10)

}