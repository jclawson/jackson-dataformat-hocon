package com.jasonclawson.jackson.dataformat.hocon;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HoconTreeTraversingParserTest {

	private static InputStream c(String name) {
		return HoconTreeTraversingParserTest.class.getResourceAsStream(name);
	}
	
	public static String s(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	@Test
	public void test() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper(new HoconFactory());
//		User user = mapper.readValue(yamlSource, User.class);
		String hocon = s(c("test.conf"));
		System.out.println(hocon);
		Configuration c = mapper.readValue(hocon, Configuration.class);
		
		System.out.println(c);
	}

}
