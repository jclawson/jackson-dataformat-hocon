package com.jasonclawson.jackson.dataformat.hocon;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestArrays {

    private String hoconOne = "list = [\"a\", \"b\"]";
    private String hoconTwo = "list.0 = \"a\"\nlist.2 = \"b\"";
    private String hoconThree = "list = {\"2\" : \"b\", \"0\" : \"a\"}";

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

    public static class Container {
        public List<String> list;
    }
}
