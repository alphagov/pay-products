package uk.gov.pay.products.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.gov.service.payments.commons.model.SupportedLanguage;

@Converter
public class SupportedLanguageJpaConverter implements AttributeConverter<SupportedLanguage, String> {
    public SupportedLanguageJpaConverter() {
    }

    public String convertToDatabaseColumn(SupportedLanguage supportedLanguage) {
        return supportedLanguage.toString();
    }

    public SupportedLanguage convertToEntityAttribute(String supportedLanguage) {
        return SupportedLanguage.fromIso639AlphaTwoCode(supportedLanguage);
    }
}

