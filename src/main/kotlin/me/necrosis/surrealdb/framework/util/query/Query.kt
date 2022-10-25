package me.necrosis.surrealdb.framework.util.query

import org.json.JSONArray
import org.json.JSONObject

/**
 * Query builder
 */
class Query{

    private var request = ""

    fun add(field: QueryField) : Query{
        request += "AND ${field.toRequest()} "
        if (request.startsWith("AND "))
            request = request.substring(4)
        return this
    }

    fun add(vararg fields: QueryField) : Query{
        fields.forEach { this.add(it) }
        return this
    }

    fun get() : String = this.request

}

class QueryField(
    private val type: QueryType,
    private val field: String,
    private val value: Any,
    private var or: QueryField? = null
){

    fun toJSONObject() : JSONObject {
        val json = JSONObject()
        json.put("type",type.syntax)
        json.put("field",field)
        json.put("value",value)
        if (or != null) json.put("or",or!!.toJSONObject())
        return json
    }

    fun toRequest() : String {
        if (value is Number){
            if (or != null) return "(${field} ${type.syntax} ${value} OR ${or!!.toRequest()})"
            return "${field} ${type.syntax} ${value}"
        }
        if (or != null) return "(${field} ${type.syntax} \"${value}\" OR ${or!!.toRequest()})"
        return "${field} ${type.syntax} \"${value}\""
    }

}

enum class QueryType(
    val syntax: String
){
    EQUALS              ("="),
    NOT_EQUALS          ("!="),
    GREATER             (">"),
    GREATER_OR_EQUALS   (">="),
    LESS                ("<"),
    LESS_OR_EQUALS      ("<=")
}