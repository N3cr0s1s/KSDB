package me.necrosis.surrealdb.framework.component

import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.util.mapper.IAnnotationAdapter
import org.json.JSONObject
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Session(val session: SessionType)

enum class SessionType(val function: String){
    DB      ("session::db()"),      //  Returns the currently selected database
    ID      ("session::id()"),      //  Returns the current user's session ID
    IP      ("session::ip()"),      //  Returns the current user's session IP address
    NS      ("session::ns()"),      //  Returns the currently selected namespace
    ORIGIN  ("session::origin()"),  //  Returns the current user's HTTP origin
    SC      ("session::sc()")       //  Returns the current user's authentication scope
}

class SessionAdapter : IAnnotationAdapter {

    override fun process(field: KProperty1<out Any, *>, fieldObject: Any, KSDB: KSDB, mappedObject: JSONObject) {
        val jField = field.javaField!!
        jField.isAccessible = true
        val annotation = jField.getAnnotation(Session::class.java)
        KSDB.getSurrealDB().sql("SELECT * FROM ${annotation.session.function}").getSync {
            jField.set(fieldObject,it!!.getString(0))
        }
    }

}