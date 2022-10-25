package me.necrosis.surrealdbtest.framework.util.query

import me.necrosis.surrealdb.api.AsyncSurrealDB
import me.necrosis.surrealdb.api.User
import me.necrosis.surrealdb.framework.KSDB
import me.necrosis.surrealdb.framework.util.query.Query
import me.necrosis.surrealdb.framework.util.query.QueryField
import me.necrosis.surrealdb.framework.util.query.QueryType
import me.necrosis.surrealdbtest.framework.TestTable
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.assertEquals

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class QueryTest {

    @Test
    fun queryTest(){
        val KSDB = KSDB(AsyncSurrealDB("localhost:8000","test","test", User("root","root")))
        KSDB.get<TestTable>()
            .find(
                Query().add(QueryField(QueryType.EQUALS,"username","Necrosis")))
        val query = Query()
        val request = query.add(
            QueryField(QueryType.EQUALS,"name","Michael",
                QueryField(
                    QueryType.EQUALS,"name","Thomas"
                )
            ),
            QueryField(QueryType.GREATER,"age",16)
        )
        val expected = "(name = \"Michael\" OR name = \"Thomas\") AND age > 16 "
        assertEquals(expected,request.get())
    }

}