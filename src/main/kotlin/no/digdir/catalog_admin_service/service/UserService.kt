package no.digdir.catalog_admin_service.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.User
import no.digdir.catalog_admin_service.model.UserToBeCreated
import no.digdir.catalog_admin_service.model.Users
import no.digdir.catalog_admin_service.repository.UserRepository

private val logger = LoggerFactory.getLogger(UserService::class.java)

@Service
class UserService(private val userRepository: UserRepository) {
    fun getUsers(catalogId: String): Users =
        Users(users = userRepository.findUsersByCatalogId(catalogId).sortedBy { it.name })

    fun getUserById(userId: String, catalogId: String): User? =
        userRepository.findUserByIdAndCatalogId(userId, catalogId)

    fun deleteUserById(userId: String) =
        try {
            userRepository.deleteById(userId)
        } catch (ex: Exception) {
            logger.error("Failed to delete user with id $userId", ex)
            throw ex
        }

    fun createUser(data: UserToBeCreated, catalogId: String): User =
        try {
            User(
                id = UUID.randomUUID().toString(),
                name = data.name,
                catalogId = catalogId,
                email = data.email,
                telephoneNumber = data.telephoneNumber
            ).let { userRepository.insert(it) }
        } catch (ex: Exception) {
            logger.error("Failed to create user for catalog $catalogId", ex)
            throw ex
        }

    fun updateUser(userId: String, catalogId: String, operations: List<JsonPatchOperation>): User? =
        try {
            userRepository.findUserByIdAndCatalogId(userId, catalogId)
                ?.let { dbUser -> patchOriginal(dbUser, operations) }
                ?.let { userRepository.save(it) }
        } catch (ex: Exception) {
            logger.error("Failed to update user with id $userId in catalog $catalogId", ex)
            throw ex
        }
}
