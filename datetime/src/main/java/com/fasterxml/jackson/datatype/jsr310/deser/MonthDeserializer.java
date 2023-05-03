package com.fasterxml.jackson.datatype.jsr310.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.DateTimeException;
import java.time.Month;
import java.time.Month;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;

/**
 * Deserializer for Java 8 temporal {@link Month}s.
 */
public class MonthDeserializer extends JSR310DateTimeDeserializerBase<Month>
{
    private static final long serialVersionUID = 1L;

    public static final MonthDeserializer INSTANCE = new MonthDeserializer();

    private static final DateTimeFormatter PARSER = new DateTimeFormatterBuilder()
            .appendValue(MONTH_OF_YEAR, 2)
            .toFormatter();


    /**
     * NOTE: only {@code public} so that use via annotations (see [modules-java8#202])
     * is possible
     *
     * @since 2.12
     */
    public MonthDeserializer() {
        this(null);
    }

    public MonthDeserializer(DateTimeFormatter formatter) {
        super(Month.class, formatter);
    }

    @Override
    protected MonthDeserializer withDateFormat(DateTimeFormatter dtf) {
        return new MonthDeserializer(dtf);
    }

    /**
     * Since 2.12
     */
    protected MonthDeserializer(MonthDeserializer base, Boolean leniency) {
        super(base, leniency);
    }

    @Override
    protected MonthDeserializer withLeniency(Boolean leniency) {
        return new MonthDeserializer(this, leniency);
    }

    @Override
    protected MonthDeserializer withShape(JsonFormat.Shape shape) { return this; }

    @Override
    public Month deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            return _fromString(parser, context, parser.getText());
        }
        // 30-Sep-2020, tatu: New! "Scalar from Object" (mostly for XML)
        if (parser.isExpectedStartObjectToken()) {
            return _fromString(parser, context,
                    context.extractScalarFromObject(parser, this, handledType()));
        }
        if (parser.isExpectedStartArrayToken()) {
            JsonToken t = parser.nextToken();
            if (t == JsonToken.END_ARRAY) {
                return null;
            }
            if ((t == JsonToken.VALUE_STRING || t == JsonToken.VALUE_EMBEDDED_OBJECT)
                    && context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
                final Month parsed = deserialize(parser, context);
                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    handleMissingEndArrayForSingle(parser, context);
                }
                return parsed;
            }
            if (t != JsonToken.VALUE_NUMBER_INT) {
                _reportWrongToken(context, JsonToken.VALUE_NUMBER_INT, "month");
            }
            int month = parser.getIntValue();
            if (parser.nextToken() != JsonToken.END_ARRAY) {
                throw context.wrongTokenException(parser, handledType(), JsonToken.END_ARRAY,
                        "Expected array to end");
            }
            return Month.of(month);
        }
        if (parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
            return (Month) parser.getEmbeddedObject();
        }
        return _handleUnexpectedToken(context, parser,
                JsonToken.VALUE_STRING, JsonToken.START_ARRAY);
    }

    protected Month _fromString(JsonParser p, DeserializationContext ctxt,
            String string0)  throws IOException
    {
        String string = string0.trim();
        if (string.length() == 0) {
            // 22-Oct-2020, tatu: not sure if we should pass original (to distinguish
            //   b/w empty and blank); for now don't which will allow blanks to be
            //   handled like "regular" empty (same as pre-2.12)
            return _fromEmptyString(p, ctxt, string);
        }
        try {
            if (_formatter == null) {
                try {
                    return PARSER.parse(string, Month::from);
                } catch (DateTimeParseException e) {
                    return Month.valueOf(string.toUpperCase());
                }
            }
            return _formatter.parse(string, Month::from);
        } catch (DateTimeException e) {
            return _handleDateTimeException(ctxt, e, string);
        }
    }
}
