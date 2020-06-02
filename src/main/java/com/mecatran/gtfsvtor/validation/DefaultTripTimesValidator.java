package com.mecatran.gtfsvtor.validation;

import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsTripAndTimes;
import com.mecatran.gtfsvtor.validation.DaoValidator.Context;
import com.mecatran.gtfsvtor.validation.impl.CompoundTripTimesValidator;
import com.mecatran.gtfsvtor.validation.impl.ValidatorInjector;

public class DefaultTripTimesValidator implements TripTimesValidator {

	private CompoundTripTimesValidator compound;
	private boolean verbose = false;

	public DefaultTripTimesValidator(ValidatorConfig config) {
		List<? extends TripTimesValidator> validators = ValidatorInjector
				.getTripTimesStreamingValidatorInjector()
				.scanPackageAndInject(config);
		compound = new CompoundTripTimesValidator(validators);
	}

	public DefaultTripTimesValidator withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public void scanValidate(Context context) {
		if (verbose) {
			compound.getValidators().forEach(v -> System.out.println(
					"Running validator: " + v.getClass().getSimpleName()));
		}
		this.start(context);
		IndexedReadOnlyDao dao = context.getDao();
		dao.getRoutes().forEach(route -> {
			dao.getTripsOfRoute(route.getId()).forEach(trip -> {
				GtfsTripAndTimes tripAndTimes = new GtfsTripAndTimes(trip,
						dao.getStopTimesOfTrip(trip.getId()));
				this.validate(context, tripAndTimes);
			});
		});
		this.end(context);
	}

	@Override
	public void start(Context context) {
		compound.start(context);
	}

	@Override
	public void validate(Context context, GtfsTripAndTimes tripAndTimes) {
		compound.validate(context, tripAndTimes);
	}

	@Override
	public void end(Context context) {
		compound.end(context);
	}
}
