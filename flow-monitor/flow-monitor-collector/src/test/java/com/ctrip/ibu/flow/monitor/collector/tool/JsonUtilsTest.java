package com.ctrip.ibu.flow.monitor.collector.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Iancy
 * @date 2022/3/5
 */
public class JsonUtilsTest {

    @Test
    public void testReadTree() {
        String jsonStr = "{\n" +
                "\t\"id\":1,\n" +
                "\t\"name\":\"test\",\n" +
                "\t\"score\" : [1,2,3]\n" +
                "}";

        JsonNode jsonNode = JsonUtils.readTree(jsonStr);
        Assert.assertEquals(jsonNode.get("id").intValue(),1);
        Assert.assertEquals(jsonNode.get("name").textValue(), "test");
        Assert.assertEquals(jsonNode.get("score").get(1).intValue(), 2);
    }

    @Test()
    public void readTreeFail() {
        String jsonStr = "{111";
        JsonUtils.readTree(jsonStr);
    }

    @Test
    public void testReadValues() {
        User u = new User(1, "hahaha");

        String str1 = JsonUtils.toJson(Lists.newArrayList(u, u, u));
        String str2 = JsonUtils.toJson(u);
        List<User> users = JsonUtils.readValues(str1,User.class);
        assertEquals(3, users.size());

        User user=JsonUtils.readValue(str2,User.class);
        Assert.assertNotNull(user);
    }

    @Getter
    @Setter
    public static class User {
        Integer id;
        String name;

        public User() {
        }

        public User(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}