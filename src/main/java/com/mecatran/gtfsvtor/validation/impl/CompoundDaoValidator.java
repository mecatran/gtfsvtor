package com.mecatran.gtfsvtor.validation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.mecatran.gtfsvtor.validation.DaoValidator;

public class CompoundDaoValidator implements DaoValidator {

	private List<? extends DaoValidator> validators;
	private boolean verbose = false;
	private int numThreads = 1;

	public CompoundDaoValidator(List<? extends DaoValidator> validators) {
		this.validators = new ArrayList<>(validators);
	}

	public CompoundDaoValidator withVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public CompoundDaoValidator withNumThreads(int numThreads) {
		this.numThreads = numThreads;
		return this;
	}

	public void validate(DaoValidator.Context context) {
		ExecutorService exec = Executors.newFixedThreadPool(numThreads);
		if (verbose && numThreads > 1) {
			System.out
					.println("Parallelizing with " + numThreads + " threads.");
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
			List<Future<Boolean>> results = exec.invokeAll(callables);
			results.forEach(f -> {
				try {
					f.get();
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			exec.shutdown();
		}
	}
}
