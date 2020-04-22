package com.mecatran.gtfsvtor.validation.dao;

import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.InvalidFieldValueIssue;
import com.mecatran.gtfsvtor.utils.Pair;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class RepeatedRouteNameValidator implements DaoValidator {

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		// Index routes by short/long name pair
		ListMultimap<Pair<String, String>, GtfsRoute> routeNames = ArrayListMultimap
				.create();
		dao.getRoutes()
				.forEach(route -> routeNames.put(
						new Pair<>(route.getShortName(), route.getLongName()),
						route));
		// Create an issue for each one having more than 2 routes
		Multimaps.asMap(routeNames).entrySet().stream()
				.filter(kv -> kv.getValue().size() >= 2).forEach(kv -> {
					reportSink.report(new InvalidFieldValueIssue(
							kv.getValue().stream().map(
									route -> route.getSourceInfo())
									.collect(Collectors.toList()),
							kv.getKey().getFirst() + " / "
									+ kv.getKey().getSecond(),
							"The same combination of route short and long name should not be used more than once",
							"route_short_name", "route_long_name")
									.withSeverity(ReportIssueSeverity.WARNING));
				});
	}
}
