package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.Logo
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LogoRepository : MongoRepository<Logo, String>
