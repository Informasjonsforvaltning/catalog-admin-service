package no.digdir.catalog_admin_service.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "code_lists")
data class CodeList(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "catalog_id", nullable = false)
    val catalogId: String,

    @Column(name = "description", nullable = false)
    val description: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "codes", columnDefinition = "jsonb", nullable = false)
    val codes: List<Code>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CodeListToBeCreated(
    val name: String,
    val description: String,
    val codes: List<Code>
)
