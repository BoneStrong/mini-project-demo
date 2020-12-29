package com.dzz.graphql;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author zoufeng
 * @date 2020-12-16
 */
public class HelloGraphQlController {

    class Book{
        private Long id;
        private String title;
        private String isbn;
        private Integer pageCount;
        private long authorId;

        public Long getId() {
            return id;
        }

        public Book setId(Long id) {
            this.id = id;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Book setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getIsbn() {
            return isbn;
        }

        public Book setIsbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Integer getPageCount() {
            return pageCount;
        }

        public Book setPageCount(Integer pageCount) {
            this.pageCount = pageCount;
            return this;
        }

        public long getAuthorId() {
            return authorId;
        }

        public Book setAuthorId(long authorId) {
            this.authorId = authorId;
            return this;
        }

    }

    @GetMapping("/hello")
    @GraphqlMapping
    public Object hello(@RequestParam(name = "id", required = false)
                        @GraphqlParam("id") Long id) {
        return new Book().setId(id).setTitle("银瓶梅");
    }

    @RequestMapping(value = "/hello2", method = RequestMethod.GET)
    @GraphqlMapping
    public Object hello2() throws UnsupportedEncodingException {
        return new Book().setId(2l).setTitle("铁瓶梅");
    }


    private static final String findBookByIdString = "query ($id:Long!){\n" +
            "    findBookById(id:$id){\n" +
            "    id\n" +
            "    title\n" +
            "    isbn\n" +
            "    pageCount\n" +
            "    author{\n" +
            "        id\n" +
            "        createdTime\n" +
            "        firstName\n" +
            "        lastName\n" +
            "    }\n" +
            "}}";


    private String encode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "utf-8");
    }

/*    @Before
    public void setupMockMvc() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }


    public static final String PING_AN_GRAPHQL_HEAD = "GRAPHQL_PROJECTION";

    @Test
    public void testFindBookById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/hello?id=" + 1)
                .header(PING_AN_GRAPHQL_HEAD, encode(findBookByIdString)) //graphql语句放置请求头
                .contentType(MediaType.ALL_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }*/

/*    返回graphql查询结果

    {
        "data": {
        "findBookById": {
            "id": 1,
                    "title": "jiingmei",
                    "isbn": "hhh",
                    "pageCount": 1,
                    "author": {
                "id": 1,
                        "createdTime": "2020-03-23 16:51:46",
                        "firstName": "he",
                        "lastName": "he"
            }
        }
    }
    }
    不加如请求头则是mvc的返回

    {
        "id": 1,
            "title": "水浒传",
            "isbn": null,
            "pageCount": null,
            "authorId": 0
    }*/
}
