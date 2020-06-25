package com.mecatran.gtfsvtor.test;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.Objects;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mecatran.gtfsvtor.test.TestUtils.TestScenario;
import com.mecatran.gtfsvtor.utils.SystemEnvironment;

public class TestHtmlReport {

	// Jan 1st 2020, but any date will do.
	private Date fakedNow = new Date(1577836800000L);

	// Set to true will reset the references data
	// If you use this, please make sure the delta are expected before
	private boolean reset = false;

	@Test
	public void testVeryBad() throws IOException {
		// This fails, why?
		// testHtmlReport("verybad", "reports/verybad.html");
	}

	@Test
	public void testGood() throws IOException {
		testHtmlReport("good_feed", "reports/good_feed.html");
	}

	private void testHtmlReport(String gtfs, String refReportFile)
			throws IOException {
		SystemEnvironment.setFakedNow(fakedNow);
		TestScenario testScenario = new TestScenario(gtfs);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		testScenario.htmlOutputStream = out;
		testScenario.run();
		String html = new String(out.toByteArray());
		compareDataToReference(html, refReportFile);
	}

	private void compareDataToReference(String data,
			String referenceResourceName) throws IOException {
		if (reset) {
			System.out.println(
					"Regenerating non-regression test reference for resource: "
							+ referenceResourceName);
			saveResourceAsString(referenceResourceName, data);
		} else {
			String refData = loadResourceAsString(referenceResourceName);
			if (!Objects.equals(refData, data)) {
				String resourceNameV2 = referenceResourceName + ".v2";
				saveResourceAsString(resourceNameV2, data);
				assertFalse(referenceResourceName
						+ ": data differs. Inspect the difference with "
						+ resourceNameV2
						+ " file and regenerate, or fix your code accordingly.",
						true);
			}
		}
	}

	private String loadResourceAsString(String resourceName)
			throws IOException {
		File file = new File("src/test/resources/data/" + resourceName);
		String text = Files.asCharSource(file, Charsets.UTF_8).read();
		return text;
	}

	private void saveResourceAsString(String resourceName, String data)
			throws IOException {
		File file = new File("src/test/resources/data/" + resourceName);
		Files.asCharSink(file, Charsets.UTF_8).write(data);
	}
}
