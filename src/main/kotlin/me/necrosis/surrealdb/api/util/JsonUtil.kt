package me.necrosis.surrealdb.api.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class JsonUtil{

    companion object {

        /**
         * Parse [me.necrosis.surrealdb.networking.WebRequest] response,
         * to [JSONObject] format.
         *
         * @param response  Database response
         */
        fun responseParser(response: String) : JSONObject {
            if (response.startsWith('[') && response.endsWith(']'))
                return JSONObject(response.substring(1,response.length-1))
            return JSONObject(response)
        }

        fun getNullKey(json: JSONObject, key: String) : String? {
            if (json.isNull(key))
                return null
            return try{
                json.get(key).toString()
            } catch (exception: JSONException){
                null
            }
        }

    }

}