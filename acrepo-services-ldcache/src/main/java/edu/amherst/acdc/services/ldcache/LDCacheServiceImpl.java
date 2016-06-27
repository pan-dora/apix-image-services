/*
 * Copyright 2016 Amherst College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.amherst.acdc.services.ldcache;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.List;

import com.github.jsonldjava.sesame.SesameJSONLDParserFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.backend.file.LDCachingFileBackend;
import org.apache.marmotta.ldcache.model.CacheConfiguration;
import org.apache.marmotta.ldcache.services.LDCache;
import org.apache.marmotta.ldclient.endpoint.rdf.SPARQLEndpoint;
import org.apache.marmotta.ldclient.endpoint.rdf.LinkedDataEndpoint;
import org.apache.marmotta.ldclient.provider.rdf.LinkedDataProvider;
import org.apache.marmotta.ldclient.provider.rdf.CacheProvider;
import org.apache.marmotta.ldclient.provider.rdf.RegexUriProvider;
import org.apache.marmotta.ldclient.provider.rdf.SPARQLProvider;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.openrdf.query.resultio.BooleanQueryResultParserRegistry;
import org.openrdf.query.resultio.TupleQueryResultParserRegistry;
import org.openrdf.query.resultio.sparqlxml.SPARQLBooleanXMLParserFactory;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.n3.N3ParserFactory;
import org.openrdf.rio.ntriples.NTriplesParserFactory;
import org.openrdf.rio.rdfjson.RDFJSONParserFactory;
import org.openrdf.rio.rdfxml.RDFXMLParserFactory;
import org.openrdf.rio.trig.TriGParserFactory;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.semarglproject.sesame.rdf.rdfa.SesameRDFaParserFactory;
import org.slf4j.Logger;

/**
 * @author acoburn
 * @since 6/21/16
 */
public class LDCacheServiceImpl implements LDCacheService {

    private long timeout;
    private LDCache ldcache;
    private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

    private static Logger LOGGER = getLogger(LDCacheServiceImpl.class);

    static {
        /* In principle, these could be added as OSGi services, as outlined here
         * http://blog.osgi.org/2013/02/javautilserviceloader-in-osgi.html
         * but as that would require wrapping the Sesame jars with `bnd`
         * and possibly the Marmotta jars, too, and as this is already two layers
         * below the current service, it seems fine to leave this as-is.
         * It's not as if Sesame and Marmotta are particularly modular at present.
         */
        RDFParserRegistry.getInstance().add(new NTriplesParserFactory());
        RDFParserRegistry.getInstance().add(new RDFXMLParserFactory());
        RDFParserRegistry.getInstance().add(new TurtleParserFactory());
        RDFParserRegistry.getInstance().add(new N3ParserFactory());
        RDFParserRegistry.getInstance().add(new SesameJSONLDParserFactory());
        RDFParserRegistry.getInstance().add(new RDFJSONParserFactory());
        RDFParserRegistry.getInstance().add(new SesameRDFaParserFactory());
        RDFParserRegistry.getInstance().add(new TriGParserFactory());
        BooleanQueryResultParserRegistry.getInstance().add(new SPARQLBooleanXMLParserFactory());
        TupleQueryResultParserRegistry.getInstance().add(new SPARQLResultsXMLParserFactory());
    }

    /**
     * Create a LDCacheServiceImpl object with a backing filesystem cache
     * @param storageDir the directory where data is stored
     * @param timeout the number of seconds before the cache is cleared
     * TODO - this constructor should accept a LDCachingBackend that is defined separately
     * as an OSGi service, but this is fine for now.
     */
    public LDCacheServiceImpl(final String storageDir, final long timeout) {
        try {
            final LDCachingBackend backend = new LDCachingFileBackend(new File(storageDir));
            backend.initialize();

            final CacheConfiguration config = new CacheConfiguration(buildClientConfiguration());
            if (timeout > 0) {
                config.setDefaultExpiry(timeout * 1000);
            }

            ldcache = new LDCache(config, backend);
        } catch (final Exception ex) {
            LOGGER.error("Could not initialize LDCacheService!", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Create a LDCacheServiceImpl object with a backing filesystem cache
     * @param storageDir the directory where data is stored
     * TODO - this constructor should accept a LDCachingBackend object (see above)
     */
    public LDCacheServiceImpl(final String storageDir) {
        this(storageDir, 0);
    }

    @Override
    public List<String> get(final String subject, final String predicate, final String lang) {
        if (subject == null) {
            LOGGER.warn("Subject is null, returning an empty List");
            return emptyList();
        }

        final URI s = valueFactory.createURI(subject);
        final URI p = predicate == null ? null : valueFactory.createURI(predicate);
        final Model model = ldcache.get(s);
        return model.filter(s, p, null).objects().stream()
            .filter(obj -> lang == null || (obj instanceof Literal &&
                        ((Literal)obj).getLanguage().equals(lang.toLowerCase())))
            .map(obj -> obj instanceof Literal ? ((Literal)obj).getLabel() : obj.stringValue())
            .collect(toList());
    }

    private static ClientConfiguration buildClientConfiguration() {
        final ClientConfiguration config = new ClientConfiguration();
        // TODO -- these may be good candidates for adding as OSGi services.
        config.addEndpoint(new SPARQLEndpoint("Getty Vocabs", "http://vocab.getty.edu/sparql",
                    "^http://vocab\\.getty\\.edu/.*"));
        config.addProvider(new LinkedDataProvider());
        config.addProvider(new CacheProvider());
        config.addProvider(new RegexUriProvider());
        config.addProvider(new SPARQLProvider());
        config.addEndpoint(new LinkedDataEndpoint());

        return config;
    }
}
