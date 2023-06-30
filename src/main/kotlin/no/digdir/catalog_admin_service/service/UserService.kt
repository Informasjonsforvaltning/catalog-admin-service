package no.digdir.catalog_admin_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.User
import no.digdir.catalog_admin_service.model.UserToBeCreated
import no.digdir.catalog_admin_service.model.Users
import no.digdir.catalog_admin_service.repository.UserRepository




@Service
class UserService(private val userRepository: UserRepository) {
    fun getUsers(catalogId: String): Users =
        Users(users = userRepository.findUsersByCatalogId(catalogId))

    fun getUserById(userId: String, catalogId: String): User? =
        userRepository.findUserByUserIdAndCatalogId(userId, catalogId)

    fun deleteUserById(userId: String) =
        userRepository.deleteById(userId)

    fun createUser(data: UserToBeCreated, catalogId: String): User =
        User(
            userId = UUID.randomUUID().toString(),
            name = data.name,
            catalogId = catalogId,
            email = data.email,
            telephoneNumber = data.telephoneNumber
        ).let { userRepository.insert(it) }


    fun updateUser(userId: String, catalogId: String, operations: List<JsonPatchOperation>): User? =
        userRepository.findUserByUserIdAndCatalogId(userId, catalogId)
            ?.let { dbUser -> patchOriginal(dbUser, operations) }
            ?.let { userRepository.save(it) }
}
