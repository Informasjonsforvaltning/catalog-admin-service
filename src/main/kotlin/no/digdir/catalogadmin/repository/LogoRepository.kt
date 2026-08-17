package no.digdir.catalogadmin.repository

import no.digdir.catalogadmin.model.Logo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LogoRepository : JpaRepository<Logo, String>
