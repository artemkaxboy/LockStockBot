package com.artemkaxboy.telerest.converter

import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.model.Ticker
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class Source1TickerDtoToTickerConverter : Converter<Source1TickerDto, Ticker> {

    override fun convert(source: Source1TickerDto): Ticker? {
        return Ticker(ticker = source.title,
            name = source.company?.title ?: "")
    }
}

// @Component
// public class StringToEnumConverterFactory
//   implements ConverterFactory<String, Enum> {
//
//     private static class StringToEnumConverter<T extends Enum>
//       implements Converter<String, T> {
//
//         private Class<T> enumType;
//
//         public StringToEnumConverter(Class<T> enumType) {
//             this.enumType = enumType;
//         }
//
//         public T convert(String source) {
//             return (T) Enum.valueOf(this.enumType, source.trim());
//         }
//     }
//
//     @Override
//     public <T extends Enum> Converter<String, T> getConverter(
//       Class<T> targetType) {
//         return new StringToEnumConverter(targetType);
//     }
// }
