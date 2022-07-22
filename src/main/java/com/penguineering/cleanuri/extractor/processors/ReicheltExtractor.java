package com.penguineering.cleanuri.extractor.processors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.micronaut.context.annotation.Bean;
import org.apache.commons.lang3.StringEscapeUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Meta-data extractor for Reichelt catalog data.
 */
@Bean
public class ReicheltExtractor implements Extractor {
	public static final String PREFIX = "http://www.reichelt.de/index.html?ARTICLE=";

	@Override
	public Map<Metakey, String> extractMetadata(URI uri) throws ExtractorException {
		if (uri == null)
			throw new NullPointerException("URI argument must not be null!");

		final String uriStr = uri.toASCIIString();

		// Check if the prefix matches
		if (!uriStr.startsWith(PREFIX))
			throw new IllegalArgumentException(
					"Reichelt extractor has been presented a URI which does not match the Reichelt prefix!");

		/*
		 * Create a URL from the provided URI.
		 * 
		 * Reichelt made everybode use HTTPS now, unfortunately this is encoded
		 * in the URI. HTTP still works, but will result in a 301 response,
		 * which we resolve beforehand by changing the http scheme in the URI to
		 * https.
		 */
		final URL url;
		try {
			// Reichelt URLs must be HTTPS now
			if (uri.getScheme().equals("http"))
				url = new URI("https" + uriStr.substring(4)).toURL();
			else
				url = uri.toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("The provided URI is not a URL!");
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Could not convert provided URL to https scheme!", e);
		}

		Map<Metakey, String> meta = new HashMap<>();

		try {
			final Document doc = Jsoup.connect(url.toExternalForm()).get();

			final Elements articleHeader = doc.select("#av_articleheader h2");
			final Optional<TextNode> articleId = articleHeader.textNodes().stream().findFirst();
			if (articleId.isPresent())
				meta.put(Metakey.ID, html2oUTF8(articleId.get().text()).trim());

			final Elements articleName = articleHeader.select("span[itemprop=\"name\"]");
			if (articleName.hasText())
				meta.put(Metakey.NAME, html2oUTF8(articleName.text()).trim());
		} catch (IOException e) {
			throw new ExtractorException("I/O exception during extraction: " + e.getMessage(), e, uri);
		}

		return meta;
	}

	private static String html2oUTF8(String html) throws UnsupportedEncodingException {
		final String iso = StringEscapeUtils.unescapeHtml4(html);

		final String encoding = "ISO-8859-1"; // according to page header
		// final String encoding = "UTF-8";

		final byte[] b = iso.getBytes(encoding);
		return new String(b, encoding);
	}
}
