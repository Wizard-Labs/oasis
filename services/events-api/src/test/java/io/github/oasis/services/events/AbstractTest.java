package io.github.oasis.services.events;

import io.github.oasis.services.events.utils.TestDispatcherFactory;
import io.github.oasis.services.events.utils.TestDispatcherService;
import io.github.oasis.services.events.utils.TestDispatcherVerticle;
import io.github.oasis.services.events.utils.TestRedisDeployVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@ExtendWith(VertxExtension.class)
public abstract class AbstractTest {

    public static final int TEST_PORT = 8010;

    protected TestDispatcherService dispatcherService;

    @BeforeEach
    void beforeEach(Vertx vertx, VertxTestContext testContext) {
        JsonObject dispatcherConf = new JsonObject().put("impl", "test:any").put("configs", new JsonObject());
        JsonObject testConfigs = new JsonObject()
                .put("http", new JsonObject().put("instances", 1).put("port", TEST_PORT))
                .put("oasis", new JsonObject().put("dispatcher", dispatcherConf));
        dispatcherService = Mockito.spy(new TestDispatcherService());
        TestDispatcherVerticle dispatcherVerticle = new TestDispatcherVerticle(dispatcherService);
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new TestDispatcherFactory(dispatcherVerticle));
        vertx.deployVerticle(new EventsApi(), options, testContext.completing());
    }

    protected void sleepWell() {
        String relaxTime = System.getenv("OASIS_RELAX_TIME");
        if (Objects.nonNull(relaxTime)) {
            long slp = Long.parseLong(relaxTime);
            if (slp > 0) {
                try {
                    Thread.sleep(slp);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
    }

    protected void awaitRedisInitialization(Vertx vertx, VertxTestContext testContext, TestRedisDeployVerticle verticle) {
        vertx.deployVerticle(verticle, testContext.succeeding());
        sleepWell();
    }

    protected HttpRequest<Buffer> callToEndPoint(String endPoint, Vertx vertx) {
        WebClient client = WebClient.create(vertx);
        return client.get(TEST_PORT, "localhost", endPoint);
    }

}
