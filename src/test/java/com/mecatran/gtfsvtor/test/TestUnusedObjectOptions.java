package com.mecatran.gtfsvtor.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.mecatran.gtfsvtor.reporting.issues.UnusedObjectWarning;
import com.mecatran.gtfsvtor.test.TestUtils.TestScenario;

public class TestUnusedObjectOptions {

	private static final Map<String, String> OPTIONS = Map.of(
			"checkAgencies", "Unused agency", "checkRoutes", "Unused route",
			"checkCalendars", "Unused calendar", "checkCalendarDates", "Unused calendar date",
			"checkShapes", "Unused shape", "checkStations", "Unused station",
			"checkLevels", "Unused level", "checkAreas", "Unused area");

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private List<String> warningsWithDisabled(Iterable<String> options) throws IOException {
		File config = temp.newFile();
		List<String> settings = new ArrayList<>();
		for (String option : options) {
			settings.add("validator.UnusedObjectsValidator." + option + "=false");
		}
		Files.write(config.toPath(), settings);
		TestScenario scenario = new TestScenario("unused_data");
		scenario.configFile = config.getAbsolutePath();
		return scenario.run().issuesOfCategory(UnusedObjectWarning.class).stream()
				.map(UnusedObjectWarning::getCategoryName).sorted().collect(Collectors.toList());
	}

	@Test
	public void eachOptionSuppressesOnlyItsOwnWarning() throws IOException {
		for (Map.Entry<String, String> option : OPTIONS.entrySet()) {
			List<String> expected = new ArrayList<>(OPTIONS.values());
			expected.remove(option.getValue());
			expected.add("Unused stop");
			Collections.sort(expected);
			assertEquals(option.getKey(), expected, warningsWithDisabled(List.of(option.getKey())));
		}
	}

	@Test
	public void disablingAllTypesPreservesTheSeparateStopWarning() throws IOException {
		assertEquals(List.of("Unused stop"), warningsWithDisabled(OPTIONS.keySet()));
	}
}
