HOCON Jackson data format
=========================
Implementation of a Jackson parser for parsing the HOCON data format.

What the heck is HOCON?
-------------------------
HOCON stands for Human-Optimized Config Object Notation and is made by Typesafe. Check out the project here: [github.com/typesafehub/config](https://github.com/typesafehub/config)

In short, HOCON combines YAML, JSON, and Properties files into a single format. On most cases, YAML, JSON, and Properties formats are all valid HOCON--- and it can be mixed and matched at will. Check out the HOCON docs for more detail on the format.

Why this project?
------------------------
I created this project because I wanted to be able to use HOCON to configure my [Dropwizard](http://www.dropwizard.io) based applications. Under the hood, Dropwizard uses Jackson to parse its configuration files in either JSON / YAML format. I wanted to be able to easily switch that to using HOCON. Thus, this project was born.

[![Build Status](https://travis-ci.org/tburch/dropwizard-extras.png?branch=master)](https://travis-ci.org/tburch/dropwizard-extras)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jasonclawson/jackson-dataformat-hocon/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jasonclawson/jackson-dataformat-hocon/)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.jasonclawson/jackson-dataformat-hocon/badge.svg)](http://www.javadoc.io/doc/com.jasonclawson/jackson-dataformat-hocon)
