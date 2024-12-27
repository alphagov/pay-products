package uk.gov.pay.products.persistence.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Converter
public class UTCDateTimeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime dateTime) {
        return Timestamp.from(dateTime.toInstant());
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp s) {
        if (s == null) {
            return null;
        } else {
            return ZonedDateTime.ofInstant(s.toInstant(), UTC);
        }
    }
}
