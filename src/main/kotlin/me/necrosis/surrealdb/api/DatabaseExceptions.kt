package me.necrosis.surrealdb.api

data class ConnectionException(override val message: String) : Exception(message)

data class InitializationException(override val message: String) : Exception(message)