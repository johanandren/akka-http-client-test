package akkatesthttp;

import static akkatesthttp.DummyHttpServer.SecureMode.HTTP;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.http.javadsl.Http;
import akka.stream.ActorMaterializer;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class HttpActorTest {
  private static ActorSystem system;
  private static ActorMaterializer materializer;
  private static DummyHttpServer dummyHttpServer = new DummyHttpServer(HTTP);


  @BeforeClass
  public static void beforeClass() throws Exception {
    dummyHttpServer.start();
    system = ActorSystem.create("test");
    materializer = ActorMaterializer.create(system);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    JavaTestKit.shutdownActorSystem(system);
    dummyHttpServer.stop();
  }

  @Test
  public void testHttpActorOk() {
    new JavaTestKit(system) {{
      ActorRef fsmActor = system.actorOf(HttpActor.props(materializer));

      watch(fsmActor);

      send(fsmActor, new HttpActor.StartIndication());

      expectMsgClass(Duration.create(5, TimeUnit.SECONDS), HttpActor.HttpOk.class);

      expectMsgClass(Terminated.class);
    }};
  }

  @Test
  public void testMoCallError404() {
    new JavaTestKit(system) {{
      ActorRef fsmActor = system.actorOf(HttpActor.props(materializer));

      watch(fsmActor);

      send(fsmActor, new HttpActor.StartIndication("404404","404404"));

      expectMsgClass(Duration.create(5, TimeUnit.SECONDS), HttpActor.HttpAbort.class);

      expectMsgClass(Terminated.class);
    }};
  }


}
