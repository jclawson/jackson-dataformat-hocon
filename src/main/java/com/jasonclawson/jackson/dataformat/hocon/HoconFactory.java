package com.jasonclawson.jackson.dataformat.hocon;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.core.io.IOContext;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

/**
 * This code was pretty much copied from the jackson YAMLFactory
 * @author jclawson
 *
 */
public class HoconFactory extends JsonFactory {
	private static final long serialVersionUID = 1L;
	
	public final static String FORMAT_NAME_HOCON = "HOCON";
	
	private final static byte UTF8_BOM_1 = (byte) 0xEF;
    private final static byte UTF8_BOM_2 = (byte) 0xBB;
    private final static byte UTF8_BOM_3 = (byte) 0xBF;
	
    public HoconFactory() { this(null); }
    
    public HoconFactory(ObjectCodec oc) {
        super(oc);
    }

    public HoconFactory(HoconFactory src, ObjectCodec oc) {
        super(src, oc);
    }

    
	@Override
    public HoconFactory copy()
    {
        _checkInvalidCopy(HoconFactory.class);
        return new HoconFactory(this, null);
    }

    /*
    /**********************************************************
    /* Serializable overrides
    /**********************************************************
     */

    /**
     * Method that we need to override to actually make restoration go
     * through constructors etc.
     * Also: must be overridden by sub-classes as well.
     */
    @Override
    protected Object readResolve() {
        return new HoconFactory(this, _objectCodec);
    }

    /*                                                                                       
    /**********************************************************                              
    /* Versioned                                                                             
    /**********************************************************                              
     */

//    @Override
//    public Version version() {
//        return PackageVersion.VERSION;
//    }
    
    /*
    /**********************************************************
    /* Format detection functionality (since 1.8)
    /**********************************************************
     */
    
    @Override
    public String getFormatName() {
        return FORMAT_NAME_HOCON;
    }
    
    /**
     * Sub-classes need to override this method (as of 1.8)
     */
    @Override
    public MatchStrength hasFormat(InputAccessor acc) throws IOException
    {
        if (!acc.hasMoreBytes()) {
            return MatchStrength.INCONCLUSIVE;
        }
        byte b = acc.nextByte();
        // Very first thing, a UTF-8 BOM?
        if (b == UTF8_BOM_1) { // yes, looks like UTF-8 BOM
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != UTF8_BOM_2) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            if (acc.nextByte() != UTF8_BOM_3) {
                return MatchStrength.NO_MATCH;
            }
            if (!acc.hasMoreBytes()) {
                return MatchStrength.INCONCLUSIVE;
            }
            b = acc.nextByte();
        }
        if (b == '{' || Character.isLetter((char) b) || Character.isDigit((char) b)) {
            return MatchStrength.WEAK_MATCH;
        }
        return MatchStrength.INCONCLUSIVE;
    }
    
    /*
    /**********************************************************
    /* Configuration, parser settings
    /**********************************************************
     */



    /*
    /**********************************************************
    /* Overridden parser factory methods (for 2.1)
    /**********************************************************
     */

    @SuppressWarnings("resource")
    @Override
    public HoconTreeTraversingParser createParser(String content)
        throws IOException, JsonParseException
    {
        Reader r = new StringReader(content);
        IOContext ctxt = _createContext(r, true); // true->own, can close
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            r = _inputDecorator.decorate(ctxt, r);
        }
        return _createParser(r, ctxt);
    }
    
    @SuppressWarnings("resource")
    @Override
    public HoconTreeTraversingParser createParser(File f)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(f, true);
        InputStream in = new FileInputStream(f);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createParser(in, ctxt);
    }
    
    @SuppressWarnings("resource")
    @Override
    public HoconTreeTraversingParser createParser(URL url)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(url, true);
        InputStream in = _optimizedStreamFromURL(url);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createParser(in, ctxt);
    }

    @SuppressWarnings("resource")
    @Override
    public HoconTreeTraversingParser createParser(InputStream in)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(in, false);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            in = _inputDecorator.decorate(ctxt, in);
        }
        return _createParser(in, ctxt);
    }

    @SuppressWarnings("resource")
    @Override
    public JsonParser createParser(Reader r)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(r, false);
        if (_inputDecorator != null) {
            r = _inputDecorator.decorate(ctxt, r);
        }
        return _createParser(r, ctxt);
    }

    @SuppressWarnings("resource")
    @Override
    public HoconTreeTraversingParser createParser(byte[] data)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(data, true);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            InputStream in = _inputDecorator.decorate(ctxt, data, 0, data.length);
            if (in != null) {
                return _createParser(in, ctxt);
            }
        }
        return _createParser(data, 0, data.length, ctxt);
    }

    @SuppressWarnings("resource")
    @Override
    public HoconTreeTraversingParser createParser(byte[] data, int offset, int len)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(data, true);
        // [JACKSON-512]: allow wrapping with InputDecorator
        if (_inputDecorator != null) {
            InputStream in = _inputDecorator.decorate(ctxt, data, offset, len);
            if (in != null) {
                return _createParser(in, ctxt);
            }
        }
        return _createParser(data, offset, len, ctxt);
    }
    
    /*
    /**********************************************************
    /* Overridden parser factory methods (2.0 and prior)
    /**********************************************************
     */

    // remove in 2.4
    @Deprecated
    @Override
    public HoconTreeTraversingParser createJsonParser(String content) throws IOException, JsonParseException {
        return createParser(content);
    }

    // remove in 2.4
    @Deprecated
    @Override
    public HoconTreeTraversingParser createJsonParser(File f) throws IOException, JsonParseException {
        return createParser(f);
    }
    
    // remove in 2.4
    @Deprecated
    @Override
    public HoconTreeTraversingParser createJsonParser(URL url) throws IOException, JsonParseException {
        return createParser(url);
    }

    // remove in 2.4
    @Deprecated
    @Override
    public HoconTreeTraversingParser createJsonParser(InputStream in) throws IOException, JsonParseException {
        return createParser(in);
    }

    // remove in 2.4
    @Deprecated
    @Override
    public JsonParser createJsonParser(Reader r) throws IOException, JsonParseException {
        return createParser(r);
    }

    // remove in 2.4
    @Deprecated
    @Override
    public HoconTreeTraversingParser createJsonParser(byte[] data) throws IOException, JsonParseException {
        return createParser(data);
    }
    
    // remove in 2.4
    @Deprecated
    @Override
    public HoconTreeTraversingParser createJsonParser(byte[] data, int offset, int len) throws IOException, JsonParseException {
        return createParser(data, offset, len);
    }

    /*
    /**********************************************************
    /* Overridden generator factory methods (2.1)
    /**********************************************************
     */

    @SuppressWarnings("resource")
    @Override
    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException
    {
    	throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }

    @SuppressWarnings("resource")
    @Override
    public JsonGenerator createGenerator(OutputStream out) throws IOException
    {
    	throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }
    
    @SuppressWarnings("resource")
    @Override
    public JsonGenerator createGenerator(Writer out) throws IOException
    {
    	throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }
    
    /*
    /**********************************************************
    /* Overridden generator factory methods (2.0 and before)
    /**********************************************************
     */

    // remove in 2.4
    @Deprecated
    @Override
    public JsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }

    // remove in 2.4
    @Deprecated
    @Override
    public JsonGenerator createJsonGenerator(OutputStream out) throws IOException {
    	throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }

    // remove in 2.4
    @Deprecated
    @Override
    public JsonGenerator createJsonGenerator(Writer out) throws IOException {
    	throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }
    
    /*
    /******************************************************
    /* Overridden internal factory methods
    /******************************************************
     */

    //protected IOContext _createContext(Object srcRef, boolean resourceManaged)

    @SuppressWarnings("resource")
    @Override
    protected HoconTreeTraversingParser _createParser(InputStream in, IOContext ctxt)
        throws IOException, JsonParseException
    {
        Reader r = _createReader(in, null, ctxt);
        return _createParser(r, ctxt);
    }

    @Override
    protected HoconTreeTraversingParser _createParser(Reader r, IOContext ctxt)
        throws IOException, JsonParseException
    {
    	ConfigParseOptions options = ConfigParseOptions.defaults();
        Config config = ConfigFactory.parseReader(r, options);
    	
    	return new HoconTreeTraversingParser(config.root(), _objectCodec);
    }

    @SuppressWarnings("resource")
    @Override
    protected HoconTreeTraversingParser _createParser(byte[] data, int offset, int len, IOContext ctxt)
        throws IOException, JsonParseException
    {
        Reader r = _createReader(data, offset, len, null, ctxt);
        return _createParser(r, ctxt);
    }

    @Override
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt)
        throws IOException
    {
    	throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }

    @SuppressWarnings("resource")
    @Deprecated
    @Override
    protected JsonGenerator _createUTF8Generator(OutputStream out, IOContext ctxt) throws IOException {
    	throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }

    @Override
    protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt) throws IOException
    {
    	throw new UnsupportedOperationException("Generating HOCON is not supported yet");
    }
    
    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    protected Reader _createReader(InputStream in, JsonEncoding enc, IOContext ctxt) throws IOException
    {
        if (enc == null) {
            enc = JsonEncoding.UTF8;
        }
        // default to UTF-8 if encoding missing
        if (enc == JsonEncoding.UTF8) {
            boolean autoClose = ctxt.isResourceManaged() || isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            return new UTF8Reader(in, autoClose);
//          return new InputStreamReader(in, UTF8);
        }
        return new InputStreamReader(in, enc.getJavaName());
    }

    protected Reader _createReader(byte[] data, int offset, int len,
            JsonEncoding enc, IOContext ctxt) throws IOException
    {
        if (enc == null) {
            enc = JsonEncoding.UTF8;
        }
        // default to UTF-8 if encoding missing
        if (enc == null || enc == JsonEncoding.UTF8) {
            return new UTF8Reader(data, offset, len, true);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(data, offset, len);
        return new InputStreamReader(in, enc.getJavaName());
    }
}

