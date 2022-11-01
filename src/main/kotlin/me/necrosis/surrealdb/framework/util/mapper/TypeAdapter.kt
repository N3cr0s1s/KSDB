package me.necrosis.surrealdb.framework.util.mapper

import me.necrosis.surrealdb.framework.KSDB
import org.json.JSONObject
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

data class TypeAdapterPair(
    val clazz: KClass<*>,
    val adapter: ITypeAdapter
)

open class TypeAdapter protected constructor() {
    companion object {

        private val adapters:List<TypeAdapterPair> = listOf(
            TypeAdapterPair(    String::class,  BaseAdapter(String::class)  ),
            TypeAdapterPair(    Double::class,  BaseAdapter(Double::class)  ),
            TypeAdapterPair(    Boolean::class, BaseAdapter(Boolean::class) ),
            TypeAdapterPair(    Float::class,   BaseAdapter(Float::class)   ),
            TypeAdapterPair(    Int::class,     BaseAdapter(Int::class)     ),
            TypeAdapterPair(    Number::class,  BaseAdapter(Number::class)  ),
            TypeAdapterPair(    Array::class,   ArrayAdapter()  ),
        )

        /**
         * Get [Annotation] adapter
         *
         * @param clazz Annotation key, as [KClass]
         */
        fun getAdapter(clazz: KClass<*>) : ITypeAdapter {
            adapters.forEach {
                if (it.clazz.java.simpleName.equals(clazz.java.simpleName))
                    return it.adapter
                if (it.clazz == clazz)
                    return it.adapter
                if(it.clazz.qualifiedName == clazz.qualifiedName)
                    return it.adapter
                return@forEach
            }
            return DefaultAdapter.get()
        }

    }
}

interface ITypeAdapter{

    /**
     * Save [field] value to [mappedObject]
     *
     * @param field         Class field
     * @param fieldObject   Object which contains the [field]
     * @param KSDB          [me.necrosis.surrealdb.api.SurrealDB] to perform database requests
     * @param mappedObject  Out object as [JSONObject]
     */
    fun write(
        field: KProperty1<out Any, *>,
        fieldObject: Any,
        KSDB: KSDB,
        mappedObject: JSONObject
    )

    /**
     * Save [result] to [field]
     *
     * @param field         Class field
     * @param fieldObject   Object which contains the [field]
     * @param KSDB          [me.necrosis.surrealdb.api.SurrealDB] to perform database requests
     * @param result        Read field value from this as [JSONObject]
     */
    fun read(
        field: Field,
        fieldObject: Any,
        KSDB: KSDB,
        result: JSONObject
    )
}