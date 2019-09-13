package org.springframework.data.couchbase.core.convert;

import org.springframework.data.convert.DefaultTypeMapper;
import org.springframework.data.couchbase.core.mapping.CouchbaseDocument;

import java.util.Collections;

public class TypeBasedCouchbaseTypeMapper extends DefaultTypeMapper<CouchbaseDocument> implements CouchbaseTypeMapper {

    private final String typeKey;

    public TypeBasedCouchbaseTypeMapper(final String typeKey) {
        super(new DefaultCouchbaseTypeMapper.CouchbaseDocumentTypeAliasAccessor(typeKey),
              Collections.singletonList(new TypeAwareTypeInformationMapper()));
        this.typeKey = typeKey;
    }

    @Override
    public String getTypeKey() {
        return typeKey;
    }
}
