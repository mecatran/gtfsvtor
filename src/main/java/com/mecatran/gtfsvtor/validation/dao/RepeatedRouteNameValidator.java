package com.mecatran.gtfsvtor.validation.dao;

import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsAgency;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.utils.Triplet;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class RepeatedRouteNameValidator implements DaoValidator {

	@ConfigurableOption(description = "Enable to take into account route agency (ie only check for duplicated route short/long name within each agency)")
	private boolean useAgency = false;

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		/*
		 * Index routes by agency ID + short + long name triplet. Having a null
		 * agency ID is always possible, even in useAgency=true mode.
		 */
		ListMultimap<Triplet<GtfsAgency.Id, String, String>, GtfsRoute> routeNames = ArrayListMultimap
				.create();
		dao.getRoutes()
				.forEach(route -> routeNames.put(
						new Triplet<>(useAgency ? route.getAgencyId() : null,
								route.getShortName(), route.getLongName()),
						route));
		// Create an issue for each group having more than 2 routes
		Multimaps.asMap(routeNames).entrySet().stream()
				.filter(kv -> kv.getValue().size() >= 2).forEach(kv -> {
					if (useAgency) {
						reportSink.report(new InvalidFieldValueIssue(
								kv.getValue().stream()
										.map(route -> route.getSourceRef())
										.collect(Collectors.toList()),
								kv.getKey().getFirst() + " / "
										+ kv.getKey().getSecond() + " / "
										+ kv.getKey().getThird(),
								"The same combination of agency ID, route short and long name should not be used more than once",
								"agency_id", "route_short_name",
								"route_long_name").withSeverity(
										ReportIssueSeverity.WARNING));
					} else {
						reportSink.report(new InvalidFieldValueIssue(
								kv.getValue().stream()
										.map(route -> route.getSourceRef())
										.collect(Collectors.toList()),
								kv.getKey().getSecond() + " / "
										+ kv.getKey().getThird(),
								"The same combination of route short and long name should not be used more than once",
								"route_short_name", "route_long_name")
										.withSeverity(
												ReportIssueSeverity.WARNING));
					}
				});
	}
}
