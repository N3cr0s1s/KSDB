package me.necrosis.surrealdb.framework.util.mapper.util

import org.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.cast
import kotlin.reflect.full.companionObject
import kotlin.reflect.safeCast

object TypeChecker {

    /**
     * Get custom type from [JSONObject]
     *
     * @param fieldName Field name
     * @param json      [JSONObject] from get type
     */
    fun <T : Any> getType(
        clazz: KClass<T>,
        fieldName: String,
        json: JSONObject
    ) : Any? {
        val value = json.get(fieldName) ?: return null
        if (value::class.qualifiedName.equals(clazz.qualifiedName))
            return value as T
        return value
    }


    fun <T: Any> getType(clazz: KClass<T>, field: KProperty1<out Any, *>, fieldObject: Any) : T? {
        val value = field.getter.call(fieldObject) ?: return null
        if (value::class.qualifiedName.equals(clazz.qualifiedName)) return value as T
        throw RuntimeException("Something went wrong, field value can't be cast to \"${value::class.qualifiedName}\"::\"${value}\" to ${clazz.qualifiedName}")
    }
}