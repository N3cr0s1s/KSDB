package me.necrosis.surrealdb.framework.component

import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.util.mapper.IAnnotationAdapter
import org.json.JSONObject
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Random(
    val randomType: RandomType,
    val min: Double = 0.0,
    val max: Double = 100.0,
    val length: Int = 15
)

enum class RandomType(val function: String){
    /**
     * Generates and returns a random floating point number
     */
    RAND    ("rand()"),

    /**
     * Generates and returns a random boolean
     */
    BOOL    ("rand::bool()"),

    /**
     * Generates and returns a random floating point number
     */
    FLOAT   ("rand::float()"),

    /**
     * Generates and returns a random floating point number, in a specific range
     */
    RFLOAT  ("rand::float(%min,%max)"),

    /**
     * Generates and returns a random guid
     */
    GUID    ("rand::guid()"),

    /**
     * Generates and returns a random guid, with length
     */
    LGUID   ("rand::guid(%length)"),

    /**
     * Generates and returns a random integer
     */
    INT     ("rand::int()"),

    /**
     * Generates and returns a random integer, in a specific range
     */
    RINT    ("rand::int(%min,%max)"),

    /**
     * Generates and returns a random string
     */
    STRING  ("rand::string()"),

    /**
     * Generates and returns a random string, with length
     */
    LSTRING ("rand::string(%length)"),

    /**
     * Generates and returns a random string, in a specific range
     */
    RSTRING ("rand::string(%min,%max)"),

    /**
     * Generates and returns a random datetime
     */
    TIME    ("rand::time()"),

    /**
     * Generates and returns a random datetime, in a specific range
     */
    RTIME   ("rand::time(%mix,%max)"),

    /**
     * Generates and returns a random UUID
     */
    UUID    ("rand::uuid()")
}

class RandomAdapter : IAnnotationAdapter {

    override fun process(field: KProperty1<out Any, *>, fieldObject: Any, KSDB: KSDB, mappedObject: JSONObject) {
        val annotation = field.javaField!!.getAnnotation(Random::class.java)
        val function =
            annotation.randomType.function
                .replace("%min",annotation.min.toString())
                .replace("%max",annotation.max.toString())
                .replace("%length",annotation.length.toString())
        KSDB.getSurrealDB().sql("SELECT * FROM $function").getSync {
            when(annotation.randomType){
                RandomType.RAND ->      field.javaField!!.set(fieldObject, it!!.get(0) as Float)
                RandomType.BOOL ->      field.javaField!!.set(fieldObject, it!!.get(0) as Boolean )
                RandomType.FLOAT ->     field.javaField!!.set(fieldObject, it!!.get(0) as Float )
                RandomType.RFLOAT ->    field.javaField!!.set(fieldObject, it!!.get(0) as Float )
                RandomType.GUID ->      field.javaField!!.set(fieldObject, it!!.get(0) as String )
                RandomType.LGUID ->     field.javaField!!.set(fieldObject, it!!.get(0) as String )
                RandomType.INT ->       field.javaField!!.set(fieldObject, it!!.get(0) as Int )
                RandomType.RINT ->      field.javaField!!.set(fieldObject, it!!.get(0) as Int )
                RandomType.STRING ->    field.javaField!!.set(fieldObject, it!!.get(0) as String )
                RandomType.LSTRING ->   field.javaField!!.set(fieldObject, it!!.get(0) as String )
                RandomType.RSTRING ->   field.javaField!!.set(fieldObject, it!!.get(0) as String )
                RandomType.TIME ->      field.javaField!!.set(fieldObject, it!!.get(0) as String )
                RandomType.RTIME ->     field.javaField!!.set(fieldObject, it!!.get(0) as String )
                RandomType.UUID ->      field.javaField!!.set(fieldObject, it!!.get(0) as String )
            }
        }
    }

}