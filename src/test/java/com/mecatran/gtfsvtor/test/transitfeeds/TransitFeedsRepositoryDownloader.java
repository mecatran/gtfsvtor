package com.mecatran.gtfsvtor.test.transitfeeds;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mecatran.gtfsvtor.test.transitfeeds.TransitFeedsResponse.TransitFeed;

/**
 * List and download locally in xdata folder all GTFS from transitfeeds.com (aka
 * OpenMobilityData), for further bulk testing on diverse GTFS data.
 */
public class TransitFeedsRepositoryDownloader {

	private static final String API_URL = "https://api.transitfeeds.com/v1";
	private static final String API_KEY = "bce662f8-04a0-495a-b971-33907aae099d";

	private static class TransitFeedsInfo {
		private String id;
		private String name;
		private String downloadUrl;
	}

	public static void main(String args[]) throws Exception {
		TransitFeedsRepositoryDownloader tfrd = new TransitFeedsRepositoryDownloader();
		tfrd.downloadAll();
	}

	private void downloadAll() throws Exception {
		System.out.println("Listing feedinfos...");
		List<TransitFeedsInfo> feedInfos = listFeedInfos();
		System.out.println("Downloaded " + feedInfos.size() + " feed infos:");
		for (TransitFeedsInfo feedInfo : feedInfos) {
			Path out = Paths.get(
					"xdata/" + feedInfo.id.replace("/", "_") + ".gtfs.zip");
			if (out.toFile().exists()) {
				System.out.println("Skipping " + feedInfo.id);
			} else {
				System.out.println("Downloading " + feedInfo.id + " ("
						+ feedInfo.name + ") -> " + feedInfo.downloadUrl);
				try {
					byte[] data = download(feedInfo.downloadUrl);
					Files.write(out, data);
					System.out.println("  OK, written " + data.length
							+ " bytes to " + out.toString());
				} catch (Exception e) {
					System.out.println("  Failed: " + e);
				}
			}
		}
	}

	private List<TransitFeedsInfo> listFeedInfos() throws Exception {
		List<TransitFeedsInfo> feedInfos = new ArrayList<>();
		int page = 1;
		int numPages = 0;
		do {
			TransitFeedsResponse response = callAPI(TransitFeedsResponse.class,
					"/getFeeds?limit=100&type=gtfs&page=" + page);
			if (!response.status.equals("OK"))
				throw new IOException(
						"Invalid status in response: " + response.status);
			System.out.println("Page " + response.results.page + "/"
					+ response.results.numPages);
			for (TransitFeed feed : response.results.feeds) {
				if (feed.type.equals("gtfs") && feed.urls != null
						&& feed.urls.downloadUrl != null) {
					TransitFeedsInfo tfi = new TransitFeedsInfo();
					tfi.id = feed.id;
					tfi.name = feed.title;
					tfi.downloadUrl = feed.urls.downloadUrl;
					feedInfos.add(tfi);
				}
			}
			page++;
			numPages = response.results.numPages;
		} while (page <= numPages);
		return feedInfos;
	}

	private <T> T callAPI(Class<T> clazz, String path) throws Exception {
		String url = API_URL + path + "&key=" + API_KEY;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		mapper.configure(
				DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		byte[] jsonData = download(url);
		if (jsonData == null) {
			return null;
		}
		T retval = mapper.readValue(jsonData, clazz);
		return retval;
	}

	private byte[] download(String url) throws Exception {
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		configBuilder.setSocketTimeout(10000);
		configBuilder.setConnectTimeout(10000);
		RequestConfig config = configBuilder.build();
		HttpClientBuilder clientBuilder = HttpClients.custom();
		clientBuilder.setDefaultRequestConfig(config);
		clientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
		CloseableHttpClient client = clientBuilder.build();
		URI uri = new URIBuilder(url).build();
		HttpGet request = new HttpGet(uri);
		byte[] data = null;
		try {
			HttpResponse response = client.execute(request);
			if (response.getEntity() != null) {
				data = IOUtils.toByteArray(response.getEntity().getContent());
			}
		} finally {
			client.close();
		}
		return data;
	}
}
