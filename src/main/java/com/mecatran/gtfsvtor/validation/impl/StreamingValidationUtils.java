package com.mecatran.gtfsvtor.validation.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.EmailValidator;

import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldFormatError;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueError;
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
			context.getReportSink().report(new MissingMandatoryValueError(
					context.getSourceInfo(), fieldName));
		}
	}

	public static <T> void checkFieldValue(Predicate<T> predicate, T t,
			String fieldName, StreamingValidator.Context context,
			String errorMessage) {
		if (predicate.test(t)) {
			context.getReportSink()
					.report(new InvalidFieldValueError(context.getSourceInfo(),
							t.toString(), errorMessage, fieldName));
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

	private static void checkFormat(FieldGetter<String> getter,
			String fieldName, StreamingValidator.Context context,
			Predicate<String> predicate, String expectedFormat) {
		String value = getter.get();
		if (value == null)
			return;
		if (!predicate.test(value)) {
			context.getReportSink().report(new InvalidFieldFormatError(
					context.getSourceInfo(), fieldName, value, expectedFormat));
		}
	}
}
