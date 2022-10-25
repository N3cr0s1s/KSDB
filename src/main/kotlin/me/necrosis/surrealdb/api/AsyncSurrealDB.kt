package me.necrosis.surrealdb.api

import me.necrosis.surrealdb.api.util.AsyncTask
import me.necrosis.surrealdb.api.util.JsonUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

open class AsyncSurrealDB : SurrealDB {

    constructor(url:String,db:String,ns:String,user: User) : super(url,db,ns,user)

    constructor(url:String,db:String,ns:String,username:String,password:String) : super(url,db,ns,username,password)

    /**
     * Connect to database.
     */
    override fun connect() : AsyncTask<Boolean> {
        return AsyncTask{
            try {
                val response = this.web.setDatabase(this.db,this.ns)
                    .setBasicAuthorization(this.user)
                    .acceptType("application/json")
                    .post("INFO FOR DB;")
                if (response.getString("status").equals("OK"))
                    return@AsyncTask true
            }catch (exception:Exception){
                throw ConnectionException("Failed to connect to database.")
            }
            return@AsyncTask false
        }
    }

    private fun getResult(json : JSONObject) : AsyncTask<JSONArray?> {
        return AsyncTask {
            val result = JsonUtil.getNullKey(json, "result") ?: return@AsyncTask null
            return@AsyncTask try {
                JSONArray(result)
            } catch (exception: JSONException) {
                JSONArray("[$result]")
            }
        }
    }

    /**
     * Allows custom SurrealQL queries
     *
     * @param query SurrealQL Query
     */
    override fun sql(query: String) : AsyncTask<JSONArray?> = getResult(this.web.post(query))

    /**
     * Selects all records in a table from the database
     *
     * @param table Table name
     */
    override fun select(table: String) : AsyncTask<JSONArray?> = getResult(this.web.getTable(table))

    /**
     * Selects the specific record from the database
     *
     * @param record Record in the database
     */
    override fun select(record: Record) : AsyncTask<JSONArray?> =
        getResult(this.web.getRecord(record.table,record.id))

    /**
     * Creates a records in a table in the database
     * Table ID is randomly generating
     *
     * @param table Table
     * @param data  Data as [JSONObject]
     */
    override fun create(table: String,data: JSONObject) : AsyncTask<JSONArray?> =
        getResult(this.web.post(data,table))

    /**
     * Creates a records in a table in the database
     *
     * @param record    Table record
     * @param data      Data as [JSONObject]
     */
    override fun create(record: Record, data: JSONObject) : AsyncTask<JSONArray?> =
        getResult(this.web.post(data,record.table,record.id))

    /**
     * Deletes all records in a table from the database
     *
     * @param table Table whose records will be deleted
     */
    override fun delete(table: String) : AsyncTask<JSONArray?> =
        getResult(this.web.deleteRecord(table))

    /**
     * Deletes the specified record from the database
     *
     * @param record Record which will deleted
     */
    override fun delete(record: Record) : AsyncTask<JSONArray?> =
        getResult(this.web.deleteRecord(record))

    /**
     * Updates the specified record in the database
     *
     * @param record Record which will be updated
     * @param data   Updated data as [JSONObject]
     */
    override fun update(record: Record, data: JSONObject) : AsyncTask<JSONArray?> =
        getResult(this.web.put(data,record))

    /**
     * Modifies the specified record in the database
     *
     * @param record Record which will be modified
     * @param data   Modified data as [JSONObject]
     */
    override fun modify(record: Record, data: JSONObject) : AsyncTask<JSONArray?> =
        getResult(this.web.patch(data,record))

}