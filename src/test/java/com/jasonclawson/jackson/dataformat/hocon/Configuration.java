package com.jasonclawson.jackson.dataformat.hocon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Configuration {
	
		public String something;
		public Context context;
		
		public static class Context {
			public Lib lib;
		}
		
		public static class Lib {
			public String foo;
			public String whatever;
		}
}
