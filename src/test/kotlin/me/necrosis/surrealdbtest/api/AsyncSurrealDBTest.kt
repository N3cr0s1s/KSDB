package me.necrosis.surrealdbtest.api

import me.necrosis.surrealdb.api.AsyncSurrealDB
import me.necrosis.surrealdb.api.Record
import me.necrosis.surrealdb.api.User
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class AsyncSurrealDBTest {

    private val surrealDB = AsyncSurrealDB("localhost:8000","test","test", User("root","root"))
    private val lock = CountDownLatch(1)

    @Order(0)
    @Test fun connectTest(){
        surrealDB.connect().get {
            assertTrue(it)
            lock.countDown()
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(1)
    @Test fun sqlTest(){
        surrealDB.connect().get {
            assertTrue(it)
            surrealDB.sql("INFO FOR DB;").get {
                assertTrue(it != null)
                lock.countDown()
            }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(1)
    @Test fun createTableTest(){
        surrealDB.connect().get {
            assertTrue(it)
            surrealDB.create("testTable", JSONObject("{name:\'testName\'}")).get { assertTrue(it != null) }
            surrealDB.create("testTable", JSONObject("{name:\'NameTest\'}")).get { assertTrue(it != null) }
            surrealDB.create("testTable", JSONObject("{name:\'testTest\'}")).get {
                assertTrue(it != null)
                lock.countDown()
            }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(1)
    @Test fun createRecordTest(){
        surrealDB.connect().get {
            assertTrue(it)
            surrealDB.create(
                Record("testTable","testID"),
                JSONObject("{name:\'testTest\'}"
                )).get {
                assertTrue(it != null)
                lock.countDown()
                }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(2)
    @Test fun selectTableTest(){
        surrealDB.connect().get {
            assertTrue(it)
            surrealDB.select("testTable").get {
                val expected = 4
                assertEquals(expected,it?.length())
                lock.countDown()
            }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(2)
    @Test fun selectRecordTest(){
        surrealDB.connect().get {
            assertTrue(it)
            surrealDB.select(Record("testTable","testID")).get {
                val result = it?.get(0)
                val expected = "{\"name\":\"testTest\",\"id\":\"testTable:testID\"}"
                assertEquals(expected,result.toString())
                lock.countDown()
            }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(3)
    @Test fun updateTest(){
        surrealDB.connect().get {
            assertTrue(it)
            surrealDB.update(Record("testTable","testID"), JSONObject("{name:\"nameName\"}")).get {
                surrealDB.select(Record("testTable","testID")).get {
                    val checkResult = it?.get(0)
                    val expected = "{\"name\":\"nameName\",\"id\":\"testTable:testID\"}"
                    assertEquals(expected,checkResult.toString())
                    lock.countDown()
                }
            }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(4)
    @Test fun modifyTest(){
        surrealDB.connect().get {
            assertTrue(it)
            surrealDB.update(Record("testTable","testID"), JSONObject("{age:16}")).get {
                surrealDB.select(Record("testTable","testID")).get {
                    val checkResult = it?.get(0)!!
                    val expected = "{\"id\":\"testTable:testID\",\"age\":16}"
                    assertEquals(expected, checkResult.toString())
                    lock.countDown()
                }
            }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(5)
    @Test fun deleteRecord(){
        surrealDB.connect().get { isConnected ->
            assertTrue(isConnected)

            val record = Record("testTable","testID")
            surrealDB.delete(record).get { deleteResult ->
                assertTrue(!deleteResult?.isEmpty!!)
                surrealDB.select(record).get { selectResult ->
                    assertTrue(selectResult?.isEmpty!!)
                    lock.countDown()
                }
            }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

    @Order(6)
    @Test fun deleteTable(){
        surrealDB.connect().get {
            assertTrue(it)
            surrealDB.sql("remove table testTable").get {
                assertTrue(it == null)
                lock.countDown()
            }
        }
        assertTrue(lock.await(5000, TimeUnit.MILLISECONDS))
    }

}
