package com.mecatran.gtfsvtor.validation.impl;

import java.util.ArrayList;
import java.util.List;

import com.mecatran.gtfsvtor.validation.DaoValidator;

public class CompoundDaoValidator implements DaoValidator {

	private List<? extends DaoValidator> validators;
	private boolean verbose = false;

	public CompoundDaoValidator(List<? extends DaoValidator> validators) {
		this.validators = new ArrayList<>(validators);
	}

	public CompoundDaoValidator withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public void validate(DaoValidator.Context context) {
		for (DaoValidator validator : validators) {
			if (verbose) {
				System.out.println("Running validator: "
						+ validator.getClass().getSimpleName());
			}
			validator.validate(context);
		}
	}
}
