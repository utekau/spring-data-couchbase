package org.springframework.data.couchbase.core.convert;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CouchbaseCustomConverters {

    private CouchbaseCustomConverters() {

    }

    public static Collection<Converter<?, ?>> getConvertersToRegister() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(CouchbaseCustomConverters.DateTimeToStringConverter.INSTANCE);
        converters.add(CouchbaseCustomConverters.StringToDateTimeConverter.INSTANCE);
        return converters;
    }

    @WritingConverter
    public static enum DateTimeToStringConverter implements Converter<DateTime, String> {
        INSTANCE {
            @Override
            public String convert(DateTime dateTime) {
                return dateTime.toString(ISODateTimeFormat.dateTimeNoMillis());
            }
        };
    }

    @ReadingConverter
    public static enum StringToDateTimeConverter implements Converter<String, DateTime> {
        INSTANCE {
            @Override
            public DateTime convert(String date) {
                DateTime dt = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(date);
                return dt;
            }
        };
    }
}
