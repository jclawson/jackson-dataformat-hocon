package com.jasonclawson.jackson.dataformat.hocon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestArrays {

    private String hoconOne = "list = [\"a\", \"b\"]";
    private String hoconTwo = "list.0 = \"a\"\nlist.1 = \"b\"";
    private String hoconThree = "list = {\"1\" : \"b\", \"0\" : \"a\"}";

    @Test
    public void testArrays() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new HoconFactory());

        System.out.println("Mapping hocon to Container class for:");
        System.out.println(hoconOne);
        Container cOne = mapper.readValue(hoconOne, Container.class);
        System.out.println("The Container class's list value is " + cOne.list);

        System.out.println("Mapping hocon to Container class for:");
        System.out.println(hoconTwo);
        Container cTwo = mapper.readValue(hoconTwo, Container.class);
        System.out.println("The Container class's list value is " + cTwo.list);

        System.out.println("Mapping hocon to Container class for:");
        System.out.println(hoconThree);
        Container cThree = mapper.readValue(hoconThree, Container.class);
        System.out.println("The Container class's list value is " + cThree.list);

        Assert.assertNotNull(cOne.list);
        Assert.assertNotNull(cTwo.list);
        Assert.assertNotNull(cThree.list);

        Assert.assertEquals(cOne.list, cTwo.list);
        Assert.assertEquals(cOne.list, cThree.list);
        Assert.assertEquals(cThree.list, cTwo.list);
    }
    
    @Test
    public void testNonArray() throws JsonProcessingException, IOException {
        testNonArray("{ \"-1\" : \"A\" }");
        testNonArray("{ \"1\" : \"A\" }");
        testNonArray("{ }");
    }
    
    private void testNonArray(String json) throws JsonProcessingException, IOException {
        ObjectMapper hoconmapper = new ObjectMapper(new HoconFactory());
        JsonNode node = hoconmapper.readTree(json);
        assertFalse("Should NOT be resolved to array :"+json, node.isArray());
    }

    @Test
    public void testSymmetry() throws IOException {
        doTestSymmetry();
        doTestSymmetry("A");
        doTestSymmetry("A", "B");
        doTestSymmetry("A", null);
        doTestSymmetry(null, "B");
        doTestSymmetry(null, null);
    }


    private void doTestSymmetry(String... values) throws IOException {
        List<String> list = new ArrayList<String>();
        for (String s : values) {
            list.add(s);
        }
        // TODO use hocon mapper for writes when available
        ObjectMapper jsonmapper = new ObjectMapper();
        ObjectMapper hoconmapper = new ObjectMapper(new HoconFactory());
        Container c1 = new Container();
        c1.list = list;
        
        String json = jsonmapper.writeValueAsString(c1);
        System.out.println(list + " => " + json);
        Container c2 = hoconmapper.readValue(json, Container.class);
        
        assertEquals(c1,c2);
    }


    public static class Container {
        public List<String> list;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((list == null) ? 0 : list.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Container other = (Container) obj;
            if (list == null) {
                if (other.list != null)
                    return false;
            } else if (!list.equals(other.list))
                return false;
            return true;
        }
    }
}
