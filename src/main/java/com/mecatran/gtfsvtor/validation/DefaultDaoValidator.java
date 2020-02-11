package com.mecatran.gtfsvtor.validation;

import java.util.List;

import com.mecatran.gtfsvtor.validation.dao.ReferencesValidator;
import com.mecatran.gtfsvtor.validation.impl.CompoundDaoValidator;
import com.mecatran.gtfsvtor.validation.impl.ValidatorInjector;

public class DefaultDaoValidator implements DaoValidator {

	private CompoundDaoValidator compound;

	public DefaultDaoValidator(ValidatorConfig config) {
		List<? extends DaoValidator> validators = ValidatorInjector
				.scanPackageAndInject(DaoValidator.class,
						this.getClass().getClassLoader(),
						ReferencesValidator.class.getPackage(), config);
		compound = new CompoundDaoValidator(validators);
	}

	@Override
	public void validate(DaoValidator.Context context) {
		compound.validate(context);
	}

}
