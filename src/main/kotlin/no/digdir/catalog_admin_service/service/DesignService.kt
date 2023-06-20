package no.digdir.catalog_admin_service.service

import no.digdir.catalog_admin_service.model.DesignDBO
import no.digdir.catalog_admin_service.model.DesignDTO
import no.digdir.catalog_admin_service.model.Logo
import no.digdir.catalog_admin_service.repository.DesignRepository
import no.digdir.catalog_admin_service.repository.LogoRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DesignService(private val designRepository: DesignRepository, private val logoRepository: LogoRepository) {
    fun getDesign(catalogId: String): DesignDTO? =
        designRepository.findByIdOrNull(catalogId)?.let {
            DesignDTO(
                backgroundColor = it.backgroundColor,
                fontColor = it.fontColor,
                logoDescription = it.logoDescription
            )
        }

    fun updateDesign(catalogId: String, design: DesignDTO): DesignDBO? =
        designRepository.save(
            DesignDBO(
                backgroundColor = design.backgroundColor,
                fontColor = design.fontColor,
                logoDescription = design.logoDescription,
                catalogId = catalogId
            )
        )

    fun getLogo(catalogId: String): Logo? =
        logoRepository.findByIdOrNull(catalogId)

    fun deleteLogo(catalogId: String) =
        logoRepository.deleteById(catalogId)

    fun saveLogo(catalogId: String, logo: ByteArray): Logo? =
        logoRepository.save(Logo(logo = logo, catalogId = catalogId))
}
