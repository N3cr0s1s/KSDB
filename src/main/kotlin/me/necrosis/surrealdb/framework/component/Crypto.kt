package me.necrosis.surrealdb.framework.component

import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.util.mapper.IAnnotationAdapter
import org.json.JSONObject
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Crypto(val crypto: CryptoType)

enum class CryptoType(val function: String){
    MD5     ("crypto::md5(%s)"),
    SHA1    ("crypto::sha1(%s)"),
    SHA256  ("crypto::sha256(%s)"),
    SHA512  ("crypto::sha512(%s)"),
    ARGON2  ("crypto::argon2::generate(%s)"),
    PBKDF2  ("crypto::pbkdf2::generate(%s)"),
    SCRYPT  ("crypto::scrypt::generate(%s)")
}

class CryptoAdapter : IAnnotationAdapter {

    override fun process(field: KProperty1<out Any, *>, fieldObject: Any, KSDB: KSDB, mappedObject: JSONObject) {
        val jField = field.javaField!!
        jField.isAccessible = true
        val annotation = jField.getAnnotation(Crypto::class.java)

        val function = annotation.crypto.function.replace("%s","'${jField.get(fieldObject)}'")
        KSDB.getSurrealDB().sql("SELECT * from $function").getSync {
            jField.set(fieldObject,it!!.getString(0))
        }
    }

}
