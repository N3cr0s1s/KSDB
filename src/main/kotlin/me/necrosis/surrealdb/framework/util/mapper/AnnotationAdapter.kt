package me.necrosis.surrealdb.framework.util.mapper

import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.component.*
import org.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class AnnotationAdapter protected constructor(){

    companion object {

        private val adapters = listOf(
            AnnotationAdapterPair(Random::class,RandomAdapter()),
            AnnotationAdapterPair(Id::class,IdAdapter()),
            AnnotationAdapterPair(Crypto::class,CryptoAdapter()),
            AnnotationAdapterPair(Session::class,SessionAdapter())
        )

        /**
         * Get [Annotation] adapter
         *
         * @param clazz Annotation key, as [KClass]
         */
        fun getAdapter(clazz: KClass<*>) : IAnnotationAdapter {
            adapters.forEach {
                if(it.annotation == clazz)
                    return it.adapter
                return@forEach
            }
            throw java.lang.NullPointerException("Adapter for this type, is not exist! Type $clazz")
        }

        /**
         * @return Is [clazz] key has any adapter
         */
        fun hasAdapter(clazz: KClass<*>) : Boolean{
            adapters.forEach {
                if(it.annotation == clazz)
                    return true
                return@forEach
            }
            return false
        }

    }
}

data class AnnotationAdapterPair(
    val annotation: KClass<*>,
    val adapter: IAnnotationAdapter
)

interface IAnnotationAdapter {

    /**
     * @param field         Class field
     * @param fieldObject   Object which contains the [field]
     * @param KSDB          [me.necrosis.surrealdb.api.SurrealDB] to perform database requests
     * @param mappedObject  Out object as [JSONObject]
     */
    fun process(
        field: KProperty1<out Any,*>,
        fieldObject: Any,
        KSDB: KSDB,
        mappedObject: JSONObject
    )

}