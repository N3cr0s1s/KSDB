package me.necrosis.surrealdb.framework.component.table

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(val tableName: String)