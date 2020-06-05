package com.mecatran.gtfsvtor.validation.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.EmailValidator;

import com.mecatran.gtfsvtor.geospatial.GeoBounds;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.reporting.issues.InvalidCoordinateError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.reporting.issues.MissingMandatoryValueError;
import com.mecatran.gtfsvtor.validation.StreamingValidator;

public class StreamingValidationUtils {

	@FunctionalInterface
	public interface FieldGetter<T> {
		public T get();
	}

	public static <T> void checkNonNull(FieldGetter<T> getter, String fieldName,
			StreamingValidator.Context context) {
		if (getter.get() == null) {
			context.getReportSink()
					.report(new MissingMandatoryValueError(
							context.getSourceRef(), fieldName),
							context.getSourceInfo());
		}
	}

	public static <T> void checkOptionalPresent(FieldGetter<Optional<T>> getter,
			String fieldName, StreamingValidator.Context context) {
		if (!getter.get().isPresent()) {
			context.getReportSink()
					.report(new MissingMandatoryValueError(
							context.getSourceRef(), fieldName),
							context.getSourceInfo());
		}
	}

	public static <T> void checkFieldValue(Predicate<T> predicate, T t,
			String fieldName, StreamingValidator.Context context,
			String errorMessage) {
		if (predicate.test(t)) {
			context.getReportSink()
					.report(new InvalidFieldValueIssue(context.getSourceRef(),
							t.toString(), errorMessage, fieldName),
							context.getSourceInfo());
		}
	}

	public static void checkUrl(FieldGetter<String> getter, String fieldName,
			StreamingValidator.Context context) {
		checkFormat(getter, fieldName, context, url -> {
			try {
				new URL(url);
				return true;
			} catch (MalformedURLException e) {
				return false;
			}
		}, "RFC 1738 URL");
	}

	public static void checkEmail(FieldGetter<String> getter, String fieldName,
			StreamingValidator.Context context) {
		checkFormat(getter, fieldName, context,
				email -> EmailValidator.getInstance().isValid(email),
				"RFC 2822 Email");
	}

	private static Set<String> LC_ISOLANG = Arrays
			.asList(Locale.getISOLanguages()).stream().map(String::toLowerCase)
			.collect(Collectors.toSet());

	public static void checkLang(FieldGetter<String> getter, String fieldName,
			StreamingValidator.Context context) {
		checkFormat(getter, fieldName, context,
				lang -> LC_ISOLANG.contains(lang.toLowerCase()),
				"ISO 639-1 language");
	}

	public static <T> void checkFormat(FieldGetter<T> getter, String fieldName,
			StreamingValidator.Context context, Predicate<T> predicate,
			String expectedFormat) {
		T value = getter.get();
		if (value == null)
			return;
		if (!predicate.test(value)) {
			context.getReportSink()
					.report(new InvalidFieldFormatError(context.getSourceRef(),
							fieldName, value.toString(), expectedFormat),
							context.getSourceInfo());
		}
	}

	public static void checkCoordinates(FieldGetter<Double> getterLat,
			FieldGetter<Double> getterLon, String fieldNameLat,
			String fieldNameLon, GeoBounds boundingBox,
			StreamingValidator.Context context) {
		Double lat = getterLat.get();
		Double lon = getterLon.get();
		if (!boundingBox.contains(lat, lon)) {
			context.getReportSink()
					.report(new InvalidCoordinateError(context.getSourceRef(),
							fieldNameLat, fieldNameLon,
							new GeoCoordinates(lat, lon), boundingBox),
							context.getSourceInfo());
		}
	}
}
