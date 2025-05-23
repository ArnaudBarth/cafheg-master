package ch.hearc.cafheg.infrastructure.persistance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
  /** Pool de connections JDBC */
  private static DataSource dataSource;

  /** Connection JDBC active par utilisateur/thread (ThreadLocal) */
  private static final ThreadLocal<Connection> connection = new ThreadLocal<>();
  private static final ThreadLocal<Connection> testConnection = new ThreadLocal<>();

  public static void setTestConnection(Connection conn) {
    testConnection.set(conn);
  }

  private static final Logger logger = LoggerFactory.getLogger(Database.class);

  /**
   * Retourne la transaction active ou throw une Exception si pas de transaction
   * active.
   * @return Connection JDBC active
   */
  static Connection activeJDBCConnection() {
    if (testConnection.get() != null) {
      return testConnection.get();
    }
    if(connection.get() == null) {
      logger.error("Aucune connexion JDBC active pour ce thread");
      throw new RuntimeException("Pas de connection JDBC active");
    }
    return connection.get();
  }

  /**
   * Exécution d'une fonction dans une transaction.
   * @param inTransaction La fonction a éxécuter au travers d'une transaction
   * @param <T> Le type du retour de la fonction
   * @return Le résultat de l'éxécution de la fonction
   */
  public static <T> T inTransaction(Supplier<T> inTransaction) {
    logger.debug("inTransaction#start");
    try {
      logger.debug("inTransaction#getConnection");
      connection.set(dataSource.getConnection());
      T result = inTransaction.get();
      logger.debug("inTransaction#getConnection");
      return result;
    } catch (Exception e) {
      logger.error("Erreur pendant l'exécution de la transaction", e);
      throw new RuntimeException(e);
    } finally {
      try {
        if (connection.get() != null) {
          connection.get().close();
          logger.error("inTransaction#closeConnection");
        }
      } catch (SQLException e) {
        logger.error("Erreur lors de la fermeture de la connexion JDBC", e);
        throw new RuntimeException(e);
      } finally {
        connection.remove();
        logger.debug("inTransaction#end");
      }
    }
  }

  DataSource dataSource() {
    return dataSource;
  }

  /**
   * Initialisation du pool de connections.
   */
  public void start() {
    logger.info("Initializing datasource");

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:sample");
    logger.debug("JDBC URL utilisée : {}", config.getJdbcUrl());

    config.setMaximumPoolSize(20);
    config.setDriverClassName("org.h2.Driver");
    dataSource = new HikariDataSource(config);

    logger.info("Datasource initialized");
  }
}