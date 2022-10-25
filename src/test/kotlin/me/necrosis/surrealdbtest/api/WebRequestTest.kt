package me.necrosis.surrealdbtest.api

import me.necrosis.surrealdb.api.ConnectionException
import me.necrosis.surrealdb.api.SurrealDB
import me.necrosis.surrealdb.api.SyncSurrealDB
import me.necrosis.surrealdb.api.User
import me.necrosis.surrealdb.api.networking.WebRequest
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UserTest {

    /**
     * Test [User.encode] encode is correct.
     */
    @Test fun userBase64Test(){
        val user = User("root","root")
        val expected = "cm9vdDpyb290"
        val result:String = user.encode()
        assertEquals(expected,result)
    }

}

@TestMethodOrder(OrderAnnotation::class)
internal class WebRequestTest {

    private val surrealDB = SyncSurrealDB("localhost:8000","test","test", User("root","root"))

    /**
     * Test connection, without [SurrealDB]
     */
    @Test fun databaseConnectTest(){
        val user = User("root","root")
        val response =
            WebRequest("localhost:8000")
                .setDatabase("test","test")
                .setBasicAuthorization(user)
                .acceptType("application/json")
                .post("INFO FOR DB;")
        val expected = "OK"
        assertEquals(expected,response.getString("status"))
    }

    /**
     * Test connection is fail,
     * with correct exception
     * [ConnectionException]
     */
    @Test fun connectFailTest(){
        val surrealDB =
            SyncSurrealDB("http://testhost:8000/sql","test","test", User("root","root"))
        assertThrows<ConnectionException> {
            surrealDB.connect().get {}
        }
    }

    /**
     * Test connection, with [SurrealDB]
     */
    @Order(1)
    @Test fun connectTest(){
        surrealDB.connect().get { Assertions.assertTrue(it) }
    }

    @Order(2)
    @Test fun postTest(){
        surrealDB.connect().get { Assertions.assertTrue(it) }
        val expected = "OK"
        val result = this.surrealDB.web.post(JSONObject("{test:\'test\'}"),"testTable","testID")
        println(result)
        assertEquals(expected,result.getString("status"))
    }

    @Order(3)
    @Test fun getTableTest(){
        surrealDB.connect().get { Assertions.assertTrue(it) }
        val expectedStatus = "OK"
        val expectedResult = "{\"test\":\"test\",\"id\":\"testTable:testID\"}"
        val result = this.surrealDB.web.getTable("testTable")
        println(result)
        assertEquals(expectedStatus,result.getString("status"))
        assertEquals(expectedResult,result.getJSONArray("result").getJSONObject(0).toString())
    }

    @Order(3)
    @Test fun getRecordTest(){
        surrealDB.connect().get { Assertions.assertTrue(it) }
        val expectedResult = "{\"test\":\"test\",\"id\":\"testTable:testID\"}"
        val result = this.surrealDB.web.getRecord("testTable","testID")
        println(result)
        assertEquals(expectedResult,result.getJSONArray("result").getJSONObject(0).toString())
    }

    @Order(4)
    @Test fun deleteRecordTest(){
        surrealDB.connect().get { Assertions.assertTrue(it) }
        val expected = "OK"
        val result = this.surrealDB.web.deleteRecord("testTable","testID")
        println(result)
        assertEquals(expected,result.getString("status"))
    }

    @Order(5)
    @Test fun deleteTableTest(){
        surrealDB.connect().get { Assertions.assertTrue(it) }
        val expected = "OK"
        val result = this.surrealDB.web.deleteRecord("testTable")
        println(result)
        assertEquals(expected,result.getString("status"))
    }

    @Order(6)
    @Test fun sqlQueryTest(){
        surrealDB.connect().get { Assertions.assertTrue(it) }
        val expected = "OK"
        val result = this.surrealDB.web.post("REMOVE TABLE testTable;")
        println(result)
        assertEquals(expected,result.getString("status"))
    }

}