package org.cdpg.dx.tgdex.validator;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdpg.dx.database.elastic.ElasticClient;
import org.cdpg.dx.tgdex.apiserver.ControllerFactory;
import org.cdpg.dx.tgdex.validator.service.ValidatorService;
import org.cdpg.dx.tgdex.validator.service.ValidatorServiceImpl;

import static org.cdpg.dx.tgdex.util.Constants.*;


/**
 * The Validator Verticle.
 *
 * <h1>Validator Verticle</h1>
 *
 * <p>The Validator Verticle implementation in the IUDX Catalogue Server exposes the {@link
 * ValidatorService} over the Vert.x Event Bus.
 *
 * @version 1.0
 * @since 2020-05-31
 */
public class ValidatorVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LogManager.getLogger(ValidatorVerticle.class);

  private ValidatorService validator;
  private String databaseIp;
  private String docIndex;
  private int databasePort;
  private String databaseUser;
  private String databasePassword;
  private ElasticClient client;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private boolean isUacInstance;
  private String vocContext;

  /**
   * This method is used to start the Verticle. It deploys a verticle in a cluster, registers the
   * service with the Event bus against an address, publishes the service with the service discovery
   * interface.
   */
  @Override
  public void start() throws Exception {
    binder = new ServiceBinder(vertx);
    databaseIp = config().getString(DATABASE_IP);
    databasePort = config().getInteger(DATABASE_PORT);
    databaseUser = config().getString(DATABASE_UNAME);
    databasePassword = config().getString(DATABASE_PASSWD);
    docIndex = config().getString(DOC_INDEX);

    /* Create a reference to HazelcastClusterManager. */

    client = new ElasticClient(databaseIp, databasePort, databaseUser, databasePassword);

    /* Create or Join a Vert.x Cluster. */

    /* Publish the Validator service with the Event Bus against an address. */

    validator = new ValidatorServiceImpl(client, docIndex, isUacInstance, vocContext);
    consumer =
        binder.setAddress(VALIDATOR_SERVICE_ADDRESS)
      .register(ValidatorService.class, validator);
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
