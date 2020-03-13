package com.mecatran.gtfsvtor.validation;

import java.util.List;

import com.mecatran.gtfsvtor.validation.impl.CompoundDaoValidator;
import com.mecatran.gtfsvtor.validation.impl.ValidatorInjector;

public class DefaultDaoValidator implements DaoValidator {

	private CompoundDaoValidator compound;

	public DefaultDaoValidator(ValidatorConfig config) {
		List<? extends DaoValidator> validators = ValidatorInjector
				.getDaoValidatorInjector().scanPackageAndInject(config);
		compound = new CompoundDaoValidator(validators);
	}

	public DefaultDaoValidator withVerbose(boolean verbose) {
		this.compound.withVerbose(verbose);
		return this;
	}

	public DefaultDaoValidator withNumThreads(int numThreads) {
		this.compound.withNumThreads(numThreads);
		return this;
	}

	@Override
	public void validate(DaoValidator.Context context) {
		compound.validate(context);
	}

}
