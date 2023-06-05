package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.CodeList
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CodeListRepository : MongoRepository<CodeList, String>{
    fun findCodeListsByCatalogId(catalogId: String): List<CodeList>
    fun findCodeListByIdAndCatalogId(id: String, catalogId: String): CodeList?
}
