package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.DesignDBO
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DesignRepository : MongoRepository<DesignDBO, String>
