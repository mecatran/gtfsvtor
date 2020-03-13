package com.mecatran.gtfsvtor.validation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mecatran.gtfsvtor.validation.DaoValidator;

public class CompoundDaoValidator implements DaoValidator {

	private List<? extends DaoValidator> validators;
	private boolean verbose = false;
	private int parallelizingFactor = 1;

	public CompoundDaoValidator(List<? extends DaoValidator> validators) {
		this.validators = new ArrayList<>(validators);
	}

	public CompoundDaoValidator withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public CompoundDaoValidator withParallelizingFactor(int factor) {
		this.parallelizingFactor = factor;
		return this;
	}

	public void validate(DaoValidator.Context context) {
		ExecutorService exec = Executors
				.newFixedThreadPool(parallelizingFactor);
		if (verbose) {
			System.out
					.println("Parallelizing factor is " + parallelizingFactor);
		}
		try {
			List<Callable<Boolean>> callables = new ArrayList<>();
			for (DaoValidator validator : validators) {
				callables.add(() -> {
					if (verbose) {
						System.out.println("Running validator: "
								+ validator.getClass().getSimpleName());
					}
					validator.validate(context);
					return true;
				});
			}
			exec.invokeAll(callables);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}
	}
}
