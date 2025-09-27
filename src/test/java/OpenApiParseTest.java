import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.*;


public class OpenApiParseTest {
    URL specUrl = Objects.requireNonNull(
            getClass().getClassLoader().getResource("openapi.yml"));

    // get and validate the openapi model/object.
    OpenApi3 api = new OpenApi3Parser().parse(specUrl, true);

    public OpenApiParseTest() throws ResolutionException, ValidationException {
    }

    // basic assert：get api model successfully
    @Test
    void testAPIexist() {
        assertNotNull(api, "Object should not be null");
    }

    //basic info validation
    @Test
    void testInfo(){
        assertEquals("a github API", api.getInfo().getTitle(),"need a correct info");
        assertEquals("1.0.2", api.getInfo().getVersion(),"correct version is 1.0.2");
        assertNotNull(api.getInfo().getDescription());
        assertNotNull(api.getInfo().getContact());
    }

    //Paths and Operations validation
    @Test
    void testPaths(){
        Map paths = api.getPaths();
        assertTrue(paths.containsKey("/api/issues"));
        assertTrue(paths.containsKey("/api/issues/{issueId}"));
        assertTrue(paths.containsKey("/api/issues/{issueId}/comments"));
        assertTrue(paths.containsKey("/api/comments/{commentId}"));
        assertTrue(paths.containsKey("/api/webhooks/github"));
    }
    @Test
    void testOperations(){
        Path path1 = api.getPath("/api/issues");
        assertNotNull(path1.getGet());
        assertNotNull(path1.getPost());
        Path path2 = api.getPath("/api/issues/{issueId}");
        assertNotNull(path2.getGet());
        assertNotNull(path2.getPatch());
        Path path3 = api.getPath("/api/issues/{issueId}/comments");
        assertNotNull(path3.getGet());
        assertNotNull(path3.getPost());
        Path path4 = api.getPath("/api/comments/{commentId}");
        assertNotNull(path4.getPatch());
        assertNotNull(path4.getDelete());
        Path path5 = api.getPath("/api/webhooks/github");
        assertNotNull(path5.getPost());
    }

    //Request & Response validation
    @Test
    void testRequestParameter() {
        Map<String, Parameter> para = api.getComponents().getParameters();
        assertTrue(para.values().stream().anyMatch((Parameter p) -> "path".equals(p.getIn())));
        assertTrue(para.values().stream().anyMatch((Parameter p) -> "query".equals(p.getIn())));
        assertTrue(para.values().stream().anyMatch((Parameter p) -> "header".equals(p.getIn())));

        for (Parameter p: para.values()) {
            if (p.getIn().equals("path")) {
                assertTrue(p.getName().equals("issueId") || p.getName().equals("commentId"));
                assertTrue(p.getRequired(),"The issueId or commentId is required");
                assertTrue(p.getSchema().getType().equals("integer"),"The type must be integer");
            } else if (p.getIn().equals("query")) {
                assertTrue(p.getName().equals("page") || p.getName().equals("per_page") || p.getName().equals("status"));
                if (p.getName().contains("page")) {
                  assertTrue(p.getSchema().getType().equals("integer"),"The page type must be integer");
                }
                else {
                    assertTrue(p.getSchema().getType().equals("string"),"The status type must be string");
                }
            }
            else {
                assertTrue(p.getName().equals("X-GitHub-Event") || p.getName().equals("X-GitHub-Delivery") || p.getName().equals("X-Hub-Signature-256"));
                assertTrue(p.getRequired(),"The X-GitHub header is required");
                assertTrue(p.getSchema().getType().equals("string"),"The type must be string");
            }
        }
    }
    
    @Test
    void testResponseCode() {
        api.getPaths().forEach((path, item) ->
                item.getOperations().forEach((m, op) ->
                {
                    String method = m.toString().toLowerCase();
                    switch (method) {
                        case "get"    -> assertTrue(has(op, "200"), "GET " + " need 200");
                        case "post"   -> assertTrue(has(op, "201") || has(op, "200")
                                || has(op, "204"), "POST "  + " need 201/200");
                        case "put", "patch"
                                -> assertTrue(has(op, "200") || has(op, "204"), method.toUpperCase()+" "+" need 200/204");
                        case "delete" -> assertTrue(has(op, "204"), "DELETE " + " need 204");
                    }
                if (!method.equals("delete") && !path.equals("/api/webhooks/github")) {
                    assertTrue(has(op, "400"), method.toUpperCase() + " " + " should define 400");
                    assertTrue(has(op, "401"), method.toUpperCase() + " " + " should define 401");
                    assertTrue(has(op, "403"), method.toUpperCase() + " " + " should define 403");
                    assertTrue(has(op, "404"), method.toUpperCase() + " " + " should define 404");
                    assertTrue(has(op, "409"), method.toUpperCase() + " " + " should define 409");
                    assertTrue(has(op, "500"), method.toUpperCase() + " " + " should define 500");
                    assertTrue(has(op, "503"), method.toUpperCase() + " " + " should define 503");
                }
        })
     );
    }
    private boolean has(Operation op, String code) {
        if (op == null || op.getResponses() == null) return false;
        if (op.hasResponse(code)) return true;           // e.g. "200" / "201" / "400" / "404"
        if ((code.startsWith("4")) && op.hasResponse("default")) return true;
        return false;
    }





}

