package me.necrosis.surrealdb.api.networking

import me.necrosis.surrealdb.api.SurrealDB
import me.necrosis.surrealdb.api.User
import me.necrosis.surrealdb.api.util.JsonUtil
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class WebRequest {

    private val url:String
    private val httpClient: HttpClient
    private val httpRequestBuilder: HttpRequest.Builder

    constructor(url: String){
        this.url = url
        this.httpClient = HttpClient.newHttpClient()
        this.httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create("http://$url/sql"))
    }

    constructor(surrealDB: SurrealDB){
        this.url = surrealDB.url
        this.httpClient = HttpClient.newHttpClient()
        this.httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create("http://${this.url}/sql"))
        this.acceptType("application/json")
            .setBasicAuthorization(surrealDB.user)
            .setDatabase(surrealDB.db,surrealDB.ns)
    }

    fun setBasicAuthorization(user: User) : WebRequest {
        this.httpRequestBuilder
            .header("Authorization","Basic ${user.encode()}")
        return this
    }

    fun setDatabase(db: String, ns: String) : WebRequest {
        this.httpRequestBuilder
            .setHeader("DB",db)
            .setHeader("NS",ns)
        return this
    }

    @Deprecated("The Content-Type: application/json is not needed anymore. Use \"WebRequest.acceptType(contentType: String)\" instead.")
    fun setContentType(contentType: String) : WebRequest {
        this.httpRequestBuilder
            .setHeader("Content-Type",contentType)
        return this
    }

    fun acceptType(contentType: String) : WebRequest {
        this.httpRequestBuilder
            .setHeader("Accept",contentType)
        return this
    }

    /**
     * Get data from SurrealDB, on [endpoint]
     *
     * @param endpoint  SurrealDB endpoint, where the get request will be sent
     */
    fun get(endpoint: String) : JSONObject {
        val request =
            this.httpRequestBuilder
                .uri(URI.create("http://" + this.url.replace("/sql","") + "/" + endpoint))
                .GET()
        val response = this.httpClient
            .send(
                request.build(),
                HttpResponse.BodyHandlers.ofString()
            ).body()
        return JsonUtil.responseParser(response)
    }

    /**
     * Get all record from SurrealDB [table].
     *
     * @param table The data will come from this table
     */
    fun getTable(table: String) : JSONObject =
        this.get("key/$table")

    /**
     * Get record from SurrealDB [table].
     *
     * @param table The record will come from this table
     * @param id    Record id, in the [table]
     */
    fun getRecord(table: String, id: String) : JSONObject =
        this.get("key/$table/$id")

    /**
     * Post [data] to SurrealDB, on [endpoint]
     *
     * @param data      Data, to post
     * @param endpoint  SurrealDB endpoint, where the post request will be sent
     */
    fun post(data: String, endpoint: String) : JSONObject {
        val request = this.httpRequestBuilder
            .uri(URI.create("http://" + this.url.replace("/sql","") + "/" + endpoint))
            .POST(HttpRequest.BodyPublishers.ofString(data))
        val response = this.httpClient
            .send(
                request.build(),
                HttpResponse.BodyHandlers.ofString()
            ).body()
        return JsonUtil.responseParser(response)
    }

    /**
     * SurrealQL query
     *
     * @param data  SurrealQL query request data
     */
    fun post(data: String) : JSONObject =
        this.post(data,"sql")

    /**
     * Post [JSONObject] to table, with randomly generated id.
     *
     * @param data  Content as [JSONObject]
     * @param table Table, where the data will be saved
     */
    fun post(data: JSONObject,table: String) : JSONObject =
        this.post(data.toString(),"key/$table")

    /**
     * Post [JSONObject] to table, with id
     *
     * @param data  Content as [JSONObject]
     * @param table Table, where the data will be saved
     * @param id    Data id, table index, like: [table]:[id]
     */
    fun post(data: JSONObject,table: String, id: String) : JSONObject = this.post(data.toString(),"key/$table/$id")

    /**
     * Delete data in SurrealDB.
     *
     * For example, [endpoint] follows the following format:
     *  ```
     *  key/:table       Deletes all records in a table from the database
     *  key/:table/:id   Deletes the specified record from the database
     *  ```
     *
     *  @param endpoint  SurrealDB endpoint, where the delete request will be sent
     */
    fun delete(endpoint: String) : JSONObject {
        val request =
            this.httpRequestBuilder
                .uri(URI.create("http://" + this.url.replace("/sql","") + "/" + endpoint))
                .DELETE()
        val response = this.httpClient
            .send(
                request.build(),
                HttpResponse.BodyHandlers.ofString()
            ).body()
        return JsonUtil.responseParser(response)
    }

    /**
     * Deletes all records in a table from the database
     *
     * @param table Table, where data will delete
     */
    fun deleteRecord(table: String) : JSONObject =
        this.delete("key/$table")

    /**
     * Deletes the specified record from the database
     *
     * @param table Table, where data will delete
     * @param id    Record index, point to record, will be deleted
     */
    fun deleteRecord(table: String, id: String) : JSONObject =
        this.delete("key/$table/$id")

    /**
     * Deletes the specified record from the database
     *
     * @param record    Record, which will deleted
     */
    fun deleteRecord(record: me.necrosis.surrealdb.api.Record) : JSONObject =
        this.delete("key/${record.table}/${record.id}")

    /**
     * Put [data] to [endpoint]
     *
     * @param data      Data which will be put
     * @param endpoint  Where the data will be put
     */
    fun put(data: String,endpoint: String) : JSONObject {
        val request =
            this.httpRequestBuilder
                .uri(URI.create("http://" + this.url.replace("/sql","") + "/" + endpoint))
                .PUT(HttpRequest.BodyPublishers.ofString(data))
        val response = this.httpClient
            .send(
                request.build(),
                HttpResponse.BodyHandlers.ofString()
            ).body()
        return JsonUtil.responseParser(response)
    }

    /**
     * Updates the specified [record] in the database.
     *
     * @param data      Data in [JSONObject] format.
     * @param record    Database [me.necrosis.surrealdb.api.Record]
     */
    fun put(data: JSONObject, record: me.necrosis.surrealdb.api.Record) : JSONObject =
        this.put(data.toString(),"key/${record.table}/${record.id}")

    /**
     * Updates the specified record in the database.
     *
     * @param data      Data in [JSONObject] format.
     * @param table     Table, where the record is
     * @param id        Record index, point to record, will be deleted
     */
    fun put(data: JSONObject, table: String, id: String) : JSONObject =
        this.put(data.toString(),"key/${table}/${id}")

    fun patch(data: String, endpoint: String) : JSONObject {
        val request =
            this.httpRequestBuilder
                .uri(URI.create("http://" + this.url.replace("/sql","") + "/" + endpoint))
                .method("PATCH",HttpRequest.BodyPublishers.ofString(data))
        val response = this.httpClient
            .send(
                request.build(),
                HttpResponse.BodyHandlers.ofString()
            ).body()
        return JsonUtil.responseParser(response)
    }

    fun patch(data: JSONObject, table: String, id: String) : JSONObject =
        this.patch(data.toString(),"key/$table/$id")

    fun patch(data: JSONObject, record: me.necrosis.surrealdb.api.Record) : JSONObject =
        this.patch(data.toString(), "key/${record.table}/${record.id}")
}