package com.jasonclawson.jackson.dataformat.hocon;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

public class HoconTreeTraversingParser extends ParserMinimalBase {

	/*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected ObjectCodec _objectCodec;

    /**
     * Traversal context within tree
     */
    protected HoconNodeCursor _nodeCursor;

    /*
    /**********************************************************
    /* State
    /**********************************************************
     */

    /**
     * Sometimes parser needs to buffer a single look-ahead token; if so,
     * it'll be stored here. This is currently used for handling 
     */
    protected JsonToken _nextToken;

    /**
     * Flag needed to handle recursion into contents of child
     * Array/Object nodes.
     */
    protected boolean _startContainer;
    
    /**
     * Flag that indicates whether parser is closed or not. Gets
     * set when parser is either closed by explicit call
     * ({@link #close}) or when end-of-input is reached.
     */
    protected boolean _closed;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public HoconTreeTraversingParser(ConfigObject n) { this(n, null); }

    public HoconTreeTraversingParser(ConfigObject n, ObjectCodec codec)
    {
        super(0);
        _objectCodec = codec;
        if (n.valueType() == ConfigValueType.LIST) {
            _nextToken = JsonToken.START_ARRAY;
            _nodeCursor = new HoconNodeCursor.Array(n, null);
        } else if (n.valueType() == ConfigValueType.OBJECT) {
            if (HoconNodeCursor.isNumericallyIndexed(n)) {
                _nextToken = JsonToken.START_ARRAY;
                _nodeCursor = new HoconNodeCursor.NumericallyIndexedObjectBackedArray(n, null);
            } else {
                _nextToken = JsonToken.START_OBJECT;
                _nodeCursor = new HoconNodeCursor.Object(n, null);
            }
        } else { // value node
            _nodeCursor = new HoconNodeCursor.RootValue(n, null);
        }
    }
    
    public static JsonToken asJsonToken(ConfigValue value) {
        if (HoconNodeCursor.isNumericallyIndexed(value)) {
            return JsonToken.START_ARRAY;
        }
        switch(value.valueType()) {
			case BOOLEAN:
				boolean bool = (Boolean) value.unwrapped();
				return (bool) ? JsonToken.VALUE_TRUE 
						      : JsonToken.VALUE_FALSE;
			case NULL:
				return JsonToken.VALUE_NULL;
			case NUMBER:
				Number unwrapped = (Number) value.unwrapped();
				if(unwrapped instanceof Double) {
					return JsonToken.VALUE_NUMBER_FLOAT;
				} else {
					return JsonToken.VALUE_NUMBER_INT;
				}
			case STRING:
				return JsonToken.VALUE_STRING;
			case LIST:
				return JsonToken.START_ARRAY;
			case OBJECT:
				return JsonToken.START_OBJECT;
			default:
				throw new IllegalArgumentException("Unhandled type "+value.valueType());
		
		}
	}

    @Override
    public void setCodec(ObjectCodec c) {
        _objectCodec = c;
    }

    @Override
    public ObjectCodec getCodec() {
        return _objectCodec;
    }

    @Override
    public Version version() {
        return com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION;
    }
    
    /*
    /**********************************************************
    /* Closeable implementation
    /**********************************************************
     */

    @Override
    public void close() throws IOException
    {
        if (!_closed) {
            _closed = true;
            _nodeCursor = null;
            _currToken = null;
        }
    }

    /*
    /**********************************************************
    /* Public API, traversal
    /**********************************************************
     */

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException
    {
        if (_nextToken != null) {
            _currToken = _nextToken;
            _nextToken = null;
            return _currToken;
        }
        // are we to descend to a container child?
        if (_startContainer) {
            _startContainer = false;
            // minor optimization: empty containers can be skipped
            if (!_nodeCursor.currentHasChildren()) {
                _currToken = (_currToken == JsonToken.START_OBJECT) ?
                    JsonToken.END_OBJECT : JsonToken.END_ARRAY;
                return _currToken;
            }
            _nodeCursor = _nodeCursor.iterateChildren();
            _currToken = _nodeCursor.nextToken();
            if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
                _startContainer = true;
            }
            return _currToken;
        }
        // No more content?
        if (_nodeCursor == null) {
            _closed = true; // if not already set
            return null;
        }
        // Otherwise, next entry from current cursor
        _currToken = _nodeCursor.nextToken();
        if (_currToken != null) {
            if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
                _startContainer = true;
            }
            return _currToken;
        }
        // null means no more children; need to return end marker
        _currToken = _nodeCursor.endToken();
        _nodeCursor = _nodeCursor.getParent();
        return _currToken;
    }
    
    // default works well here:
    //public JsonToken nextValue() throws IOException, JsonParseException

    @Override
    public JsonParser skipChildren() throws IOException, JsonParseException
    {
        if (_currToken == JsonToken.START_OBJECT) {
            _startContainer = false;
            _currToken = JsonToken.END_OBJECT;
        } else if (_currToken == JsonToken.START_ARRAY) {
            _startContainer = false;
            _currToken = JsonToken.END_ARRAY;
        }
        return this;
    }

    @Override
    public boolean isClosed() {
        return _closed;
    }

    /*
    /**********************************************************
    /* Public API, token accessors
    /**********************************************************
     */

    @Override
    public String getCurrentName() {
        return (_nodeCursor == null) ? null : _nodeCursor.getCurrentName();
    }

    @Override
    public void overrideCurrentName(String name)
    {
        if (_nodeCursor != null) {
            _nodeCursor.overrideCurrentName(name);
        }
    }
    
    @Override
    public JsonStreamContext getParsingContext() {
        return _nodeCursor;
    }

    @Override
    public JsonLocation getTokenLocation() {
        return JsonLocation.NA;
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return JsonLocation.NA;
    }

    /*
    /**********************************************************
    /* Public API, access to textual content
    /**********************************************************
     */

    @Override
    public String getText()
    {
        if (_closed) {
            return null;
        }
        // need to separate handling a bit...
        switch (_currToken) {
        case FIELD_NAME:
            return _nodeCursor.getCurrentName();
        case VALUE_STRING:
            return (String) currentNode().unwrapped();
        case VALUE_NUMBER_INT:
        case VALUE_NUMBER_FLOAT:
            return String.valueOf(currentNode().unwrapped());
        case VALUE_EMBEDDED_OBJECT:
        	throw new UnsupportedOperationException("VALUE_EMBEDDED_OBJECT is not supported by HOCON");
        default:
        	return (_currToken == null) ? null : _currToken.asString();
        }
    }

    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        return getText().toCharArray();
    }

    @Override
    public int getTextLength() throws IOException, JsonParseException {
        return getText().length();
    }

    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        return 0;
    }

    @Override
    public boolean hasTextCharacters() {
        // generally we do not have efficient access as char[], hence:
        return false;
    }
    
    /*
    /**********************************************************
    /* Public API, typed non-text access
    /**********************************************************
     */

    //public byte getByteValue() throws IOException, JsonParseException

    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
    	ConfigValue n = currentNumericNode();
    	if(n == null)
    		return null;
    	
    	Number value = (Number) n.unwrapped();
    	if(value instanceof Double) {
    		return NumberType.DOUBLE;
    	} else if(value instanceof Long) {
    		return NumberType.LONG;
    	} else {
    		return NumberType.INT;
    	}
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
    	//I wish we could get at the string representation instead
    	Long value = ((Number)  currentNumericNode().unwrapped()).longValue();
    	return BigInteger.valueOf(value);
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        Double value = ((Number) currentNumericNode().unwrapped()).doubleValue();
    	return BigDecimal.valueOf(value);
    }

    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        return ((Number) currentNumericNode().unwrapped()).doubleValue();
    }

    @Override
    public float getFloatValue() throws IOException, JsonParseException {
        return ((Number) currentNumericNode().unwrapped()).floatValue();
    }

    @Override
    public long getLongValue() throws IOException, JsonParseException {
        return ((Number) currentNumericNode().unwrapped()).longValue();
    }

    @Override
    public int getIntValue() throws IOException, JsonParseException {
        return ((Number) currentNumericNode().unwrapped()).intValue();
    }

    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        return (Number) currentNumericNode().unwrapped();
    }

    @Override
    public Object getEmbeddedObject()
    {
//        if (!_closed) {
//            ConfigValue n = currentNode();
//            if (n != null) {
//                if (n.isPojo()) {
//                    return ((POJONode) n).getPojo();
//                }
//                if (n.isBinary()) {
//                    return ((BinaryNode) n).binaryValue();
//                }
//            }
//        }
        return null;
    }

    /*
    /**********************************************************
    /* Public API, typed binary (base64) access
    /**********************************************************
     */

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException, JsonParseException
    {
        // otherwise return null to mark we have no binary content
        return null;
    }


    @Override
    public int readBinaryValue(Base64Variant b64variant, OutputStream out)
            throws IOException, JsonParseException
    {
        byte[] data = getBinaryValue(b64variant);
        if (data != null) {
            out.write(data, 0, data.length);
            return data.length;
        }
        return 0;
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    protected ConfigValue currentNode() {
        if (_closed || _nodeCursor == null) {
            return null;
        }
        return _nodeCursor.currentNode();
    }

    protected ConfigValue currentNumericNode()
        throws JsonParseException
    {
    	ConfigValue n = currentNode();
        if (n == null || n.valueType() != ConfigValueType.NUMBER) {
            JsonToken t = (n == null) ? null : asJsonToken(n);
            throw _constructError("Current token ("+t+") not numeric, can not use numeric value accessors");
        }
        return n;
    }

    @Override
    protected void _handleEOF() throws JsonParseException {
        _throwInternal(); // should never get called
    }

}
