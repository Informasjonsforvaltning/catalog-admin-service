package no.digdir.catalog_admin_service.service

import com.github.bgalek.security.svg.SvgSecurityValidator
import no.digdir.catalog_admin_service.model.DesignDBO
import no.digdir.catalog_admin_service.model.DesignDTO
import no.digdir.catalog_admin_service.model.JsonPatchOperation
import no.digdir.catalog_admin_service.model.Logo
import no.digdir.catalog_admin_service.repository.DesignRepository
import no.digdir.catalog_admin_service.repository.LogoRepository
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.Base64
import javax.imageio.ImageIO


private val logger = LoggerFactory.getLogger(DesignService::class.java)

@Service
open class DesignService(private val designRepository: DesignRepository, private val logoRepository: LogoRepository) {

    private fun getDesignDBO(catalogId: String): DesignDBO =
        designRepository.findByIdOrNull(catalogId)
            ?: DesignDBO(catalogId, null, null, null)

    private fun DesignDBO.mapToDTO(): DesignDTO =
        DesignDTO(
            backgroundColor = backgroundColor,
            fontColor = fontColor,
            logoDescription = logoDescription,
            hasLogo = hasLogo
        )

    fun getDesign(catalogId: String): DesignDTO =
        getDesignDBO(catalogId).mapToDTO()

    fun updateDesign(catalogId: String, operations: List<JsonPatchOperation>): DesignDTO =
        try {
            patchOriginal(getDesignDBO(catalogId), operations)
                .let { designRepository.save(it) }
                .mapToDTO()
        } catch (ex: Exception) {
            logger.error("Failed to update design for catalog $catalogId", ex)
            throw ex
        }

    fun getLogo(catalogId: String): Logo? =
        logoRepository.findByIdOrNull(catalogId)

    fun deleteLogo(catalogId: String): DesignDBO =
        try {
            logoRepository.deleteById(catalogId)
            getDesignDBO(catalogId)
                .copy(hasLogo = false)
                .run { designRepository.save(this) }
        } catch (ex: Exception) {
            logger.error("Failed to delete logo for catalog $catalogId", ex)
            throw ex
        }

    open fun saveLogo(catalogId: String, logoFile: MultipartFile) {
        logger.info("uploading logo for $catalogId")
        val contentType = logoFile.contentType
        val bytes: ByteArray = logoFile.inputStream.readAllBytes()

        when (contentType) {
            MediaType.IMAGE_PNG_VALUE -> validatePNG(logoFile.inputStream)
            "image/svg+xml" -> validateSVG(bytes)
            else -> {
                logger.error("Logo content-type '${logoFile.contentType}' is not supported")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
        }

        logoRepository.save(
            Logo(
                base64Logo = Base64.getEncoder().encodeToString(bytes),
                contentType = contentType,
                catalogId = catalogId,
                filename = logoFile.originalFilename ?: "logo.${fileEndingFromContentType(contentType)}"
            )
        )

        getDesignDBO(catalogId)
            .copy(hasLogo = true)
            .run { designRepository.save(this) }
    }

    private fun fileEndingFromContentType(contentType: String): String =
        when {
            contentType.contains("svg") -> "svg"
            contentType.contains("png") -> "png"
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

    private fun validatePNG(inputStream: InputStream) {
        try {
            val bufferedImage = ImageIO.read(inputStream)
            if (bufferedImage == null) {
                logger.error("Uploaded file is not an image")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
        } catch (ex: Exception) {
            logger.error("Exception when validating png", ex)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    private fun validateSVG(bytes: ByteArray) {
        try {
            val result = SvgSecurityValidator.builder().build().validate(bytes)
            if (result.hasViolations()) {
                logger.error("Uploaded svg has violations ${result.offendingElements}")
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
        } catch (ex: Exception) {
            logger.error("Exception when validating svg", ex)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

}

fun Logo.inputStreamResource(): InputStreamResource =
    InputStreamResource(
        ByteArrayInputStream(
            Base64.getDecoder().decode(base64Logo)
        )
    )
