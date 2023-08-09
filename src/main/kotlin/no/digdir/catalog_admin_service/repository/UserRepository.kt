package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: MongoRepository<User, String>  {
    fun findUsersByCatalogId(catalogId: String): List<User>
    fun findUserByIdAndCatalogId(id: String, catalogId: String): User?
}
