package no.digdir.catalogadmin.repository

import no.digdir.catalogadmin.model.DesignDBO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DesignRepository : JpaRepository<DesignDBO, String>
