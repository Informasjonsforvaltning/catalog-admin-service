package no.digdir.catalog_admin_service.utils

import no.digdir.catalog_admin_service.model.CodeList
import org.bson.Document
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017
const val MONGO_DATABASE = "catalogAdminService"
const val MONGO_COLLECTION = "codeLists"

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)

val CODE_LIST_0 = CodeList(id="123", name = "name")
fun codeListPopulation(): List<Document> =
    listOf(CODE_LIST_0)
        .map { it.mapDBO() }

private fun CodeList.mapDBO(): Document =
    Document()
        .append("_id", id)
        .append("name", name)
