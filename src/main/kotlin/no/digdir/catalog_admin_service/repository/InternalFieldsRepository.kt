package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.Field
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface InternalFieldsRepository : MongoRepository<Field, String> {
    fun findFieldsByCatalogId(catalogId: String): List<Field>
}
