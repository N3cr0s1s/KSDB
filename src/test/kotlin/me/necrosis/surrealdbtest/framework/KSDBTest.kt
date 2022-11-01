package me.necrosis.surrealdbtest.framework

import me.necrosis.surrealdb.api.SyncSurrealDB
import me.necrosis.surrealdb.api.User
import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.util.query.Query
import me.necrosis.surrealdb.framework.util.query.QueryField
import me.necrosis.surrealdb.framework.util.query.QueryType
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.*
import kotlin.test.assertEquals

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class KSDBTest {

    val KSDB = KSDB(SyncSurrealDB("localhost:8000","test","test", User("root","root")))
    val tableHandler = KSDB.get<TestTable>()

    @Test
    fun mathTest(){
        val array = arrayOf(10,1,5,8,9,6,5,1,7)
        var id = ""
        KSDB.get<MathTest>().create(MathTest(array),wait=true){
            id = it.getString("_id")
        }
        val math = KSDB.get<MathTest>().find(id).get()
        println(array.contentToString())
        println("Array max: ${math?.mathArrayMax} ; Array min: ${math?.mathArrayMin}")
        println("3.14 ceil: ${math?.ceilValue}")
    }

    @Test @Order(0)
    fun createTest(){
        val testTable = TestTable("Username","VeryStrongPassword!","username@email.com","now",
            TestRelation("Username","HashedValue")
        )
        tableHandler.create(testTable, wait = true)
    }

    @Test @Order(1)
    fun relationTest(){
        val completableFuture = tableHandler.find(
            Query().add(QueryField(QueryType.EQUALS,"username","Username"))
        )
        val result = completableFuture.get()
        var relation: TestRelation? = null
        result.forEach { relation = it.testRelation!! }
        val expected = "Username"
        assertEquals(expected,relation?.name)
    }

    @Test @Order(2)
    fun findTest(){
        val completableFuture = tableHandler.find(
            Query().add(QueryField(QueryType.EQUALS,"username","Username"))
        )
        val result = completableFuture.get()
        val expected = 1
        assertEquals(expected,result.size)
    }

    @Test @Order(3)
    fun updateTest(){
        val completableFuture = tableHandler.find(
            Query().add(QueryField(QueryType.EQUALS,"username","Username"))
        )
        val result = completableFuture.get()
        result.forEach {
            it.username = "New Username"
            tableHandler.update(it, wait = true)
        }

        val newEntity = tableHandler.find(
            Query().add(QueryField(QueryType.EQUALS,"username","New Username"))
        ).get()[0]

        val expected = "New Username"
        assertEquals(expected,newEntity.username)
    }

    @Test @Order(4)
    fun deleteTest(){
        tableHandler.deleteTable{}
        KSDB.get<MathTest>().deleteTable {}
    }

}
