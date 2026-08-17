package no.digdir.catalogadmin.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

data class Fields(val editable: EditableFields, val internal: List<Field>)

@Entity
@Table(name = "internal_fields")
data class Field(
    @Id
    @Column(name = "id")
    val id: String,
    @Column(name = "catalog_id", nullable = false)
    val catalogId: String,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label", columnDefinition = "jsonb", nullable = false)
    val label: MultiLanguageTexts,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description", columnDefinition = "jsonb", nullable = false)
    val description: MultiLanguageTexts,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: FieldType,
    @Enumerated(EnumType.STRING)
    @Column(name = "location", nullable = false)
    val location: FieldLocation,
    @Column(name = "code_list_id")
    val codeListId: String?,
    @Column(name = "enable_filter")
    val enableFilter: Boolean?,
)

@Entity
@Table(name = "editable_fields")
data class EditableFields(
    @Id
    @Column(name = "catalog_id")
    val catalogId: String,
    @Column(name = "domain_code_list_id")
    val domainCodeListId: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FieldToBeCreated(
    val label: MultiLanguageTexts,
    val description: MultiLanguageTexts?,
    val type: FieldType?,
    val location: FieldLocation?,
    val codeListId: String?,
    val enableFilter: Boolean?,
)

enum class FieldType(private val value: String) {
    BOOLEAN("boolean"),
    TEXT_SHORT("text_short"),
    TEXT_LONG("text_long"),
    CODE_LIST("code_list"),
    USER_LIST("user_list"),
    ;

    @JsonValue
    fun jsonValue(): String = value
}

enum class FieldLocation(private val value: String) {
    MAIN_COLUMN("main_column"),
    RIGHT_COLUMN("right_column"),
    ;

    @JsonValue
    fun jsonValue(): String = value
}
