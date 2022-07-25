package com.penguineering.cleanuri.extractor.processors;

import com.penguineering.cleanuri.common.message.MetaData;

import java.net.URI;
import java.util.Map;

/**
 * <p>
 * The extractor is responsible for resolving the provided URI and extracting
 * meta-data available for the item.
 * </p>
 * 
 * <p>
 * Note that the URI must not necessary match the best URL and some conversion
 * may be necessary, like changing the scheme from http to https.
 * </p>
 */
public interface Extractor {
	boolean isSuitable(URI uri);

		/**
         * Extract meta-data for the given URI.
         *
         * @param uri
         *            The target URI.
         * @return a map of meta-data values.
         * @throws ExtractorException
         *             if extraction fails.
         * @throws NullPointerException
         *             if the URI argument is null.
         * @throws IllegalArgumentException
         *             if the URI is not suitable for use with this extractor.
         */
	Map<MetaData.Fields, String> extractMetadata(URI uri) throws ExtractorException;
}
