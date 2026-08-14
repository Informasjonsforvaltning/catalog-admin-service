package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.Field
import no.digdir.catalog_admin_service.model.FieldType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InternalFieldsRepository : JpaRepository<Field, String> {
    fun findByCatalogId(catalogId: String): List<Field>

    fun findByIdAndCatalogId(id: String, catalogId: String): Field?

    fun findByCatalogIdAndTypeAndCodeListId(catalogId: String, type: FieldType, codeListId: String): List<Field>
}
