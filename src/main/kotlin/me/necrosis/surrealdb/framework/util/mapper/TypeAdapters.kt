package me.necrosis.surrealdb.framework.util.mapper

import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.component.table.Table
import me.necrosis.surrealdb.framework.util.mapper.util.TypeChecker
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible

class DefaultAdapter : ITypeAdapter {

    companion object{

        var instance:DefaultAdapter? = null

        fun get() : DefaultAdapter {
            if (instance == null) instance = DefaultAdapter()
            return instance!!
        }
    }

    override fun write(field: KProperty1<out Any, *>, fieldObject: Any, KSDB: KSDB, mappedObject: JSONObject) {
        val obj = field.getter.call(fieldObject) ?:
            throw NullPointerException("${this::class.qualifiedName}#write :: Field value is null.")
        /**
         * RELATIONS
         *
         * Create a new record in the field table
         */
        if(obj::class.hasAnnotation<Table>()){
            val table: Table = obj::class.findAnnotation()!!
            KSDB.get(obj::class)
                .createUnsafe(obj, wait = true){
                    val relation: String = if (it.isNull("_id")) "${table.tableName}:${it.getString("id")}"
                    else "${table.tableName}:${it.getString("_id")}"
                    mappedObject.put(field.name,relation)
                }
            return
        }
        mappedObject.put(field.name,obj.toString())
    }

    override fun read(field: Field, fieldObject: Any, KSDB: KSDB, result: JSONObject) {
        val fieldType = field.type
        field.isAccessible = true

        /**
         * RELATIONS
         *
         * Load field id from db,
         * find the relation record, create a new object,
         * set relation field to object.
         */
        if(fieldType.isAnnotationPresent(Table::class.java)){
            val relationResult = KSDB.get(fieldType.kotlin)
                .find(result.getString(field.name))
                .get()

            if (relationResult == null) {
                field.set(fieldObject,null)
                return
            }

            field.set(fieldObject, relationResult)
            return
        }
        field.set(fieldObject,result.get(field.name))

    }

}

class BaseAdapter<T: Any>(private val type : KClass<out T>) : ITypeAdapter{

    companion object{
        inline operator fun <reified T: Any> invoke() = BaseAdapter(T::class)
    }

    override fun write(field: KProperty1<out Any, *>, fieldObject: Any, KSDB: KSDB, mappedObject: JSONObject) {
        mappedObject.put(field.name, TypeChecker.getType(type,field,fieldObject))
    }

    override fun read(field: Field, fieldObject: Any, KSDB: KSDB, result: JSONObject) {
        field.set(fieldObject,TypeChecker.getType(type,field.name,result))
    }

}

class ArrayAdapter: ITypeAdapter{

    override fun write(field: KProperty1<out Any, *>, fieldObject: Any, KSDB: KSDB, mappedObject: JSONObject) {
        field.isAccessible = true
        val array:Array<*> = field.getter.call(fieldObject) as Array<*>
        val jsonArray = JSONArray()
        array.forEach { jsonArray.put(it) }
        mappedObject.put(field.name,jsonArray)
    }

    override fun read(field: Field, fieldObject: Any, KSDB: KSDB, result: JSONObject) {
        val array = result.getJSONArray(field.name)
        val compType = field.type.componentType
        val newArray = java.lang.reflect.Array.newInstance(compType,array.length())
        for(i in 0 until array.length())
            java.lang.reflect.Array.set(newArray,i,array[i])
        val kField = fieldObject::class.declaredMemberProperties.find { it.name == field.name }
        kField?.isAccessible = true
        kField.takeIf { it is KMutableProperty<*> }
            ?.let { it as KMutableProperty<*> }
            ?.setter?.call(fieldObject,newArray)
    }
}