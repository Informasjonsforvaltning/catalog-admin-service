package no.digdir.catalog_admin_service.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class UNESKOS {
    companion object {
        const val uri = "http://purl.org/umu/uneskos#"

        val contains: Property = ResourceFactory.createProperty( "${uri}contains")
    }
}
