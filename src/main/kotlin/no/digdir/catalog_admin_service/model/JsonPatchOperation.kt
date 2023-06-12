package no.digdir.catalog_admin_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonPatchOperation (
    val op: OpEnum,
    val path: String,
    val value: Any? = null,
    val from: String? = null
)

enum class OpEnum(val value: String) {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test");

    @JsonValue
    fun jsonValue(): String = value
}
