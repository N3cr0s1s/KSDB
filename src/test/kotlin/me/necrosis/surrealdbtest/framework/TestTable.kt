package me.necrosis.surrealdbtest.framework

import me.necrosis.surrealdb.framework.component.*
import me.necrosis.surrealdb.framework.component.table.Table

@Table("testTable")
class TestTable{

    @Random(
        RandomType.RSTRING,
        min = 10.0,
        max = 50.0
    )
    @Id
    var id: String? = null
    var username: String? = null
    @Crypto(CryptoType.SHA512)
    var password: String? = null
    var email: String? = null
    var createdAt: String? = null

    var testRelation: TestRelation? = null

    constructor(
        username: String?,
        password: String?,
        email: String?,
        createdAt: String?,
        testRelation: TestRelation
    ){
        this.username = username
        this.password = password
        this.email = email
        this.createdAt = createdAt
        this.testRelation = testRelation
    }

    constructor()

    override fun toString(): String {
        return """$username:$password"""
    }
}

@Table("testRelation")
class TestRelation{

    @Random(RandomType.RSTRING, min = 10.0, max = 50.0)
    @Id
    var id: String? = null
    var name: String? = null
    @Crypto(CryptoType.SHA256)
    var hash: String? = null

    constructor()

    constructor(name: String,hash: String){
        this.name = name
        this.hash = hash
    }

}