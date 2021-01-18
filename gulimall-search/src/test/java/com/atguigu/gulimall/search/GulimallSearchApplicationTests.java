package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import net.minidev.json.JSONArray;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    /**
     * Copyright 2020 bejson.com
     */
    @Data
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

        @Override
        public String toString() {
            return "Account{" +
                    "account_number=" + account_number +
                    ", balance=" + balance +
                    ", firstname='" + firstname + '\'' +
                    ", lastname='" + lastname + '\'' +
                    ", age=" + age +
                    ", gender='" + gender + '\'' +
                    ", address='" + address + '\'' +
                    ", employer='" + employer + '\'' +
                    ", email='" + email + '\'' +
                    ", city='" + city + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }

    @Test
    void contextLoads() {
        System.out.println(client);
    }

    @Test
    void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
//        indexRequest.source("userName", "William", "age", 18, "Gender", "Male");
        User user = new User();
        user.setAge(18);
        user.setUserName("William");
        user.setGender("M");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);

        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    @Test
    void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
//
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);

        AvgAggregationBuilder aveAgg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(aveAgg);

        System.out.println(searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

//        System.out.println(searchResponse.toString());

//        Map map = JSON.parseObject(searchResponse.toString(), Map.class);
//        System.out.println(map);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String string = hit.getSourceAsString();
//            System.out.println(string);
            Account account = JSON.parseObject(string, Account.class);
//            System.out.println(account);
        }

        Aggregations aggregations = searchResponse.getAggregations();
        for (Aggregation aggregation:aggregations) {
            System.out.println(aggregation.getName());
        }

//        Map<String, Aggregation> aggregationMap = aggregations.getAsMap();
//        for (String key:aggregationMap.keySet()) {
//            if (key.equals("ageAgg")) {
//                Terms ageAgg1 = (Terms) aggregationMap.get(key);
//                System.out.println(ageAgg1.toString());
//                for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
//            String keyAsString = bucket.getKeyAsString();
//                    System.out.println(keyAsString);
//                }
//            }
//
//        }
//        Terms ageAgg1 = (Terms) aggregationMap.get("ageAgg");
//        System.out.println(aggregationMap.toString());
//        for (String keys:aggregationMap.keySet()) {
//            System.out.println(keys.getClass());
//        }
        Terms ageAgg1 =
                aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println(keyAsString);
        }
//
        Avg balanceAgg = aggregations.get("balanceAvg");
        System.out.println(balanceAgg.getValue());

    }

    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;

    }

}
