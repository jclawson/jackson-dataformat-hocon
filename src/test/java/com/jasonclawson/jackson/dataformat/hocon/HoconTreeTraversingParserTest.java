package com.jasonclawson.jackson.dataformat.hocon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

public class HoconTreeTraversingParserTest {

	private static URL url(String name) {
		return HoconTreeTraversingParserTest.class.getResource(name);
	}

	private static InputStream stream(String name) throws IOException {
		return url(name).openStream();
	}

	private static Reader reader(InputStream is) throws IOException {
		return new InputStreamReader(is, "UTF-8");
	}

	private static String string(InputStream is) {
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private void assertConf(Configuration c) {
		Assert.assertEquals("This value comes from complex-app's complex2.conf", c.something);
		Assert.assertEquals(2.0, c.value, .001);
		Assert.assertEquals("This value comes from complex-app's complex2.conf in its custom simple-lib-context", c.context.lib.foo);
		Assert.assertEquals("This value comes from complex-app's complex2.conf in its custom simple-lib-context", c.context.lib.whatever);
	}

	@Test
	public void testUrl() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new HoconFactory());
		Configuration c = mapper.readValue(url("test.conf"), Configuration.class);
		assertConf(c);
	}

	@Test
	public void testStream() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new HoconFactory());
		Configuration c = mapper.readValue(stream("test.conf"), Configuration.class);
		assertConf(c);
	}

	@Test
	public void testReader() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new HoconFactory());
		Configuration c = mapper.readValue(reader(stream("test.conf")), Configuration.class);
		assertConf(c);
	}

	@Test
	public void testString() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new HoconFactory());
		Configuration c = mapper.readValue(stream("test.conf"), Configuration.class);
		assertConf(c);
	}

	@Test
	public void testSubstitution() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new HoconFactory());
		Map<String, String> map = mapper.readValue(stream("test-substitution.conf"), Map.class);
		Assert.assertEquals("This value ", map.get("foo"));
		Assert.assertEquals("This value  commes from foo", map.get("bar"));
	}

	@Test
	public void testInclusionUrl() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new HoconFactory());
		Configuration c = mapper.readValue(url("test-inclusion.conf"), Configuration.class);
		assertConf(c);
	}

	@Test
	public void testInclusionStreamFails() throws IOException {
		ObjectMapper mapper = new ObjectMapper(new HoconFactory());
		Configuration c = mapper.readValue(stream("test-inclusion.conf"), Configuration.class);
		// inclusion will not work with Stream
		Assert.assertNull(c.something);
		Assert.assertEquals(0.0, c.value, .001);
	}

}
