import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.util.features.CouchbaseFeature;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;
import com.couchbase.client.java.query.N1qlQuery;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
public class MainVertex extends AbstractVerticle{

    Bucket bucket;


    public static void main(String[] args) {


        Launcher.executeCommand("run",MainVertex.class.getName());

    }
    private static ObjectMapper objectMapper=new ObjectMapper();


    @Override
    public void start(Future<Void> fut) throws IOException {

        //  createSomeData();
        CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
                //this set the IO socket timeout globally, to 45s
                .socketConnectTimeout((int) TimeUnit.SECONDS.toMillis(10000))
                //this sets the connection timeout for openBucket calls globally (unless a particular call provides its own timeout)
                .connectTimeout(TimeUnit.SECONDS.toMillis(12000))
                .build();






        Cluster cluster= CouchbaseCluster.create(env,"http://127.0.0.1:8091");
        bucket=cluster.openBucket("default","" );
        System.out.println("bucket open........");
        JsonObject products=JsonObject.create();
        JsonDocument document=JsonDocument.create("cb2",products);
        Router router = Router.router(vertx);

        ObjectMapper objectMapper=new ObjectMapper();
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });


        router.route("/assets/all").handler(BodyHandler.create());


        router.post("/assets/all").handler(this::addOne1);
        router.get("/assets/all").handler(this::getAll);

        router.get("/assets/all/:id").handler(this::finone);

        router.delete("/assets/all/:id").handler(this::deleteone);


        router.route("/assets/all/:id").handler(BodyHandler.create());
        router.put("/assets/all/:id").handler(this::update1);


        //router.get("/assets/all/:id1/:id2").handler(this::findBetween);

        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        8972,
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
    }


    private void addOne1(RoutingContext routingContext)  {
        String s1=routingContext.getBodyAsString();

        Whisky whisky=null;
        try{
            whisky = new ObjectMapper().readValue(s1, Whisky.class);

        }catch (Exception e){
            e.getCause();
        }

        System.out.println("NEW VALUE........"+s1);

        final JsonObject jsonObject = JsonObject.fromJson(s1);
        JsonDocument jsonDocument=JsonDocument.create(String.valueOf(whisky.getId()),jsonObject);

        bucket.upsert(jsonDocument);

        routingContext.response().setStatusCode(209)
                .putHeader("content-type","application/json; charset=utf-8")
                .end(s1);

    }

    private void getAll(RoutingContext routingContext) {

        HashMap<String,String> logParameters=new HashMap<>();
        logParameters.putAll(routingContext.request().params().entries().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        /*N1qlQuery qlQuery=N1qlQuery.simple("SELECT * FROM default");
        System.out.println(qlQuery);
        N1qlQueryResult n1qlQueryResult=bucket.query(qlQuery);
        System.out.println(n1qlQueryResult);*/


        System.out.println (logParameters );


        /*routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(n1qlQueryResult.allRows().toString());*/
    }

    private void finone(RoutingContext routingContext){
        String id1=routingContext.request().getParam("id");
        System.out.println(id1);
        JsonDocument jsonDocument= bucket.get(id1);
        System.out.println(jsonDocument);
        System.out.println(".............................");
        JsonObject jsonObject = jsonDocument.content();
        System.out.println(jsonObject);
        routingContext.response().setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(jsonObject.toString());
    }

    private void deleteone(RoutingContext routingContext) {
        String s1=routingContext.request().getParam("id");
        System.out.println(s1);

        JsonDocument jsonDocument=bucket.remove(s1);
        System.out.println(jsonDocument.content());

        routingContext.response().setStatusCode(200)
                .putHeader("content-type","application/json; charset=utf-8")
                .end(jsonDocument.content().toString());

    }


    private void update1(RoutingContext routingContext) {


        String s1=routingContext.getBodyAsString();
        System.out.println("value of passing body ...............");
        System.out.println("VALUE..............."+s1);
        Whisky whisky=null;
        try{
            whisky = new ObjectMapper().readValue(s1, Whisky.class);

        }catch (Exception e){
            e.getCause();
        }
        System.out.println(whisky.getId());


        JsonObject jsonObject=JsonObject.fromJson(s1);

        JsonDocument jsonDocument=JsonDocument.create(String.valueOf(whisky.getId()),jsonObject);
        bucket.upsert(jsonDocument);

        routingContext.response().setStatusCode(201)

                .putHeader("content-type", "application/json; charset=utf-8")
                .end(jsonDocument.content().toString());
    }

}


