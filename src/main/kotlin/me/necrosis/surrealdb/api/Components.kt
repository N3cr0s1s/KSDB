package me.necrosis.surrealdb.api

import java.util.*

data class Record(val table: String, val id: String)

class User(private val username:String, private val password:String){

    fun encode() : String{
        return Base64.getEncoder().encodeToString(("$username:$password").toByteArray())
    }

}