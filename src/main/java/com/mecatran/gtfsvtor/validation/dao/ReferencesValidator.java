package com.mecatran.gtfsvtor.validation.dao;

import com.mecatran.gtfsvtor.validation.DaoValidator;
import com.mecatran.gtfsvtor.validation.DefaultDisabledValidator;

@DefaultDisabledValidator
public class ReferencesValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {

		// Note: Stop->parent reference has its own validator

		/*
		 * Currently this validator does not validate any reference. But if the
		 * dependency graph has some cycle, some reference validation cannot be
		 * made in the streaming validators. IFAIK this is not (yet) the case,
		 * but just to be sure we'll keep this validator for now; disabled.
		 */
	}
}
