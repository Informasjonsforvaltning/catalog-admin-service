package no.digdir.catalogadmin.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class UNESKOS {
    companion object {
        const val URI = "http://purl.org/umu/uneskos#"

        val contains: Property = ResourceFactory.createProperty("${URI}contains")
    }
}
