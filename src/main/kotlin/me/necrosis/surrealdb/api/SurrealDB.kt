package me.necrosis.surrealdb.api

import me.necrosis.surrealdb.api.networking.WebRequest
import me.necrosis.surrealdb.api.util.JsonUtil
import me.necrosis.surrealdb.api.util.Task
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import org.json.JSONObject

abstract class SurrealDB {

    val url:String
    val db:String
    val ns:String
    val user: User

    val web: WebRequest;

    constructor(url:String,db:String,ns:String,user: User){
        this.url = url
        this.db = db
        this.ns = ns
        this.user = user
        this.web = WebRequest(this.url)
    }

    constructor(url:String,db:String,ns:String,username:String,password:String){
        this.url = url
        this.db = db
        this.ns = ns
        this.user = User(username,password)
        this.web = WebRequest(this.url)
    }

    /**
     * Connect to database.
     */
    abstract fun connect() : Task<Boolean>

    /**
     * Allows custom SurrealQL queries
     *
     * @param query SurrealQL Query
     */
    abstract fun sql(query: String) : Task<JSONArray?>

    /**
     * Selects all records in a table from the database
     *
     * @param table Table name
     */
    abstract fun select(table: String) : Task<JSONArray?>

    /**
     * Selects the specific record from the database
     *
     * @param record Record in the database
     */
    abstract fun select(record: Record) : Task<JSONArray?>

    /**
     * Creates a records in a table in the database
     * Table ID is randomly generating
     *
     * @param table Table
     * @param data  Data as [JSONObject]
     */
    abstract fun create(table: String,data: JSONObject) : Task<JSONArray?>

    /**
     * Creates a records in a table in the database
     *
     * @param record    Table record
     * @param data      Data as [JSONObject]
     */
    abstract fun create(record: Record, data: JSONObject) : Task<JSONArray?>

    /**
     * Deletes all records in a table from the database
     *
     * @param table Table whose records will be deleted
     */
    abstract fun delete(table: String) : Task<JSONArray?>

    /**
     * Deletes the specified record from the database
     *
     * @param record Record which will deleted
     */
    abstract fun delete(record: Record) : Task<JSONArray?>

    /**
     * Updates the specified record in the database
     *
     * @param record Record which will be updated
     * @param data   Updated data as [JSONObject]
     */
    abstract fun update(record: Record, data: JSONObject) : Task<JSONArray?>

    /**
     * Modifies the specified record in the database
     *
     * @param record Record which will be modified
     * @param data   Modified data as [JSONObject]
     */
    abstract fun modify(record: Record, data: JSONObject) : Task<JSONArray?>

}