package me.necrosis.surrealdb.framework.component

import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.util.mapper.IAnnotationAdapter
import org.json.JSONObject
import kotlin.reflect.KProperty1

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Id

class IdAdapter : IAnnotationAdapter {

    override fun process(field: KProperty1<out Any, *>, fieldObject: Any, KSDB: KSDB, mappedObject: JSONObject) {
        val result: String = field.getter.call(fieldObject).toString().replace(" ","_")
        var checked = ""
        val regex = """[A-Za-z0-9_]+""".toRegex()
        result.forEach {
            checked = if(regex.matches(it.toString()))
                "$checked$it"
            else "${checked}_"
        }
        mappedObject.put("_id",checked)
    }

}