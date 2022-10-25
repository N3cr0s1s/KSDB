package me.necrosis.surrealdb.framework

import me.necrosis.surrealdb.api.SurrealDB
import me.necrosis.surrealdb.framework.component.table.Table
import me.necrosis.surrealdb.framework.component.table.TableHandler
import me.necrosis.surrealdb.framework.exception.JSDBException
import me.necrosis.surrealdb.framework.util.mapper.DefaultAdapter
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * Kotlin SurrealDB framework class
 */
class KSDB(private val surrealDB:SurrealDB)  {

    fun getSurrealDB() : SurrealDB = this.surrealDB

    /**
     * KSDB initialization stage
     */
    init {
        this.surrealDB.connect().getSync {
            if (it) return@getSync
            throw JSDBException("Connection failed to SurrealDB.")
        }
    }

    /**
     * Get [TableHandler] to handle database tables.
     *
     * @param T Table which used in [TableHandler]
     */
    inline fun <reified T : Any> get() : TableHandler<T> = get(T::class)

    fun <T: Any> get(clazz: KClass<T>) : TableHandler<T> {
        if(!clazz.hasAnnotation<Table>())
            throw JSDBException("Class '${clazz.qualifiedName}' does not have ${Table::class.qualifiedName} annotation!")
        val tableAnnotation:Table = clazz.findAnnotation()!!
        return TableHandler(this,tableAnnotation,clazz)
    }

}