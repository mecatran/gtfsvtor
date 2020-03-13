package com.mecatran.gtfsvtor.validation.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DefaultDisabledValidator;
import com.mecatran.gtfsvtor.validation.ValidatorConfig;

/*
 * TODO Replace static method by a configurable injector instance.
 */
public class ValidatorInjector {

	public static <T> void listValidatorOptions(Class<T> validatorClass,
			ClassLoader classLoader, Package pckge, PrintStream pw) {
		for (T validator : listValidatorOfPackage(validatorClass, classLoader,
				pckge)) {
			@SuppressWarnings("unchecked")
			Class<? extends T> clazz = (Class<? extends T>) validator
					.getClass();
			boolean defDisabled = clazz
					.isAnnotationPresent(DefaultDisabledValidator.class);
			pw.println(" • " + validator.getClass().getSimpleName()
					+ (defDisabled ? " (disabled by default)" : ""));
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(ConfigurableOption.class)) {
					ConfigurableOption confOpt = field
							.getAnnotation(ConfigurableOption.class);
					String fieldName = confOpt.name().isEmpty()
							? field.getName()
							: confOpt.name();
					String fieldDesc = confOpt.description().isEmpty() ? null
							: confOpt.description();
					Object defOptVal = null;
					try {
						field.setAccessible(true);
						defOptVal = field.get(validator);
					} catch (Exception e) {
						System.err.println(e);
					}
					pw.println("    - " + fieldName + " ("
							+ field.getType().getSimpleName() + ") = "
							+ (defOptVal == null ? "?" : defOptVal.toString()));
					if (fieldDesc != null) {
						pw.println("      " + fieldDesc);
					}
				}
			}
		}
	}

	public static <T> List<? extends T> scanPackageAndInject(
			Class<T> validatorClass, ClassLoader classLoader, Package pckge,
			ValidatorConfig config) {
		List<T> validators = new ArrayList<>();
		for (T validator : listValidatorOfPackage(validatorClass, classLoader,
				pckge)) {
			if (isValidatorEnabled(validator, config)) {
				// Inject configuration using annotations
				configureValidator(validator, config);
				validators.add(validator);
			}
		}
		// TODO How to sort validators?
		// Order by class name for now to make list stable
		Collections.sort(validators, new Comparator<T>() {
			@Override
			public int compare(T v1, T v2) {
				return v1.getClass().getName()
						.compareTo(v2.getClass().getName());
			}
		});
		return validators;
	}

	private static <T> List<? extends T> listValidatorOfPackage(
			Class<T> validatorClass, ClassLoader classLoader, Package pckge) {
		List<T> ret = new ArrayList<>();
		try {
			ClassPath cp = ClassPath.from(classLoader);
			for (ClassInfo classInfo : cp.getTopLevelClasses(pckge.getName())) {
				Class<?> clazz = classInfo.load();
				if (validatorClass.isAssignableFrom(clazz)) {
					try {
						@SuppressWarnings("unchecked")
						T validator = (T) clazz.getConstructor().newInstance();
						ret.add(validator);
					} catch (Exception e) {
						// TODO Log
						System.err.println("Cannot instantiate Validator "
								+ clazz + ": " + e);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Cannot scan package " + pckge);
		}
		Collections.sort(ret, new Comparator<T>() {
			@Override
			public int compare(T t1, T t2) {
				return t1.getClass().getSimpleName()
						.compareTo(t2.getClass().getSimpleName());
			}
		});
		return ret;
	}

	private static <T> boolean isValidatorEnabled(T validator,
			ValidatorConfig config) {
		@SuppressWarnings("unchecked")
		Class<? extends T> clazz = (Class<? extends T>) validator.getClass();
		boolean defEnabled = !clazz
				.isAnnotationPresent(DefaultDisabledValidator.class);
		boolean enabled = config.getBoolean(config.getKey(validator, "enabled"),
				defEnabled);
		if (enabled != defEnabled) {
			System.out.println((enabled ? "Enabling" : "Disabling")
					+ " validator " + validator.getClass().getSimpleName());
		}
		return enabled;
	}

	@SuppressWarnings("unchecked")
	private static <T> void configureValidator(T validator,
			ValidatorConfig config) {
		Class<? extends T> clazz = (Class<? extends T>) validator.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(ConfigurableOption.class)) {
				ConfigurableOption confOpt = field
						.getAnnotation(ConfigurableOption.class);
				String fieldName = confOpt.name().isEmpty() ? field.getName()
						: confOpt.name();
				String configKey = config.getKey(validator, fieldName);
				field.setAccessible(true);
				Class<?> fieldType = field.getType();
				Object value = null;
				if (fieldType.equals(String.class)) {
					value = config.getString(configKey, null);
				} else if (fieldType.equals(double.class)
						|| fieldType.equals(Double.class)) {
					value = config.getDouble(configKey, null);
				} else if (fieldType.equals(float.class)
						|| fieldType.equals(Float.class)) {
					value = config.getDouble(configKey, null);
					if (value != null)
						value = new Float((float) value);
				} else if (fieldType.equals(long.class)
						|| fieldType.equals(Long.class)) {
					value = config.getLong(configKey, null);
				} else if (fieldType.equals(int.class)
						|| fieldType.equals(Integer.class)) {
					value = config.getLong(configKey, null);
					if (value != null)
						value = new Integer((int) value);
				} else if (fieldType.equals(boolean.class)
						|| fieldType.equals(Boolean.class)) {
					value = config.getBoolean(configKey, null);
					if (value != null)
						value = new Boolean((boolean) value);
				} else {
					System.err.println("Cannot configure validator "
							+ validator.getClass().getSimpleName()
							+ " parameter " + fieldName + ": unsupported type "
							+ fieldType);
				}
				try {
					if (value != null) {
						field.set(validator, value);
					}
				} catch (IllegalAccessException e) {
					System.err.println("Cannot configure validator "
							+ validator.getClass().getSimpleName()
							+ " parameter " + fieldName + ": " + e);
				}
			}
		}
	}

}
