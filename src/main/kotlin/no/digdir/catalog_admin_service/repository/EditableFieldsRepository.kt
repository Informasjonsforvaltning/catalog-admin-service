package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.EditableFields
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EditableFieldsRepository : MongoRepository<EditableFields, String> {
    fun findByCatalogIdAndDomainCodeListId(catalogId: String, codeListId: String): EditableFields?
}
