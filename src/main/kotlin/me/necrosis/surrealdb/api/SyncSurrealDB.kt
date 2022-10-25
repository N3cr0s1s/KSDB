package me.necrosis.surrealdb.api

import me.necrosis.surrealdb.api.util.JsonUtil
import me.necrosis.surrealdb.api.util.SyncTask
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

open class SyncSurrealDB : SurrealDB{

    constructor(url:String,db:String,ns:String,user: User) : super(url,db,ns,user)

    constructor(url:String,db:String,ns:String,username:String,password:String) : super(url,db,ns,username,password)

    /**
     * Connect to database.
     */
    override fun connect() : SyncTask<Boolean> {
        return SyncTask{
            try {
                val response = this.web.setDatabase(this.db,this.ns)
                    .setBasicAuthorization(this.user)
                    .acceptType("application/json")
                    .post("INFO FOR DB;")
                if (response.getString("status").equals("OK"))
                    return@SyncTask true
            }catch (exception:Exception){
                throw ConnectionException("Failed to connect to database.")
            }
            return@SyncTask false
        }
    }

    private fun getResult(json : JSONObject) : SyncTask<JSONArray?> {
        return SyncTask {
            val result = JsonUtil.getNullKey(json, "result") ?: return@SyncTask null
            return@SyncTask try {
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
    override fun sql(query: String) : SyncTask<JSONArray?> = getResult(this.web.post(query))

    /**
     * Selects all records in a table from the database
     *
     * @param table Table name
     */
    override fun select(table: String) : SyncTask<JSONArray?> = getResult(this.web.getTable(table))

    /**
     * Selects the specific record from the database
     *
     * @param record Record in the database
     */
    override fun select(record: Record) : SyncTask<JSONArray?> =
        getResult(this.web.getRecord(record.table,record.id))

    /**
     * Creates a records in a table in the database
     * Table ID is randomly generating
     *
     * @param table Table
     * @param data  Data as [JSONObject]
     */
    override fun create(table: String,data: JSONObject) : SyncTask<JSONArray?> =
        getResult(this.web.post(data,table))

    /**
     * Creates a records in a table in the database
     *
     * @param record    Table record
     * @param data      Data as [JSONObject]
     */
    override fun create(record: Record, data: JSONObject) : SyncTask<JSONArray?> =
        getResult(this.web.post(data,record.table,record.id))

    /**
     * Deletes all records in a table from the database
     *
     * @param table Table whose records will be deleted
     */
    override fun delete(table: String) : SyncTask<JSONArray?> =
        getResult(this.web.deleteRecord(table))

    /**
     * Deletes the specified record from the database
     *
     * @param record Record which will deleted
     */
    override fun delete(record: Record) : SyncTask<JSONArray?> =
        getResult(this.web.deleteRecord(record))

    /**
     * Updates the specified record in the database
     *
     * @param record Record which will be updated
     * @param data   Updated data as [JSONObject]
     */
    override fun update(record: Record, data: JSONObject) : SyncTask<JSONArray?> =
        getResult(this.web.put(data,record))

    /**
     * Modifies the specified record in the database
     *
     * @param record Record which will be modified
     * @param data   Modified data as [JSONObject]
     */
    override fun modify(record: Record, data: JSONObject) : SyncTask<JSONArray?> =
        getResult(this.web.patch(data,record))

}