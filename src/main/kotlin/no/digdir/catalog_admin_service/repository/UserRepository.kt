package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findUsersByCatalogId(catalogId: String): List<User>
    fun findUserByIdAndCatalogId(id: String, catalogId: String): User?
}
