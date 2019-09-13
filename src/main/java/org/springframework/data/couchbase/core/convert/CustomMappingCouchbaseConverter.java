package org.springframework.data.couchbase.core.convert;

import org.springframework.data.couchbase.core.mapping.CouchbasePersistentEntity;
import org.springframework.data.couchbase.core.mapping.CouchbasePersistentProperty;
import org.springframework.data.mapping.context.MappingContext;

public class CustomMappingCouchbaseConverter extends MappingCouchbaseConverter {

    public CustomMappingCouchbaseConverter(final MappingContext<? extends CouchbasePersistentEntity<?>,
                                            CouchbasePersistentProperty> mappingContext) {

        super(mappingContext, CUSTOM_DOCUMENT_TYPEKEY);
        this.typeMapper = new TypeBasedCouchbaseTypeMapper(CUSTOM_DOCUMENT_TYPEKEY);
    }

    public static final String CUSTOM_DOCUMENT_TYPEKEY = "type";

}
