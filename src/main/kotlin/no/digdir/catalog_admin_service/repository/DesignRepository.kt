package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.DesignDBO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DesignRepository : JpaRepository<DesignDBO, String>
