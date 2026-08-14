package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.CodeList
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CodeListRepository : JpaRepository<CodeList, String> {
    fun findCodeListsByCatalogId(catalogId: String): List<CodeList>

    fun findCodeListByIdAndCatalogId(id: String, catalogId: String): CodeList?
}
