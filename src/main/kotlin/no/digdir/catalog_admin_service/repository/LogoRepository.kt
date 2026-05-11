package no.digdir.catalog_admin_service.repository

import no.digdir.catalog_admin_service.model.Logo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LogoRepository : JpaRepository<Logo, String>
