package me.necrosis.surrealdb.framework.component

import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.util.mapper.IAnnotationAdapter
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Math(
    val type: MathType,
    val fixed: Int = 2,
    val arrayFieldName: String = ""
)

enum class MathType(val function: String){
    ABS("math::abs(%number)"),
    CEIL("math::ceil(%number)"),
    FIXED("math::fixed(%number,%fixed)"),
    FLOOR("math::floor(%number)"),
    ARR_MAX("math::max(%array)"),
    ARR_MEAN("math::mean(%array)"),
    ARR_MEDIAN("math::median(%array)"),
    ARR_MIN("math::min(%array)"),
    ARR_PRODUCT("math::product(%array)"),
    ROUND("math::round(%number)"),
    SQRT("math::sqrt(%number)"),
    ARR_SUM("math::sum(%array)")
}

class MathAdapter : IAnnotationAdapter{

    override fun process(field: KProperty1<out Any, *>, fieldObject: Any, KSDB: KSDB, mappedObject: JSONObject) {
        val math = field.javaField!!.getAnnotation(Math::class.java)
        field.isAccessible = true
        var array = ""
        if (math.arrayFieldName != ""){
            val arrayField = try{
                fieldObject::class.java.getDeclaredField(math.arrayFieldName)
            }catch (e:Exception){
                throw RuntimeException("Array field name is invalid! {\"fieldName\":\"${math.arrayFieldName}\",\"inClass\":\"${fieldObject::class.qualifiedName}\"}")
            }
            arrayField.isAccessible = true
            val result = arrayField.get(fieldObject)
            if (result is Array<*>)
                array = Arrays.toString(result)
        }
        var request = "SELECT * FROM "
        request += if (math.type.toString().startsWith("ARR_")){
            if (array == "") throw RuntimeException("Array field name is required, to use \"ARR_\" functions! {\"fieldName\":\"${math.arrayFieldName}\",\"inClass\":\"${fieldObject::class.qualifiedName}\"}")
            math.type.function
                .replace("%array",array)
        }else{
            math.type.function
                .replace("%number",field.getter.call(fieldObject).toString())
                .replace("%fixed",math.fixed.toString())
        }
        KSDB.getSurrealDB().sql(request).getSync {
            if (it == null) throw RuntimeException("SurrealDB not responding.")
            if (it.length() < 1) return@getSync
            val value: String = it.get(0).toString()
            when(field.javaField!!.type.simpleName.lowercase(Locale.getDefault())){
                "boolean" ->  field.javaField!!.set(fieldObject,value.toBoolean())
                "float" -> field.javaField!!.set(fieldObject,value.toFloat())
                "double" -> field.javaField!!.set(fieldObject,value.toDouble())
                "byte" -> field.javaField!!.set(fieldObject,value.toByte())
                "short" -> field.javaField!!.set(fieldObject,value.toShort())
                "int","integer" -> field.javaField!!.set(fieldObject,value.toInt())
                "long" -> field.javaField!!.set(fieldObject,value.toLong())
                else -> throw RuntimeException("Unsupported field type, type: ${field.javaField!!.type.simpleName}")
            }
        }
    }

}
