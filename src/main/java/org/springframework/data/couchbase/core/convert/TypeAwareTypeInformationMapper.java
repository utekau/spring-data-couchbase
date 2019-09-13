package org.springframework.data.couchbase.core.convert;

import org.springframework.data.convert.SimpleTypeInformationMapper;
import org.springframework.data.couchbase.core.mapping.DocumentType;
import org.springframework.data.mapping.Alias;
import org.springframework.data.util.TypeInformation;

public class TypeAwareTypeInformationMapper extends SimpleTypeInformationMapper {

    @Override
    public Alias createAliasFor(TypeInformation<?> type) {
        DocumentType[] documentType = type.getType().getAnnotationsByType(DocumentType.class);

        // If the class annotated with "DocumentType" then it will be used, else class name will be put as ordinary
        if (documentType.length == 1) {
            return Alias.of(documentType[0].value());
        }

        return super.createAliasFor(type);
    }
}
