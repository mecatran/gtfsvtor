package com.mecatran.gtfsvtor.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.junit.Test;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mecatran.gtfsvtor.test.TestUtils.TestScenario;
import com.mecatran.gtfsvtor.utils.SystemEnvironment;

public class TestHtmlReport {

	// Jan 1st 2020, but any date will do.
	private Date fakedNow = new Date(1577836800000L);

	// Set to true will reset the references data
	// If you use this, please make sure the delta are expected before
	// Never commit with value true
	private boolean reset = false;

	@Test
	public void testVeryBadHtml() throws IOException {
		testHtmlReport("verybad.html", "verybad");
	}

	@Test
	public void testVeryBadJson() throws IOException {
		testJsonReport("verybad.json", "verybad");
	}

	@Test
	public void testGoodHtml() throws IOException {
		testHtmlReport("good_feed.html", "good_feed");
	}

	@Test
	public void testGoodJson() throws IOException {
		testJsonReport("good_feed.json", "good_feed");
	}

	@Test
	public void testJsonAppend() throws IOException {
		testJsonReportAppend("good_feed_append.json", "good_feed",
				"does_not_exists", "verybad");
	}

	private void testHtmlReport(String refReportFile, String gtfs)
			throws IOException {
		/* Force tests to be consistent across platforms */
		SystemEnvironment.setFakedNow(fakedNow);
		Locale.setDefault(Locale.US);
		TestScenario testScenario = new TestScenario(gtfs);
		testScenario.htmlDataIO = new TestDataIO();
		testScenario.run();
		String html = new String(testScenario.htmlDataIO.getData());
		compareDataToReference(html, refReportFile);
	}

	private void testJsonReport(String refReportFile, String gtfs)
			throws IOException {
		/* Force tests to be consistent across platforms */
		SystemEnvironment.setFakedNow(fakedNow);
		Locale.setDefault(Locale.US);
		TestScenario testScenario = new TestScenario(gtfs);
		testScenario.jsonDataIO = new TestDataIO();
		testScenario.run();
		String json = new String(testScenario.jsonDataIO.getData());
		compareDataToReference(json, refReportFile);
	}

	private void testJsonReportAppend(String refReportFile, String... gtfsList)
			throws IOException {

		Locale.setDefault(Locale.US);
		long timestamp = fakedNow.getTime();

		TestScenario previousScenario = null;
		for (String gtfs : gtfsList) {
			/* Force tests to be consistent across platforms */
			SystemEnvironment.setFakedNow(new Date(timestamp));
			timestamp += 7 * 24 * 60 * 60 * 1000L;
			TestScenario testScenario = new TestScenario(gtfs);
			testScenario.jsonDataIO = new TestDataIO(
					previousScenario == null ? null
							: previousScenario.jsonDataIO.getData());
			testScenario.run();
			previousScenario = testScenario;
		}

		String json = new String(previousScenario.jsonDataIO.getData());
		compareDataToReference(json, refReportFile);
	}

	private void compareDataToReference(String genData, String refResourceName)
			throws IOException {
		if (reset) {
			System.out.println(String.format(
					"Regenerating non-regression test reference for resource (%s)",
					refResourceName));
			saveResourceAsString(refResourceName, genData);
		} else {
			String refData = loadResourceAsString(refResourceName);
			if (!Objects.equals(refData, genData)) {
				String genResourceName = refResourceName + ".new";
				String message = String.format(
						"Output differs between reference data (%s) and generated data (%s).\n"
								+ "Inspect the difference, and either regenerate the reference if this is expected, "
								+ "or fix your code if not.",
						refResourceName, genResourceName);
				System.err.println(message);
				System.err.println(
						"Below a list of delta between the two versions:");
				saveResourceAsString(genResourceName, genData);
				try {
					List<String> refLines = Arrays.asList(refData.split("\\R"));
					List<String> genlines = Arrays.asList(genData.split("\\R"));
					Patch<String> patch = DiffUtils.diff(refLines, genlines);
					for (AbstractDelta<String> delta : patch.getDeltas()) {
						// Simulate kind of unified diff
						System.err.println(String.format("@@ -%d,%d  +%d,%d @@",
								delta.getSource().getPosition(),
								delta.getSource().getLines().size(),
								delta.getTarget().getPosition(),
								delta.getTarget().getLines().size()));
						delta.getSource().getLines()
								.forEach(l -> System.err.println("- " + l));
						delta.getTarget().getLines()
								.forEach(l -> System.err.println("+ " + l));
					}
				} catch (DiffException e) {
					throw new RuntimeException(e);
				}
				fail(message);
			}
		}
	}

	private String loadResourceAsString(String resourceName)
			throws IOException {
		File file = new File("src/test/resources/reports/" + resourceName);
		String text = Files.asCharSource(file, Charsets.UTF_8).read();
		return text;
	}

	private void saveResourceAsString(String resourceName, String data)
			throws IOException {
		File file = new File("src/test/resources/reports/" + resourceName);
		Files.asCharSink(file, Charsets.UTF_8).write(data);
	}
}
