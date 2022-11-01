package me.necrosis.surrealdb.framework.component.table

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.necrosis.surrealdb.api.Record
import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.util.mapper.ObjectMapper
import me.necrosis.surrealdb.framework.util.query.Query
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

class TableHandler<T: Any>(
    val KSDB: KSDB,
    val table: Table,
    private val type: KClass<T>
) {

    companion object{
        /**
         * Custom operator, to create generic type class.
         */
        inline operator fun <reified T: Any> invoke(KSDB: KSDB,table: Table) = TableHandler(KSDB,table,T::class)
    }

    private val objectMapper = ObjectMapper(this.KSDB)
    private val idMatch = """[A-Za-z0-9]+""".toRegex()

    /**
     * Find [T] list, with filters.
     * Use [Query] object to filter data.
     *
     * @param where [Query] object
     * @return A new list with [T] values, or empty list, if not found anything.
     */
    fun find(where: Query) : CompletableFuture<List<T>> {
        val future = CompletableFuture<List<T>>()
        val entityList = mutableListOf<T>()
        this.KSDB.getSurrealDB().sql("SELECT * FROM ${table.tableName} WHERE ${where.get()};").get { jsonArray ->
            this.objectMapper.unmapObject(type,jsonArray!!,{list ->
                list.forEach{ entityList.add(it) }
                future.complete(entityList)
            })
        }
        return future
    }

    /**
     * Find [T] entity, where id equals [id].
     * Entity ID is unique, so the return value is one entity, or null.
     *
     * @param id    Entity ID
     * @return If record exist, return the entity object, otherwise null.
     */
    fun find(id: String) : CompletableFuture<T?> {
        val future = CompletableFuture<T?>()
        var finalId = id.replace("${table.tableName}:","")
        if(!idMatch.matches(finalId))
            finalId = "⟨$finalId⟩"
        KSDB.getSurrealDB().select(Record(table.tableName,finalId)).get { jsonArray ->
            if (jsonArray?.length()!! < 1) future.complete(null)
            this.objectMapper.unmapObject(type,jsonArray,{list ->
                list.forEach{ future.complete(it) }
            })
        }
        return future
    }

    /**
     * Create new entity to table.
     * Execute all adapter, change values to correct format.
     *
     * @param entity    Entity to save
     * @param wait      Wait for done, `default = false`
     * @param callback  Run callback with [entity], `default = {}`
     */
    fun create(
        entity: T,
        wait:Boolean = false,
        callback: (JSONObject)->Unit = {}
    ) = runBlocking{
        val job = launch {
            objectMapper.mapObject(entity) { json ->
                if (json.has("_id")) {
                    val id = json.getString("_id")
                    callback(json)
                    json.remove("_id")
                    KSDB.getSurrealDB().create(Record(table.tableName, id), json).get { }
                    return@mapObject
                }
                callback(json)
                KSDB.getSurrealDB().create(table.tableName, json).get { }
            }
        }
        if (wait) job.join()
    }

    /**
     * Create new entity to table, with **UNSAFE** cast!
     *
     * @see TableHandler.create
     * @see me.necrosis.surrealdb.framework.util.mapper.DefaultAdapter
     */
    fun createUnsafe(
        entity: Any,
        wait: Boolean = false,
        callback: (JSONObject)->Unit = {}
    ) = create(entity as T,wait,callback)

    /**
     * Create new entities to table.
     * Execute all adapter, and change values to correct format.
     *
     * @param entity    Entity list
     * @param wait      Wait for end, `default = false`
     * @param callback  Run callback with every [entity], `default = {}`
     */
    fun create(
        vararg entity: T,
        wait: Boolean = false,
        callback: (JSONObject) -> Unit = {}
    ) = entity.forEach { create(it,wait,callback) }

    /**
     * Update record.
     * This function prevent object mapper, to execute Annotation based functions,
     * and update the record, with the correct way.
     *
     * @param entity    Entity, type [T]
     * @param wait      Wait for end, `default = false`
     * @param callback  Run callback with [entity] [JSONObject], `default = {}`
     */
    fun update(
        entity: T,
        wait: Boolean = false,
        callback: (JSONObject) -> Unit = {}
    ) = runBlocking {
        val job = launch {
            objectMapper.mapObject(entity,true) { json ->
                if (json.has("_id")) {
                    val id = json.getString("_id")
                        .replace("${table.tableName}_","")
                    callback(json)
                    json.remove("_id")
                    KSDB.getSurrealDB().update(Record(table.tableName, id), json).get { }
                    return@mapObject
                }
                callback(json)
                KSDB.getSurrealDB()
                    .update(
                        Record(
                            table.tableName,
                            json.getString("id")
                                .replace("${table.tableName}:","")
                        ), json)
                    .get { }
            }
        }
        if (wait) job.join()
    }

    /**
     * Update records.
     * This function prevent object mapper, to execute Annotation based functions,
     * and update the record, with the correct way.
     *
     * @param entity    Entity array, type [T]
     * @param wait      Wait for end, 'default = false'
     * @param callback  Run callback with every [entity], `default = {}`
     */
    fun update(
        vararg entity:T,
        wait: Boolean = false,
        callback: (JSONObject) -> Unit = {}
    ) = runBlocking {
        val job = launch { entity.forEach { update(it,false,callback) } }
        if (wait) job.join()
    }

    /**
     * Delete table, and all records in it.
     *
     * @param wait      Wait for end, 'default = false'
     * @param callback  SurrealDB response, as [JSONArray]
     */
    fun deleteTable(
        wait: Boolean = false,
        callback: (JSONArray?) -> Unit = {}
    ) = runBlocking {
        val job = launch {
            KSDB.getSurrealDB()
                .sql("REMOVE table ${table.tableName};")
                .getSync { callback(it) }
        }
        if (wait) job.join()
    }

    /**
     * Delete record from table, where id equals [id].
     *
     * @param id        Record id
     * @param wait      Wait for end, 'default = false'
     * @param callback  SurrealDB response, as [JSONArray]
     */
    fun delete(
        id: String,
        wait: Boolean = false,
        callback: (JSONArray?) -> Unit) = runBlocking {
        val job = launch {
            KSDB.getSurrealDB()
                .delete(Record(table.tableName,id))
                .getSync { callback(it) }
        }
        if (wait) job.join()
    }

    /**
     * Delete records from table, where [where] case is true.
     *
     * @param where     [Query] object
     * @param wait      Wait for end, 'default = false'
     * @param callback  SurrealDB response, as [JSONArray]
     */
    fun delete(
        where: Query,
        wait: Boolean = false,
        callback: (JSONArray?) -> Unit = {}
    ) = runBlocking {
        val job = launch {
            KSDB.getSurrealDB()
                .sql("DELETE ${table.tableName} WHERE ${where.get()};")
                .getSync { callback(it) }
        }
        if (wait) job.join()
    }
}