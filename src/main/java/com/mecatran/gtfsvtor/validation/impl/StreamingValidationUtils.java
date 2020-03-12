package com.mecatran.gtfsvtor.validation.impl;

import java.util.function.Predicate;

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
							fieldName, t.toString(), errorMessage));
		}
	}
}
