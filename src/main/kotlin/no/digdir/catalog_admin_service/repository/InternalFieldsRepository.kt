package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.Field
import no.digdir.catalog_admin_service.model.FieldType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface InternalFieldsRepository : MongoRepository<Field, String> {
    fun findByCatalogId(catalogId: String): List<Field>
    fun findByIdAndCatalogId(id: String, catalogId: String): Field?
    fun findByCatalogIdAndTypeAndCodeListId(catalogId: String, type: FieldType, codeListId: String): List<Field>
}
