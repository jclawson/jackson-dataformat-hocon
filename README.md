HOCON Jackson data format
=========================
Implementation of a [Jackson](https://github.com/FasterXML/jackson) parser for parsing the HOCON data format.

What the heck is HOCON?
-------------------------
HOCON stands for Human-Optimized Config Object Notation and is made by [Typesafe](github.com/typesafehub/config).

In short, HOCON combines YAML, JSON, and Properties files into a single format. On most cases, YAML, JSON, and Properties formats are all valid HOCON--- and it can be mixed and matched at will. Check out the HOCON docs for more detail on the format.

Why this project?
------------------------
This project lets you use HOCON to configure any application that uses Jackson to parse its configuration files.

How to Use
------------
Add the following fragment to your project pom to include HOCON data format:
```xml
  <dependency>
    <groupId>org.honton.chas.hocon</groupId>
    <artifactId>jackson-dataformat-hocon</artifactId>
    <version>1.1.1</version>
  </dependency>
```

Create the Jackson ObjectMapper with the following constructor:
```java
  ObjectMapper mapper = new ObjectMapper(new HoconFactory());
```

Some Caveats
------------
There is support for HOCON include statements if the URL or File version of ObjectMapper is used.  (Unfortunately, the Jackson InputDecorator will be ignored).
```java
  Configuration c = mapper.readValue(new URL("http://example.com/path/test.conf"), Configuration.class);
```
or
```java
  Configuration c = mapper.readValue(new File(filepath), Configuration.class);
```

There is support for Jackson InputDecorator if the InputStream or Reader version of ObjectMapper is used.  (Unfortunately, the HOCON statements include will be ignored).
```java
  Configuration c = mapper.readValue(new FileInputStream("http://example.com/path/test.conf"), Configuration.class);
```
or
```java
  Configuration c = mapper.readValue(new InputStreamReader(is), Configuration.class);
```

[![Build Status](https://travis-ci.org/tburch/dropwizard-extras.png?branch=master)](https://travis-ci.org/tburch/dropwizard-extras)
