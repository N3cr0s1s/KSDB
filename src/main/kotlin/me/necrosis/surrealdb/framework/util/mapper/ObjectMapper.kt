package me.necrosis.surrealdb.framework.util.mapper

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.component.Id
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Object mapper
 */
class ObjectMapper(val KSDB: KSDB){

    fun mapObject(
        obj: Any,
        ignoreAnnotation: Boolean = false,
        callBack: (JSONObject)->Unit
    ) = runBlocking {
        launch {
            val clazz = obj::class
            val json = JSONObject()
            clazz.declaredMemberProperties.forEach { kField ->
                kField.isAccessible = true
                val field = kField.javaField!!
                if (!ignoreAnnotation) {
                    field.declaredAnnotations.forEach {
                        val annClazz = Class.forName(
                            (it.toString())
                                .replace("@", "")
                                .split('(')[0]
                        ).kotlin
                        AnnotationAdapter.getAdapter(annClazz).process(
                            kField, obj, KSDB, json
                        )
                    }
                }
                if (field.isAnnotationPresent(Id::class.java)) {
                    if (ignoreAnnotation)
                        AnnotationAdapter.getAdapter(Id::class).process(kField, obj, KSDB, json)
                    return@forEach
                }
                TypeAdapter.getAdapter(field.get(obj)::class)
                    .write(kField, obj, KSDB, json)
            }
            callBack(json)
        }
    }

    fun <T : Any> unmapObject(
        clazz: KClass<out T>,
        array: JSONArray,
        callBack: (List<T>) -> Unit,
        wait: Boolean = true
    ) = runBlocking {
        val job = launch {
            val arrayList: MutableList<T> = mutableListOf()
            for (i in 0 until array.length()){
                val jsonObject = array.getJSONObject(i)
                unmapObject(clazz,jsonObject,{
                    arrayList.add(it)
                },true)
            }
            callBack(arrayList)
        }
        if (wait) job.join()
    }

    fun <T : Any> unmapObject(clazz_:KClass<out T>,jsonObject: JSONObject, callBack: (T) -> Unit, wait: Boolean = true) = runBlocking{
        val job = launch{
            val clazz = clazz_.java
            val newObject = clazz.getConstructor().newInstance()
            clazz.declaredFields.forEach {
                it.isAccessible = true
                //  Write data to field
                TypeAdapter.getAdapter(it.type::class)
                    .read(it,newObject,KSDB,jsonObject)
            }
            callBack(newObject as T)
        }
        if (wait) job.join()
    }


}