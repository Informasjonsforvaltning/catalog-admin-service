package no.digdir.catalogadmin.repository

import no.digdir.catalogadmin.model.EditableFields
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EditableFieldsRepository : JpaRepository<EditableFields, String>
